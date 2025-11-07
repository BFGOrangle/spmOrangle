# E2E Test Cases: Task Grouping and Organisation Epic

**Epic:** Task Grouping and Organisation
**Status:** Test Planning Phase
**Total ACs:** 20
**Mapped Test Cases:** TBD

This document maps each Acceptance Criteria (AC) from the epic to specific E2E test cases.

---

## 📋 Test Case Mapping Summary

| Ticket | ACs | Test Cases | Status |
|--------|-----|------------|--------|
| Ticket 1: Department Hierarchy | 2 | TBD | 🔴 Not Started |
| Ticket 2: Task Visibility | 4 | TBD | 🔴 Not Started |
| Ticket 3: Project Visibility | 2 | TBD | 🔴 Not Started |
| Ticket 4: Tag Management | 5 | TBD | 🔴 Not Started |
| Ticket 5: Task Grouping | 4 | TBD | 🔴 Not Started |
| Ticket 6: Project Permissions | 3 | TBD | 🔴 Not Started |
| **Total** | **20** | **TBD** | **0% Complete** |

---

## Ticket 1: Department Hierarchy Support

### AC1.1: Department Parent-Child Relationships
**Requirement:** Departments configured with parent-child relationships correctly reference their parent departments

#### Test Cases

**TC1.1.1: Verify Department Hierarchy Structure**
```gherkin
GIVEN departments are seeded in the database
WHEN I query the departments table
THEN each child department has a valid parent_id
AND the parent_id references an existing department
AND there are no circular references
```
**Test Type:** Database validation
**Priority:** High
**Preconditions:** Database seeded with mock departments

**TC1.1.2: Verify Department API Returns Hierarchy**
```gherkin
GIVEN I am authenticated as any user
WHEN I call GET /api/departments
THEN the response includes department hierarchy information
AND each department shows its parent relationship
```
**Test Type:** API integration
**Priority:** Medium
**Preconditions:** Valid authentication token

---

### AC1.2: Multi-Level Hierarchy Visibility
**Requirement:** Users in parent departments see tasks from child departments (downward visibility)

#### Test Cases

**TC1.2.1: Parent Department Sees Child Department Tasks**
```gherkin
GIVEN I am a MANAGER in "Engineering" (parent department)
AND a task is assigned to "Frontend Team" (child of Engineering)
WHEN I view my tasks list
THEN I see the Frontend Team task
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** User in parent dept, task in child dept

**TC1.2.2: Multi-Level Hierarchy Visibility (3 levels)**
```gherkin
GIVEN Department structure: Engineering > Frontend > UI Team
AND I am a DIRECTOR in Engineering department
AND a task is assigned to UI Team
WHEN I view my tasks list
THEN I see the UI Team task (2 levels down)
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** 3-level department hierarchy

**TC1.2.3: Child Department Cannot See Parent Tasks**
```gherkin
GIVEN I am a STAFF in "Frontend Team" (child department)
AND a task is assigned only to "Engineering" (parent department)
WHEN I view my tasks list
THEN I do NOT see the Engineering department task
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** User in child dept, task in parent dept

**TC1.2.4: Same Department Sees Each Other's Tasks**
```gherkin
GIVEN I am a STAFF in "Frontend Team"
AND a MANAGER in "Frontend Team" has a task
WHEN I view my tasks list
THEN I see the MANAGER's task (same department, bidirectional)
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Two users in same department

---

## Ticket 2: Task Visibility (Department-Based Filtering)

### AC2.1: View Tasks Within Same Department
**Requirement:** Users see tasks from their own department

#### Test Cases

**TC2.1.1: User Sees Own Department Tasks**
```gherkin
GIVEN I am a STAFF in "Marketing" department
AND there are 3 tasks assigned to Marketing staff
WHEN I navigate to the Tasks page
THEN I see all 3 Marketing tasks
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** User + 3 tasks in same department

**TC2.1.2: STAFF Sees MANAGER Tasks in Same Department**
```gherkin
GIVEN I am a STAFF in "Marketing" department
AND a MANAGER in "Marketing" has a task
WHEN I navigate to the Tasks page
THEN I see the MANAGER's task
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** STAFF and MANAGER in same dept

