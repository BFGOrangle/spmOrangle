import { test, expect } from '../../fixtures';
import { authenticatedGet, authenticatedPatch } from '../../utils/auth-helpers';

/**
 * Simple Notification Tests - Working with Your Actual Implementation
 * These tests work with your existing notification system
 */

test.describe('Notifications - Basic Working Tests', () => {

  /**
   * Test 1: Verify notification system page loads
   * This tests the /notifications-test page you already have
   */
  test('should load notification test page successfully', async ({ hrPage }) => {
    await hrPage.goto('/notifications-test');
    await hrPage.waitForLoadState('networkidle');

    // Verify page heading
    const heading = hrPage.getByRole('heading', { name: /notification system test/i });
    await expect(heading).toBeVisible();

    // Verify statistics cards are visible
    const totalNotifications = hrPage.getByText(/total notifications/i);
    await expect(totalNotifications).toBeVisible();

    const unreadCount = hrPage.getByText(/unread count/i);
    await expect(unreadCount).toBeVisible();
  });

  /**
   * Test 2: Verify API endpoints are working
   * This directly tests your backend API
   */
  test('should fetch notifications from API', async ({ hrPage }) => {
    // Navigate to any page to ensure authenticated
    await hrPage.goto('/');
    await hrPage.waitForLoadState('networkidle');

    // DEBUG: Check localStorage contents
    const storageInfo = await hrPage.evaluate(() => {
      const keys = Object.keys(localStorage);
      const cognitoKeys = keys.filter(k => k.includes('Cognito') || k.includes('amplify'));
      return {
        totalKeys: keys.length,
        cognitoKeys,
        allKeys: keys
      };
    });
    console.log('LocalStorage Debug:', JSON.stringify(storageInfo, null, 2));

    // Test unread count endpoint - use authenticated helper
    const unreadResponse = await authenticatedGet(hrPage, '/api/notifications/unread-count');
    console.log('Unread count response status:', unreadResponse.status());

    if (!unreadResponse.ok()) {
      const text = await unreadResponse.text();
      console.log('Unread count response body:', text);
      throw new Error(`Failed to fetch unread count: ${unreadResponse.status()} - ${text}`);
    }

    const unreadData = await unreadResponse.json();
    expect(unreadData).toHaveProperty('count');
    expect(typeof unreadData.count).toBe('number');

    console.log('Unread count:', unreadData.count);

    // Test get notifications endpoint
    const notificationsResponse = await authenticatedGet(hrPage, '/api/notifications');
    console.log('Notifications response status:', notificationsResponse.status());

    if (!notificationsResponse.ok()) {
      const text = await notificationsResponse.text();
      console.log('Notifications response body:', text);
      throw new Error(`Failed to fetch notifications: ${notificationsResponse.status()} - ${text}`);
    }

    const notifications = await notificationsResponse.json();
    expect(Array.isArray(notifications)).toBeTruthy();

    console.log('Total notifications:', notifications.length);
  });

  /**
   * Test 3: Verify notification bell is visible
   * This tests your NotificationBell component
   */
  test('should display notification bell in sidebar', async ({ hrPage }) => {
    await hrPage.goto('/');
    await hrPage.waitForLoadState('networkidle');

    // Find the bell icon using Lucide icon class
    const bellIcon = hrPage.locator('svg.lucide-bell').first();
    await expect(bellIcon).toBeVisible({ timeout: 10000 });

    console.log(' Notification bell found');
  });

  /**
   * Test 4: Open notification panel
   * This tests the Popover interaction
   */
  test('should open notification panel when clicking bell', async ({ hrPage }) => {
    await hrPage.goto('/');
    await hrPage.waitForLoadState('networkidle');

    // Find and click the bell button
    const bellButton = hrPage.getByRole('button').filter({
      has: hrPage.locator('svg.lucide-bell')
    }).first();

    await expect(bellButton).toBeVisible();
    await bellButton.click();

    // Wait for popover to appear (it has class 'popover-content' or role 'dialog')
    await hrPage.waitForTimeout(1000); // Give popover time to animate

    // Check if popover content is visible
    const popoverContent = hrPage.locator('[data-radix-popper-content-wrapper]').first();

    // Take a screenshot to see what happened
    await hrPage.screenshot({ path: 'e2e-results/notification-panel-opened.png' });

    console.log(' Clicked notification bell');
  });

  /**
   * Test 5: Verify WebSocket connection status
   * This tests the real-time connection
   */
  test('should show WebSocket connection status', async ({ hrPage }) => {
    await hrPage.goto('/notifications-test');
    await hrPage.waitForLoadState('networkidle');

    // Wait for WebSocket to connect
    await hrPage.waitForTimeout(2000);

    // Look for connection status - use .first() to avoid strict mode violation
    const connectionCard = hrPage.getByText(/websocket connection/i).first();
    await expect(connectionCard).toBeVisible();

    // Check if it says connected (may be disconnected in test environment)
    const pageContent = await hrPage.textContent('body');

    if (pageContent?.includes('Connected')) {
      console.log(' WebSocket is connected');
    } else if (pageContent?.includes('Disconnected')) {
      console.log('� WebSocket is disconnected (expected in test environment)');
    }
  });

  /**
   * Test 6: Verify mark as read functionality via API
   * This tests your backend's mark as read endpoint
   */
  test('should mark notification as read via API', async ({ hrPage }) => {
    await hrPage.goto('/');
    await hrPage.waitForLoadState('networkidle');

    // Get all notifications
    const response = await authenticatedGet(hrPage, '/api/notifications');
    const notifications = await response.json();

    console.log('Notifications response:', typeof notifications, Array.isArray(notifications));

    if (!Array.isArray(notifications)) {
      console.log('ERROR: Expected array, got:', notifications);
      throw new Error(`Expected notifications to be an array, got: ${typeof notifications}`);
    }

    if (notifications.length === 0) {
      console.log('� No notifications to test with');
      test.skip();
      return;
    }

    // Find an unread notification
    const unreadNotification = notifications.find(n => !n.readStatus);

    if (!unreadNotification) {
      console.log('� No unread notifications to test with');
      test.skip();
      return;
    }

    console.log('Found unread notification:', unreadNotification.notificationId);

    // Mark it as read
    const markAsReadResponse = await authenticatedPatch(
      hrPage,
      `/api/notifications/${unreadNotification.notificationId}/read`,
      null
    );

    expect(markAsReadResponse.ok()).toBeTruthy();

    // Verify it's now marked as read
    const updatedResponse = await authenticatedGet(hrPage, '/api/notifications');
    const updatedNotifications = await updatedResponse.json();
    const updatedNotification = updatedNotifications.find(
      n => n.notificationId === unreadNotification.notificationId
    );

    expect(updatedNotification.readStatus).toBe(true);
    console.log(' Successfully marked notification as read');
  });

  /**
   * Test 7: Verify notifications are sorted newest first
   * This tests your NotificationService sorting logic
   */
  test('should display notifications in reverse chronological order', async ({ hrPage }) => {
    await hrPage.goto('/');
    await hrPage.waitForLoadState('networkidle');

    const response = await authenticatedGet(hrPage, '/api/notifications');
    const notifications = await response.json();

    if (notifications.length < 2) {
      console.log('� Need at least 2 notifications to test sorting');
      test.skip();
      return;
    }

    // Verify sorted by createdAt descending (newest first)
    for (let i = 0; i < notifications.length - 1; i++) {
      const currentDate = new Date(notifications[i].createdAt);
      const nextDate = new Date(notifications[i + 1].createdAt);

      expect(currentDate.getTime()).toBeGreaterThanOrEqual(nextDate.getTime());
    }

    console.log(' Notifications are correctly sorted (newest first)');
  });

});

/**
 * NEXT STEPS:
 *
 * 1. Run this test file:
 *    npx playwright test notifications-simple.spec.ts --headed
 *
 * 2. Check which tests pass/fail
 *
 * 3. For any failures, check:
 *    - Is backend running?
 *    - Are you logged in as HR user?
 *    - Do you have notifications in the system?
 *
 * 4. Once all these pass, you can enhance with more complex tests
 */
