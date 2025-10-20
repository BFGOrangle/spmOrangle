-- Insert projects for each department (2 per department)
-- Each project includes all department members + 1 cross-department collaborator

-- Front Office Projects
INSERT INTO syncup.projects (id, name, owner_id, description, delete_ind, created_by, updated_by)
VALUES
  (1, 'Customer Onboarding Portal', 1, 'Streamline customer registration and onboarding process with automated workflows', false, 1, 1),
  (2, 'Staff Training System', 1, 'Internal training management system for employee development', false, 1, 1);

-- Marketing Projects
INSERT INTO syncup.projects (id, name, owner_id, description, delete_ind, created_by, updated_by)
VALUES
  (3, 'Q4 Campaign Management', 4, 'Manage all marketing campaigns for Q4 2025 including social media and email', false, 4, 4),
  (4, 'Social Media Analytics', 4, 'Track and analyze social media engagement across all platforms', false, 4, 4);

-- Software Projects
INSERT INTO syncup.projects (id, name, owner_id, description, delete_ind, created_by, updated_by)
VALUES
  (5, 'Internal DevOps Platform', 9, 'Build internal CI/CD pipeline and infrastructure automation tools', false, 9, 9),
  (6, 'Mobile App Rewrite', 9, 'Complete rewrite of mobile application using React Native', false, 9, 9);

-- Project Members
-- Project 1: Customer Onboarding Portal (Front Office + Marketing staff_jordan)
INSERT INTO syncup.project_members (project_id, user_id, added_by)
VALUES
  (1, 1, 1),  -- Manager: OrangleManagerTestUser
  (1, 2, 1),  -- Staff: staff_tester
  (1, 13, 1), -- Staff: staff_tester_2
  (1, 8, 1);  -- Cross-dept: staff_jordan from Marketing

-- Project 2: Staff Training System (Front Office + Software staff_qing)
INSERT INTO syncup.project_members (project_id, user_id, added_by)
VALUES
  (2, 1, 1),  -- Manager: OrangleManagerTestUser
  (2, 2, 1),  -- Staff: staff_tester
  (2, 13, 1), -- Staff: staff_tester_2
  (2, 10, 1); -- Cross-dept: staff_qing from Software

-- Project 3: Q4 Campaign Management (Marketing + Front Office staff_tester)
INSERT INTO syncup.project_members (project_id, user_id, added_by)
VALUES
  (3, 4, 4),  -- Manager: manager_jitt
  (3, 8, 4),  -- Staff: staff_jordan
  (3, 12, 4), -- Staff: staff_yc
  (3, 2, 4);  -- Cross-dept: staff_tester from Front Office

-- Project 4: Social Media Analytics (Marketing + Software staff_kylene)
INSERT INTO syncup.project_members (project_id, user_id, added_by)
VALUES
  (4, 4, 4),  -- Manager: manager_jitt
  (4, 8, 4),  -- Staff: staff_jordan
  (4, 12, 4), -- Staff: staff_yc
  (4, 11, 4); -- Cross-dept: staff_kylene from Software

-- Project 5: Internal DevOps Platform (Software + Marketing staff_yc)
INSERT INTO syncup.project_members (project_id, user_id, added_by)
VALUES
  (5, 9, 9),  -- Manager: manager_ee13
  (5, 10, 9), -- Staff: staff_qing
  (5, 11, 9), -- Staff: staff_kylene
  (5, 12, 9); -- Cross-dept: staff_yc from Marketing

-- Project 6: Mobile App Rewrite (Software + Front Office staff_tester_2)
INSERT INTO syncup.project_members (project_id, user_id, added_by)
VALUES
  (6, 9, 9),  -- Manager: manager_ee13
  (6, 10, 9), -- Staff: staff_qing
  (6, 11, 9), -- Staff: staff_kylene
  (6, 13, 9); -- Cross-dept: staff_tester_2 from Front Office

-- Reset the sequence
SELECT setval('syncup.projects_id_seq', (SELECT MAX(id) FROM syncup.projects));

