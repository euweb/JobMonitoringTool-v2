-- Initial schema creation for Job Monitoring Tool
-- SQLite compatible schema with PostgreSQL migration path in mind

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

-- Jobs table for individual job monitoring
CREATE TABLE IF NOT EXISTS jobs (
    id VARCHAR(100) PRIMARY KEY,  -- External job ID
    name VARCHAR(200) NOT NULL,
    description TEXT,
    job_type VARCHAR(50) NOT NULL,
    system_name VARCHAR(100),
    schedule_expression VARCHAR(100),  -- Cron expression or schedule info
    estimated_duration INTEGER,  -- In seconds
    priority INTEGER DEFAULT 0,
    tags TEXT,  -- JSON array of tags
    metadata TEXT,  -- JSON object for additional data
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    version INTEGER NOT NULL DEFAULT 0
);

-- Job chains for orchestrating multiple jobs
CREATE TABLE IF NOT EXISTS job_chains (
    id VARCHAR(100) PRIMARY KEY,  -- External job chain ID
    name VARCHAR(200) NOT NULL,
    description TEXT,
    chain_type VARCHAR(50) NOT NULL,
    system_name VARCHAR(100),
    schedule_expression VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL DEFAULT 'system',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) NOT NULL DEFAULT 'system',
    version INTEGER NOT NULL DEFAULT 0
);

-- Job chain dependencies (many-to-many relationship)
CREATE TABLE IF NOT EXISTS job_chain_jobs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_chain_id VARCHAR(100) NOT NULL,
    job_id VARCHAR(100) NOT NULL,
    execution_order INTEGER NOT NULL DEFAULT 0,
    is_critical BOOLEAN NOT NULL DEFAULT TRUE,  -- If job failure should fail the chain
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_chain_id) REFERENCES job_chains (id) ON DELETE CASCADE,
    FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
    UNIQUE (job_chain_id, job_id)
);

-- Job execution history
CREATE TABLE IF NOT EXISTS job_executions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_id VARCHAR(100) NOT NULL,
    job_chain_id VARCHAR(100),  -- NULL if standalone job execution
    execution_id VARCHAR(100),  -- External execution ID
    status VARCHAR(20) NOT NULL CHECK (status IN ('SCHEDULED', 'RUNNING', 'SUCCESS', 'FAILED', 'CANCELLED', 'TIMEOUT')),
    start_time DATETIME,
    end_time DATETIME,
    duration INTEGER,  -- In seconds
    exit_code INTEGER,
    log_output TEXT,
    error_message TEXT,
    triggered_by VARCHAR(50),
    execution_host VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
    FOREIGN KEY (job_chain_id) REFERENCES job_chains (id) ON DELETE SET NULL
);

-- User favorites (jobs and job chains)
CREATE TABLE IF NOT EXISTS user_favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    favorite_type VARCHAR(20) NOT NULL CHECK (favorite_type IN ('JOB', 'JOB_CHAIN')),
    favorite_id VARCHAR(100) NOT NULL,  -- job_id or job_chain_id
    display_order INTEGER DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    UNIQUE (user_id, favorite_type, favorite_id)
);

-- Notification preferences per user per job/job_chain
CREATE TABLE IF NOT EXISTS notification_preferences (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    target_type VARCHAR(20) NOT NULL CHECK (target_type IN ('JOB', 'JOB_CHAIN')),
    target_id VARCHAR(100) NOT NULL,  -- job_id or job_chain_id
    channels TEXT NOT NULL,  -- JSON array: ['EMAIL', 'SLACK', 'TEAMS']
    triggers TEXT NOT NULL,  -- JSON array: ['SUCCESS', 'FAILURE', 'START', 'DELAY']
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    custom_message TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
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

-- Job import configurations
CREATE TABLE IF NOT EXISTS job_import_configs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name VARCHAR(100) NOT NULL UNIQUE,
    import_type VARCHAR(50) NOT NULL,  -- CSV, EXTERNAL_API, etc.
    config_data TEXT NOT NULL,  -- JSON configuration
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_import_at DATETIME,
    last_import_status VARCHAR(20),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_users_username ON users (username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users (role);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);

CREATE INDEX IF NOT EXISTS idx_jobs_name ON jobs (name);
CREATE INDEX IF NOT EXISTS idx_jobs_job_type ON jobs (job_type);
CREATE INDEX IF NOT EXISTS idx_jobs_system_name ON jobs (system_name);
CREATE INDEX IF NOT EXISTS idx_jobs_enabled ON jobs (enabled);

CREATE INDEX IF NOT EXISTS idx_job_chains_name ON job_chains (name);
CREATE INDEX IF NOT EXISTS idx_job_chains_enabled ON job_chains (enabled);

CREATE INDEX IF NOT EXISTS idx_job_executions_job_id ON job_executions (job_id);
CREATE INDEX IF NOT EXISTS idx_job_executions_status ON job_executions (status);
CREATE INDEX IF NOT EXISTS idx_job_executions_start_time ON job_executions (start_time);

CREATE INDEX IF NOT EXISTS idx_user_favorites_user_id ON user_favorites (user_id);
CREATE INDEX IF NOT EXISTS idx_user_favorites_type_id ON user_favorites (favorite_type, favorite_id);

CREATE INDEX IF NOT EXISTS idx_notification_prefs_user_id ON notification_preferences (user_id);
CREATE INDEX IF NOT EXISTS idx_notification_prefs_target ON notification_preferences (target_type, target_id);

CREATE INDEX IF NOT EXISTS idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_logs_created_at ON audit_logs (created_at);