import { NotificationService } from '@/services/notification-service';
import { AuthenticatedApiClient } from '@/services/authenticated-api-client';
import { NotificationDto, NotificationFilter } from '@/types/notification';

// Mock the AuthenticatedApiClient
jest.mock('@/services/authenticated-api-client', () => ({
  AuthenticatedApiClient: jest.fn().mockImplementation(() => ({
    get: jest.fn(),
    patch: jest.fn(),
  })),
}));

describe('NotificationService', () => {
  let mockClient: jest.Mocked<AuthenticatedApiClient>;

  beforeEach(() => {
    mockClient = new AuthenticatedApiClient() as jest.Mocked<AuthenticatedApiClient>;
    (NotificationService as any).client = mockClient;
    jest.clearAllMocks();
  });

  describe('getNotifications', () => {
    const mockNotifications: NotificationDto[] = [
      {
        notificationId: 1,
        authorId: 100,
        targetId: 1,
        notificationType: 'MENTION',
        subject: 'You were mentioned',
        message: 'Sarah mentioned you in a comment',
        channels: ['IN_APP'],
        readStatus: false,
        dismissedStatus: false,
        priority: 'HIGH',
        link: '/tasks/123/comments',
        createdAt: '2025-01-15T09:30:00Z',
        readAt: null,
      },
      {
        notificationId: 2,
        authorId: 101,
        targetId: 1,
        notificationType: 'TASK_ASSIGNED',
        subject: 'New task assigned',
        message: 'You have been assigned to Fix login bug',
        channels: ['IN_APP'],
        readStatus: true,
        dismissedStatus: false,
        priority: 'MEDIUM',
        link: '/tasks/456',
        createdAt: '2025-01-14T10:30:00Z',
        readAt: '2025-01-14T11:00:00Z',
      },
    ];

    it('should fetch all notifications without filter', async () => {
      mockClient.get.mockResolvedValue(mockNotifications);

      const result = await NotificationService.getNotifications();

      expect(mockClient.get).toHaveBeenCalledWith('/api/notifications');
      expect(result).toEqual(mockNotifications);
    });

    it('should fetch unread notifications only', async () => {
      const filter: NotificationFilter = { unreadOnly: true };
      mockClient.get.mockResolvedValue(mockNotifications);

      const result = await NotificationService.getNotifications(filter);

      expect(mockClient.get).toHaveBeenCalledWith('/api/notifications?unreadOnly=true');
      expect(result).toEqual(mockNotifications);
    });

    it('should sort notifications by newest first', async () => {
      const unsortedNotifications = [...mockNotifications].reverse();
      mockClient.get.mockResolvedValue(unsortedNotifications);

      const result = await NotificationService.getNotifications();

      expect(result[0].notificationId).toBe(1); // Newer notification first
      expect(result[1].notificationId).toBe(2);
    });

    it('should handle API errors gracefully', async () => {
      const error = new Error('API Error');
      mockClient.get.mockRejectedValue(error);

      await expect(NotificationService.getNotifications()).rejects.toThrow('API Error');
    });
  });

  describe('getUnreadCount', () => {
    it('should fetch unread count', async () => {
      const mockResponse = { count: 5 };
      mockClient.get.mockResolvedValue(mockResponse);

      const result = await NotificationService.getUnreadCount();

      expect(mockClient.get).toHaveBeenCalledWith('/api/notifications/unread-count');
      expect(result).toBe(5);
    });

    it('should handle API errors', async () => {
      const error = new Error('Failed to fetch');
      mockClient.get.mockRejectedValue(error);

      await expect(NotificationService.getUnreadCount()).rejects.toThrow('Failed to fetch');
    });
  });

  describe('markAsRead', () => {
    it('should mark notification as read', async () => {
      mockClient.patch.mockResolvedValue(undefined);

      await NotificationService.markAsRead(1);

      expect(mockClient.patch).toHaveBeenCalledWith('/api/notifications/1/read', null);
    });

    it('should handle API errors', async () => {
      const error = new Error('Failed to mark as read');
      mockClient.patch.mockRejectedValue(error);

      await expect(NotificationService.markAsRead(1)).rejects.toThrow('Failed to mark as read');
    });
  });

  describe('markAllAsRead', () => {
    it('should mark all notifications as read', async () => {
      mockClient.patch.mockResolvedValue(undefined);

      await NotificationService.markAllAsRead();

      expect(mockClient.patch).toHaveBeenCalledWith('/api/notifications/mark-all-read', null);
    });

    it('should handle API errors', async () => {
      const error = new Error('Failed to mark all as read');
      mockClient.patch.mockRejectedValue(error);

      await expect(NotificationService.markAllAsRead()).rejects.toThrow('Failed to mark all as read');
    });
  });

  describe('dismissNotification', () => {
    it('should dismiss notification', async () => {
      mockClient.patch.mockResolvedValue(undefined);

      await NotificationService.dismissNotification(1);

      expect(mockClient.patch).toHaveBeenCalledWith('/api/notifications/1/dismiss', null);
    });

    it('should handle API errors', async () => {
      const error = new Error('Failed to dismiss');
      mockClient.patch.mockRejectedValue(error);

      await expect(NotificationService.dismissNotification(1)).rejects.toThrow('Failed to dismiss');
    });
  });

  describe('performBulkAction', () => {
    it('should perform bulk mark as read action', async () => {
      mockClient.patch.mockResolvedValue(undefined);

      await NotificationService.performBulkAction({
        type: 'markAsRead',
        notificationIds: [1, 2, 3],
      });

      expect(mockClient.patch).toHaveBeenCalledTimes(3);
      expect(mockClient.patch).toHaveBeenCalledWith('/api/notifications/1/read', null);
      expect(mockClient.patch).toHaveBeenCalledWith('/api/notifications/2/read', null);
      expect(mockClient.patch).toHaveBeenCalledWith('/api/notifications/3/read', null);
    });

    it('should perform bulk dismiss action', async () => {
      mockClient.patch.mockResolvedValue(undefined);

      await NotificationService.performBulkAction({
        type: 'dismiss',
        notificationIds: [1, 2],
      });

      expect(mockClient.patch).toHaveBeenCalledTimes(2);
      expect(mockClient.patch).toHaveBeenCalledWith('/api/notifications/1/dismiss', null);
      expect(mockClient.patch).toHaveBeenCalledWith('/api/notifications/2/dismiss', null);
    });

    it('should handle unknown action types', async () => {
      await expect(
        NotificationService.performBulkAction({
          type: 'unknown' as any,
          notificationIds: [1],
        })
      ).rejects.toThrow('Unknown bulk action type: unknown');
    });
  });

  describe('markMultipleAsRead', () => {
    it('should mark multiple notifications as read', async () => {
      mockClient.patch.mockResolvedValue(undefined);

      await NotificationService.markMultipleAsRead([1, 2, 3]);

      expect(mockClient.patch).toHaveBeenCalledTimes(3);
    });

    it('should handle errors', async () => {
      const error = new Error('Failed to mark as read');
      mockClient.patch.mockRejectedValue(error);

      await expect(NotificationService.markMultipleAsRead([1, 2])).rejects.toThrow();
    });
  });

  describe('dismissMultiple', () => {
    it('should dismiss multiple notifications', async () => {
      mockClient.patch.mockResolvedValue(undefined);

      await NotificationService.dismissMultiple([1, 2]);

      expect(mockClient.patch).toHaveBeenCalledTimes(2);
    });

    it('should handle errors', async () => {
      const error = new Error('Failed to dismiss');
      mockClient.patch.mockRejectedValue(error);

      await expect(NotificationService.dismissMultiple([1])).rejects.toThrow();
    });
  });
});