ALTER TABLE syncup.tasks

ADD COLUMN IF NOT EXISTS is_rescheduled BOOLEAN DEFAULT FALSE;

-- Update all existing rows to have FALSE as the default value
UPDATE syncup.tasks
SET is_rescheduled = FALSE
WHERE is_rescheduled IS NULL;