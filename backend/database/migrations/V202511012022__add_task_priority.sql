ALTER TABLE syncup.tasks

    ADD COLUMN IF NOT EXISTS priority BIGINT DEFAULT 5;

-- Update all existing rows to have priority 5 (medium) as the default value
UPDATE syncup.tasks
SET priority = 5
WHERE priority IS NULL;