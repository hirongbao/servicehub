-- V10：为个人网站动态增加分类字段
ALTER TABLE site_post
    ADD COLUMN category_id VARCHAR(32) NULL AFTER status,
    ADD COLUMN category_name VARCHAR(50) NULL AFTER category_id,
    ADD KEY idx_category_status_created (category_id, status, created_at);

UPDATE site_post
SET category_id = 'notes', category_name = '随笔'
WHERE category_id IS NULL OR category_id = '';
