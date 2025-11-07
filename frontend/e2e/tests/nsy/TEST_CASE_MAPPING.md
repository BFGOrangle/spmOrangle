# E2E Test Case to Acceptance Criteria Mapping

**Total E2E Tests:** 26
**Total Test Files:** 6
**Total Acceptance Criteria:** 26
**Coverage:** 100% (26/26 ACs covered)
**Last Updated:** November 6, 2025

---

## TICKET 1: Mentions in Comments Notification

**Test File:** `notifications-mentions.spec.ts`
**Total Tests:** 2
**Coverage:** 100% (2/2 ACs)

### AC 1.1: Receive Notification When Mentioned

** Test:** `should receive notification when mentioned in comment` (line 27)

**Steps:**
1. Log in as User A
2. Navigate to a task
3. Have User B post a comment mentioning @UserA
4. Open notifications panel as User A
5. Verify notification appears with:
   - Comment snippet
   - Task link
   - Mention indicator
6. Verify email notification was sent

---

### AC 1.2: Navigate to Mentioned Comment

** Test:** `should navigate to mentioned comment when clicking notification` (line 52)

**Steps:**
1. Log in as User A
2. Receive a mention notification (from setup)
3. Open notifications panel
4. Click on the mention notification
5. Verify redirected to task page
6. Verify comment section is visible
7. Verify specific mentioned comment is highlighted

---

## TICKET 2: New Comment Notifications

**Test File:** `notifications-comments.spec.ts`
**Total Tests:** 5
**Coverage:** 100% (5/5 ACs)

### AC 2.1: Notify Assignees on New Comment

** Test:** `should notify assignees when new comment is posted` (line 27)

**Steps:**
1. Log in as User A
2. Create a task and assign to User A
3. Have User B post a comment on the task
4. Check notifications panel as User A
5. Verify notification contains:
   - Comment snippet (first ~200 chars or first line)
   - Task title
   - Comment author (User B)
   - Timestamp
6. Verify both in-app and email notification received

---

### AC 2.2: Navigate to Comment via Notification

** Test:** `should navigate to comment section when clicking notification` (line 56)

**Steps:**
1. Log in as User A (assigned to task)
2. Receive comment notification (from setup)
3. Click notification from panel
4. Verify redirected to task's comment section
5. Verify new comment is highlighted

---

### AC 2.3: Notify on Thread Replies

** Test:** `should notify on thread replies` (line 81)

**Steps:**
1. Log in as User A (assigned to task)
2. User B posts initial comment
3. User C posts a reply to User B's comment
4. Check User A's notifications
5. Verify notification received for reply
6. Verify snippet references reply content
7. Verify notification indicates it's a reply

---

### AC 2.4: Prevent Self-Notification

** Test:** `should not notify user about their own comment` (line 106)

**Steps:**
1. Log in as User A (assigned to task)
2. Post a comment on the task as User A
3. Wait for notification processing
4. Check notifications panel
5. Verify no notification for own comment
6. Verify notification count did not increase

---

### AC 2.5: Stop Notifications After Unassignment

** Test:** `should stop notifications after unassignment` (line 131)

**Steps:**
1. Log in as User A (initially assigned to task)
2. Receive initial comment notification to confirm setup
3. Have User B unassign User A from the task
4. User C posts a new comment
5. Check User A's notifications
6. Verify no new notification received for comment after unassignment

---

## TICKET 3: Notification Interaction & Management

**Test File:** `notifications-management.spec.ts`
**Total Tests:** 6
**Coverage:** 100% (6/6 ACs)

### AC 3.1: Display Notifications in Reverse Chronological Order

** Test:** `should display notifications in reverse chronological order` (line 27)

**Steps:**
1. Log in as User A
2. Create multiple notifications (at least 3) with different timestamps
3. Open notifications panel
4. Get list of notifications
5. Verify notifications are sorted newest first
6. Verify each notification displays:
   - Title/event type
   - Snippet
   - Timestamp
   - Source (task/project)
   - Read state

---

### AC 3.2: Persist Read State

** Test:** `should persist read state across page refreshes` (line 56)

**Steps:**
1. Log in as User A
2. Open notifications panel
3. Mark a specific notification as Read
4. Verify notification shows Read state
5. Refresh the page
6. Open notifications panel again
7. Verify notification still marked as Read

---

### AC 3.3: Persist Dismiss State

** Test:** `should persist dismiss state across page refreshes` (line 81)

**Steps:**
1. Log in as User A
2. Open notifications panel
3. Get initial notification count
4. Dismiss a specific notification
5. Verify notification removed from list
6. Refresh the page
7. Open notifications panel again
8. Verify dismissed notification is still not visible

---

### AC 3.4: Navigate to Context from Notification

** Test:** `should navigate to context when clicking notification` (line 106)

**Steps:**
1. Log in as User A
2. Create notifications for different contexts:
   - Task status change
   - New comment
   - Assignment change
