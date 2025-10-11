// Notification types based on the API documentation

export type NotificationType =
  // Comment events
  | "MENTION"                       // User mentioned in comment
  | "COMMENT_REPLY"                 // Reply to user's comment

  // Task events
  | "TASK_ASSIGNED"                 // Task assigned to user
  | "TASK_COMPLETED"                // Task marked as complete
  | "TASK_DEADLINE_APPROACHING"     // Task deadline approaching

  // Project events
  | "PROJECT_INVITE"                // Invited to project
  | "PROJECT_MEMBER_JOINED"         // New member joined project
  | "PROJECT_DEADLINE_APPROACHING"  // Project deadline approaching

  // User events
  | "USER_REGISTERED"               // Welcome notification
  | "PASSWORD_RESET_REQUESTED"      // Password reset

  // System events
  | "SYSTEM_MAINTENANCE"            // System maintenance
  | "SECURITY_ALERT";               // Security alert

export type Channel =
  | "IN_APP"   // In-application notification
  | "EMAIL"    // Email notification
  | "SMS";     // SMS notification

export type Priority =
  | "LOW"      // Low priority (e.g., informational)
  | "MEDIUM"   // Medium priority (e.g., task updates)
  | "HIGH";    // High priority (e.g., mentions, urgent tasks)

export interface NotificationDto {
  notificationId: number;      // Unique notification ID
  authorId: number | null;     // User who triggered the notification (null for system)
  targetId: number;            // User who receives this notification
  notificationType: NotificationType;  // Type of notification (see enum above)
  subject: string;             // Short title (e.g., "You were mentioned")
  message: string;             // Full message content
  channels: Channel[];         // Delivery channels (IN_APP, EMAIL, SMS)
  readStatus: boolean;         // true if user has read it
  dismissedStatus: boolean;    // true if user dismissed it
  priority: Priority;          // LOW, MEDIUM, HIGH
  link: string | null;         // Navigation link (e.g., "/tasks/123/comments")
  createdAt: string;           // ISO 8601 timestamp (UTC)
  readAt: string | null;       // When marked as read (ISO 8601, nullable)
}

export interface UnreadCountDto {
  count: number;  // Number of unread notifications
}

// Frontend-specific interfaces for UI state management
export interface NotificationFilter {
  unreadOnly: boolean;
  priority?: Priority;
  type?: NotificationType;
}

export interface NotificationBulkAction {
  type: 'markAsRead' | 'dismiss';
  notificationIds: number[];
}

export interface NotificationState {
  notifications: NotificationDto[];
  unreadCount: number;
  isConnected: boolean;
  isLoading: boolean;
  error: string | null;
  selectedIds: Set<number>;
  filter: NotificationFilter;
}

// WebSocket message type for real-time updates
export interface NotificationWebSocketMessage {
  type: 'notification' | 'unreadCountUpdate' | 'markAsRead' | 'dismiss';
  data: NotificationDto | UnreadCountDto | { notificationId: number };
}