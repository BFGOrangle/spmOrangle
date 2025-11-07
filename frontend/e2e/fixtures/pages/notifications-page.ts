import { Page, Locator } from '@playwright/test';
import { BasePage } from './base-page';

/**
 * Notifications Page Object
 * Handles all interactions with the notifications panel and notification-related UI elements
 */
export class NotificationsPage extends BasePage {
  // Locators
  private readonly notificationBellIcon: Locator;
  private readonly notificationPanel: Locator;
  private readonly notificationBadge: Locator;
  private readonly notificationsList: Locator;
  private readonly notificationItem: Locator;
  private readonly markAsReadButton: Locator;
  private readonly dismissButton: Locator;
  private readonly bulkSelectCheckbox: Locator;
  private readonly bulkMarkAsReadButton: Locator;
  private readonly bulkDismissButton: Locator;

  constructor(page: Page) {
    super(page);

    // Initialize locators
    this.notificationBellIcon = page.getByTestId('notification-bell-icon');
    this.notificationPanel = page.getByTestId('notification-panel');
    this.notificationBadge = page.getByTestId('notification-badge');
    this.notificationsList = page.getByTestId('notifications-list');
    this.notificationItem = page.getByTestId('notification-item');
    this.markAsReadButton = page.getByRole('button', { name: /mark as read/i });
    this.dismissButton = page.getByRole('button', { name: /dismiss/i });
    this.bulkSelectCheckbox = page.getByTestId('bulk-select-checkbox');
    this.bulkMarkAsReadButton = page.getByTestId('bulk-mark-as-read');
    this.bulkDismissButton = page.getByTestId('bulk-dismiss');
  }

  /**
   * Open the notifications panel
   */
  async openNotificationsPanel(): Promise<void> {
    await this.click(this.notificationBellIcon);
    await this.waitForVisible(this.notificationPanel);
  }

  /**
   * Close the notifications panel
   */
  async closeNotificationsPanel(): Promise<void> {
    // Click outside the panel or press Escape
    await this.pressKey('Escape');
    await this.waitForHidden(this.notificationPanel);
  }

  /**
   * Get notification badge count
   */
  async getNotificationBadgeCount(): Promise<number> {
    const badgeText = await this.getText(this.notificationBadge);
    return badgeText ? parseInt(badgeText, 10) : 0;
  }

  /**
   * Check if notification badge is visible
   */
  async isNotificationBadgeVisible(): Promise<boolean> {
    return await this.isVisible(this.notificationBadge);
  }

  /**
   * Get all notifications
   */
  async getAllNotifications(): Promise<Locator> {
    return this.notificationItem;
  }

  /**
   * Get notification count
   */
  async getNotificationCount(): Promise<number> {
    return await this.getCount(this.notificationItem);
  }

  /**
   * Get notification by index (0-based)
   */
  getNotificationByIndex(index: number): Locator {
    return this.notificationItem.nth(index);
  }

  /**
   * Get notification by text content
   */
  getNotificationByText(text: string | RegExp): Locator {
    return this.page.getByTestId('notification-item').filter({ hasText: text });
  }

  /**
   * Click on a notification
   */
  async clickNotification(index: number): Promise<void> {
    await this.click(this.getNotificationByIndex(index));
  }

  /**
   * Click on a notification by text
   */
  async clickNotificationByText(text: string | RegExp): Promise<void> {
    await this.click(this.getNotificationByText(text));
  }

  /**
   * Mark notification as read by index
   */
  async markNotificationAsRead(index: number): Promise<void> {
    const notification = this.getNotificationByIndex(index);
    await this.hover(notification);
    await this.click(notification.getByRole('button', { name: /mark as read/i }));
  }

  /**
   * Dismiss notification by index
   */
  async dismissNotification(index: number): Promise<void> {
    const notification = this.getNotificationByIndex(index);
    await this.hover(notification);
    await this.click(notification.getByRole('button', { name: /dismiss/i }));
  }

  /**
   * Check if notification is read
   */
  async isNotificationRead(index: number): Promise<boolean> {
    const notification = this.getNotificationByIndex(index);
    const className = await this.getAttribute(notification, 'class');
    return className?.includes('read') || false;
  }

