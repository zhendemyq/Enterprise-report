package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ReportScheduleLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时任务执行日志Mapper
 */
@Mapper
public interface ReportScheduleLogMapper extends BaseMapper<ReportScheduleLog> {
}
