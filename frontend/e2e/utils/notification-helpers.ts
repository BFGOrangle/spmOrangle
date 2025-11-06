/**
 * Notification Helper Utilities
 * Helper functions for creating and managing notifications in E2E tests
 */

import { Page, APIRequestContext } from '@playwright/test';
import { NotificationData, NotificationType, DigestNotificationData } from '../fixtures/data/notification-data-types';

/**
 * Create a notification via API
 * This is a placeholder - adjust based on your actual API endpoints
 */
export async function createNotificationViaAPI(
  context: APIRequestContext,
  notification: NotificationData
): Promise<string> {
  // TODO: Replace with actual API endpoint
  const response = await context.post('/api/notifications', {
    data: notification,
  });

  if (!response.ok()) {
    throw new Error(`Failed to create notification: ${response.status()}`);
  }

  const data = await response.json();
  return data.id || data.notificationId;
}

/**
 * Create multiple notifications via API
 */
export async function createNotificationsViaAPI(
  context: APIRequestContext,
  notifications: NotificationData[]
): Promise<string[]> {
  const ids: string[] = [];
  for (const notification of notifications) {
    const id = await createNotificationViaAPI(context, notification);
    ids.push(id);
  }
  return ids;
}

/**
 * Mark notification as read via API
 */
export async function markNotificationAsReadViaAPI(
  context: APIRequestContext,
  notificationId: string
): Promise<void> {
  const response = await context.patch(`/api/notifications/${notificationId}`, {
    data: { status: 'READ' },
  });

  if (!response.ok()) {
    throw new Error(`Failed to mark notification as read: ${response.status()}`);
  }
}

/**
 * Dismiss notification via API
 */
export async function dismissNotificationViaAPI(
  context: APIRequestContext,
  notificationId: string
): Promise<void> {
  const response = await context.delete(`/api/notifications/${notificationId}`);

  if (!response.ok()) {
    throw new Error(`Failed to dismiss notification: ${response.status()}`);
  }
}

/**
 * Get notifications for a user via API
 */
export async function getNotificationsViaAPI(
  context: APIRequestContext,
  userId: string
): Promise<NotificationData[]> {
  const response = await context.get(`/api/notifications?userId=${userId}`);

  if (!response.ok()) {
    throw new Error(`Failed to get notifications: ${response.status()}`);
  }

  const data = await response.json();
  return data.notifications || data;
}

/**
 * Clear all notifications for a user via API
 */
export async function clearNotificationsViaAPI(
  context: APIRequestContext,
  userId: string
): Promise<void> {
  const response = await context.delete(`/api/notifications/user/${userId}`);

  if (!response.ok()) {
    throw new Error(`Failed to clear notifications: ${response.status()}`);
  }
}

/**
 * Trigger reminder scheduler
 * This is a test helper to manually trigger the reminder job
 */
export async function triggerReminderScheduler(
  context: APIRequestContext
): Promise<void> {
  // TODO: Replace with actual scheduler trigger endpoint
  const response = await context.post('/api/admin/scheduler/run-reminders');

  if (!response.ok()) {
    throw new Error(`Failed to trigger reminder scheduler: ${response.status()}`);
  }
}

/**
 * Trigger digest scheduler
 * This is a test helper to manually trigger the digest job
 */
export async function triggerDigestScheduler(
  context: APIRequestContext
): Promise<void> {
  // TODO: Replace with actual scheduler trigger endpoint
  const response = await context.post('/api/admin/scheduler/run-digest');

  if (!response.ok()) {
    throw new Error(`Failed to trigger digest scheduler: ${response.status()}`);
  }
}

/**
 * Wait for notification to appear in UI
 */
export async function waitForNotificationInUI(
  page: Page,
  notificationText: string | RegExp,
  timeout = 10000
): Promise<void> {
  await page.waitForSelector(`[data-testid="notification-item"]`, {
    state: 'visible',
    timeout,
  });

  // Wait for specific notification text
  const notificationLocator = page
    .getByTestId('notification-item')
    .filter({ hasText: notificationText });

  await notificationLocator.waitFor({ state: 'visible', timeout });
}

/**
 * Get notification count from badge
 */
