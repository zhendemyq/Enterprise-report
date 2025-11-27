package com.example.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.backend.entity.ReportCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报表分类Mapper
 */
@Mapper
public interface ReportCategoryMapper extends BaseMapper<ReportCategory> {
}
