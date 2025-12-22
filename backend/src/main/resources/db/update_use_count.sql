-- 添加 use_count 字段到 report_template 表
ALTER TABLE report_template ADD COLUMN use_count INT DEFAULT 0 COMMENT '使用次数' AFTER sort;
