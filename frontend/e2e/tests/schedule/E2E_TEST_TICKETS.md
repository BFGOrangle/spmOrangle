# E2E Test Cases - Schedule & Task Management Feature
## Test Coverage Breakdown by Tickets

**Total Tests:** TBD
**Test Files:** 4
**Last Updated:** November 6, 2025

---

## 📋 TICKET 1: Overdue Alerts

**Epic:** Task Management - Alerts & Notifications  
**Priority:** P0 (Critical)  
**Test File:** `task-overdue-alerts.spec.ts`

### User Story
As a staff user, I want to be alerted when a task is overdue so that I know which tasks require higher priority and immediate attention.

---

## TC-001-001
**Test Scenario:** System marks task as overdue when due date passes

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists in the system
- Task status is not "Done" or "Completed"

**Test Details:**
- **Test Steps:**
  1. Create a task with a due date in the past (e.g., yesterday)
  2. Set task status to "In Progress" or "To Do" (not completed)
  3. Navigate to task list view
  4. Verify task has "Overdue" status indicator
  5. Verify task metadata shows it is overdue

- **Test Data:**
  - Task Title: "E2E Test - Overdue Task"
  - Due Date: Yesterday's date
  - Status: "In Progress" or "To Do"

- **Expected Result:**
  - Task is marked as "Overdue"
  - Overdue status indicator is visible
  - Task metadata correctly reflects overdue state

**Test Results:** ⏳ Pending

---

## TC-001-002
**Test Scenario:** Overdue task highlighted in red in list view

**Pre Conditions:**
- User logged in as: Staff
- Overdue task exists (due date in past, status not "Done")
- User has navigated to task list view

**Test Details:**
- **Test Steps:**
  1. Create an overdue task (due date in past, status not "Done")
  2. Navigate to task list view
  3. Verify task row has red background or red border
  4. Verify overdue visual indicator is present (icon, badge, or color)

- **Test Data:**
  - Task Title: "E2E Test - Overdue Visual"
  - Due Date: 1 day in the past
  - Status: "In Progress"

- **Expected Result:**
  - Task row displays with red background or border
  - Overdue icon/badge is visible
  - Visual indicator clearly distinguishes overdue task from others

**Test Results:** ⏳ Pending

---

## TC-001-003
**Test Scenario:** Overdue task highlighted in board view

**Pre Conditions:**
- User logged in as: Staff
- Overdue task exists
- User has navigated to board view (Kanban)

**Test Details:**
- **Test Steps:**
  1. Create an overdue task
  2. Navigate to board view (Kanban)
  3. Locate the overdue task card
  4. Verify card has red border or red background
  5. Verify overdue badge/icon is visible on card

- **Test Data:**
  - Task Title: "E2E Test - Overdue Board"
  - Due Date: 1 day in the past
  - Status: "In Progress"

- **Expected Result:**
  - Task card has red visual indicator
  - Overdue badge/icon is displayed on card
  - Card stands out visually from non-overdue tasks

**Test Results:** ⏳ Pending

---

## TC-001-004
**Test Scenario:** Overdue task highlighted in calendar view

**Pre Conditions:**
- User logged in as: Staff
- Overdue task exists with due date 2 days ago
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create an overdue task with due date 2 days ago
  2. Navigate to calendar view
  3. Go to the date where the task was due
  4. Verify task appears with red color or red border
  5. Verify overdue indicator is visible on calendar event

- **Test Data:**
  - Task Title: "E2E Test - Overdue Calendar"
  - Due Date: 2 days in the past
  - Status: "To Do"

- **Expected Result:**
  - Task appears in calendar with red color
  - Overdue indicator is visible on calendar event
  - Task is easily identifiable as overdue

**Test Results:** ⏳ Pending

---

## TC-001-005
**Test Scenario:** Overdue status clears when task is marked as done

**Pre Conditions:**
- User logged in as: Staff
- Overdue task exists (due yesterday, status "In Progress")
- Task has overdue indicator visible

**Test Details:**
- **Test Steps:**
  1. Create an overdue task (due yesterday, status "In Progress")
  2. Verify task is marked as overdue (red indicator)
  3. Update task status to "Done"
  4. Verify overdue indicator is removed
  5. Verify task no longer has red highlighting

- **Test Data:**
  - Task Title: "E2E Test - Clear Overdue"
  - Due Date: Yesterday's date
  - Initial Status: "In Progress"
  - Updated Status: "Done"

- **Expected Result:**
  - Overdue indicator is removed
  - Red highlighting is removed
  - Task displays as completed, not overdue

**Test Results:** ⏳ Pending

---

## TC-001-007
**Test Scenario:** Overdue reminder email sent 1 day after due date

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists
- Email service is configured
- Task is 1 day overdue

**Test Details:**
- **Test Steps:**
  1. Create a task with due date set to 2 days ago
  2. Mock or verify email service was called
  3. Verify email contains subject mentioning "Overdue" or "Past Due"
  4. Verify email contains task title
  5. Verify email contains original due date/time and current task status

- **Test Data:**
  - Task Title: "E2E Test - Email Reminder"
  - Due Date: 2 days in the past
  - Status: "In Progress"

- **Expected Result:**
  - Email is sent to assigned user
  - Email subject mentions "Overdue"
  - Email contains all required information
  - Note: Full email testing requires email service integration or mocking

**Test Results:** ⏳ Pending (Integration Test)

---

## TC-001-008
**Test Scenario:** Overdue reminder email contains all required information

**Pre Conditions:**
- User logged in as: Staff
- Overdue reminder email has been triggered
- Email service is configured

**Test Details:**
- **Test Steps:**
  1. Trigger overdue email for a specific task
  2. Capture email content
  3. Verify email contains task title
  4. Verify email contains due date/time formatted correctly
  5. Verify email contains current status
  6. Verify email contains clickable link to task detail page
  7. Click link and verify it navigates to correct task

- **Test Data:**
  - Task Title: "E2E Test - Email Content"
  - Due Date: 1 day in the past
  - Status: "In Progress"

- **Expected Result:**
  - Email includes task title, due date/time, current status
  - Email includes clickable link to task in app
  - Link successfully navigates to task detail page

**Test Results:** ⏳ Pending (Integration Test)

---

## TC-001-011
**Test Scenario:** Completed task not marked as overdue even if past due date

**Pre Conditions:**
- User logged in as: Staff
- Task was completed before or on the due date

**Test Details:**
- **Test Steps:**
  1. Create a task with due date yesterday
  2. Mark task as "Done" before due date passes (simulate completed on time)
  3. Navigate to task list
  4. Verify task does NOT have overdue indicator
  5. Verify task shows "Completed" status, not "Overdue"

- **Test Data:**
  - Task Title: "E2E Test - Completed On Time"
  - Due Date: Yesterday's date
  - Status: "Done"
  - Completion Date: On or before due date

- **Expected Result:**
  - Task does NOT have overdue indicator
  - Task shows "Completed" status
  - No red highlighting or overdue badge

**Test Results:** ⏳ Pending

---

## TC-001-012
**Test Scenario:** Tasks with no due date handled gracefully (no overdue status)

**Pre Conditions:**
- User logged in as: Staff
- Task without due date exists

