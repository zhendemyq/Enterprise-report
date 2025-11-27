package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ReportDatasource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源配置Mapper
 */
@Mapper
public interface ReportDatasourceMapper extends BaseMapper<ReportDatasource> {
}
