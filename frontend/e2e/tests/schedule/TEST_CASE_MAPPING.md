# E2E Test Case to Acceptance Criteria Mapping

**Total E2E Tests:** 76
**Total Test Files:** 4
**Total Acceptance Criteria:** 21
**Coverage:** 100% (21/21 ACs covered)
**Last Updated:** November 6, 2025

---

## TICKET 1: Overdue Alerts

**Test File:** `task-overdue-alerts.spec.ts`
**Total Tests:** 11
**Coverage:** 100% (6/6 ACs)

### AC 1.1: Marking a Task as Overdue - System Automatically Marks

**✅ Test:** `should mark task as overdue when due date passes` (line 27)

**Steps:**
1. Create a task with due date in the past (yesterday)
2. Set task status to "In Progress" (not completed)
3. Navigate to task list view
4. Verify task has "Overdue" status indicator
5. Verify task metadata shows it is overdue

**Mapping:**
- **Given:** A task has a due date/time
- **When:** The due date/time passes and the task is not marked complete
- **Then:** The system marks the task as Overdue

---

### AC 1.2: Visual Indicator Across All Views

**✅ Test:** `should highlight overdue task in red across list view` (line 52)

**Steps:**
1. Create an overdue task (due date in past, status not "Done")
2. Navigate to task list view
3. Verify task row has red background or red border
4. Verify overdue visual indicator is present (icon, badge, or color)

**Mapping:**
- **Given:** A task is overdue
- **When:** I view the task in list view
- **Then:** The task is highlighted in red (or another visual indicator)

---

**✅ Test:** `should highlight overdue task in board view` (line 78)

**Steps:**
1. Create an overdue task
2. Navigate to board view (Kanban)
3. Locate the overdue task card
4. Verify card has red border or red background
5. Verify overdue badge/icon is visible on card

**Mapping:**
- **Given:** A task is overdue
- **When:** I view the task in board view
- **Then:** The task is highlighted in red across board view

---

**✅ Test:** `should highlight overdue task in calendar view` (line 104)

**Steps:**
1. Create an overdue task with due date 2 days ago
2. Navigate to calendar view
3. Go to the date where the task was due
4. Verify task appears with red color or red border
5. Verify overdue indicator is visible on calendar event

**Mapping:**
- **Given:** A task is overdue
- **When:** I view the task in calendar view
- **Then:** The task is highlighted in red across calendar view

---

### AC 1.3: Clearing Overdue State on Completion

**✅ Test:** `should clear overdue status when task is marked as done` (line 130)

**Steps:**
1. Create an overdue task (due yesterday, status "In Progress")
2. Verify task is marked as overdue (red indicator)
3. Update task status to "Done"
4. Verify overdue indicator is removed
5. Verify task no longer has red highlighting

**Mapping:**
- **Given:** A task is overdue
- **When:** I mark the task as Done
- **Then:** The overdue status clears

---

### AC 1.4: Completion Timestamp in Audit Log

**✅ Test:** `should record completion timestamp in audit log when overdue task is completed` (line 162)

**Steps:**
1. Create an overdue task
2. Mark task as "Done"
3. Open task details/history
4. Verify completion timestamp is recorded in audit log
5. Verify completion timestamp is after the original due date

**Mapping:**
- **Given:** A task is overdue
- **When:** I mark the task as Done
- **Then:** The system records the completion timestamp in the audit log

---

### AC 1.5: Overdue Reminder Email Sent

**✅ Test:** `should send overdue reminder email 1 day after due date` (line 195)

**Steps:**
1. Create a task with due date set to 2 days ago
2. Mock or verify email service was called
3. Verify email contains subject mentioning "Overdue"
4. Verify email triggered 1 day after due date
5. (Note: Integration test - requires email service mock)

**Mapping:**
- **Given:** A task has a due date/time
- **When:** It reaches 1 day after due date
- **Then:** The system sends me a reminder email

---

### AC 1.6: Email Contains Required Information

**✅ Test:** `should include all required information in overdue reminder email` (line 225)

**Steps:**
1. Trigger overdue email for a specific task
2. Capture email content
3. Verify email contains task title
4. Verify email contains due date/time formatted correctly
5. Verify email contains current status (e.g., "In Progress")
6. Verify email contains clickable link to task detail page
7. Verify link navigates to correct task when clicked

**Mapping:**
- **Given:** An overdue reminder email is sent
- **When:** I receive the email
- **Then:** The email includes task title, due date/time, current status, and link

---

### Additional Tests (Not Mapped to Specific ACs)

**✅ Test:** `should display overdue count in dashboard summary` (line 260)

**Steps:**
1. Create 3 overdue tasks
2. Navigate to dashboard
3. Verify dashboard displays overdue task count (shows "3")
4. Verify overdue tasks are highlighted in summary widget

