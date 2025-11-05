-- Map users to org units using stable usernames (no hardcoded IDs)

-- Managers / leads
UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'OrangleManagerTestUser'
  AND d.name = 'Sales Director (Derek Tan)';

UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'manager_jitt'
  AND d.name = 'Engineering Operation Division Director (Philip Lee)';

UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'manager_ee13'
  AND d.name = 'HR and Admin Director (Sally Loh)';

-- HR
UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'hr_tester'
  AND d.name = 'HR Team (40)';

-- Sales subtree
UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'staff_tester'
  AND d.name = 'Account Managers (60)';

-- System Solutioning
UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'staff_tester_2'
  AND d.name = 'Support Team (30)';

UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'staff_jordan'
  AND d.name = 'Developers (50)';

-- Engineering Ops
UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'staff_yc'
  AND d.name = 'Junior Engineers (150)';

-- Finance
UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'staff_qing'
  AND d.name = 'Finance Executive (50)';

-- IT
UPDATE syncup.users u
SET department_id = d.id
FROM syncup.departments d
WHERE u.username = 'staff_kylene'
  AND d.name = 'IT Team (30)';
