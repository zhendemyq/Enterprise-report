package com.example.backend.dto;

import lombok.Data;

/**
 * 分页查询基类
 */
@Data
public class PageDTO {

    /**
     * 当前页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方式 asc/desc
     */
    private String orderType = "desc";
}
