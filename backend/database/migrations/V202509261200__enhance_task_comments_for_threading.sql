-- Enhance task_comments table to support threaded comments and subtask comments

-- Add threading support
ALTER TABLE syncup.task_comments
ADD COLUMN parent_comment_id BIGINT REFERENCES syncup.task_comments(id) ON DELETE CASCADE;

-- Add subtask support
ALTER TABLE syncup.task_comments
ADD COLUMN subtask_id BIGINT REFERENCES syncup.subtasks(id) ON DELETE CASCADE;

-- Add edit and delete tracking
ALTER TABLE syncup.task_comments
ADD COLUMN is_edited BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- Support multiple mentions (array of user IDs)
ALTER TABLE syncup.task_comments
ADD COLUMN mentioned_user_ids BIGINT[];

-- Rename details to content for clarity
ALTER TABLE syncup.task_comments
RENAME COLUMN details TO content;

-- Make content required and set proper length
ALTER TABLE syncup.task_comments
ALTER COLUMN content SET NOT NULL,
ALTER COLUMN content TYPE VARCHAR(2000);

-- Make task_id nullable since we can have subtask-only comments
ALTER TABLE syncup.task_comments
ALTER COLUMN task_id DROP NOT NULL;

-- Make mentioned_user_id nullable since we're adding array version
ALTER TABLE syncup.task_comments
ALTER COLUMN mentioned_user_id DROP NOT NULL;

-- Add constraint to ensure either task_id or subtask_id is set, but not both
ALTER TABLE syncup.task_comments
ADD CONSTRAINT chk_task_or_subtask
CHECK (
  (task_id IS NOT NULL AND subtask_id IS NULL) OR
  (task_id IS NULL AND subtask_id IS NOT NULL)
);

-- Add index for parent_comment_id for efficient threading queries
CREATE INDEX idx_task_comments_parent_id ON syncup.task_comments (parent_comment_id);

-- Add index for subtask_id
CREATE INDEX idx_task_comments_subtask_id ON syncup.task_comments (subtask_id);

-- Add index for is_deleted to filter out deleted comments
CREATE INDEX idx_task_comments_is_deleted ON syncup.task_comments (is_deleted);

-- Add trigger for updated_at
CREATE TRIGGER task_comments_updated_at
  BEFORE UPDATE ON syncup.task_comments
  FOR EACH ROW
  EXECUTE FUNCTION set_updated_at();