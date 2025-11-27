package com.example.backend.controller;

import com.example.backend.common.Result;
import com.example.backend.dto.ReportDatasourceDTO;
import com.example.backend.service.ReportDatasourceService;
import com.example.backend.vo.ReportDatasourceVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 数据源管理控制器
 */
@Tag(name = "数据源管理", description = "数据源配置、测试、查询等接口")
@RestController
@RequestMapping("/report/datasource")
public class ReportDatasourceController {

    @Autowired
    private ReportDatasourceService datasourceService;

    @Operation(summary = "创建数据源")
    @PostMapping
    public Result<Long> createDatasource(@Valid @RequestBody ReportDatasourceDTO datasourceDTO) {
        return Result.success(datasourceService.createDatasource(datasourceDTO));
    }

    @Operation(summary = "更新数据源")
    @PutMapping("/{id}")
    public Result<Void> updateDatasource(@PathVariable Long id, @Valid @RequestBody ReportDatasourceDTO datasourceDTO) {
        datasourceService.updateDatasource(id, datasourceDTO);
        return Result.success();
    }

    @Operation(summary = "删除数据源")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDatasource(@PathVariable Long id) {
        datasourceService.deleteDatasource(id);
        return Result.success();
    }

    @Operation(summary = "获取数据源详情")
    @GetMapping("/{id}")
    public Result<ReportDatasourceVO> getDatasourceDetail(@PathVariable Long id) {
        return Result.success(datasourceService.getDatasourceDetail(id));
    }

    @Operation(summary = "获取所有数据源列表")
    @GetMapping("/list")
    public Result<List<ReportDatasourceVO>> listAllDatasources() {
        return Result.success(datasourceService.listAllDatasources());
    }

    @Operation(summary = "测试数据源连接")
    @PostMapping("/{id}/test")
    public Result<Boolean> testConnection(@PathVariable Long id) {
        return Result.success(datasourceService.testConnection(id));
    }

    @Operation(summary = "获取数据源的表列表")
    @GetMapping("/{id}/tables")
    public Result<List<String>> getTables(@PathVariable Long id) {
        return Result.success(datasourceService.getTables(id));
    }

    @Operation(summary = "获取表的字段列表")
    @GetMapping("/{id}/tables/{tableName}/columns")
    public Result<List<Map<String, Object>>> getTableColumns(@PathVariable Long id, @PathVariable String tableName) {
        return Result.success(datasourceService.getTableColumns(id, tableName));
    }

    @Operation(summary = "执行SQL查询预览")
    @PostMapping("/{id}/query")
    public Result<List<Map<String, Object>>> executeQuery(@PathVariable Long id,
                                                          @RequestParam String sql,
                                                          @RequestBody(required = false) Map<String, Object> params) {
        return Result.success(datasourceService.executeQuery(id, sql, params));
    }
}