export async function getNotificationBadgeCount(page: Page): Promise<number> {
  const badge = page.getByTestId('notification-badge');
  const isVisible = await badge.isVisible();

  if (!isVisible) {
    return 0;
  }

  const text = await badge.textContent();
  return text ? parseInt(text, 10) : 0;
}

/**
 * Create task with due date for reminder testing
 */
export async function createTaskWithDueDate(
  context: APIRequestContext,
  taskData: {
    title: string;
    assigneeId: string;
    dueDate: Date;
    status?: string;
  }
): Promise<string> {
  const response = await context.post('/api/tasks', {
    data: {
      title: taskData.title,
      assignees: [taskData.assigneeId],
      dueDate: taskData.dueDate.toISOString(),
      status: taskData.status || 'TODO',
    },
  });

  if (!response.ok()) {
    throw new Error(`Failed to create task: ${response.status()}`);
  }

  const data = await response.json();
  return data.id || data.taskId;
}

/**
 * Create comment with mention
 */
export async function createCommentWithMention(
  context: APIRequestContext,
  taskId: string,
  content: string,
  mentionedUsernames: string[]
): Promise<string> {
  const response = await context.post(`/api/tasks/${taskId}/comments`, {
    data: {
      content,
      mentions: mentionedUsernames,
    },
  });

  if (!response.ok()) {
    throw new Error(`Failed to create comment: ${response.status()}`);
  }

  const data = await response.json();
  return data.id || data.commentId;
}

/**
 * Update task status
 */
export async function updateTaskStatus(
  context: APIRequestContext,
  taskId: string,
  newStatus: string
): Promise<void> {
  const response = await context.patch(`/api/tasks/${taskId}`, {
    data: { status: newStatus },
  });

  if (!response.ok()) {
    throw new Error(`Failed to update task status: ${response.status()}`);
  }
}

/**
 * Update task assignees
 */
export async function updateTaskAssignees(
  context: APIRequestContext,
  taskId: string,
  assigneeIds: string[]
): Promise<void> {
  const response = await context.patch(`/api/tasks/${taskId}`, {
    data: { assignees: assigneeIds },
  });

  if (!response.ok()) {
    throw new Error(`Failed to update task assignees: ${response.status()}`);
  }
}

/**
 * Get tomorrow's date at specific time
 */
export function getTomorrowAt(hours: number, minutes: number = 0): Date {
  const tomorrow = new Date();
  tomorrow.setDate(tomorrow.getDate() + 1);
  tomorrow.setHours(hours, minutes, 0, 0);
  return tomorrow;
}

/**
 * Get date X hours from now
 */
export function getDateHoursFromNow(hours: number): Date {
  const date = new Date();
  date.setHours(date.getHours() + hours);
  return date;
}

/**
 * Format date for API (YYYY-MM-DD)
 */
export function formatDateForAPI(date: Date): string {
  return date.toISOString().split('T')[0];
}

/**
 * Format datetime for API (ISO string)
 */
export function formatDateTimeForAPI(date: Date): string {
  return date.toISOString();
}

/**
 * Wait for real-time notification update
 * Useful for WebSocket/polling based notifications
 */
export async function waitForRealtimeUpdate(
  page: Page,
  timeout = 5000
): Promise<void> {
  await page.waitForTimeout(timeout);
}

/**
 * Mock email service to capture sent emails
 * This is a placeholder - adjust based on your email service implementation
 */
export async function setupEmailMock(page: Page): Promise<void> {
  await page.route('**/api/email/**', async (route) => {
    // Intercept email sending and log it
    const request = route.request();
    console.log('[EMAIL MOCK] Email would be sent:', {
      url: request.url(),
      method: request.method(),
      postData: request.postDataJSON(),
    });

    // Continue with success response
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ success: true }),
    });
  });
}

/**
 * Verify email was sent (mock verification)
 * This is a placeholder - adjust based on your email testing strategy
 */
export function verifyEmailSent(
  emailData: any,
  expectedRecipient: string,
  expectedSubject?: string
): boolean {
  // TODO: Implement based on your email testing approach
  // Options:
  // 1. Check email service mock/spy
  // 2. Query test email service (Mailhog, Mailtrap)
  // 3. Check database for sent emails
  return true;
}
