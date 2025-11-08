-- Migration for imported job execution monitoring schema
-- This replaces the original job creation schema with import-based monitoring

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
CREATE INDEX IF NOT EXISTS idx_imported_executions_job_name ON imported_job_executions(job_name);
CREATE INDEX IF NOT EXISTS idx_imported_executions_status ON imported_job_executions(status);
CREATE INDEX IF NOT EXISTS idx_imported_executions_submitted_at ON imported_job_executions(submitted_at);
CREATE INDEX IF NOT EXISTS idx_imported_executions_parent_id ON imported_job_executions(parent_execution_id);
CREATE INDEX IF NOT EXISTS idx_imported_executions_import_timestamp ON imported_job_executions(import_timestamp);

CREATE INDEX IF NOT EXISTS idx_job_favorites_user_id ON job_favorites(user_id);
CREATE INDEX IF NOT EXISTS idx_job_favorites_job_name ON job_favorites(job_name);
