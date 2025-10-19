-- Set config for audit trail
SELECT set_config('app.user_id', '1', true);

-- Tasks for Project 1: Customer Onboarding Portal (Front Office)
-- task_type values: 'BUG', 'FEATURE', 'CHORE', 'RESEARCH'
INSERT INTO syncup.tasks (id, project_id, owner_id, task_type, title, description, status, delete_ind, created_by, updated_by)
VALUES
  (1, 1, 1, 'FEATURE', 'Build User Registration Flow', 'Create multi-step registration form with validation', 'IN_PROGRESS', false, 1, 1),
  (2, 1, 2, 'BUG', 'Fix Email Verification Bug', 'Users not receiving verification emails', 'TODO', false, 1, 1),
  (3, 1, 13, 'CHORE', 'Setup Welcome Email Template', 'Design and implement automated welcome email', 'COMPLETED', false, 1, 1);

-- Tasks for Project 2: Staff Training System (Front Office)
INSERT INTO syncup.tasks (id, project_id, owner_id, task_type, title, description, status, delete_ind, created_by, updated_by)
VALUES
  (4, 2, 1, 'RESEARCH', 'Research Learning Management Systems', 'Evaluate LMS options for integration', 'IN_PROGRESS', false, 1, 1),
  (5, 2, 2, 'FEATURE', 'Create Course Catalog Page', 'Build browsable course catalog with filters', 'TODO', false, 1, 1),
  (6, 2, 10, 'FEATURE', 'Implement Progress Tracking', 'Track user progress through training modules', 'TODO', false, 1, 1);

-- Tasks for Project 3: Q4 Campaign Management (Marketing)
INSERT INTO syncup.tasks (id, project_id, owner_id, task_type, title, description, status, delete_ind, created_by, updated_by)
VALUES
  (7, 3, 4, 'FEATURE', 'Launch Black Friday Campaign', 'Coordinate email and social media campaign', 'IN_PROGRESS', false, 4, 4),
  (8, 3, 8, 'CHORE', 'Update Campaign Landing Pages', 'Refresh landing pages with Q4 messaging', 'TODO', false, 4, 4),
  (9, 3, 12, 'BUG', 'Fix Analytics Tracking Code', 'UTM parameters not tracking properly', 'BLOCKED', false, 4, 4);

-- Tasks for Project 4: Social Media Analytics (Marketing)
INSERT INTO syncup.tasks (id, project_id, owner_id, task_type, title, description, status, delete_ind, created_by, updated_by)
VALUES
  (10, 4, 4, 'FEATURE', 'Build Instagram Insights Dashboard', 'Create dashboard for Instagram metrics', 'TODO', false, 4, 4),
  (11, 4, 8, 'RESEARCH', 'Analyze Competitor Social Presence', 'Research competitor social media strategies', 'COMPLETED', false, 4, 4),
  (12, 4, 12, 'FEATURE', 'Automate Weekly Reports', 'Generate automated weekly performance reports', 'IN_PROGRESS', false, 4, 4);

-- Tasks for Project 5: Internal DevOps Platform (Software)
INSERT INTO syncup.tasks (id, project_id, owner_id, task_type, title, description, status, delete_ind, created_by, updated_by)
VALUES
  (13, 5, 9, 'FEATURE', 'Setup Kubernetes Cluster', 'Configure production K8s cluster', 'COMPLETED', false, 9, 9),
  (14, 5, 10, 'CHORE', 'Implement CI/CD Pipeline', 'Build automated deployment pipeline', 'IN_PROGRESS', false, 9, 9),
  (15, 5, 11, 'BUG', 'Fix Docker Build Failures', 'Resolve intermittent Docker build issues', 'TODO', false, 9, 9);

-- Tasks for Project 6: Mobile App Rewrite (Software)
INSERT INTO syncup.tasks (id, project_id, owner_id, task_type, title, description, status, delete_ind, created_by, updated_by)
VALUES
  (16, 6, 9, 'RESEARCH', 'Research State Management Solutions', 'Compare Redux vs Context API vs Zustand', 'COMPLETED', false, 9, 9),
  (17, 6, 10, 'FEATURE', 'Build Authentication Module', 'Implement OAuth2 authentication flow', 'IN_PROGRESS', false, 9, 9),
  (18, 6, 11, 'FEATURE', 'Create Reusable UI Components', 'Build component library for app', 'TODO', false, 9, 9);

-- Task Assignees (0-2 assignees per task, all must be project members)
INSERT INTO syncup.task_assignees (task_id, user_id, assigned_by)
VALUES
  -- Project 1 tasks
  (1, 2, 1),    -- Task 1: Build User Registration Flow -> staff_tester
  (1, 8, 1),    -- Task 1: Build User Registration Flow -> staff_jordan (cross-dept)
  (2, 2, 1),    -- Task 2: Fix Email Verification Bug -> staff_tester
  (3, 13, 1),   -- Task 3: Setup Welcome Email Template -> staff_tester_2
  
  -- Project 2 tasks
  (4, 1, 1),    -- Task 4: Research LMS -> OrangleManagerTestUser
  (4, 10, 1),   -- Task 4: Research LMS -> staff_qing (cross-dept)
  (5, 2, 1),    -- Task 5: Create Course Catalog -> staff_tester
  (6, 10, 1),   -- Task 6: Implement Progress Tracking -> staff_qing
  
  -- Project 3 tasks
  (7, 8, 4),    -- Task 7: Launch Black Friday Campaign -> staff_jordan
  (7, 12, 4),   -- Task 7: Launch Black Friday Campaign -> staff_yc
  (8, 8, 4),    -- Task 8: Update Campaign Landing Pages -> staff_jordan
  (9, 12, 4),   -- Task 9: Fix Analytics Tracking -> staff_yc
  
  -- Project 4 tasks
  (10, 8, 4),   -- Task 10: Build Instagram Dashboard -> staff_jordan
  (11, 8, 4),   -- Task 11: Analyze Competitor Presence -> staff_jordan
  (12, 12, 4),  -- Task 12: Automate Weekly Reports -> staff_yc
  (12, 11, 4),  -- Task 12: Automate Weekly Reports -> staff_kylene (cross-dept)
  
  -- Project 5 tasks
  (13, 10, 9),  -- Task 13: Setup K8s Cluster -> staff_qing
  (13, 11, 9),  -- Task 13: Setup K8s Cluster -> staff_kylene
  (14, 10, 9),  -- Task 14: Implement CI/CD -> staff_qing
  (15, 11, 9),  -- Task 15: Fix Docker Build -> staff_kylene
  
  -- Project 6 tasks
  (16, 10, 9),  -- Task 16: Research State Management -> staff_qing
  (17, 10, 9),  -- Task 17: Build Auth Module -> staff_qing
  (18, 11, 9);  -- Task 18: Create UI Components -> staff_kylene

-- Reset sequence
SELECT setval('syncup.tasks_id_seq', (SELECT MAX(id) FROM syncup.tasks));

