-- Add due_datetime column to tasks table
-- This column will store the due date and time for each task

ALTER TABLE syncup.tasks
ADD COLUMN due_datetime TIMESTAMPTZ;
