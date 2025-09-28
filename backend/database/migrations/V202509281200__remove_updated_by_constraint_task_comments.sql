-- Remove NOT NULL constraint from task_comments.updated_by to match tasks table design
-- Comments should only have updated_by set when they are actually updated, not at creation

ALTER TABLE syncup.task_comments
    ALTER COLUMN updated_by DROP NOT NULL,
    ALTER COLUMN updated_at DROP NOT NULL;