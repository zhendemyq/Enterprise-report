package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ReportRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表生成记录Mapper
 */
@Mapper
public interface ReportRecordMapper extends BaseMapper<ReportRecord> {
}