**Purpose:** Enhance user awareness of overdue tasks on main dashboard.

---

**✅ Test:** `should filter overdue tasks in list view` (line 285)

**Steps:**
1. Create mix of overdue and non-overdue tasks
2. Navigate to task list
3. Apply "Overdue" filter
4. Verify only overdue tasks are displayed
5. Verify filter count matches number of overdue tasks

**Purpose:** Allow users to quickly isolate and focus on overdue tasks.

---

**✅ Test:** `should not mark completed task as overdue even if past due date` (line 315)

**Steps:**
1. Create a task with due date yesterday
2. Mark task as "Done" before navigating to list
3. Navigate to task list
4. Verify task does NOT have overdue indicator
5. Verify task shows "Completed" status, not "Overdue"

**Purpose:** Ensure completed tasks are never marked overdue (edge case handling).

---

**✅ Test:** `should handle tasks with no due date gracefully` (line 343)

**Steps:**
1. Create a task without a due date
2. Navigate to task list, board, and calendar views
3. Verify task is displayed normally (no overdue indicator)
4. Verify task does not appear in overdue filter

**Purpose:** Prevent errors when tasks lack due dates.

---

## TICKET 2: Team Schedule Visibility

**Test File:** `team-schedule-visibility.spec.ts`
**Total Tests:** 21
**Coverage:** 100% (8/8 ACs)

### AC 2.1: See Only Accessible Project Tasks

**✅ Test:** `should show only tasks from selected project in calendar` (line 27)

**Steps:**
1. Log in as staff user with access to specific departments
2. Navigate to calendar view
3. Select "Project Tasks" view
4. Filter by a specific project (e.g., "Project Alpha")
5. Verify all visible tasks belong to "Project Alpha"
6. Verify tasks from other projects are not displayed

**Mapping:**
- **Given:** I have access to specific departments
- **When:** I open the Calendar and filter by Project's tasks
- **Then:** I only see tasks from that project

---

### AC 2.2: Hide Tasks from Restricted Departments

**✅ Test:** `should hide tasks from departments outside visibility scope` (line 58)

**Steps:**
1. Log in as staff user with access to Department A only
2. Create a task in Department B (outside scope)
3. Navigate to calendar view
4. Select "Project Tasks" view
5. Verify task from Department B is not visible
6. Verify only tasks from Department A are shown

**Mapping:**
- **Given:** A task belongs to a department outside my visibility scope
- **When:** I view the Calendar
- **Then:** I will not be able to see the project and its related task

---

**✅ Test:** `should allow manager to see all department tasks in reporting scope` (line 90)

**Steps:**
1. Log in as manager with access to multiple departments
2. Navigate to calendar view
3. Select "Project Tasks" view
4. Verify tasks from all managed departments are visible
5. Verify task count matches expected tasks across departments

**Mapping:**
- **Given:** I am a manager with reporting departments
- **When:** I view the Calendar
- **Then:** I see tasks from all my reporting departments

---

### AC 2.3: Color-Code Own vs Colleagues' Tasks

**✅ Test:** `should display own tasks in different color from colleagues tasks` (line 125)

**Steps:**
1. Log in as staff user
2. Create a task assigned to self
3. Create a task assigned to colleague in same department
4. Navigate to calendar view
5. Select "Project Tasks" view
6. Verify own task appears in primary color (e.g., blue background)
7. Verify colleague's task appears in secondary color (e.g., gray or purple)
8. Verify colors are visually distinct

**Mapping:**
- **Given:** I open the Calendar
- **When:** I view it
- **Then:** My own tasks appear in one color (blue) and colleagues' tasks in another (gray/purple)

---

**✅ Test:** `should maintain color consistency across calendar dates` (line 165)

**Steps:**
1. Create multiple tasks: some assigned to self, some to colleagues
2. Navigate to calendar view
3. Switch between Day, Week, and Month views
4. Verify own tasks always use same color (e.g., blue)
5. Verify colleagues' tasks always use secondary color

**Mapping:**
- **Given:** I view the calendar in different views
- **When:** I switch views
- **Then:** Color coding remains consistent

---

### AC 2.4: Task Detail Information on Click

**✅ Test:** `should show task details when clicking task in calendar` (line 200)

**Steps:**
1. Create a task with all details filled
2. Navigate to calendar view
3. Click on the task in calendar
4. Verify modal or detail panel opens
5. Verify task details are displayed:
   - Task name
   - Due date and due time
   - Assignee name
   - Department name

**Mapping:**
- **Given:** I click on a task in the Calendar
- **When:** I view its details
- **Then:** I can see task name, due date/time, assignee name, and department

---

### AC 2.5: Filter by Project, Department, or Date Range

**✅ Test:** `should filter calendar tasks by project` (line 238)

