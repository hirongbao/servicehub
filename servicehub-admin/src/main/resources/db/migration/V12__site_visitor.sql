-- V12：个人网站访客持久化统计
CREATE TABLE IF NOT EXISTS site_visitor (
    visitor_key VARCHAR(64) PRIMARY KEY,
    first_seen DATETIME NOT NULL,
    last_seen DATETIME NOT NULL,
    last_visit_at DATETIME NOT NULL,
    visit_count INT NOT NULL DEFAULT 1,
    KEY idx_last_visit (last_visit_at)
);
