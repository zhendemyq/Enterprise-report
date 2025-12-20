# SQL参数处理修复说明

## 问题描述

在生成报表时遇到SQL语法错误:
```
java.sql.SQLSyntaxErrorException: You have an error in your SQL syntax; 
check the manual that corresponds to your MySQL server version for the right syntax 
to use near ':statMonth GROUP BY dept_name' at line 1
```

## 根本原因

1. **SQL参数格式不支持**: 原代码只支持`${paramName}`格式,不支持`:paramName`格式
2. **参数字段名不匹配**: 数据库配置使用`field`字段,前端期望`name`字段
3. **缺少参数验证**: 没有在执行SQL前验证必需参数

## 修复内容

### 1. ReportDatasourceServiceImpl - SQL参数处理增强

#### 新增参数验证方法
```java
private void validateSqlParams(String sql, Map<String, Object> params)
```
- 使用正则表达式查找SQL中的`:paramName`和`${paramName}`占位符
- 验证所有必需参数是否都已提供
- 如果参数缺失,抛出明确的业务异常

#### 增强参数替换方法
```java
private String replaceSqlParams(String sql, Map<String, Object> params)
```
- **支持两种参数格式**:
  - `${paramName}` - 原有格式
  - `:paramName` - 新增支持的命名参数格式
- **智能类型处理**:
  - `NULL` → `NULL`
  - 数字 → 直接输出: `123`
  - 字符串 → 加引号并转义: `'2024-05'`
- **SQL注入防护**: 转义字符串中的单引号(`'` → `''`)

### 2. ReportTemplateServiceImpl - 参数配置兼容

#### 修改convertToVO方法
```java
// 转换参数配置: 将 field 字段映射为 name 字段(兼容前端)
if (template.getParamConfig() != null && !template.getParamConfig().isEmpty()) {
    List<Map<String, Object>> params = template.getParamConfig();
    for (Map<String, Object> param : params) {
        if (param.containsKey("field") && !param.containsKey("name")) {
            param.put("name", param.get("field"));
        }
    }
    vo.setParams(params);
}
```
- 将数据库中的`field`字段复制为`name`字段
- 确保前端可以通过`param.name`访问参数名

### 3. ReportTemplateVO - 添加兼容字段

```java
/**
 * 参数配置(前端兼容字段,与paramConfig相同)
 */
private List<Map<String, Object>> params;
```
- 添加`params`字段作为`paramConfig`的别名
- 前端可以同时使用两个字段名访问参数配置

## 使用示例

### 数据库配置示例
```json
{
  "label": "统计月份",
  "field": "statMonth",
  "type": "month",
  "required": true
}
```

### SQL模板示例
```sql
-- 支持两种格式
SELECT * FROM finance_profit WHERE stat_month = :statMonth
SELECT * FROM finance_profit WHERE stat_month = ${statMonth}
```

### 前端调用示例
```javascript
const data = {
  templateId: 1,
  reportName: '月度利润汇总',
  fileType: 'xlsx',
  params: {
    statMonth: '2024-05'  // 参数名对应SQL中的占位符
  }
}
await generateReport(data)
```

## 测试步骤

1. **重启后端服务**
   ```bash
   cd backend
   ./mvnw spring-boot:run
   ```

2. **访问报表生成页面**
   - 选择"月度利润汇总"模板
   - 填写"统计月份"参数(如: 2024-05)
   - 点击"生成报表"

3. **预期结果**
   - 参数验证通过
   - SQL正确替换为: `WHERE stat_month = '2024-05'`
   - 报表成功生成

## 错误处理

### 参数缺失
```
业务异常: 缺少必需的SQL参数: statMonth
```
→ 检查前端是否正确填写并传递参数

### SQL语法错误
```
业务异常: SQL执行失败: bad SQL grammar
```
→ 检查SQL模板语法和参数替换结果

## 安全考虑

1. **SQL注入防护**: 字符串参数自动转义单引号
2. **参数验证**: 执行前验证所有必需参数
3. **类型安全**: 根据值类型智能格式化

## 后续优化建议

1. 使用PreparedStatement替代字符串拼接(更安全)
2. 支持更多参数类型(日期、布尔等)
3. 添加参数格式验证(如日期格式)
4. 支持参数默认值配置