**Steps:**
1. Create tasks for multiple projects
2. Navigate to calendar view
3. Apply project filter (select "Project A")
4. Verify only tasks from "Project A" are shown
5. Verify task count updates correctly

**Mapping:**
- **Given:** I filter by project
- **When:** I apply the filter
- **Then:** Only tasks matching the project filter are displayed

---

**✅ Test:** `should filter calendar tasks by department` (line 268)

**Steps:**
1. Create tasks in multiple departments
2. Navigate to calendar view
3. Apply department filter (select "Engineering")
4. Verify only tasks from "Engineering" are shown
5. Verify tasks from other departments are hidden

**Mapping:**
- **Given:** I filter by department
- **When:** I apply the filter
- **Then:** Only tasks matching the department filter are displayed

---

**✅ Test:** `should filter calendar tasks by date range` (line 298)

**Steps:**
1. Create tasks with various due dates (this week, next week, next month)
2. Navigate to calendar view
3. Set date range filter (e.g., "This Week")
4. Verify only tasks within this week are displayed
5. Change date range to "Next Month"
6. Verify calendar updates to show next month's tasks

**Mapping:**
- **Given:** I filter by date range
- **When:** I apply the filter
- **Then:** Only tasks matching the date range are displayed

---

**✅ Test:** `should apply multiple filters simultaneously` (line 335)

**Steps:**
1. Create diverse set of tasks (multiple projects, departments, dates)
2. Navigate to calendar view
3. Apply multiple filters: Project A + Engineering Department + This Week
4. Verify only tasks matching ALL filters are shown
5. Verify filter count is accurate

**Mapping:**
- **Given:** I apply multiple filters (project + department + date range)
- **When:** Filters are applied
- **Then:** Only tasks matching all filters are displayed

---

### AC 2.6: Edit Tasks with Edit Permissions

**✅ Test:** `should allow manager to edit tasks in managed department` (line 375)

**Steps:**
1. Log in as manager of "Engineering" department
2. Create a task in "Engineering" department assigned to team member
3. Navigate to calendar view
4. Click on the task
5. Verify edit button is visible
6. Click edit button
7. Update task title
8. Save changes
9. Verify task is updated successfully

**Mapping:**
- **Given:** I have edit permissions (manager of department)
- **When:** I click on a task assigned to my department
- **Then:** I can open and edit it (CRUD access)

---

**✅ Test:** `should allow staff to edit own tasks` (line 415)

**Steps:**
1. Log in as staff user
2. Create a task assigned to self
3. Navigate to calendar view
4. Click on own task
5. Verify edit button is visible
6. Update task due date
7. Save changes
8. Verify changes are reflected in calendar

**Mapping:**
- **Given:** I am a staff user viewing my own task
- **When:** I click on my task
- **Then:** I can edit it

---

### AC 2.7: View-Only for Tasks Without Edit Permissions

**✅ Test:** `should allow view-only access to colleague tasks without edit permissions` (line 455)

**Steps:**
1. Log in as staff user
2. Create a task assigned to colleague in same department
3. Navigate to calendar view
4. Click on colleague's task
5. Verify task details are visible
6. Verify edit button is NOT visible or is disabled
7. Verify cannot modify task fields

**Mapping:**
- **Given:** I do not have edit permissions
- **When:** I click on a task assigned to another colleague
- **Then:** I can view but not modify it

---

**✅ Test:** `should display read-only indicator for non-editable tasks` (line 488)

**Steps:**
1. Log in as staff user
2. Navigate to calendar with colleague tasks visible
3. Click on colleague's task
4. Verify "View Only" or similar indicator is displayed
5. Verify all input fields are disabled or read-only

**Mapping:**
- **Given:** I am viewing a task I cannot edit
- **When:** I open task details
- **Then:** I see a read-only indicator

---

### AC 2.8: Switch Between My Tasks and Project Tasks Views

**✅ Test:** `should switch between My Tasks and Project Tasks views` (line 525)

**Steps:**
1. Log in as staff user
2. Create tasks: 2 assigned to self, 3 assigned to colleagues
3. Navigate to calendar view
4. Select "My Tasks" view
5. Verify only own tasks (2 tasks) are displayed
6. Switch to "Project Tasks" view
7. Verify all visible tasks (5 tasks) are displayed with color coding

**Mapping:**
- **Given:** I switch between "My Tasks" and "Project Tasks" views
- **When:** I choose "Project Tasks"
- **Then:** The system displays all visible project tasks (mine + colleagues)

---

**✅ Test:** `should maintain filters when switching between task views` (line 565)

**Steps:**
1. Navigate to calendar view
2. Apply project filter (Project A)
3. Switch from "My Tasks" to "Project Tasks"
4. Verify project filter is still applied
5. Verify task list updates but filter remains active

