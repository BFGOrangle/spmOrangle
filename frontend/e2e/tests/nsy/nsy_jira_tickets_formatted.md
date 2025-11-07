# Notifications Feature - Jira Tickets & Acceptance Criteria

**Total Tests:** TBD
**Test Files:** TBD
**Total Tickets:** 6
**Last Updated:** November 6, 2025

---

## TICKET 1: Mentions in Comments Notification

### User Story
As a user,
I want to be notified when I am mentioned in a comment,
so that I don't miss directed collaboration.

### Acceptance Criteria

**AC 1.1: Receive Notification When Mentioned**
```
Given I am mentioned with @username in a comment
When the comment is saved
Then I receive both in-app and email notifications
And the notification contains the comment snippet and a task link
```

**AC 1.2: Navigate to Mentioned Comment**
```
Given I receive a mention notification
When I click the notification
Then I am redirected to the specific comment in the task
```

---

## TICKET 2: New Comment Notifications

### User Story
As a user,
I want to be notified when a new comment is added to a task I'm assigned to,
so that I stay informed about updates and can respond quickly.

### Acceptance Criteria

**AC 2.1: Notify Assignees on New Comment**
```
Given I am assigned to a task
When a new comment is posted on that task
Then I receive both an in-app and an email notification
And the notification contains a comment snippet (first ~200 chars or first line)
And the notification includes the task title, comment author, and timestamp
```

**AC 2.2: Navigate to Comment via Notification**
```
Given I receive a comment notification
When I click the notification
Then I am taken directly to the task's comment section
And the new comment is highlighted
```

**AC 2.3: Notify on Thread Replies**
```
Given I am assigned to a task
When a reply is added under an existing comment thread
Then I receive both in-app and email notifications
And the snippet references the reply content and indicates it's a reply
```

**AC 2.4: Prevent Self-Notification**
```
Given I post a comment
When the system processes notifications
Then I do not receive a notification about my own comment
```

**AC 2.5: Stop Notifications After Unassignment**
```
Given I was assigned but am later unassigned from the task
When new comments are posted after unassignment
Then I no longer receive comment notifications (unless I'm mentioned or am a watcher)
```

---

## TICKET 3: Notification Interaction & Management

### User Story
As a user,
I want to view and manage notifications,
so that I can track updates and dismiss them after review.

### Acceptance Criteria

**AC 3.1: Display Notifications in Reverse Chronological Order**
```
Given I have unread notifications
When I open the Notifications panel
Then notifications are listed in reverse chronological order (newest first)
And each item shows title/event type, snippet, timestamp, source (task/project), and read state
```

**AC 3.2: Persist Read State**
```
Given I mark a notification as Read
When I refresh or reopen the panel
Then the notification remains marked as Read
```

**AC 3.3: Persist Dismiss State**
```
Given I dismiss a notification
When I refresh or reopen the panel
Then the notification is removed from the view
```

**AC 3.4: Navigate to Context from Notification**
```
Given a notification references a task or comment
When I click the notification
Then I am taken to the relevant context (Task Details, Comments section, Status panel)
And the referenced entity is highlighted
```

**AC 3.5: Display and Update Badge Count**
```
Given I have unread notifications
When the panel is closed
Then the unread badge/count shows the current number
And the count updates in real time as items become read/dismissed or as new ones arrive
```

**AC 3.6: Perform Bulk Actions on Notifications**
```
Given I have multiple notifications
When I select several items
Then I can Mark as Read or Dismiss them in bulk
And the badge/count and list update accordingly
```

---

## TICKET 4: Task Update Notifications

### User Story
As a user,
I want to be notified of task updates (status changes, reassignments),
so that I am aware of changes that affect me.

### Acceptance Criteria

**AC 4.1: Notify on Status Change**
```
Given I am assigned to a task
When the task's status changes (e.g., To Do ’ In Progress ’ Done/Blocked)
Then I receive both an in-app and an email notification
And the notification shows the new status, previous status, task title, and editor
```

**AC 4.2: Notify When Added as Assignee**
```
Given I am added to a task as an assignee
When the assignment is saved
Then I receive both an in-app and an email notification
And the notification indicates I've been assigned with a link to the task
```

