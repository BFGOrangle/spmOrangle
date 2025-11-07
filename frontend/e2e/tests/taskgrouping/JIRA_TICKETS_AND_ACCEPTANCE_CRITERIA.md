# Epic: Task Grouping and Organisation

**Epic Status:** ✅ **COMPLETE** (6/6 tickets fully implemented)

This document tracks acceptance criteria for the Task Grouping and Organisation epic and implementation status.

---

## 📊 Implementation Summary

| Ticket | Status | ACs Met | Outstanding Issues |
|--------|--------|---------|-------------------|
| Ticket 1: Department Hierarchy | ✅ Complete | 2/2 | E2E Tests Missing |
| Ticket 2: Task Visibility | ✅ Complete | 4/4 | E2E Tests Missing |
| Ticket 3: Project Visibility | ✅ Complete | 2/2 | E2E Tests Missing |
| Ticket 4: Tag Management | ✅ Complete | 5/5 | E2E Tests Missing (implemented 2025-11-05) |
| Ticket 5: Task Grouping | ✅ Complete | 4/4 | E2E Tests Missing |
| Ticket 6: Project Permissions | ✅ Complete | 3/3 | E2E Tests Missing |

---

## Ticket 1: Department Hierarchy Support

**User Story:**
As a system, I need to support department reporting hierarchies so that task visibility can be derived based on organizational structure.

**Status:** ✅ **FULLY IMPLEMENTED**

**Note:** Departments are pre-loaded via database migrations. This ticket demonstrates the functionality of hierarchical department relationships and their impact on task visibility.

### Implementation Files
- Backend: [DepartmentMgmtController.java](backend/spmorangle/src/main/java/com/spmorangle/crm/departmentmgmt/controller/DepartmentMgmtController.java)
- Model: [Department.java](backend/spmorangle/src/main/java/com/spmorangle/crm/departmentmgmt/model/Department.java)
- Service: [DepartmentalVisibilityServiceImpl.java](backend/spmorangle/src/main/java/com/spmorangle/crm/departmentmgmt/service/impl/DepartmentalVisibilityServiceImpl.java)
- Migration: [V202510301800__create_departments_table.sql](backend/database/migrations/V202510301800__create_departments_table.sql)
- Mock Data: [V202511041922__insert_mock_departments.sql](backend/database/migrations/V202511041922__insert_mock_departments.sql)

### Acceptance Criteria

#### ✅ AC1.1: Department Parent-Child Relationships
```gherkin
GIVEN departments are configured with parent-child relationships
WHEN the system loads department hierarchy
THEN child departments correctly reference their parent departments
```
**Implementation:**
- Backend API: `POST /api/departments` with `parentId` field (HR-only via `@PreAuthorize("hasRole('HR')")`)
- Database: Self-referencing `parent_id` foreign key in departments table

#### ✅ AC1.2: Multi-Level Hierarchy Visibility
```gherkin
GIVEN Department C reports to Department B
AND Department B reports to Department A
WHEN a task is assigned to Department C staff
THEN users in Departments A and B can both see the task (downward visibility)
```
**Implementation:** `DepartmentalVisibilityServiceImpl.visibleDepartmentsForAssignedDept()` recursively traverses child departments

**Visibility Rules:**
- ✅ **Same Department:** All users see each other's tasks (STAFF sees MANAGER tasks, vice versa)
- ✅ **Parent → Child:** Parent department users see child department tasks (downward only)
- ❌ **Child → Parent:** Child department users CANNOT see parent department tasks (no upward visibility)

---

## Ticket 2: Task Visibility (Department-Based Filtering)

**User Story:**
As a user, I want to view only tasks from my department or reporting departments so that I can stay updated on my team's work without seeing unrelated tasks.

**Status:** ✅ **FULLY IMPLEMENTED**

