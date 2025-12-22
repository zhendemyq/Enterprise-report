-- 为角色表添加颜色字段
-- 执行时间: 适用于已有数据库的升级

-- 添加 color 列（如果不存在）
ALTER TABLE sys_role ADD COLUMN IF NOT EXISTS color VARCHAR(20) DEFAULT '#007AFF' COMMENT '角色颜色' AFTER description;

-- 为现有角色设置默认颜色
UPDATE sys_role SET color = '#007AFF' WHERE color IS NULL AND role_code = 'ROLE_ADMIN';
UPDATE sys_role SET color = '#34C759' WHERE color IS NULL AND role_code = 'ROLE_USER';
UPDATE sys_role SET color = '#FF9500' WHERE color IS NULL AND role_code NOT IN ('ROLE_ADMIN', 'ROLE_USER') AND color IS NULL;
