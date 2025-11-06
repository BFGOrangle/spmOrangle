/**
 * Notification Data Types
 * Type definitions for notification test data
 */

export enum NotificationType {
  MENTION = 'MENTION',
  COMMENT = 'COMMENT',
  TASK_STATUS_CHANGE = 'TASK_STATUS_CHANGE',
  ASSIGNMENT_ADDED = 'ASSIGNMENT_ADDED',
  ASSIGNMENT_REMOVED = 'ASSIGNMENT_REMOVED',
  DUE_DATE_REMINDER = 'DUE_DATE_REMINDER',
  DAILY_DIGEST = 'DAILY_DIGEST',
}

export enum NotificationStatus {
  UNREAD = 'UNREAD',
  READ = 'READ',
  DISMISSED = 'DISMISSED',
}

export interface NotificationData {
  id?: string;
  type: NotificationType;
  recipientId: string;
  title: string;
  message: string;
  snippet?: string;
  taskId?: string;
  taskTitle?: string;
  commentId?: string;
  actorId?: string;
  actorName?: string;
  status?: NotificationStatus;
  createdAt?: string;
  readAt?: string;
  link?: string;
  metadata?: Record<string, any>;
}

export interface MentionNotificationData extends NotificationData {
  type: NotificationType.MENTION;
  commentId: string;
  mentionedUsername: string;
}

export interface CommentNotificationData extends NotificationData {
  type: NotificationType.COMMENT;
  commentId: string;
  commentAuthor: string;
  isReply?: boolean;
  parentCommentId?: string;
}

export interface TaskUpdateNotificationData extends NotificationData {
  type: NotificationType.TASK_STATUS_CHANGE | NotificationType.ASSIGNMENT_ADDED | NotificationType.ASSIGNMENT_REMOVED;
  previousValue?: string;
  newValue?: string;
  editorId: string;
  editorName: string;
}

export interface ReminderNotificationData extends NotificationData {
  type: NotificationType.DUE_DATE_REMINDER;
  dueDate: string;
  dueTime?: string;
  hoursBeforeDue: number;
}

export interface DigestNotificationData {
  recipientId: string;
  emailSubject: string;
  tasks: DigestTaskData[];
  summary: {
    totalPending: number;
    byStatus: Record<string, number>;
  };
  sentAt?: string;
}

export interface DigestTaskData {
  id: string;
  title: string;
  dueDate: string;
  dueTime?: string;
  status: string;
  link: string;
}
