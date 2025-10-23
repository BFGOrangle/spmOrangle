-- Clean up all existing seeding data to prepare for fresh data
-- Tables are truncated in order respecting foreign key dependencies

-- Disable triggers temporarily to avoid issues with audit logging
SET session_replication_role = replica;

-- Truncate child tables first (those with foreign keys pointing to parents)
TRUNCATE TABLE syncup.notification_channels CASCADE;
TRUNCATE TABLE syncup.notifications CASCADE;

TRUNCATE TABLE syncup.task_time_tracking CASCADE;
TRUNCATE TABLE syncup.files CASCADE;
TRUNCATE TABLE syncup.subtasks CASCADE;
TRUNCATE TABLE syncup.task_comments CASCADE;
TRUNCATE TABLE syncup.task_assignees CASCADE;
TRUNCATE TABLE syncup.tasks CASCADE;

TRUNCATE TABLE syncup.project_members CASCADE;
TRUNCATE TABLE syncup.projects CASCADE;

TRUNCATE TABLE syncup.users CASCADE;

-- Also clean up the log table
TRUNCATE TABLE syncup.update_logs CASCADE;

-- Re-enable triggers
SET session_replication_role = DEFAULT;

