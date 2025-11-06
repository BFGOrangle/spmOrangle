/**
 * Notification Data Builder
 * Provides fluent interface for creating test notification data
 */

import {
  NotificationData,
  NotificationType,
  NotificationStatus,
  MentionNotificationData,
  CommentNotificationData,
  TaskUpdateNotificationData,
  ReminderNotificationData,
  DigestNotificationData,
  DigestTaskData,
} from './notification-data-types';

export class NotificationDataBuilder {
  private notificationData: Partial<NotificationData> = {};

  withType(type: NotificationType): NotificationDataBuilder {
    this.notificationData.type = type;
    return this;
  }

  withRecipient(recipientId: string): NotificationDataBuilder {
    this.notificationData.recipientId = recipientId;
    return this;
  }

  withTitle(title: string): NotificationDataBuilder {
    this.notificationData.title = title;
    return this;
  }

  withMessage(message: string): NotificationDataBuilder {
    this.notificationData.message = message;
    return this;
  }

  withSnippet(snippet: string): NotificationDataBuilder {
    this.notificationData.snippet = snippet;
    return this;
  }

  withTask(taskId: string, taskTitle: string): NotificationDataBuilder {
    this.notificationData.taskId = taskId;
    this.notificationData.taskTitle = taskTitle;
    return this;
  }

  withActor(actorId: string, actorName: string): NotificationDataBuilder {
    this.notificationData.actorId = actorId;
    this.notificationData.actorName = actorName;
    return this;
  }

  withStatus(status: NotificationStatus): NotificationDataBuilder {
    this.notificationData.status = status;
    return this;
  }

  withLink(link: string): NotificationDataBuilder {
    this.notificationData.link = link;
    return this;
  }

  withMetadata(metadata: Record<string, any>): NotificationDataBuilder {
    this.notificationData.metadata = metadata;
    return this;
  }

  build(): NotificationData {
    return {
      type: this.notificationData.type || NotificationType.COMMENT,
      recipientId: this.notificationData.recipientId || 'test-recipient',
      title: this.notificationData.title || 'Test Notification',
      message: this.notificationData.message || 'This is a test notification',
      status: this.notificationData.status || NotificationStatus.UNREAD,
      ...this.notificationData,
    };
  }

  buildMention(username: string, commentId: string): MentionNotificationData {
    return {
      type: NotificationType.MENTION,
      recipientId: this.notificationData.recipientId || 'test-recipient',
      title: `${username} mentioned you in a comment`,
      message: `@${username} mentioned you`,
      snippet: this.notificationData.snippet || 'Hey @you, check this out!',
      commentId,
      mentionedUsername: username,
      taskId: this.notificationData.taskId,
      taskTitle: this.notificationData.taskTitle,
      status: NotificationStatus.UNREAD,
      ...this.notificationData,
    } as MentionNotificationData;
  }

  buildComment(commentAuthor: string, commentId: string): CommentNotificationData {
    return {
      type: NotificationType.COMMENT,
      recipientId: this.notificationData.recipientId || 'test-recipient',
      title: `${commentAuthor} commented on a task`,
      message: `${commentAuthor} added a comment`,
      snippet: this.notificationData.snippet || 'This is a test comment',
      commentId,
      commentAuthor,
      taskId: this.notificationData.taskId,
      taskTitle: this.notificationData.taskTitle,
      status: NotificationStatus.UNREAD,
      ...this.notificationData,
    } as CommentNotificationData;
  }

  buildStatusChange(
    editorName: string,
    previousStatus: string,
    newStatus: string
  ): TaskUpdateNotificationData {
    return {
      type: NotificationType.TASK_STATUS_CHANGE,
      recipientId: this.notificationData.recipientId || 'test-recipient',
      title: `Task status changed to ${newStatus}`,
      message: `${editorName} changed task status from ${previousStatus} to ${newStatus}`,
      previousValue: previousStatus,
      newValue: newStatus,
      editorId: this.notificationData.actorId || 'test-editor',
      editorName,
      taskId: this.notificationData.taskId,
      taskTitle: this.notificationData.taskTitle,
      status: NotificationStatus.UNREAD,
      ...this.notificationData,
    } as TaskUpdateNotificationData;
  }

