package com.example.backend.service.impl;

import com.example.backend.exception.BusinessException;
import com.example.backend.service.PdfConvertService;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.DashedBorder;
import com.itextpdf.layout.borders.DottedBorder;
import com.itextpdf.layout.borders.DoubleBorder;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * PDF转换服务实现
 * 使用Apache POI读取Excel，iText7生成PDF
 */
@Service
public class PdfConvertServiceImpl implements PdfConvertService {

    private static final Logger logger = LoggerFactory.getLogger(PdfConvertServiceImpl.class);
    
    private static final float DEFAULT_FONT_SIZE = 10f;
    private static final float CELL_PADDING = 5f;

    @Override
    public void convertExcelToPdf(String excelPath, String pdfPath) {
        File excelFile = new File(excelPath);
        if (!excelFile.exists()) {
            throw new BusinessException("Excel文件不存在: " + excelPath);
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             FileOutputStream fos = new FileOutputStream(pdfPath)) {
            convertExcelToPdf(fis, fos);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("PDF转换失败: {}", e.getMessage(), e);
            throw new BusinessException("PDF转换失败: " + e.getMessage());
        }
    }

    @Override
    public void convertExcelToPdf(InputStream excelInputStream, OutputStream pdfOutputStream) {
        try (Workbook workbook = new XSSFWorkbook(excelInputStream)) {
            PdfWriter writer = new PdfWriter(pdfOutputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc, PageSize.A4.rotate());
            document.setMargins(20, 20, 20, 20);

            // Create font for this specific PDF document (fonts cannot be shared across documents)
            PdfFont chineseFont = createChineseFont();

            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet.getPhysicalNumberOfRows() == 0) {
                    continue;
                }

                if (sheetIndex > 0) {
                    document.add(new com.itextpdf.layout.element.AreaBreak());
                }

                processSheet(document, sheet, chineseFont);
            }

