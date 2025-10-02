# Notification API Documentation

Complete guide for implementing in-app notifications with REST API and WebSocket real-time updates.

---

## Table of Contents
- [Overview](#overview)
- [REST API Endpoints](#rest-api-endpoints)
- [WebSocket Real-Time Notifications](#websocket-real-time-notifications)
- [Data Types](#data-types)
- [Frontend Implementation Guide](#frontend-implementation-guide)
- [Testing](#testing)

---

## Overview

The notification system provides:
- **REST API** for fetching and managing notifications
- **WebSocket** for real-time notification delivery
- **Mock data** for frontend development (backend implementation in progress)

**Base URL:** `http://localhost:8080`

---

## REST API Endpoints

### 1. Get Notifications

Fetch all notifications for the current user.

**Endpoint:** `GET /api/notifications`

**Query Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `unreadOnly` | boolean | No | `false` | If `true`, only return unread notifications |

**Request Example:**
```bash
# Get all notifications
GET /api/notifications

# Get only unread notifications
GET /api/notifications?unreadOnly=true
```

**Response:** `200 OK`
```json
[
  {
    "notificationId": 1,
    "authorId": 100,
    "targetId": 1,
    "notificationType": "MENTION",
    "subject": "You were mentioned",
    "message": "Sarah mentioned you in a comment",
    "channels": ["IN_APP"],
    "readStatus": false,
    "dismissedStatus": false,
    "priority": "HIGH",
    "link": "/tasks/123/comments",
    "createdAt": "2025-01-15T09:30:00Z",
    "readAt": null
  },
  {
    "notificationId": 2,
    "authorId": 101,
    "targetId": 1,
    "notificationType": "TASK_ASSIGNED",
    "subject": "New task assigned",
    "message": "You have been assigned to 'Fix login bug'",
    "channels": ["IN_APP"],
    "readStatus": true,
    "dismissedStatus": false,
    "priority": "MEDIUM",
    "link": "/tasks/456",
    "createdAt": "2025-01-14T10:30:00Z",
    "readAt": "2025-01-14T11:00:00Z"
  }
]
```

---

### 2. Get Unread Count

Get the count of unread notifications (for badge).

**Endpoint:** `GET /api/notifications/unread-count`

**Request Example:**
```bash
GET /api/notifications/unread-count
```

**Response:** `200 OK`
```json
{
  "count": 5
}
```

**Use Case:** Display badge count on notification bell icon

---

### 3. Mark Notification as Read

Mark a single notification as read.

**Endpoint:** `PATCH /api/notifications/{id}/read`

**Path Parameters:**
| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | Long | Notification ID to mark as read |

**Request Example:**
```bash
PATCH /api/notifications/1/read
```

**Response:** `204 No Content`

**Use Case:** When user clicks on a notification

---

### 4. Mark All as Read

Mark all user's notifications as read.

**Endpoint:** `PATCH /api/notifications/mark-all-read`

**Request Example:**
```bash
PATCH /api/notifications/mark-all-read
```

**Response:** `204 No Content`

**Use Case:** "Mark all as read" button in notification panel

---

## WebSocket Real-Time Notifications

### Connection Setup

**Endpoint:** `ws://localhost:8080/ws/notifications`
**Protocol:** STOMP over SockJS

**Dependencies Required:**
```bash
npm install sockjs-client @stomp/stompjs
```

**JavaScript Example:**
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// 1. Create WebSocket connection
const socket = new SockJS('http://localhost:8080/ws/notifications');
const stompClient = Stomp.over(socket);

// 2. Connect to server
stompClient.connect({}, (frame) => {
    console.log('Connected to WebSocket:', frame);

    // 3. Subscribe to user-specific notifications
    const userId = getCurrentUserId(); // Replace with your auth logic

    stompClient.subscribe(`/topic/notifications/${userId}`, (message) => {
        const notification = JSON.parse(message.body);
        handleNotification(notification);
    });
});

// 4. Handle disconnection
socket.onclose = () => {
    console.log('WebSocket disconnected');
    // Implement reconnection logic
};

function handleNotification(notification) {
    console.log('New notification:', notification);

    // Update UI
    showNotificationToast(notification.subject, notification.message);
    updateBadgeCount();
    playNotificationSound();

    // Add to notification list if panel is open
    addToNotificationList(notification);
}
```

**TypeScript Example:**
```typescript
import SockJS from 'sockjs-client';
import { Client, Message } from '@stomp/stompjs';

class NotificationWebSocket {
    private stompClient: Client;

    connect(userId: number) {
        const socket = new SockJS('http://localhost:8080/ws/notifications');
        this.stompClient = Stomp.over(socket);

        this.stompClient.connect({}, () => {
            console.log('WebSocket connected');

            this.stompClient.subscribe(
                `/topic/notifications/${userId}`,
                (message: Message) => {
                    const notification: NotificationDto = JSON.parse(message.body);
                    this.handleNotification(notification);
                }
            );
        });
    }

    private handleNotification(notification: NotificationDto) {
        // Handle incoming notification
    }

    disconnect() {
        if (this.stompClient?.connected) {
            this.stompClient.disconnect();
        }
    }
}
```

---

### Available Topics

#### User-Specific Notifications
**Topic Pattern:** `/topic/notifications/{userId}`

**Description:** Receive real-time notifications for a specific user

**When Messages Are Sent:**
- User mentioned in a comment
- Task assigned to user
- Reply to user's comment
- Task deadline approaching
- Project invitation
- System alerts targeted to user

**Example:**
```javascript
// Subscribe to notifications for user ID 123
stompClient.subscribe('/topic/notifications/123', (message) => {
    const notification = JSON.parse(message.body);
    console.log('Received:', notification);
});
```

#### System-Wide Broadcasts (Optional)
**Topic Pattern:** `/topic/notifications/all`

**Description:** Receive system-wide announcements

**When Messages Are Sent:**
- System maintenance notifications
- Security alerts
- Feature announcements

**Example:**
```javascript
// Subscribe to system-wide notifications
stompClient.subscribe('/topic/notifications/all', (message) => {
    const systemAlert = JSON.parse(message.body);
    showSystemAlert(systemAlert);
});
```

---

## Data Types

### NotificationDto

```typescript
interface NotificationDto {
  notificationId: number;      // Unique notification ID
  authorId: number | null;     // User who triggered the notification (null for system)
  targetId: number;            // User who receives this notification
  notificationType: NotificationType;  // Type of notification (see enum below)
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
```

### NotificationType Enum

```typescript
type NotificationType =
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
```

### Channel Enum

```typescript
type Channel =
  | "IN_APP"   // In-application notification
  | "EMAIL"    // Email notification
  | "SMS";     // SMS notification
```

### Priority Enum

```typescript
type Priority =
  | "LOW"      // Low priority (e.g., informational)
  | "MEDIUM"   // Medium priority (e.g., task updates)
  | "HIGH";    // High priority (e.g., mentions, urgent tasks)
```

### UnreadCountDto

```typescript
interface UnreadCountDto {
  count: number;  // Number of unread notifications
}
```

---

## Frontend Implementation Guide

### Complete React Hook Example

```typescript
import { useEffect, useState, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Stomp, Client } from '@stomp/stompjs';

interface UseNotificationsResult {
  notifications: NotificationDto[];
  unreadCount: number;
  isConnected: boolean;
  markAsRead: (notificationId: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  refresh: () => Promise<void>;
}

export function useNotifications(userId: number): UseNotificationsResult {
  const [notifications, setNotifications] = useState<NotificationDto[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isConnected, setIsConnected] = useState(false);
  const [stompClient, setStompClient] = useState<Client | null>(null);

  // Fetch initial notifications
  const fetchNotifications = useCallback(async () => {
    try {
      const response = await fetch('/api/notifications');
      const data = await response.json();
      setNotifications(data);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
    }
  }, []);

  // Fetch unread count
  const fetchUnreadCount = useCallback(async () => {
    try {
      const response = await fetch('/api/notifications/unread-count');
      const data = await response.json();
      setUnreadCount(data.count);
    } catch (error) {
      console.error('Failed to fetch unread count:', error);
    }
  }, []);

  // Mark single notification as read
  const markAsRead = useCallback(async (notificationId: number) => {
    try {
      await fetch(`/api/notifications/${notificationId}/read`, {
        method: 'PATCH',
      });

      // Update local state
      setNotifications(prev =>
        prev.map(n =>
          n.notificationId === notificationId
            ? { ...n, readStatus: true, readAt: new Date().toISOString() }
            : n
        )
      );
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (error) {
      console.error('Failed to mark as read:', error);
    }
  }, []);

  // Mark all as read
  const markAllAsRead = useCallback(async () => {
    try {
      await fetch('/api/notifications/mark-all-read', {
        method: 'PATCH',
      });

      // Update local state
      setNotifications(prev =>
        prev.map(n => ({ ...n, readStatus: true, readAt: new Date().toISOString() }))
      );
      setUnreadCount(0);
    } catch (error) {
      console.error('Failed to mark all as read:', error);
    }
  }, []);

  // Setup WebSocket connection
  useEffect(() => {
    fetchNotifications();
    fetchUnreadCount();

    const socket = new SockJS('/ws/notifications');
    const client = Stomp.over(socket);

    client.connect({}, () => {
      console.log('WebSocket connected');
      setIsConnected(true);

      client.subscribe(`/topic/notifications/${userId}`, (message) => {
        const notification: NotificationDto = JSON.parse(message.body);

        console.log('New notification received:', notification);

        // Add to notifications list
        setNotifications(prev => [notification, ...prev]);

        // Increment unread count if unread
        if (!notification.readStatus) {
          setUnreadCount(prev => prev + 1);
        }

        // Show toast notification
        showNotificationToast(notification);
      });
    });

    socket.onclose = () => {
      console.log('WebSocket disconnected');
      setIsConnected(false);
    };

    setStompClient(client);

    // Cleanup on unmount
    return () => {
      if (client.connected) {
        client.disconnect();
      }
    };
  }, [userId, fetchNotifications, fetchUnreadCount]);

  return {
    notifications,
    unreadCount,
    isConnected,
    markAsRead,
    markAllAsRead,
    refresh: fetchNotifications,
  };
}

function showNotificationToast(notification: NotificationDto) {
  // Implement your toast/snackbar logic here
  console.log('Toast:', notification.subject);
}
```

### UI Component Examples

#### Notification Bell with Badge
```typescript
import { Badge, IconButton, Popover } from '@mui/material';
import NotificationsIcon from '@mui/icons-material/Notifications';

function NotificationBell() {
  const { unreadCount, isConnected } = useNotifications(userId);
  const [anchorEl, setAnchorEl] = useState(null);

  return (
    <>
      <IconButton onClick={(e) => setAnchorEl(e.currentTarget)}>
        <Badge badgeContent={unreadCount} color="error">
          <NotificationsIcon color={isConnected ? 'primary' : 'disabled'} />
        </Badge>
      </IconButton>

      <Popover
        open={Boolean(anchorEl)}
        anchorEl={anchorEl}
        onClose={() => setAnchorEl(null)}
      >
        <NotificationList />
      </Popover>
    </>
  );
}
```

#### Notification List
```typescript
function NotificationList() {
  const { notifications, markAsRead, markAllAsRead } = useNotifications(userId);

  return (
    <div style={{ width: 400, maxHeight: 500 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', padding: 16 }}>
        <h3>Notifications</h3>
        <button onClick={markAllAsRead}>Mark all as read</button>
      </div>

      {notifications.map(notif => (
        <NotificationItem
          key={notif.notificationId}
          notification={notif}
          onRead={() => markAsRead(notif.notificationId)}
        />
      ))}

      {notifications.length === 0 && (
        <div style={{ padding: 32, textAlign: 'center' }}>
          No notifications
        </div>
      )}
    </div>
  );
}
```

#### Individual Notification Item
```typescript
function NotificationItem({ notification, onRead }) {
  const navigate = useNavigate();

  const handleClick = () => {
    if (!notification.readStatus) {
      onRead();
    }
    if (notification.link) {
      navigate(notification.link);
    }
  };

  return (
    <div
      onClick={handleClick}
      style={{
        padding: 12,
        borderBottom: '1px solid #eee',
        backgroundColor: notification.readStatus ? 'white' : '#f0f8ff',
        cursor: 'pointer',
      }}
    >
      <div style={{ display: 'flex', alignItems: 'start', gap: 8 }}>
        {!notification.readStatus && (
          <div style={{
            width: 8,
            height: 8,
            borderRadius: '50%',
            backgroundColor: '#1976d2',
            marginTop: 4,
          }} />
        )}

        <div style={{ flex: 1 }}>
          <div style={{ fontWeight: notification.readStatus ? 'normal' : 'bold' }}>
            {notification.subject}
          </div>
          <div style={{ fontSize: 14, color: '#666', marginTop: 4 }}>
            {notification.message}
          </div>
          <div style={{ fontSize: 12, color: '#999', marginTop: 4 }}>
            {formatDistanceToNow(new Date(notification.createdAt))} ago
          </div>
        </div>

        <PriorityBadge priority={notification.priority} />
      </div>
    </div>
  );
}
```

---

## Testing

### Test REST API Endpoints

```bash
# 1. Get all notifications
curl http://localhost:8080/api/notifications

# 2. Get only unread notifications
curl http://localhost:8080/api/notifications?unreadOnly=true

# 3. Get unread count
curl http://localhost:8080/api/notifications/unread-count

# 4. Mark notification as read
curl -X PATCH http://localhost:8080/api/notifications/1/read

# 5. Mark all as read
curl -X PATCH http://localhost:8080/api/notifications/mark-all-read
```

### Test WebSocket Connection

**Browser Console:**
```javascript
// 1. Load SockJS and STOMP libraries (add to HTML or install via npm)

// 2. Connect
const socket = new SockJS('http://localhost:8080/ws/notifications');
const stompClient = Stomp.over(socket);

stompClient.connect({}, () => {
    console.log('✅ Connected!');

    // 3. Subscribe
    stompClient.subscribe('/topic/notifications/123', (message) => {
        console.log('📩 Message received:', JSON.parse(message.body));
    });
});

// 4. Check connection status
console.log('Connection status:', socket.readyState);
// 0 = CONNECTING, 1 = OPEN, 2 = CLOSING, 3 = CLOSED
```

### Current Limitations (Backend In Progress)

**What Works Now:**
- ✅ REST API endpoints (return mock data)
- ✅ WebSocket connection and subscription
- ✅ Data structures and types

**What Doesn't Work Yet:**
- ⚠️ WebSocket messages (nothing sends notifications to WebSocket yet)
- ⚠️ Real database persistence (using mock data)
- ⚠️ Mark as read functionality (returns success but doesn't persist)

**Workaround for Testing WebSocket:**
Frontend developers can build and test the WebSocket connection logic. Actual notification messages will flow once the backend RabbitMQ consumer is implemented.

---

## Error Handling

### REST API Errors

```typescript
async function fetchNotifications() {
  try {
    const response = await fetch('/api/notifications');

    if (!response.ok) {
      if (response.status === 401) {
        // Unauthorized - redirect to login
        window.location.href = '/login';
        return;
      }
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    return await response.json();
  } catch (error) {
    console.error('Failed to fetch notifications:', error);
    // Show error toast to user
    showErrorToast('Failed to load notifications');
    return [];
  }
}
```

### WebSocket Reconnection

```typescript
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 1000; // 1 second

function connectWebSocket() {
  const socket = new SockJS('/ws/notifications');
  const stompClient = Stomp.over(socket);

  stompClient.connect({}, () => {
    console.log('WebSocket connected');
    reconnectAttempts = 0; // Reset on success

    stompClient.subscribe(`/topic/notifications/${userId}`, handleMessage);
  });

  socket.onclose = () => {
    console.log('WebSocket disconnected');

    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      reconnectAttempts++;
      console.log(`Reconnecting... (attempt ${reconnectAttempts})`);

      setTimeout(() => {
        connectWebSocket();
      }, RECONNECT_DELAY * reconnectAttempts); // Exponential backoff
    } else {
      console.error('Max reconnection attempts reached');
      showErrorToast('Lost connection to notification server');
    }
  };
}
```

---

## Support & Questions

For questions or issues:
1. Check this documentation first
2. Verify WebSocket connection status
3. Check browser console for errors
4. Contact backend team with specific error messages

---

**Last Updated:** 2025-01-15
**API Version:** 1.0 (Mock Data)
**Backend Status:** WebSocket configured, RabbitMQ consumer in development
