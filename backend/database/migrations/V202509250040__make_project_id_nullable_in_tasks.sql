-- Make project_id nullable to support personal tasks
ALTER TABLE syncup.tasks ALTER COLUMN project_id DROP NOT NULL;

-- Add index for personal tasks (where project_id is null)
CREATE INDEX idx_tasks_personal ON syncup.tasks (owner_id) WHERE project_id IS NULL;

-- Add constraint to ensure either project_id is set OR it's a personal task
-- This prevents orphaned tasks
ALTER TABLE syncup.tasks ADD CONSTRAINT chk_task_assignment 
  CHECK (project_id IS NOT NULL OR owner_id IS NOT NULL);
