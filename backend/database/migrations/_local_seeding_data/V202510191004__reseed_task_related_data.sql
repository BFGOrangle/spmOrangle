-- Insert task comments, subtasks, and time tracking data

-- Set config for audit trail
SELECT set_config('app.user_id', '1', true);

-- ====================
-- TASK COMMENTS
-- ====================

INSERT INTO syncup.task_comments (id, task_id, project_id, content, mentioned_user_id, created_by, updated_by)
VALUES
  -- Project 1: Customer Onboarding Portal
  (1, 1, 1, 'We should add social login options too', 2, 1, 1),
  (2, 1, 1, '@staff_tester Can you review the password validation logic?', 2, 8, 8),
  (3, 2, 1, 'Checked spam folder - emails are being blocked by provider', 13, 2, 2),
  (4, 3, 1, 'Template looks great! Ready to deploy', 1, 13, 13),
  
  -- Project 2: Staff Training System
  (5, 4, 2, 'Canvas LMS looks promising for our use case', 10, 1, 1),
  (6, 4, 2, '@OrangleManagerTestUser Should we schedule a demo?', 1, 10, 10),
  (7, 5, 2, 'Need to add filter by department and skill level', 2, 2, 2),
  (8, 6, 2, 'This will integrate with our badge system', 1, 10, 10),
  
  -- Project 3: Q4 Campaign Management
  (9, 7, 3, 'Email templates are ready for approval', 4, 8, 8),
  (10, 7, 3, '@staff_yc Can you prepare the social media content?', 12, 4, 4),
  (11, 8, 3, 'Landing page design needs to match brand guidelines', 12, 8, 8),
  (12, 9, 3, 'Issue is with Google Analytics configuration', 2, 12, 12),
  
  -- Project 4: Social Media Analytics
  (13, 10, 4, 'Should include story views and engagement rates', 11, 8, 8),
  (14, 11, 4, 'Competitors are focusing heavily on Reels content', 4, 8, 8),
  (15, 12, 4, '@staff_kylene Can you help with the backend API?', 11, 12, 12),
  
  -- Project 5: Internal DevOps Platform
  (16, 13, 5, 'Cluster is running smoothly with 3 nodes', 9, 10, 10),
  (17, 14, 5, 'Pipeline should include automated testing stages', 12, 10, 10),
  (18, 14, 5, '@staff_qing Let''s add deployment rollback capability', 10, 11, 11),
  (19, 15, 5, 'Issue occurs when building arm64 images', 11, 11, 11),
  
  -- Project 6: Mobile App Rewrite
  (20, 16, 6, 'Zustand offers the best developer experience', 13, 10, 10),
  (21, 17, 6, 'Need to handle token refresh properly', 9, 10, 10),
  (22, 18, 6, 'Component library should follow Material Design', 10, 11, 11);

-- ====================
-- SUBTASKS
-- ====================

INSERT INTO syncup.subtasks (id, task_id, project_id, task_type, title, details, delete_ind, created_by, updated_by)
VALUES
  -- Subtasks for Task 1: Build User Registration Flow
  (1, 1, 1, 'FEATURE', 'Design form UI', 'Create wireframes and mockups', false, 1, 1),
  (2, 1, 1, 'FEATURE', 'Implement form validation', 'Add client-side and server-side validation', false, 1, 1),
  (3, 1, 1, 'FEATURE', 'Add password strength meter', 'Visual indicator for password strength', false, 1, 1),
  
  -- Subtasks for Task 4: Research Learning Management Systems
  (4, 4, 2, 'RESEARCH', 'Compare Canvas vs Moodle', 'Feature comparison and pricing', false, 1, 1),
  (5, 4, 2, 'RESEARCH', 'Schedule vendor demos', 'Book demo sessions with top 3 vendors', false, 1, 1),
  
  -- Subtasks for Task 7: Launch Black Friday Campaign
  (6, 7, 3, 'CHORE', 'Create email campaign', 'Design and schedule email blasts', false, 4, 4),
  (7, 7, 3, 'CHORE', 'Prepare social media posts', 'Create content for all platforms', false, 4, 4),
  (8, 7, 3, 'CHORE', 'Setup promo codes', 'Configure discount codes in system', false, 4, 4),
  
  -- Subtasks for Task 12: Automate Weekly Reports
  (9, 12, 4, 'FEATURE', 'Design report template', 'Create standardized report layout', false, 4, 4),
  (10, 12, 4, 'FEATURE', 'Build data aggregation', 'Collect metrics from all platforms', false, 4, 4),
  
  -- Subtasks for Task 14: Implement CI/CD Pipeline
  (11, 14, 5, 'CHORE', 'Configure GitHub Actions', 'Setup workflow files', false, 9, 9),
  (12, 14, 5, 'CHORE', 'Add automated tests', 'Include unit and integration tests', false, 9, 9),
  (13, 14, 5, 'CHORE', 'Setup deployment stages', 'Configure dev, staging, prod environments', false, 9, 9),
  
  -- Subtasks for Task 17: Build Authentication Module
  (14, 17, 6, 'FEATURE', 'Implement login screen', 'Build UI for authentication', false, 9, 9),
  (15, 17, 6, 'FEATURE', 'Add biometric authentication', 'Support Face ID and fingerprint', false, 9, 9);

