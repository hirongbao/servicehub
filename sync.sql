USE servicehub;
UPDATE site_user SET avatar_url = (SELECT avatar_url FROM site_profile ORDER BY id LIMIT 1) WHERE role = 'ADMIN';
