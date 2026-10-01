-- Revert user_id for legacy anonymous comments that were incorrectly assigned to admin
UPDATE site_comment SET user_id = NULL WHERE author = '访客';
