ALTER TABLE site_anniversary ADD COLUMN user_id BIGINT NULL;
CREATE INDEX idx_site_anniversary_user_id ON site_anniversary(user_id);
