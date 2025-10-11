import { 
  NotificationDto, 
  NotificationType, 
  Channel, 
  Priority, 
  NotificationFilter, 
  NotificationBulkAction, 
  NotificationState,
  NotificationWebSocketMessage,
  UnreadCountDto
} from '@/types/notification';

describe('Notification Types', () => {
  describe('NotificationDto', () => {
    it('should create a valid notification DTO', () => {
      const notification: NotificationDto = {
        notificationId: 1,
        authorId: 2,
        targetId: 1,
        notificationType: "TASK_ASSIGNED",
        subject: "Task assigned to you",
        message: "You have been assigned a new task",
        channels: ["IN_APP"],
        readStatus: false,
        dismissedStatus: false,
        priority: "HIGH",
        link: "/tasks/1",
        createdAt: "2024-01-01T10:00:00Z",
        readAt: null,
      };

      expect(notification.notificationId).toBe(1);
      expect(notification.authorId).toBe(2);
      expect(notification.targetId).toBe(1);
      expect(notification.notificationType).toBe("TASK_ASSIGNED");
      expect(notification.subject).toBe("Task assigned to you");
      expect(notification.message).toBe("You have been assigned a new task");
      expect(notification.channels).toEqual(["IN_APP"]);
      expect(notification.readStatus).toBe(false);
      expect(notification.dismissedStatus).toBe(false);
      expect(notification.priority).toBe("HIGH");
      expect(notification.link).toBe("/tasks/1");
      expect(notification.createdAt).toBe("2024-01-01T10:00:00Z");
      expect(notification.readAt).toBe(null);
    });

    it('should allow null values for optional fields', () => {
      const notification: NotificationDto = {
        notificationId: 1,
        authorId: null, // System notification
        targetId: 1,
        notificationType: "SYSTEM_MAINTENANCE",
        subject: "System maintenance",
        message: "Scheduled maintenance tonight",
        channels: ["IN_APP", "EMAIL"],
        readStatus: true,
        dismissedStatus: false,
        priority: "MEDIUM",
        link: null, // No navigation link
        createdAt: "2024-01-01T10:00:00Z",
        readAt: "2024-01-01T10:30:00Z",
      };

      expect(notification.authorId).toBe(null);
      expect(notification.link).toBe(null);
      expect(notification.readAt).toBe("2024-01-01T10:30:00Z");
    });

    it('should support multiple channels', () => {
      const notification: NotificationDto = {
        notificationId: 1,
        authorId: 2,
        targetId: 1,
        notificationType: "SECURITY_ALERT",
        subject: "Security alert",
        message: "Suspicious login detected",
        channels: ["IN_APP", "EMAIL", "SMS"],
        readStatus: false,
        dismissedStatus: false,
        priority: "HIGH",
        link: "/security",
        createdAt: "2024-01-01T10:00:00Z",
        readAt: null,
      };

      expect(notification.channels).toHaveLength(3);
      expect(notification.channels).toContain("IN_APP");
      expect(notification.channels).toContain("EMAIL");
      expect(notification.channels).toContain("SMS");
    });
  });

  describe('NotificationType', () => {
    it('should include all expected notification types', () => {
      const types: NotificationType[] = [
        "MENTION",
        "COMMENT_REPLY",
        "TASK_ASSIGNED",
        "TASK_COMPLETED",
        "TASK_DEADLINE_APPROACHING",
        "PROJECT_INVITE",
        "PROJECT_MEMBER_JOINED",
        "PROJECT_DEADLINE_APPROACHING",
        "USER_REGISTERED",
        "PASSWORD_RESET_REQUESTED",
        "SYSTEM_MAINTENANCE",
        "SECURITY_ALERT"
      ];

      types.forEach(type => {
        expect(type).toBeDefined();
        expect(typeof type).toBe('string');
      });
    });
  });

  describe('Channel', () => {
    it('should include all expected channels', () => {
      const channels: Channel[] = ["IN_APP", "EMAIL", "SMS"];

      channels.forEach(channel => {
        expect(channel).toBeDefined();
        expect(typeof channel).toBe('string');
      });
    });
  });

  describe('Priority', () => {
    it('should include all expected priorities', () => {
      const priorities: Priority[] = ["LOW", "MEDIUM", "HIGH"];

      priorities.forEach(priority => {
        expect(priority).toBeDefined();
        expect(typeof priority).toBe('string');
      });
    });
  });

  describe('NotificationFilter', () => {
    it('should create a basic filter', () => {
      const filter: NotificationFilter = {
        unreadOnly: true
      };

      expect(filter.unreadOnly).toBe(true);
      expect(filter.priority).toBeUndefined();
      expect(filter.type).toBeUndefined();
    });

    it('should create a filter with all options', () => {
      const filter: NotificationFilter = {
        unreadOnly: false,
        priority: "HIGH",
        type: "TASK_ASSIGNED"
      };

      expect(filter.unreadOnly).toBe(false);
      expect(filter.priority).toBe("HIGH");
      expect(filter.type).toBe("TASK_ASSIGNED");
    });
  });

  describe('NotificationBulkAction', () => {
    it('should create a mark as read bulk action', () => {
      const action: NotificationBulkAction = {
        type: 'markAsRead',
        notificationIds: [1, 2, 3]
      };

      expect(action.type).toBe('markAsRead');
      expect(action.notificationIds).toEqual([1, 2, 3]);
    });

    it('should create a dismiss bulk action', () => {
      const action: NotificationBulkAction = {
        type: 'dismiss',
        notificationIds: [4, 5]
      };

      expect(action.type).toBe('dismiss');
      expect(action.notificationIds).toEqual([4, 5]);
    });
  });

  describe('NotificationState', () => {
    it('should create a valid notification state', () => {
      const state: NotificationState = {
        notifications: [],
        unreadCount: 0,
        isConnected: false,
        isLoading: true,
        error: null,
        selectedIds: new Set<number>(),
        filter: { unreadOnly: false }
      };

      expect(state.notifications).toEqual([]);
      expect(state.unreadCount).toBe(0);
      expect(state.isConnected).toBe(false);
      expect(state.isLoading).toBe(true);
      expect(state.error).toBe(null);
      expect(state.selectedIds).toBeInstanceOf(Set);
      expect(state.selectedIds.size).toBe(0);
      expect(state.filter.unreadOnly).toBe(false);
    });

    it('should handle state with notifications and selections', () => {
      const notifications: NotificationDto[] = [
        {
          notificationId: 1,
          authorId: 2,
          targetId: 1,
          notificationType: "TASK_ASSIGNED",
          subject: "Task assigned",
          message: "New task assigned",
          channels: ["IN_APP"],
          readStatus: false,
          dismissedStatus: false,
          priority: "MEDIUM",
          link: "/tasks/1",
          createdAt: "2024-01-01T10:00:00Z",
          readAt: null,
        }
      ];

      const state: NotificationState = {
        notifications,
        unreadCount: 1,
        isConnected: true,
        isLoading: false,
        error: "Connection error",
        selectedIds: new Set([1, 2]),
        filter: { unreadOnly: true, priority: "HIGH" }
      };

      expect(state.notifications).toEqual(notifications);
      expect(state.unreadCount).toBe(1);
      expect(state.isConnected).toBe(true);
      expect(state.isLoading).toBe(false);
      expect(state.error).toBe("Connection error");
      expect(state.selectedIds.has(1)).toBe(true);
      expect(state.selectedIds.has(2)).toBe(true);
      expect(state.filter.unreadOnly).toBe(true);
      expect(state.filter.priority).toBe("HIGH");
    });
  });

  describe('NotificationWebSocketMessage', () => {
    it('should create a notification message', () => {
      const notification: NotificationDto = {
        notificationId: 1,
        authorId: 2,
        targetId: 1,
        notificationType: "MENTION",
        subject: "You were mentioned",
        message: "Someone mentioned you",
        channels: ["IN_APP"],
        readStatus: false,
        dismissedStatus: false,
        priority: "MEDIUM",
        link: "/comments/1",
        createdAt: "2024-01-01T10:00:00Z",
        readAt: null,
      };

      const message: NotificationWebSocketMessage = {
        type: 'notification',
        data: notification
      };

      expect(message.type).toBe('notification');
      expect(message.data).toEqual(notification);
    });

    it('should create an unread count update message', () => {
      const unreadCount: UnreadCountDto = {
        count: 5
      };

      const message: NotificationWebSocketMessage = {
        type: 'unreadCountUpdate',
        data: unreadCount
      };

      expect(message.type).toBe('unreadCountUpdate');
      expect(message.data).toEqual(unreadCount);
    });

    it('should create a mark as read message', () => {
      const markAsReadData = {
        notificationId: 1
      };

      const message: NotificationWebSocketMessage = {
        type: 'markAsRead',
        data: markAsReadData
      };

      expect(message.type).toBe('markAsRead');
      expect(message.data).toEqual(markAsReadData);
    });

    it('should create a dismiss message', () => {
      const dismissData = {
        notificationId: 2
      };

      const message: NotificationWebSocketMessage = {
        type: 'dismiss',
        data: dismissData
      };

      expect(message.type).toBe('dismiss');
      expect(message.data).toEqual(dismissData);
    });
  });

  describe('UnreadCountDto', () => {
    it('should create a valid unread count DTO', () => {
      const unreadCount: UnreadCountDto = {
        count: 10
      };

      expect(unreadCount.count).toBe(10);
    });

    it('should allow zero count', () => {
      const unreadCount: UnreadCountDto = {
        count: 0
      };

      expect(unreadCount.count).toBe(0);
    });
  });

  describe('Type Guards and Utilities', () => {
    it('should work with type narrowing for notification types', () => {
      const notificationType: NotificationType = "TASK_ASSIGNED";
      
      if (notificationType === "TASK_ASSIGNED") {
        expect(notificationType).toBe("TASK_ASSIGNED");
      }
    });

    it('should work with type narrowing for priorities', () => {
      const priority: Priority = "HIGH";
      
      const priorities: Priority[] = ["HIGH", "MEDIUM", "LOW"];
      expect(priorities).toContain(priority);
      
      // Test each priority value
      const testPriority = (p: Priority) => {
        switch (p) {
          case "HIGH":
            return "high";
          case "MEDIUM":
            return "medium";
          case "LOW":
            return "low";
          default:
            return "unknown";
        }
      };

      expect(testPriority("HIGH")).toBe("high");
      expect(testPriority("MEDIUM")).toBe("medium");
      expect(testPriority("LOW")).toBe("low");
    });

    it('should work with type narrowing for channels', () => {
      const channel: Channel = "EMAIL";
      
      if (channel === "EMAIL" || channel === "SMS" || channel === "IN_APP") {
        expect(["EMAIL", "SMS", "IN_APP"]).toContain(channel);
      }
    });
  });
});