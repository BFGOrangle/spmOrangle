-- Add fields to support future filtering and resolution features

-- Add resolution tracking
ALTER TABLE syncup.task_comments
ADD COLUMN is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN resolved_by BIGINT REFERENCES syncup.users(id),
ADD COLUMN resolved_at TIMESTAMPTZ;

-- Add priority for future comment prioritization
ALTER TABLE syncup.task_comments
ADD COLUMN priority VARCHAR(10) DEFAULT 'NORMAL';

-- Add indexes for filtering performance
CREATE INDEX idx_task_comments_resolved ON syncup.task_comments (is_resolved);
CREATE INDEX idx_task_comments_priority ON syncup.task_comments (priority);

-- Add index for efficient author filtering
CREATE INDEX idx_task_comments_created_by ON syncup.task_comments (created_by);