-- ====================
-- TASK TIME TRACKING
-- ====================

-- Time tracking for COMPLETED and IN_PROGRESS tasks
INSERT INTO syncup.task_time_tracking (id, task_id, user_id, started_at, completed_at, total_hours, created_at, updated_at)
VALUES
  -- Task 1: Build User Registration Flow (IN_PROGRESS)
  (1, 1, 2, '2025-10-15 09:00:00+00', NULL, 12.5, now(), now()),
  (2, 1, 8, '2025-10-16 10:00:00+00', NULL, 8.0, now(), now()),
  
  -- Task 3: Setup Welcome Email Template (COMPLETED)
  (3, 3, 13, '2025-10-10 08:00:00+00', '2025-10-12 17:00:00+00', 16.0, now(), now()),
  
  -- Task 4: Research Learning Management Systems (IN_PROGRESS)
  (4, 4, 1, '2025-10-14 09:00:00+00', NULL, 6.5, now(), now()),
  (5, 4, 10, '2025-10-14 14:00:00+00', NULL, 5.0, now(), now()),
  
  -- Task 7: Launch Black Friday Campaign (IN_PROGRESS)
  (6, 7, 8, '2025-10-12 08:00:00+00', NULL, 20.0, now(), now()),
  (7, 7, 12, '2025-10-13 09:00:00+00', NULL, 18.5, now(), now()),
  
  -- Task 11: Analyze Competitor Social Presence (COMPLETED)
  (8, 11, 8, '2025-10-08 09:00:00+00', '2025-10-10 16:00:00+00', 14.0, now(), now()),
  
  -- Task 12: Automate Weekly Reports (IN_PROGRESS)
  (9, 12, 12, '2025-10-16 10:00:00+00', NULL, 10.0, now(), now()),
  (10, 12, 11, '2025-10-17 09:00:00+00', NULL, 7.5, now(), now()),
  
  -- Task 13: Setup Kubernetes Cluster (COMPLETED)
  (11, 13, 10, '2025-10-05 08:00:00+00', '2025-10-09 18:00:00+00', 32.0, now(), now()),
  (12, 13, 11, '2025-10-05 08:00:00+00', '2025-10-09 18:00:00+00', 28.0, now(), now()),
  
  -- Task 14: Implement CI/CD Pipeline (IN_PROGRESS)
  (13, 14, 10, '2025-10-11 09:00:00+00', NULL, 24.0, now(), now()),
  
  -- Task 16: Research State Management Solutions (COMPLETED)
  (14, 16, 10, '2025-10-01 09:00:00+00', '2025-10-03 17:00:00+00', 18.0, now(), now()),
  
  -- Task 17: Build Authentication Module (IN_PROGRESS)
  (15, 17, 10, '2025-10-14 08:00:00+00', NULL, 22.5, now(), now());

-- Reset sequences
SELECT setval('syncup.task_comments_id_seq', (SELECT MAX(id) FROM syncup.task_comments));
SELECT setval('syncup.subtasks_id_seq', (SELECT MAX(id) FROM syncup.subtasks));
SELECT setval('syncup.task_time_tracking_id_seq', (SELECT MAX(id) FROM syncup.task_time_tracking));

