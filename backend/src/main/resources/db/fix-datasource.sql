-- 修复数据源配置,将远程数据库改为本地数据库
-- 这样可以在演示环境中正常运行

-- 更新ID=2的数据源,指向本地数据库
UPDATE report_datasource 
SET host = 'localhost',
    database_name = 'enterprise_report',
    username = 'root',
    password = 'root',
    description = '本地开发数据库(演示用-销售数据)',
    test_result = 0,
    last_test_time = NULL
WHERE id = 2;

-- 查看更新结果
SELECT id, datasource_name, host, database_name, description 
FROM report_datasource 
WHERE id = 2;
