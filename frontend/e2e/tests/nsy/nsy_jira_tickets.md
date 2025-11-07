## Ticket 1: Mentions in comments Notification

Summary: As a user, I want to be notified when I am mentioned in a comment so that I don’t miss directed collaboration.

Description:
When another user tags me with @username in a comment, I should be alerted via in-app and email.

Acceptance Criteria (GWT):

Given I am mentioned in a comment, when the comment is saved, then I receive both in-app and email notifications containing the comment snippet and a task link.

Given I open the notification, when I click it, then I am redirected to the specific comment in the task.

# Ticket 2: New comment notifications

As a user
I want to be notified when a new comment is added to a task I’m assigned to
So that I stay informed about updates and can respond quickly

Acceptance Criteria (Given / When / Then)
1) Notify assignees on new comment
Given I am assigned to a task

When a new comment is posted on that task

Then I receive both an in-app and an email notification containing a comment snippet (first ~200 chars or first line)

And the notification includes the task title, comment author, and timestamp.

2) Clickthrough behavior
Given I receive a comment notification

When I click the notification

Then I am taken directly to the task’s comment section, with the new comment highlighted.

3) Thread replies behave the same
Given I am assigned to a task

When a reply is added under an existing comment thread

Then I receive notifications as in Scenario 1

And the snippet references the reply content and indicates it’s a reply.

4) Author self-notifications
Given I post a comment

When the system processes notifications

Then I do not receive a notification about my own comment

5) Unassignment / reassignment
Given I was assigned but am later unassigned from the task

When new comments are posted after unassignment

Then I no longer receive comment notifications (unless I’m mentioned or am a watcher, if that role exists)


## Ticket 3: Notification interaction & management

As a user
I want to view and manage notifications
So that I can track updates and dismiss them after review

Acceptance Criteria (Given / When / Then)

1) Panel listing & order

Given I have unread notifications

When I open the Notifications panel

Then notifications are listed in reverse chronological order (newest first)

And each item shows title/event type, snippet, timestamp, source (task/project), and read state.

2) Read state persistence

Given I mark a notification as Read

When I refresh or reopen the panel

Then the notification remains marked as Read.

3) Dismiss (archive) persistence

Given I Dismiss a notification

When I refresh or reopen the panel

Then the notification is removed from the view

4) Clickthrough to context

Given a notification references a task or comment

When I click the notification

Then I am taken to the relevant context (e.g., Task Details, Comments section, Status panel) with the referenced entity highlighted.

5) Badge count & auto-updates

Given I have unread notifications

When the panel is closed

Then the unread badge/count shows the current number

And the count updates in real time as items become read/dismissed or as new ones arrive.

6) Bulk actions

Given I have multiple notifications

When I select several items

Then I can Mark as Read or Dismiss them in bulk

And the badge/count and list update accordingly.

## Ticket 4: Task update notifications

As a user
I want to be notified of task updates (status changes, reassignments)
So that I am aware of changes that affect me

Acceptance Criteria (Given / When / Then)

1) Status change notification

Given I am assigned to a task

When the task’s status changes (e.g., To Do → In Progress → Done/Blocked)

Then I receive both an in-app and an email notification showing the new status, the previous status, the task title, and the editor.

2) Added as assignee

Given I am added to a task as an assignee

When the assignment is saved

Then I receive both an in-app and an email notification indicating I’ve been assigned, with a link to the task.

3) Removed as assignee

Given I am removed from a task as an assignee

When the update is saved

Then I receive both an in-app and an email notification informing me that I am no longer assigned, with a link that still opens the task (access permitting).

4) Clickthrough behavior

Given I receive a task update notification

When I click it

Then I am taken to the Task Details page, scrolled/highlighted to the change context (status section or assignees panel).

## Ticket 5: Upcoming Due Date Reminder

As a user,
I want to be reminded shortly before a task is due,
so that I don’t miss deadlines.

Acceptance Criteria (Given / When / Then)

24-hour reminder

Given I’m assigned to a task with a due date/time,
When the scheduler reaches 24 hours (by default) before the due date/time in my timezone,
Then I receive one in-app notification and one email containing the task title, due date/time, and a link to the task.

No duplicates

Given I already received the 24-hour reminder,
When the scheduler runs again before the task is due,
Then I do not receive another 24-hour reminder for that task.

Reschedule on due-date change

Given a task’s due date/time changes,
When the scheduler next runs,
Then the previous reminder is canceled and a new reminder is scheduled for 12 hours before the new due date/time.

Assignment changes

Given I was assigned to the task,
When I am unassigned (or someone else is assigned),
Then I stop receiving future reminders for that task, and the new assignee receives them instead.

Completion before reminder

Given a task is completed before the reminder time,
When the scheduler runs,
Then no reminder is sent.


## Ticket 6: Daily Digest Email

As a user,
I want a daily email summarizing tasks due tomorrow,
so that I can plan my work for the next day.

Acceptance Criteria (Given / When / Then)

Daily digest

Given I have one or more tasks due tomorrow,
When the digest job runs at 09:00 in my timezone,
Then I receive one email listing those tasks with title, due time, status, and links,
And the email includes a summary section with the total number of pending tasks due tomorrow (i.e., not Completed),
And the summary also shows my breakdown by status (e.g., To Do, In Progress, Under Review, Blocked).

Respects changes

Given a task’s due date/time changes,
When the next digest runs,
Then inclusion is based on the updated due date/time.

Assignment changes

Given I was assigned to a task slated for the digest,
When I’m unassigned before 09:00,
Then it’s excluded from my digest and included in the new assignee’s digest (if applicable).

Exclude completed

Given a task due tomorrow is completed before 09:00,
When the digest runs,
Then it is not included.


