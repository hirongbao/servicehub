-- V8：动态支持多图（content 可空，媒体拆分为子表并迁移存量数据）
ALTER TABLE site_post MODIFY content VARCHAR(2000) NULL;

CREATE TABLE IF NOT EXISTS site_post_media (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    media_type VARCHAR(16) NOT NULL,
    media_url VARCHAR(1024) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    KEY idx_post (post_id)
);

INSERT INTO site_post_media (post_id, media_type, media_url, sort_order)
SELECT id, media_type, media_url, 0 FROM site_post
WHERE media_type IS NOT NULL AND media_url IS NOT NULL AND media_url <> '';

ALTER TABLE site_post DROP COLUMN media_type;
ALTER TABLE site_post DROP COLUMN media_url;
