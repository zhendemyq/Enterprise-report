package com.example.backend.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.dto.ReportDatasourceDTO;
import com.example.backend.entity.ReportDatasource;
import com.example.backend.exception.BusinessException;
import com.example.backend.mapper.ReportDatasourceMapper;
import com.example.backend.service.ReportDatasourceService;
import com.example.backend.vo.ReportDatasourceVO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据源服务实现
 */
@Service
public class ReportDatasourceServiceImpl extends ServiceImpl<ReportDatasourceMapper, ReportDatasource> implements ReportDatasourceService {

    private static final Logger logger = LoggerFactory.getLogger(ReportDatasourceServiceImpl.class);

    /**
     * 数据源连接池缓存
     */
    private final Map<Long, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();

    @Override
    public Long createDatasource(ReportDatasourceDTO datasourceDTO) {
        // 检查编码是否重复
        LambdaQueryWrapper<ReportDatasource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ReportDatasource::getDatasourceCode, datasourceDTO.getDatasourceCode());
        if (count(wrapper) > 0) {
            throw new BusinessException("数据源编码已存在");
        }

        ReportDatasource datasource = BeanUtil.copyProperties(datasourceDTO, ReportDatasource.class);
        datasource.setStatus(1);
        save(datasource);
        return datasource.getId();
    }

    @Override
    public void updateDatasource(Long id, ReportDatasourceDTO datasourceDTO) {
        ReportDatasource datasource = getById(id);
        if (datasource == null) {
            throw new BusinessException("数据源不存在");
        }

        BeanUtil.copyProperties(datasourceDTO, datasource, "id", "datasourceCode");
        updateById(datasource);

        // 清除缓存的连接池
        closeDataSource(id);
    }

    @Override
    public void deleteDatasource(Long id) {
        closeDataSource(id);
        removeById(id);
    }

    @Override
    public ReportDatasourceVO getDatasourceDetail(Long id) {
        ReportDatasource datasource = getById(id);
        if (datasource == null) {
            throw new BusinessException("数据源不存在");
        }
        return convertToVO(datasource);
    }

    @Override
    public List<ReportDatasourceVO> listAllDatasources() {
        List<ReportDatasource> list = list(new LambdaQueryWrapper<ReportDatasource>()
                .eq(ReportDatasource::getStatus, 1)
                .orderByDesc(ReportDatasource::getCreateTime));
        return list.stream().map(this::convertToVO).toList();
    }

    @Override
    public boolean testConnection(Long id) {
        ReportDatasource datasource = getById(id);
        if (datasource == null) {
            throw new BusinessException("数据源不存在");
        }

        boolean success = false;
        try {
            HikariDataSource ds = getOrCreateDataSource(datasource);
            try (Connection conn = ds.getConnection()) {
                success = conn.isValid(5);
            }
        } catch (Exception e) {
            logger.error("数据源连接测试失败", e);
        }

        // 更新测试结果
        datasource.setLastTestTime(LocalDateTime.now());
        datasource.setTestResult(success ? 1 : 0);
        updateById(datasource);

        return success;
    }

    @Override
    public List<Map<String, Object>> executeQuery(Long datasourceId, String sql, Map<String, Object> params) {
        ReportDatasource datasource = getById(datasourceId);
        if (datasource == null) {
            throw new BusinessException("数据源不存在");
        }

        try {
            HikariDataSource ds = getOrCreateDataSource(datasource);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

            // 处理参数替换
            String execSql = replaceSqlParams(sql, params);
            logger.debug("执行SQL: {}", execSql);

            return jdbcTemplate.queryForList(execSql);
        } catch (Exception e) {
            logger.error("SQL执行失败", e);
            throw new BusinessException("SQL执行失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getTables(Long datasourceId) {
        ReportDatasource datasource = getById(datasourceId);
        if (datasource == null) {
            throw new BusinessException("数据源不存在");
        }

        try {
            HikariDataSource ds = getOrCreateDataSource(datasource);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

            String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";
            return jdbcTemplate.queryForList(sql, String.class, datasource.getDatabaseName());
        } catch (Exception e) {
            logger.error("获取表列表失败", e);
            throw new BusinessException("获取表列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<Map<String, Object>> getTableColumns(Long datasourceId, String tableName) {
        ReportDatasource datasource = getById(datasourceId);
        if (datasource == null) {
            throw new BusinessException("数据源不存在");
        }

        try {
            HikariDataSource ds = getOrCreateDataSource(datasource);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);

            String sql = "SELECT column_name, data_type, column_comment " +
                    "FROM information_schema.columns " +
                    "WHERE table_schema = ? AND table_name = ?";
            return jdbcTemplate.queryForList(sql, datasource.getDatabaseName(), tableName);
        } catch (Exception e) {
            logger.error("获取字段列表失败", e);
            throw new BusinessException("获取字段列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取或创建数据源连接池
     */
    private HikariDataSource getOrCreateDataSource(ReportDatasource datasource) {
        return dataSourceCache.computeIfAbsent(datasource.getId(), id -> {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(buildJdbcUrl(datasource));
            config.setUsername(datasource.getUsername());
            config.setPassword(datasource.getPassword());
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            return new HikariDataSource(config);
        });
    }

    /**
     * 构建JDBC URL
     */
    private String buildJdbcUrl(ReportDatasource datasource) {
        return switch (datasource.getDatasourceType()) {
            case 1 -> String.format("jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai",
                    datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
            case 2 -> String.format("jdbc:postgresql://%s:%d/%s",
                    datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
            case 3 -> String.format("jdbc:oracle:thin:@%s:%d:%s",
                    datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
            case 4 -> String.format("jdbc:sqlserver://%s:%d;databaseName=%s",
                    datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
            default -> throw new BusinessException("不支持的数据源类型");
        };
    }

    /**
     * 关闭数据源连接池
     */
    private void closeDataSource(Long id) {
        HikariDataSource ds = dataSourceCache.remove(id);
        if (ds != null) {
            ds.close();
        }
    }

    /**
     * 替换SQL参数
     */
    private String replaceSqlParams(String sql, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return sql;
        }

        String result = sql;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            Object value = entry.getValue();
            String replacement = value == null ? "NULL" : 
                    (value instanceof Number ? value.toString() : "'" + value.toString() + "'");
            result = result.replace(placeholder, replacement);
        }
        return result;
    }

    private ReportDatasourceVO convertToVO(ReportDatasource datasource) {
        ReportDatasourceVO vo = BeanUtil.copyProperties(datasource, ReportDatasourceVO.class);
        // VO中不包含password字段，BeanUtil.copyProperties不会复制该字段

        // 设置类型名称
        if (datasource.getDatasourceType() != null) {
            String[] typeNames = {"", "MySQL", "PostgreSQL", "Oracle", "SQLServer", "API接口"};
            vo.setDatasourceTypeName(typeNames[datasource.getDatasourceType()]);
        }

        // 设置状态名称
        if (datasource.getStatus() != null) {
            vo.setStatusName(datasource.getStatus() == 1 ? "正常" : "禁用");
        }

        // 设置测试结果名称
        if (datasource.getTestResult() != null) {
            vo.setTestResultName(datasource.getTestResult() == 1 ? "成功" : "失败");
        }

        return vo;
    }
}
