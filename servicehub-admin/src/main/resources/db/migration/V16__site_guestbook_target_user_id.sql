ALTER TABLE site_guestbook ADD COLUMN target_user_id BIGINT NULL;
CREATE INDEX idx_site_guestbook_target_user_id ON site_guestbook(target_user_id);
