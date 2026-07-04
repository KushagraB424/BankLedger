ALTER TABLE customers ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT 'TEMP_PASSWORD';
-- Remove the default now that existing rows (if any) are updated
ALTER TABLE customers ALTER COLUMN password DROP DEFAULT;
