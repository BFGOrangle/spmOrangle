ALTER TABLE syncup.tasks

ADD COLUMN IF NOT EXISTS has_sent_overdue BOOLEAN DEFAULT FALSE;

-- Update all existing rows to have FALSE as the default value
UPDATE syncup.tasks
SET has_sent_overdue = FALSE
WHERE has_sent_overdue IS NULL;