**Test Details:**
- **Test Steps:**
  1. Create a task without a due date
  2. Navigate to task list, board, and calendar views
  3. Verify task is displayed normally (no overdue indicator)
  4. Verify task does not appear in overdue filter

- **Test Data:**
  - Task Title: "E2E Test - No Due Date"
  - Due Date: None/Null
  - Status: "To Do"

- **Expected Result:**
  - Task is displayed normally without overdue indicator
  - Task does not appear when overdue filter is applied
  - Task is accessible in all views except calendar

**Test Results:** ⏳ Pending

---

## 📅 TICKET 2: Team Schedule Visibility

**Epic:** Calendar & Schedule Management  
**Priority:** P0 (Critical)  
**Test File:** `team-schedule-visibility.spec.ts`

### User Story
As a staff or manager, I want to view a project calendar that shows both my own tasks and my colleagues' tasks (within my visible departments), so that I can understand overall project schedules, track dependencies, and better coordinate work.

---

## TC-002-001
**Test Scenario:** Show only tasks from selected project in calendar

**Pre Conditions:**
- User logged in as: Staff
- User has access to specific departments
- User has navigated to calendar view
- Multiple projects exist with tasks

**Test Details:**
- **Test Steps:**
  1. Log in as staff user with access to specific departments
  2. Navigate to calendar view
  3. Select "Project Tasks" view
  4. Filter by a specific project (e.g., "Project Alpha")
  5. Verify all visible tasks belong to "Project Alpha"
  6. Verify tasks from other projects are not displayed

- **Test Data:**
  - Project Filter: "Project Alpha"
  - User has access to: Department A, Department B
  - Tasks exist in: Project Alpha, Project Beta

- **Expected Result:**
  - Only tasks from "Project Alpha" are displayed
  - Tasks from other projects are hidden
  - Filter accurately restricts task visibility

**Test Results:** ⏳ Pending

---

## TC-002-002
**Test Scenario:** Hide tasks from departments outside visibility scope