---

### AC2.2: View Tasks from Sub-Departments
**Requirement:** Parent department users see child department tasks

#### Test Cases

**TC2.2.1: Manager Sees Sub-Department Tasks**
```gherkin
GIVEN I am a MANAGER in "Engineering" (parent)
AND there are tasks in "Frontend Team" (child)
WHEN I navigate to the Tasks page
THEN I see Frontend Team tasks
AND I see Engineering tasks
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** Manager in parent, tasks in child

**TC2.2.2: Director Sees All Descendant Tasks**
```gherkin
GIVEN I am a DIRECTOR in "Engineering"
AND there are tasks in multiple sub-departments
WHEN I navigate to the Tasks page
THEN I see tasks from all Engineering sub-departments
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Director with multi-level hierarchy

---

### AC2.3: Cannot View Tasks from Unrelated Departments
**Requirement:** Users cannot see tasks from departments they don't have visibility to

#### Test Cases

**TC2.3.1: Staff Cannot See Unrelated Department Tasks**
```gherkin
GIVEN I am a STAFF in "Marketing" department
AND there are tasks in "Engineering" department
AND Marketing and Engineering are unrelated departments
WHEN I navigate to the Tasks page
THEN I do NOT see any Engineering tasks
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** Two unrelated departments

**TC2.3.2: Manager Cannot See Peer Department Tasks**
```gherkin
GIVEN I am a MANAGER in "Frontend Team"
AND there are tasks in "Backend Team" (peer department)
WHEN I navigate to the Tasks page
THEN I do NOT see Backend Team tasks
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Peer departments at same level

---

### AC2.4: View Cross-Department Tasks When Involved
**Requirement:** Users see tasks from other departments if they are assignees

#### Test Cases

**TC2.4.1: User Sees Cross-Department Task as Assignee**
```gherkin
GIVEN I am a STAFF in "Marketing" department
AND there is a task owned by "Engineering"
AND I am added as an assignee to that task
WHEN I navigate to the Tasks page
THEN I see the Engineering task
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** Cross-department task assignment

**TC2.4.2: User Sees Task with Mixed Department Assignees**
```gherkin
GIVEN I am in "Marketing" department
AND a task has assignees from Marketing, Engineering, and Sales
WHEN I navigate to the Tasks page
THEN I see the task (at least one assignee is visible)
```
**Test Type:** End-to-end
**Priority:** Medium
**Test Data:** Multi-department collaboration task

---

## Ticket 3: Project Visibility (Derived from Task Assignments)

### AC3.1: View Projects with Assigned Tasks
**Requirement:** Users see projects where they have task assignments

#### Test Cases

**TC3.1.1: User Sees Projects with Direct Task Assignment**
```gherkin
GIVEN I am a STAFF user
AND I am assigned to a task in "Project Alpha"
WHEN I navigate to the Projects page
THEN I see "Project Alpha" in "My Projects" section
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** User assigned to project task

**TC3.1.2: Project Owner Sees Their Projects**
```gherkin
GIVEN I am a MANAGER who created "Project Beta"
WHEN I navigate to the Projects page
THEN I see "Project Beta" in "My Projects" section
AND it is marked as owned by me
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Manager as project owner

**TC3.1.3: User Without Tasks Sees No Projects**
```gherkin
GIVEN I am a new STAFF user with no task assignments
WHEN I navigate to the Projects page
THEN I see an empty state message
AND I do NOT see any projects
```
**Test Type:** End-to-end
**Priority:** Medium
**Test Data:** New user with no assignments

---

### AC3.2: View Related Projects from Visible Departments
**Requirement:** ALL users see projects where colleagues from visible departments work (view-only)

#### Test Cases

**TC3.2.1: STAFF Sees Related Projects from Same Department**
```gherkin
GIVEN I am a STAFF in "Marketing" department
AND another Marketing colleague is working on "Project Gamma"
AND I am NOT assigned to Project Gamma
WHEN I navigate to the Projects page
THEN I see "Project Gamma" in "Related Projects" section
AND it shows "View Only" badge
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** Colleague in same dept working on project

