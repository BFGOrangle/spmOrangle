import { useEffect, useState, useCallback, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client, Message } from '@stomp/stompjs';
import { toast } from 'sonner';
import { 
  NotificationDto, 
  NotificationFilter, 
  NotificationState,
  NotificationBulkAction 
} from '@/types/notification';
import { NotificationService } from '@/services/notification-service';
import { useCurrentUser } from '@/contexts/user-context';
import { getBearerToken } from '@/lib/auth-utils';

interface UseNotificationsResult {
  notifications: NotificationDto[];
  unreadCount: number;
  isConnected: boolean;
  isLoading: boolean;
  error: string | null;
  selectedIds: Set<number>;
  filter: NotificationFilter;
  
  // Actions
  markAsRead: (notificationId: number) => Promise<void>;
  markAllAsRead: () => Promise<void>;
  dismissNotification: (notificationId: number) => Promise<void>;
  performBulkAction: (action: NotificationBulkAction) => Promise<void>;
  toggleSelection: (notificationId: number) => void;
  selectAll: () => void;
  clearSelection: () => void;
  setFilter: (filter: NotificationFilter) => void;
  refresh: () => Promise<void>;
  reconnect: () => void;
}

const WEBSOCKET_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
const WEBSOCKET_ENDPOINT = '/ws/notifications';
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 1000; // 1 second

