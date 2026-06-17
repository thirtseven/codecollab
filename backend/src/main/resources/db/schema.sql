CREATE DATABASE IF NOT EXISTS codecollab DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE codecollab;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    phone VARCHAR(20) DEFAULT NULL,
    bio VARCHAR(500) DEFAULT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    status INT NOT NULL DEFAULT 1,
    github_url VARCHAR(200) DEFAULT NULL,
    website VARCHAR(200) DEFAULT NULL,
    last_login_time DATETIME DEFAULT NULL,
    last_login_ip VARCHAR(50) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) NOT NULL,
    code VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    expire_time DATETIME NOT NULL,
    used INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email_type (email, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    creator_id BIGINT NOT NULL,
    max_members INT NOT NULL DEFAULT 50,
    status INT NOT NULL DEFAULT 1,
    invite_code VARCHAR(20) DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    INDEX idx_creator (creator_id),
    INDEX idx_invite_code (invite_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_team_user (team_id, user_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS projects (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    team_id BIGINT NOT NULL,
    creator_id BIGINT NOT NULL,
    language VARCHAR(50) DEFAULT NULL,
    repository_url VARCHAR(500) DEFAULT NULL,
    status INT NOT NULL DEFAULT 1,
    is_public INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    INDEX idx_team (team_id),
    INDEX idx_creator (creator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS project_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    content LONGTEXT,
    file_type VARCHAR(50) DEFAULT NULL,
    file_size BIGINT DEFAULT 0,
    version INT NOT NULL DEFAULT 1,
    creator_id BIGINT NOT NULL,
    parent_id BIGINT DEFAULT NULL,
    is_directory INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    INDEX idx_project (project_id),
    INDEX idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS code_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    branch VARCHAR(100) DEFAULT 'main',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    review_comment TEXT,
    reviewer_id BIGINT DEFAULT NULL,
    reviewed_at DATETIME DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    INDEX idx_project (project_id),
    INDEX idx_user (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS submission_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    submission_id BIGINT NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(200) NOT NULL,
    content LONGTEXT,
    diff_content LONGTEXT,
    change_type VARCHAR(20) DEFAULT 'MODIFY',
    file_size BIGINT DEFAULT 0,
    INDEX idx_submission (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_conversations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(200) DEFAULT 'New Conversation',
    project_id BIGINT DEFAULT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted INT NOT NULL DEFAULT 0,
    INDEX idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    conversation_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL,
    content LONGTEXT NOT NULL,
    token_count INT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(20) NOT NULL,
    resource VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    description VARCHAR(200) DEFAULT NULL,
    UNIQUE KEY uk_role_resource_action (role, resource, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS system_configs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE,
    config_value TEXT,
    description VARCHAR(200) DEFAULT NULL,
    type VARCHAR(20) DEFAULT 'STRING',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (username, email, password, role, status, nickname) VALUES
('admin', 'admin@codecollab.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', 1, 'System Admin');

INSERT INTO permissions (role, resource, action, description) VALUES
('ADMIN', 'user', 'manage', 'Manage all users'),
('ADMIN', 'team', 'manage', 'Manage all teams'),
('ADMIN', 'project', 'manage', 'Manage all projects'),
('ADMIN', 'submission', 'manage', 'Manage all submissions'),
('ADMIN', 'system', 'config', 'Configure system settings'),
('TEACHER', 'team', 'create', 'Create teams'),
('TEACHER', 'team', 'manage', 'Manage own teams'),
('TEACHER', 'project', 'review', 'Review submissions'),
('STUDENT', 'team', 'join', 'Join teams'),
('STUDENT', 'project', 'create', 'Create projects'),
('STUDENT', 'submission', 'create', 'Create submissions');

INSERT INTO system_configs (config_key, config_value, description, type) VALUES
('site_name', 'CodeCollab', 'Site name', 'STRING'),
('allow_register', 'true', 'Allow self-registration', 'BOOLEAN'),
('max_team_members', '50', 'Max members per team', 'NUMBER'),
('ai_enabled', 'true', 'Enable AI assistant', 'BOOLEAN'),
('ai_api_url', 'https://api.openai.com/v1/chat/completions', 'AI API URL', 'STRING'),
('ai_model', 'gpt-3.5-turbo', 'AI model name', 'STRING');
