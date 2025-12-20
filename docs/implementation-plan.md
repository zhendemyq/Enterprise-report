# 企业报表生成系统 - 未完成功能实施计划

> 制定日期：2025年12月8日  
> 预计完成周期：4-5周  
> 当前进度：80%  
> 目标进度：100%

---

## 一、实施计划总览

| 阶段 | 时间 | 主要任务 | 优先级 |
|------|------|----------|--------|
| 第一阶段 | 第1周 | Excel转PDF、邮件发送 | 高 |
| 第二阶段 | 第2周 | Univer报表设计器集成 | 高 |
| 第三阶段 | 第3周 | XXL-JOB调度集成、权限完善 | 中 |
| 第四阶段 | 第4周 | 测试与性能优化 | 中 |
| 第五阶段 | 第5周 | 文档完善、论文素材准备 | 低 |

---

## 二、第一阶段：核心功能补全（第1周）

### 2.1 Excel转PDF功能实现

**目标**：实现报表从Excel格式转换为PDF格式

**技术方案**：使用 Apache POI + iText7 实现转换

**实施步骤**：

1. 创建 `PdfConvertService` 服务类
2. 实现 Excel 读取与解析
3. 使用 iText7 生成 PDF 文档
4. 处理表格样式、合并单元格、字体等
5. 集成到 `ReportGenerateServiceImpl.convertToPdf()` 方法

**代码位置**：
- `backend/src/main/java/com/example/backend/service/PdfConvertService.java`（新建）
- `backend/src/main/java/com/example/backend/service/impl/PdfConvertServiceImpl.java`（新建）
- `backend/src/main/java/com/example/backend/service/impl/ReportGenerateServiceImpl.java`（修改）

**预计工时**：2天

**验收标准**：
- [ ] 支持基本表格转换
- [ ] 支持合并单元格
- [ ] 支持中文字体
- [ ] 文件大小合理（<10MB/万行）

---

### 2.2 邮件发送功能实现

**目标**：实现定时报表的邮件推送功能

**技术方案**：Spring Boot Mail + 模板引擎

**实施步骤**：

1. 创建 `EmailService` 服务接口
2. 实现邮件发送逻辑（支持附件）
3. 创建邮件模板（HTML格式）
4. 集成到定时任务执行流程
5. 添加发送失败重试机制

**代码位置**：
- `backend/src/main/java/com/example/backend/service/EmailService.java`（新建）
- `backend/src/main/java/com/example/backend/service/impl/EmailServiceImpl.java`（新建）
- `backend/src/main/resources/templates/email/report-notification.html`（新建）
- `backend/src/main/java/com/example/backend/service/impl/ReportScheduleServiceImpl.java`（修改）

**预计工时**：1.5天

**验收标准**：
- [ ] 支持多收件人
- [ ] 支持附件（Excel/PDF）
- [ ] 邮件内容包含报表基本信息
- [ ] 发送失败有日志记录

---

### 2.3 PDF在线预览完善

**目标**：前端集成PDF预览组件

**实施步骤**：

1. 完善后端预览接口（流式返回）
2. 前端集成 vue-pdf-embed 组件
3. 添加预览弹窗组件
4. 处理大文件分页加载

**代码位置**：
- `frontend/src/components/PdfPreview.vue`（新建）
- `frontend/src/views/report/records/index.vue`（修改）

**预计工时**：1天

**验收标准**：
- [ ] PDF正常显示
- [ ] 支持翻页
- [ ] 支持缩放
- [ ] 加载状态提示

---

## 三、第二阶段：报表设计器集成（第2周）

### 3.1 Univer组件深度集成

**目标**：将模拟的表格设计器替换为真正的Univer组件

**技术方案**：@univerjs/sheets + @univerjs/sheets-ui

**实施步骤**：

1. 研究Univer API文档
2. 创建Univer实例初始化逻辑
3. 实现工作表数据双向绑定
4. 实现样式配置面板联动
5. 实现设计数据序列化/反序列化
6. 字段拖拽绑定功能

**代码位置**：
- `frontend/src/components/SpreadsheetDesigner.vue`（新建）
- `frontend/src/composables/useUniver.js`（新建）
- `frontend/src/views/report/template/design.vue`（重构）

**预计工时**：4天

**验收标准**：
- [ ] 表格可编辑
- [ ] 支持合并单元格
- [ ] 支持公式输入
- [ ] 支持样式设置（字体、颜色、边框）
- [ ] 设计数据可保存为JSON
- [ ] 可从JSON恢复设计

---

### 3.2 模板与数据绑定机制

**目标**：实现单元格与数据源字段的绑定

**实施步骤**：

1. 定义字段绑定数据结构
2. 实现拖拽绑定交互
3. 绑定标记可视化显示
4. 后端解析绑定配置生成报表

**代码位置**：
- `frontend/src/utils/bindingParser.js`（新建）
- `backend/src/main/java/com/example/backend/service/TemplateParserService.java`（新建）

**预计工时**：2天

**验收标准**：
- [ ] 字段可拖拽到单元格
- [ ] 绑定字段有视觉标识
- [ ] 支持循环区域标记
- [ ] 后端能正确解析并填充数据

---

## 四、第三阶段：调度与权限（第3周）

### 4.1 XXL-JOB调度集成

**目标**：实现定时任务与XXL-JOB Admin的对接

**实施步骤**：

1. 创建XXL-JOB执行器Handler
2. 实现任务注册/更新/删除API调用
3. 任务状态同步机制
4. 执行日志回调处理