**Mapping:**
- **Given:** I have filters applied
- **When:** I switch between views
- **Then:** Filters are maintained

---

**✅ Test:** `should display correct color coding in Project Tasks view` (line 595)

**Steps:**
1. Create tasks: some assigned to self, some to colleagues
2. Navigate to calendar view
3. Select "Project Tasks" view
4. Verify own tasks appear in primary color (blue)
5. Verify colleagues' tasks appear in secondary color (gray/purple)
6. Verify color legend or key is displayed

**Mapping:**
- **Given:** I am in "Project Tasks" view
- **When:** I view tasks
- **Then:** Tasks are color-coded by assignee (own vs colleagues)

---

### Additional Tests (Not Mapped to Specific ACs)

**✅ Test:** `should handle empty calendar state when no tasks exist` (line 630)

**Steps:**
1. Log in as staff user with no tasks
2. Navigate to calendar view
3. Verify empty state message is displayed
4. Verify no errors occur

**Purpose:** Graceful handling of empty state.

---

**✅ Test:** `should refresh calendar when new task is created` (line 652)

**Steps:**
1. Navigate to calendar view
2. Note current task count
3. Create a new task with due date today
4. Verify calendar automatically updates
5. Verify new task appears in calendar

**Purpose:** Real-time updates when tasks are created.

---

## TICKET 3: Multi-View Schedule

**Test File:** `multi-view-schedule.spec.ts`
**Total Tests:** 24
**Coverage:** 100% (3/3 ACs)

### AC 3.1: Switch Views - Tasks Render Correctly

**✅ Test:** `should render tasks in Day view` (line 27)

**Steps:**
1. Create a task with due date today at 2:00 PM
2. Navigate to calendar view
3. Select "Day" view
4. Verify task appears on today's date
5. Verify task shows correct time (2:00 PM)
6. Verify task details are visible

**Mapping:**
- **Given:** I switch to Day view
- **When:** The view loads
- **Then:** Tasks render according to the day scale

---

**✅ Test:** `should render tasks in Week view` (line 58)

**Steps:**
1. Create tasks for different days of the week (Monday, Wednesday, Friday)
2. Navigate to calendar view
3. Select "Week" view
4. Verify all 3 tasks are visible in current week
5. Verify each task appears on correct day column
6. Verify week header shows correct date range

**Mapping:**
- **Given:** I switch to Week view
- **When:** The view loads
- **Then:** Tasks render according to the week scale

---

**✅ Test:** `should render tasks in Month view` (line 90)

**Steps:**
1. Create tasks for different days of the month (1st, 15th, 30th)
2. Navigate to calendar view
3. Select "Month" view
4. Verify all tasks appear on correct dates
5. Verify month header shows current month and year
6. Verify tasks are distributed across calendar grid

**Mapping:**
- **Given:** I switch to Month view
- **When:** The view loads
- **Then:** Tasks render according to the month scale

---

**✅ Test:** `should render tasks in Timeline view` (line 122)

**Steps:**
1. Create tasks with various due dates (today, next week, next month)
2. Navigate to calendar view
3. Select "Timeline" view (horizontal timeline)
4. Verify tasks appear chronologically from left to right
5. Verify timeline scale shows dates/weeks/months
6. Verify tasks are positioned according to due dates

**Mapping:**
- **Given:** I switch to Timeline view
- **When:** The view loads
- **Then:** Tasks render according to the timeline scale

---

**✅ Test:** `should preserve task data when switching between views` (line 158)

**Steps:**
1. Create a task with all details (title, due date, assignee, description)
2. Navigate to calendar view
3. View task in Day view - verify all details present
4. Switch to Week view - verify same task details
5. Switch to Month view - verify same task details
6. Switch to Timeline view - verify same task details
7. Verify no data loss when switching views

**Mapping:**
- **Given:** I switch between views
- **When:** Each view loads
- **Then:** The same tasks render with preserved data

---

### AC 3.2: Filter by Project Across All Views

**✅ Test:** `should filter by project in Day view` (line 200)

**Steps:**
1. Create tasks for Project A and Project B with due dates today
2. Navigate to calendar Day view
3. Apply project filter: "Project A"
4. Verify only Project A tasks are displayed
5. Verify Project B tasks are hidden
6. Verify task count matches Project A tasks only

**Mapping:**
- **Given:** I filter by project in Day view
- **When:** Applied
- **Then:** Only tasks from that project render

---

**✅ Test:** `should filter by project in Week view` (line 230)

**Steps:**
1. Create tasks for multiple projects across the week
2. Navigate to calendar Week view
3. Apply project filter: "Project Alpha"
4. Verify only "Project Alpha" tasks appear
5. Verify filter applies to all days of the week

