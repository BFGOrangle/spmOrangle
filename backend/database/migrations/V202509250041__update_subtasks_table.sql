-- Step 1: Add status column
ALTER TABLE syncup.subtasks ADD COLUMN status VARCHAR(50) NOT NULL DEFAULT 'TODO';

-- Step 2: Add missing default values and constraints for delete_ind
ALTER TABLE syncup.subtasks ALTER COLUMN delete_ind SET DEFAULT false;
ALTER TABLE syncup.subtasks ALTER COLUMN delete_ind SET NOT NULL;

-- Step 3: Add updated_at trigger for subtasks
CREATE TRIGGER subtasks_updated_at
  BEFORE UPDATE ON syncup.subtasks
  FOR EACH ROW
  EXECUTE FUNCTION set_updated_at();

DROP TRIGGER IF EXISTS task_update_log_trg ON syncup.subtasks;

CREATE TRIGGER subtasks_update_log_trg
  AFTER UPDATE ON syncup.subtasks
  FOR EACH ROW 
  EXECUTE FUNCTION log_update();