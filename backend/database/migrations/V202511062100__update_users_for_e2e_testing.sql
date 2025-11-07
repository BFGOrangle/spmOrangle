-- Update users table with E2E test user data
-- This script updates existing users with correct role assignments and department mappings

-- Update user 1 (OrangleManagerTestUser - Primary test manager)
UPDATE syncup.users
SET
    username = 'OrangleManagerTestUser',
    email = 'contactus@seniorsync.sg',
    role_type = 'MANAGER',
    cognito_sub = '69ca95fc-90a1-7075-9c7d-3e9a5dff231d',
    created_at = '2025-09-21 17:07:14.465369+00',
    updated_at = '2025-11-06 05:00:12.858806+00',
    is_active = true,
    department_id = 3
WHERE id = 1;

-- Update user 2 (acct_manager_manager)
UPDATE syncup.users
SET
    username = 'acct_manager_manager',
    email = 'orangletester1@gmail.com',
    role_type = 'MANAGER',
    cognito_sub = '394ae54c-f041-70dd-53c3-24cfc3abe446',
    created_at = '2025-10-18 10:00:08.512247+00',
    updated_at = '2025-11-06 14:37:59.803739+00',
    is_active = true,
    department_id = 11
WHERE id = 2;

-- Update user 3 (hr_tester - Used for E2E tests with TEST_HR_AUTH_TOKEN)
UPDATE syncup.users
SET
    username = 'hr_tester',
    email = 'qyprojects@gmail.com',
    role_type = 'HR',
    cognito_sub = '79aa85fc-e021-709a-85b2-fb77b6c327b3',
    created_at = '2025-10-18 10:02:26.600669+00',
    updated_at = '2025-11-06 05:00:12.858806+00',
    is_active = true,
    department_id = 19
WHERE id = 3;

-- Update user 4 (staff_jitt)
UPDATE syncup.users
SET
    username = 'staff_jitt',
    email = 'jhlim.2023@smu.edu.sg',
    role_type = 'STAFF',
    cognito_sub = '096a454c-c071-70c8-172a-bbc28059e0e9',
    created_at = '2025-10-19 03:45:23.800889+00',
    updated_at = '2025-11-06 14:37:13.325873+00',
    is_active = true,
    department_id = 10
WHERE id = 4;

-- Update user 8 (staff_jordan)
UPDATE syncup.users
SET
    username = 'staff_jordan',
    email = 'congyao.mok.2023@smu.edu.sg',
    role_type = 'STAFF',
    cognito_sub = '99aa35cc-80e1-70d5-7def-0f48b583b38e',
    created_at = '2025-10-19 04:13:35.933093+00',
    updated_at = '2025-11-06 05:00:12.858806+00',
    is_active = true,
    department_id = 13
WHERE id = 8;

-- Update user 9 (manager_ee13)
UPDATE syncup.users
SET
    username = 'manager_ee13',
    email = 'taneeherng@gmail.com',
    role_type = 'MANAGER',
    cognito_sub = '69ea85dc-80e1-70d4-ff48-d6344d863a76',
    created_at = '2025-10-19 04:20:06.593925+00',
    updated_at = '2025-11-06 14:29:47.505679+00',
    is_active = true,
    department_id = 10
WHERE id = 9;

-- Update user 10 (staff_qing)
UPDATE syncup.users
SET
    username = 'staff_qing',
    email = 'qing@gmail.com',
    role_type = 'STAFF',
    cognito_sub = '497ab54c-2061-70b0-94f9-199a84c5aabe',
    created_at = '2025-10-19 04:20:38.556487+00',
    updated_at = '2025-11-06 05:00:12.858806+00',
    is_active = true,
    department_id = 23
WHERE id = 10;

-- Update user 11 (staff_kylene)
UPDATE syncup.users
SET
    username = 'staff_kylene',
    email = 'kylenestaff@gmail.com',
    role_type = 'STAFF',
    cognito_sub = 'b9ca655c-30e1-70cf-8a67-eea05f6ca28b',
    created_at = '2025-10-19 04:21:01.413984+00',
    updated_at = '2025-11-06 05:00:12.858806+00',
    is_active = true,
    department_id = 24
WHERE id = 11;

-- Update user 12 (staff_yc)
UPDATE syncup.users
SET
    username = 'staff_yc',
    email = 'yeoyuchen@outlook.com',
    role_type = 'STAFF',
    cognito_sub = '292a751c-5001-7097-77a2-02e18c1f98ec',
    created_at = '2025-10-19 04:22:41.278072+00',
    updated_at = '2025-11-06 05:00:12.858806+00',
    is_active = true,
    department_id = 16
WHERE id = 12;

-- Update user 13 (staff_tester_2)
UPDATE syncup.users
SET
    username = 'staff_tester_2',
    email = 'eeherng.tan.2023@smu.edu.sg',
    role_type = 'STAFF',
    cognito_sub = '094a556c-5031-702f-45a2-8e7db25735f6',
    created_at = '2025-10-19 04:25:01.381906+00',
    updated_at = '2025-11-06 14:33:23.162927+00',
    is_active = true,
    department_id = 10
WHERE id = 13;

-- Verify the updates
SELECT id, username, email, role_type, cognito_sub, department_id, is_active
FROM syncup.users
WHERE id IN (1, 2, 3, 4, 8, 9, 10, 11, 12, 13)
ORDER BY id;
