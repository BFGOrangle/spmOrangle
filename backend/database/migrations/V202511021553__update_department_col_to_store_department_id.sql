-- 1) Add the new bigint column (left NULL)
ALTER TABLE syncup.users
  ADD COLUMN IF NOT EXISTS department_id bigint;

-- 2) Drop old index on 'department' if present
DROP INDEX IF EXISTS idx_users_department;

-- 3) Drop the old 'department' column
ALTER TABLE syncup.users
  DROP COLUMN IF EXISTS department;

-- 4) Create an index for the new column (optional but mirrors the old index)
CREATE INDEX IF NOT EXISTS idx_users_department_id
  ON syncup.users (department_id);
