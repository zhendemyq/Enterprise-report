package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ReportSchedule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时报表任务Mapper
 */
@Mapper
public interface ReportScheduleMapper extends BaseMapper<ReportSchedule> {
}