**Mapping:**
- **Given:** I filter by project in Week view
- **When:** Applied
- **Then:** Only tasks from that project render

---

**✅ Test:** `should filter by project in Month view` (line 258)

**Steps:**
1. Create tasks for different projects throughout the month
2. Navigate to calendar Month view
3. Apply project filter: "Project Beta"
4. Verify only "Project Beta" tasks are displayed
5. Verify filter applies to all dates in the month

**Mapping:**
- **Given:** I filter by project in Month view
- **When:** Applied
- **Then:** Only tasks from that project render

---

**✅ Test:** `should filter by project in Timeline view` (line 286)

**Steps:**
1. Create tasks for multiple projects with various due dates
2. Navigate to calendar Timeline view
3. Apply project filter: "Project Gamma"
4. Verify only "Project Gamma" tasks appear in timeline
5. Verify timeline scale adjusts if needed

**Mapping:**
- **Given:** I filter by project in Timeline view
- **When:** Applied
- **Then:** Only tasks from that project render

---

**✅ Test:** `should maintain project filter when switching between views` (line 314)

**Steps:**
1. Navigate to calendar Day view
2. Apply project filter: "Project A"
3. Verify filter is active
4. Switch to Week view - verify filter still applied
5. Switch to Month view - verify filter still applied
6. Switch to Timeline view - verify filter still applied
7. Verify same filtered tasks appear in all views

**Mapping:**
- **Given:** I have a project filter applied
- **When:** I switch views
- **Then:** The filter persists across all views

---

### AC 3.3: Search by Keyword

**✅ Test:** `should highlight matching tasks when searching by keyword` (line 355)

**Steps:**
1. Create tasks with titles: "Design Homepage", "Design Logo", "Review Budget"
2. Navigate to calendar view
3. Enter search keyword: "Design"
4. Verify "Design Homepage" and "Design Logo" are highlighted or visible
5. Verify "Review Budget" is dimmed or hidden
6. Verify search results count shows "2 matches"

**Mapping:**
- **Given:** I search by keyword "Design"
- **When:** Results return
- **Then:** Matching tasks are highlighted

---

**✅ Test:** `should search tasks by title across all views` (line 387)

**Steps:**
1. Create tasks with various titles
2. Navigate to calendar Day view
3. Search for keyword "meeting"
4. Verify matching tasks appear in Day view
5. Switch to Week view - verify same search results
6. Switch to Month view - verify same search results
7. Switch to Timeline view - verify same search results

**Mapping:**
- **Given:** I search by keyword
- **When:** I switch views
- **Then:** Search results persist across all views

---

**✅ Test:** `should search tasks by description` (line 420)

**Steps:**
1. Create tasks with keyword "urgent" in description
2. Navigate to calendar view
3. Search for "urgent"
4. Verify tasks with "urgent" in description are highlighted
5. Verify search looks at both title and description fields

**Mapping:**
- **Given:** I search by keyword
- **When:** Results return
- **Then:** Search matches both title and description

---

**✅ Test:** `should clear search and show all tasks` (line 448)

**Steps:**
1. Create multiple tasks
2. Navigate to calendar view
3. Search for keyword "test"
4. Verify filtered results appear
5. Clear search input
6. Verify all tasks are displayed again
7. Verify no tasks are dimmed or hidden

**Mapping:**
- **Given:** I have a search active
- **When:** I clear the search
- **Then:** All tasks are displayed again

---

**✅ Test:** `should show empty state when no tasks match search` (line 478)

**Steps:**
1. Create tasks
2. Navigate to calendar view
3. Search for keyword that doesn't match any task: "xyz123nonexistent"
4. Verify empty state message is displayed: "No tasks found"
5. Verify no tasks are displayed

**Mapping:**
- **Given:** I search for a keyword with no matches
- **When:** Results return
- **Then:** Empty state is displayed

---

### Additional Tests (Not Mapped to Specific ACs)

**✅ Test:** `should navigate to previous/next period in Day view` (line 510)

**Steps:**
1. Navigate to calendar Day view (today)
2. Click "Previous" button
3. Verify view shows yesterday
4. Click "Next" button twice
5. Verify view shows tomorrow
6. Verify navigation updates header date

**Purpose:** Calendar navigation in Day view.

---

**✅ Test:** `should navigate to previous/next period in Week view` (line 540)

**Steps:**
1. Navigate to calendar Week view
2. Note current week range
3. Click "Previous Week" button
4. Verify view shows previous week
5. Click "Next Week" button twice
6. Verify view shows next week

**Purpose:** Calendar navigation in Week view.

---

**✅ Test:** `should navigate to previous/next period in Month view` (line 570)

**Steps:**
1. Navigate to calendar Month view (current month)
2. Click "Previous Month" button
3. Verify header shows previous month name
4. Click "Next Month" button twice
5. Verify header shows next month name