export function useNotifications(): UseNotificationsResult {
  const { currentUser } = useCurrentUser();
  const [state, setState] = useState<NotificationState>({
    notifications: [],
    unreadCount: 0,
    isConnected: false,
    isLoading: true,
    error: null,
    selectedIds: new Set(),
    filter: { unreadOnly: false }
  });

  const stompClientRef = useRef<Client | null>(null);
  const reconnectAttemptsRef = useRef(0);
  const reconnectTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Fetch initial notifications and unread count
  const fetchInitialData = useCallback(async () => {
    if (!currentUser?.backendStaffId) return;

    setState(prev => ({ ...prev, isLoading: true, error: null }));

    try {
      const [notifications, unreadCount] = await Promise.all([
        NotificationService.getNotifications(state.filter),
        NotificationService.getUnreadCount()
      ]);

      setState(prev => ({
        ...prev,
        notifications,
        unreadCount,
        isLoading: false,
        error: null
      }));
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
      setState(prev => ({
        ...prev,
        isLoading: false,
        error: error instanceof Error ? error.message : 'Failed to fetch notifications'
      }));
    }
  }, [currentUser?.backendStaffId, state.filter]);

  // Handle incoming WebSocket notification
  const handleWebSocketNotification = useCallback((notification: NotificationDto) => {
    console.log('📩 New notification received:', notification);

    setState(prev => {
      const existingIndex = prev.notifications.findIndex(
        n => n.notificationId === notification.notificationId
      );

      let updatedNotifications;
      
      if (existingIndex >= 0) {
        // Update existing notification
        updatedNotifications = [...prev.notifications];
        updatedNotifications[existingIndex] = notification;
      } else {
        // Add new notification at the top
        updatedNotifications = [notification, ...prev.notifications];
      }

      // Sort by newest first
      updatedNotifications.sort((a, b) => 
        new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
      );

      // Update unread count if this is a new unread notification
      const unreadCountAdjustment = 
        existingIndex < 0 && !notification.readStatus ? 1 : 0;

      return {
        ...prev,
        notifications: updatedNotifications,
        unreadCount: prev.unreadCount + unreadCountAdjustment
      };
    });

    // Show toast notification for new notifications
    if (!notification.readStatus) {
      toast(notification.subject, {
        description: notification.message,
        action: notification.link ? {
          label: 'View',
          onClick: () => window.location.href = notification.link!
        } : undefined,
      });
    }
  }, []);

  // Connect to WebSocket
  const connectWebSocket = useCallback(async () => {
    if (!currentUser?.backendStaffId || stompClientRef.current?.connected) {
      return;
    }

    console.log('🔌 Connecting to WebSocket...', {
      baseUrl: WEBSOCKET_BASE_URL,
      endpoint: WEBSOCKET_ENDPOINT,
      userId: currentUser.backendStaffId
    });

    // Get authentication token
    let authToken = '';
    try {
      authToken = await getBearerToken();
      console.log('🔑 Got auth token for WebSocket:', authToken ? 'Yes' : 'No');
    } catch (error) {
      console.error('🚨 Failed to get auth token:', error);
      setState(prev => ({ ...prev, error: 'Authentication failed' }));
      return;
    }

    try {
      // Add token as query parameter for WebSocket authentication
      const tokenParam = authToken ? `?token=${encodeURIComponent(authToken.replace('Bearer ', ''))}` : '';
      const wsUrlWithToken = `${WEBSOCKET_BASE_URL}${WEBSOCKET_ENDPOINT}${tokenParam}`;
      
      // Create SockJS connection with authentication token
      const socket = new SockJS(wsUrlWithToken);
      const client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log('STOMP Debug:', str),
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      client.onConnect = () => {
        console.log('✅ WebSocket connected');
        setState(prev => ({ ...prev, isConnected: true, error: null }));
        reconnectAttemptsRef.current = 0;

        // Subscribe to user-specific notifications
        const userId = currentUser.backendStaffId;
        const topic = `/topic/notifications/${userId}`;
        console.log('📡 Subscribing to topic:', topic);
        
        client.subscribe(topic, (message: Message) => {
          try {
            const notification: NotificationDto = JSON.parse(message.body);
            console.log('📩 Received notification:', notification);
            handleWebSocketNotification(notification);
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
          }
        });
      };

      client.onDisconnect = () => {
        console.log('❌ WebSocket disconnected');
        setState(prev => ({ ...prev, isConnected: false }));
      };

      client.onStompError = (frame) => {
        console.error('STOMP error:', frame);
        setState(prev => ({ ...prev, error: 'WebSocket connection error' }));
      };

      client.onWebSocketClose = () => {
        console.log('🔌 WebSocket closed');
        setState(prev => ({ ...prev, isConnected: false }));
        
        // Attempt to reconnect
        if (reconnectAttemptsRef.current < MAX_RECONNECT_ATTEMPTS) {
          reconnectAttemptsRef.current++;
          const delay = RECONNECT_DELAY * reconnectAttemptsRef.current;
          
          console.log(`🔄 Attempting to reconnect... (attempt ${reconnectAttemptsRef.current})`);
          
          reconnectTimeoutRef.current = setTimeout(() => {
            connectWebSocket();
          }, delay);
        } else {
          console.error('Max reconnection attempts reached');
          setState(prev => ({ 
            ...prev, 
            error: 'Lost connection to notification server' 
          }));
        }
      };

      stompClientRef.current = client;
      client.activate();
    } catch (error) {
      console.error('Failed to connect to WebSocket:', error);
      setState(prev => ({ 
        ...prev, 
        error: 'Failed to connect to notification server' 
      }));
    }
  }, [currentUser?.backendStaffId, handleWebSocketNotification]);

  // Disconnect WebSocket
  const disconnectWebSocket = useCallback(() => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }

    if (stompClientRef.current?.connected) {
      console.log('🔌 Disconnecting WebSocket...');
      stompClientRef.current.deactivate();
    }
    
    setState(prev => ({ ...prev, isConnected: false }));
  }, []);

  // Mark single notification as read
  const markAsRead = useCallback(async (notificationId: number) => {
    try {
      await NotificationService.markAsRead(notificationId);

      setState(prev => ({
        ...prev,
        notifications: prev.notifications.map(n =>
          n.notificationId === notificationId
            ? { ...n, readStatus: true, readAt: new Date().toISOString() }
            : n
        ),
        unreadCount: Math.max(0, prev.unreadCount - 1)
      }));
    } catch (error) {
      console.error('Failed to mark notification as read:', error);
      setState(prev => ({ 
        ...prev, 
        error: 'Failed to mark notification as read' 
      }));
    }
  }, []);

  // Mark all notifications as read
  const markAllAsRead = useCallback(async () => {
    try {
      await NotificationService.markAllAsRead();

      setState(prev => ({
        ...prev,
        notifications: prev.notifications.map(n => ({
          ...n,
          readStatus: true,
          readAt: new Date().toISOString()
        })),
        unreadCount: 0
      }));
    } catch (error) {
      console.error('Failed to mark all notifications as read:', error);
      setState(prev => ({ 
        ...prev, 
        error: 'Failed to mark all notifications as read' 
      }));
    }
  }, []);

  // Dismiss notification
  const dismissNotification = useCallback(async (notificationId: number) => {
    try {
      await NotificationService.dismissNotification(notificationId);

      setState(prev => {
        const notification = prev.notifications.find(n => n.notificationId === notificationId);
        const unreadCountAdjustment = notification && !notification.readStatus ? -1 : 0;

        return {
          ...prev,
          notifications: prev.notifications.filter(n => n.notificationId !== notificationId),
          unreadCount: Math.max(0, prev.unreadCount + unreadCountAdjustment),
          selectedIds: new Set([...prev.selectedIds].filter(id => id !== notificationId))
        };
      });
    } catch (error) {
      console.error('Failed to dismiss notification:', error);
      setState(prev => ({ 
        ...prev, 
        error: 'Failed to dismiss notification' 
      }));
    }
  }, []);

  // Perform bulk action
  const performBulkAction = useCallback(async (action: NotificationBulkAction) => {
    try {
      await NotificationService.performBulkAction(action);

      setState(prev => {
        let updatedNotifications = [...prev.notifications];
        let unreadCountChange = 0;

        if (action.type === 'markAsRead') {
          updatedNotifications = updatedNotifications.map(n => {
            if (action.notificationIds.includes(n.notificationId)) {
              if (!n.readStatus) unreadCountChange--;
              return { ...n, readStatus: true, readAt: new Date().toISOString() };
            }
            return n;
          });
        } else if (action.type === 'dismiss') {
          action.notificationIds.forEach(id => {
            const notification = updatedNotifications.find(n => n.notificationId === id);
            if (notification && !notification.readStatus) unreadCountChange--;
          });
          updatedNotifications = updatedNotifications.filter(
            n => !action.notificationIds.includes(n.notificationId)
          );
        }

        return {
          ...prev,
          notifications: updatedNotifications,
          unreadCount: Math.max(0, prev.unreadCount + unreadCountChange),
          selectedIds: new Set()
        };
      });
    } catch (error) {
      console.error('Failed to perform bulk action:', error);
      setState(prev => ({ 
        ...prev, 
        error: `Failed to ${action.type} notifications` 
      }));
    }
  }, []);

  // Selection management
  const toggleSelection = useCallback((notificationId: number) => {
    setState(prev => {
      const newSelectedIds = new Set(prev.selectedIds);
      if (newSelectedIds.has(notificationId)) {
        newSelectedIds.delete(notificationId);
      } else {
        newSelectedIds.add(notificationId);
      }
      return { ...prev, selectedIds: newSelectedIds };
    });
  }, []);

  const selectAll = useCallback(() => {
    setState(prev => ({
      ...prev,
      selectedIds: new Set(prev.notifications.map(n => n.notificationId))
    }));
  }, []);

  const clearSelection = useCallback(() => {
    setState(prev => ({ ...prev, selectedIds: new Set() }));
  }, []);

  // Filter management
  const setFilter = useCallback((filter: NotificationFilter) => {
    setState(prev => ({ ...prev, filter }));
  }, []);

  // Manual refresh
  const refresh = useCallback(async () => {
    await fetchInitialData();
  }, [fetchInitialData]);

  // Manual reconnect
  const reconnect = useCallback(() => {
    disconnectWebSocket();
    reconnectAttemptsRef.current = 0;
    setTimeout(connectWebSocket, 1000);
  }, [disconnectWebSocket, connectWebSocket]);

  // Initialize on mount and when user changes
  useEffect(() => {
    if (currentUser?.backendStaffId) {
      fetchInitialData();
      connectWebSocket();
    }

    // Cleanup on unmount
    return () => {
      disconnectWebSocket();
    };
  }, [currentUser?.backendStaffId, fetchInitialData, connectWebSocket, disconnectWebSocket]);

  // Re-fetch when filter changes
  useEffect(() => {
    if (currentUser?.backendStaffId) {
      fetchInitialData();
    }
  }, [state.filter, fetchInitialData]);

  return {
    notifications: state.notifications,
    unreadCount: state.unreadCount,
    isConnected: state.isConnected,
    isLoading: state.isLoading,
    error: state.error,
    selectedIds: state.selectedIds,
    filter: state.filter,
    markAsRead,
    markAllAsRead,
    dismissNotification,
    performBulkAction,
    toggleSelection,
    selectAll,
    clearSelection,
    setFilter,
    refresh,
    reconnect,
  };
}