**TC3.2.2: Manager Sees Related Projects from Sub-Departments**
```gherkin
GIVEN I am a MANAGER in "Engineering" (parent)
AND a Frontend Team member is working on "Project Delta"
AND I am NOT assigned to Project Delta
WHEN I navigate to the Projects page
THEN I see "Project Delta" in "Related Projects" section
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Sub-department colleague project

**TC3.2.3: User Cannot See Unrelated Department Projects**
```gherkin
GIVEN I am a STAFF in "Marketing"
AND "Engineering" has "Project Epsilon"
AND I am NOT assigned to Project Epsilon
AND Marketing and Engineering are unrelated departments
WHEN I navigate to the Projects page
THEN I do NOT see "Project Epsilon"
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Unrelated department project

**TC3.2.4: Related Project Shows View-Only Badge**
```gherkin
GIVEN I have related projects from visible departments
WHEN I view the Projects page
THEN each related project displays a "View Only" badge
AND I cannot edit related projects
```
**Test Type:** End-to-end
**Priority:** Medium
**Test Data:** Related projects

---

## Ticket 4: Tag Management (Multi-Tag Categorization)

### AC4.1: Create Tags (Manager-Only)
**Requirement:** Only MANAGER can create new tags

#### Test Cases

**TC4.1.1: Manager Successfully Creates Tag**
```gherkin
GIVEN I am logged in as MANAGER
AND I am creating a task
WHEN I enter a new tag "urgent" that doesn't exist
AND I submit the task
THEN the tag "urgent" is created in the system
AND the task is tagged with "urgent"
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** MANAGER user, new tag name

**TC4.1.2: STAFF Cannot Create Tag (API Level)**
```gherkin
GIVEN I am logged in as STAFF
WHEN I directly call POST /api/tag with new tag "important"
THEN I receive 403 Forbidden error
AND the tag is NOT created
```
**Test Type:** API integration
**Priority:** High
**Test Data:** STAFF user, API call

**TC4.1.3: HR Cannot Create Tag**
```gherkin
GIVEN I am logged in as HR
WHEN I attempt to create a task with new tag "admin"
THEN the tag creation should fail
OR the system should prevent tag creation in UI
```
**Test Type:** End-to-end
**Priority:** Medium
**Test Data:** HR user

---

### AC4.2: Delete Tags (Manager-Only)
**Requirement:** Only MANAGER can soft-delete tags

#### Test Cases

**TC4.2.1: Manager Successfully Soft-Deletes Tag**
```gherkin
GIVEN I am logged in as MANAGER
AND a tag "deprecated" exists in the system
WHEN I call DELETE /api/tag/{id}
THEN the tag is marked as delete_ind = true
AND the tag no longer appears in tag suggestions
```
**Test Type:** API integration
**Priority:** Critical
**Test Data:** MANAGER user, existing tag

**TC4.2.2: STAFF Cannot Delete Tag**
```gherkin
GIVEN I am logged in as STAFF
AND a tag "test" exists
WHEN I call DELETE /api/tag/{id}
THEN I receive 403 Forbidden error
AND the tag remains active
```
**Test Type:** API integration
**Priority:** High
**Test Data:** STAFF user, existing tag

**TC4.2.3: Deleted Tag Not Shown in Suggestions**
```gherkin
GIVEN a tag "old-tag" has been soft-deleted
WHEN I am creating a task and view tag suggestions
THEN "old-tag" does NOT appear in the suggestions list
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Soft-deleted tag

**TC4.2.4: Tag Reactivation on Recreation**
```gherkin
GIVEN a tag "feature" was soft-deleted
WHEN a MANAGER creates a new tag with name "feature"
THEN the existing tag is reactivated (delete_ind = false)
AND no duplicate tag is created
```
**Test Type:** API integration
**Priority:** Medium
**Test Data:** Soft-deleted tag, MANAGER user

