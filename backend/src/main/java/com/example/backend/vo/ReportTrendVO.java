package com.example.backend.vo;

import lombok.Data;

import java.util.List;

/**
 * 报表生成趋势VO
 */
@Data
public class ReportTrendVO {

    /**
     * 日期列表
     */
    private List<String> dates;

    /**
     * 数量列表
     */
    private List<Long> counts;
}
