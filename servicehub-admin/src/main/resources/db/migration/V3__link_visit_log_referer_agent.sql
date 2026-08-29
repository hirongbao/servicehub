-- V3：link_visit_log 记录访问来源与设备信息
ALTER TABLE link_visit_log ADD COLUMN referer VARCHAR(512) NULL AFTER link_id;
ALTER TABLE link_visit_log ADD COLUMN user_agent VARCHAR(512) NULL AFTER referer;
