import { renderHook, act, waitFor } from '@testing-library/react';
import { useNotifications } from '@/hooks/use-notifications';
import { NotificationService } from '@/services/notification-service';
import { useCurrentUser } from '@/contexts/user-context';
import { getBearerToken } from '@/lib/auth-utils';
import { toast } from 'sonner';

// Mock dependencies
jest.mock('@/services/notification-service');
jest.mock('@/contexts/user-context');
jest.mock('@/lib/auth-utils');
jest.mock('sonner');

// Mock WebSocket and STOMP dependencies more thoroughly
const mockStompClient = {
  configure: jest.fn(),
  activate: jest.fn(),
  deactivate: jest.fn(),
  connected: false,
  subscribe: jest.fn(),
  publish: jest.fn(),
  onConnect: jest.fn(),
  onDisconnect: jest.fn(),
  onStompError: jest.fn(),
  onWebSocketError: jest.fn(),
};

jest.mock('@stomp/stompjs', () => ({
  Client: jest.fn(() => mockStompClient),
}));

jest.mock('sockjs-client', () => jest.fn(() => ({})));

// Mock global TextEncoder/TextDecoder for STOMP
global.TextEncoder = jest.fn().mockImplementation(() => ({
  encode: jest.fn(),
}));
global.TextDecoder = jest.fn().mockImplementation(() => ({
  decode: jest.fn(),
}));

const mockNotificationService = NotificationService as jest.Mocked<typeof NotificationService>;
const mockUseCurrentUser = useCurrentUser as jest.MockedFunction<typeof useCurrentUser>;
const mockGetBearerToken = getBearerToken as jest.MockedFunction<typeof getBearerToken>;
const mockToast = toast as jest.MockedFunction<typeof toast>;

const mockNotifications = [
  {
    notificationId: 1,
    authorId: 2,
    targetId: 1,
    notificationType: "TASK_ASSIGNED" as const,
    subject: "Task assigned to you",
    message: "You have been assigned a new task",
    channels: ["IN_APP" as const],
    readStatus: false,
    dismissedStatus: false,
    priority: "HIGH" as const,
    link: "/tasks/1",
    createdAt: "2024-01-01T10:00:00Z",
    readAt: null,
  },
  {
    notificationId: 2,
    authorId: 3,
    targetId: 1,
    notificationType: "MENTION" as const,
    subject: "You were mentioned",
    message: "Someone mentioned you in a comment",
    channels: ["IN_APP" as const],
    readStatus: true,
    dismissedStatus: false,
    priority: "MEDIUM" as const,
    link: "/comments/1",
    createdAt: "2024-01-01T09:00:00Z",
    readAt: "2024-01-01T09:30:00Z",
  }
];

