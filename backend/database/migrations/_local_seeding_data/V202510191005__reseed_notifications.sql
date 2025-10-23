-- Insert notification data for various events
-- Notifications cover task assignments, mentions, project invites, and task completions

INSERT INTO syncup.notifications (
  notification_id,
  author_id,
  target_id,
  notification_type,
  subject,
  message,
  read_status,
  dismissed_status,
  priority,
  link,
  metadata
) VALUES
  -- Notifications for Front Office staff (user_id 2: staff_tester)
  (1, 1, 2, 'TASK_ASSIGNED', 'New Task Assigned', 
   'OrangleManagerTestUser assigned you to "Build User Registration Flow"', false, false, 'HIGH',
   '/tasks/1', '{"task_id":1,"project_id":1}'),
  
  (2, 8, 2, 'MENTION', 'You were mentioned in a comment',
   'staff_jordan mentioned you in "Build User Registration Flow"', false, false, 'MEDIUM',
   '/tasks/1', '{"task_id":1,"comment_id":2}'),
  
  (3, 1, 2, 'TASK_ASSIGNED', 'New Task Assigned',
   'OrangleManagerTestUser assigned you to "Create Course Catalog Page"', true, false, 'MEDIUM',
   '/tasks/5', '{"task_id":5,"project_id":2}'),
  
  -- Notifications for Front Office staff (user_id 13: staff_tester_2)
  (4, 1, 13, 'PROJECT_INVITE', 'Added to Project',
   'You were added to "Customer Onboarding Portal"', true, false, 'MEDIUM',
   '/projects/1', '{"project_id":1}'),
  
  (5, 13, 13, 'TASK_COMPLETED', 'Task Completed',
   'You completed "Setup Welcome Email Template"', true, false, 'LOW',
   '/tasks/3', '{"task_id":3,"project_id":1}'),
  
  -- Notifications for Marketing manager (user_id 4: manager_jitt)
  (6, 4, 4, 'PROJECT_MEMBER_JOINED', 'Team Member Joined',
   'staff_jordan joined "Q4 Campaign Management"', true, true, 'LOW',
   '/projects/3', '{"project_id":3,"user_id":8}'),
  
  (7, 12, 4, 'MENTION', 'You were mentioned in a comment',
   'staff_yc mentioned you in "Launch Black Friday Campaign"', false, false, 'MEDIUM',
   '/tasks/7', '{"task_id":7,"comment_id":10}'),
  
  -- Notifications for Marketing staff (user_id 8: staff_jordan)
  (8, 4, 8, 'TASK_ASSIGNED', 'New Task Assigned',
   'manager_jitt assigned you to "Launch Black Friday Campaign"', true, false, 'HIGH',
   '/tasks/7', '{"task_id":7,"project_id":3}'),
  
  (9, 1, 8, 'PROJECT_INVITE', 'Added to Project',
   'You were added to "Customer Onboarding Portal"', true, false, 'MEDIUM',
   '/projects/1', '{"project_id":1}'),
  
  (10, 8, 8, 'TASK_COMPLETED', 'Task Completed',
   'You completed "Analyze Competitor Social Presence"', true, true, 'LOW',
   '/tasks/11', '{"task_id":11,"project_id":4}'),
  
  -- Notifications for Marketing staff (user_id 12: staff_yc)
  (11, 4, 12, 'TASK_ASSIGNED', 'New Task Assigned',
   'manager_jitt assigned you to "Automate Weekly Reports"', false, false, 'MEDIUM',
   '/tasks/12', '{"task_id":12,"project_id":4}'),
  
  (12, 12, 12, 'MENTION', 'You were mentioned in a comment',
   'staff_kylene mentioned you in "Automate Weekly Reports"', false, false, 'MEDIUM',
   '/tasks/12', '{"task_id":12,"comment_id":15}'),
  
  -- Notifications for Software manager (user_id 9: manager_ee13)
  (13, 9, 9, 'PROJECT_MEMBER_JOINED', 'Team Member Joined',
   'staff_qing joined "Internal DevOps Platform"', true, true, 'LOW',
   '/projects/5', '{"project_id":5,"user_id":10}'),
  
  (14, 10, 9, 'COMMENT_ADDED', 'New Comment',
   'staff_qing commented on "Build Authentication Module"', false, false, 'LOW',
   '/tasks/17', '{"task_id":17,"comment_id":21}'),
  
  -- Notifications for Software staff (user_id 10: staff_qing)
  (15, 9, 10, 'TASK_ASSIGNED', 'New Task Assigned',
   'manager_ee13 assigned you to "Setup Kubernetes Cluster"', true, false, 'HIGH',
   '/tasks/13', '{"task_id":13,"project_id":5}'),
  
  (16, 11, 10, 'MENTION', 'You were mentioned in a comment',
   'staff_kylene mentioned you in "Implement CI/CD Pipeline"', false, false, 'MEDIUM',
   '/tasks/14', '{"task_id":14,"comment_id":18}'),
  
  (17, 10, 10, 'TASK_COMPLETED', 'Task Completed',
   'You completed "Research State Management Solutions"', true, false, 'LOW',
   '/tasks/16', '{"task_id":16,"project_id":6}'),
  
  (18, 1, 10, 'PROJECT_INVITE', 'Added to Project',
   'You were added to "Staff Training System"', true, false, 'MEDIUM',
   '/projects/2', '{"project_id":2}'),
  
  -- Notifications for Software staff (user_id 11: staff_kylene)
  (19, 9, 11, 'TASK_ASSIGNED', 'New Task Assigned',
   'manager_ee13 assigned you to "Create Reusable UI Components"', false, false, 'MEDIUM',
   '/tasks/18', '{"task_id":18,"project_id":6}'),
  
  (20, 4, 11, 'PROJECT_INVITE', 'Added to Project',
   'You were added to "Social Media Analytics"', true, false, 'MEDIUM',
   '/projects/4', '{"project_id":4}'),
  
  -- Additional system notifications
  (21, 1, 1, 'TASK_DEADLINE_APPROACHING', 'Task Deadline Approaching',
   'Task "Build User Registration Flow" is due in 2 days', false, false, 'URGENT',
   '/tasks/1', '{"task_id":1,"due_date":"2025-10-21"}'),
  
  (22, 4, 4, 'PROJECT_DEADLINE_APPROACHING', 'Project Deadline Approaching',
   'Project "Q4 Campaign Management" milestone due in 5 days', false, false, 'HIGH',
   '/projects/3', '{"project_id":3,"milestone":"Q4 Launch"}');

