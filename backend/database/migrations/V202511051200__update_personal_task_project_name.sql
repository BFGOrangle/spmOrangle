-- Update the personal task project name from "Personal Task Repository" to "personal task(No project)"
UPDATE syncup.projects
SET name = 'Personal Task(No Project)'
WHERE id = 0
  AND name IN ('Personal Task Repository', 'Personal Tasks');
