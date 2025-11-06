# Schedule & Task Management E2E Tests

This directory contains comprehensive end-to-end tests for the Schedule and Task Management features of the SPM Orange application.

## 📁 Directory Structure

```
schedule/
├── README.md                              # This file
├── E2E_TEST_TICKETS.md                   # Detailed test case documentation
├── JIRA_TICKETS_AND_ACCEPTANCE_CRITERIA.md # Jira tickets and ACs
├── TEST_CASE_MAPPING.md                  # AC to test case mapping
├── task-overdue-alerts.spec.ts           # Overdue alerts tests (pending)
├── team-schedule-visibility.spec.ts      # Team schedule tests (pending)
├── multi-view-schedule.spec.ts           # Calendar views tests (pending)
└── task-due-dates.spec.ts                # Due date management tests (pending)
```

## 📊 Test Coverage Overview

| Ticket # | Feature | Test File | Tests | Status |
|----------|---------|-----------|-------|--------|
| 1 | Overdue Alerts | `task-overdue-alerts.spec.ts` | 11 | ⏳ Pending |
| 2 | Team Schedule Visibility | `team-schedule-visibility.spec.ts` | 21 | ⏳ Pending |
| 3 | Multi-View Schedule | `multi-view-schedule.spec.ts` | 24 | ⏳ Pending |
| 4 | Attach Due Dates | `task-due-dates.spec.ts` | 20 | ⏳ Pending |
| **TOTAL** | **4 Features** | **4 Files** | **76** | **0% Complete** |

## 🎯 Feature Descriptions

### Ticket 1: Overdue Alerts
**User Story:** As a staff user, I want to be alerted when a task is overdue so that I know which tasks require higher priority and immediate attention.

**Key Features:**
- Automatic overdue detection when due date passes
- Red visual indicators across list, board, and calendar views
- Overdue status clears when task is marked complete
- Email reminders 1 day after due date
- Audit log tracking for completed overdue tasks

**Priority:** P0 (Critical)

---

### Ticket 2: Team Schedule Visibility
**User Story:** As a staff or manager, I want to view a project calendar that shows both my own tasks and my colleagues' tasks (within my visible departments), so that I can understand overall project schedules, track dependencies, and better coordinate work.

