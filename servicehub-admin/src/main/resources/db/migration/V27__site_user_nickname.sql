ALTER TABLE site_user ADD COLUMN nickname VARCHAR(64) DEFAULT NULL AFTER account_name;
UPDATE site_user SET nickname = account_name WHERE nickname IS NULL;
