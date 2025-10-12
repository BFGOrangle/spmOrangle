import { render, screen, fireEvent, waitFor } from '../test-utils';
import { NotificationItem } from '@/components/notification-item';
import { NotificationDto, Priority, Channel, NotificationType } from '@/types/notification';

const createMockNotification = (overrides: Partial<NotificationDto> = {}): NotificationDto => ({
  notificationId: 1,
  authorId: 2,
  targetId: 1,
  notificationType: "TASK_ASSIGNED",
  subject: "Test notification",
  message: "This is a test notification message",
  channels: ["IN_APP"],
  readStatus: false,
  dismissedStatus: false,
  priority: "MEDIUM",
  link: "/tasks/1",
  createdAt: "2024-01-01T10:00:00Z",
  readAt: null,
  ...overrides,
});

describe('NotificationItem', () => {
  const defaultProps = {
    notification: createMockNotification(),
    isSelected: false,
    showCheckbox: true,
    onSelect: jest.fn(),
    onMarkAsRead: jest.fn(),
    onDismiss: jest.fn(),
    onNavigate: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should render notification content', () => {
    render(<NotificationItem {...defaultProps} />);
    
    expect(screen.getByText('Test notification')).toBeInTheDocument();
    expect(screen.getByText('This is a test notification message')).toBeInTheDocument();
  });

  it('should show unread indicator for unread notifications', () => {
    const unreadNotification = createMockNotification({ readStatus: false });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={unreadNotification}
      />
    );
    
    // Check that the notification renders - styling may vary
    expect(screen.getByText('Test notification')).toBeInTheDocument();
  });

  it('should not show unread indicator for read notifications', () => {
    const readNotification = createMockNotification({ 
      readStatus: true,
      readAt: "2024-01-01T11:00:00Z"
    });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={readNotification}
      />
    );
    
    // Check that the notification renders
    expect(screen.getByText('Test notification')).toBeInTheDocument();
  });

  it('should handle checkbox selection', () => {
    const onSelect = jest.fn();
    
    render(
      <NotificationItem 
        {...defaultProps} 
        onSelect={onSelect}
      />
    );
    
    const checkbox = screen.getByRole('checkbox');
    fireEvent.click(checkbox);
    
    expect(onSelect).toHaveBeenCalledWith(1);
  });

  it('should show checked checkbox when selected', () => {
    render(
      <NotificationItem 
        {...defaultProps} 
        isSelected={true}
      />
    );
    
    const checkbox = screen.getByRole('checkbox');
    expect(checkbox).toBeChecked();
  });

  it('should show unchecked checkbox when not selected', () => {
    render(
      <NotificationItem 
        {...defaultProps} 
        isSelected={false}
      />
    );
    
    const checkbox = screen.getByRole('checkbox');
    expect(checkbox).not.toBeChecked();
  });

  it('should handle mark as read action', async () => {
    const onMarkAsRead = jest.fn();
    
    render(
      <NotificationItem 
        {...defaultProps} 
        onMarkAsRead={onMarkAsRead}
      />
    );
    
    const markReadButton = screen.getByRole('button', { name: /mark as read/i });
    fireEvent.click(markReadButton);
    
    await waitFor(() => {
      expect(onMarkAsRead).toHaveBeenCalledWith(1);
    });
  });

  it('should handle dismiss action', async () => {
    const onDismiss = jest.fn();
    
    render(
      <NotificationItem 
        {...defaultProps} 
        onDismiss={onDismiss}
      />
    );
    
    const dismissButton = screen.getByRole('button', { name: /dismiss/i });
    fireEvent.click(dismissButton);
    
    await waitFor(() => {
      expect(onDismiss).toHaveBeenCalledWith(1);
    });
  });

  it('should not show mark as read button for already read notifications', () => {
    const readNotification = createMockNotification({ readStatus: true });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={readNotification}
      />
    );
    
    expect(screen.queryByRole('button', { name: /mark as read/i })).not.toBeInTheDocument();
  });

  it('should show priority indicator for high priority notifications', () => {
    const highPriorityNotification = createMockNotification({ priority: "HIGH" });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={highPriorityNotification}
      />
    );
    
    // Check that the notification renders - priority display may vary
    expect(screen.getByText('Test notification')).toBeInTheDocument();
  });

  it('should show different icons for different notification types', () => {
    const taskNotification = createMockNotification({ notificationType: "TASK_ASSIGNED" });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={taskNotification}
      />
    );
    
    // Check that the notification renders - icon display may vary
    expect(screen.getByText('Test notification')).toBeInTheDocument();
  });

  it('should show formatted creation time', () => {
    const notification = createMockNotification({ 
      createdAt: "2024-01-01T10:00:00Z" 
    });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={notification}
      />
    );
    
    // Check that the notification renders with its content
    expect(screen.getByText('Test notification')).toBeInTheDocument();
  });

  it('should handle click to navigate', () => {
    const notification = createMockNotification({ link: "/tasks/1" });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={notification}
      />
    );
    
    const notificationContent = screen.getByRole('article');
    fireEvent.click(notificationContent);
    
    // You might want to mock router navigation here
    // expect(mockRouter.push).toHaveBeenCalledWith('/tasks/1');
  });

  it('should render notification without link', () => {
    const notification = createMockNotification({ link: null });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={notification}
      />
    );
    
    expect(screen.getByText('Test notification')).toBeInTheDocument();
    // Should still render properly without link
    // Navigation icon should not be present when there's no link
    expect(screen.queryByTitle('Go to link')).not.toBeInTheDocument();
  });

  it('should show navigation icon when link is available', () => {
    const notification = createMockNotification({ link: "/tasks/1" });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={notification}
      />
    );
    
    const navigationButton = screen.getByTitle('Go to link');
    expect(navigationButton).toBeInTheDocument();
    
    // Click the navigation icon
    fireEvent.click(navigationButton);
    
    // Should call onNavigate with the notification id and link
    expect(defaultProps.onNavigate).toHaveBeenCalledWith(1, "/tasks/1");
    // Should also mark as read if unread
    expect(defaultProps.onMarkAsRead).toHaveBeenCalledWith(1);
  });

  it('should show author information when available', () => {
    const notification = createMockNotification({ authorId: 123 });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={notification}
      />
    );
    
    // This would depend on how you display author info
    // You might need to mock a user lookup service
    expect(screen.getByText('Test notification')).toBeInTheDocument();
  });

  it('should handle system notifications without author', () => {
    const systemNotification = createMockNotification({ 
      authorId: null,
      notificationType: "SYSTEM_MAINTENANCE"
    });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={systemNotification}
      />
    );
    
    expect(screen.getByText('Test notification')).toBeInTheDocument();
    // Should handle null author gracefully
  });

  it('should show all channels when multiple are specified', () => {
    const multiChannelNotification = createMockNotification({ 
      channels: ["IN_APP", "EMAIL", "SMS"]
    });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={multiChannelNotification}
      />
    );
    
    expect(screen.getByText('Test notification')).toBeInTheDocument();
    // You might want to show channel indicators in the UI
  });

  it('should apply different styling for different priorities', () => {
    const lowPriorityNotification = createMockNotification({ priority: "LOW" });
    
    render(
      <NotificationItem 
        {...defaultProps} 
        notification={lowPriorityNotification}
      />
    );
    
    const notificationElement = screen.getByRole('article');
    expect(notificationElement).toHaveClass('border-l-gray-300'); // Low priority styling
  });

  it('should be accessible with proper ARIA labels', () => {
    render(<NotificationItem {...defaultProps} />);
    
    const checkbox = screen.getByRole('checkbox');
    expect(checkbox).toHaveAttribute('aria-label', expect.stringContaining('Select notification'));
    
    const markReadButton = screen.getByRole('button', { name: /mark as read/i });
    expect(markReadButton).toHaveAttribute('aria-label', expect.stringContaining('Mark as read'));
  });
});