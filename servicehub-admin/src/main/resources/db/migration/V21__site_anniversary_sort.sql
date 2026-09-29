ALTER TABLE site_anniversary ADD COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT '排序，越小越靠前';
