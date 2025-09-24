-- Step 1: Add temporary columns for the new enum values
ALTER TABLE syncup.tasks ADD COLUMN task_type_enum VARCHAR(50);
ALTER TABLE syncup.tasks ADD COLUMN status_enum VARCHAR(50);

-- Step 2: Map existing task_type IDs to enum values
-- Based on your seeding data: 1=BUG, 2=FEATURE, 3=CHORE, 4=RESEARCH
UPDATE syncup.tasks SET task_type_enum = 'BUG' WHERE task_type = 1;
UPDATE syncup.tasks SET task_type_enum = 'FEATURE' WHERE task_type = 2;
UPDATE syncup.tasks SET task_type_enum = 'CHORE' WHERE task_type = 3;
UPDATE syncup.tasks SET task_type_enum = 'RESEARCH' WHERE task_type = 4;

-- Set default for any unmapped task types
UPDATE syncup.tasks SET task_type_enum = 'FEATURE' WHERE task_type_enum IS NULL;

-- Step 3: Map existing status enum values to string values
UPDATE syncup.tasks SET status_enum = status::text;

-- Step 4: Drop the old constraints and columns
ALTER TABLE syncup.tasks DROP CONSTRAINT IF EXISTS tasks_task_type_fkey;
ALTER TABLE syncup.tasks DROP COLUMN task_type;
ALTER TABLE syncup.tasks DROP COLUMN status;

-- Step 5: Rename new columns to original names
ALTER TABLE syncup.tasks RENAME COLUMN task_type_enum TO task_type;
ALTER TABLE syncup.tasks RENAME COLUMN status_enum TO status;

-- Step 6: Make columns NOT NULL
ALTER TABLE syncup.tasks ALTER COLUMN task_type SET NOT NULL;
ALTER TABLE syncup.tasks ALTER COLUMN status SET NOT NULL;

-- Step 7: Update subtasks table to use string task_type as well
ALTER TABLE syncup.subtasks ADD COLUMN task_type_enum VARCHAR(50);
UPDATE syncup.subtasks SET task_type_enum = 'BUG' WHERE task_type = 1;
UPDATE syncup.subtasks SET task_type_enum = 'FEATURE' WHERE task_type = 2;
UPDATE syncup.subtasks SET task_type_enum = 'CHORE' WHERE task_type = 3;
UPDATE syncup.subtasks SET task_type_enum = 'RESEARCH' WHERE task_type = 4;
UPDATE syncup.subtasks SET task_type_enum = 'FEATURE' WHERE task_type_enum IS NULL;

ALTER TABLE syncup.subtasks DROP CONSTRAINT IF EXISTS subtasks_task_type_fkey;
ALTER TABLE syncup.subtasks DROP COLUMN task_type;
ALTER TABLE syncup.subtasks RENAME COLUMN task_type_enum TO task_type;
ALTER TABLE syncup.subtasks ALTER COLUMN task_type SET NOT NULL;

-- Step 8: Drop the task_types table and request_status type
DROP TABLE IF EXISTS syncup.task_types CASCADE;
DROP TYPE IF EXISTS request_status CASCADE;