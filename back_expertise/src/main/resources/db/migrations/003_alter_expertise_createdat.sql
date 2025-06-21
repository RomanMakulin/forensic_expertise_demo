ALTER TABLE expertise ADD COLUMN created_at TIMESTAMP;
UPDATE expertise SET created_at = NOW() WHERE created_at IS NULL;