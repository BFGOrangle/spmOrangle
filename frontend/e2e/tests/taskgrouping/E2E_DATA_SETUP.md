# E2E Test Data Setup Guide

## Overview
This document explains the test data seeding required for E2E tests in the Task Grouping and Organisation epic.

## Current Test Data Status

### Test Users (from `.env`)
| Email | Role | Department (After Seeding) | Purpose |
|-------|------|---------------------------|---------|
| `contactus@seniorsync.sg` | MANAGER | Sales Manager (5) | Test parent dept user seeing child tasks |
| `orangletester1@gmail.com` | STAFF | Account Managers (60) | Test child dept user NOT seeing parent tasks |
| `qyprojects@gmail.com` | HR | HR Team (40) | Test unrelated department isolation |
| `qyprojects1@gmail.com` | STAFF | Sales Manager (5) | Test same department visibility |

### Department Hierarchy (Pre-seeded)
```
Managing Director (Jack Sim)
├── Sales Director (Derek Tan)
│   └── Sales Manager (5)
│       └── Account Managers (60) ← STAFF user
├── HR and Admin Director (Sally Loh)
│   └── HR Team (40) ← HR user
├── Engineering Operation Division Director (Philip Lee)
│   ├── Senior Engineers (20)
│   ├── Junior Engineers (150)
│   ├── Call Centre (60)
│   └── Operation Planning Team (30)
└── [Other divisions...]
```

## Required Seeding Migration

**File:** `backend/database/migrations/_local_seeding_data/V202511052000__seed_e2e_test_data.sql`

**What it does:**
1. ✅ Updates test user `department_id` to use hierarchical departments
2. ✅ Creates 4 test tasks:
   - **Task 1 (Child Dept):** Assigned to STAFF in `Account Managers (60)`
   - **Task 2 (Parent Dept):** Assigned to MANAGER in `Sales Manager (5)`
   - **Task 3 (Unrelated):** Assigned to HR in `HR Team (40)`
   - **Task 4 (Same Dept):** Assigned to another user in `Sales Manager (5)`

## How to Apply Seeding

### Option 1: Run Migration (Recommended)
```bash
cd backend/spmorangle
./mvnw flyway:migrate
```

### Option 2: Manual SQL (Development Only)
```bash
cd backend
psql -h localhost -U <username> -d <database> -f database/migrations/_local_seeding_data/V202511052000__seed_e2e_test_data.sql
```

## Test Coverage After Seeding

### TC1.2.1: Parent Department Sees Child Department Tasks
- **GIVEN:** MANAGER (contactus@seniorsync.sg) in "Sales Manager (5)"
- **AND:** Task assigned to STAFF in "Account Managers (60)" (child dept)
- **THEN:** MANAGER should see "E2E Test Task - Child Department"

### TC1.2.3: Child Department Cannot See Parent Tasks
- **GIVEN:** STAFF (orangletester1@gmail.com) in "Account Managers (60)"
- **AND:** Task assigned to MANAGER in "Sales Manager (5)" (parent dept)
- **THEN:** STAFF should NOT see "E2E Test Task - Parent Department"

### TC2.1.1: User Sees Own Department Tasks
- **GIVEN:** MANAGER in "Sales Manager (5)"
- **WHEN:** MANAGER views tasks
- **THEN:** MANAGER sees both:
  - "E2E Test Task - Parent Department" (own task)
  - "E2E Test Task - Same Department" (colleague's task)

### TC2.3.1: Cannot See Unrelated Department Tasks
- **GIVEN:** MANAGER/STAFF in Sales departments
- **WHEN:** They view tasks
- **THEN:** They should NOT see "E2E Test Task - Unrelated Department" (HR dept)

## Verification Queries

### Check User Departments
```sql
SELECT u.email, u.role_type, d.name as department, d.parent_id
FROM syncup.users u
LEFT JOIN syncup.departments d ON u.department_id = d.id
WHERE u.email IN (
  'contactus@seniorsync.sg',
  'orangletester1@gmail.com',
  'qyprojects@gmail.com',
  'qyprojects1@gmail.com'
);
```

### Check Test Tasks
```sql
SELECT
  t.title,
  t.status,
  u.email as owner,
  d.name as owner_department
FROM syncup.tasks t
JOIN syncup.users u ON t.owner_id = u.id
LEFT JOIN syncup.departments d ON u.department_id = d.id
WHERE t.title LIKE 'E2E Test Task%';
```

### Check Department Hierarchy
```sql
WITH RECURSIVE dept_tree AS (
  SELECT id, name, parent_id, 0 as level, name::text as path
  FROM syncup.departments
  WHERE name = 'Sales Director (Derek Tan)'

  UNION ALL

  SELECT d.id, d.name, d.parent_id, dt.level + 1,
         dt.path || ' > ' || d.name
  FROM syncup.departments d
  JOIN dept_tree dt ON d.parent_id = dt.id
)
SELECT level, name, path FROM dept_tree ORDER BY level, name;
```

## Expected Test Results (With Seeding)

| Test Case | User | Should See Tasks | Should NOT See Tasks |
|-----------|------|------------------|---------------------|
| TC1.2.1 | MANAGER (Sales Manager) | Child Dept Task | Unrelated Dept Task |
| TC1.2.3 | STAFF (Account Managers) | Own Task | Parent Dept Task, Unrelated Task |
| TC2.1.1 | MANAGER (Sales Manager) | Own + Same Dept + Child Tasks | Unrelated Task |
| TC2.3.1 | STAFF (Account Managers) | Own Task | Parent Task, Unrelated Task |

## Troubleshooting

### Issue: Tests Still Failing After Seeding
**Solution:** Clear test data cache and restart backend
```bash
cd backend/spmorangle
./mvnw clean spring-boot:run
```

### Issue: Users Not in Correct Departments
**Solution:** Check if `department_id` was properly updated
```sql
SELECT email, department_id FROM syncup.users
WHERE email IN ('contactus@seniorsync.sg', 'orangletester1@gmail.com');
```

### Issue: Tasks Not Visible
**Solution:** Check task assignees and department relationships
```sql
SELECT t.title, ta.staff_id, u.email, u.department_id
FROM syncup.tasks t
JOIN syncup.task_assignee ta ON t.id = ta.task_id
JOIN syncup.users u ON ta.staff_id = u.id
WHERE t.title LIKE 'E2E Test%';
```

---

**Last Updated:** 2025-11-05
**Status:** ⏳ Migration Created, Awaiting Application
**Next Step:** Run Flyway migration before executing E2E tests
