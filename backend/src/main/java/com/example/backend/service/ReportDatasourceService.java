package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.dto.ReportDatasourceDTO;
import com.example.backend.entity.ReportDatasource;
import com.example.backend.vo.ReportDatasourceVO;

import java.util.List;
import java.util.Map;

/**
 * 数据源服务接口
 */
public interface ReportDatasourceService extends IService<ReportDatasource> {

    /**
     * 创建数据源
     */
    Long createDatasource(ReportDatasourceDTO datasourceDTO);

    /**
     * 更新数据源
     */
    void updateDatasource(Long id, ReportDatasourceDTO datasourceDTO);

    /**
     * 删除数据源
     */
    void deleteDatasource(Long id);

    /**
     * 获取数据源详情
     */
    ReportDatasourceVO getDatasourceDetail(Long id);

    /**
     * 获取所有数据源列表
     */
    List<ReportDatasourceVO> listAllDatasources();

    /**
     * 测试数据源连接
     */
    boolean testConnection(Long id);

    /**
     * 执行SQL查询（用于报表数据获取）
     */
    List<Map<String, Object>> executeQuery(Long datasourceId, String sql, Map<String, Object> params);

    /**
     * 获取数据源的表列表
     */
    List<String> getTables(Long datasourceId);

    /**
     * 获取表的字段列表
     */
    List<Map<String, Object>> getTableColumns(Long datasourceId, String tableName);
}
