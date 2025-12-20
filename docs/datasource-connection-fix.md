# 数据源连接失败问题解决方案

## 问题描述

在生成"区域销售日报"报表时出现数据库连接超时错误:
```
Communications link failure: Connection timed out
Host: 10.0.0.21:3306
Database: sales_dw
```

## 原因分析

ID=2的数据源("销售数仓")配置为连接到远程数据库`10.0.0.21:3306`,但该服务器在当前网络环境中**不可达**。这是示例数据中的配置,仅用于演示多数据源场景。

## 解决方案

### 方案1: 通过前端数据源管理页面修改(推荐)

1. **登录系统**,进入"系统管理" → "数据源管理"

2. **找到"销售数仓"数据源**,点击"编辑"

3. **修改连接信息**为本地数据库:
   - 主机地址: `localhost`
   - 端口: `3306`
   - 数据库名: `enterprise_report`
   - 用户名: `root`
   - 密码: `root`
   - 描述: `本地开发数据库(演示用)`

4. **测试连接**,确保连接成功

5. **保存配置**

### 方案2: 直接执行SQL更新(快速)

使用MySQL客户端工具(如Navicat、DBeaver、MySQL Workbench)连接到本地数据库,执行以下SQL:

```sql
-- 更新数据源配置
UPDATE report_datasource 
SET host = 'localhost',
    database_name = 'enterprise_report',
    username = 'root',
    password = 'root',
    description = '本地开发数据库(演示用-销售数据)',
    test_result = 0,
    last_test_time = NULL
WHERE id = 2;
```

或者运行准备好的SQL脚本:
```bash
# 在MySQL中执行
SOURCE backend/src/main/resources/db/fix-datasource.sql
```

### 方案3: 禁用该数据源

如果不需要使用该数据源,可以将其状态设置为禁用:

```sql
UPDATE report_datasource 
SET status = 0,
    description = CONCAT(description, ' [已禁用-网络不可达]')
WHERE id = 2;
```

然后将使用该数据源的模板改为使用ID=1的本地数据源:

```sql
UPDATE report_template 
SET datasource_id = 1 
WHERE datasource_id = 2;
```

### 方案4: 配置真实的远程数据库(生产环境)

如果在生产环境中确实需要连接远程数据库:

1. **确保网络连通**
   - 检查防火墙规则
   - 确认MySQL远程访问权限
   - 测试网络连接: `telnet 10.0.0.21 3306`

2. **配置正确的连接参数**
   - 正确的主机地址、端口
   - 有效的数据库用户名和密码
   - 必要的连接参数(超时时间等)

3. **授权远程访问**
   ```sql
   -- 在远程MySQL服务器上执行
   GRANT ALL PRIVILEGES ON sales_dw.* TO 'dw_reader'@'%' IDENTIFIED BY 'dw_reader@123';
   FLUSH PRIVILEGES;
   ```

## 快速修复脚本(PowerShell)

如果安装了MySQL命令行工具,可以运行:

```powershell
# 在项目根目录执行
$mysqlPath = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"  # 根据实际安装路径调整
& $mysqlPath -uroot -proot enterprise_report -e @"
UPDATE report_datasource 
SET host='localhost', database_name='enterprise_report', 
    username='root', password='root', 
    description='本地开发数据库(演示用)', test_result=0 
WHERE id=2;
"@
Write-Host "数据源配置已更新,请重新生成报表" -ForegroundColor Green
```

## 验证修复

修复后,可以在"数据源管理"页面:

1. **找到"销售数仓"数据源**
2. **点击"测试连接"按钮**
3. **确认显示"连接成功"**
4. **重新生成"区域销售日报"报表**

## 相关数据源和模板

使用ID=2数据源的模板:
- ID=2: 区域销售日报 (tpl_sales_region_daily)

如果该模板的查询SQL中引用的表在本地数据库不存在,可能需要:
1. 创建相应的表结构
2. 或者修改模板的查询SQL,使用本地已有的表

## 预防措施

在开发/演示环境中:
1. **使用统一的本地数据源**
2. **避免配置不可达的远程数据库**
3. **在数据源描述中注明环境信息**(开发/测试/生产)
4. **定期检查数据源连接状态**

## 相关文件

- SQL脚本: `backend/src/main/resources/db/fix-datasource.sql`
- 初始化SQL: `backend/src/main/resources/db/init.sql` (第287行)
- 服务实现: `backend/src/main/java/com/example/backend/service/impl/ReportDatasourceServiceImpl.java`
