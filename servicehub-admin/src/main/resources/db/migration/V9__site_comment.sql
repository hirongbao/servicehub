-- V9：个人网站访客评论
CREATE TABLE IF NOT EXISTS site_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    author VARCHAR(50) NOT NULL DEFAULT '访客',
    content VARCHAR(500) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_post_created (post_id, created_at)
);