---

### AC4.3: Assign Existing Tags (All Users)
**Requirement:** All users can assign existing tags to tasks

#### Test Cases

**TC4.3.1: STAFF Assigns Existing Tag to Task**
```gherkin
GIVEN I am logged in as STAFF
AND tags "bug" and "urgent" exist in the system
WHEN I create a task and select these tags
THEN the task is created with both tags
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** STAFF user, existing tags

**TC4.3.2: Multiple Users Can Use Same Tags**
```gherkin
GIVEN tag "frontend" exists
AND multiple users (STAFF, MANAGER, HR) create tasks
WHEN each user assigns "frontend" tag to their task
THEN all tasks are successfully tagged
```
**Test Type:** End-to-end
**Priority:** Medium
**Test Data:** Multiple users, shared tag

---

### AC4.4: Tag Permissions Enforcement
**Requirement:** Tag creation/deletion restricted to MANAGER only

#### Test Cases

**TC4.4.1: Verify Backend Permission on Tag Creation**
```gherkin
GIVEN I have STAFF authentication token
WHEN I call POST /api/tag with new tag
THEN I receive 403 Forbidden
AND response indicates insufficient permissions
```
**Test Type:** API integration
**Priority:** Critical
**Test Data:** STAFF token, API endpoint

**TC4.4.2: Verify Backend Permission on Tag Deletion**
```gherkin
GIVEN I have STAFF authentication token
AND a tag with id=123 exists
WHEN I call DELETE /api/tag/123
THEN I receive 403 Forbidden
```
**Test Type:** API integration
**Priority:** Critical
**Test Data:** STAFF token, tag ID

---

### AC4.5: Multi-Tag Task Display and Filtering
**Requirement:** Tasks can have multiple tags; tags used for filtering and grouping

#### Test Cases

**TC4.5.1: Task Displayed Under Multiple Tags in Tag View**
```gherkin
GIVEN a task has tags "bug", "urgent", and "frontend"
WHEN I navigate to Tasks page and select "Tag view" grouping
THEN the task appears under "bug" group
AND the task appears under "urgent" group
AND the task appears under "frontend" group
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Multi-tagged task

**TC4.5.2: Tag Filtering Works Across All Views**
```gherkin
GIVEN tasks with various tags exist
WHEN I apply tag filter "backend"
THEN only tasks with "backend" tag are shown
AND the filter works in status board view
AND the filter works in project view
AND the filter works in department view
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Tasks with different tags

**TC4.5.3: Empty Tag Group Shows No Tasks**
```gherkin
GIVEN a tag "testing" exists but has no tasks
WHEN I view Tasks page in "Tag view" grouping
THEN I see "testing" group header
AND it shows 0 tasks
```
**Test Type:** End-to-end
**Priority:** Low
**Test Data:** Tag with no tasks

---

## Ticket 5: Task Grouping Views

### AC5.1: Toggle Between Grouping Views
**Requirement:** Users can switch between status/project/department/tag grouping

#### Test Cases

**TC5.1.1: Switch to Status Board View**
```gherkin
GIVEN I am on the Tasks page
WHEN I select "Status board" from grouping dropdown
THEN tasks are grouped into TODO, IN_PROGRESS, BLOCKED, COMPLETED columns
AND each column shows its task count
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** Tasks with different statuses

**TC5.1.2: Switch to Project View**
```gherkin
GIVEN I am on the Tasks page
WHEN I select "Project view" from grouping dropdown
THEN tasks are grouped by project name
AND each project shows its task count
AND tasks with no project appear in separate group
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** Tasks in multiple projects

**TC5.1.3: Switch to Department View**
```gherkin
GIVEN I am on the Tasks page
WHEN I select "Team/Department view" from grouping dropdown
THEN tasks are grouped by department
AND I only see departments I have visibility to
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Tasks across departments

