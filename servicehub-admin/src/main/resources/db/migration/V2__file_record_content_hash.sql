-- V2：file_record 补充内容哈希列与唯一索引（原 FileRecordSchemaMigrator 逻辑）
ALTER TABLE file_record ADD COLUMN content_hash VARCHAR(64) NULL AFTER content_type;
ALTER TABLE file_record ADD UNIQUE INDEX uk_content_hash (content_hash);
