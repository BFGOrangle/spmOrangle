ALTER TABLE syncup.tasks

ADD COLUMN IF NOT EXISTS has_sent_predue BOOLEAN DEFAULT FALSE;

-- Update all existing rows to have FALSE as the default value
UPDATE syncup.tasks
SET has_sent_predue = FALSE
WHERE has_sent_predue IS NULL;