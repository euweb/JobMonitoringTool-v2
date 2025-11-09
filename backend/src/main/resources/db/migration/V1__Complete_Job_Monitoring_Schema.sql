-- Complete Job Monitoring Tool Schema
-- Consolidated migration combining all functionality
-- SQLite compatible with comprehensive job monitoring features

-- Users table with RBAC support
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN', 'USER')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    version INTEGER NOT NULL DEFAULT 0
);

-- Refresh tokens for JWT authentication
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    token VARCHAR(255) NOT NULL UNIQUE,
    user_id INTEGER NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Job Monitoring Jobs table - Main job configuration
CREATE TABLE IF NOT EXISTS job_monitor_jobs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    job_type VARCHAR(20) NOT NULL CHECK (job_type IN ('SCHEDULED', 'ON_DEMAND', 'TRIGGERED')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'INACTIVE', 'PAUSED', 'DELETED')) DEFAULT 'ACTIVE',
    priority VARCHAR(20) NOT NULL CHECK (priority IN ('LOW', 'NORMAL', 'HIGH', 'CRITICAL')) DEFAULT 'NORMAL',
    cron_expression VARCHAR(100),
    command TEXT,
    working_directory VARCHAR(500),
    environment_variables TEXT,
    timeout_minutes INTEGER,
    max_retries INTEGER DEFAULT 0,
    retry_delay_minutes INTEGER DEFAULT 5,
    notification_email VARCHAR(255),
    notify_on_success BOOLEAN DEFAULT FALSE,
    notify_on_failure BOOLEAN DEFAULT TRUE,
    created_by INTEGER,
    last_modified_by INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_modified_at TIMESTAMP,
    last_executed_at TIMESTAMP,
    next_execution_at TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users (id),
    FOREIGN KEY (last_modified_by) REFERENCES users (id)
);

-- Job Executions table - Execution history and tracking
CREATE TABLE IF NOT EXISTS job_executions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_id INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('QUEUED', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED', 'TIMEOUT', 'RETRYING')),
    trigger_type VARCHAR(20) NOT NULL CHECK (trigger_type IN ('SCHEDULED', 'MANUAL', 'API', 'RETRY', 'DEPENDENCY')),
    execution_number INTEGER,
    retry_attempt INTEGER DEFAULT 0,
    started_at TIMESTAMP NOT NULL,
    ended_at TIMESTAMP,
    duration_ms INTEGER,
    exit_code INTEGER,
    output TEXT,
    error_output TEXT,
    failure_reason VARCHAR(500),
    host_name VARCHAR(100),
    process_id INTEGER,
    memory_usage_mb REAL,
    cpu_usage_percent REAL,
    triggered_by INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES job_monitor_jobs (id) ON DELETE CASCADE,
    FOREIGN KEY (triggered_by) REFERENCES users (id)
);

-- Job Schedules table - Scheduling configuration
CREATE TABLE IF NOT EXISTS job_schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_id BIGINT NOT NULL UNIQUE,
    cron_expression VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ACTIVE', 'PAUSED', 'DISABLED', 'EXPIRED')) DEFAULT 'ACTIVE',
    timezone VARCHAR(50) DEFAULT 'UTC',
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    next_execution_at TIMESTAMP,
    last_execution_at TIMESTAMP,
    execution_count BIGINT DEFAULT 0,
    max_executions INTEGER,
    allow_concurrent BOOLEAN DEFAULT FALSE,
    misfire_policy VARCHAR(50) DEFAULT 'DO_NOTHING',
    description VARCHAR(500),
    created_by INTEGER,
    last_modified_by INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES job_monitor_jobs (id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users (id),
    FOREIGN KEY (last_modified_by) REFERENCES users (id)
);

-- User favorites (jobs)
CREATE TABLE IF NOT EXISTS user_favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    favorite_type VARCHAR(20) NOT NULL CHECK (favorite_type IN ('JOB')),
    favorite_id INTEGER NOT NULL,  -- job_monitor_jobs.id
    display_order INTEGER DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (favorite_id) REFERENCES job_monitor_jobs (id) ON DELETE CASCADE,
    UNIQUE (user_id, favorite_type, favorite_id)
);

-- Notification preferences per user per job
CREATE TABLE IF NOT EXISTS notification_preferences (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('JOB')),
    target_id INTEGER NOT NULL,  -- job_monitor_jobs.id
    channels TEXT NOT NULL,  -- JSON array: ['EMAIL', 'SLACK', 'TEAMS']
    triggers TEXT NOT NULL,  -- JSON array: ['SUCCESS', 'FAILURE', 'START', 'DELAY']
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    custom_message TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (target_id) REFERENCES job_monitor_jobs (id) ON DELETE CASCADE,
    UNIQUE (user_id, target_type, target_id)
);

