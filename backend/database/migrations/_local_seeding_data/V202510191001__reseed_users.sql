-- Insert users with exact cognito_sub values and departments
-- User IDs are explicitly set to match the provided JSON data

INSERT INTO syncup.users (id, username, email, role_type, cognito_sub, department, created_at, updated_at)
VALUES
  -- Front Office Department
  (1, 'OrangleManagerTestUser', 'contactus@seniorsync.sg', 'MANAGER', '69ca95fc-90a1-7075-9c7d-3e9a5dff231d', 'Front Office', '2025-09-21 17:07:14.465369+00', '2025-10-19 04:01:08.688102+00'),
  (2, 'staff_tester', 'orangletester1@gmail.com', 'STAFF', '394ae54c-f041-70dd-53c3-24cfc3abe446', 'Front Office', '2025-10-18 10:00:08.512247+00', '2025-10-19 04:01:13.20769+00'),
  (13, 'staff_tester_2', 'qyprojects1@gmail.com', 'STAFF', '094a556c-5031-702f-45a2-8e7db25735f6', 'Front Office', '2025-10-19 04:25:01.381906+00', '2025-10-19 04:25:13.322273+00'),
  
  -- Human Resource Department (no projects/tasks, just user record)
  (3, 'hr_tester', 'qyprojects@gmail.com', 'HR', '79aa85fc-e021-709a-85b2-fb77b6c327b3', 'Human Resource', '2025-10-18 10:02:26.600669+00', '2025-10-19 04:00:53.167203+00'),
  
  -- Marketing Department
  (4, 'manager_jitt', 'jhlim.2023@smu.edu.sg', 'MANAGER', '096a454c-c071-70c8-172a-bbc28059e0e9', 'Marketing', '2025-10-19 03:45:23.800889+00', '2025-10-19 04:05:01.082835+00'),
  (8, 'staff_jordan', 'congyao.mok.2023@smu.edu.sg', 'STAFF', '99aa35cc-80e1-70d5-7def-0f48b583b38e', 'Marketing', '2025-10-19 04:13:35.933093+00', '2025-10-19 04:17:29.391403+00'),
  (12, 'staff_yc', 'yeoyuchen@outlook.com', 'STAFF', '292a751c-5001-7097-77a2-02e18c1f98ec', 'Marketing', '2025-10-19 04:22:41.278072+00', '2025-10-19 04:24:26.852919+00'),
  
  -- Software Department
  (9, 'manager_ee13', 'ee13@gmail.com', 'MANAGER', '69ea85dc-80e1-70d4-ff48-d6344d863a76', 'Software', '2025-10-19 04:20:06.593925+00', '2025-10-19 04:21:21.445314+00'),
  (10, 'staff_qing', 'qing@gmail.com', 'STAFF', '497ab54c-2061-70b0-94f9-199a84c5aabe', 'Software', '2025-10-19 04:20:38.556487+00', '2025-10-19 04:21:25.355362+00'),
  (11, 'staff_kylene', 'kylenestaff@gmail.com', 'STAFF', 'b9ca655c-30e1-70cf-8a67-eea05f6ca28b', 'Software', '2025-10-19 04:21:01.413984+00', '2025-10-19 04:21:28.49591+00');

-- Reset the sequence to the highest ID + 1
SELECT setval('syncup.users_id_seq', (SELECT MAX(id) FROM syncup.users));

