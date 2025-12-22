package com.example.backend.service.impl;

import cn.dev33.satoken.stp.StpUtil;
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
import com.example.backend.service.PermissionService;
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

    @Autowired
    private PermissionService permissionService;

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();
        boolean isAdmin = permissionService.isCurrentUserAdmin();
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 统计模板总数（管理员看所有，普通用户看有权限的）
        if (isAdmin) {
            stats.setTemplateCount(reportTemplateMapper.selectCount(null));
        } else {
            List<Long> permittedTemplateIds = permissionService.getCurrentUserPermittedTemplateIds(1);
            if (permittedTemplateIds == null) {
                // null表示不限制，可以看所有已发布模板
                LambdaQueryWrapper<ReportTemplate> templateWrapper = new LambdaQueryWrapper<>();
                templateWrapper.eq(ReportTemplate::getStatus, 1); // 只统计已发布的
                stats.setTemplateCount(reportTemplateMapper.selectCount(templateWrapper));
            } else if (permittedTemplateIds.isEmpty()) {
                stats.setTemplateCount(0L);
            } else {
                LambdaQueryWrapper<ReportTemplate> templateWrapper = new LambdaQueryWrapper<>();
                templateWrapper.in(ReportTemplate::getId, permittedTemplateIds);
                stats.setTemplateCount(reportTemplateMapper.selectCount(templateWrapper));
            }
        }

        // 统计报表总数（管理员看所有，普通用户看自己创建的）
        if (isAdmin) {
            stats.setReportCount(reportRecordMapper.selectCount(null));
        } else {
            LambdaQueryWrapper<ReportRecord> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(ReportRecord::getCreateBy, currentUserId);
            stats.setReportCount(reportRecordMapper.selectCount(recordWrapper));
        }

        // 统计数据源总数（只有管理员能看数据源）
        if (isAdmin) {
            stats.setDatasourceCount(reportDatasourceMapper.selectCount(null));
        } else {
            stats.setDatasourceCount(0L);
        }

        // 统计定时任务数（管理员看所有，普通用户看自己创建的）
        if (isAdmin) {
            stats.setScheduleCount(reportScheduleMapper.selectCount(null));
        } else {
            LambdaQueryWrapper<ReportSchedule> scheduleWrapper = new LambdaQueryWrapper<>();
            scheduleWrapper.eq(ReportSchedule::getCreateBy, currentUserId);
            stats.setScheduleCount(reportScheduleMapper.selectCount(scheduleWrapper));
        }

        // 今日生成报表数
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<ReportRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(ReportRecord::getCreateTime, todayStart);
        if (!isAdmin) {
            todayWrapper.eq(ReportRecord::getCreateBy, currentUserId);
        }
        stats.setTodayReportCount(reportRecordMapper.selectCount(todayWrapper));

        // 本周生成报表数
        LocalDateTime weekStart = LocalDate.now().minusDays(LocalDate.now().getDayOfWeek().getValue() - 1).atStartOfDay();
        LambdaQueryWrapper<ReportRecord> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.ge(ReportRecord::getCreateTime, weekStart);
        if (!isAdmin) {
            weekWrapper.eq(ReportRecord::getCreateBy, currentUserId);
        }
        stats.setWeekReportCount(reportRecordMapper.selectCount(weekWrapper));

        // 本月生成报表数
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LambdaQueryWrapper<ReportRecord> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.ge(ReportRecord::getCreateTime, monthStart);
        if (!isAdmin) {
            monthWrapper.eq(ReportRecord::getCreateBy, currentUserId);
        }
        stats.setMonthReportCount(reportRecordMapper.selectCount(monthWrapper));

        return stats;
    }

    @Override
    public List<ReportRecordVO> getRecentReports(int limit) {
        boolean isAdmin = permissionService.isCurrentUserAdmin();
        Long currentUserId = StpUtil.getLoginIdAsLong();

        LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
        // 非管理员只能看自己创建的记录
        if (!isAdmin) {
            wrapper.eq(ReportRecord::getCreateBy, currentUserId);
        }
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
        boolean isAdmin = permissionService.isCurrentUserAdmin();
        Long currentUserId = StpUtil.getLoginIdAsLong();

        // 按生成次数统计模板使用情况
        LambdaQueryWrapper<ReportRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(ReportRecord::getTemplateId);
        // 非管理员只统计自己创建的记录
        if (!isAdmin) {
            wrapper.eq(ReportRecord::getCreateBy, currentUserId);
        }
        List<ReportRecord> records = reportRecordMapper.selectList(wrapper);

        // 统计每个模板的使用次数
        Map<Long, Long> countMap = records.stream()
                .filter(r -> r.getTemplateId() != null)
                .collect(Collectors.groupingBy(ReportRecord::getTemplateId, Collectors.counting()));

        // 获取用户有权限的模板ID列表
        List<Long> permittedTemplateIds = permissionService.getCurrentUserPermittedTemplateIds(1);

        // 按使用次数排序，并过滤用户有权限的模板
        List<Long> topTemplateIds = countMap.entrySet().stream()
                .filter(entry -> {
                    if (isAdmin || permittedTemplateIds == null) {
                        return true; // 管理员或不限制的用户可以看所有
                    }
                    return permittedTemplateIds.contains(entry.getKey());
                })
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

        // 如果热门模板不足，补充用户有权限的最新模板
        if (result.size() < limit) {
            LambdaQueryWrapper<ReportTemplate> templateWrapper = new LambdaQueryWrapper<>();
            templateWrapper.notIn(!topTemplateIds.isEmpty(), ReportTemplate::getId, topTemplateIds);
            // 非管理员只能看有权限的模板
            if (!isAdmin && permittedTemplateIds != null && !permittedTemplateIds.isEmpty()) {
                templateWrapper.in(ReportTemplate::getId, permittedTemplateIds);
            } else if (!isAdmin && permittedTemplateIds != null && permittedTemplateIds.isEmpty()) {
                // 用户没有任何模板权限，不补充
                return result;
            } else if (!isAdmin) {
                // permittedTemplateIds == null 表示可以看所有已发布模板
                templateWrapper.eq(ReportTemplate::getStatus, 1);
            }
            templateWrapper.orderByDesc(ReportTemplate::getCreateTime)
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
        boolean isAdmin = permissionService.isCurrentUserAdmin();
        Long currentUserId = StpUtil.getLoginIdAsLong();

        ReportTrendVO trend = new ReportTrendVO();
        List<String> dates = new ArrayList<>();
        List<Long> counts = new ArrayList<>();
        List<Long> downloadCounts = new ArrayList<>();

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
                // 非管理员只统计自己创建的记录
                if (!isAdmin) {
                    wrapper.eq(ReportRecord::getCreateBy, currentUserId);
                }
                Long count = reportRecordMapper.selectCount(wrapper);

                // 统计下载次数
                LambdaQueryWrapper<ReportRecord> downloadWrapper = new LambdaQueryWrapper<>();
                downloadWrapper.ge(ReportRecord::getCreateTime, monthStart.atStartOfDay())
                        .lt(ReportRecord::getCreateTime, monthEnd.atStartOfDay());
                if (!isAdmin) {
                    downloadWrapper.eq(ReportRecord::getCreateBy, currentUserId);
                }
                List<ReportRecord> records = reportRecordMapper.selectList(downloadWrapper);
                Long downloadCount = records.stream()
                        .mapToLong(r -> r.getDownloadCount() != null ? r.getDownloadCount() : 0)
                        .sum();

                dates.add(monthStart.format(formatter));
                counts.add(count);
                downloadCounts.add(downloadCount);
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
                // 非管理员只统计自己创建的记录
                if (!isAdmin) {
                    wrapper.eq(ReportRecord::getCreateBy, currentUserId);
                }
                Long count = reportRecordMapper.selectCount(wrapper);

                // 统计下载次数
                LambdaQueryWrapper<ReportRecord> downloadWrapper = new LambdaQueryWrapper<>();
                downloadWrapper.ge(ReportRecord::getCreateTime, dayStart)
                        .lt(ReportRecord::getCreateTime, dayEnd);
                if (!isAdmin) {
                    downloadWrapper.eq(ReportRecord::getCreateBy, currentUserId);
                }
                List<ReportRecord> records = reportRecordMapper.selectList(downloadWrapper);
                Long downloadCount = records.stream()
                        .mapToLong(r -> r.getDownloadCount() != null ? r.getDownloadCount() : 0)
                        .sum();

                dates.add(date.format(formatter));
                counts.add(count);
                downloadCounts.add(downloadCount);
            }
        }

        trend.setDates(dates);
        trend.setCounts(counts);
        trend.setDownloadCounts(downloadCounts);
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
