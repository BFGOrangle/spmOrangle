-- Migration: Add is_owner flag to project_members table
-- Purpose: Support multiple project owners and simplify ownership logic
-- Date: 2025-11-06

-- Step 1: Add is_owner column with default false
ALTER TABLE syncup.project_members
ADD COLUMN is_owner BOOLEAN NOT NULL DEFAULT false;

-- Step 2: Mark existing project owners in project_members table
-- This finds rows where the user_id matches the project's owner_id
UPDATE syncup.project_members pm
SET is_owner = true
WHERE EXISTS (
    SELECT 1
    FROM syncup.projects p
    WHERE p.id = pm.project_id
    AND p.owner_id = pm.user_id
    AND p.delete_ind = false
);

-- Step 3: Handle edge case - ensure project owners who aren't yet in project_members get added
-- This should not happen in current data, but protects against inconsistencies
INSERT INTO syncup.project_members (project_id, user_id, is_owner, added_by, added_at)
SELECT
    p.id,
    p.owner_id,
    true,
    p.created_by,
    p.created_at
FROM syncup.projects p
WHERE p.delete_ind = false
AND NOT EXISTS (
    SELECT 1
    FROM syncup.project_members pm
    WHERE pm.project_id = p.id
    AND pm.user_id = p.owner_id
);

-- Step 4: Create partial index for fast owner lookups
-- Only indexes rows where is_owner = true for better performance
CREATE INDEX idx_project_members_is_owner
ON syncup.project_members(project_id, is_owner)
WHERE is_owner = true;

-- Step 5: Create composite index for permission checks
CREATE INDEX idx_project_members_composite
ON syncup.project_members(project_id, user_id, is_owner);

-- Step 6: Add comment for documentation
COMMENT ON COLUMN syncup.project_members.is_owner IS
'Flag indicating if this member is a project owner. Multiple owners per project are supported.';

-- Validation queries (for manual verification after migration)
-- These can be run separately to verify migration success:

-- 1. Verify all projects have at least one owner in project_members
-- SELECT p.id, p.name, COUNT(pm.user_id) as owner_count
-- FROM syncup.projects p
-- LEFT JOIN syncup.project_members pm ON p.id = pm.project_id AND pm.is_owner = true
-- WHERE p.delete_ind = false
-- GROUP BY p.id, p.name
-- HAVING COUNT(pm.user_id) = 0;
-- Expected: 0 rows (all projects should have at least one owner)

-- 2. Verify is_owner flag matches projects.owner_id
-- SELECT p.id, p.name, p.owner_id, pm.user_id, pm.is_owner
-- FROM syncup.projects p
-- JOIN syncup.project_members pm ON p.id = pm.project_id
-- WHERE p.delete_ind = false
-- AND p.owner_id = pm.user_id
-- AND pm.is_owner = false;
-- Expected: 0 rows (all matching owner_ids should have is_owner=true)

-- 3. Check for multiple owners (should work but verify if intentional)
-- SELECT project_id, COUNT(*) as owner_count
-- FROM syncup.project_members
-- WHERE is_owner = true
-- GROUP BY project_id
-- HAVING COUNT(*) > 1;
-- Expected: 0 rows initially (only one owner per project before cross-dept features)
