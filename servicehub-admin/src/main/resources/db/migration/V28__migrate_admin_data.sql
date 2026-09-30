-- Find admin user id and update records
UPDATE site_post SET user_id = (SELECT id FROM site_user WHERE role = 'ADMIN' LIMIT 1) WHERE user_id IS NULL;
UPDATE site_comment SET user_id = (SELECT id FROM site_user WHERE role = 'ADMIN' LIMIT 1) WHERE user_id IS NULL;
UPDATE site_guestbook SET target_user_id = (SELECT id FROM site_user WHERE role = 'ADMIN' LIMIT 1) WHERE target_user_id IS NULL;
UPDATE site_anniversary SET user_id = (SELECT id FROM site_user WHERE role = 'ADMIN' LIMIT 1) WHERE user_id IS NULL;
