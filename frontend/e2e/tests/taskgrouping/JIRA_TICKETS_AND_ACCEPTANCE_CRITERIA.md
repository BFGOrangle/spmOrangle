# Epic: Task Grouping and Organisation

This document tracks acceptance criteria for the Task Grouping and Organisation epic and implementation status.

---

## Ticket 1: Company Organisation (Department Hierarchy)

**User Story:**
As an admin (HR)
I want to define and manage department reporting relationships
So that the system correctly reflects who reports to whom and downstream visibility can be derived.

**Implementation Status:** ✅ IMPLEMENTED
**Files:**
- Backend: [DepartmentMgmtController.java](backend/spmorangle/src/main/java/com/spmorangle/crm/departmentmgmt/controller/DepartmentMgmtController.java)
- Model: [Department.java](backend/spmorangle/src/main/java/com/spmorangle/crm/departmentmgmt/model/Department.java)
- Service: [DepartmentalVisibilityServiceImpl.java](backend/spmorangle/src/main/java/com/spmorangle/crm/departmentmgmt/service/impl/DepartmentalVisibilityServiceImpl.java)
- Migration: [V202510301800__create_departments_table.sql](backend/database/migrations/V202510301800__create_departments_table.sql)

### Acceptance Criteria

#### AC1.1: Linking department with parent-child relationship ✅
**GIVEN** I am an admin (HR)
**WHEN** a new department "Finance Executive" is created and its parent is set to "Finance Managers"
**THEN** "Finance Executive" is stored as a child of "Finance Managers"

**Implementation:** HR-only endpoint `POST /api/departments` creates departments with `parentId` field

#### AC1.2: Multiple levels of reporting ✅
**GIVEN** Department C reports to Department B
**AND** Department B reports to Department A
**WHEN** a task is assigned to a Department C staff
**THEN** users in Department A and Department B can both see the task due to hierarchical reporting

**Implementation:** `DepartmentalVisibilityServiceImpl.visibleDepartmentsForAssignedDept()` recursively traverses child departments

**⚠️ CONCERNS:**
- **Frontend UI Gap**: No department hierarchy visualization or management interface for HR users to create/edit departments through the UI
- **E2E Tests Missing**: No end-to-end tests validating department creation and hierarchy traversal
- **Role Naming**: Ticket says "admin" but implementation uses "HR" role - clarify which role should manage departments

---

## Ticket 2: Visibility of Tasks (Department-Based Filtering)

**User Story:**
As a User
I want to view only tasks that have at least one assignee from my department (or reporting departments)
So that I can stay updated on my team's work but not see unrelated tasks.

**Implementation Status:** ✅ IMPLEMENTED
**Files:**
- Backend: [TaskServiceImpl.java](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/service/impl/TaskServiceImpl.java:271-295)
- Tests: [TaskServiceDepartmentFilteringTest.java](backend/spmorangle/src/test/java/com/spmorangle/crm/taskmanagement/service/impl/TaskServiceDepartmentFilteringTest.java)

### Acceptance Criteria

#### AC2.1: View task within department ✅
**GIVEN** I am a user in Department A
**AND** a task has at least one assignee from Department A
**WHEN** I view the task list
**THEN** I can see that task

**Test:** `TaskServiceDepartmentFilteringTest.testGetAllUserTasks_SameInDepartment()`

#### AC2.2: View task from sub-department ✅
**GIVEN** I am a user in Department A
**AND** a task has an assignee from Department A.1 (a sub-department)
**WHEN** I view the task list
**THEN** I can see that task

**Test:** `TaskServiceDepartmentFilteringTest.testGetAllUserTasks_UserInParentDepartment()`

#### AC2.3: Cannot view task from unrelated department ✅
**GIVEN** I am a user in Department A
**AND** a task has only assignees from Department B (unrelated)
**WHEN** I view the task list
**THEN** I do not see that task

**Test:** `TaskServiceDepartmentFilteringTest.testGetAllUserTasks_DifferentDepartment()`

