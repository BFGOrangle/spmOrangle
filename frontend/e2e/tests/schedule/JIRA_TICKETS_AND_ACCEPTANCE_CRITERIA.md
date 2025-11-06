# Schedule & Task Management Feature - Jira Tickets & Acceptance Criteria

**Total Tests:** 76
**Test Files:** 4
**Total Tickets:** 4
**Last Updated:** November 6, 2025

---

## TICKET 1: Overdue Alerts

### User Story
As a staff user,
I want to be alerted when a task is overdue,
so that I know which tasks require higher priority and immediate attention.

### Acceptance Criteria

**AC 1.1: Marking a Task as Overdue - System Automatically Marks**
```
Given a task has a due date/time
When the due date/time passes and the task is not marked complete
Then the system marks the task as Overdue
```

**AC 1.2: Visual Indicator Across All Views**
```
Given a task is overdue
When I view the task in list, board, or calendar views
Then the task is highlighted in red (or another visual indicator) across all views
```

**AC 1.3: Clearing Overdue State on Completion**
```
Given a task is overdue
When I mark the task as Done
Then the overdue status clears
And the task no longer displays overdue indicators
```

**AC 1.4: Completion Timestamp in Audit Log**
```
Given a task is overdue
When I mark the task as Done
Then the system records the completion timestamp in the audit log
And the timestamp shows when the task was actually completed
```

**AC 1.5: Overdue Reminder Email Sent**
```
Given a task has a due date/time
When it reaches 1 day after due date
Then the system sends me a reminder email notifying that the task is overdue
```

**AC 1.6: Email Contains Required Information**
```
Given an overdue reminder email is sent
When I receive the email
Then the email should include:
  - Task title and due date/time
  - Current status (e.g., "In Progress")
  - A link to open the task in the app
```

---

## TICKET 2: Team Schedule Visibility

### User Story
As a staff or manager,
I want to view a project calendar that shows both my own tasks and my colleagues' tasks (within my visible departments),
so that I can understand overall project schedules, track dependencies, and better coordinate work.

### Acceptance Criteria

### Visibility Rules

**AC 2.1: See Only Accessible Project Tasks**
```
Given I have access to specific departments or reporting departments
When I open the Calendar and filter by Project's tasks
Then I only see tasks from that project
And tasks are limited to departments within my visibility scope
```

**AC 2.2: Hide Tasks from Restricted Departments**
```
Given a task belongs to a department outside my visibility scope
When I view the Calendar
Then I will not be able to see the project and its related task
And the task count excludes tasks from restricted departments
```

### Display Rules

**AC 2.3: Color-Code Own vs Colleagues' Tasks**
```
Given I open the Calendar
When I view it
Then my own assigned tasks appear in one color (e.g., blue)
And tasks assigned to my colleagues appear in a different color (e.g., gray or purple)
And the color coding is consistent across all calendar dates
```

**AC 2.4: Task Detail Information on Click**
```
Given I click on a task in the Calendar
When I view its details
Then I can see task name, due date/time, assignee name, and department
And all information is clearly formatted and readable
```

**AC 2.5: Filter by Project, Department, or Date Range**
```
Given I filter by project, department, or date range
When I apply the filter
Then only tasks matching those filters are displayed on the calendar
And the task count updates to reflect filtered results
And I can apply multiple filters simultaneously
```

### Permissions & Interaction

**AC 2.6: Edit Tasks with Edit Permissions**
```
Given I have edit permissions (e.g., manager of department)
When I click on a task assigned to my department
Then I can open and edit it (CRUD access)
And changes are saved successfully
```

**AC 2.7: View-Only for Tasks Without Edit Permissions**
```
Given I do not have edit permissions
When I click on a task assigned to another colleague
Then I can view but not modify it
And edit controls are disabled or hidden
```

### Calendar Integration

**AC 2.8: Switch Between My Tasks and Project Tasks Views**
```
Given I switch between "My Tasks" and "Project Tasks" views
When I choose "Project Tasks"
Then the system displays all visible project tasks (mine + colleagues)
And tasks are color-coded according to department visibility
And filters are maintained when switching views
```

---

## TICKET 3: Multi-View Schedule

### User Story
As a staff user,
I want calendar views (Day/Week/Month) and a horizontal timeline,
so that I can plan effectively.

