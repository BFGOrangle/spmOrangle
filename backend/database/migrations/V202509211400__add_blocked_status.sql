ALTER TYPE request_status ADD VALUE 'BLOCKED';

-- Add tags column to tasks table as an array of text
ALTER TABLE syncup.tasks ADD COLUMN tags TEXT[];

-- Create index for better performance when searching by tags
CREATE INDEX idx_tasks_tags ON syncup.tasks USING GIN (tags);

-- Add some sample tags to existing tasks for testing
UPDATE syncup.tasks SET tags = ARRAY['backend', 'authentication'] WHERE title = 'Implement Auth Flow';
UPDATE syncup.tasks SET tags = ARRAY['bug', 'critical', 'upload'] WHERE title = 'Fix Upload Crash';
UPDATE syncup.tasks SET tags = ARRAY['devops', 'ci/cd'] WHERE title = 'Add CI Pipeline';
UPDATE syncup.tasks SET tags = ARRAY['research', 'architecture'] WHERE title = 'Evaluate Queue Systems';
UPDATE syncup.tasks SET tags = ARRAY['frontend', 'ui'] WHERE title = 'User Profile Page';