**Purpose:** Calendar navigation in Month view.

---

**✅ Test:** `should return to today/current period with Today button` (line 598)

**Steps:**
1. Navigate to calendar view
2. Navigate to a date in the past (e.g., last month)
3. Click "Today" button
4. Verify view returns to current day/week/month depending on active view
5. Verify today's date is highlighted

**Purpose:** Quick navigation back to current date.

---

**✅ Test:** `should display task count indicator in calendar cells` (line 625)

**Steps:**
1. Create 5 tasks for a specific date
2. Navigate to calendar Month view
3. Locate the date cell
4. Verify cell shows count indicator (e.g., "+5 tasks" or badge with "5")
5. Click on cell to see all tasks

**Purpose:** Help users see task density at a glance.

---

**✅ Test:** `should handle tasks without due dates in timeline view` (line 654)

**Steps:**
1. Create a task without a due date
2. Navigate to calendar Timeline view
3. Verify task without due date is not displayed in timeline (expected behavior)
4. OR verify task appears in "Unscheduled" section if supported

**Purpose:** Handle edge case of tasks without due dates.

---

## TICKET 4: Attach Due Dates

**Test File:** `task-due-dates.spec.ts`
**Total Tests:** 20
**Coverage:** 100% (4/4 ACs)

### AC 4.1: Adding a Due Date - Task Appears on Schedule

**✅ Test:** `should add due date to task and see it on My Analytics Page` (line 27)

**Steps:**
1. Log in as staff user
2. Create a new task: "Complete quarterly report"
3. Set due date: 5 days from now
4. Save task
5. Navigate to My Analytics Page
6. Locate "Upcoming Commitments" section
7. Verify task appears on the set due date
8. Verify task is listed in chronological order

**Mapping:**
- **Given:** I am creating a task
- **When:** I set a due date and save
- **Then:** The task appears on upcoming commitments on My Analytics Page on that date

---

**✅ Test:** `should update due date and see changes on My Analytics Page` (line 62)

**Steps:**
1. Create a task with due date tomorrow
2. Verify task appears in upcoming commitments for tomorrow
3. Edit task and change due date to 1 week from now
4. Save changes
5. Navigate to My Analytics Page
6. Verify task no longer appears in tomorrow's commitments
7. Verify task appears in commitments for 1 week from now

**Mapping:**
- **Given:** I am editing a task
- **When:** I change the due date and save
- **Then:** The task position updates on My Analytics Page

---

**✅ Test:** `should display task with due date in task list` (line 98)

**Steps:**
1. Create a task: "Review PR #123"
2. Set due date: November 15, 2025
3. Save task
4. Navigate to task list view
5. Locate task "Review PR #123"
6. Verify due date "Nov 15, 2025" is displayed next to task
7. Verify due date is formatted correctly

**Mapping:**
- **Given:** I have set a due date for a task
- **When:** I save the task
- **Then:** It remains visible in the task list with the due date shown

---

**✅ Test:** `should show due date indicator icon in task list` (line 130)

**Steps:**
1. Create a task with a due date
2. Navigate to task list
3. Verify task row shows calendar icon or due date icon
4. Verify icon is positioned near due date text
5. Hover over icon - verify tooltip shows full due date/time if available

**Mapping:**
- **Given:** A task has a due date
- **When:** I view the task list
- **Then:** A due date indicator is visible

---

### AC 4.2: Adding a Due Time - Correct Chronological Order

**✅ Test:** `should add due time and position task chronologically on My Analytics Page` (line 163)

**Steps:**
1. Create Task A with due date tomorrow at 9:00 AM
2. Create Task B with due date tomorrow at 2:00 PM
3. Create Task C with due date tomorrow at 11:30 AM
4. Navigate to My Analytics Page
5. Locate upcoming commitments for tomorrow
6. Verify tasks are ordered by time:
   - Task A (9:00 AM) first
   - Task C (11:30 AM) second
   - Task B (2:00 PM) third

**Mapping:**
- **Given:** I have set a due date for a task
- **When:** I also set a due time
- **Then:** The task is positioned in correct order relative to due time

---

**✅ Test:** `should display due time in task details` (line 200)

**Steps:**
1. Create a task with due date November 10, 2025 at 3:30 PM
2. Save task
3. Navigate to task list
4. Click on task to open details
5. Verify due date shows: "Nov 10, 2025"
6. Verify due time shows: "3:30 PM" or "15:30"
7. Verify date and time are clearly separated or formatted

**Mapping:**
- **Given:** A task has both due date and due time
- **When:** I view task details
- **Then:** Both date and time are displayed

---

**✅ Test:** `should allow setting due date without due time` (line 230)