**TC5.1.4: Switch to Tag View**
```gherkin
GIVEN I am on the Tasks page
WHEN I select "Tag view" from grouping dropdown
THEN tasks are grouped by tags
AND tasks appear under each of their tags
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Multi-tagged tasks

---

### AC5.2: Group Headers Show Task Counts
**Requirement:** Each group displays the number of tasks it contains

#### Test Cases

**TC5.2.1: Task Counts Accurate in Status View**
```gherkin
GIVEN there are 5 TODO, 3 IN_PROGRESS, 1 BLOCKED, 2 COMPLETED tasks
WHEN I view Status board
THEN TODO column shows "(5 tasks)"
AND IN_PROGRESS column shows "(3 tasks)"
AND BLOCKED column shows "(1 task)"
AND COMPLETED column shows "(2 tasks)"
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Tasks with counts

**TC5.2.2: Task Counts Update After Status Change**
```gherkin
GIVEN I am viewing Status board with task counts
WHEN I drag a task from TODO to IN_PROGRESS
THEN TODO count decreases by 1
AND IN_PROGRESS count increases by 1
```
**Test Type:** End-to-end
**Priority:** Medium
**Test Data:** Drag and drop enabled

---

### AC5.3: Multi-Tag Task Display in Grouping
**Requirement:** Tasks with multiple tags appear in each tag group; tasks with one project appear once

#### Test Cases

**TC5.3.1: Multi-Tag Task Appears in All Tag Groups**
```gherkin
GIVEN a task has tags "bug", "urgent", "backend"
WHEN I view Tag grouping
THEN the task appears in "bug" group
AND the task appears in "urgent" group
AND the task appears in "backend" group
AND the same task is rendered 3 times (once per tag)
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Task with 3 tags

**TC5.3.2: Single-Project Task Appears Once in Project View**
```gherkin
GIVEN a task belongs to "Project Alpha"
WHEN I view Project grouping
THEN the task appears only once under "Project Alpha"
AND the task does NOT appear in other project groups
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Single-project task

---

### AC5.4: Respect Permissions in Grouped Views
**Requirement:** Department filtering applies across all grouping views

#### Test Cases

**TC5.4.1: Department Filtering in Status Board View**
```gherkin
GIVEN I am in "Marketing" department
AND there are tasks in both Marketing and Engineering
WHEN I view Status board grouping
THEN I only see Marketing tasks (and visible dept tasks)
AND I do NOT see Engineering tasks
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** Cross-department tasks

**TC5.4.2: Department Filtering in Tag View**
```gherkin
GIVEN I am in "Marketing" department
AND Engineering has tasks tagged "backend"
WHEN I view Tag grouping and expand "backend" group
THEN I do NOT see Engineering tasks
AND I only see visible department tasks
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Tag shared across departments

**TC5.4.3: Parent Department Sees Child Tasks in All Views**
```gherkin
GIVEN I am a MANAGER in parent "Engineering" department
AND child "Frontend Team" has tasks
WHEN I switch between different grouping views
THEN I see Frontend Team tasks in all views
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Parent-child department structure

---

## Ticket 6: Project Creation Permissions (Manager-Only)

### AC6.1: Project Creation Permissions (Frontend)
**Requirement:** Only MANAGER sees Create Project button; non-MANAGER users don't see option

#### Test Cases

**TC6.1.1: MANAGER Sees Create Project Button**
```gherkin
GIVEN I am logged in as MANAGER
WHEN I navigate to the Projects page
THEN I see "New Project" button in the header
AND I see "Create Project" button in empty state (if no projects)
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** MANAGER user

**TC6.1.2: STAFF Does Not See Create Project Button**
```gherkin
GIVEN I am logged in as STAFF
WHEN I navigate to the Projects page
THEN I do NOT see "New Project" button in the header
AND I do NOT see "Create Project" button in empty state
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** STAFF user

**TC6.1.3: HR Does Not See Create Project Button**
```gherkin
GIVEN I am logged in as HR
WHEN I navigate to the Projects page
THEN I do NOT see "New Project" button
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** HR user