### Acceptance Criteria

**AC 3.1: Switch Views - Tasks Render Correctly**
```
Given I switch views (Day/Week/Month/Timeline)
When the view loads
Then the same tasks render according to the selected scale
And task data is preserved across view switches
And tasks are positioned correctly based on their due dates
```

**AC 3.2: Filter by Project Across All Views**
```
Given I filter by project
When applied
Then only tasks from that project render across all views
And the filter persists when switching between views
And the task count updates accordingly
```

**AC 3.3: Search by Keyword**
```
Given I search by keyword
When results return
Then matching tasks are highlighted and non-matching are dimmed/hidden
And the search looks at both task title and description
And I can clear the search to show all tasks again
```

---

## TICKET 4: Attach Due Dates

### User Story
As a staff user,
I want to attach a due date (and optional time) to a task,
so that it appears on my schedule and I can plan work effectively.

### Acceptance Criteria

**AC 4.1: Adding a Due Date - Task Appears on Schedule**
```
Given I am creating or editing a task
When I set a due date and save
Then the task appears on the upcoming commitment on My Analytics Page on that date
And the task remains visible in the task list with the due date shown
```

**AC 4.2: Adding a Due Time - Correct Chronological Order**
```
Given I have set a due date for a task
When I also set a due time
Then in upcoming commitment on My Analytics Page, the task is positioned in the correct order relative to due date
And tasks are sorted by time within the same day
```

**AC 4.3: Removing a Due Date - Disappears from Calendar/Timeline**
```
Given a task has a due date
When I remove the due date and save
Then the task disappears from all calendar/timeline views
And the task remains in the task list without a date
```

**AC 4.4: Validation - Prevent Past Due Date on Initial Creation**
```
Given I am setting a due date for a task
When I select a date in the past
Then the system prevents me from doing so on initial creation
And displays a validation error message
And the Save button is disabled until a valid date is selected
```

---

## Summary

| Ticket # | Ticket Name | Total ACs | Test Priority |
|----------|-------------|-----------|---------------|
| 1 | Overdue Alerts | 6 | P0 (Critical) |
| 2 | Team Schedule Visibility | 8 | P0 (Critical) |
| 3 | Multi-View Schedule | 3 | P1 (High) |
| 4 | Attach Due Dates | 4 | P0 (Critical) |
| **TOTAL** | **4 Tickets** | **21** | - |

---

## Copy-Paste Instructions for Jira

### For Each Ticket:

1. **Create a new Story** in Jira
2. **Copy the User Story** section into the Description field
3. **Add each Acceptance Criteria** as separate items in Jira's AC section
4. **Use the GWT format blocks** directly (already formatted)
5. **Add labels:** `schedule`, `calendar`, `e2e-tested`, `frontend`
6. **Set priority** based on table above

### Example Jira Format:

```
Title: Overdue Alerts

Story:
As a staff user,
I want to be alerted when a task is overdue,
so that I know which tasks require higher priority and immediate attention.

Acceptance Criteria:

1. Marking a Task as Overdue - System Automatically Marks
Given a task has a due date/time
When the due date/time passes and the task is not marked complete
Then the system marks the task as Overdue

2. Visual Indicator Across All Views
Given a task is overdue
When I view the task in list, board, or calendar views
Then the task is highlighted in red (or another visual indicator) across all views

[... continue with remaining ACs ...]

Labels: schedule, calendar, overdue, e2e-tested, frontend
Priority: P0 (Critical)
Story Points: 8
```

---

## Story Points Estimation

- **Ticket 1** (Overdue Alerts): 8 points
  - Backend logic for overdue detection
  - Email notification system integration
  - Visual indicators across multiple views
  - Audit logging

- **Ticket 2** (Team Schedule Visibility): 13 points
  - Complex permission logic
  - Multiple view types (list, board, calendar)
  - Color coding system
  - Filter system with multiple criteria
  - View switching with state management

- **Ticket 3** (Multi-View Schedule): 13 points
  - Four different calendar views
  - Timeline component implementation
  - Search/filter functionality
  - Navigation between time periods
  - Responsive layout for all views

- **Ticket 4** (Attach Due Dates): 8 points
  - Date/time picker integration
  - Validation logic
  - Analytics page integration
  - Calendar synchronization
  - Timezone handling

