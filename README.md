# Enterprise Report System

企业报表生成系统 - 毕业设计项目

## 📊 项目概述

企业报表生成系统是一个功能完整的报表管理平台，支持多数据源接入、在线模板设计、定时任务调度和多格式导出。

## 🏗️ 项目结构

```
├── backend/          # 后端服务 (Spring Boot 3.2.0)
├── frontend/         # 前端应用 (Vue 3 + Vite)
├── xxl-job-3.2.0/    # 定时任务调度中心
└── docs/             # 项目文档
```

## 🛠️ 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.0 | 核心框架 |
| MyBatis Plus | 3.5.5 | ORM框架 |
| Sa-Token | 1.37.0 | 认证授权 |
| EasyExcel | 3.3.3 | Excel处理 |
| Apache POI | 5.2.5 | Office文档 |
| Jxls | 2.14.0 | 模板引擎 |
| XXL-JOB | 2.4.0 | 任务调度 |
| Redis | 6.x | 缓存 |
| MySQL | 8.0 | 数据库 |
| Knife4j | 4.x | API文档 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 核心框架 |
| Vite | 5.0.10 | 构建工具 |
| Element Plus | 2.4.4 | UI组件库 |
| Pinia | 2.1.7 | 状态管理 |
| Vue Router | 4.2.5 | 路由管理 |
| Axios | 1.6.2 | HTTP客户端 |
| ECharts | 5.4.3 | 图表库 |
| vue-pdf-embed | 1.2.0 | PDF预览 |

## ✨ 功能特性

### 已完成功能 ✅
- [x] **用户管理** - 用户CRUD、角色分配、权限控制
- [x] **角色管理** - RBAC权限模型、菜单权限配置
- [x] **报表模板管理** - 模板设计、发布/下线、版本控制
- [x] **数据源管理** - MySQL/PostgreSQL/Oracle/SQLServer/API
- [x] **报表生成** - Excel/PDF导出、同步/异步生成
- [x] **定时任务** - XXL-JOB集成、Cron表达式、邮件通知
- [x] **报表分类** - 树形结构分类管理
- [x] **Dashboard** - 统计卡片、趋势图、热门模板
- [x] **缓存支持** - Redis缓存集成
- [x] **API文档** - Knife4j在线文档

### 预留接口 ⏳
- [ ] **SSO单点登录** - CAS/OAuth2/SAML支持
- [ ] **LDAP认证** - Active Directory集成

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis 6.x+

### 后端启动
```bash
cd backend
./mvnw spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### XXL-JOB调度中心
```bash
cd xxl-job-3.2.0/xxl-job-admin
mvn spring-boot:run
```

## 📁 目录说明

### 后端目录
```
backend/src/main/java/com/example/backend/
├── common/         # 通用类（Result、ResultCode）
├── config/         # 配置类
├── controller/     # 控制器
├── dto/            # 数据传输对象
├── entity/         # 实体类
├── exception/      # 异常处理
├── job/            # 定时任务
├── mapper/         # MyBatis映射
├── service/        # 服务层
├── util/           # 工具类
└── vo/             # 视图对象
```

### 前端目录
```
frontend/src/
├── api/            # API接口
├── components/     # 公共组件
├── layout/         # 布局组件
├── router/         # 路由配置
├── stores/         # Pinia状态
├── styles/         # 样式文件
├── utils/          # 工具函数
└── views/          # 页面视图
```

## 📊 测试覆盖

| 测试类型 | 用例数 | 通过率 |
|----------|--------|--------|
| 单元测试 | 36 | 100% |
| 集成测试 | 18 | 100% |
| 性能测试 | 10 | 100% |

### 性能指标
- 10万行数据查询：< 30秒
- 10万行Excel导出：< 30秒
- 100并发响应时间：< 1秒

## 📝 相关文档

- [测试报告](docs/test-report.md)
- [实现计划](docs/implementation-plan.md)
- [XXL-JOB配置](docs/xxl-job-setup.md)

## 📄 License

MIT License