  /**
   * Check if notification is unread
   */
  async isNotificationUnread(index: number): Promise<boolean> {
    return !(await this.isNotificationRead(index));
  }

  /**
   * Get notification text content
   */
  async getNotificationText(index: number): Promise<string | null> {
    const notification = this.getNotificationByIndex(index);
    return await this.getText(notification);
  }

  /**
   * Get notification timestamp
   */
  async getNotificationTimestamp(index: number): Promise<string | null> {
    const notification = this.getNotificationByIndex(index);
    const timestamp = notification.getByTestId('notification-timestamp');
    return await this.getText(timestamp);
  }

  /**
   * Get notification snippet
   */
  async getNotificationSnippet(index: number): Promise<string | null> {
    const notification = this.getNotificationByIndex(index);
    const snippet = notification.getByTestId('notification-snippet');
    return await this.getText(snippet);
  }

  /**
   * Get notification type/event
   */
  async getNotificationType(index: number): Promise<string | null> {
    const notification = this.getNotificationByIndex(index);
    const type = notification.getByTestId('notification-type');
    return await this.getText(type);
  }

  /**
   * Select notification for bulk action
   */
  async selectNotificationForBulkAction(index: number): Promise<void> {
    const notification = this.getNotificationByIndex(index);
    const checkbox = notification.getByRole('checkbox');
    await this.check(checkbox);
  }

  /**
   * Deselect notification from bulk action
   */
  async deselectNotificationFromBulkAction(index: number): Promise<void> {
    const notification = this.getNotificationByIndex(index);
    const checkbox = notification.getByRole('checkbox');
    await this.uncheck(checkbox);
  }

  /**
   * Select multiple notifications for bulk action
   */
  async selectMultipleNotifications(indices: number[]): Promise<void> {
    for (const index of indices) {
      await this.selectNotificationForBulkAction(index);
    }
  }

  /**
   * Perform bulk mark as read
   */
  async bulkMarkAsRead(): Promise<void> {
    await this.click(this.bulkMarkAsReadButton);
  }

  /**
   * Perform bulk dismiss
   */
  async bulkDismiss(): Promise<void> {
    await this.click(this.bulkDismissButton);
  }

  /**
   * Check if notification contains mention indicator
   */
  async hasMentionIndicator(index: number): Promise<boolean> {
    const notification = this.getNotificationByIndex(index);
    const mentionIcon = notification.getByTestId('mention-indicator');
    return await this.isVisible(mentionIcon);
  }

  /**
   * Check if notification contains task link
   */
  async hasTaskLink(index: number): Promise<boolean> {
    const notification = this.getNotificationByIndex(index);
    const taskLink = notification.getByRole('link');
    return await this.isVisible(taskLink);
  }

  /**
   * Assert notification panel is open
   */
  async assertNotificationPanelOpen(): Promise<void> {
    await this.assertVisible(this.notificationPanel);
  }

  /**
   * Assert notification panel is closed
   */
  async assertNotificationPanelClosed(): Promise<void> {
    await this.assertHidden(this.notificationPanel);
  }

  /**
   * Assert notification badge count
   */
  async assertNotificationBadgeCount(expectedCount: number): Promise<void> {
    const actualCount = await this.getNotificationBadgeCount();
    if (actualCount !== expectedCount) {
      throw new Error(`Expected notification badge count to be ${expectedCount}, but got ${actualCount}`);
    }
  }

  /**
   * Assert notification exists by text
   */
  async assertNotificationExists(text: string | RegExp): Promise<void> {
    await this.assertVisible(this.getNotificationByText(text));
  }

  /**
   * Assert notification does not exist by text
   */
  async assertNotificationNotExists(text: string | RegExp): Promise<void> {
    await this.assertHidden(this.getNotificationByText(text));
  }

  /**
   * Assert notification count
   */
  async assertNotificationCount(expectedCount: number): Promise<void> {
    await this.assertCount(this.notificationItem, expectedCount);
  }

  /**
   * Wait for notification to appear
   */
  async waitForNotification(text: string | RegExp, timeout = 10000): Promise<void> {
    await this.waitForVisible(this.getNotificationByText(text), timeout);
  }

  /**
   * Wait for notification badge to update
   */
  async waitForBadgeUpdate(timeout = 5000): Promise<void> {
    await this.wait(timeout);
  }
}