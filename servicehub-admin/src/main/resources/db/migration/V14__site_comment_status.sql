ALTER TABLE site_comment ADD COLUMN status TINYINT NOT NULL DEFAULT 0 COMMENT '0:pending, 1:approved, 2:rejected';
