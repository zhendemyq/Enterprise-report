package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReportGenerateDTO;
import com.example.backend.dto.ReportRecordQueryDTO;
import com.example.backend.entity.ReportRecord;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportRecordMapper;
import com.example.backend.service.PdfConvertService;
import com.example.backend.service.ReportDatasourceService;
import com.example.backend.service.ReportGenerateService;
import com.example.backend.service.ReportTemplateService;
import com.example.backend.vo.ReportRecordVO;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 报表生成服务实现
 */
@Service
public class ReportGenerateServiceImpl extends ServiceImpl<ReportRecordMapper, ReportRecord> implements ReportGenerateService {

    private static final Logger logger = LoggerFactory.getLogger(ReportGenerateServiceImpl.class);

    @Autowired
    private ReportTemplateService templateService;

    @Autowired
    private ReportDatasourceService datasourceService;

    @Autowired
    private PdfConvertService pdfConvertService;

    @Value("${report.storage.path:./upload/reports}")
    private String storagePath;

    @Value("${report.generate.max-rows:100000}")
    private int maxRows;

    @Value("${report.generate.page-size:5000}")
    private int pageSize;

    @Override
    public ReportRecordVO generateReport(ReportGenerateDTO generateDTO) {
        // 获取模板
        ReportTemplate template = templateService.getById(generateDTO.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        // 创建生成记录
        ReportRecord record = new ReportRecord();
        record.setTemplateId(template.getId());
        record.setTemplateName(template.getTemplateName());
        record.setReportName(StringUtils.isNotBlank(generateDTO.getReportName()) 
                ? generateDTO.getReportName() 
                : template.getTemplateName() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        record.setGenerateParams(generateDTO.getParams());
        record.setFileType(generateDTO.getFileType());
        record.setStatus(0); // 生成中
        record.setStartTime(LocalDateTime.now());
        save(record);

        try {
            // 查询数据
            List<Map<String, Object>> dataList = queryReportData(template, generateDTO.getParams());
            record.setDataRows(dataList.size());

            // 检查数据量
            if (dataList.size() > maxRows) {
                throw new BusinessException(ResultCode.REPORT_TOO_LARGE, 
                        "数据量超过限制，当前: " + dataList.size() + "行，最大: " + maxRows + "行");
            }

            // 生成报表文件
            String fileName = generateReportFile(template, dataList, generateDTO.getFileType());
            record.setFilePath(fileName);
            
            File file = new File(storagePath, fileName);
            record.setFileSize(file.length());
            record.setStatus(1); // 成功
            record.setEndTime(LocalDateTime.now());
            record.setDuration(java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMillis());

            // 增加模板使用次数
            templateService.incrementUseCount(template.getId());

        } catch (BusinessException e) {
            record.setStatus(2); // 失败
            record.setErrorMsg(e.getMessage());
            record.setEndTime(LocalDateTime.now());
            record.setDuration(java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMillis());
            updateById(record);
            throw e;
        } catch (Exception e) {
            logger.error("报表生成失败", e);
            record.setStatus(2);
            record.setErrorMsg(e.getMessage());
            record.setEndTime(LocalDateTime.now());
            record.setDuration(java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMillis());
            updateById(record);
            throw new BusinessException(ResultCode.REPORT_GENERATE_ERROR, e.getMessage());
        }

        updateById(record);
        return convertToVO(record);
    }

    @Override
    @Async
    public Long generateReportAsync(ReportGenerateDTO generateDTO) {
        // 创建记录
        ReportTemplate template = templateService.getById(generateDTO.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        ReportRecord record = new ReportRecord();
        record.setTemplateId(template.getId());
        record.setTemplateName(template.getTemplateName());
        record.setReportName(StringUtils.isNotBlank(generateDTO.getReportName()) 
                ? generateDTO.getReportName() 
                : template.getTemplateName() + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        record.setGenerateParams(generateDTO.getParams());
        record.setFileType(generateDTO.getFileType());
        record.setStatus(0);
        record.setStartTime(LocalDateTime.now());
        save(record);

        // 异步执行生成
        try {
            List<Map<String, Object>> dataList = queryReportData(template, generateDTO.getParams());
            record.setDataRows(dataList.size());

            String fileName = generateReportFile(template, dataList, generateDTO.getFileType());
            record.setFilePath(fileName);

            File file = new File(storagePath, fileName);
            record.setFileSize(file.length());
            record.setStatus(1);
            record.setEndTime(LocalDateTime.now());
            record.setDuration(java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMillis());

            // 增加模板使用次数
            templateService.incrementUseCount(template.getId());
        } catch (Exception e) {
            logger.error("异步报表生成失败", e);
            record.setStatus(2);
            record.setErrorMsg(e.getMessage());
            record.setEndTime(LocalDateTime.now());
            record.setDuration(java.time.Duration.between(record.getStartTime(), record.getEndTime()).toMillis());
        }
        updateById(record);

        return record.getId();
    }

    @Override
    public void previewReport(Long recordId, HttpServletResponse response) {
        ReportRecord record = getById(recordId);
        if (record == null || record.getStatus() != 1) {
            throw new BusinessException("报表不存在或未生成完成");
        }

        File file = new File(storagePath, record.getFilePath());
        if (!file.exists()) {
            throw new BusinessException("报表文件不存在");
        }

        try {
            String fileType = record.getFileType().toLowerCase();
            String contentType;
            String extension;

            switch (fileType) {
                case "pdf":
                    contentType = "application/pdf";
                    extension = ".pdf";
                    break;
                case "xlsx":
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    extension = ".xlsx";
                    break;
                case "csv":
                    contentType = "text/csv; charset=UTF-8";
                    extension = ".csv";
                    break;
                default:
                    throw new BusinessException("不支持的文件格式预览: " + fileType);
            }

            response.setContentType(contentType);

            String encodedFileName = URLEncoder.encode(record.getReportName() + extension, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            response.setHeader("Content-Disposition", "inline; filename=\"" + encodedFileName + "\"");
            response.setContentLengthLong(file.length());
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);

            try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
                IoUtil.copy(inputStream, response.getOutputStream());
                response.getOutputStream().flush();
            }
        } catch (IOException e) {
            logger.error("预览失败: recordId={}, file={}", recordId, file.getAbsolutePath(), e);
            throw new BusinessException("预览失败: " + e.getMessage());
        }
    }

    @Override
    public void downloadReport(Long recordId, HttpServletResponse response) {
        ReportRecord record = getById(recordId);
        if (record == null || record.getStatus() != 1) {
            throw new BusinessException("报表不存在或未生成完成");
        }

        File file = new File(storagePath, record.getFilePath());
        if (!file.exists()) {
            throw new BusinessException("报表文件不存在");
        }

        try {
            String contentType = "xlsx".equals(record.getFileType()) 
                    ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    : "application/pdf";
            response.setContentType(contentType);
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                    URLEncoder.encode(record.getReportName() + "." + record.getFileType(), StandardCharsets.UTF_8));
            IoUtil.copy(new FileInputStream(file), response.getOutputStream());

            // 更新下载次数
            record.setDownloadCount(record.getDownloadCount() == null ? 1 : record.getDownloadCount() + 1);
            updateById(record);
        } catch (IOException e) {
            throw new BusinessException("下载失败: " + e.getMessage());
        }
    }

    @Override
    public IPage<ReportRecordVO> pageRecords(ReportRecordQueryDTO queryDTO) {
        Page<ReportRecord> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(queryDTO.getTemplateId() != null, ReportRecord::getTemplateId, queryDTO.getTemplateId())
                .like(StringUtils.isNotBlank(queryDTO.getTemplateName()), 
                    ReportRecord::getTemplateName, queryDTO.getTemplateName())
                .eq(queryDTO.getStatus() != null, ReportRecord::getStatus, queryDTO.getStatus())
                .eq(StringUtils.isNotBlank(queryDTO.getFileType()), ReportRecord::getFileType, queryDTO.getFileType())
                .ge(queryDTO.getStartTime() != null, ReportRecord::getCreateTime, queryDTO.getStartTime())
                .le(queryDTO.getEndTime() != null, ReportRecord::getCreateTime, queryDTO.getEndTime())
                .eq(ReportRecord::getCreateBy, StpUtil.getLoginIdAsLong())
                .orderByDesc(ReportRecord::getCreateTime);

        IPage<ReportRecord> recordPage = page(page, wrapper);
        return recordPage.convert(this::convertToVO);
    }

    @Override
    public ReportRecordVO getRecordDetail(Long id) {
        ReportRecord record = getById(id);
        if (record == null) {
            throw new BusinessException("记录不存在");
        }
        return convertToVO(record);
    }

    @Override
    public void deleteRecord(Long id) {
        ReportRecord record = getById(id);
        if (record != null && StringUtils.isNotBlank(record.getFilePath())) {
            // 删除文件
            File file = new File(storagePath, record.getFilePath());
            FileUtil.del(file);
        }
        removeById(id);
    }

    @Override
    public ReportRecordVO regenerateReport(Long recordId) {
        ReportRecord oldRecord = getById(recordId);
        if (oldRecord == null) {
            throw new BusinessException("记录不存在");
        }

        ReportGenerateDTO generateDTO = new ReportGenerateDTO();
        generateDTO.setTemplateId(oldRecord.getTemplateId());
        generateDTO.setReportName(oldRecord.getReportName());
        generateDTO.setParams(oldRecord.getGenerateParams());
        generateDTO.setFileType(oldRecord.getFileType());

        return generateReport(generateDTO);
    }

    /**
     * 查询报表数据
     */
    private List<Map<String, Object>> queryReportData(ReportTemplate template, Map<String, Object> params) {
        if (template.getDatasourceId() == null || StringUtils.isBlank(template.getQuerySql())) {
            throw new BusinessException("模板数据源或SQL未配置");
        }
        return datasourceService.executeQuery(template.getDatasourceId(), template.getQuerySql(), params);
    }

    /**
     * 生成报表文件
     */
    private String generateReportFile(ReportTemplate template, List<Map<String, Object>> dataList, String fileType) {
        // 确保目录存在
        FileUtil.mkdir(storagePath);

        String fileName = UUID.randomUUID().toString() + "." + fileType;
        String filePath = storagePath + File.separator + fileName;

        if ("xlsx".equals(fileType)) {
            // 如果有模板文件，使用Jxls渲染；否则使用EasyExcel
            if (StringUtils.isNotBlank(template.getTemplateFile())) {
                generateExcelWithJxls(template, dataList, filePath);
            } else {
                generateExcel(template, dataList, filePath);
            }
        } else if ("pdf".equals(fileType)) {
            // 先生成Excel再转PDF
            String tempExcel = storagePath + File.separator + UUID.randomUUID().toString() + ".xlsx";
            if (StringUtils.isNotBlank(template.getTemplateFile())) {
                generateExcelWithJxls(template, dataList, tempExcel);
            } else {
                generateExcel(template, dataList, tempExcel);
            }

            // 转换PDF，成功后清理临时文件，失败时保留用于调试
            convertToPdfWithCleanup(tempExcel, filePath);
        } else if ("csv".equals(fileType)) {
            generateCsv(dataList, filePath);
        }

        return fileName;
    }

    /**
     * 使用Jxls模板引擎生成Excel
     * 模板中使用 jx:each 标签定义循环区域
     * 示例: jx:each(items="dataList" var="item" lastCell="D2")
     */
    private void generateExcelWithJxls(ReportTemplate template, List<Map<String, Object>> dataList, String outputPath) {
        String templatePath = storagePath + File.separator + template.getTemplateFile();
        File templateFile = new File(templatePath);

        if (!templateFile.exists()) {
            throw new BusinessException(ResultCode.TEMPLATE_FILE_NOT_FOUND, "模板文件不存在: " + template.getTemplateFile());
        }

        try (InputStream is = new FileInputStream(templateFile);
             OutputStream os = new FileOutputStream(outputPath)) {

            Context context = new Context();
            context.putVar("dataList", dataList);
            context.putVar("reportName", template.getTemplateName());
            context.putVar("generateTime", LocalDateTime.now());

            // 添加参数配置中的变量
            if (template.getParamConfig() != null) {
                for (Map<String, Object> param : template.getParamConfig()) {
                    String paramName = (String) param.get("name");
                    Object paramValue = param.get("value");
                    if (StringUtils.isNotBlank(paramName) && paramValue != null) {
                        context.putVar(paramName, paramValue);
                    }
                }
            }

            JxlsHelper.getInstance().processTemplate(is, os, context);
            logger.info("Jxls模板渲染完成: template={}, output={}", templatePath, outputPath);

        } catch (IOException e) {
            logger.error("Jxls模板渲染失败: template={}", templatePath, e);
            throw new BusinessException(ResultCode.REPORT_GENERATE_ERROR, "模板渲染失败: " + e.getMessage());
        }
    }

    /**
     * 将Excel转换为PDF，并处理临时文件清理
     * 成功时删除临时Excel文件，失败时保留用于调试
     * @param tempExcelPath 临时Excel文件路径
     * @param pdfPath PDF输出路径
     * @throws BusinessException 转换失败时抛出PDF_CONVERT_ERROR
     */
    private void convertToPdfWithCleanup(String tempExcelPath, String pdfPath) {
        try {
            convertToPdf(tempExcelPath, pdfPath);
            // 转换成功，删除临时Excel文件
            boolean deleted = FileUtil.del(tempExcelPath);
            if (deleted) {
                logger.info("临时Excel文件已清理: {}", tempExcelPath);
            } else {
                logger.warn("临时Excel文件清理失败: {}", tempExcelPath);
            }
        } catch (BusinessException e) {
            // 转换失败，保留临时文件用于调试
            logger.error("PDF转换失败，保留临时Excel文件用于调试: {}", tempExcelPath, e);
            // Re-throw with specific PDF conversion error code
            throw new BusinessException(ResultCode.PDF_CONVERT_ERROR, e.getMessage());
        } catch (Exception e) {
            // 转换失败，保留临时文件用于调试
            logger.error("PDF转换失败，保留临时Excel文件用于调试: {}", tempExcelPath, e);
            throw new BusinessException(ResultCode.PDF_CONVERT_ERROR, "PDF转换失败: " + e.getMessage());
        }
    }

    /**
     * 从模板配置中提取字段布局
     * @param template 报表模板
     * @return 字段布局列表，每个元素包含 field(字段名) 和 label(显示名称)
     */
    private List<Map<String, String>> extractFieldLayout(ReportTemplate template) {
        Map<String, Object> templateConfig = template.getTemplateConfig();
        if (templateConfig == null) {
            return null;
        }

        // 尝试从 spreadsheetData.cellData 中提取字段布局
        Map<String, Object> spreadsheetData = (Map<String, Object>) templateConfig.get("spreadsheetData");
        if (spreadsheetData == null) {
            // 兼容旧格式：直接从 cellData 中提取
            spreadsheetData = (Map<String, Object>) templateConfig.get("cellData");
            if (spreadsheetData == null) {
                return null;
            }
        }

        Map<String, Object> cellData = (Map<String, Object>) spreadsheetData.get("cellData");
        if (cellData == null) {
            cellData = spreadsheetData; // 兼容直接存储 cellData 的情况
        }

        // 解析单元格数据，提取字段映射
        List<Map<String, String>> fieldMappings = new java.util.ArrayList<>();
        java.util.regex.Pattern fieldPattern = java.util.regex.Pattern.compile("\\$\\{(\\w+)\\}");

        for (Map.Entry<String, Object> entry : cellData.entrySet()) {
            String cellRef = entry.getKey();
            Object value = entry.getValue();
            if (value == null) continue;

            String cellValue = value.toString();
            java.util.regex.Matcher matcher = fieldPattern.matcher(cellValue);
            if (matcher.find()) {
                String fieldName = matcher.group(1);

                // 提取列号
                String col = cellRef.replaceAll("\\d+", "");
                int row = Integer.parseInt(cellRef.replaceAll("[A-Z]+", ""));

                // 查找该列的表头（假设表头在第1行）
                String headerCell = col + "1";
                Object headerValue = cellData.get(headerCell);
                String label = fieldName;
                if (headerValue != null) {
                    String headerStr = headerValue.toString();
                    if (!headerStr.startsWith("${")) {
                        label = headerStr;
                    }
                }

                Map<String, String> mapping = new java.util.HashMap<>();
                mapping.put("field", fieldName);
                mapping.put("label", label);
                mapping.put("col", col);
                fieldMappings.add(mapping);
            }
        }

        // 按列排序
        String colOrder = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        fieldMappings.sort((a, b) -> colOrder.indexOf(a.get("col")) - colOrder.indexOf(b.get("col")));

        return fieldMappings.isEmpty() ? null : fieldMappings;
    }

    /**
     * 生成Excel文件（使用EasyExcel流式写入）
     */
    private void generateExcel(ReportTemplate template, List<Map<String, Object>> dataList, String filePath) {
        if (dataList.isEmpty()) {
            // 空数据时创建空文件
            try (ExcelWriter excelWriter = EasyExcel.write(filePath).build()) {
                WriteSheet writeSheet = EasyExcel.writerSheet("报表数据").build();
                excelWriter.write(List.of(), writeSheet);
            }
            return;
        }

        // 尝试从模板配置中提取字段布局
        List<Map<String, String>> fieldLayout = extractFieldLayout(template);

        List<String> headers;
        List<String> fields;

        if (fieldLayout != null && !fieldLayout.isEmpty()) {
            // 使用设计器布局
            headers = fieldLayout.stream().map(f -> f.get("label")).toList();
            fields = fieldLayout.stream().map(f -> f.get("field")).toList();
            logger.info("使用设计器布局生成报表，字段数: {}", fields.size());
        } else {
            // 没有设计器布局，使用所有字段
            headers = new java.util.ArrayList<>(dataList.get(0).keySet());
            fields = headers;
            logger.info("未找到设计器布局，使用全部字段生成报表，字段数: {}", fields.size());
        }

        try (ExcelWriter excelWriter = EasyExcel.write(filePath).build()) {
            WriteSheet writeSheet = EasyExcel.writerSheet("报表数据").build();

            // 先写入表头行
            List<List<Object>> headerRow = List.of(headers.stream().map(h -> (Object) h).toList());
            excelWriter.write(headerRow, writeSheet);

            // 分页写入数据，避免内存溢出
            int totalSize = dataList.size();
            int pages = (totalSize + pageSize - 1) / pageSize;

            for (int i = 0; i < pages; i++) {
                int fromIndex = i * pageSize;
                int toIndex = Math.min(fromIndex + pageSize, totalSize);
                List<Map<String, Object>> pageData = dataList.subList(fromIndex, toIndex);

                // 转换为List<List<Object>>格式，按字段顺序获取值
                List<String> finalFields = fields;
                List<List<Object>> writeData = pageData.stream()
                        .map(row -> finalFields.stream().map(f -> row.get(f)).toList())
                        .toList();

                excelWriter.write(writeData, writeSheet);
            }
        }
    }

    /**
     * 生成CSV文件
     */
    private void generateCsv(List<Map<String, Object>> dataList, String filePath) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.OutputStreamWriter(
                new java.io.FileOutputStream(filePath), java.nio.charset.StandardCharsets.UTF_8))) {
            // 写入BOM以支持Excel正确识别UTF-8
            writer.write('\ufeff');

            if (dataList.isEmpty()) {
                return;
            }

            // 写入表头
            List<String> headers = new java.util.ArrayList<>(dataList.get(0).keySet());
            writer.println(String.join(",", headers));

            // 写入数据行
            for (Map<String, Object> row : dataList) {
                List<String> values = headers.stream()
                        .map(h -> {
                            Object val = row.get(h);
                            if (val == null) return "";
                            String str = val.toString();
                            // 处理包含逗号、引号或换行的值
                            if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
                                return "\"" + str.replace("\"", "\"\"") + "\"";
                            }
                            return str;
                        })
                        .toList();
                writer.println(String.join(",", values));
            }
        } catch (java.io.IOException e) {
            throw new BusinessException("CSV文件生成失败: " + e.getMessage());
        }
    }

    /**
     * 将Excel转换为PDF
     * @param excelPath Excel文件路径
     * @param pdfPath PDF输出路径
     */
    private void convertToPdf(String excelPath, String pdfPath) {
        logger.info("开始将Excel转换为PDF: {} -> {}", excelPath, pdfPath);
        pdfConvertService.convertExcelToPdf(excelPath, pdfPath);
        logger.info("PDF转换完成: {}", pdfPath);
    }

    private ReportRecordVO convertToVO(ReportRecord record) {
        ReportRecordVO vo = BeanUtil.copyProperties(record, ReportRecordVO.class);

        // 设置状态名称
        if (record.getStatus() != null) {
            String[] statusNames = {"生成中", "成功", "失败"};
            vo.setStatusName(statusNames[record.getStatus()]);
        }

        // 格式化文件大小
        if (record.getFileSize() != null) {
            vo.setFileSizeStr(formatFileSize(record.getFileSize()));
        }

        // 格式化耗时
        if (record.getDuration() != null) {
            vo.setDurationStr(formatDuration(record.getDuration()));
        }

        return vo;
    }

    private String formatFileSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }

    private String formatDuration(long millis) {
        if (millis < 1000) return millis + "ms";
        if (millis < 60000) return String.format("%.2fs", millis / 1000.0);
        return String.format("%.2fmin", millis / 60000.0);
    }
}