**Pre Conditions:**
- User logged in as: Staff
- User has access to Department A only
- Tasks exist in Department B (outside user's scope)
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Log in as staff user with access to Department A only
  2. Create a task in Department B (outside scope)
  3. Navigate to calendar view
  4. Select "Project Tasks" view
  5. Verify task from Department B is not visible
  6. Verify only tasks from Department A are shown

- **Test Data:**
  - User Access: Department A
  - Task Created In: Department B
  - Department B is outside user's visibility scope

- **Expected Result:**
  - Task from Department B is not visible
  - Only tasks from Department A are shown
  - Department visibility rules are enforced

**Test Results:** ⏳ Pending

---

## TC-002-003
**Test Scenario:** Manager can see all department tasks in reporting scope

**Pre Conditions:**
- User logged in as: Manager
- Manager has access to multiple departments
- Tasks exist across multiple departments
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Log in as manager with access to multiple departments
  2. Navigate to calendar view
  3. Select "Project Tasks" view
  4. Verify tasks from all managed departments are visible
  5. Verify task count matches expected tasks across departments

- **Test Data:**
  - Manager Access: Department A, Department B, Department C
  - Tasks created in all three departments

- **Expected Result:**
  - Tasks from all managed departments are visible
  - Task count matches expected total
  - Manager has full visibility within reporting scope

**Test Results:** ⏳ Pending

---

## TC-002-004
**Test Scenario:** Own tasks displayed in different color from colleagues' tasks

**Pre Conditions:**
- User logged in as: Staff
- Tasks assigned to self exist
- Tasks assigned to colleagues exist
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Log in as staff user
  2. Create a task assigned to self
  3. Create a task assigned to colleague in same department
  4. Navigate to calendar view
  5. Select "Project Tasks" view
  6. Verify own task appears in primary color (e.g., blue background)
  7. Verify colleague's task appears in secondary color (e.g., gray or purple)
  8. Verify colors are visually distinct

- **Test Data:**
  - Task 1: Assigned to self
  - Task 2: Assigned to colleague in same department
  - Both tasks have due dates

- **Expected Result:**
  - Own task appears in primary color (blue)
  - Colleague's task appears in secondary color (gray/purple)
  - Colors are visually distinct and easy to differentiate

**Test Results:** ⏳ Pending

---

## TC-002-005
**Test Scenario:** Color consistency maintained across calendar dates and views

**Pre Conditions:**
- User logged in as: Staff
- Multiple tasks exist (some assigned to self, some to colleagues)
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create multiple tasks: some assigned to self, some to colleagues
  2. Navigate to calendar view
  3. Switch between Day, Week, and Month views
  4. Verify own tasks always use same color (e.g., blue)
  5. Verify colleagues' tasks always use secondary color

- **Test Data:**
  - 3 tasks assigned to self
  - 3 tasks assigned to colleagues
  - Tasks spread across different dates

- **Expected Result:**
  - Own tasks consistently use primary color across all views
  - Colleagues' tasks consistently use secondary color across all views
  - Color coding is maintained when switching views

**Test Results:** ⏳ Pending

---

## TC-002-006
**Test Scenario:** Task details displayed when clicking task in calendar

**Pre Conditions:**
- User logged in as: Staff
- Task with complete details exists
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create a task with all details filled
  2. Navigate to calendar view
  3. Click on the task in calendar
  4. Verify modal or detail panel opens
  5. Verify task details are displayed: task name, due date and time, assignee name, department name

- **Test Data:**
  - Task Name: "E2E Test - Full Details"
  - Due Date: Tomorrow at 2:00 PM
  - Assignee: Current user
  - Department: Engineering

- **Expected Result:**
  - Modal or detail panel opens when task is clicked
  - All task details are visible and correctly formatted
  - Task name, due date/time, assignee name, and department are displayed

**Test Results:** ⏳ Pending

---

## TC-002-007
**Test Scenario:** Filter calendar tasks by project

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist for multiple projects
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks for multiple projects
  2. Navigate to calendar view
  3. Apply project filter (select "Project A")
  4. Verify only tasks from "Project A" are shown
  5. Verify task count updates correctly

- **Test Data:**
  - Tasks in Project A: 3 tasks
  - Tasks in Project B: 2 tasks
  - Filter Applied: "Project A"

- **Expected Result:**
  - Only tasks from "Project A" are displayed (3 tasks)
  - Task count shows "3"
  - Tasks from other projects are hidden

**Test Results:** ⏳ Pending

---

## TC-002-008
**Test Scenario:** Filter calendar tasks by department

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist in multiple departments
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks in multiple departments
  2. Navigate to calendar view
  3. Apply department filter (select "Engineering")
  4. Verify only tasks from "Engineering" are shown
  5. Verify tasks from other departments are hidden

- **Test Data:**
  - Tasks in Engineering: 4 tasks
  - Tasks in Marketing: 2 tasks
  - Filter Applied: "Engineering"

- **Expected Result:**
  - Only tasks from "Engineering" are displayed (4 tasks)
  - Tasks from other departments are hidden
  - Filter accurately restricts visibility

**Test Results:** ⏳ Pending

---

## TC-002-009
**Test Scenario:** Filter calendar tasks by date range

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist with various due dates
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks with various due dates (this week, next week, next month)
  2. Navigate to calendar view
  3. Set date range filter (e.g., "This Week")
  4. Verify only tasks within this week are displayed
  5. Change date range to "Next Month"
  6. Verify calendar updates to show next month's tasks

- **Test Data:**
  - Tasks this week: 3 tasks
  - Tasks next week: 2 tasks
  - Tasks next month: 4 tasks

- **Expected Result:**
  - Date range filter correctly restricts task visibility
  - Switching date ranges updates calendar display
  - Only tasks within selected date range are shown

**Test Results:** ⏳ Pending

---

## TC-002-010
**Test Scenario:** Apply multiple filters simultaneously

**Pre Conditions:**
- User logged in as: Staff
- Diverse set of tasks exist (multiple projects, departments, dates)
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create diverse set of tasks (multiple projects, departments, dates)
  2. Navigate to calendar view
  3. Apply multiple filters: Project A + Engineering Department + This Week
  4. Verify only tasks matching ALL filters are shown
  5. Verify filter count is accurate

- **Test Data:**
  - Project A, Engineering, This Week: 2 tasks
  - Project A, Engineering, Next Week: 1 task
  - Project B, Engineering, This Week: 1 task

- **Expected Result:**
  - Only tasks matching ALL filters are displayed (2 tasks)
  - Multiple filters work in conjunction (AND logic)
  - Filter count is accurate

**Test Results:** ⏳ Pending

---

## TC-002-011
**Test Scenario:** Manager can edit tasks in managed department

**Pre Conditions:**
- User logged in as: Manager
- Manager of "Engineering" department
- Task exists in "Engineering" department assigned to team member
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Log in as manager of "Engineering" department
  2. Create a task in "Engineering" department assigned to team member
  3. Navigate to calendar view
  4. Click on the task
  5. Verify edit button is visible
  6. Click edit button
  7. Update task title
  8. Save changes
  9. Verify task is updated successfully

- **Test Data:**
  - Manager Role: Engineering Manager
  - Task Department: Engineering
  - Original Title: "E2E Test - Before Edit"
  - Updated Title: "E2E Test - After Edit"

- **Expected Result:**
  - Edit button is visible
  - Manager can successfully edit task
  - Changes are saved and reflected
  - Manager has full CRUD access

**Test Results:** ⏳ Pending

---

## TC-002-012
**Test Scenario:** Staff can edit own tasks

**Pre Conditions:**
- User logged in as: Staff
- Task assigned to self exists
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Log in as staff user
  2. Create a task assigned to self
  3. Navigate to calendar view
  4. Click on own task
  5. Verify edit button is visible
  6. Update task due date
  7. Save changes
  8. Verify changes are reflected in calendar

- **Test Data:**
  - Task Assignee: Self
  - Original Due Date: Tomorrow
  - Updated Due Date: Next week

- **Expected Result:**
  - Edit button is visible for own task
  - Staff can successfully edit own task
  - Changes are saved and reflected in calendar
  - Due date update is visible

**Test Results:** ⏳ Pending

---

## TC-002-013
**Test Scenario:** View-only access to colleague tasks without edit permissions

**Pre Conditions:**
- User logged in as: Staff
- Task assigned to colleague exists in same department
- User has navigated to calendar view
- User does not have edit permissions for colleague's task

**Test Details:**
- **Test Steps:**
  1. Log in as staff user
  2. Create a task assigned to colleague in same department
  3. Navigate to calendar view
  4. Click on colleague's task
  5. Verify task details are visible
  6. Verify edit button is NOT visible or is disabled
  7. Verify cannot modify task fields

- **Test Data:**
  - Task Assignee: Colleague in same department
  - User Role: Staff (no edit permissions for others' tasks)

- **Expected Result:**
  - Task details are visible
  - Edit button is not visible or is disabled
  - Input fields are read-only
  - User cannot modify colleague's task

**Test Results:** ⏳ Pending

---

## TC-002-014
**Test Scenario:** Read-only indicator displayed for non-editable tasks

**Pre Conditions:**
- User logged in as: Staff
- Colleague tasks are visible in calendar
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Log in as staff user
  2. Navigate to calendar with colleague tasks visible
  3. Click on colleague's task
  4. Verify "View Only" or similar indicator is displayed
  5. Verify all input fields are disabled or read-only

- **Test Data:**
  - Task Assignee: Colleague
  - Expected Indicator: "View Only" label or icon

- **Expected Result:**
  - "View Only" indicator is displayed
  - All input fields are disabled or read-only
  - Clear visual indication of read-only state

**Test Results:** ⏳ Pending

---

## TC-002-015
**Test Scenario:** Switch between My Tasks and Project Tasks views

**Pre Conditions:**
- User logged in as: Staff
- Tasks assigned to self exist (2 tasks)
- Tasks assigned to colleagues exist (3 tasks)
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Log in as staff user
  2. Create tasks: 2 assigned to self, 3 assigned to colleagues
  3. Navigate to calendar view
  4. Select "My Tasks" view
  5. Verify only own tasks (2 tasks) are displayed
  6. Switch to "Project Tasks" view
  7. Verify all visible tasks (5 tasks) are displayed with color coding

- **Test Data:**
  - Own Tasks: 2
  - Colleague Tasks: 3
  - Total Visible: 5

- **Expected Result:**
  - "My Tasks" view shows only own tasks (2)
  - "Project Tasks" view shows all visible tasks (5)
  - Color coding applied in Project Tasks view
  - View switching works correctly

**Test Results:** ⏳ Pending

---

## TC-002-016
**Test Scenario:** Filters maintained when switching between task views

**Pre Conditions:**
- User logged in as: Staff
- Project filter is applied
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Navigate to calendar view
  2. Apply project filter (Project A)
  3. Switch from "My Tasks" to "Project Tasks"
  4. Verify project filter is still applied
  5. Verify task list updates but filter remains active

- **Test Data:**
  - Project Filter: "Project A"
  - Tasks in Project A: Multiple tasks

- **Expected Result:**
  - Project filter remains active when switching views
  - Task list updates appropriately
  - Filter state is preserved

**Test Results:** ⏳ Pending

---

## TC-002-017
**Test Scenario:** Correct color coding displayed in Project Tasks view

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist (some assigned to self, some to colleagues)
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks: some assigned to self, some to colleagues
  2. Navigate to calendar view
  3. Select "Project Tasks" view
  4. Verify own tasks appear in primary color (blue)
  5. Verify colleagues' tasks appear in secondary color (gray/purple)
  6. Verify color legend or key is displayed

- **Test Data:**
  - Own Tasks: 3 tasks
  - Colleague Tasks: 3 tasks

- **Expected Result:**
  - Own tasks use primary color (blue)
  - Colleagues' tasks use secondary color (gray/purple)
  - Color legend or key is visible
  - Color coding is consistent

**Test Results:** ⏳ Pending

---

## TC-002-018
**Test Scenario:** Empty calendar state handled when no tasks exist

**Pre Conditions:**
- User logged in as: Staff
- No tasks exist in the system
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Log in as staff user with no tasks
  2. Navigate to calendar view
  3. Verify empty state message is displayed
  4. Verify no errors occur

- **Test Data:**
  - Task Count: 0

- **Expected Result:**
  - Empty state message is displayed (e.g., "No tasks scheduled")
  - No JavaScript errors occur
  - Calendar renders properly

**Test Results:** ⏳ Pending

---

## TC-002-019
**Test Scenario:** Calendar refreshes when new task is created

**Pre Conditions:**
- User logged in as: Staff
- User has navigated to calendar view
- Calendar is displayed

**Test Details:**
- **Test Steps:**
  1. Navigate to calendar view
  2. Note current task count
  3. Create a new task with due date today
  4. Verify calendar automatically updates
  5. Verify new task appears in calendar

- **Test Data:**
  - New Task: "E2E Test - Auto Refresh"
  - Due Date: Today

- **Expected Result:**
  - Calendar automatically updates/refreshes
  - New task appears in calendar without manual refresh
  - Task count increases by 1

**Test Results:** ⏳ Pending

---

## 📊 TICKET 3: Multi-View Schedule

**Epic:** Calendar & Schedule Management  
**Priority:** P1 (High)  
**Test File:** `multi-view-schedule.spec.ts`

### User Story
As a staff user, I want calendar views (Day/Week/Month) and a horizontal timeline so that I can plan effectively.

---

## TC-003-001
**Test Scenario:** Tasks render correctly in Day view

**Pre Conditions:**
- User logged in as: Staff
- Task with due date and time exists
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create a task with due date today at 2:00 PM
  2. Navigate to calendar view
  3. Select "Day" view
  4. Verify task appears on today's date
  5. Verify task shows correct time (2:00 PM)
  6. Verify task details are visible

- **Test Data:**
  - Task Title: "E2E Test - Day View"
  - Due Date: Today
  - Due Time: 2:00 PM

- **Expected Result:**
  - Task appears on today's date in Day view
  - Task displays correct time (2:00 PM)
  - Task details are visible and readable

**Test Results:** ⏳ Pending

---

## TC-003-002
**Test Scenario:** Tasks render correctly in Week view

**Pre Conditions:**
- User logged in as: Staff
- Multiple tasks exist for different days of the week
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks for different days of the week (Monday, Wednesday, Friday)
  2. Navigate to calendar view
  3. Select "Week" view
  4. Verify all 3 tasks are visible in current week
  5. Verify each task appears on correct day column
  6. Verify week header shows correct date range

- **Test Data:**
  - Task 1: Monday at 10:00 AM
  - Task 2: Wednesday at 3:00 PM
  - Task 3: Friday at 11:00 AM

- **Expected Result:**
  - All 3 tasks are visible in Week view
  - Each task appears on correct day column
  - Week header displays correct date range

**Test Results:** ⏳ Pending

---

## TC-003-003
**Test Scenario:** Tasks render correctly in Month view

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist for different days of the month
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks for different days of the month (1st, 15th, 30th)
  2. Navigate to calendar view
  3. Select "Month" view
  4. Verify all tasks appear on correct dates
  5. Verify month header shows current month and year
  6. Verify tasks are distributed across calendar grid

- **Test Data:**
  - Task 1: 1st of current month
  - Task 2: 15th of current month
  - Task 3: 30th of current month

- **Expected Result:**
  - All tasks appear on correct dates
  - Month header displays current month and year
  - Tasks are properly distributed in calendar grid

**Test Results:** ⏳ Pending

---

## TC-003-004
**Test Scenario:** Tasks render correctly in Timeline view

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist with various due dates
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks with various due dates (today, next week, next month)
  2. Navigate to calendar view
  3. Select "Timeline" view (horizontal timeline)
  4. Verify tasks appear chronologically from left to right
  5. Verify timeline scale shows dates/weeks/months
  6. Verify tasks are positioned according to due dates

- **Test Data:**
  - Task 1: Today
  - Task 2: Next week
  - Task 3: Next month

- **Expected Result:**
  - Tasks appear chronologically from left to right
  - Timeline scale is visible and correct
  - Tasks are positioned accurately by due date

**Test Results:** ⏳ Pending

---

## TC-003-005
**Test Scenario:** Task data preserved when switching between views

**Pre Conditions:**
- User logged in as: Staff
- Task with complete details exists
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create a task with all details (title, due date, assignee, description)
  2. Navigate to calendar view
  3. View task in Day view - verify all details present
  4. Switch to Week view - verify same task details
  5. Switch to Month view - verify same task details
  6. Switch to Timeline view - verify same task details
  7. Verify no data loss when switching views

- **Test Data:**
  - Task Title: "E2E Test - Complete Task"
  - Due Date: Tomorrow at 3:00 PM
  - Assignee: Current user
  - Description: "Full task details for testing"

- **Expected Result:**
  - All task details are preserved across all views
  - No data loss when switching views
  - Task appears consistently in all views

**Test Results:** ⏳ Pending

---

## TC-003-006
**Test Scenario:** Filter tasks by project in Day view

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist for multiple projects (Project A and Project B)
- Tasks have due dates today
- User has navigated to calendar Day view

**Test Details:**
- **Test Steps:**
  1. Create tasks for Project A and Project B with due dates today
  2. Navigate to calendar Day view
  3. Apply project filter: "Project A"
  4. Verify only Project A tasks are displayed
  5. Verify Project B tasks are hidden
  6. Verify task count matches Project A tasks only

- **Test Data:**
  - Project A tasks: 2 tasks
  - Project B tasks: 3 tasks
  - Filter Applied: "Project A"

- **Expected Result:**
  - Only Project A tasks are displayed (2 tasks)
  - Project B tasks are hidden
  - Task count is accurate

**Test Results:** ⏳ Pending

---

## TC-003-007
**Test Scenario:** Filter tasks by project in Week view

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist for multiple projects across the week
- User has navigated to calendar Week view

**Test Details:**
- **Test Steps:**
  1. Create tasks for multiple projects across the week
  2. Navigate to calendar Week view
  3. Apply project filter: "Project Alpha"
  4. Verify only "Project Alpha" tasks appear
  5. Verify filter applies to all days of the week

- **Test Data:**
  - Project Alpha tasks: 4 tasks across the week
  - Other project tasks: 3 tasks
  - Filter Applied: "Project Alpha"

- **Expected Result:**
  - Only "Project Alpha" tasks are visible
  - Filter applies consistently across all days
  - Other project tasks are hidden

**Test Results:** ⏳ Pending

---

## TC-003-008
**Test Scenario:** Filter tasks by project in Month view

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist for different projects throughout the month
- User has navigated to calendar Month view

**Test Details:**
- **Test Steps:**
  1. Create tasks for different projects throughout the month
  2. Navigate to calendar Month view
  3. Apply project filter: "Project Beta"
  4. Verify only "Project Beta" tasks are displayed
  5. Verify filter applies to all dates in the month

- **Test Data:**
  - Project Beta tasks: 6 tasks throughout month
  - Other project tasks: 5 tasks
  - Filter Applied: "Project Beta"

- **Expected Result:**
  - Only "Project Beta" tasks are displayed
  - Filter applies to all dates in month
  - Task distribution is accurate

**Test Results:** ⏳ Pending

---

## TC-003-009
**Test Scenario:** Filter tasks by project in Timeline view

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist for multiple projects with various due dates
- User has navigated to calendar Timeline view

**Test Details:**
- **Test Steps:**
  1. Create tasks for multiple projects with various due dates
  2. Navigate to calendar Timeline view
  3. Apply project filter: "Project Gamma"
  4. Verify only "Project Gamma" tasks appear in timeline
  5. Verify timeline scale adjusts if needed

- **Test Data:**
  - Project Gamma tasks: 5 tasks with various due dates
  - Other project tasks: 4 tasks
  - Filter Applied: "Project Gamma"

- **Expected Result:**
  - Only "Project Gamma" tasks appear in timeline
  - Tasks are positioned chronologically
  - Timeline scale is appropriate

**Test Results:** ⏳ Pending

---

## TC-003-010
**Test Scenario:** Project filter maintained when switching between views

**Pre Conditions:**
- User logged in as: Staff
- Project filter is applied
- User is in calendar view

**Test Details:**
- **Test Steps:**
  1. Navigate to calendar Day view
  2. Apply project filter: "Project A"
  3. Verify filter is active
  4. Switch to Week view - verify filter still applied
  5. Switch to Month view - verify filter still applied
  6. Switch to Timeline view - verify filter still applied
  7. Verify same filtered tasks appear in all views

- **Test Data:**
  - Filter Applied: "Project A"
  - Project A tasks: Multiple tasks

- **Expected Result:**
  - Filter remains active when switching views
  - Same filtered tasks appear in all views
  - Filter state is preserved across view changes

**Test Results:** ⏳ Pending

---

## TC-003-011
**Test Scenario:** Highlight matching tasks when searching by keyword

**Pre Conditions:**
- User logged in as: Staff
- Multiple tasks exist with different titles
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks with titles: "Design Homepage", "Design Logo", "Review Budget"
  2. Navigate to calendar view
  3. Enter search keyword: "Design"
  4. Verify "Design Homepage" and "Design Logo" are highlighted or visible
  5. Verify "Review Budget" is dimmed or hidden
  6. Verify search results count shows "2 matches"

- **Test Data:**
  - Task 1: "Design Homepage"
  - Task 2: "Design Logo"
  - Task 3: "Review Budget"
  - Search Keyword: "Design"

- **Expected Result:**
  - Matching tasks are highlighted or visible (2 tasks)
  - Non-matching tasks are dimmed or hidden
  - Search results count is accurate ("2 matches")

**Test Results:** ⏳ Pending

---

## TC-003-012
**Test Scenario:** Search tasks by title across all views

**Pre Conditions:**
- User logged in as: Staff
- Tasks with various titles exist
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks with various titles
  2. Navigate to calendar Day view
  3. Search for keyword "meeting"
  4. Verify matching tasks appear in Day view
  5. Switch to Week view - verify same search results
  6. Switch to Month view - verify same search results
  7. Switch to Timeline view - verify same search results

- **Test Data:**
  - Tasks with "meeting" in title: 3 tasks
  - Other tasks: 4 tasks
  - Search Keyword: "meeting"

- **Expected Result:**
  - Search results are consistent across all views
  - Only matching tasks are displayed
  - Search state is maintained when switching views

**Test Results:** ⏳ Pending

---

## TC-003-013
**Test Scenario:** Search tasks by description content

**Pre Conditions:**
- User logged in as: Staff
- Tasks with keyword in description exist
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks with keyword "urgent" in description
  2. Navigate to calendar view
  3. Search for "urgent"
  4. Verify tasks with "urgent" in description are highlighted
  5. Verify search looks at both title and description fields

- **Test Data:**
  - Tasks with "urgent" in description: 2 tasks
  - Search Keyword: "urgent"

- **Expected Result:**
  - Tasks with keyword in description are found
  - Search checks both title and description fields
  - Matching tasks are highlighted or visible

**Test Results:** ⏳ Pending

---

## TC-003-014
**Test Scenario:** Clear search and display all tasks

**Pre Conditions:**
- User logged in as: Staff
- Search is active with filtered results
- User is in calendar view

**Test Details:**
- **Test Steps:**
  1. Create multiple tasks
  2. Navigate to calendar view
  3. Search for keyword "test"
  4. Verify filtered results appear
  5. Clear search input
  6. Verify all tasks are displayed again
  7. Verify no tasks are dimmed or hidden

- **Test Data:**
  - Total tasks: 7 tasks
  - Tasks matching "test": 2 tasks

- **Expected Result:**
  - Clearing search displays all tasks
  - No tasks are dimmed or hidden after clearing
  - Full task list is restored

**Test Results:** ⏳ Pending

---

## TC-003-015
**Test Scenario:** Empty state displayed when no tasks match search

**Pre Conditions:**
- User logged in as: Staff
- Tasks exist in the system
- User has navigated to calendar view

**Test Details:**
- **Test Steps:**
  1. Create tasks
  2. Navigate to calendar view
  3. Search for keyword that doesn't match any task: "xyz123nonexistent"
  4. Verify empty state message is displayed: "No tasks found"
  5. Verify no tasks are displayed

- **Test Data:**
  - Search Keyword: "xyz123nonexistent" (no matches)

- **Expected Result:**
  - Empty state message is displayed
  - Message indicates no tasks found
  - No tasks are displayed in calendar

**Test Results:** ⏳ Pending

---

## TC-003-016
**Test Scenario:** Navigate to previous/next period in Day view

**Pre Conditions:**
- User logged in as: Staff
- User is in calendar Day view (today)

**Test Details:**
- **Test Steps:**
  1. Navigate to calendar Day view (today)
  2. Click "Previous" button
  3. Verify view shows yesterday
  4. Click "Next" button twice
  5. Verify view shows tomorrow
  6. Verify navigation updates header date

- **Test Data:**
  - Starting View: Today
  - Navigation: Previous (-1 day), Next (+2 days)

- **Expected Result:**
  - Previous button shows yesterday
  - Next button shows tomorrow
  - Header date updates correctly
  - Navigation is smooth and accurate

**Test Results:** ⏳ Pending

---

## TC-003-017
**Test Scenario:** Navigate to previous/next period in Week view

**Pre Conditions:**
- User logged in as: Staff
- User is in calendar Week view

**Test Details:**
- **Test Steps:**
  1. Navigate to calendar Week view
  2. Note current week range
  3. Click "Previous Week" button
  4. Verify view shows previous week
  5. Click "Next Week" button twice
  6. Verify view shows next week

- **Test Data:**
  - Starting View: Current week
  - Navigation: Previous week, Next week (x2)

- **Expected Result:**
  - Previous button shows previous week
  - Next button shows next week
  - Week range updates correctly
  - Navigation is smooth

**Test Results:** ⏳ Pending

---

## TC-003-018
**Test Scenario:** Navigate to previous/next period in Month view

**Pre Conditions:**
- User logged in as: Staff
- User is in calendar Month view (current month)

**Test Details:**
- **Test Steps:**
  1. Navigate to calendar Month view (current month)
  2. Click "Previous Month" button
  3. Verify header shows previous month name
  4. Click "Next Month" button twice
  5. Verify header shows next month name

- **Test Data:**
  - Starting View: Current month (November 2025)

- **Expected Result:**
  - Previous button shows previous month
  - Next button shows next month
  - Month header updates correctly
  - Calendar grid displays correct dates

**Test Results:** ⏳ Pending

---

## TC-003-019
**Test Scenario:** Return to today/current period with Today button

**Pre Conditions:**
- User logged in as: Staff
- User has navigated away from current date
- User is in calendar view

**Test Details:**
- **Test Steps:**
  1. Navigate to calendar view
  2. Navigate to a date in the past (e.g., last month)
  3. Click "Today" button
  4. Verify view returns to current day/week/month depending on active view
  5. Verify today's date is highlighted

- **Test Data:**
  - Current Date: November 6, 2025

- **Expected Result:**
  - "Today" button returns to current period
  - Today's date is highlighted
  - Appropriate view level is maintained (Day/Week/Month)

**Test Results:** ⏳ Pending

---

## TC-003-020
**Test Scenario:** Task count indicator displayed in calendar cells

**Pre Conditions:**
- User logged in as: Staff
- Multiple tasks exist for specific date
- User has navigated to calendar Month view

**Test Details:**
- **Test Steps:**
  1. Create 5 tasks for a specific date
  2. Navigate to calendar Month view
  3. Locate the date cell
  4. Verify cell shows count indicator (e.g., "+5 tasks" or badge with "5")
  5. Click on cell to see all tasks

- **Test Data:**
  - Tasks for specific date: 5 tasks
  - View: Month view

- **Expected Result:**
  - Cell displays task count indicator
  - Count is accurate (shows "5")
  - Clicking cell shows all tasks

**Test Results:** ⏳ Pending

---

## TC-003-021
**Test Scenario:** Tasks without due dates handled in timeline view

**Pre Conditions:**
- User logged in as: Staff
- Task without due date exists
- User has navigated to calendar Timeline view

**Test Details:**
- **Test Steps:**
  1. Create a task without a due date
  2. Navigate to calendar Timeline view
  3. Verify task without due date is not displayed in timeline (expected behavior)
  4. OR verify task appears in "Unscheduled" section if supported

- **Test Data:**
  - Task Title: "E2E Test - No Due Date"
  - Due Date: None/Null

- **Expected Result:**
  - Task without due date is not shown in timeline
  - OR task appears in "Unscheduled" section
  - No errors occur
  - Timeline displays correctly

**Test Results:** ⏳ Pending

---

## 📅 TICKET 4: Attach Due Dates

**Epic:** Task Management - Due Dates  
**Priority:** P0 (Critical)  
**Test File:** `task-due-dates.spec.ts`

### User Story
As a staff user, I want to attach a due date (and optional time) to a task so that it appears on my schedule and I can plan work effectively.

---

## TC-004-001
**Test Scenario:** Add due date to task and see it on My Analytics Page

**Pre Conditions:**
- User logged in as: Staff
- User can create tasks
- My Analytics Page is accessible

**Test Details:**
- **Test Steps:**
  1. Log in as staff user
  2. Create a new task
  3. Set task title: "Complete quarterly report"
  4. Set due date: 5 days from now
  5. Save task
  6. Navigate to My Analytics Page
  7. Locate "Upcoming Commitments" section
  8. Verify task "Complete quarterly report" appears on the set due date
  9. Verify task is listed in chronological order

- **Test Data:**
  - Task Title: "Complete quarterly report"
  - Due Date: Current Date + 5 days

- **Expected Result:**
  - Task appears in "Upcoming Commitments" on My Analytics Page
  - Task is displayed on the correct due date
  - Task is listed in chronological order

**Test Results:** ⏳ Pending

---

## TC-004-002
**Test Scenario:** Update due date and see changes on My Analytics Page

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists
- User has navigated to My Analytics Page

**Test Details:**
- **Test Steps:**
  1. Create a task with due date tomorrow
  2. Verify task appears in upcoming commitments for tomorrow
  3. Edit task and change due date to 1 week from now
  4. Save changes
  5. Navigate to My Analytics Page
  6. Verify task no longer appears in tomorrow's commitments
  7. Verify task appears in commitments for 1 week from now

- **Test Data:**
  - Original Due Date: Tomorrow
  - Updated Due Date: Current Date + 7 days

- **Expected Result:**
  - Task is removed from original due date
  - Task appears on new due date
  - My Analytics Page updates correctly

**Test Results:** ⏳ Pending

---

## TC-004-003
**Test Scenario:** Task with due date displayed in task list

**Pre Conditions:**
- User logged in as: Staff
- User can create tasks
- User has navigated to task list view

**Test Details:**
- **Test Steps:**
  1. Create a task with title "Review PR #123"
  2. Set due date: November 15, 2025
  3. Save task
  4. Navigate to task list view
  5. Locate task "Review PR #123"
  6. Verify due date "Nov 15, 2025" is displayed next to task
  7. Verify due date is formatted correctly

- **Test Data:**
  - Task Title: "Review PR #123"
  - Due Date: November 15, 2025

- **Expected Result:**
  - Task is visible in task list
  - Due date is displayed next to task
  - Due date format is correct (e.g., "Nov 15, 2025")

**Test Results:** ⏳ Pending

---

## TC-004-004
**Test Scenario:** Due date indicator icon displayed in task list

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists
- User has navigated to task list

**Test Details:**
- **Test Steps:**
  1. Create a task with a due date
  2. Navigate to task list
  3. Verify task row shows calendar icon or due date icon
  4. Verify icon is positioned near due date text
  5. Hover over icon - verify tooltip shows full due date/time if available

- **Test Data:**
  - Task with due date: Tomorrow at 2:00 PM

- **Expected Result:**
  - Calendar or due date icon is displayed
  - Icon is positioned appropriately
  - Tooltip shows full due date/time information

**Test Results:** ⏳ Pending

---

## TC-004-005
**Test Scenario:** Tasks with due time positioned chronologically on My Analytics Page

**Pre Conditions:**
- User logged in as: Staff
- Multiple tasks with same due date but different times exist
- User has navigated to My Analytics Page

**Test Details:**
- **Test Steps:**
  1. Create Task A with due date tomorrow at 9:00 AM
  2. Create Task B with due date tomorrow at 2:00 PM
  3. Create Task C with due date tomorrow at 11:30 AM
  4. Navigate to My Analytics Page
  5. Locate upcoming commitments for tomorrow
  6. Verify tasks are ordered by time: Task A (9:00 AM), Task C (11:30 AM), Task B (2:00 PM)

- **Test Data:**
  - Task A: Tomorrow at 9:00 AM
  - Task B: Tomorrow at 2:00 PM
  - Task C: Tomorrow at 11:30 AM

- **Expected Result:**
  - Tasks are ordered chronologically by time
  - Task A appears first, Task C second, Task B third
  - Time-based ordering is accurate

**Test Results:** ⏳ Pending

---

## TC-004-006
**Test Scenario:** Due time displayed in task details

**Pre Conditions:**
- User logged in as: Staff
- Task with due date and time exists
- User has navigated to task list

**Test Details:**
- **Test Steps:**
  1. Create a task with due date November 10, 2025 at 3:30 PM
  2. Save task
  3. Navigate to task list
  4. Click on task to open details
  5. Verify due date shows: "Nov 10, 2025"
  6. Verify due time shows: "3:30 PM" or "15:30" depending on format
  7. Verify date and time are clearly separated or formatted

- **Test Data:**
  - Due Date: November 10, 2025
  - Due Time: 3:30 PM

- **Expected Result:**
  - Due date is displayed correctly
  - Due time is displayed correctly
  - Date and time are clearly formatted

**Test Results:** ⏳ Pending

---

## TC-004-007
**Test Scenario:** Set due date without due time

**Pre Conditions:**
- User logged in as: Staff
- User is creating a new task

**Test Details:**
- **Test Steps:**
  1. Create a task
  2. Set due date: November 20, 2025
  3. Leave due time empty
  4. Save task
  5. Verify task is saved successfully
  6. Verify task appears in task list with due date only
  7. Verify no time is displayed (or shows "All Day" or similar)

- **Test Data:**
  - Due Date: November 20, 2025
  - Due Time: None/Empty

- **Expected Result:**
  - Task is saved successfully
  - Due date is displayed
  - No time is shown (or "All Day" indicator appears)

**Test Results:** ⏳ Pending

---

## TC-004-008
**Test Scenario:** Task removed from calendar when due date is removed

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists
- Task appears in calendar view

**Test Details:**
- **Test Steps:**
  1. Create a task with due date tomorrow
  2. Navigate to calendar view
  3. Verify task appears on tomorrow's date
  4. Edit task and remove due date (set to null/empty)
  5. Save changes
  6. Verify task no longer appears in calendar view
  7. Verify calendar shows correct task count (decreased by 1)

- **Test Data:**
  - Original Due Date: Tomorrow
  - Updated Due Date: None/Null

- **Expected Result:**
  - Task is removed from calendar view
  - Task count decreases by 1
  - Calendar updates correctly

**Test Results:** ⏳ Pending

---

## TC-004-009
**Test Scenario:** Task removed from timeline when due date is removed

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists
- Task appears in timeline view

**Test Details:**
- **Test Steps:**
  1. Create a task with due date next week
  2. Navigate to timeline view
  3. Verify task appears in timeline at correct position
  4. Edit task and remove due date
  5. Save changes
  6. Verify task no longer appears in timeline view

- **Test Data:**
  - Original Due Date: Next week (Current Date + 7 days)
  - Updated Due Date: None/Null

- **Expected Result:**
  - Task is removed from timeline view
  - Timeline updates correctly
  - No errors occur

**Test Results:** ⏳ Pending

---

## TC-004-010
**Test Scenario:** Task removed from My Analytics upcoming commitments when due date removed

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists
- Task appears in upcoming commitments on My Analytics Page

**Test Details:**
- **Test Steps:**
  1. Create a task with due date 3 days from now
  2. Navigate to My Analytics Page
  3. Verify task appears in upcoming commitments
  4. Edit task and remove due date
  5. Save changes
  6. Navigate back to My Analytics Page
  7. Verify task no longer appears in upcoming commitments section

- **Test Data:**
  - Original Due Date: Current Date + 3 days
  - Updated Due Date: None/Null

- **Expected Result:**
  - Task is removed from upcoming commitments
  - My Analytics Page updates correctly
  - Task is no longer scheduled

**Test Results:** ⏳ Pending

---

## TC-004-011
**Test Scenario:** Task remains in list after removing due date

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists in task list

**Test Details:**
- **Test Steps:**
  1. Create a task "Update documentation"
  2. Set due date: December 1, 2025
  3. Verify task appears in task list with due date
  4. Edit task and remove due date
  5. Save changes
  6. Navigate to task list
  7. Verify task "Update documentation" is still visible
  8. Verify due date field is empty or shows "No due date"
  9. Verify task is still accessible and editable

- **Test Data:**
  - Task Title: "Update documentation"
  - Original Due Date: December 1, 2025
  - Updated Due Date: None/Null

- **Expected Result:**
  - Task remains visible in task list
  - Due date field is empty or shows "No due date"
  - Task is still accessible and editable

**Test Results:** ⏳ Pending

---

## TC-004-012
**Test Scenario:** No date indicator shown when due date is removed

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists
- Due date has been removed

**Test Details:**
- **Test Steps:**
  1. Create a task with a due date
  2. Remove due date
  3. Navigate to task list
  4. Verify task shows "No due date" text or empty date field
  5. Verify no calendar icon is displayed next to task

- **Test Data:**
  - Task with due date initially set
  - Due date removed

- **Expected Result:**
  - "No due date" text or empty date field is displayed
  - Calendar icon is not displayed
  - Clear indication that task has no due date

**Test Results:** ⏳ Pending

---

## TC-004-013
**Test Scenario:** Prevent setting past due date when creating new task

**Pre Conditions:**
- User logged in as: Staff
- User is creating a new task
- User attempts to set past due date

**Test Details:**
- **Test Steps:**
  1. Click "Create New Task" button
  2. Fill in task title: "New task"
  3. Try to set due date: 1 week in the past (e.g., November 1, 2025 if today is Nov 8)
  4. Verify validation error appears: "Due date cannot be in the past"
  5. Verify "Save" button is disabled or error prevents saving
  6. Change due date to future date
  7. Verify validation error clears
  8. Verify task can be saved successfully

- **Test Data:**
  - Task Title: "New task"
  - Attempted Due Date: 1 week in the past
  - Corrected Due Date: Future date

- **Expected Result:**
  - Validation error is displayed
  - Save button is disabled or error prevents saving
  - Error clears when valid date is entered
  - Task saves successfully with valid date

**Test Results:** ⏳ Pending

---

## TC-004-014
**Test Scenario:** Allow editing existing task to past due date (overdue scenario)

**Pre Conditions:**
- User logged in as: Staff
- Existing task with due date exists
- User is editing the task

**Test Details:**
- **Test Steps:**
  1. Create a task with due date tomorrow
  2. Wait or simulate time passing (task is now created)
  3. Edit the task
  4. Try to set due date to yesterday
  5. Verify system allows this (for overdue task scenario)
  6. OR verify system shows warning but allows override
  7. Save task
  8. Verify task is marked as overdue

- **Test Data:**
  - Original Due Date: Tomorrow
  - Updated Due Date: Yesterday (past date)

- **Expected Result:**
  - System allows setting past due date for existing task
  - OR system shows warning but allows override
  - Task is marked as overdue after saving

**Test Results:** ⏳ Pending

---

## TC-004-015
**Test Scenario:** Date picker shows today highlighted and past dates disabled

**Pre Conditions:**
- User logged in as: Staff
- User is creating a new task

**Test Details:**
- **Test Steps:**
  1. Create a new task
  2. Click on due date field to open date picker
  3. Verify date picker opens
  4. Verify today's date is highlighted or marked
  5. Verify past dates are disabled or grayed out
  6. Verify future dates are selectable

- **Test Data:**
  - Current Date: November 6, 2025

- **Expected Result:**
  - Date picker opens correctly
  - Today's date is highlighted
  - Past dates are disabled or grayed out
  - Future dates are selectable

**Test Results:** ⏳ Pending

---

## TC-004-016
**Test Scenario:** Update task due date via drag and drop in calendar

**Pre Conditions:**
- User logged in as: Staff
- Task with due date exists
- User has navigated to calendar view
- Drag and drop functionality is supported

**Test Details:**
- **Test Steps:**
  1. Create a task with due date tomorrow
  2. Navigate to calendar view
  3. Locate the task on tomorrow's date
  4. Drag task to a different date (e.g., next week)
  5. Drop task on new date
  6. Verify task updates to new due date
  7. Verify change is persisted (refresh page and verify)

- **Test Data:**
  - Original Due Date: Tomorrow
  - New Due Date: Next week

- **Expected Result:**
  - Task can be dragged to new date
  - Due date updates correctly
  - Change is persisted in database

**Test Results:** ⏳ Pending (Advanced Feature)

---

## TC-004-017
**Test Scenario:** Set recurring due dates for tasks

**Pre Conditions:**
- User logged in as: Staff
- Recurring tasks feature is supported
- User is creating a new task

**Test Details:**
- **Test Steps:**
  1. Create a task: "Weekly team standup"
  2. Set due date: next Monday
  3. Enable recurring option: "Every week"
  4. Save task
  5. Navigate to calendar view
  6. Verify task appears on multiple Mondays
  7. Verify recurring indicator is shown

- **Test Data:**
  - Task Title: "Weekly team standup"
  - First Due Date: Next Monday
  - Recurrence: Every week

- **Expected Result:**
  - Task is created with recurring due dates
  - Task appears on multiple Mondays in calendar
  - Recurring indicator is displayed

**Test Results:** ⏳ Pending (Optional Feature)

---

## TC-004-018
**Test Scenario:** Display due date in different timezone based on user settings

**Pre Conditions:**
- User logged in as: Staff
- Timezone settings are supported
- Task with due date and time exists

**Test Details:**
- **Test Steps:**
  1. Set user timezone to PST (GMT-8)
  2. Create a task with due date/time: Nov 10, 2025, 5:00 PM PST
  3. Change user timezone to EST (GMT-5)
  4. View task details
  5. Verify due time shows: 8:00 PM EST (3 hour difference)
  6. Verify timezone is displayed

- **Test Data:**
  - Original Timezone: PST (GMT-8)
  - Original Time: 5:00 PM
  - New Timezone: EST (GMT-5)
  - Expected Time: 8:00 PM

- **Expected Result:**
  - Due time adjusts based on timezone change
  - Time difference is calculated correctly (3 hours)
  - Timezone is displayed with time

**Test Results:** ⏳ Pending (Advanced Feature)

---

## TC-004-019
**Test Scenario:** Overdue badge displayed when current time passes due date

**Pre Conditions:**
- User logged in as: Staff
- Task with past due date exists
- Task is not completed
- User has navigated to task list

**Test Details:**
- **Test Steps:**
  1. Create a task with due date yesterday
  2. Set status: "In Progress" (not completed)
  3. Navigate to task list
  4. Verify task shows overdue badge/indicator
  5. Verify due date is displayed in red or with warning color

- **Test Data:**
  - Due Date: Yesterday
  - Status: "In Progress"

- **Expected Result:**
  - Overdue badge/indicator is displayed
  - Due date shows in red or warning color
  - Task is clearly marked as overdue

**Test Results:** ⏳ Pending

---

## TC-004-020
**Test Scenario:** Sort tasks by due date in task list

**Pre Conditions:**
- User logged in as: Staff
- Multiple tasks with different due dates exist
- User has navigated to task list

**Test Details:**
- **Test Steps:**
  1. Create Task A with due date in 3 days
  2. Create Task B with due date tomorrow
  3. Create Task C with due date in 1 week
  4. Navigate to task list
  5. Apply sort: "Due Date (Earliest First)"
  6. Verify task order: Task B, Task A, Task C
  7. Apply sort: "Due Date (Latest First)"
  8. Verify task order: Task C, Task A, Task B

- **Test Data:**
  - Task A: Due in 3 days
  - Task B: Due tomorrow
  - Task C: Due in 1 week

- **Expected Result:**
  - Sorting by earliest first: Task B, Task A, Task C
  - Sorting by latest first: Task C, Task A, Task B
  - Sort functionality works correctly

**Test Results:** ⏳ Pending

---

## 📈 Test Coverage Summary

| Ticket | Category | Tests | Priority |
|--------|----------|-------|----------|
| 1 | Overdue Alerts | 11 | P0 (Critical) |
| 2 | Team Schedule Visibility | 21 | P0 (Critical) |
| 3 | Multi-View Schedule | 24 | P1 (High) |
| 4 | Attach Due Dates | 20 | P0 (Critical) |
| **TOTAL** | **4 Tickets** | **76** | - |

---

## 🎯 Priority Breakdown

- **P0 (Critical):** 52 tests across 3 tickets (Tickets 1, 2, 4)
- **P1 (High):** 24 tests across 1 ticket (Ticket 3)

---

## 📝 Test Execution Notes

### Best Practices to Implement
1. **Page Object Model (POM):** Create `CalendarPage`, `TaskListPage`, `AnalyticsPage` classes
2. **Test Data Builders:** Create fluent API for task creation (`createTaskWithDueDate()`, `createOverdueTask()`)
3. **Custom Assertions:** Reusable helpers (`assertTaskIsOverdue()`, `assertTaskInCalendar()`)
4. **Time Mocking:** Use Playwright's `page.clock.install()` for overdue scenarios
5. **Authentication Fixtures:** Pre-authenticated user contexts for staff and manager roles

### Test Data Requirements
- Users: Staff users in multiple departments, Manager users
- Projects: Multiple projects across departments
- Tasks: Mix of tasks with various due dates, statuses, and assignees
- Departments: At least 2-3 departments for visibility testing

### Environment Setup
```bash
# Install dependencies
npm install

# Set up test environment variables
cp .env.example .env.test

# Ensure backend is running
cd backend && docker-compose up -d

# Run database migrations
npm run db:migrate
```

### Running Tests
```bash
# Run all schedule tests
npm run test:e2e:schedule

# Run specific ticket tests
npx playwright test task-overdue-alerts.spec.ts       # Ticket 1
npx playwright test team-schedule-visibility.spec.ts  # Ticket 2
npx playwright test multi-view-schedule.spec.ts       # Ticket 3
npx playwright test task-due-dates.spec.ts            # Ticket 4

# Run with UI mode for debugging
npx playwright test --ui

# Run specific test
npx playwright test task-overdue-alerts.spec.ts:27
```

### Known Considerations
- Email testing for overdue alerts requires email service mock or integration
- Timezone testing may require additional configuration
- Drag-and-drop in calendar requires advanced Playwright gestures
- Real-time updates may need WebSocket testing
- Performance testing for large task sets recommended

---

## 🔄 CI/CD Integration

```yaml
# .github/workflows/e2e-schedule-tests.yml
name: E2E Schedule Tests

on:
  pull_request:
    paths:
      - 'frontend/app/**'
      - 'frontend/components/**'
      - 'frontend/e2e/tests/schedule/**'
      - 'backend/**'

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-node@v3
        with:
          node-version: '18'
      - name: Install dependencies
        run: npm ci
      - name: Install Playwright
        run: npx playwright install --with-deps
      - name: Start backend services
        run: docker-compose up -d
      - name: Run E2E tests
        run: npm run test:e2e:schedule
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: playwright-report
          path: playwright-report/
```

---

**Last Updated:** November 6, 2025  
**Created By:** SPM Orange Team  
**Document Status:** Draft - Pending Implementation
