ALTER TABLE syncup.subtasks DROP CONSTRAINT IF EXISTS subtasks_updated_by_fkey;

ALTER TABLE syncup.subtasks 
    ALTER COLUMN updated_by DROP NOT NULL,
    ALTER COLUMN updated_at DROP NOT NULL;
