-- file_record 表增加 user_id 列，支持多用户文件归属追踪
ALTER TABLE file_record ADD COLUMN user_id BIGINT NULL;
CREATE INDEX idx_file_record_user_id ON file_record(user_id);
-- 将已有的管理员上传文件归属到站长
UPDATE file_record SET user_id = (SELECT id FROM site_user WHERE role='ADMIN' LIMIT 1)
  WHERE source_type = 'ADMIN';
