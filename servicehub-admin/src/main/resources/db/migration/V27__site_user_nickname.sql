ALTER TABLE site_user ADD COLUMN nickname VARCHAR(64) DEFAULT NULL AFTER accountName;
UPDATE site_user SET nickname = accountName WHERE nickname IS NULL;
