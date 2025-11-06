-- Insert test users
-- Password 'admin123' -> bcrypt hash
-- Password 'user123' -> bcrypt hash

-- Admin user
INSERT OR REPLACE INTO users (
    id, username, password_hash, email, first_name, last_name, role,
    enabled, account_non_expired, account_non_locked, credentials_non_expired,
    created_at, created_by, version
) VALUES (
    1, 'admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 
    'admin@jobmonitor.com', 'System', 'Administrator', 'ADMIN',
    1, 1, 1, 1, datetime('now'), 'system', 0
);

-- Test user
INSERT OR REPLACE INTO users (
    id, username, password_hash, email, first_name, last_name, role,
    enabled, account_non_expired, account_non_locked, credentials_non_expired,
    created_at, created_by, version
) VALUES (
    2, 'user', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'user@jobmonitor.com', 'Test', 'User', 'USER',
    1, 1, 1, 1, datetime('now'), 'system', 0
);

-- Another test user
INSERT OR REPLACE INTO users (
    id, username, password_hash, email, first_name, last_name, role,
    enabled, account_non_expired, account_non_locked, credentials_non_expired,
    created_at, created_by, version
) VALUES (
    3, 'testuser', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.',
    'test@jobmonitor.com', 'Test', 'User2', 'USER',
    1, 1, 1, 1, datetime('now'), 'system', 0
);