            document.close();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("PDF转换失败: {}", e.getMessage(), e);
            throw new BusinessException("PDF转换失败: " + e.getMessage());
        }
    }

    /**
     * 创建中文字体（每个PDF文档需要独立的字体实例）
     * 按优先级尝试加载字体：
     * 1. 系统字体 (SimSun/SimHei)
     * 2. 类路径下的嵌入字体
     * 3. iText内置的CJK字体 (STSong-Light)
     * 4. 默认字体作为最后备用
     */
    private PdfFont createChineseFont() {
        // 1. 尝试加载系统字体
        PdfFont font = tryLoadSystemFonts();
        if (font != null) {
            return font;
        }

        // 2. 尝试加载类路径下的嵌入字体
        font = tryLoadClasspathFonts();
        if (font != null) {
            return font;
        }

        // 3. 使用iText内置的CJK字体作为备用
        font = tryLoadBuiltInCjkFonts();
        if (font != null) {
            return font;
        }

        // 4. 最后备用：使用默认字体
        return createDefaultFont();
    }

    /**
     * 尝试加载系统字体 (SimSun/SimHei)
     */
    private PdfFont tryLoadSystemFonts() {
        // 系统字体路径列表，按优先级排序
        String[][] systemFontPaths = {
            // Windows系统字体
            {"C:/Windows/Fonts/simsun.ttc,0", "SimSun"},
            {"C:/Windows/Fonts/simhei.ttf", "SimHei"},
            {"C:/Windows/Fonts/msyh.ttc,0", "Microsoft YaHei"},
            {"C:/Windows/Fonts/msyhbd.ttc,0", "Microsoft YaHei Bold"},
            // Linux系统字体
            {"/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc,0", "WenQuanYi Zen Hei"},
            {"/usr/share/fonts/truetype/wqy/wqy-microhei.ttc,0", "WenQuanYi Micro Hei"},
            {"/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0", "Noto Sans CJK"},
            {"/usr/share/fonts/truetype/droid/DroidSansFallbackFull.ttf", "Droid Sans Fallback"},
            // macOS系统字体
            {"/System/Library/Fonts/PingFang.ttc,0", "PingFang SC"},
            {"/System/Library/Fonts/STHeiti Light.ttc,0", "STHeiti Light"},
            {"/Library/Fonts/Songti.ttc,0", "Songti SC"}
        };

        for (String[] fontInfo : systemFontPaths) {
            String fontPath = fontInfo[0];
            String fontName = fontInfo[1];
            try {
                File fontFile = new File(fontPath.contains(",") ? fontPath.substring(0, fontPath.lastIndexOf(",")) : fontPath);
                if (fontFile.exists()) {
                    PdfFont font = PdfFontFactory.createFont(fontPath, PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    logger.info("成功加载系统中文字体: {} ({})", fontName, fontPath);
                    return font;
                }
            } catch (Exception e) {
                logger.debug("系统字体加载失败: {} - {}", fontName, e.getMessage());
            }
        }
        
        logger.debug("未找到可用的系统中文字体");
        return null;
    }

    /**
     * 尝试加载类路径下的嵌入字体
     */
    private PdfFont tryLoadClasspathFonts() {
        String[] classpathFonts = {
            "fonts/simsun.ttf",
            "fonts/simhei.ttf",
            "fonts/NotoSansSC-Regular.ttf"
        };

        for (String fontResource : classpathFonts) {
            try {
                ClassPathResource resource = new ClassPathResource(fontResource);
                if (resource.exists()) {
                    // 将字体文件复制到临时目录
                    Path tempFont = Files.createTempFile("font_", ".ttf");
                    try (InputStream is = resource.getInputStream();
                         OutputStream os = Files.newOutputStream(tempFont)) {
                        is.transferTo(os);
                    }
                    
                    PdfFont font = PdfFontFactory.createFont(tempFont.toString(), PdfEncodings.IDENTITY_H,
                            PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    logger.info("成功加载嵌入中文字体: {}", fontResource);
                    
                    // 注册临时文件在JVM退出时删除
                    tempFont.toFile().deleteOnExit();
                    return font;
                }
            } catch (Exception e) {
                logger.debug("嵌入字体加载失败: {} - {}", fontResource, e.getMessage());
            }
        }
        
        logger.debug("未找到可用的嵌入中文字体");
        return null;
    }

    /**
     * 尝试加载iText内置的CJK字体
     */
    private PdfFont tryLoadBuiltInCjkFonts() {
        // iText内置CJK字体列表
        String[][] builtInFonts = {
            {"STSong-Light", "UniGB-UCS2-H"},      // 简体中文宋体
            {"STSongStd-Light", "UniGB-UCS2-H"},   // 简体中文宋体标准
            {"MHei-Medium", "UniCNS-UCS2-H"},      // 繁体中文黑体
            {"MSung-Light", "UniCNS-UCS2-H"}       // 繁体中文宋体
        };

        for (String[] fontInfo : builtInFonts) {
            String fontName = fontInfo[0];
            String encoding = fontInfo[1];
            try {
                PdfFont font = PdfFontFactory.createFont(fontName, encoding,
                        PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
                logger.info("成功加载iText内置CJK字体: {}", fontName);
                return font;
            } catch (Exception e) {
                logger.debug("内置CJK字体加载失败: {} - {}", fontName, e.getMessage());
            }
        }
        
        logger.warn("无法加载任何内置CJK字体");
        return null;
    }

    /**
     * 创建默认字体作为最后备用
     */
    private PdfFont createDefaultFont() {
        try {
            PdfFont font = PdfFontFactory.createFont();
            logger.warn("使用默认字体，中文字符可能无法正确显示");
            return font;
        } catch (Exception e) {
            logger.error("默认字体创建失败: {}", e.getMessage());
            throw new BusinessException("字体初始化失败: 无法创建任何可用字体");
        }
    }

    /**
     * 检查当前字体是否支持中文
     * @param font 要检查的字体
     * @return 是否支持中文
     */
    private boolean supportsChinese(PdfFont font) {
        if (font == null) {
            return false;
        }
        try {
            // 测试一些常用中文字符
            String testChars = "中文测试";
            for (char c : testChars.toCharArray()) {
                if (!font.containsGlyph(c)) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 处理单个工作表
     */
    private void processSheet(Document document, Sheet sheet, PdfFont chineseFont) {
        int lastRowNum = sheet.getLastRowNum();
        int maxColNum = getMaxColumnNum(sheet);
        
        if (maxColNum == 0) {
            return;
        }

        // 获取合并单元格信息
        Map<String, CellRangeAddress> mergedRegions = getMergedRegions(sheet);
        Map<String, Boolean> processedMergedCells = new HashMap<>();

        // 计算列宽
        float[] columnWidths = calculateColumnWidths(sheet, maxColNum);
        Table table = new Table(UnitValue.createPointArray(columnWidths));
        table.setWidth(UnitValue.createPercentValue(100));

        for (int rowIndex = 0; rowIndex <= lastRowNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            
            for (int colIndex = 0; colIndex < maxColNum; colIndex++) {
                String cellKey = rowIndex + "," + colIndex;
                
                // 检查是否是已处理的合并单元格的一部分
                if (processedMergedCells.containsKey(cellKey)) {
                    continue;
                }

                org.apache.poi.ss.usermodel.Cell excelCell = (row != null) ? row.getCell(colIndex) : null;
                CellRangeAddress mergedRegion = mergedRegions.get(cellKey);

                Cell pdfCell;
                if (mergedRegion != null) {
                    // 处理合并单元格
                    pdfCell = createMergedCell(excelCell, mergedRegion, sheet, chineseFont);
                    markMergedCellsAsProcessed(mergedRegion, processedMergedCells);
                } else {
                    pdfCell = createCell(excelCell, chineseFont);
                }

                table.addCell(pdfCell);
            }
        }

        document.add(table);
    }

    /**
     * 获取最大列数
     */
    private int getMaxColumnNum(Sheet sheet) {
        int maxCol = 0;
        for (int i = 0; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getLastCellNum() > maxCol) {
                maxCol = row.getLastCellNum();
            }
        }
        return maxCol;
    }

    /**
     * 获取合并单元格区域映射
     */
    private Map<String, CellRangeAddress> getMergedRegions(Sheet sheet) {
        Map<String, CellRangeAddress> mergedRegions = new HashMap<>();
        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            CellRangeAddress region = sheet.getMergedRegion(i);
            String key = region.getFirstRow() + "," + region.getFirstColumn();
            mergedRegions.put(key, region);
        }
        return mergedRegions;
    }

    /**
     * 计算列宽
     */
    private float[] calculateColumnWidths(Sheet sheet, int maxColNum) {
        float[] widths = new float[maxColNum];
        float totalWidth = 0;
        
        for (int i = 0; i < maxColNum; i++) {
            int excelWidth = sheet.getColumnWidth(i);
            widths[i] = excelWidth / 256f * 7; // 转换为点
            totalWidth += widths[i];
        }

        // 归一化
        float pageWidth = PageSize.A4.rotate().getWidth() - 40;
        if (totalWidth > pageWidth) {
            float scale = pageWidth / totalWidth;
            for (int i = 0; i < widths.length; i++) {
                widths[i] *= scale;
            }
        }

        return widths;
    }


    /**
     * 创建合并单元格
     */
    private Cell createMergedCell(org.apache.poi.ss.usermodel.Cell excelCell, 
                                   CellRangeAddress region, Sheet sheet, PdfFont chineseFont) {
        String cellValue = getCellValue(excelCell);
        Cell pdfCell = new Cell(
                region.getLastRow() - region.getFirstRow() + 1,
                region.getLastColumn() - region.getFirstColumn() + 1
        );
        
        Paragraph paragraph = new Paragraph(cellValue);
        if (chineseFont != null) {
            paragraph.setFont(chineseFont);
        }
        paragraph.setFontSize(DEFAULT_FONT_SIZE);
        pdfCell.add(paragraph);
        pdfCell.setPadding(CELL_PADDING);
        
        applyCellStyle(pdfCell, excelCell);
        
        return pdfCell;
    }

    /**
     * 标记合并单元格区域为已处理
     */
    private void markMergedCellsAsProcessed(CellRangeAddress region, 
                                             Map<String, Boolean> processedCells) {
        for (int r = region.getFirstRow(); r <= region.getLastRow(); r++) {
            for (int c = region.getFirstColumn(); c <= region.getLastColumn(); c++) {
                if (r != region.getFirstRow() || c != region.getFirstColumn()) {
                    processedCells.put(r + "," + c, true);
                }
            }
        }
    }

    /**
     * 创建普通单元格
     */
    private Cell createCell(org.apache.poi.ss.usermodel.Cell excelCell, PdfFont chineseFont) {
        String cellValue = getCellValue(excelCell);
        Cell pdfCell = new Cell();
        
        Paragraph paragraph = new Paragraph(cellValue);
        if (chineseFont != null) {
            paragraph.setFont(chineseFont);
        }
        paragraph.setFontSize(DEFAULT_FONT_SIZE);
        pdfCell.add(paragraph);
        pdfCell.setPadding(CELL_PADDING);
        
        applyCellStyle(pdfCell, excelCell);
        
        return pdfCell;
    }

    /**
     * 获取单元格值
     */
    private String getCellValue(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) {
            return "";
        }
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toString();
                }
                double value = cell.getNumericCellValue();
                if (value == Math.floor(value)) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    try {
                        yield cell.getStringCellValue();
                    } catch (Exception ex) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }


    /**
     * 应用单元格样式
     */
    private void applyCellStyle(Cell pdfCell, org.apache.poi.ss.usermodel.Cell excelCell) {
        if (excelCell == null) {
            pdfCell.setBorder(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
            return;
        }

        CellStyle style = excelCell.getCellStyle();
        
        // 应用对齐方式
        HorizontalAlignment hAlign = style.getAlignment();
        switch (hAlign) {
            case CENTER -> pdfCell.setTextAlignment(TextAlignment.CENTER);
            case RIGHT -> pdfCell.setTextAlignment(TextAlignment.RIGHT);
            default -> pdfCell.setTextAlignment(TextAlignment.LEFT);
        }

        // 应用边框样式
        applyBorderStyle(pdfCell, style);

        // 应用背景色
        short bgColorIndex = style.getFillForegroundColor();
        if (bgColorIndex != IndexedColors.AUTOMATIC.getIndex()) {
            try {
                Color bgColor = style.getFillForegroundColorColor();
                if (bgColor instanceof org.apache.poi.xssf.usermodel.XSSFColor xssfColor) {
                    byte[] rgb = xssfColor.getRGB();
                    if (rgb != null && rgb.length >= 3) {
                        pdfCell.setBackgroundColor(new DeviceRgb(
                                rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF));
                    }
                }
            } catch (Exception e) {
                // 忽略颜色转换错误
            }
        }
    }

    /**
     * 应用边框样式
     */
    private void applyBorderStyle(Cell pdfCell, CellStyle style) {
        // 获取边框颜色
        com.itextpdf.kernel.colors.Color topColor = getBorderColor(style, BorderSide.TOP);
        com.itextpdf.kernel.colors.Color bottomColor = getBorderColor(style, BorderSide.BOTTOM);
        com.itextpdf.kernel.colors.Color leftColor = getBorderColor(style, BorderSide.LEFT);
        com.itextpdf.kernel.colors.Color rightColor = getBorderColor(style, BorderSide.RIGHT);

        // 转换边框样式
        Border topBorder = convertBorder(style.getBorderTop(), topColor);
        Border bottomBorder = convertBorder(style.getBorderBottom(), bottomColor);
        Border leftBorder = convertBorder(style.getBorderLeft(), leftColor);
        Border rightBorder = convertBorder(style.getBorderRight(), rightColor);

        pdfCell.setBorderTop(topBorder);
        pdfCell.setBorderBottom(bottomBorder);
        pdfCell.setBorderLeft(leftBorder);
        pdfCell.setBorderRight(rightBorder);
    }

    /**
     * 边框方向枚举
     */
    private enum BorderSide {
        TOP, BOTTOM, LEFT, RIGHT
    }

    /**
     * 获取边框颜色
     */
    private com.itextpdf.kernel.colors.Color getBorderColor(CellStyle style, BorderSide side) {
        try {
            Color excelColor = switch (side) {
                case TOP -> style.getTopBorderColor() != IndexedColors.AUTOMATIC.getIndex() 
                        ? getColorFromIndex(style.getTopBorderColor()) : null;
                case BOTTOM -> style.getBottomBorderColor() != IndexedColors.AUTOMATIC.getIndex() 
                        ? getColorFromIndex(style.getBottomBorderColor()) : null;
                case LEFT -> style.getLeftBorderColor() != IndexedColors.AUTOMATIC.getIndex() 
                        ? getColorFromIndex(style.getLeftBorderColor()) : null;
                case RIGHT -> style.getRightBorderColor() != IndexedColors.AUTOMATIC.getIndex() 
                        ? getColorFromIndex(style.getRightBorderColor()) : null;
            };

            if (excelColor instanceof org.apache.poi.xssf.usermodel.XSSFColor xssfColor) {
                byte[] rgb = xssfColor.getRGB();
                if (rgb != null && rgb.length >= 3) {
                    return new DeviceRgb(rgb[0] & 0xFF, rgb[1] & 0xFF, rgb[2] & 0xFF);
                }
            }
        } catch (Exception e) {
            logger.debug("获取边框颜色失败: {}", e.getMessage());
        }
        return ColorConstants.BLACK;
    }

    /**
     * 从索引获取颜色
     */
    private Color getColorFromIndex(short colorIndex) {
        IndexedColors indexedColor = IndexedColors.fromInt(colorIndex);
        if (indexedColor != null && indexedColor != IndexedColors.AUTOMATIC) {
            // 返回null让调用者使用默认颜色
            return null;
        }
        return null;
    }

    /**
     * 转换边框样式
     */
    private Border convertBorder(BorderStyle excelBorder, com.itextpdf.kernel.colors.Color color) {
        if (excelBorder == null || excelBorder == BorderStyle.NONE) {
            return Border.NO_BORDER;
        }

        // 使用传入的颜色，如果为null则使用黑色
        com.itextpdf.kernel.colors.Color borderColor = (color != null) ? color : ColorConstants.BLACK;

        // 根据Excel边框样式确定宽度和类型
        return switch (excelBorder) {
            case THIN -> new SolidBorder(borderColor, 0.5f);
            case HAIR -> new SolidBorder(borderColor, 0.25f);
            case MEDIUM -> new SolidBorder(borderColor, 1f);
            case THICK -> new SolidBorder(borderColor, 1.5f);
            case DOUBLE -> new DoubleBorder(borderColor, 1.5f);
            case DASHED -> new DashedBorder(borderColor, 0.5f);
            case DOTTED -> new DottedBorder(borderColor, 0.5f);
            case MEDIUM_DASHED -> new DashedBorder(borderColor, 1f);
            case DASH_DOT -> new DashedBorder(borderColor, 0.5f);
            case MEDIUM_DASH_DOT -> new DashedBorder(borderColor, 1f);
            case DASH_DOT_DOT -> new DashedBorder(borderColor, 0.5f);
            case MEDIUM_DASH_DOT_DOT -> new DashedBorder(borderColor, 1f);
            case SLANTED_DASH_DOT -> new DashedBorder(borderColor, 0.5f);
            default -> new SolidBorder(borderColor, 0.5f);
        };
    }
}
