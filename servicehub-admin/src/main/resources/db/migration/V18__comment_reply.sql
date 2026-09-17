-- V18：评论回复功能，增加父评论引用字段
ALTER TABLE site_comment ADD COLUMN parent_id BIGINT NULL COMMENT '父评论ID，NULL表示顶级评论' AFTER post_id;
ALTER TABLE site_comment ADD COLUMN reply_to_author VARCHAR(50) NULL COMMENT '被回复者的昵称' AFTER ip_address;
CREATE INDEX idx_parent_id ON site_comment (parent_id);