  buildAssignmentAdded(editorName: string): TaskUpdateNotificationData {
    return {
      type: NotificationType.ASSIGNMENT_ADDED,
      recipientId: this.notificationData.recipientId || 'test-recipient',
      title: 'You were assigned to a task',
      message: `${editorName} assigned you to a task`,
      editorId: this.notificationData.actorId || 'test-editor',
      editorName,
      taskId: this.notificationData.taskId,
      taskTitle: this.notificationData.taskTitle,
      status: NotificationStatus.UNREAD,
      ...this.notificationData,
    } as TaskUpdateNotificationData;
  }

  buildAssignmentRemoved(editorName: string): TaskUpdateNotificationData {
    return {
      type: NotificationType.ASSIGNMENT_REMOVED,
      recipientId: this.notificationData.recipientId || 'test-recipient',
      title: 'You were removed from a task',
      message: `${editorName} removed you from a task`,
      editorId: this.notificationData.actorId || 'test-editor',
      editorName,
      taskId: this.notificationData.taskId,
      taskTitle: this.notificationData.taskTitle,
      status: NotificationStatus.UNREAD,
      ...this.notificationData,
    } as TaskUpdateNotificationData;
  }

  buildReminder(dueDate: string, hoursBeforeDue: number = 24): ReminderNotificationData {
    return {
      type: NotificationType.DUE_DATE_REMINDER,
      recipientId: this.notificationData.recipientId || 'test-recipient',
      title: 'Task due soon',
      message: `Task is due in ${hoursBeforeDue} hours`,
      dueDate,
      hoursBeforeDue,
      taskId: this.notificationData.taskId,
      taskTitle: this.notificationData.taskTitle,
      status: NotificationStatus.UNREAD,
      ...this.notificationData,
    } as ReminderNotificationData;
  }
}

export class DigestDataBuilder {
  private digestData: Partial<DigestNotificationData> = {
    tasks: [],
    summary: {
      totalPending: 0,
      byStatus: {},
    },
  };

  withRecipient(recipientId: string): DigestDataBuilder {
    this.digestData.recipientId = recipientId;
    return this;
  }

  withSubject(subject: string): DigestDataBuilder {
    this.digestData.emailSubject = subject;
    return this;
  }

  addTask(taskData: DigestTaskData): DigestDataBuilder {
    if (!this.digestData.tasks) {
      this.digestData.tasks = [];
    }
    this.digestData.tasks.push(taskData);

    // Update summary
    if (this.digestData.summary) {
      this.digestData.summary.totalPending = this.digestData.tasks.length;

      // Update status count
      const status = taskData.status;
      if (!this.digestData.summary.byStatus) {
        this.digestData.summary.byStatus = {};
      }
      this.digestData.summary.byStatus[status] =
        (this.digestData.summary.byStatus[status] || 0) + 1;
    }

    return this;
  }

  withTasks(tasks: DigestTaskData[]): DigestDataBuilder {
    this.digestData.tasks = tasks;

    // Update summary
    if (this.digestData.summary) {
      this.digestData.summary.totalPending = tasks.length;
      this.digestData.summary.byStatus = tasks.reduce((acc, task) => {
        acc[task.status] = (acc[task.status] || 0) + 1;
        return acc;
      }, {} as Record<string, number>);
    }

    return this;
  }

  build(): DigestNotificationData {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    const dateStr = tomorrow.toISOString().split('T')[0];

    return {
      recipientId: this.digestData.recipientId || 'test-recipient',
      emailSubject: this.digestData.emailSubject || `Daily Task Digest - ${this.digestData.tasks?.length || 0} tasks due tomorrow`,
      tasks: this.digestData.tasks || [],
      summary: this.digestData.summary || {
        totalPending: 0,
        byStatus: {},
      },
      sentAt: new Date().toISOString(),
    };
  }

  buildWithSampleTasks(count: number = 3): DigestNotificationData {
    const tasks: DigestTaskData[] = [];
    const statuses = ['To Do', 'In Progress', 'Under Review'];

    for (let i = 0; i < count; i++) {
      const tomorrow = new Date();
      tomorrow.setDate(tomorrow.getDate() + 1);
      tomorrow.setHours(10 + i * 2, 0, 0, 0);

      tasks.push({
        id: `test-task-${i}`,
        title: `Test Task ${i + 1}`,
        dueDate: tomorrow.toISOString().split('T')[0],
        dueTime: tomorrow.toTimeString().split(' ')[0],
        status: statuses[i % statuses.length],
        link: `/tasks/test-task-${i}`,
      });
    }

    return this.withTasks(tasks).build();
  }
}
