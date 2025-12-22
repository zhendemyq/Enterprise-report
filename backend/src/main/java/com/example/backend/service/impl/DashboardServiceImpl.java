package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.entity.ReportDatasource;
import com.example.backend.entity.ReportRecord;
import com.example.backend.entity.ReportSchedule;
import com.example.backend.entity.ReportTemplate;
import com.example.backend.mapper.ReportDatasourceMapper;
import com.example.backend.mapper.ReportRecordMapper;
import com.example.backend.mapper.ReportScheduleMapper;
import com.example.backend.mapper.ReportTemplateMapper;
import com.example.backend.service.DashboardService;
import com.example.backend.vo.DashboardStatsVO;
import com.example.backend.vo.ReportRecordVO;
import com.example.backend.vo.ReportTemplateVO;
import com.example.backend.vo.ReportTrendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仪表盘服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private ReportTemplateMapper reportTemplateMapper;

    @Autowired
    private ReportRecordMapper reportRecordMapper;

    @Autowired
    private ReportDatasourceMapper reportDatasourceMapper;

    @Autowired
    private ReportScheduleMapper reportScheduleMapper;

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        // 统计模板总数
        stats.setTemplateCount(reportTemplateMapper.selectCount(null));

        // 统计报表总数
        stats.setReportCount(reportRecordMapper.selectCount(null));

        // 统计数据源总数
        stats.setDatasourceCount(reportDatasourceMapper.selectCount(null));

        // 统计定时任务数
        stats.setScheduleCount(reportScheduleMapper.selectCount(null));

        // 今日生成报表数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<ReportRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(ReportRecord::getCreateTime, todayStart);
        stats.setTodayReportCount(reportRecordMapper.selectCount(todayWrapper));

        // 本周生成报表数
        LocalDateTime weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).atStartOfDay();
        LambdaQueryWrapper<ReportRecord> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.ge(ReportRecord::getCreateTime, weekStart);
        stats.setWeekReportCount(reportRecordMapper.selectCount(weekWrapper));

        // 本月生成报表数
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LambdaQueryWrapper<ReportRecord> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(ReportRecord::getCreateTime, monthStart);
        stats.setMonthReportCount(reportRecordMapper.selectCount(monthWrapper));

        return stats;
    }

    @Override
    public List<ReportRecordVO> getRecentReports(int limit) {
        LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ReportRecord::getCreateTime)
                .last("LIMIT " + limit);
        List<ReportRecord> records = reportRecordMapper.selectList(wrapper);
        
        return records.stream()
                .map(record -> {
                    ReportRecordVO vo = BeanUtil.copyProperties(record, ReportRecordVO.class);
                    // 设置状态名称
                    vo.setStatusName(getStatusName(record.getStatus()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportTemplateVO> getPopularTemplates(int limit) {
        // 按生成次数统计模板使用情况
        LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ReportRecord::getTemplateId);
        List<ReportRecord> records = reportRecordMapper.selectList(wrapper);
        
        // 统计每个模板的使用次数
        Map<Long, Long> countMap = records.stream()
                .filter(r -> r.getTemplateId() != null)
                .collect(Collectors.groupingBy(ReportRecord::getTemplateId, Collectors.counting()));
        
        // 按使用次数排序
        List<Long> topTemplateIds = countMap.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // 获取模板详情
        List<ReportTemplateVO> result = new ArrayList<>();
        for (Long templateId : topTemplateIds) {
            ReportTemplate template = reportTemplateMapper.selectById(templateId);
            if (template != null) {
                ReportTemplateVO vo = BeanUtil.copyProperties(template, ReportTemplateVO.class);
                vo.setUseCount(countMap.get(templateId).intValue());
                result.add(vo);
            }
        }
        
        // 如果热门模板不足，补充最新模板
        if (result.size() < limit) {
            LambdaQueryWrapper<ReportTemplate> templateWrapper = new LambdaQueryWrapper<>();
            templateWrapper.notIn(!topTemplateIds.isEmpty(), ReportTemplate::getId, topTemplateIds)
                    .orderByDesc(ReportTemplate::getCreateTime)
                    .last("LIMIT " + (limit - result.size()));
            List<ReportTemplate> templates = reportTemplateMapper.selectList(templateWrapper);
            for (ReportTemplate template : templates) {
                ReportTemplateVO vo = BeanUtil.copyProperties(template, ReportTemplateVO.class);
                vo.setUseCount(0);
                result.add(vo);
            }
        }
        
        return result;
    }

    @Override
    public ReportTrendVO getReportTrend(String period) {
        ReportTrendVO trend = new ReportTrendVO();
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        int days;
        switch (period) {
            case "month":
                days = 30;
                break;
            case "year":
                days = 365;
                formatter = DateTimeFormatter.ofPattern("yyyy-MM");
                break;
            case "week":
            default:
                days = 7;
                break;
        }
        
        if ("year".equals(period)) {
            // 按月统计
            for (int i = 11; i >= 0; i--) {
                LocalDate monthStart = today.minusMonths(i).withDayOfMonth(1);
                LocalDate monthEnd = monthStart.plusMonths(1);
                
                LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.ge(ReportRecord::getCreateTime, monthStart.atStartOfDay())
                        .lt(ReportRecord::getCreateTime, monthEnd.atStartOfDay());
                Long count = reportRecordMapper.selectCount(wrapper);
                
                dates.add(monthStart.format(formatter));
                counts.add(count);
            }
        } else {
            // 按天统计
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = today.minusDays(i);
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
                
                LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
                wrapper.ge(ReportRecord::getCreateTime, dayStart)
                        .lt(ReportRecord::getCreateTime, dayEnd);
                Long count = reportRecordMapper.selectCount(wrapper);
                
                dates.add(date.format(formatter));
                counts.add(count);
            }
        }
        
        trend.setDates(dates);
        trend.setCounts(counts);
        return trend;
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "生成中";
            case 1 -> "成功";
            case 2 -> "失败";
            default -> "未知";
        };
    }
}
