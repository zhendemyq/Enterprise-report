package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ReportTemplate;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表模板Mapper
 */
@Mapper
public interface ReportTemplateMapper extends BaseMapper<ReportTemplate> {
}
