-- 1. Create site_user table
CREATE TABLE site_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_name VARCHAR(64) NOT NULL UNIQUE COMMENT '用户唯一账号名，不可修改',
    email VARCHAR(128) NOT NULL UNIQUE COMMENT '邮箱',
    password_hash VARCHAR(128) NOT NULL COMMENT '密码哈希',
    role VARCHAR(32) NOT NULL DEFAULT 'USER' COMMENT '角色: ADMIN, USER',
    avatar_url VARCHAR(512) COMMENT '头像URL',
    status INT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert current admin user from properties (will be updated via startup code if missing, but we can insert a dummy and update later)

-- 2. Modify site_comment
ALTER TABLE site_comment ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '关联的用户ID';

-- 3. Create site_guestbook
CREATE TABLE site_guestbook (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_user_created (user_id, created_at)
);

-- 4. Modify site_post
ALTER TABLE site_post ADD COLUMN user_id BIGINT DEFAULT NULL COMMENT '发布者ID(NULL代表站长)';
ALTER TABLE site_post ADD COLUMN audit_status INT NOT NULL DEFAULT 1 COMMENT '审核状态: 0待审核, 1已通过, 2已驳回';
