-- Fix tasks that were incorrectly set to priority 1 by the previous buggy migration
-- These should have been set to 5 (medium) as the default
UPDATE syncup.tasks
SET priority = 5
WHERE priority = 1;