#### AC2.4: Cross-department task ✅
**GIVEN** I am a user in Department A
**AND** a task has assignees from Department A and Department C
**WHEN** I view the task list
**THEN** I can see that task

**Test:** `TaskServiceDepartmentFilteringTest.testGetAllUserTasks_CrossDepartment()`

**⚠️ CONCERNS:**
- **Visibility Direction**: Current implementation only supports downward visibility (managers see subordinate tasks). No upward visibility (staff seeing manager tasks) implemented. Clarify if this is intentional.
- **E2E Tests Missing**: Only unit tests exist; need E2E tests validating full user workflow

---

## Ticket 3: User Visibility of Projects (Derived from Task Assignments)

**User Story:**
As a user
I want to view only projects that have at least one assignee from my department (or reporting departments)
So that I can stay updated on my team's work but not see unrelated projects.

**Implementation Status:** ✅ IMPLEMENTED
**Files:**
- Backend: [ProjectServiceImpl.java](backend/spmorangle/src/main/java/com/spmorangle/crm/projectmanagement/service/impl/ProjectServiceImpl.java:117-173)
- Frontend: [projects-list.tsx](frontend/components/projects-list.tsx)

### Acceptance Criteria

#### AC3.1: Project visibility derived from tasks ✅
**GIVEN** I am a user in Department A
**AND** Project X contains tasks where at least one task has myself as an assignee
**WHEN** I view the project list
**THEN** I can see Project X under "my projects"

**Implementation:** `getUserProjects()` with `isRelated = false` filters projects where user is owner/collaborator

#### AC3.2: Grouping of related projects (view only) ✅
**GIVEN** I am a user in Department A
**AND** Project X contains tasks where at least one task has an assignee from my department (or visible departments)
**WHEN** I view the project list
**THEN** I can see Project X under "related projects"
**AND** I only see the tasks under Project X that has an assignee from my department

**Implementation:** `getUserProjects()` with `isRelated = true` (ALL users with departments) + frontend shows "View only" badge

**✅ UPDATED (2025-11-05):** Changed from MANAGER-only to ALL users - anyone can now see related projects where colleagues from their visible departments are working. This provides team transparency for all roles.

**Task Filtering:** Related project tasks are filtered by department visibility via `TaskServiceImpl.getProjectTasks()` when `isRelatedProject = true`

**⚠️ CONCERNS:**
- **E2E Tests Missing**: No tests validating project visibility scenarios for all user roles

---

## Ticket 4: Tag Tasks (Multi-Tag Categorization)

**User Story:**
As a manager
I want to tag tasks with multiple tags
So that I can categorise and filter them

**Implementation Status:** ⚠️ PARTIALLY IMPLEMENTED
**Files:**
- Backend: [Tag.java](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/model/Tag.java), [TagServiceImpl.java](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/service/impl/TagServiceImpl.java)
- Frontend: [tasks/page.tsx](frontend/app/(app)/tasks/page.tsx)
- Migration: [V202510051200__migrate_tags_to_entity.sql](backend/database/migrations/V202510051200__migrate_tags_to_entity.sql)

### Acceptance Criteria

#### AC4.1: Assigning Tags ✅
**GIVEN** I am creating or editing a task
**WHEN** I add tags
**THEN** a task can have zero or more tags assigned

**Implementation:** Many-to-many relationship via `task_tag` junction table

#### AC4.2: Custom vs. Predefined Tags ✅
**GIVEN** I am adding tags
**WHEN** I type in a new value
**THEN** I can choose from predefined tags or create a custom tag

**Implementation:** `POST /api/tag` creates new tags; frontend allows tag selection and creation

#### AC4.3: Inactive or Removed Tags ❌ NOT IMPLEMENTED
**GIVEN** a tag is removed or marked inactive
**WHEN** I view tasks previously associated with it
**THEN** those tasks remain unchanged
**AND** the tag appears as inactive/disabled in the UI

**Missing:** No "inactive" flag in Tag model; no soft-delete functionality; no UI handling for inactive tags

