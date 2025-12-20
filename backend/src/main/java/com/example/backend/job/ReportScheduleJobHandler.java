package com.example.backend.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.ReportSchedule;
import com.example.backend.service.ReportScheduleService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 报表定时任务Handler
 */
@Component
public class ReportScheduleJobHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReportScheduleJobHandler.class);

    @Autowired
    private ReportScheduleService reportScheduleService;

    /**
     * 报表定时生成任务
     * 扫描所有启用且到达执行时间的定时任务并执行
     */
    @XxlJob("reportScheduleHandler")
    public void execute() {
        XxlJobHelper.log("开始执行报表定时任务扫描...");
        logger.info("开始执行报表定时任务扫描...");

        try {
            // 查询所有启用且到达执行时间的任务
            LambdaQueryWrapper<ReportSchedule> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ReportSchedule::getStatus, 1)
                    .le(ReportSchedule::getNextExecuteTime, LocalDateTime.now());

            List<ReportSchedule> schedules = reportScheduleService.list(wrapper);

            if (schedules.isEmpty()) {
                XxlJobHelper.log("没有需要执行的定时任务");
                logger.info("没有需要执行的定时任务");
                return;
            }

            XxlJobHelper.log("找到 {} 个待执行任务", schedules.size());
            logger.info("找到 {} 个待执行任务", schedules.size());

            int successCount = 0;
            int failCount = 0;

            for (ReportSchedule schedule : schedules) {
                try {
                    XxlJobHelper.log("执行任务: id={}, name={}", schedule.getId(), schedule.getTaskName());
                    reportScheduleService.executeNow(schedule.getId());
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    XxlJobHelper.log("任务执行失败: id={}, error={}", schedule.getId(), e.getMessage());
                    logger.error("任务执行失败: id={}, name={}", schedule.getId(), schedule.getTaskName(), e);
                }
            }

            String result = String.format("执行完成: 成功=%d, 失败=%d", successCount, failCount);
            XxlJobHelper.log(result);
            logger.info(result);

        } catch (Exception e) {
            XxlJobHelper.log("定时任务扫描异常: {}", e.getMessage());
            logger.error("定时任务扫描异常", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }

    /**
     * 单个报表任务执行（通过任务参数指定scheduleId）
     */
    @XxlJob("reportSingleHandler")
    public void executeSingle() {
        String param = XxlJobHelper.getJobParam();
        XxlJobHelper.log("执行单个报表任务, scheduleId={}", param);

        try {
            Long scheduleId = Long.parseLong(param);
            reportScheduleService.executeNow(scheduleId);
            XxlJobHelper.log("任务执行成功");
        } catch (NumberFormatException e) {
            XxlJobHelper.log("参数格式错误，需要传入scheduleId");
            XxlJobHelper.handleFail("参数格式错误");
        } catch (Exception e) {
            XxlJobHelper.log("任务执行失败: {}", e.getMessage());
            logger.error("单个报表任务执行失败", e);
            XxlJobHelper.handleFail(e.getMessage());
        }
    }
}
