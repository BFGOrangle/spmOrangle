-- Create tag table
CREATE TABLE IF NOT EXISTS syncup.tag (
    id BIGSERIAL PRIMARY KEY,
    tag_name VARCHAR(255) NOT NULL UNIQUE
);

-- Create junction table for many-to-many relationship between tasks and tags
CREATE TABLE IF NOT EXISTS syncup.task_tag (
    task_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (task_id, tag_id),
    FOREIGN KEY (task_id) REFERENCES syncup.tasks(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES syncup.tag(id) ON DELETE CASCADE
);

-- Migrate existing tags from tasks.tags array to the new structure
-- First, extract all unique tags from existing tasks
INSERT INTO syncup.tag (tag_name)
SELECT DISTINCT unnest(tags) as tag_name
FROM syncup.tasks
WHERE tags IS NOT NULL AND array_length(tags, 1) > 0
ON CONFLICT (tag_name) DO NOTHING;

-- Create the relationships in task_tag table
INSERT INTO syncup.task_tag (task_id, tag_id)
SELECT t.id, tag.id
FROM syncup.tasks t
CROSS JOIN LATERAL unnest(t.tags) as task_tag_name
INNER JOIN syncup.tag ON syncup.tag.tag_name = task_tag_name
WHERE t.tags IS NOT NULL AND array_length(t.tags, 1) > 0
ON CONFLICT DO NOTHING;

-- Drop the old tags column from tasks table
-- IMPORTANT: Only run this after verifying the migration was successful
-- ALTER TABLE syncup.tasks DROP COLUMN IF EXISTS tags;

-- Note: Commented out the DROP COLUMN command for safety.
-- After verifying the migration, you can uncomment and run it separately.
