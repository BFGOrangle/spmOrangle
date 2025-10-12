import { 
  NotificationDto, 
  UnreadCountDto, 
  NotificationFilter,
  NotificationBulkAction 
} from '@/types/notification';
import { AuthenticatedApiClient } from './authenticated-api-client';

export class NotificationService {
  private static readonly BASE_URL = '/api/notifications';
  private static client = new AuthenticatedApiClient();

  /**
   * Fetch all notifications for the current user
   * @param filter - Optional filter to apply (e.g., unreadOnly)
   * @returns Promise<NotificationDto[]>
   */
  static async getNotifications(filter?: NotificationFilter): Promise<NotificationDto[]> {
    try {
      let url = this.BASE_URL;
      const params = new URLSearchParams();

      if (filter?.unreadOnly) {
        params.append('unreadOnly', 'true');
      }

      if (params.toString()) {
        url += `?${params.toString()}`;
      }

      const notifications: NotificationDto[] = await this.client.get(url);
      
      // Sort by newest first (reverse chronological order)
      return notifications.sort((a, b) => 
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );
    } catch (error) {
      console.error('Error fetching notifications:', error);
      throw error;
    }
  }

  /**
   * Get the count of unread notifications
   * @returns Promise<number>
   */
  static async getUnreadCount(): Promise<number> {
    try {
      const data: UnreadCountDto = await this.client.get(`${this.BASE_URL}/unread-count`);
      return data.count;
    } catch (error) {
      console.error('Error fetching unread count:', error);
      throw error;
    }
  }

  /**
   * Mark a single notification as read
   * @param notificationId - ID of the notification to mark as read
   * @returns Promise<void>
   */
  static async markAsRead(notificationId: number): Promise<void> {
    try {
      await this.client.patch(`${this.BASE_URL}/${notificationId}/read`, null);
    } catch (error) {
      console.error('Error marking notification as read:', error);
      throw error;
    }
  }

  /**
   * Mark all notifications as read
   * @returns Promise<void>
   */
  static async markAllAsRead(): Promise<void> {
    try {
      await this.client.patch(`${this.BASE_URL}/mark-all-read`, null);
    } catch (error) {
      console.error('Error marking all notifications as read:', error);
      throw error;
    }
  }

  /**
   * Dismiss (archive) a single notification
   * Note: This endpoint might not be implemented in the backend yet
   * @param notificationId - ID of the notification to dismiss
   * @returns Promise<void>
   */
  static async dismissNotification(notificationId: number): Promise<void> {
    try {
      await this.client.patch(`${this.BASE_URL}/${notificationId}/dismiss`, null);
    } catch (error) {
      console.error('Error dismissing notification:', error);
      throw error;
    }
  }

  /**
   * Perform bulk actions on multiple notifications
   * @param action - The bulk action to perform
   * @returns Promise<void>
   */
  static async performBulkAction(action: NotificationBulkAction): Promise<void> {
    try {
      const promises = action.notificationIds.map(id => {
        switch (action.type) {
          case 'markAsRead':
            return this.markAsRead(id);
          case 'dismiss':
            return this.dismissNotification(id);
          default:
            throw new Error(`Unknown bulk action type: ${action.type}`);
        }
      });

      await Promise.all(promises);
    } catch (error) {
      console.error('Error performing bulk action:', error);
      throw error;
    }
  }

  /**
   * Mark multiple notifications as read in bulk
   * This is a convenience method for better performance if the backend supports it
   * @param notificationIds - Array of notification IDs to mark as read
   * @returns Promise<void>
   */
  static async markMultipleAsRead(notificationIds: number[]): Promise<void> {
    try {
      // For now, we'll use individual API calls
      // TODO: If backend implements a bulk endpoint, use that instead
      const promises = notificationIds.map(id => this.markAsRead(id));
      await Promise.all(promises);
    } catch (error) {
      console.error('Error marking multiple notifications as read:', error);
      throw error;
    }
  }

  /**
   * Dismiss multiple notifications in bulk
   * @param notificationIds - Array of notification IDs to dismiss
   * @returns Promise<void>
   */
  static async dismissMultiple(notificationIds: number[]): Promise<void> {
    try {
      const promises = notificationIds.map(id => this.dismissNotification(id));
      await Promise.all(promises);
    } catch (error) {
      console.error('Error dismissing multiple notifications:', error);
      throw error;
    }
  }
}

// Create a singleton instance for easier import
export const notificationService = NotificationService;