**Total Estimated Story Points:** 42 points

---

## Dependencies

### Ticket Dependencies:
- **Ticket 1** depends on **Ticket 4** (need due dates before detecting overdue)
- **Ticket 2** depends on **Ticket 4** (calendar needs tasks with due dates)
- **Ticket 3** depends on **Ticket 4** (all views require due dates)

### Recommended Implementation Order:
1. **Ticket 4** - Attach Due Dates (Foundation)
2. **Ticket 1** - Overdue Alerts (Builds on due dates)
3. **Ticket 3** - Multi-View Schedule (Calendar views)
4. **Ticket 2** - Team Schedule Visibility (Team features)

---

## Technical Implementation Notes

### Backend API Endpoints Required:

**For Ticket 1 (Overdue Alerts):**
- `GET /api/tasks/overdue` - Get all overdue tasks
- `POST /api/tasks/{id}/complete` - Mark task as done (with audit logging)
- `POST /api/notifications/overdue-reminder` - Trigger email reminder

**For Ticket 2 (Team Schedule Visibility):**
- `GET /api/calendar/team-tasks` - Get team tasks with visibility filtering
- `GET /api/tasks/by-department/{departmentId}` - Get department tasks
- `GET /api/tasks/by-project/{projectId}` - Get project tasks
- `GET /api/users/team-members` - Get visible team members

**For Ticket 3 (Multi-View Schedule):**
- `GET /api/calendar/tasks?view={day|week|month|timeline}` - Get tasks for specific view
- `GET /api/tasks/search?q={keyword}` - Search tasks by keyword

**For Ticket 4 (Attach Due Dates):**
- `POST /api/tasks` - Create task with due date/time
- `PUT /api/tasks/{id}/due-date` - Update task due date
- `DELETE /api/tasks/{id}/due-date` - Remove due date
- `GET /api/analytics/upcoming-commitments` - Get upcoming tasks for analytics page

### Frontend Components to Create:

**Ticket 1:**
- `OverdueBadge.tsx` - Red indicator for overdue tasks
- `OverdueTaskList.tsx` - List view with overdue highlighting
- `OverdueEmailTemplate.tsx` - Email template component

**Ticket 2:**
- `TeamCalendar.tsx` - Calendar with team visibility
- `TaskColorLegend.tsx` - Legend showing color coding
- `CalendarFilterPanel.tsx` - Filter controls
- `TaskViewSwitcher.tsx` - Switch between My Tasks / Project Tasks

**Ticket 3:**
- `CalendarDayView.tsx` - Day view component
- `CalendarWeekView.tsx` - Week view component
- `CalendarMonthView.tsx` - Month view component
- `TimelineView.tsx` - Horizontal timeline component
- `CalendarSearchBar.tsx` - Search functionality

**Ticket 4:**
- `DueDatePicker.tsx` - Date/time picker component
- `DueDateDisplay.tsx` - Display component for due dates
- `UpcomingCommitments.tsx` - Analytics page widget
- `DueDateValidation.tsx` - Validation logic component

---

## Testing Strategy

### Unit Tests:
- Date validation logic
- Overdue detection algorithm
- Permission checking functions
- Color coding logic
- Search/filter functions

### Integration Tests:
- API endpoint integration
- Email service integration
- Calendar library integration
- Authentication/authorization flow

### E2E Tests:
- 76 comprehensive E2E tests (see E2E_TEST_TICKETS.md)
- Cover all user workflows
- Test cross-browser compatibility
- Test mobile responsiveness

### Performance Tests:
- Calendar rendering with 100+ tasks
- Filter performance with large datasets
- Search responsiveness
- Timeline rendering performance

---

## Acceptance Checklist

Before marking a ticket as "Done", verify:

- [ ] All acceptance criteria are met
- [ ] E2E tests are passing
- [ ] Unit tests are passing
- [ ] Code review completed
- [ ] Documentation updated
- [ ] API endpoints tested
- [ ] UI/UX reviewed by design team
- [ ] Accessibility (a11y) requirements met
- [ ] Mobile responsiveness verified
- [ ] Performance benchmarks met
- [ ] Security review completed (for email/notifications)

---

**Last Updated:** November 6, 2025  
**Maintained By:** SPM Orange Team  
**Document Version:** 1.0