#### AC4.4: Tag Permissions ❌ NOT IMPLEMENTED
**GIVEN** tags are centrally managed
**WHEN** a user creates or deletes a tag
**THEN** only Managers can delete or globally deactivate tags

**Partial:** Tag creation is MANAGER-only (`@PreAuthorize("hasRole('MANAGER')")`), but deletion endpoint doesn't exist

#### AC4.5: Tag View ✅
**GIVEN** tasks are tagged
**WHEN** a user filters by tasks in the task page
**THEN** user will be able to see all tasks tagged under the tag in the kanban view and the detailed task view

**Implementation:** Tag filtering and tag grouping view in `tasks/page.tsx`

**⚠️ CONCERNS:**
- **Tag Deletion Missing**: No DELETE endpoint for tags; no soft-delete or "inactive" functionality
- **Tag Permissions Incomplete**: Creation is manager-only, but who can assign existing tags to tasks? Should staff be able to use existing tags?
- **E2E Tests Missing**: No tests for tag lifecycle

---

## Ticket 5: Group Tasks by Projects, Teams or Tags

**User Story:**
As a user
I want to view my tasks grouped by project, department, or tags
So that I can organise and manage my workload efficiently

**Implementation Status:** ✅ IMPLEMENTED
**Files:**
- Frontend: [tasks/page.tsx](frontend/app/(app)/tasks/page.tsx:1026-1087)

### Acceptance Criteria

#### AC5.1: Group View Toggle ✅
**GIVEN** I am viewing my Projects page
**WHEN** I change the grouping option
**THEN** I can toggle between:
- Project view
- Team/Department view
- *(Tag view - bonus)*

**Implementation:** Dropdown with 4 options: Status board, Project view, Team/Department view, Tag view

**Note:** AC mentions "Projects page" but implementation is on "Tasks page" - clarify location

#### AC5.2: Group Headers with Counts ✅
**GIVEN** tasks are displayed in grouped view
**WHEN** I view a grouping (e.g., by project or tag)
**THEN** each group header shows the number of tasks under it

**Implementation:** All grouping sections show task counts in headers

#### AC5.3: Multi-Group Display ✅
**GIVEN** a task belongs to one project and multiple tags
**WHEN** I view tasks grouped by tags
**THEN** the task appears under each tag grouping
**AND** when I view by project
**THEN** it appears under only one project grouping

**Implementation:** Tag grouping shows tasks under each tag they belong to; project grouping shows each task once

#### AC5.4: Permissions and Visibility ✅
**GIVEN** I am viewing grouped tasks
**WHEN** I expand groups
**THEN** I only see tasks and projects I have permission to view

**Implementation:** Department-based filtering already applied via `getAllUserTasks()` before grouping

**⚠️ CONCERNS:**
- **Location Mismatch**: AC says "Projects page" but implementation is on "Tasks page" - which is correct?
- **E2E Tests Missing**: No tests validating grouping behavior

---

## Ticket 6: Project Grouping Creation (Manager-Only Permissions)

**User Story:**
As a Manager
I want to be the only role allowed to create or manage projects in the system
So that team structures and groupings (projects, departments, tags) are centrally controlled

**Implementation Status:** ⚠️ PARTIALLY IMPLEMENTED
**Files:**
- Backend: [ProjectController.java](backend/spmorangle/src/main/java/com/spmorangle/crm/projectmanagement/controller/ProjectController.java)
- Frontend: [create-project-modal.tsx](frontend/components/create-project-modal.tsx)

### Acceptance Criteria

#### AC6.1: Project Creation Permissions ⚠️ PARTIAL
**GIVEN** a user is creating a project
**WHEN** they attempt to access the Create Project option
**THEN** only users with the Manager role can create new projects
**AND** Staff cannot see or access the option

**Implementation:**
- Backend: `POST /api/projects` has `@PreAuthorize("hasRole('MANAGER')")`
- Frontend: Create button visible to all users (backend enforces permission with 403)

**Gap:** Frontend shows create button to all users instead of hiding it from staff

#### AC6.2: Project Metadata Required ✅
**GIVEN** I am creating a new project as a Manager
**WHEN** I fill out the form
**THEN** I must provide:
- Project name (required)
- Department assignment (required)

