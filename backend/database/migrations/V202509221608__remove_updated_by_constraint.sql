ALTER TABLE syncup.tasks DROP CONSTRAINT IF EXISTS tasks_updated_by_fkey;

ALTER TABLE syncup.tasks 
    ALTER COLUMN updated_by DROP NOT NULL,
    ALTER COLUMN updated_at DROP NOT NULL;
