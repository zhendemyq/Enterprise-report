-- 企业报表生成系统数据库初始化脚本
-- 数据库: enterprise_report
-- 字符集: utf8mb4

-- 创建数据库
CREATE DATABASE IF NOT EXISTS enterprise_report DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE enterprise_report;

-- ===========================
-- 系统管理相关表
-- ===========================

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) COMMENT '昵称',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    gender TINYINT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    dept_id BIGINT COMMENT '部门ID',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    remark VARCHAR(500) COMMENT '备注',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- ===========================
-- 报表管理相关表
-- ===========================

-- 报表分类表
CREATE TABLE IF NOT EXISTS report_category (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    category_name VARCHAR(50) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(50) NOT NULL UNIQUE COMMENT '分类编码',
    parent_id BIGINT DEFAULT 0 COMMENT '父级ID',
    description VARCHAR(200) COMMENT '分类描述',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    icon VARCHAR(50) COMMENT '图标',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_parent_id (parent_id),
    INDEX idx_category_code (category_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表分类表';

-- 数据源配置表
CREATE TABLE IF NOT EXISTS report_datasource (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    datasource_name VARCHAR(100) NOT NULL COMMENT '数据源名称',
    datasource_code VARCHAR(50) NOT NULL UNIQUE COMMENT '数据源编码',
    datasource_type TINYINT NOT NULL COMMENT '数据源类型 1-MySQL 2-PostgreSQL 3-Oracle 4-SQLServer 5-API接口',
    host VARCHAR(100) COMMENT '主机地址',
    port INT COMMENT '端口',
    database_name VARCHAR(100) COMMENT '数据库名',
    username VARCHAR(100) COMMENT '用户名',
    password VARCHAR(200) COMMENT '密码（加密存储）',
    connection_params JSON COMMENT '连接参数',
    api_url VARCHAR(500) COMMENT 'API地址',
    api_headers JSON COMMENT 'API请求头',
    description VARCHAR(200) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    last_test_time DATETIME COMMENT '最后测试时间',
    test_result TINYINT COMMENT '测试结果 0-失败 1-成功',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_datasource_code (datasource_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源配置表';

-- 报表模板表
CREATE TABLE IF NOT EXISTS report_template (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    template_code VARCHAR(50) NOT NULL UNIQUE COMMENT '模板编码',
    template_type TINYINT DEFAULT 1 COMMENT '模板类型 1-明细表 2-汇总表 3-分组统计表 4-图表报表',
    category_id BIGINT COMMENT '模板分类ID',
    description VARCHAR(500) COMMENT '模板描述',
    template_config JSON COMMENT '模板配置（Univer设计器JSON）',
    template_file VARCHAR(255) COMMENT '模板文件路径',
    datasource_id BIGINT COMMENT '数据源ID',
    query_sql TEXT COMMENT '查询SQL',
    param_config JSON COMMENT '参数配置',
    style_config JSON COMMENT '样式配置',
    status TINYINT DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-已下线',
    version INT DEFAULT 1 COMMENT '版本号',
    need_audit TINYINT DEFAULT 0 COMMENT '是否需要审核',
    sort INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_template_code (template_code),
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表模板表';

-- 报表生成记录表
CREATE TABLE IF NOT EXISTS report_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    template_name VARCHAR(100) COMMENT '模板名称',
    report_name VARCHAR(200) NOT NULL COMMENT '报表名称',
    generate_params JSON COMMENT '生成参数',
    file_type VARCHAR(10) DEFAULT 'xlsx' COMMENT '文件类型',
    file_path VARCHAR(255) COMMENT '文件路径',
    file_size BIGINT COMMENT '文件大小（字节）',
    data_rows INT COMMENT '数据行数',
    status TINYINT DEFAULT 0 COMMENT '状态 0-生成中 1-成功 2-失败',
    error_msg VARCHAR(500) COMMENT '错误信息',
    start_time DATETIME COMMENT '生成开始时间',
    end_time DATETIME COMMENT '生成结束时间',
    duration BIGINT COMMENT '耗时（毫秒）',
    download_count INT DEFAULT 0 COMMENT '下载次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_template_id (template_id),
    INDEX idx_create_by (create_by),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表生成记录表';

-- 定时报表任务表
CREATE TABLE IF NOT EXISTS report_schedule (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_code VARCHAR(50) UNIQUE COMMENT '任务编码',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    generate_params JSON COMMENT '生成参数',
    cron_expression VARCHAR(50) NOT NULL COMMENT 'Cron表达式',
    schedule_type TINYINT DEFAULT 4 COMMENT '调度类型 1-每日 2-每周 3-每月 4-自定义',
    file_type VARCHAR(10) DEFAULT 'xlsx' COMMENT '文件类型',
    send_email TINYINT DEFAULT 0 COMMENT '是否发送邮件',
    email_receivers VARCHAR(500) COMMENT '收件人邮箱',
    email_subject VARCHAR(200) COMMENT '邮件主题',
    email_content TEXT COMMENT '邮件内容',
    status TINYINT DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    last_execute_time DATETIME COMMENT '上次执行时间',
    next_execute_time DATETIME COMMENT '下次执行时间',
    execute_count INT DEFAULT 0 COMMENT '执行次数',
    fail_count INT DEFAULT 0 COMMENT '失败次数',
    xxl_job_id INT COMMENT 'XXL-JOB任务ID',
    description VARCHAR(200) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_template_id (template_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时报表任务表';

-- 报表权限表
CREATE TABLE IF NOT EXISTS report_permission (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '模板ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_type TINYINT NOT NULL COMMENT '权限类型 1-查看 2-生成 3-下载 4-编辑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    UNIQUE KEY uk_template_role_permission (template_id, role_id, permission_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表权限表';

-- 定时任务执行日志表
CREATE TABLE IF NOT EXISTS report_schedule_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    schedule_id BIGINT NOT NULL COMMENT '任务ID',
    task_name VARCHAR(100) COMMENT '任务名称',
    execute_params JSON COMMENT '执行参数',
    status TINYINT DEFAULT 0 COMMENT '执行状态 0-执行中 1-成功 2-失败',
    error_msg VARCHAR(1000) COMMENT '错误信息',
    report_record_id BIGINT COMMENT '生成的报表ID',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '耗时（毫秒）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除标记',
    INDEX idx_schedule_id (schedule_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志表';