### Implementation Files
- Backend: [TaskServiceImpl.java](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/service/impl/TaskServiceImpl.java#L271-295)
- Tests: [TaskServiceDepartmentFilteringTest.java](backend/spmorangle/src/test/java/com/spmorangle/crm/taskmanagement/service/impl/TaskServiceDepartmentFilteringTest.java)

### Acceptance Criteria

#### ✅ AC2.1: View Tasks Within Same Department
```gherkin
GIVEN I am in Department A
AND a task has at least one assignee from Department A
WHEN I view the task list
THEN I can see that task
```
**Unit Test:** `testGetAllUserTasks_SameInDepartment()`

#### ✅ AC2.2: View Tasks from Sub-Departments
```gherkin
GIVEN I am in Department A (parent)
AND a task has assignee from Department A.1 (child)
WHEN I view the task list
THEN I can see that task (downward visibility)
```
**Unit Test:** `testGetAllUserTasks_UserInParentDepartment()`

#### ✅ AC2.3: Cannot View Tasks from Unrelated Departments
```gherkin
GIVEN I am in Department A
AND a task has only assignees from Department B (unrelated)
WHEN I view the task list
THEN I do NOT see that task
```
**Unit Test:** `testGetAllUserTasks_DifferentDepartment()`

#### ✅ AC2.4: View Cross-Department Tasks
```gherkin
GIVEN I am in Department A
AND a task has assignees from both Department A and Department C
WHEN I view the task list
THEN I can see that task (at least one assignee is visible)
```
**Unit Test:** `testGetAllUserTasks_CrossDepartment()`

---

## Ticket 3: Project Visibility (Derived from Task Assignments)

**User Story:**
As a user, I want to view only projects with assignees from my visible departments so that I can stay updated on my team's work.

**Status:** ✅ **FULLY IMPLEMENTED** | 🔄 **UPDATED 2025-11-05**

### Implementation Files
- Backend: [ProjectServiceImpl.java](backend/spmorangle/src/main/java/com/spmorangle/crm/projectmanagement/service/impl/ProjectServiceImpl.java#L56-153)
- Frontend: [projects-list.tsx](frontend/components/projects-list.tsx)
- Tests: [ProjectServiceImplTest.java](backend/spmorangle/src/test/java/com/spmorangle/crm/projectmanagement/service/impl/ProjectServiceImplTest.java)

### Acceptance Criteria

#### ✅ AC3.1: My Projects (Direct Membership)
```gherkin
GIVEN I am in Department A
AND Project X has me as owner or collaborator
WHEN I view the project list
THEN I see Project X under "My Projects"
```
**Implementation:** `getUserProjects()` with `isRelated = false`

#### ✅ AC3.2: Related Projects (Colleague Awareness) 🆕
```gherkin
GIVEN I am in Department A
AND Project X has tasks assigned to colleagues from my visible departments
AND I am NOT a direct member of Project X
WHEN I view the project list
THEN I see Project X under "Related Projects" with "View only" badge
AND I only see tasks assigned to colleagues from my visible departments
```
**Implementation:** `getUserProjects()` with `isRelated = true` (**ALL users with departments**)

**🔄 Recent Change (2025-11-05):**
- **Before:** Only MANAGER role could see related projects
- **After:** ALL users (DIRECTOR, MANAGER, HR, STAFF) can see related projects
- **Reason:** Team transparency - all team members should see what colleagues are working on

**Task Filtering:** Related project tasks filtered by `TaskServiceImpl.getProjectTasks()` when `isRelatedProject = true`

---

## Ticket 4: Tag Management (Multi-Tag Categorization)

**User Story:**
As a manager, I want to tag tasks with multiple tags so that I can categorize and filter them.

**Status:** ✅ **FULLY IMPLEMENTED** (5/5 ACs met) | 🔄 **UPDATED 2025-11-05**

### Implementation Files
- Backend: [Tag.java](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/model/Tag.java), [TagServiceImpl.java](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/service/impl/TagServiceImpl.java), [TagController.java](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/controller/TagController.java)
- Frontend: [tasks/page.tsx](frontend/app/(app)/tasks/page.tsx)
- Migration: [V202510051200__migrate_tags_to_entity.sql](backend/database/migrations/V202510051200__migrate_tags_to_entity.sql)

### Acceptance Criteria

#### ✅ AC4.1: Assign Multiple Tags to Tasks
```gherkin
GIVEN I am creating or editing a task
WHEN I add tags
THEN a task can have zero or more tags assigned
```
**Implementation:** Many-to-many via `task_tag` junction table

#### ✅ AC4.2: Create or Select Existing Tags
```gherkin
GIVEN I am adding tags to a task
WHEN I type a new value
THEN I can choose from existing tags or create a new tag
```
**Implementation:** `POST /api/tag` (MANAGER-only for creation); frontend tag selector supports both

#### ✅ AC4.3: Inactive/Removed Tags (Soft-Delete)
```gherkin
GIVEN a tag is soft-deleted (delete_ind = true)
WHEN I view tasks with that tag
THEN tasks remain unchanged with existing tags
AND the deleted tag does NOT appear in tag suggestions for new tasks
```
**Status:** ✅ **IMPLEMENTED** (2025-11-05)
- ✅ `delete_ind` boolean flag in Tag model at [Tag.java:21](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/model/Tag.java#L21)
- ✅ Soft-delete functionality via `DELETE /api/tag/{id}` at [TagController.java:40-47](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/controller/TagController.java#L40-L47)
- ✅ Tag reactivation on recreation at [TagServiceImpl.java:41-54](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/service/impl/TagServiceImpl.java#L41-L54)
- ✅ Frontend filters deleted tags from suggestions in 3 components:
  - [task-creation-dialog.tsx:228-230](frontend/components/task-creation-dialog.tsx#L228-L230)
  - [task-update-dialog.tsx:272-274](frontend/components/task-update-dialog.tsx#L272-L274)
  - [tasks/page.tsx:858-860](frontend/app/(app)/tasks/page.tsx#L858-L860)
- ✅ Migration: [V202511051400__add_tag_delete_ind.sql](backend/database/migrations/V202511051400__add_tag_delete_ind.sql)

#### ✅ AC4.4: Tag Permissions
```gherkin
GIVEN tags are centrally managed
WHEN a user creates or deletes a tag
THEN only MANAGER can create or delete tags
AND all users can assign existing tags to tasks
```
**Status:** ✅ **FULLY IMPLEMENTED** (2025-11-05)
- ✅ Tag creation: MANAGER-only (`@PreAuthorize("hasRole('MANAGER')")` on `POST /api/tag` at [TagController.java:23](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/controller/TagController.java#L23))
- ✅ Tag deletion: MANAGER-only (`@PreAuthorize("hasRole('MANAGER')")` on `DELETE /api/tag/{id}` at [TagController.java:40](backend/spmorangle/src/main/java/com/spmorangle/crm/taskmanagement/controller/TagController.java#L40))
- ✅ Tag assignment: All users can assign existing tags when creating/editing tasks
- ✅ API returns 403 Forbidden for unauthorized users attempting tag creation/deletion

#### ✅ AC4.5: Tag Filtering and Viewing
```gherkin
GIVEN tasks are tagged
WHEN I filter by tags on the tasks page
THEN I see tasks in kanban view and detailed view
```
**Implementation:** Tag filtering + tag grouping view in `tasks/page.tsx`

---

## Ticket 5: Task Grouping Views

**User Story:**
As a user, I want to view my tasks grouped by project, department, or tags so that I can organize and manage my workload efficiently.

**Status:** ✅ **FULLY IMPLEMENTED**

### Implementation Files
- Frontend: [tasks/page.tsx](frontend/app/(app)/tasks/page.tsx#L1026-1087)

### Acceptance Criteria

#### ✅ AC5.1: Toggle Between Grouping Views
```gherkin
GIVEN I am on the Tasks page
WHEN I change the grouping option
THEN I can toggle between:
  - Status board (default kanban)
  - Project view
  - Team/Department view
  - Tag view
```
**Implementation:** Dropdown selector with 4 grouping options

#### ✅ AC5.2: Group Headers Show Task Counts
```gherkin
GIVEN tasks are in grouped view
WHEN I view any grouping
THEN each group header shows the task count
```
**Implementation:** All group headers display `(X tasks)`

#### ✅ AC5.3: Multi-Tag Task Display
```gherkin
GIVEN a task has multiple tags and one project
WHEN viewing tag grouping
THEN task appears under each tag
WHEN viewing project grouping
THEN task appears under only one project
```
**Implementation:** Tag grouping duplicates tasks across tags; project grouping shows each task once

#### ✅ AC5.4: Respect Permissions in Grouped Views
```gherkin
GIVEN I am viewing grouped tasks
WHEN I expand groups
THEN I only see tasks I have permission to view
```
**Implementation:** Department filtering via `getAllUserTasks()` applied before grouping

---

## Ticket 6: Project Creation Permissions (Manager-Only)

**User Story:**
As a MANAGER, I want to be the only role allowed to create projects so that project structures are centrally controlled.

**Status:** ✅ **FULLY IMPLEMENTED** (3/3 ACs met)

### Implementation Files
- Backend: [ProjectController.java](backend/spmorangle/src/main/java/com/spmorangle/crm/projectmanagement/controller/ProjectController.java#L52)
- Frontend: [projects-list.tsx](frontend/components/projects-list.tsx#L127-129,223-228,246-251)
- Frontend: [create-project-modal.tsx](frontend/components/create-project-modal.tsx)

### Acceptance Criteria

#### ✅ AC6.1: Project Creation Permissions
```gherkin
GIVEN a user attempts to create a project
WHEN they access the Create Project option
THEN only MANAGER can create projects
AND non-MANAGER users cannot see the option
```
**Status:** ✅ **COMPLETE** (2025-11-05)
- ✅ Backend: `POST /api/projects` has `@PreAuthorize("hasRole('MANAGER')")` at [ProjectController.java:52](backend/spmorangle/src/main/java/com/spmorangle/crm/projectmanagement/controller/ProjectController.java#L52)
- ✅ Frontend: "New Project" button hidden for non-MANAGER at [projects-list.tsx:223-228](frontend/components/projects-list.tsx#L223-L228)
- ✅ Frontend: "Create Project" button in empty state hidden for non-MANAGER at [projects-list.tsx:246-251](frontend/components/projects-list.tsx#L246-L251)
- ✅ Helper function `isManager()` added at [projects-list.tsx:127-129](frontend/components/projects-list.tsx#L127-L129)
- ✅ API returns 403 Forbidden for unauthorized users

#### ✅ AC6.2: Required Project Metadata
```gherkin
GIVEN I am creating a project as MANAGER
WHEN I fill the form
THEN I must provide:
  - Project name (required)
  - Department assignment (required)
```
**Implementation:** `CreateProjectDto` enforces required fields; auto-adds department members

#### ✅ AC6.3: Unauthorized Access Handling
```gherkin
GIVEN a non-MANAGER tries to create a project
WHEN the system processes the request
THEN it returns 403 Forbidden with error message
```
**Implementation:** Spring Security returns 403; frontend shows error

### ℹ️ Scope Clarification
**Project Editing:** Out of scope for this ticket - projects are immutable after creation

---

## ℹ️ Implementation Notes

### Key Design Decisions

1. **Visibility Direction:** Downward only (parent sees child, child cannot see parent). Same department = bidirectional.
2. **Related Projects Access:** ALL users with departments can see related projects (changed from MANAGER-only on 2025-11-05)
3. **Role Naming:** "HR" role (not "admin")
4. **Page Location:** Task grouping on "Tasks page" (not Projects page)
5. **Tag Management:** All users assign existing tags; only MANAGER creates/deletes new tags
6. **Project Editing:** Out of scope - projects are immutable after creation
7. **Department Management:** Departments are pre-loaded via migrations; no UI needed

### Recent Changes (2025-11-05)

- ✅ **Ticket 4:** Implemented tag soft-delete with `delete_ind` flag, MANAGER-only deletion, tag reactivation
- ✅ **Ticket 6:** Added frontend permission checks to hide Create Project button from non-MANAGER users
- ✅ **Ticket 3:** Changed related projects from MANAGER-only to ALL users with departments

---

**Last Updated:** 2025-11-05
**Epic Completion:** 100% (6/6 tickets fully implemented)
**E2E Test Coverage:** 0% (test cases to be documented separately)
