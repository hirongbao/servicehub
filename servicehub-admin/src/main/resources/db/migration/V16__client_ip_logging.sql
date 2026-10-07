-- V16：新增全站 HTTP 请求访问审计表，并在短链访问表和动态评论表补充 IP 记录字段
CREATE TABLE IF NOT EXISTS access_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL,
    method VARCHAR(10) NOT NULL,
    path VARCHAR(512) NOT NULL,
    query_string VARCHAR(1024) NULL,
    status_code INT NOT NULL,
    cost_ms BIGINT NOT NULL,
    user_agent VARCHAR(512) NULL,
    referer VARCHAR(512) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_ip_created (ip_address, created_at),
    KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE link_visit_log ADD COLUMN ip_address VARCHAR(45) NULL AFTER link_id;
ALTER TABLE site_comment ADD COLUMN ip_address VARCHAR(45) NULL AFTER author;
