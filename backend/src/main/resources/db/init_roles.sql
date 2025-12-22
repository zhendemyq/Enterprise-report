-- 初始化系统角色数据
-- 执行时间: 系统首次部署或升级时执行

-- 系统核心角色（使用 INSERT IGNORE 避免重复插入）

-- 1. 系统管理员 - 拥有所有权限，无需配置
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_ADMIN', '系统管理员', '拥有系统所有权限，可管理所有功能', '#FF3B30', 1, 1);

-- 2. 报表管理员 - 管理报表模板和数据源
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_REPORT_MANAGER', '报表管理员', '管理报表模板、数据源配置，拥有所有报表的编辑权限', '#FF9500', 2, 1);

-- 3. 报表用户 - 查看和生成报表
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_REPORT_USER', '报表用户', '可查看和生成所有已发布的报表', '#34C759', 3, 1);

-- 4. 普通用户 - 基础权限，需要配置具体报表权限
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_USER', '普通用户', '基础用户角色，需要单独配置报表访问权限', '#007AFF', 4, 1);

-- 部门主管角色（可选，根据业务需求添加）

-- 5. 财务主管 - 财务相关报表权限
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_FINANCE_MANAGER', '财务主管', '负责财务相关报表的查看、生成和下载', '#5856D6', 10, 1);

-- 6. 人事主管 - 人事相关报表权限
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_HR_MANAGER', '人事主管', '负责人事相关报表的查看、生成和下载', '#AF52DE', 11, 1);

-- 7. 销售主管 - 销售相关报表权限
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_SALES_MANAGER', '销售主管', '负责销售相关报表的查看、生成和下载', '#FF2D55', 12, 1);

-- 8. 仓库主管 - 仓库相关报表权限
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_WAREHOUSE_MANAGER', '仓库主管', '负责仓库相关报表的查看、生成和下载', '#00C7BE', 13, 1);

-- 9. 数据分析师 - 数据分析相关权限
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_DATA_ANALYST', '数据分析师', '负责数据分析和报表统计工作', '#32ADE6', 14, 1);

-- 10. 报表查看员 - 只读权限
INSERT IGNORE INTO sys_role (role_code, role_name, description, color, sort, status) VALUES
('ROLE_REPORT_VIEWER', '报表查看员', '只能查看报表，不能生成和下载', '#A2845E', 15, 1);

-- 为admin用户分配系统管理员角色（如果存在）
INSERT IGNORE INTO sys_user_role (user_id, role_id)
SELECT u.id, r.id
FROM sys_user u, sys_role r
WHERE u.username = 'admin' AND r.role_code = 'ROLE_ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur
    WHERE ur.user_id = u.id AND ur.role_id = r.id
);

-- 权限说明：
-- ====================================
-- 角色                  | 权限说明
-- ====================================
-- ROLE_ADMIN           | 所有权限，自动拥有，无需配置
-- ROLE_REPORT_MANAGER  | 所有报表的编辑权限（包含查看、生成、下载）
-- ROLE_REPORT_USER     | 所有已发布报表的查看和生成权限
-- ROLE_USER            | 需通过 report_permission 表配置具体权限
-- 部门主管角色          | 需通过 report_permission 表配置部门相关报表权限
-- ====================================
