package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.ResultCode;
import com.example.backend.dto.ReportEmailRequest;
import com.example.backend.dto.ReportGenerateDTO;
import com.example.backend.dto.ReportScheduleDTO;
import com.example.backend.dto.ReportScheduleQueryDTO;
import com.example.backend.entity.ReportSchedule;
import com.example.backend.entity.ReportScheduleLog;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportScheduleLogMapper;
import com.example.backend.mapper.ReportScheduleMapper;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.service.EmailService;
import com.example.backend.service.ReportGenerateService;
import com.example.backend.service.ReportScheduleService;
import com.example.backend.vo.ReportRecordVO;
import com.example.backend.vo.ReportScheduleVO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.util.CronUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 定时报表任务服务实现
 */
@Service
public class ReportScheduleServiceImpl extends ServiceImpl<ReportScheduleMapper, ReportSchedule> implements ReportScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ReportScheduleServiceImpl.class);

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Autowired
    private ReportScheduleLogMapper reportScheduleLogMapper;

    @Autowired
    private ReportGenerateService reportGenerateService;

    @Autowired
    private EmailService emailService;

    @Value("${report.storage.path:./upload/reports}")
    private String storagePath;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSchedule(ReportScheduleDTO scheduleDTO) {
        // 检查模板是否存在
        ReportTemplate template = reportTemplateMapper.selectById(scheduleDTO.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        ReportSchedule schedule = BeanUtil.copyProperties(scheduleDTO, ReportSchedule.class);
        
        // 生成任务编码
        if (StringUtils.isBlank(schedule.getTaskCode())) {
            schedule.setTaskCode("SCH_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        
        schedule.setStatus(0); // 默认禁用
        schedule.setExecuteCount(0);
        schedule.setFailCount(0);
        
        // 计算下次执行时间
        schedule.setNextExecuteTime(calculateNextExecuteTime(scheduleDTO.getCronExpression()));
        
        save(schedule);
        return schedule.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSchedule(Long id, ReportScheduleDTO scheduleDTO) {
        ReportSchedule schedule = getById(id);
        if (schedule == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 检查模板是否存在
        ReportTemplate template = reportTemplateMapper.selectById(scheduleDTO.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        BeanUtil.copyProperties(scheduleDTO, schedule, "id", "taskCode", "status", "executeCount", "failCount");
        
        // 重新计算下次执行时间
        schedule.setNextExecuteTime(calculateNextExecuteTime(scheduleDTO.getCronExpression()));
        
        updateById(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSchedule(Long id) {
        ReportSchedule schedule = getById(id);
        if (schedule == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 删除执行日志
        LambdaQueryWrapper<ReportScheduleLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(ReportScheduleLog::getScheduleId, id);
        reportScheduleLogMapper.delete(logWrapper);

        removeById(id);
    }

    @Override
    public ReportScheduleVO getScheduleDetail(Long id) {
        ReportSchedule schedule = getById(id);
        if (schedule == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        return convertToVO(schedule);
    }

    @Override
    public IPage<ReportScheduleVO> pageSchedules(ReportScheduleQueryDTO queryDTO) {
        Page<ReportSchedule> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        LambdaQueryWrapper<ReportSchedule> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（任务名称）
        if (StringUtils.isNotBlank(queryDTO.getKeyword())) {
            wrapper.like(ReportSchedule::getTaskName, queryDTO.getKeyword());
        }

        wrapper.like(StringUtils.isNotBlank(queryDTO.getTaskName()), ReportSchedule::getTaskName, queryDTO.getTaskName())
                .eq(queryDTO.getTemplateId() != null, ReportSchedule::getTemplateId, queryDTO.getTemplateId())
                .eq(queryDTO.getStatus() != null, ReportSchedule::getStatus, queryDTO.getStatus())
                .eq(queryDTO.getScheduleType() != null, ReportSchedule::getScheduleType, queryDTO.getScheduleType())
                .orderByDesc(ReportSchedule::getCreateTime);

        IPage<ReportSchedule> schedulePage = page(page, wrapper);
        return schedulePage.convert(this::convertToVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enableSchedule(Long id) {
        ReportSchedule schedule = getById(id);
        if (schedule == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        schedule.setStatus(1);
        schedule.setNextExecuteTime(calculateNextExecuteTime(schedule.getCronExpression()));
        updateById(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disableSchedule(Long id) {
        ReportSchedule schedule = getById(id);
        if (schedule == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }
        schedule.setStatus(0);
        updateById(schedule);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeNow(Long id) {
        ReportSchedule schedule = getById(id);
        if (schedule == null) {
            throw new BusinessException(ResultCode.TASK_NOT_FOUND);
        }

        // 获取模板信息
        ReportTemplate template = reportTemplateMapper.selectById(schedule.getTemplateId());
        if (template == null) {
            throw new BusinessException(ResultCode.TEMPLATE_NOT_FOUND);
        }

        // 创建执行日志
        ReportScheduleLog log = new ReportScheduleLog();
        log.setScheduleId(id);
        log.setTaskName(schedule.getTaskName());
        log.setExecuteParams(schedule.getGenerateParams());
        log.setStatus(0); // 执行中
        log.setStartTime(LocalDateTime.now());
        reportScheduleLogMapper.insert(log);

        ReportRecordVO reportRecord = null;
        try {
            // 构建报表生成请求
            ReportGenerateDTO generateDTO = new ReportGenerateDTO();
            generateDTO.setTemplateId(schedule.getTemplateId());
            generateDTO.setReportName(schedule.getTaskName() + "_" + 
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
            generateDTO.setParams(schedule.getGenerateParams());
            generateDTO.setFileType(StringUtils.isNotBlank(schedule.getFileType()) ? schedule.getFileType() : "xlsx");

            // 执行报表生成
            reportRecord = reportGenerateService.generateReport(generateDTO);
            
            log.setStatus(1); // 成功
            log.setReportRecordId(reportRecord.getId());
            log.setEndTime(LocalDateTime.now());
            log.setDuration(System.currentTimeMillis() - log.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            
            // 更新任务执行信息
            schedule.setExecuteCount(schedule.getExecuteCount() + 1);
            schedule.setLastExecuteTime(LocalDateTime.now());
            schedule.setNextExecuteTime(calculateNextExecuteTime(schedule.getCronExpression()));
            updateById(schedule);

            // 发送邮件通知（如果启用）
            if (Boolean.TRUE.equals(schedule.getSendEmail()) && StringUtils.isNotBlank(schedule.getEmailReceivers())) {
                sendReportEmail(schedule, reportRecord, template);
            }
        } catch (Exception e) {
            logger.error("定时任务执行失败: taskId={}, taskName={}", id, schedule.getTaskName(), e);
            log.setStatus(2); // 失败
            log.setErrorMsg(e.getMessage());
            log.setEndTime(LocalDateTime.now());
            log.setDuration(System.currentTimeMillis() - log.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            
            schedule.setFailCount(schedule.getFailCount() + 1);
            updateById(schedule);
        }
        
        reportScheduleLogMapper.updateById(log);
    }

    /**
     * 发送报表邮件通知
     * @param schedule 定时任务配置
     * @param reportRecord 生成的报表记录
     * @param template 报表模板
     */
    private void sendReportEmail(ReportSchedule schedule, ReportRecordVO reportRecord, ReportTemplate template) {
        try {
            // 解析收件人列表（逗号分隔）
            List<String> recipients = Arrays.stream(schedule.getEmailReceivers().split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());

            if (recipients.isEmpty()) {
                logger.warn("邮件收件人列表为空，跳过发送: taskId={}", schedule.getId());
                return;
            }

            // 构建附件路径
            String attachmentPath = storagePath + File.separator + reportRecord.getFilePath();
            String attachmentName = reportRecord.getReportName() + "." + reportRecord.getFileType();

            // 构建邮件主题
            String subject = StringUtils.isNotBlank(schedule.getEmailSubject()) 
                    ? schedule.getEmailSubject() 
                    : "报表通知: " + reportRecord.getReportName();

            // 构建邮件请求
            ReportEmailRequest emailRequest = ReportEmailRequest.builder()
                    .recipients(recipients)
                    .subject(subject)
                    .reportName(reportRecord.getReportName())
                    .generateTime(reportRecord.getEndTime())
                    .dataRows(reportRecord.getDataRows())
                    .attachmentPath(attachmentPath)
                    .attachmentName(attachmentName)
                    .build();

            // 发送邮件
            emailService.sendReportEmail(emailRequest);
            logger.info("报表邮件发送成功: taskId={}, recipients={}", schedule.getId(), recipients);
        } catch (Exception e) {
            // 邮件发送失败不影响任务执行状态，只记录日志
            logger.error("报表邮件发送失败: taskId={}, error={}", schedule.getId(), e.getMessage(), e);
        }
    }

    @Override
    public List<ReportScheduleVO> getExecuteHistory(Long id) {
        // 这个方法返回执行日志而不是VO，但为了保持接口一致性，这里暂时返回空列表
        // 实际应该创建一个专门的日志查询方法
        return List.of();
    }

    /**
     * 获取执行日志
     */
    public IPage<ReportScheduleLog> getScheduleLogs(Long scheduleId, int pageNum, int pageSize) {
        Page<ReportScheduleLog> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<ReportScheduleLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportScheduleLog::getScheduleId, scheduleId)
                .orderByDesc(ReportScheduleLog::getCreateTime);
        return reportScheduleLogMapper.selectPage(page, wrapper);
    }

    /**
     * 转换为VO
     */
    private ReportScheduleVO convertToVO(ReportSchedule schedule) {
        ReportScheduleVO vo = BeanUtil.copyProperties(schedule, ReportScheduleVO.class);
        
        // 获取模板名称
        ReportTemplate template = reportTemplateMapper.selectById(schedule.getTemplateId());
        if (template != null) {
            vo.setTemplateName(template.getTemplateName());
        }
        
        // 设置状态名称
        vo.setStatusName(schedule.getStatus() == 1 ? "启用" : "禁用");
        
        // 设置调度类型名称
        vo.setScheduleTypeName(getScheduleTypeName(schedule.getScheduleType()));
        
        return vo;
    }

    /**
     * 获取调度类型名称
     */
    private String getScheduleTypeName(Integer scheduleType) {
        if (scheduleType == null) return "未知";
        return switch (scheduleType) {
            case 1 -> "每日";
            case 2 -> "每周";
            case 3 -> "每月";
            case 4 -> "自定义";
            default -> "未知";
        };
    }

    /**
     * 计算下次执行时间
     *
     * @param cronExpression Cron表达式
     * @return 下次执行时间，如果表达式无效则返回null
     */
    private LocalDateTime calculateNextExecuteTime(String cronExpression) {
        if (StringUtils.isBlank(cronExpression)) {
            return null;
        }
        LocalDateTime nextTime = CronUtils.getNextExecutionTime(cronExpression);
        if (nextTime == null) {
            // 如果无法计算，返回默认值（1天后）
            return LocalDateTime.now().plusDays(1);
        }
        return nextTime;
    }

    /**
     * 验证Cron表达式是否有效
     *
     * @param cronExpression Cron表达式
     * @return true-有效，false-无效
     */
    public boolean isValidCronExpression(String cronExpression) {
        return CronUtils.isValidExpression(cronExpression);
    }
}