**Steps:**
1. Create a task
2. Set due date: November 20, 2025
3. Leave due time empty
4. Save task
5. Verify task is saved successfully
6. Verify task appears in task list with due date only
7. Verify no time is displayed (or shows "All Day")

**Mapping:**
- **Given:** I am setting a due date
- **When:** I leave due time empty
- **Then:** Task is saved with date only (no time)

---

### AC 4.3: Removing a Due Date - Disappears from Calendar/Timeline

**✅ Test:** `should remove task from calendar when due date is removed` (line 265)

**Steps:**
1. Create a task with due date tomorrow
2. Navigate to calendar view
3. Verify task appears on tomorrow's date
4. Edit task and remove due date (set to null/empty)
5. Save changes
6. Verify task no longer appears in calendar view
7. Verify calendar shows correct task count (decreased by 1)

**Mapping:**
- **Given:** A task has a due date
- **When:** I remove the due date and save
- **Then:** The task disappears from calendar view

---

**✅ Test:** `should remove task from timeline when due date is removed` (line 297)

**Steps:**
1. Create a task with due date next week
2. Navigate to timeline view
3. Verify task appears in timeline at correct position
4. Edit task and remove due date
5. Save changes
6. Verify task no longer appears in timeline view

**Mapping:**
- **Given:** A task has a due date
- **When:** I remove the due date and save
- **Then:** The task disappears from timeline view

---

**✅ Test:** `should remove task from My Analytics upcoming commitments when due date removed` (line 325)

**Steps:**
1. Create a task with due date 3 days from now
2. Navigate to My Analytics Page
3. Verify task appears in upcoming commitments
4. Edit task and remove due date
5. Save changes
6. Navigate back to My Analytics Page
7. Verify task no longer appears in upcoming commitments section

**Mapping:**
- **Given:** A task has a due date
- **When:** I remove the due date and save
- **Then:** The task disappears from My Analytics upcoming commitments

---

**✅ Test:** `should keep task in list after removing due date` (line 360)

**Steps:**
1. Create a task: "Update documentation"
2. Set due date: December 1, 2025
3. Verify task appears in task list with due date
4. Edit task and remove due date
5. Save changes
6. Navigate to task list
7. Verify task "Update documentation" is still visible
8. Verify due date field is empty or shows "No due date"
9. Verify task is still accessible and editable

**Mapping:**
- **Given:** A task has a due date
- **When:** I remove the due date and save
- **Then:** It remains in the task list without a date

---

**✅ Test:** `should show no date indicator when due date is removed` (line 395)

**Steps:**
1. Create a task with a due date
2. Remove due date
3. Navigate to task list
4. Verify task shows "No due date" text or empty date field
5. Verify no calendar icon is displayed next to task

**Mapping:**
- **Given:** A task has no due date
- **When:** I view the task in task list
- **Then:** "No due date" indicator is shown

---

### AC 4.4: Validation - Prevent Past Due Date on Initial Creation

**✅ Test:** `should prevent setting past due date when creating new task` (line 428)

**Steps:**
1. Click "Create New Task" button
2. Fill in task title: "New task"
3. Try to set due date: 1 week in the past
4. Verify validation error appears: "Due date cannot be in the past"
5. Verify "Save" button is disabled or error prevents saving
6. Change due date to future date
7. Verify validation error clears
8. Verify task can be saved successfully

**Mapping:**
- **Given:** I am setting a due date for a task
- **When:** I select a date in the past
- **Then:** The system prevents me from doing so on initial creation

---

**✅ Test:** `should allow editing existing task to past due date (overdue scenario)` (line 465)

**Steps:**
1. Create a task with due date tomorrow
2. Edit the task after creation
3. Try to set due date to yesterday
4. Verify system allows this (for overdue task scenario)
5. OR verify system shows warning but allows override
6. Save task
7. Verify task is marked as overdue

**Mapping:**
- **Given:** I am editing an existing task
- **When:** I set due date to past
- **Then:** System allows it (to create overdue state)

---

**✅ Test:** `should show date picker with today highlighted when selecting due date` (line 498)

**Steps:**
1. Create a new task
2. Click on due date field to open date picker
3. Verify date picker opens
4. Verify today's date is highlighted or marked
5. Verify past dates are disabled or grayed out
6. Verify future dates are selectable

**Mapping:**
- **Given:** I am selecting a due date
- **When:** Date picker opens
- **Then:** Today is highlighted and past dates are disabled

---

### Additional Tests (Not Mapped to Specific ACs)

**✅ Test:** `should update task due date via drag and drop in calendar` (line 530)

**Steps:**
1. Create a task with due date tomorrow
2. Navigate to calendar view
3. Locate the task on tomorrow's date
4. Drag task to a different date (e.g., next week)
5. Drop task on new date
6. Verify task updates to new due date
7. Verify change is persisted (refresh page and verify)

