-- 1) Seed task_types
INSERT INTO syncup.task_types (name)
VALUES ('Bug'), ('Feature'), ('Chore'), ('Research');

-- 2) Seed tasks
-- Assumes valid project_ids (1–5) and user_ids (1–10)
SELECT set_config('app.user_id', '1', true);

INSERT INTO syncup.tasks
  (project_id, owner_id, task_type, title, description, status, delete_ind, created_by, updated_by)
VALUES
  (1, 1, 2, 'Implement Auth Flow',     'Build login + signup using Cognito',         'TODO',        false, 1, 1),
  (1, 2, 1, 'Fix Upload Crash',        'Resolve 500 error in upload service',         'IN_PROGRESS', false, 2, 2),
  (2, 3, 3, 'Add CI Pipeline',         'Integrate GitHub Actions for testing',        'TODO',        false, 3, 3),
  (2, 4, 4, 'Evaluate Queue Systems',  'Compare SQS vs RabbitMQ for messaging',       'COMPLETED',   false, 4, 4),
  (3, 5, 2, 'User Profile Page',       'Build profile & settings pages',               'TODO',        false, 5, 5),
  (3, 6, 3, 'Nightly Cleanup Job',     'Purge old data regularly',                     DEFAULT,       false, 6, 6),
  (4, 7, 1, 'Fix Search Bug',          'Search returns inconsistent results',          'IN_PROGRESS', false, 7, 7),
  (4, 8, 2, 'Design Knowledge Hub',    'Build internal wiki layout',                   'TODO',        false, 8, 8),
  (5, 9, 4, 'Research Support Tools',  'Evaluate new support ticketing options',       'TODO',        false, 9, 9),
  (5, 10, 3, 'Migrate Chat Backend',   'Move to WebSocket-based backend',               'TODO',        false, 10, 10);

-- 3) Seed task_assignees
INSERT INTO syncup.task_assignees (task_id, user_id, assigned_by)
VALUES
  (1, 2, 1),
  (1, 3, 1),
  (2, 4, 2),
  (2, 5, 2),
  (3, 6, 3),
  (3, 7, 3),
  (4, 8, 4),
  (5, 1, 5),
  (5, 2, 5),
  (6, 3, 6),
  (7, 4, 7),
  (8, 5, 8),
  (9, 6, 9),
  (10, 7, 10);

-- 4) Seed task_comments
INSERT INTO syncup.task_comments
  (task_id, project_id, details, mentioned_user_id, created_by, updated_by)
VALUES
  (1, 1, 'We need MFA too',                        2, 1, 1),
  (2, 1, 'Error happens on large files',            4, 2, 2),
  (3, 2, 'CI should cache dependencies',            6, 3, 3),
  (4, 2, 'RabbitMQ seems simpler than SQS',         8, 4, 4),
  (5, 3, 'UI mockups ready for review',             1, 5, 5),
  (6, 3, 'Cleanup window should be 30 days',        3, 6, 6),
  (7, 4, 'Bug is not reproducible on staging',      7, 7, 7),
  (8, 4, 'Add search bar to homepage',              5, 8, 8),
  (9, 5, 'Zendesk looks promising',                 9, 9, 9),
  (10, 5, 'We must migrate chat before peak load',  10, 10, 10);

-- 5) Seed subtasks
INSERT INTO syncup.subtasks
  (task_id, project_id, task_type, title, details, delete_ind, created_by, updated_by)
VALUES
  (1, 1, 2, 'Add Hosted UI',           'Use Cognito hosted UI flow', false, 1, 1),
  (1, 1, 2, 'Implement Refresh Tokens','Store & rotate tokens',      false, 1, 1),
  (2, 1, 1, 'Fix null pointer checks', 'Guard all backend handlers', false, 2, 2),
  (3, 2, 3, 'Cache dependencies',      'Use actions/cache',          false, 3, 3),
  (4, 2, 4, 'Write evaluation report', 'Summarize findings',         false, 4, 4),
  (5, 3, 2, 'Settings page layout',    'Use grid for forms',         false, 5, 5);

-- 6) Seed files
INSERT INTO syncup.files
  (task_id, project_id, file_url, created_by, updated_by)
VALUES
  (1, 1, 'https://files.local/docs/auth-spec.pdf',        1, 1),
  (2, 1, 'https://files.local/logs/upload-error.log',     2, 2),
  (3, 2, 'https://files.local/ci/gha-pipeline.yml',       3, 3),
  (4, 2, 'https://files.local/reports/queue-eval.docx',   4, 4),
  (5, 3, 'https://files.local/design/profile-mockups.fig',5, 5);

-- Fire task update trigger
SELECT set_config('app.user_id', '2', true);
UPDATE syncup.tasks
SET status = 'IN_PROGRESS', updated_by = 2
WHERE title = 'Implement Auth Flow';

-- Fire subtask update trigger
SELECT set_config('app.user_id', '3', true);
UPDATE syncup.subtasks
SET details = 'Ensure token rotation handled securely', updated_by = 3
WHERE title = 'Implement Refresh Tokens';