**Key Features:**
- Department-based visibility rules
- Color-coded tasks (own tasks vs colleagues' tasks)
- Click to view task details (name, due date, assignee, department)
- Filter by project, department, or date range
- Permission-based editing (managers can edit department tasks, staff edit own tasks)
- Switch between "My Tasks" and "Project Tasks" views

**Priority:** P0 (Critical)

---

### Ticket 3: Multi-View Schedule
**User Story:** As a staff user, I want calendar views (Day/Week/Month) and a horizontal timeline so that I can plan effectively.

**Key Features:**
- Four calendar views: Day, Week, Month, Timeline
- Tasks render correctly based on selected view scale
- Project filters apply across all views
- Search by keyword (highlights matching tasks, dims non-matching)
- Navigation controls (previous/next period, today button)
- Responsive layout for all view types

**Priority:** P1 (High)

---

### Ticket 4: Attach Due Dates
**User Story:** As a staff user, I want to attach a due date (and optional time) to a task so that it appears on my schedule and I can plan work effectively.

**Key Features:**
- Add due date and optional due time to tasks
- Tasks appear in "Upcoming Commitments" on My Analytics Page
- Tasks positioned chronologically by due time
- Remove due dates (tasks disappear from calendar but remain in task list)
- Validation: prevent past due dates on task creation
- Due date shown in task list with indicator icon

**Priority:** P0 (Critical)

---

## 🚀 Getting Started

### Prerequisites

```bash
# Install dependencies
npm install

# Install Playwright browsers
npx playwright install --with-deps

# Set up environment variables
cp .env.example .env.test
```

### Environment Variables

```bash
# .env.test
TEST_STAFF_EMAIL=staff@example.com
TEST_STAFF_PASSWORD=password123
TEST_MANAGER_EMAIL=manager@example.com
TEST_MANAGER_PASSWORD=password123
BASE_URL=http://localhost:3000
API_URL=http://localhost:8080
```

### Running Tests

```bash
# Run all schedule tests
npm run test:e2e:schedule

# Run specific test file
npx playwright test task-overdue-alerts.spec.ts
npx playwright test team-schedule-visibility.spec.ts
npx playwright test multi-view-schedule.spec.ts
npx playwright test task-due-dates.spec.ts

# Run with UI mode (recommended for development)
npx playwright test --ui

# Run specific test
npx playwright test task-overdue-alerts.spec.ts:27

# Run with grep pattern
npx playwright test --grep "overdue"
npx playwright test --grep "calendar.*view"

# Generate HTML report
npx playwright test --reporter=html
npx playwright show-report
```

---

## 📝 Documentation Files

### 1. E2E_TEST_TICKETS.md
**Comprehensive test case documentation following the format of existing reports tests.**

Contains:
- User stories for each ticket
- Detailed acceptance criteria
- Test case descriptions with line numbers
- Test steps for each scenario
- Priority levels (P0, P1)
- Test execution notes
- Best practices and setup instructions

**Use this file for:** Understanding what tests need to be implemented and how they should work.

---

### 2. JIRA_TICKETS_AND_ACCEPTANCE_CRITERIA.md
**Jira-ready format for tickets and acceptance criteria.**

Contains:
- User stories in Jira format
- Acceptance criteria using Given-When-Then (GWT) format
- Copy-paste instructions for Jira
- Story point estimations
- Dependencies between tickets
- Technical implementation notes
- API endpoints required
- Frontend components to create
- Testing strategy

**Use this file for:** Creating Jira tickets and planning implementation.

---

### 3. TEST_CASE_MAPPING.md
**Detailed mapping between acceptance criteria and E2E tests.**

Contains:
- Every acceptance criteria mapped to specific tests
- Test steps for each test case
- Explicit mapping showing coverage
- Additional tests beyond ACs
- Test execution commands
- Coverage statistics (100% AC coverage)

**Use this file for:** Verifying test coverage and understanding which tests validate which ACs.

---

## 🏗️ Implementation Guide

### Recommended Implementation Order

1. **Ticket 4: Attach Due Dates** (Foundation)
   - Implement due date/time picker component
   - Add due date fields to task model
   - Update My Analytics Page with upcoming commitments
   - Implement validation logic
   - **Tests to implement:** 20 tests in `task-due-dates.spec.ts`

2. **Ticket 1: Overdue Alerts** (Builds on due dates)
   - Implement overdue detection logic
   - Add red visual indicators across views
   - Set up email notification system
   - Add audit logging
   - **Tests to implement:** 11 tests in `task-overdue-alerts.spec.ts`

3. **Ticket 3: Multi-View Schedule** (Calendar infrastructure)
   - Implement calendar views (Day, Week, Month, Timeline)
   - Add view switching controls
   - Implement search and filter functionality
   - Add navigation controls
   - **Tests to implement:** 24 tests in `multi-view-schedule.spec.ts`

4. **Ticket 2: Team Schedule Visibility** (Team features)
   - Implement visibility rules based on departments
   - Add color coding for own vs colleagues' tasks
   - Implement permission-based editing
   - Add My Tasks / Project Tasks toggle
   - **Tests to implement:** 21 tests in `team-schedule-visibility.spec.ts`

### Why This Order?

- **Dependencies:** Each ticket builds on the previous one
- **Risk Reduction:** Core functionality (due dates) implemented first
- **Incremental Value:** Each ticket delivers user value independently
- **Testing:** Easier to test incrementally as features are built

---

## 🧪 Test Implementation Guidelines

### Page Object Model (POM)

Create page objects for each major view:

```typescript
// fixtures/pages/calendar-page.ts
export class CalendarPage {
  constructor(private page: Page) {}
  
  async navigate() { /* ... */ }
  async selectView(view: 'day' | 'week' | 'month' | 'timeline') { /* ... */ }
  async filterByProject(projectName: string) { /* ... */ }
  async getTaskByTitle(title: string) { /* ... */ }
  async assertTaskIsOverdue(taskTitle: string) { /* ... */ }
}

// fixtures/pages/task-list-page.ts
export class TaskListPage {
  constructor(private page: Page) {}
  
  async navigate() { /* ... */ }
  async createTask(task: TaskData) { /* ... */ }
  async updateTask(taskId: string, updates: Partial<TaskData>) { /* ... */ }
  async assertTaskHasDueDate(taskTitle: string, expectedDate: string) { /* ... */ }
}

// fixtures/pages/analytics-page.ts
export class AnalyticsPage {
  constructor(private page: Page) {}
  
  async navigate() { /* ... */ }
  async getUpcomingCommitments() { /* ... */ }
  async assertTaskInUpcomingCommitments(taskTitle: string, date: string) { /* ... */ }
}
```

### Test Data Builders

Create fluent API for test data:

```typescript
// utils/test-data.ts
export function createTask(overrides?: Partial<TaskData>): TaskData {
  return {
    title: 'Test Task',
    description: 'Test description',
    status: 'TODO',
    ...overrides
  };
}

export function createOverdueTask(overrides?: Partial<TaskData>): TaskData {
  return createTask({
    dueDate: subDays(new Date(), 2), // 2 days ago
    status: 'IN_PROGRESS',
    ...overrides
  });
}

export function createTaskWithDueDate(daysFromNow: number): TaskData {
  return createTask({
    dueDate: addDays(new Date(), daysFromNow)
  });
}
```

### Custom Assertions

Create reusable assertion helpers:

```typescript
// utils/assertions.ts
export async function assertTaskIsOverdue(page: Page, taskTitle: string) {
  const task = page.getByTestId(`task-${taskTitle}`);
  await expect(task).toHaveClass(/overdue|red/);
  await expect(task.getByTestId('overdue-badge')).toBeVisible();
}

export async function assertTaskInCalendar(page: Page, taskTitle: string, date: Date) {
  const dateCell = page.getByTestId(`calendar-cell-${formatDate(date)}`);
  const task = dateCell.getByText(taskTitle);
  await expect(task).toBeVisible();
}
```

### Time Mocking

Use Playwright's clock API for time-sensitive tests:

```typescript
// Example: Test overdue detection
test('should mark task as overdue after due date passes', async ({ page }) => {
  // Set fixed time
  await page.clock.install({ time: new Date('2025-11-06T10:00:00') });
  
  // Create task with due date yesterday
  const task = await createTask({
    dueDate: new Date('2025-11-05T15:00:00')
  });
  
  // Navigate to task list
  await taskListPage.navigate();
  
  // Verify task is marked as overdue
  await assertTaskIsOverdue(page, task.title);
});
```

---

## 🔧 Technical Requirements

### Backend APIs Required

```typescript
// Task Management
GET    /api/tasks/overdue
POST   /api/tasks
PUT    /api/tasks/:id
DELETE /api/tasks/:id/due-date
GET    /api/tasks/:id/audit-log

// Calendar & Schedule
GET    /api/calendar/tasks?view=day|week|month|timeline
GET    /api/calendar/team-tasks
GET    /api/tasks/by-department/:departmentId
GET    /api/tasks/by-project/:projectId
GET    /api/tasks/search?q=keyword

// Notifications
POST   /api/notifications/overdue-reminder
GET    /api/notifications/email-logs

// Analytics
GET    /api/analytics/upcoming-commitments
GET    /api/analytics/overdue-summary
```

### Frontend Components Required

```typescript
// Ticket 1: Overdue Alerts
- OverdueBadge.tsx
- OverdueTaskList.tsx
- OverdueEmailTemplate.tsx

// Ticket 2: Team Schedule Visibility
- TeamCalendar.tsx
- TaskColorLegend.tsx
- CalendarFilterPanel.tsx
- TaskViewSwitcher.tsx

// Ticket 3: Multi-View Schedule
- CalendarDayView.tsx
- CalendarWeekView.tsx
- CalendarMonthView.tsx
- TimelineView.tsx
- CalendarSearchBar.tsx
- CalendarNavigation.tsx

// Ticket 4: Attach Due Dates
- DueDatePicker.tsx
- DueDateDisplay.tsx
- UpcomingCommitments.tsx
- DueDateValidation.tsx
```

### External Libraries

Consider these libraries for implementation:

```json
{
  "dependencies": {
    "@fullcalendar/react": "^6.1.0",           // Calendar views
    "@fullcalendar/daygrid": "^6.1.0",         // Month view
    "@fullcalendar/timegrid": "^6.1.0",        // Week/Day views
    "@fullcalendar/timeline": "^6.1.0",        // Timeline view
    "react-datepicker": "^4.21.0",             // Date/time picker
    "date-fns": "^2.30.0",                     // Date manipulation
    "framer-motion": "^10.16.0"                // Animations for drag-drop
  }
}
```

---

## ✅ Definition of Done

Before marking a ticket as complete, ensure:

### Code Quality
- [ ] All E2E tests passing
- [ ] Unit tests written and passing
- [ ] Code reviewed by at least one peer
- [ ] No linting errors
- [ ] TypeScript types properly defined

### Functionality
- [ ] All acceptance criteria met
- [ ] Edge cases handled
- [ ] Error states implemented
- [ ] Loading states implemented
- [ ] Empty states implemented

### User Experience
- [ ] Responsive design (mobile, tablet, desktop)
- [ ] Accessibility (WCAG 2.1 AA compliance)
- [ ] Keyboard navigation works
- [ ] Screen reader compatible
- [ ] Color contrast meets standards

### Performance
- [ ] Calendar renders <500ms for 100 tasks
- [ ] Search responds <200ms
- [ ] No memory leaks
- [ ] Optimized re-renders

### Documentation
- [ ] Component documentation updated
- [ ] API documentation updated
- [ ] User guide updated (if applicable)
- [ ] Release notes prepared

### Testing
- [ ] E2E tests pass on all browsers (Chrome, Firefox, Safari)
- [ ] Integration tests pass
- [ ] Manual testing completed
- [ ] QA sign-off obtained

---

## 🐛 Known Issues / Considerations

### Email Testing
- Overdue email tests require email service integration or mocking
- Consider using services like MailHog or Ethereal for testing
- Email content verification may require HTML parsing

### Timezone Handling
- Due date/time should respect user timezone settings
- Consider storing dates in UTC, displaying in user's local time
- Test across different timezones

### Real-time Updates
- Calendar may need WebSocket integration for live updates
- Consider polling as fallback for environments without WebSockets

### Performance with Large Datasets
- Calendar views may slow down with 500+ tasks
- Consider pagination or virtual scrolling for large datasets
- Implement lazy loading for off-screen dates

### Drag and Drop
- Drag-and-drop to reschedule requires advanced Playwright gestures
- May need custom test utilities for drag-drop interactions

---

## 📚 Additional Resources

### Playwright Documentation
- [Playwright Best Practices](https://playwright.dev/docs/best-practices)
- [Page Object Model](https://playwright.dev/docs/pom)
- [API Testing](https://playwright.dev/docs/api-testing)

### Testing Patterns
- [Reports Tests](../reports/) - Example of similar test structure
- [Test Fixtures](../../fixtures/) - Reusable test utilities

### Calendar Libraries
- [FullCalendar Documentation](https://fullcalendar.io/docs)
- [React DatePicker](https://reactdatepicker.com/)

---

## 👥 Contact & Support

**Team:** SPM Orange Team  
**Maintained By:** Development Team  
**Last Updated:** November 6, 2025

For questions or issues:
1. Check existing documentation in this directory
2. Review reports tests as reference implementation
3. Reach out to team lead or senior developers
4. Create a ticket in Jira for tracking

---

## 📈 Progress Tracking

Update this section as tests are implemented:

### Implementation Progress

| Date | Ticket | Status | Tests Passing | Notes |
|------|--------|--------|---------------|-------|
| TBD | Ticket 4 | ⏳ Not Started | 0/20 | - |
| TBD | Ticket 1 | ⏳ Not Started | 0/11 | - |
| TBD | Ticket 3 | ⏳ Not Started | 0/24 | - |
| TBD | Ticket 2 | ⏳ Not Started | 0/21 | - |

### Overall Progress
- **Total Tests:** 76
- **Implemented:** 0 (0%)
- **Passing:** 0 (0%)
- **Failing:** 0
- **Skipped:** 0

---

**Happy Testing! 🎉**