**代码位置**：
- `backend/src/main/java/com/example/backend/job/ReportJobHandler.java`（新建）
- `backend/src/main/java/com/example/backend/service/XxlJobService.java`（新建）
- `backend/src/main/java/com/example/backend/service/impl/XxlJobServiceImpl.java`（新建）

**预计工时**：2天

**验收标准**：
- [ ] 任务可在XXL-JOB Admin查看
- [ ] 定时触发正常执行
- [ ] 执行结果正确记录
- [ ] 失败任务可重试

---

### 4.2 报表权限管理界面

**目标**：实现报表模板的权限配置界面

**实施步骤**：

1. 设计权限配置弹窗UI
2. 实现角色-权限关联接口
3. 报表列表根据权限过滤
4. 操作按钮权限控制

**代码位置**：
- `frontend/src/components/PermissionDialog.vue`（新建）
- `backend/src/main/java/com/example/backend/controller/PermissionController.java`（完善）
- `frontend/src/views/report/template/index.vue`（修改）

**预计工时**：2天

**验收标准**：
- [ ] 可配置模板的查看/生成/下载/编辑权限
- [ ] 不同角色看到不同模板
- [ ] 无权限操作有提示

---

### 4.3 系统配置管理界面

**目标**：实现系统参数的可视化配置

**实施步骤**：

1. 创建系统配置表
2. 实现配置CRUD接口
3. 前端配置管理页面
4. 配置热更新机制

**代码位置**：
- `backend/src/main/java/com/example/backend/entity/SystemConfig.java`（新建）
- `frontend/src/views/system/config/index.vue`（新建）

**预计工时**：1.5天

**验收标准**：
- [ ] 可配置报表存储路径
- [ ] 可配置最大行数限制
- [ ] 可配置缓存策略
- [ ] 配置修改即时生效

---

## 五、第四阶段：测试与优化（第4周）

### 5.1 单元测试补充

**目标**：核心服务测试覆盖率达到60%以上

**测试范围**：
- ReportGenerateService
- ReportTemplateService
- ReportDatasourceService
- ReportScheduleService

**代码位置**：
- `backend/src/test/java/com/example/backend/service/`

**预计工时**：2天

---

### 5.2 集成测试

**目标**：验证主要业务流程

**测试场景**：
1. 用户登录 → 选择模板 → 填写参数 → 生成报表 → 下载
2. 创建定时任务 → 触发执行 → 邮件发送
3. 设计模板 → 保存 → 发布 → 生成报表

**预计工时**：1天

---

### 5.3 性能测试与优化

**目标**：验证10万行数据报表生成性能

**测试内容**：
1. 准备10万行测试数据
2. 测试生成耗时
3. 测试内存占用
4. 并发生成测试（5并发）

**优化方向**：
- SQL查询优化
- 流式写入优化
- 缓存策略调整

**预计工时**：2天

**验收标准**：
- [ ] 10万行Excel生成 < 30秒
- [ ] 内存峰值 < 512MB
- [ ] 5并发无OOM

---

## 六、第五阶段：文档与收尾（第5周）

### 6.1 API文档完善

**目标**：Knife4j文档完整可用

**工作内容**：
- 补充接口描述
- 添加请求/响应示例
- 分组整理

**预计工时**：1天

---

### 6.2 论文素材准备

**目标**：准备论文所需的图表和数据

**工作内容**：
1. 系统架构图
2. 数据库E-R图
3. 主要功能截图
4. 性能测试数据表
5. 用例图、时序图

**预计工时**：2天

---

### 6.3 部署文档

**目标**：编写完整的部署指南

**内容**：
- 环境要求
- 数据库初始化
- 后端部署步骤
- 前端部署步骤
- XXL-JOB配置
- Nginx配置示例

**预计工时**：1天

---

## 七、风险与应对

| 风险 | 可能性 | 影响 | 应对措施 |
|------|--------|------|----------|
| Univer集成复杂度超预期 | 中 | 高 | 预留1天缓冲；必要时简化功能 |
| PDF转换样式丢失 | 中 | 中 | 使用成熟方案；接受部分样式损失 |
| XXL-JOB版本兼容问题 | 低 | 中 | 使用稳定版本；提前测试 |
| 性能测试不达标 | 低 | 高 | 分批生成；异步处理 |

---

## 八、里程碑检查点

| 日期 | 检查点 | 交付物 |
|------|--------|--------|
| 第1周末 | 核心功能补全 | PDF转换、邮件发送可用 |
| 第2周末 | 设计器可用 | Univer集成完成，可设计模板 |
| 第3周末 | 功能完整 | 所有功能可正常使用 |
| 第4周末 | 质量达标 | 测试通过，性能达标 |
| 第5周末 | 项目完成 | 文档齐全，可交付 |

---

## 九、资源需求

### 开发环境
- JDK 17+
- Node.js 18+
- MySQL 8.0
- Redis 6.0+
- XXL-JOB Admin 2.4.0

### 测试数据
- 10万行销售数据（可用脚本生成）
- 多种模板样例

### 参考资料
- [Univer官方文档](https://univer.ai/docs)
- [EasyExcel文档](https://easyexcel.opensource.alibaba.com/)
- [iText7文档](https://itextpdf.com/resources)
- [XXL-JOB文档](https://www.xuxueli.com/xxl-job/)

---

*计划制定完成，建议每周进行一次进度回顾，及时调整计划。*
