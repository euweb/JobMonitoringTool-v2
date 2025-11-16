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

-- Table for imported job executions (from CSV)
CREATE TABLE IF NOT EXISTS imported_job_executions (
    execution_id BIGINT PRIMARY KEY,
    job_type VARCHAR(50),
    job_name VARCHAR(200) NOT NULL,
    script_path VARCHAR(500),
    priority INTEGER,
    strategy INTEGER,
    status VARCHAR(20),
    submitted_by VARCHAR(100),
    submitted_at TIMESTAMP,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    host VARCHAR(100),
    parent_execution_id BIGINT,
    duration_seconds BIGINT,
    import_timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    csv_source_file VARCHAR(500)
);

-- Table for job favorites
CREATE TABLE IF NOT EXISTS job_favorites (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    job_name VARCHAR(200) NOT NULL,
    user_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notify_on_failure BOOLEAN DEFAULT TRUE,
    notify_on_success BOOLEAN DEFAULT FALSE,
    notify_on_start BOOLEAN DEFAULT FALSE,
    last_notified_execution_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE(user_id, job_name)
);


-- Indexes for performance
CREATE INDEX idx_users_username ON users (username);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);

CREATE INDEX IF NOT EXISTS idx_imported_executions_job_name ON imported_job_executions(job_name);
CREATE INDEX IF NOT EXISTS idx_imported_executions_status ON imported_job_executions(status);
CREATE INDEX IF NOT EXISTS idx_imported_executions_submitted_at ON imported_job_executions(submitted_at);
CREATE INDEX IF NOT EXISTS idx_imported_executions_parent_id ON imported_job_executions(parent_execution_id);
CREATE INDEX IF NOT EXISTS idx_imported_executions_import_timestamp ON imported_job_executions(import_timestamp);

CREATE INDEX IF NOT EXISTS idx_job_favorites_user_id ON job_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_job_favorites_job_name ON job_favorites(job_name);

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
