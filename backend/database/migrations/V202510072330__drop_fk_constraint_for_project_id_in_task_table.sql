ALTER TABLE syncup.task_comments
ALTER COLUMN project_id DROP NOT NULL;

ALTER TABLE syncup.task_comments
DROP CONSTRAINT task_comments_project_id_fkey;