**AC 4.3: Notify When Removed as Assignee**
```
Given I am removed from a task as an assignee
When the update is saved
Then I receive both an in-app and an email notification
And the notification informs me that I am no longer assigned
And the notification includes a link that still opens the task (access permitting)
```

**AC 4.4: Navigate to Updated Task**
```
Given I receive a task update notification
When I click it
Then I am taken to the Task Details page
And the view is scrolled/highlighted to the change context (status section or assignees panel)
```

---

## TICKET 5: Upcoming Due Date Reminder

### User Story
As a user,
I want to be reminded shortly before a task is due,
so that I don't miss deadlines.

### Acceptance Criteria

**AC 5.1: Send 24-Hour Reminder**
```
Given I'm assigned to a task with a due date/time
When the scheduler reaches 24 hours (by default) before the due date/time in my timezone
Then I receive one in-app notification and one email
And the notification contains the task title, due date/time, and a link to the task
```

**AC 5.2: Prevent Duplicate Reminders**
```
Given I already received the 24-hour reminder
When the scheduler runs again before the task is due
Then I do not receive another 24-hour reminder for that task
```

**AC 5.3: Reschedule Reminder on Due Date Change**
```
Given a task's due date/time changes
When the scheduler next runs
Then the previous reminder is canceled
And a new reminder is scheduled for 12 hours before the new due date/time
```

**AC 5.4: Handle Assignment Changes for Reminders**
```
Given I was assigned to the task
When I am unassigned (or someone else is assigned)
Then I stop receiving future reminders for that task
And the new assignee receives them instead
```

**AC 5.5: Skip Reminder for Completed Tasks**
```
Given a task is completed before the reminder time
When the scheduler runs
Then no reminder is sent
```

---

## TICKET 6: Daily Digest Email

### User Story
As a user,
I want a daily email summarizing tasks due tomorrow,
so that I can plan my work for the next day.

### Acceptance Criteria

**AC 6.1: Send Daily Digest with Tasks Due Tomorrow**
```
Given I have one or more tasks due tomorrow
When the digest job runs at 09:00 in my timezone
Then I receive one email listing those tasks with title, due time, status, and links
And the email includes a summary section with the total number of pending tasks due tomorrow
And the summary shows my breakdown by status (To Do, In Progress, Under Review, Blocked)
```

**AC 6.2: Reflect Due Date Changes in Digest**
```
Given a task's due date/time changes
When the next digest runs
Then inclusion is based on the updated due date/time
```

**AC 6.3: Update Digest on Assignment Changes**
```
Given I was assigned to a task slated for the digest
When I'm unassigned before 09:00
Then it's excluded from my digest
And it's included in the new assignee's digest (if applicable)
```

**AC 6.4: Exclude Completed Tasks from Digest**
```
Given a task due tomorrow is completed before 09:00
When the digest runs
Then it is not included in the digest
```

---

## Summary

| Ticket # | Ticket Name | Total ACs | Test Priority |
|----------|-------------|-----------|---------------|
| 1 | Mentions in Comments Notification | 2 | P0 (Critical) |
| 2 | New Comment Notifications | 5 | P0 (Critical) |
| 3 | Notification Interaction & Management | 6 | P0 (Critical) |
| 4 | Task Update Notifications | 4 | P0 (Critical) |
| 5 | Upcoming Due Date Reminder | 5 | P0 (Critical) |
| 6 | Daily Digest Email | 4 | P0 (Critical) |
| **TOTAL** | **6 Tickets** | **26** | - |

---

## Copy-Paste Instructions for Jira

1. **Create a new Story** in Jira
2. **Copy the User Story** section into the Description field
3. **Add each Acceptance Criteria** as separate items in Jira's AC section
4. **Use the GWT format blocks** directly (already formatted)
5. **Add labels:** `notifications`, `e2e-tested`, `frontend`, `nsy`
6. **Set priority** based on table above

---

## Story Points Estimation

- **Ticket 1** (Mentions in Comments): 3 points
- **Ticket 2** (New Comment Notifications): 5 points
- **Ticket 3** (Notification Management): 8 points
- **Ticket 4** (Task Update Notifications): 5 points
- **Ticket 5** (Due Date Reminders): 5 points
- **Ticket 6** (Daily Digest Email): 5 points