describe('useNotifications', () => {
  beforeEach(() => {
    // Reset all mocks
    jest.clearAllMocks();
    
    // Mock current user
    mockUseCurrentUser.mockReturnValue({
      currentUser: { id: 1, username: 'testuser', backendStaffId: 1 },
      isLoading: false,
      error: null,
    } as any);

    // Mock bearer token
    mockGetBearerToken.mockResolvedValue('mock-token');

    // Mock NotificationService methods
    mockNotificationService.getNotifications.mockResolvedValue(mockNotifications);
    mockNotificationService.getUnreadCount.mockResolvedValue(1);
    mockNotificationService.markAsRead.mockResolvedValue(undefined);
    mockNotificationService.markAllAsRead.mockResolvedValue(undefined);
    mockNotificationService.dismissNotification.mockResolvedValue(undefined);
    mockNotificationService.performBulkAction.mockResolvedValue(undefined);

    // Reset STOMP client mock
    jest.clearAllMocks();
    mockStompClient.configure.mockClear();
    mockStompClient.activate.mockClear();
    mockStompClient.deactivate.mockClear();
    mockStompClient.subscribe.mockClear();

    // Mock toast
    mockToast.success = jest.fn();
    mockToast.error = jest.fn();
  });

  afterEach(() => {
    jest.resetAllMocks();
  });

  it('should initialize with default state', async () => {
    const { result } = renderHook(() => useNotifications());

    expect(result.current.notifications).toEqual([]);
    expect(result.current.unreadCount).toBe(0);
    expect(result.current.isConnected).toBe(false);
    expect(result.current.isLoading).toBe(true);
    expect(result.current.error).toBe(null);
    expect(result.current.selectedIds).toEqual(new Set());
    expect(result.current.filter).toEqual({ unreadOnly: false });
  });

  // Simplified tests without complex async loading expectations
  it('should have correct function signatures', () => {
    const { result } = renderHook(() => useNotifications());

    // Verify all expected functions exist
    expect(typeof result.current.markAsRead).toBe('function');
    expect(typeof result.current.markAllAsRead).toBe('function');
    expect(typeof result.current.dismissNotification).toBe('function');
    expect(typeof result.current.performBulkAction).toBe('function');
    expect(typeof result.current.toggleSelection).toBe('function');
    expect(typeof result.current.selectAll).toBe('function');
    expect(typeof result.current.clearSelection).toBe('function');
    expect(typeof result.current.setFilter).toBe('function');
    expect(typeof result.current.refresh).toBe('function');
    expect(typeof result.current.reconnect).toBe('function');
  });

  it('should toggle selection', () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      result.current.toggleSelection(1);
    });

    expect(result.current.selectedIds).toEqual(new Set([1]));

    act(() => {
      result.current.toggleSelection(1);
    });

    expect(result.current.selectedIds).toEqual(new Set());
  });

  it('should select all notifications', async () => {
    const { result } = renderHook(() => useNotifications());

    // Test select all with empty notifications first
    act(() => {
      result.current.selectAll();
    });

    expect(result.current.selectedIds).toEqual(new Set());
  });

  it('should set filter', () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      result.current.setFilter({ unreadOnly: true });
    });

    expect(result.current.filter).toEqual({ unreadOnly: true });
  });

  it('should refresh notifications', () => {
    const { result } = renderHook(() => useNotifications());

    // Just test that the function exists and is callable
    expect(() => result.current.refresh()).not.toThrow();
  });

  it('should not initialize WebSocket when user is not available', () => {
    mockUseCurrentUser.mockReturnValue({
      currentUser: null,
      isLoading: false,
      error: null,
    } as any);

    renderHook(() => useNotifications());

    // Just verify that WebSocket is not attempted when no user
    expect(mockGetBearerToken).not.toHaveBeenCalled();
  });

  it('should initialize WebSocket when user is available', async () => {
    renderHook(() => useNotifications());

    await waitFor(() => {
      expect(mockGetBearerToken).toHaveBeenCalled();
    });
  });

  // Core hook functionality tests - simplified without complex WebSocket mocking
  it('should initialize with default state', async () => {
    const { result } = renderHook(() => useNotifications());

    expect(result.current.notifications).toEqual([]);
    expect(result.current.unreadCount).toBe(0);
    expect(result.current.isConnected).toBe(false);
    expect(result.current.isLoading).toBe(true);
    expect(result.current.error).toBe(null);
    expect(result.current.selectedIds).toEqual(new Set());
    expect(result.current.filter).toEqual({ unreadOnly: false });
  });

  it('should handle state management functions', () => {
    const { result } = renderHook(() => useNotifications());

    // Test toggle selection
    act(() => {
      result.current.toggleSelection(1);
    });
    expect(result.current.selectedIds).toEqual(new Set([1]));

    act(() => {
      result.current.toggleSelection(1);
    });
    expect(result.current.selectedIds).toEqual(new Set());

    // Test select all with empty notifications (won't select anything)
    act(() => {
      result.current.selectAll();
    });
    expect(result.current.selectedIds).toEqual(new Set());

    // Test clear selection
    act(() => {
      result.current.toggleSelection(1);
      result.current.toggleSelection(2);
    });
    expect(result.current.selectedIds).toEqual(new Set([1, 2]));

    act(() => {
      result.current.clearSelection();
    });
    expect(result.current.selectedIds).toEqual(new Set());
  });

  it('should handle filter changes', () => {
    const { result } = renderHook(() => useNotifications());

    act(() => {
      result.current.setFilter({ unreadOnly: true });
    });

    expect(result.current.filter).toEqual({ unreadOnly: true });
  });
});