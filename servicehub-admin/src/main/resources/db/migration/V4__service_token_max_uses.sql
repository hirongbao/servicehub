-- V4：service_token 支持使用额度上限（NULL 或 0 表示不限制）
ALTER TABLE service_token ADD COLUMN max_uses INT NULL AFTER token_type;