**Implementation:** `CreateProjectDto` requires name and departmentId; auto-adds department members

#### AC6.3: Project Management Permissions ❌ NOT IMPLEMENTED
**GIVEN** a project exists
**WHEN** a user tries to update project settings (e.g., name, description, department, tags, collaborators)
**THEN** only Managers can edit or manage project details

**Missing:**
- `PUT /api/projects/{id}` endpoint is commented out in controller
- No edit functionality in frontend
- No tests for update permissions

#### AC6.4: Unauthorized Access Attempt ✅
**GIVEN** a non-Manager tries to create or manage a project (via UI or API)
**WHEN** the system processes the request
**THEN** it rejects the action and displays an "Insufficient permissions" error

**Implementation:** Spring Security returns 403 Forbidden

**⚠️ CONCERNS:**
- **Project Editing Missing**: No update endpoint or UI for editing existing projects
- **Frontend Permission Check**: Create button should be hidden from staff users based on role
- **E2E Tests Missing**: No tests validating permission enforcement

---

## Summary of Implementation Gaps

### 🔴 Critical Missing Features
1. **Tag Deletion/Deactivation**: No soft-delete or inactive state for tags (AC4.3, AC4.4)
2. **Project Update Functionality**: Cannot edit existing projects despite AC requirement (AC6.3)
3. **Department Management UI**: No frontend interface for HR to create/manage departments (AC1)

### 🟡 Partial Implementations
1. **Tag Permissions**: Creation is manager-only, but deletion doesn't exist (AC4.4)
2. **Frontend Permission Checks**: Create project button visible to all users instead of manager-only (AC6.1)

### 🟠 Missing Tests
1. **E2E Tests**: No end-to-end test implementations despite documentation existing
2. **Integration Tests**: Limited to unit tests; need full workflow validation
3. **Permission Tests**: No E2E tests validating role-based access control

### ❓ Clarifications Needed
1. **Visibility Direction**: ✅ CLARIFIED - Downward visibility only (managers see subordinate tasks, child depts cannot see parent tasks). Within same department, all users see each other's tasks.
2. **Related Projects Access**: ✅ RESOLVED - Changed to ALL users (previously manager-only)
3. **Role Naming**: "Admin" in ticket vs "HR" in implementation for department management
4. **Page Location**: AC5 mentions "Projects page" but implementation is on "Tasks page"
5. **Tag Assignment Permissions**: Who can assign existing tags to tasks - only managers or all users?

---

## E2E Test Checklist (To Be Implemented)

### Ticket 1: Department Hierarchy
- [ ] HR can create department with parent
- [ ] Multi-level hierarchy visibility works
- [ ] Non-HR cannot access department management

### Ticket 2: Task Visibility
- [ ] User sees tasks from own department
- [ ] User sees tasks from sub-departments
- [ ] User cannot see tasks from unrelated departments
- [ ] User sees cross-department tasks if involved

### Ticket 3: Project Visibility
- [ ] User sees "My Projects" when assigned to tasks
- [ ] ALL users (not just managers) see "Related Projects" from visible departments
- [ ] Staff user sees related projects where colleagues are working
- [ ] Related projects show "View only" badge
- [ ] User does not see unrelated projects

### Ticket 4: Tags
- [ ] Manager can create tags
- [ ] Staff cannot create tags
- [ ] All users can assign existing tags to tasks
- [ ] Tag filtering works in all views
- [ ] ~~Inactive tags appear disabled~~ (not implemented)

### Ticket 5: Grouping Views
- [ ] Toggle between status/project/department/tag views
- [ ] Group headers show correct task counts
- [ ] Tasks appear in multiple tag groups when tagged with multiple tags
- [ ] Department filtering applies across all grouping views

### Ticket 6: Project Creation
- [ ] Manager can create projects
- [ ] Staff cannot create projects (UI and API)
- [ ] Required fields enforced
- [ ] 403 error shown for unauthorized attempts
- [ ] ~~Manager can edit projects~~ (not implemented)