**TC6.1.4: DIRECTOR Does Not See Create Project Button**
```gherkin
GIVEN I am logged in as DIRECTOR
WHEN I navigate to the Projects page
THEN I do NOT see "New Project" button
```
**Test Type:** End-to-end
**Priority:** Medium
**Test Data:** DIRECTOR user

---

### AC6.2: Required Project Metadata
**Requirement:** Project creation requires name and department

#### Test Cases

**TC6.2.1: Manager Creates Project Successfully**
```gherkin
GIVEN I am logged in as MANAGER
WHEN I click "New Project" button
AND I enter name "New Marketing Campaign"
AND I enter description "Q1 2025 Campaign"
AND I submit the form
THEN project is created successfully
AND I see the new project in my projects list
AND all my department members are auto-added
```
**Test Type:** End-to-end
**Priority:** Critical
**Test Data:** MANAGER user, valid project data

**TC6.2.2: Project Creation Requires Name**
```gherkin
GIVEN I am creating a project as MANAGER
WHEN I leave the name field empty
AND I attempt to submit
THEN I see validation error "Name is required"
AND project is NOT created
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** Invalid form data

**TC6.2.3: Project Creation Auto-Adds Department Members**
```gherkin
GIVEN I am a MANAGER in "Marketing" department
AND Marketing has 5 active staff members
WHEN I create a project "Project Zeta"
THEN all 5 Marketing staff are added as project members
AND they can see the project in their projects list
```
**Test Type:** End-to-end
**Priority:** High
**Test Data:** MANAGER with department colleagues

---

### AC6.3: Unauthorized Access Handling
**Requirement:** Non-MANAGER receives 403 Forbidden when attempting to create project

#### Test Cases

**TC6.3.1: STAFF Receives 403 on Direct API Call**
```gherkin
GIVEN I have STAFF authentication token
WHEN I call POST /api/projects directly via API
THEN I receive 403 Forbidden error
AND project is NOT created
```
**Test Type:** API integration
**Priority:** Critical
**Test Data:** STAFF token, project data

**TC6.3.2: Backend Enforces Permission on Project Creation**
```gherkin
GIVEN I attempt to bypass frontend restrictions
WHEN I send POST /api/projects with STAFF credentials
THEN backend returns 403 Forbidden
AND response includes "Access Denied" message
```
**Test Type:** API integration
**Priority:** High
**Test Data:** Non-MANAGER token

---

## 🎯 Test Execution Strategy

### Test Priorities

**Critical (Must Test First):**
- TC1.2.1, TC1.2.3: Department visibility core behavior
- TC2.1.1, TC2.3.1: Basic task visibility
- TC3.1.1, TC3.2.1: Project visibility
- TC4.1.1, TC4.2.1: Tag creation/deletion
- TC5.1.1 - TC5.1.4: Grouping view switching
- TC6.1.1, TC6.1.2: Project creation permissions
- TC6.3.1: Permission enforcement

**High (Test Next):**
- All "Cannot See" negative test cases
- API permission enforcement tests
- Multi-level hierarchy tests

**Medium (Test After Core):**
- Edge cases and validation
- UI state management
- Empty states

**Low (Nice to Have):**
- Cosmetic checks
- Count accuracy
- Badge display

### Test Data Requirements

**Users:**
- DIRECTOR (Engineering)
- MANAGER (Marketing, Frontend Team)
- HR (Admin department)
- STAFF (Multiple departments)

**Departments:**
- Engineering (parent)
  - Frontend Team (child)
    - UI Team (grandchild)
  - Backend Team (child)
- Marketing (parent)
  - Content Team (child)
- Sales (standalone)

**Projects:**
- 3-5 projects across departments
- Mix of owned and related projects

**Tasks:**
- 20+ tasks distributed across:
  - Different statuses
  - Different projects
  - Different departments
  - Different tag combinations

**Tags:**
- 10+ tags (bug, urgent, frontend, backend, feature, etc.)
- At least 1 soft-deleted tag

---

**Last Updated:** 2025-11-05
**Total Test Cases Mapped:** 72
**Test Implementation Status:** 0%
**Next Step:** Begin implementing critical test cases