-- Audit log for tracking user actions and system events
CREATE TABLE IF NOT EXISTS audit_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER,  -- NULL for system events
    action VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50),
    entity_id VARCHAR(100),
    old_values TEXT,  -- JSON object
    new_values TEXT,  -- JSON object
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL
);

-- Indexes for performance
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

CREATE INDEX idx_job_monitor_jobs_status ON job_monitor_jobs (status);
CREATE INDEX idx_job_monitor_jobs_job_type ON job_monitor_jobs (job_type);
CREATE INDEX idx_job_monitor_jobs_created_by ON job_monitor_jobs (created_by);
CREATE INDEX idx_job_monitor_jobs_next_execution ON job_monitor_jobs (next_execution_at);

CREATE INDEX idx_job_executions_job_id ON job_executions (job_id);
CREATE INDEX idx_job_executions_status ON job_executions (status);
CREATE INDEX idx_job_executions_started_at ON job_executions (started_at);
CREATE INDEX idx_job_executions_trigger_type ON job_executions (trigger_type);

CREATE INDEX idx_job_schedules_job_id ON job_schedules (job_id);
CREATE INDEX idx_job_schedules_status ON job_schedules (status);
CREATE INDEX idx_job_schedules_next_execution ON job_schedules (next_execution_at);

CREATE INDEX idx_user_favorites_user_id ON user_favorites (user_id);
CREATE INDEX idx_user_favorites_type_id ON user_favorites (favorite_type, favorite_id);

CREATE INDEX idx_notification_prefs_user_id ON notification_preferences (user_id);
CREATE INDEX idx_notification_prefs_target ON notification_preferences (target_type, target_id);

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);

-- Sample data for testing

-- Insert default admin user (password: admin123)
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
    '$2a$10$ey1jQj9vFhJsV.fGcZsZPeh.NJImCdW95pnB6Zro89W4sRbuYDJf6',  -- admin123 (BCrypt)
    'System',
    'Administrator',
    'ADMIN',
    TRUE,
    'system',
    'system'
);

-- Sample jobs for testing
INSERT INTO job_monitor_jobs (name, description, job_type, status, priority, cron_expression, command, created_by) VALUES
('Daily Backup', 'Daily backup of application data', 'SCHEDULED', 'ACTIVE', 'HIGH', '0 2 * * *', 'backup_script.sh', 1),
('Weekly Report', 'Generate weekly performance report', 'SCHEDULED', 'ACTIVE', 'NORMAL', '0 9 * * 1', 'generate_report.py', 1),
('Data Cleanup', 'Clean up old temporary files', 'ON_DEMAND', 'ACTIVE', 'LOW', NULL, 'cleanup.sh', 1),
('Database Maintenance', 'Optimize and reindex database tables', 'SCHEDULED', 'ACTIVE', 'HIGH', '0 3 * * 0', 'db_maintenance.sql', 1),
('Log Archive', 'Archive old log files to storage', 'SCHEDULED', 'ACTIVE', 'NORMAL', '0 1 * * *', 'archive_logs.sh', 1);

-- Sample schedules
INSERT INTO job_schedules (job_id, cron_expression, status, timezone, created_by) VALUES
(1, '0 2 * * *', 'ACTIVE', 'UTC', 1),
(2, '0 9 * * 1', 'ACTIVE', 'UTC', 1),
(4, '0 3 * * 0', 'ACTIVE', 'UTC', 1),
(5, '0 1 * * *', 'ACTIVE', 'UTC', 1);

-- Sample execution history
INSERT INTO job_executions (job_id, status, trigger_type, execution_number, started_at, ended_at, duration_ms, exit_code, output, triggered_by) VALUES
(1, 'SUCCESS', 'SCHEDULED', 1, datetime('now', '-1 day', '+2 hours'), datetime('now', '-1 day', '+2 hours', '+15 minutes'), 900000, 0, 'Backup completed successfully', 1),
(1, 'SUCCESS', 'SCHEDULED', 2, datetime('now', '+2 hours'), datetime('now', '+2 hours', '+12 minutes'), 720000, 0, 'Backup completed successfully', 1),
(2, 'FAILED', 'SCHEDULED', 1, datetime('now', '-3 days', '+9 hours'), datetime('now', '-3 days', '+9 hours', '+5 minutes'), 300000, 1, 'Database connection failed', 1),
(3, 'SUCCESS', 'MANUAL', 1, datetime('now', '-2 hours'), datetime('now', '-2 hours', '+3 minutes'), 180000, 0, 'Cleanup completed: 1.2GB freed', 1),
(4, 'RUNNING', 'SCHEDULED', 1, datetime('now', '-30 minutes'), NULL, NULL, NULL, 'Maintenance in progress...', 1),
(5, 'SUCCESS', 'SCHEDULED', 1, datetime('now', '-1 day', '+1 hour'), datetime('now', '-1 day', '+1 hour', '+8 minutes'), 480000, 0, 'Archive completed: 500MB archived', 1);
