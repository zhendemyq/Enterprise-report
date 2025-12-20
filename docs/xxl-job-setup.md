# XXL-Job 安装配置指南 (Windows)

## 一、下载 XXL-Job

1. 访问 GitHub 发布页：https://github.com/xuxueli/xxl-job/releases
2. 下载最新版本源码（如 `xxl-job-2.4.0.zip`）
3. 解压到任意目录，如 `D:\xxl-job`

## 二、初始化数据库

### 1. 创建数据库
```sql
CREATE DATABASE IF NOT EXISTS xxl_job DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 导入表结构
执行 XXL-Job 源码包中的 SQL 文件：
- 路径：`xxl-job/doc/db/tables_xxl_job.sql`

使用 MySQL 客户端或命令行：
```powershell
mysql -u root -p xxl_job < D:\xxl-job\doc\db\tables_xxl_job.sql
```

## 三、配置 XXL-Job Admin（调度中心）

### 1. 修改配置文件
编辑 `xxl-job-admin/src/main/resources/application.properties`：

```properties
### web
server.port=8081
server.servlet.context-path=/xxl-job-admin

### datasource (修改为你的数据库配置)
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/xxl_job?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

### xxl-job, access token (可选，设置后需要在执行器也配置相同的 token)
xxl.job.accessToken=
```

### 2. 启动调度中心

**方式一：使用 Maven 命令（推荐）**
```powershell
cd D:\xxl-job\xxl-job-admin
mvn spring-boot:run
```

**方式二：打包后运行**
```powershell
cd D:\xxl-job\xxl-job-admin
mvn clean package -DskipTests
java -jar target/xxl-job-admin-2.4.0.jar
```

### 3. 访问管理后台
- 地址：http://localhost:8081/xxl-job-admin
- 默认账号：`admin`
- 默认密码：`123456`

## 四、配置执行器（本项目已配置）

本项目的 `application.yml` 已包含 XXL-Job 执行器配置：

```yaml
xxl:
  job:
    admin:
      addresses: http://localhost:8081/xxl-job-admin  # 调度中心地址
    executor:
      appname: enterprise-report-executor             # 执行器名称
      port: 9999                                       # 执行器端口
      logpath: ./logs/xxl-job                         # 日志路径
      logretentiondays: 30                            # 日志保留天数
    accessToken:                                       # 访问令牌（与调度中心一致）
```

## 五、在调度中心添加执行器

1. 登录 XXL-Job Admin 后台
2. 进入「执行器管理」
3. 点击「新增」
4. 填写信息：
   - AppName：`enterprise-report-executor`
   - 名称：`企业报表执行器`
   - 注册方式：`自动注册`
5. 保存

## 六、启动顺序

1. **先启动 MySQL 数据库**
2. **启动 XXL-Job Admin 调度中心**（端口 8081）
3. **启动本项目后端服务**（端口 8080）

## 七、验证

1. 访问 http://localhost:8081/xxl-job-admin
2. 登录后进入「执行器管理」
3. 查看 `enterprise-report-executor` 是否显示「在线」

## 八、常见问题

### Q1: 执行器一直显示离线？
- 检查防火墙是否开放 9999 端口
- 确认调度中心地址配置正确
- 查看后端日志是否有连接错误

### Q2: 数据库连接失败？
- 确认 MySQL 服务已启动
- 检查用户名密码是否正确
- 确认 xxl_job 数据库已创建

### Q3: 端口冲突？
- 修改 `server.port` 使用其他端口
- 或关闭占用端口的程序

## 九、相关链接

- 官方文档：https://www.xuxueli.com/xxl-job/
- GitHub：https://github.com/xuxueli/xxl-job