-- Insert notification channels  
INSERT INTO syncup.notification_channels (notification_id, channel)
VALUES
  -- High priority notifications get IN_APP + EMAIL
  (1, 'IN_APP'),
  (1, 'EMAIL'),
  (8, 'IN_APP'),
  (8, 'EMAIL'),
  (15, 'IN_APP'),
  (15, 'EMAIL'),
  (21, 'IN_APP'),
  (21, 'EMAIL'),
  (21, 'SMS'),
  (22, 'IN_APP'),
  (22, 'EMAIL'),
  
  -- Medium priority mentions get IN_APP + EMAIL
  (2, 'IN_APP'),
  (2, 'EMAIL'),
  (7, 'IN_APP'),
  (7, 'EMAIL'),
  (12, 'IN_APP'),
  (12, 'EMAIL'),
  (16, 'IN_APP'),
  (16, 'EMAIL'),
  
  -- Project invites get IN_APP + EMAIL
  (4, 'IN_APP'),
  (4, 'EMAIL'),
  (9, 'IN_APP'),
  (9, 'EMAIL'),
  (18, 'IN_APP'),
  (18, 'EMAIL'),
  (20, 'IN_APP'),
  (20, 'EMAIL'),
  
  -- Other notifications get IN_APP only
  (3, 'IN_APP'),
  (5, 'IN_APP'),
  (6, 'IN_APP'),
  (10, 'IN_APP'),
  (11, 'IN_APP'),
  (13, 'IN_APP'),
  (14, 'IN_APP'),
  (17, 'IN_APP'),
  (19, 'IN_APP');

-- Reset sequence
SELECT setval('syncup.notifications_notification_id_seq', (SELECT MAX(notification_id) FROM syncup.notifications));