3. Open notifications panel
4. Click task status notification
5. Verify redirected to Task Details page
6. Verify status section is highlighted
7. Repeat for comment notification
8. Verify redirected to Comments section

---

### AC 3.5: Display and Update Badge Count

** Test:** `should display and update badge count in real time` (line 131)

**Steps:**
1. Log in as User A
2. Verify initial badge count (e.g., 3 unread)
3. Open notifications panel
4. Mark one notification as Read
5. Close notifications panel
6. Verify badge count decreased by 1
7. Create a new notification
8. Verify badge count increased by 1
9. Verify count updates without page refresh

---

### AC 3.6: Perform Bulk Actions on Notifications

** Test:** `should perform bulk actions on notifications` (line 156)

**Steps:**
1. Log in as User A
2. Open notifications panel with multiple unread notifications
3. Select 3 notifications using checkboxes
4. Click "Mark as Read" bulk action
5. Verify all 3 notifications marked as Read
6. Verify badge count decreased by 3
7. Select 2 notifications
8. Click "Dismiss" bulk action
9. Verify notifications removed from list
10. Verify badge and list updated accordingly

---

## TICKET 4: Task Update Notifications

**Test File:** `notifications-task-updates.spec.ts`
**Total Tests:** 4
**Coverage:** 100% (4/4 ACs)

### AC 4.1: Notify on Status Change

** Test:** `should notify on task status change` (line 27)

**Steps:**
1. Log in as User A
2. Create task assigned to User A with status "To Do"
3. Have User B change status to "In Progress"
4. Check User A's notifications
5. Verify notification received showing:
   - New status: In Progress
   - Previous status: To Do
   - Task title
   - Editor: User B
6. Verify both in-app and email notification

---

### AC 4.2: Notify When Added as Assignee

** Test:** `should notify when added as assignee` (line 56)

**Steps:**
1. Log in as User A
2. Create task without User A as assignee
3. Have User B add User A as assignee
4. Check User A's notifications
5. Verify notification received with:
   - Message indicating assignment
   - Link to the task
6. Click notification link
7. Verify redirected to task details

---

### AC 4.3: Notify When Removed as Assignee

** Test:** `should notify when removed as assignee` (line 81)

**Steps:**
1. Log in as User A (initially assigned to task)
2. Have User B remove User A from assignees
3. Check User A's notifications
4. Verify notification received with:
   - Message indicating removal from assignment
   - Link to task (still accessible if permissions allow)
5. Click notification link
6. Verify can still view task (if access permitted)

---

### AC 4.4: Navigate to Updated Task

** Test:** `should navigate to updated task when clicking notification` (line 106)

**Steps:**
1. Log in as User A
2. Receive task update notification (status change)
3. Open notifications panel
4. Click the task update notification
5. Verify redirected to Task Details page
6. Verify view scrolled/highlighted to change context:
   - For status change: status section highlighted
   - For assignee change: assignees panel highlighted

---

## TICKET 5: Upcoming Due Date Reminder

**Test File:** `notifications-reminders.spec.ts`
**Total Tests:** 5
**Coverage:** 100% (5/5 ACs)

### AC 5.1: Send 24-Hour Reminder

** Test:** `should send 24-hour reminder for upcoming due date` (line 27)

**Steps:**
1. Log in as User A
2. Create task assigned to User A with due date 24 hours from now
3. Trigger reminder scheduler (or wait for scheduled run)
4. Check User A's notifications
5. Verify reminder notification received containing:
   - Task title
   - Due date/time
   - Link to task
6. Verify in-app notification present
7. Verify email notification sent

---

### AC 5.2: Prevent Duplicate Reminders

** Test:** `should not send duplicate reminders` (line 52)

**Steps:**
1. Log in as User A
2. Create task with due date 24 hours from now
3. Trigger reminder scheduler
4. Verify reminder sent
5. Run scheduler again (before task is due)
6. Check notifications
7. Verify no duplicate reminder sent
8. Verify notification count unchanged

---

### AC 5.3: Reschedule Reminder on Due Date Change

** Test:** `should reschedule reminder when due date changes` (line 77)

**Steps:**
1. Log in as User A
2. Create task with due date 24 hours from now
3. Verify reminder scheduled
4. Change due date to 48 hours from now
5. Trigger scheduler
6. Verify old reminder canceled
7. Verify new reminder scheduled for 12 hours before new due date
8. Wait for new reminder time
9. Verify reminder sent at new time

---

### AC 5.4: Handle Assignment Changes for Reminders

** Test:** `should update reminders on assignment changes` (line 102)

**Steps:**
1. Log in as User A (assigned to task with upcoming due date)
2. Verify User A has pending reminder
3. Unassign User A and assign User B
4. Trigger scheduler
5. Verify User A no longer receives reminder
6. Log in as User B
7. Verify User B receives reminder instead

---

### AC 5.5: Skip Reminder for Completed Tasks

** Test:** `should skip reminder for completed tasks` (line 127)

