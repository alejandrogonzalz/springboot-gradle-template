-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role ENUM('ADMIN', 'USER', 'GUEST') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_date TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NULL,
    updated_by VARCHAR(50) NULL,
    deleted_at TIMESTAMP NULL,
    deleted_by VARCHAR(50) NULL,
    version BIGINT DEFAULT 0,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email),
    INDEX idx_user_active (is_active),
    INDEX idx_user_deleted_at (deleted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create user_permissions table (for ElementCollection)
CREATE TABLE IF NOT EXISTS user_permissions (
    user_id BIGINT NOT NULL,
    permission ENUM('READ', 'CREATE', 'UPDATE', 'DELETE', 'ADMIN', 'MANAGE_USERS', 'VIEW_AUDIT_LOGS') NOT NULL,
    PRIMARY KEY (user_id, permission),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_permissions_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Create refresh_tokens table
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_token (token),
    INDEX idx_refresh_token_user_id (user_id),
    INDEX idx_refresh_token_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Auto-cleanup expired tokens (optional - can also be done via scheduled job)
-- CREATE EVENT cleanup_expired_refresh_tokens ON SCHEDULE EVERY 1 DAY DO DELETE FROM refresh_tokens WHERE expires_at < NOW();
