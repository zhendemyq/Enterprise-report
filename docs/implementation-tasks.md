# 实施任务清单（可勾选）

## 第一阶段：核心功能补全（第1周）

### Task 1.1 Excel转PDF功能
- [ ] 创建 `PdfConvertService` 接口
- [ ] 实现 `PdfConvertServiceImpl`
- [ ] 添加中文字体支持（SimSun/SimHei）
- [ ] 处理合并单元格转换
- [ ] 处理表格边框样式
- [ ] 修改 `ReportGenerateServiceImpl.convertToPdf()`
- [ ] 单元测试
- [ ] 功能验证

### Task 1.2 邮件发送功能
- [ ] 创建 `EmailService` 接口
- [ ] 实现 `EmailServiceImpl`
- [ ] 创建邮件HTML模板
- [ ] 实现附件发送
- [ ] 集成到 `ReportScheduleServiceImpl.executeNow()`
- [ ] 添加发送日志记录
- [ ] 配置测试邮箱验证
- [ ] 功能验证

### Task 1.3 PDF在线预览
- [ ] 创建 `PdfPreview.vue` 组件
- [ ] 集成 vue-pdf-embed
- [ ] 添加翻页控制
- [ ] 添加缩放控制
- [ ] 集成到报表记录页面
- [ ] 功能验证

---

## 第二阶段：报表设计器集成（第2周）

### Task 2.1 Univer基础集成
- [ ] 研究Univer API（0.5天）
- [ ] 创建 `useUniver.js` composable
- [ ] 创建 `SpreadsheetDesigner.vue` 组件
- [ ] 实现Univer实例初始化
- [ ] 实现工作表创建
- [ ] 实现数据读取/写入
- [ ] 功能验证

### Task 2.2 设计器功能完善
- [ ] 实现样式面板联动
- [ ] 实现合并单元格功能
- [ ] 实现公式支持
- [ ] 实现设计数据序列化（toJSON）
- [ ] 实现设计数据反序列化（fromJSON）
- [ ] 重构 `design.vue` 页面
- [ ] 功能验证

### Task 2.3 字段绑定机制
- [ ] 定义绑定数据结构
- [ ] 实现字段拖拽功能
- [ ] 实现绑定标记显示
- [ ] 创建 `TemplateParserService`
- [ ] 实现后端模板解析
- [ ] 端到端测试

---

## 第三阶段：调度与权限（第3周）

### Task 3.1 XXL-JOB集成
- [ ] 创建 `ReportJobHandler`
- [ ] 实现 `@XxlJob` 注解方法
- [ ] 创建 `XxlJobService` 接口
- [ ] 实现任务注册API调用
- [ ] 实现任务更新/删除
- [ ] 修改 `ReportScheduleServiceImpl`
- [ ] 与XXL-JOB Admin联调
- [ ] 功能验证

### Task 3.2 权限管理界面
- [ ] 创建 `PermissionDialog.vue`
- [ ] 实现角色选择
- [ ] 实现权限类型选择
- [ ] 完善 `PermissionController`
- [ ] 实现权限保存接口
- [ ] 集成到模板管理页面
- [ ] 实现列表权限过滤
- [ ] 功能验证

### Task 3.3 系统配置管理
- [ ] 创建 `sys_config` 表
- [ ] 创建 `SystemConfig` 实体
- [ ] 创建配置CRUD接口
- [ ] 创建 `config/index.vue` 页面
- [ ] 实现配置热加载
- [ ] 功能验证

---

## 第四阶段：测试与优化（第4周）

### Task 4.1 单元测试
- [ ] `ReportGenerateServiceTest`
- [ ] `ReportTemplateServiceTest`
- [ ] `ReportDatasourceServiceTest`
- [ ] `ReportScheduleServiceTest`
- [ ] `PdfConvertServiceTest`
- [ ] `EmailServiceTest`
- [ ] 测试覆盖率检查

### Task 4.2 集成测试
- [ ] 编写测试用例文档
- [ ] 报表生成全流程测试
- [ ] 定时任务全流程测试
- [ ] 模板设计全流程测试
- [ ] 问题修复

### Task 4.3 性能测试
- [ ] 准备10万行测试数据脚本
- [ ] 执行性能测试
- [ ] 记录测试数据
- [ ] 分析性能瓶颈
- [ ] 实施优化措施
- [ ] 复测验证

---

## 第五阶段：文档与收尾（第5周）

### Task 5.1 API文档
- [ ] 补充Swagger注解
- [ ] 添加请求示例
- [ ] 添加响应示例
- [ ] 接口分组整理
- [ ] 文档审查

### Task 5.2 论文素材
- [ ] 绘制系统架构图
- [ ] 绘制E-R图
- [ ] 绘制用例图
- [ ] 绘制时序图
- [ ] 截取功能截图
- [ ] 整理性能数据

### Task 5.3 部署文档
- [ ] 编写环境要求
- [ ] 编写后端部署步骤
- [ ] 编写前端部署步骤
- [ ] 编写XXL-JOB配置
- [ ] 编写Nginx配置
- [ ] 文档审查

---

## 进度跟踪

| 阶段 | 计划开始 | 计划结束 | 实际开始 | 实际结束 | 状态 |
|------|----------|----------|----------|----------|------|
| 第一阶段 | | | | | 未开始 |
| 第二阶段 | | | | | 未开始 |
| 第三阶段 | | | | | 未开始 |
| 第四阶段 | | | | | 未开始 |
| 第五阶段 | | | | | 未开始 |

---

## 问题记录

| 日期 | 问题描述 | 解决方案 | 状态 |
|------|----------|----------|------|
| | | | |