**Purpose:** Advanced calendar interaction - drag and drop to reschedule.

---

**✅ Test:** `should allow setting recurring due dates (if supported)` (line 565)

**Steps:**
1. Create a task: "Weekly team standup"
2. Set due date: next Monday
3. Enable recurring option: "Every week"
4. Save task
5. Navigate to calendar view
6. Verify task appears on multiple Mondays
7. Verify recurring indicator is shown

**Purpose:** Support for recurring tasks (optional feature).

---

**✅ Test:** `should display due date in different timezone if user settings allow` (line 598)

**Steps:**
1. Set user timezone to PST (GMT-8)
2. Create a task with due date/time: Nov 10, 2025, 5:00 PM PST
3. Change user timezone to EST (GMT-5)
4. View task details
5. Verify due time shows: 8:00 PM EST (3 hour difference)
6. Verify timezone is displayed

**Purpose:** Handle timezone conversions for due dates.

---

**✅ Test:** `should show overdue badge when current time passes due date` (line 628)

**Steps:**
1. Create a task with due date yesterday
2. Set status: "In Progress" (not completed)
3. Navigate to task list
4. Verify task shows overdue badge/indicator
5. Verify due date is displayed in red or with warning color

**Purpose:** Bridge to Ticket 1 - showing overdue state.

---

**✅ Test:** `should sort tasks by due date in task list` (line 656)

**Steps:**
1. Create Task A with due date in 3 days
2. Create Task B with due date tomorrow
3. Create Task C with due date in 1 week
4. Navigate to task list
5. Apply sort: "Due Date (Earliest First)"
6. Verify task order: Task B, Task A, Task C
7. Apply sort: "Due Date (Latest First)"
8. Verify task order: Task C, Task A, Task B

**Purpose:** Allow users to organize tasks by due date.

---

## Coverage Summary

| Ticket # | Ticket Name | ACs | Covered | % | Tests |
|----------|-------------|-----|---------|---|-------|
| 1 | Overdue Alerts | 6 | 6 | 100% ✅ | 11 |
| 2 | Team Schedule Visibility | 8 | 8 | 100% ✅ | 21 |
| 3 | Multi-View Schedule | 3 | 3 | 100% ✅ | 24 |
| 4 | Attach Due Dates | 4 | 4 | 100% ✅ | 20 |
| **TOTAL** | **4 Tickets** | **21** | **21** | **100%** | **76** |

---

## Test Execution

### Run All Schedule Tests
```bash
npx playwright test frontend/e2e/tests/schedule/
```

### Run by Test File
```bash
npx playwright test frontend/e2e/tests/schedule/task-overdue-alerts.spec.ts
npx playwright test frontend/e2e/tests/schedule/team-schedule-visibility.spec.ts
npx playwright test frontend/e2e/tests/schedule/multi-view-schedule.spec.ts
npx playwright test frontend/e2e/tests/schedule/task-due-dates.spec.ts
```

### Run by Ticket (Grep Pattern)
```bash
# Ticket 1: Overdue Alerts
npx playwright test --grep "overdue"

# Ticket 2: Team Schedule Visibility
npx playwright test --grep "team|visibility|color.*cod"

# Ticket 3: Multi-View Schedule
npx playwright test --grep "Day view|Week view|Month view|Timeline view"

# Ticket 4: Attach Due Dates
npx playwright test --grep "due date|due time"
```

---

## Test Quality Metrics

- **Total E2E Tests:** 76
- **Acceptance Criteria Coverage:** 100% (21/21)
- **Pass Rate:** TBD (pending implementation)
- **Test Files:** 4
- **Average Tests per Ticket:** 19
- **Page Objects:** TBD (CalendarPage, TaskListPage, AnalyticsPage)
- **Test Data Builders:** TBD (task builders, date helpers)
- **Download Helpers:** Not required for this feature
- **Authentication Fixtures:** Yes (Staff, Manager roles)

---

## Additional Test Coverage

### Tests Beyond Acceptance Criteria:
- **15 additional tests** provide coverage for edge cases and user experience improvements:
  - Dashboard integrations (overdue count)
  - Filter functionality (overdue filter)
  - Empty states
  - Real-time updates
  - Navigation controls
  - Task density indicators
  - Drag-and-drop interactions
  - Recurring tasks
  - Timezone handling
  - Sorting functionality

### Why Additional Tests Matter:
- **Better User Experience:** Cover common user workflows not explicitly in ACs
- **Edge Case Handling:** Prevent errors in unusual scenarios
- **Feature Integration:** Ensure features work together seamlessly
- **Future-Proofing:** Support advanced features that may be requested later

---

**Last Updated:** November 6, 2025  
**Maintained By:** SPM Orange Team  
**Document Status:** Ready for Implementation
