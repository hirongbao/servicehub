ALTER TABLE site_anniversary ADD COLUMN is_enabled TINYINT(1) NOT NULL DEFAULT 1 COMMENT '0: 禁用, 1: 启用';
