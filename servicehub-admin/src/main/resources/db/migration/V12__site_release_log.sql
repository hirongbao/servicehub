CREATE TABLE IF NOT EXISTS site_release_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(120) NOT NULL,
    version VARCHAR(40) NULL,
    summary VARCHAR(500) NULL,
    content TEXT NULL,
    status TINYINT NOT NULL DEFAULT 1,
    published_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_release_status_published (status, published_at, id)
);
