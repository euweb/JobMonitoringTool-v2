-- Insert default admin user
-- Password: admin123 (hashed with BCrypt)
INSERT INTO users (
    username, 
    email, 
    password_hash, 
    first_name, 
    last_name, 
    role, 
    enabled,
    created_by,
    updated_by
) VALUES (
    'admin',
    'admin@jobmonitor.com',
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqyc3YJy3gEeezb3AbEVE.m',  -- admin123
    'System',
    'Administrator',
    'ADMIN',
    TRUE,
    'system',
    'system'
);

-- Sample job import configuration for CSV
INSERT INTO job_import_configs (
    name,
    import_type,
    config_data,
    enabled
) VALUES (
    'Default CSV Import',
    'CSV',
    '{"delimiter": ",", "hasHeader": true, "encoding": "UTF-8", "dateFormat": "yyyy-MM-dd HH:mm:ss"}',
    TRUE
);

-- Sample jobs for testing
INSERT INTO jobs (
    id,
    name,
    description,
    job_type,
    system_name,
    schedule_expression,
    estimated_duration,
    priority,
    enabled
) VALUES 
    ('ETL_DAILY_SALES', 'Daily Sales ETL', 'Extract, transform and load daily sales data', 'ETL', 'DWH_SYSTEM', '0 2 * * *', 3600, 1, TRUE),
    ('BACKUP_DATABASE', 'Database Backup', 'Full database backup process', 'BACKUP', 'DWH_SYSTEM', '0 23 * * *', 1800, 2, TRUE),
    ('REPORT_GENERATOR', 'Daily Reports', 'Generate daily business reports', 'REPORT', 'REPORT_SYSTEM', '0 6 * * *', 900, 1, TRUE),
    ('DATA_VALIDATION', 'Data Quality Check', 'Validate data quality and integrity', 'VALIDATION', 'DWH_SYSTEM', '0 */4 * * *', 600, 1, TRUE),
    ('ARCHIVE_CLEANUP', 'Archive Cleanup', 'Clean up old archived files', 'MAINTENANCE', 'STORAGE_SYSTEM', '0 1 * * 0', 2400, 3, TRUE);

-- Sample job chain
INSERT INTO job_chains (
    id,
    name,
    description,
    chain_type,
    system_name,
    schedule_expression,
    enabled
) VALUES 
    ('DAILY_ETL_CHAIN', 'Daily ETL Processing Chain', 'Complete daily ETL workflow including validation and reporting', 'ETL_WORKFLOW', 'DWH_SYSTEM', '0 1 * * *', TRUE);

-- Link jobs to job chain
INSERT INTO job_chain_jobs (
    job_chain_id,
    job_id,
    execution_order,
    is_critical
) VALUES 
    ('DAILY_ETL_CHAIN', 'ETL_DAILY_SALES', 1, TRUE),
    ('DAILY_ETL_CHAIN', 'DATA_VALIDATION', 2, TRUE),
    ('DAILY_ETL_CHAIN', 'REPORT_GENERATOR', 3, FALSE),
    ('DAILY_ETL_CHAIN', 'BACKUP_DATABASE', 4, FALSE);

-- Sample job executions for testing dashboard
INSERT INTO job_executions (
    job_id,
    job_chain_id,
    execution_id,
    status,
    start_time,
    end_time,
    duration,
    exit_code,
    triggered_by
) VALUES 
    ('ETL_DAILY_SALES', 'DAILY_ETL_CHAIN', 'ETL_20241105_001', 'SUCCESS', '2024-11-05 02:00:00', '2024-11-05 02:58:30', 3510, 0, 'scheduler'),
    ('DATA_VALIDATION', 'DAILY_ETL_CHAIN', 'VAL_20241105_001', 'SUCCESS', '2024-11-05 02:58:30', '2024-11-05 03:05:15', 405, 0, 'scheduler'),
    ('REPORT_GENERATOR', 'DAILY_ETL_CHAIN', 'RPT_20241105_001', 'RUNNING', '2024-11-05 03:05:15', NULL, NULL, NULL, 'scheduler'),
    ('BACKUP_DATABASE', NULL, 'BCK_20241104_001', 'SUCCESS', '2024-11-04 23:00:00', '2024-11-04 23:28:45', 1725, 0, 'scheduler'),
    ('ARCHIVE_CLEANUP', NULL, 'ARC_20241103_001', 'FAILED', '2024-11-03 01:00:00', '2024-11-03 01:15:30', 930, 1, 'scheduler');