**Steps:**
1. Log in as User A
2. Create task with due date 24 hours from now
3. Mark task as Completed before reminder time
4. Trigger scheduler at reminder time
5. Check User A's notifications
6. Verify no reminder sent
7. Verify notification count unchanged

---

## TICKET 6: Daily Digest Email

**Test File:** `notifications-digest.spec.ts`
**Total Tests:** 4
**Coverage:** 100% (4/4 ACs)

### AC 6.1: Send Daily Digest with Tasks Due Tomorrow

** Test:** `should send daily digest email with tasks due tomorrow` (line 27)

**Steps:**
1. Log in as User A
2. Create 3 tasks assigned to User A with due date = tomorrow
   - Task 1: Status "To Do"
   - Task 2: Status "In Progress"
   - Task 3: Status "Under Review"
3. Trigger digest job at 09:00 (or scheduled time)
4. Check User A's email
5. Verify digest email received containing:
   - List of all 3 tasks with title, due time, status, and links
   - Summary section showing total pending tasks (3)
   - Breakdown by status (To Do: 1, In Progress: 1, Under Review: 1)

---

### AC 6.2: Reflect Due Date Changes in Digest

** Test:** `should reflect due date changes in digest` (line 52)

**Steps:**
1. Log in as User A
2. Create task with due date = tomorrow
3. Change due date to next week before digest runs
4. Trigger digest job
5. Check digest email
6. Verify task is NOT included (due date no longer tomorrow)
7. Create another task with due date = tomorrow
8. Trigger digest again
9. Verify new task IS included

---

### AC 6.3: Update Digest on Assignment Changes

** Test:** `should update digest on assignment changes` (line 77)

**Steps:**
1. Log in as User A
2. Create task assigned to User A with due date = tomorrow
3. Unassign User A and assign User B before 09:00
4. Trigger digest job
5. Check User A's digest email
6. Verify task excluded from User A's digest
7. Log in as User B
8. Check User B's digest email
9. Verify task included in User B's digest

---

### AC 6.4: Exclude Completed Tasks from Digest

** Test:** `should exclude completed tasks from digest` (line 102)

**Steps:**
1. Log in as User A
2. Create 2 tasks due tomorrow:
   - Task 1: Status "In Progress"
   - Task 2: Status "To Do"
3. Mark Task 2 as "Completed" before 09:00
4. Trigger digest job
5. Check digest email
6. Verify only Task 1 is included
7. Verify Task 2 (completed) is excluded
8. Verify summary shows count = 1 (only pending tasks)

---

## Coverage Summary

| Ticket # | Ticket Name | ACs | Covered | % | Tests |
|----------|-------------|-----|---------|---|-------|
| 1 | Mentions Notifications | 2 | 2 | 100%  | 2 |
| 2 | Comment Notifications | 5 | 5 | 100%  | 5 |
| 3 | Notification Management | 6 | 6 | 100%  | 6 |
| 4 | Task Update Notifications | 4 | 4 | 100%  | 4 |
| 5 | Due Date Reminders | 5 | 5 | 100%  | 5 |
| 6 | Daily Digest Email | 4 | 4 | 100%  | 4 |
| **TOTAL** | **6 Tickets** | **26** | **26** | **100%** | **26** |

---

## Test Execution

### Run All Notification Tests
```bash
npx playwright test frontend/e2e/tests/nsy/
```

### Run by Test File
```bash
npx playwright test frontend/e2e/tests/reports/nsy/notifications-mentions.spec.ts
npx playwright test frontend/e2e/tests/reports/nsy/notifications-comments.spec.ts
npx playwright test frontend/e2e/tests/reports/nsy/notifications-management.spec.ts
npx playwright test frontend/e2e/tests/reports/nsy/notifications-task-updates.spec.ts
npx playwright test frontend/e2e/tests/reports/nsy/notifications-reminders.spec.ts
npx playwright test frontend/e2e/tests/reports/nsy/notifications-digest.spec.ts
```

### Run by Ticket (Grep Pattern)
```bash
# Ticket 1: Mentions
npx playwright test --grep "mention"

# Ticket 2: Comments
npx playwright test --grep "comment"

# Ticket 3: Management
npx playwright test --grep "notification.*management|badge|bulk"

# Ticket 4: Task Updates
npx playwright test --grep "task.*update|status change|assignee"

# Ticket 5: Reminders
npx playwright test --grep "reminder|due date"

# Ticket 6: Digest
npx playwright test --grep "digest"
```

---

## Test Quality Metrics

- **Total E2E Tests:** 26
- **Pass Rate:** TBD
- **Test Files:** 6
- **Average Tests per Ticket:** 4.3
- **Page Objects:** Yes (NotificationsPage, TaskPage)
- **Test Data Builders:** Yes
- **Email Verification Helpers:** Yes
- **Authentication Fixtures:** Yes (User A, User B, User C)

---

**Last Updated:** November 6, 2025
**Maintained By:** SPM Orangle Team - NSY
