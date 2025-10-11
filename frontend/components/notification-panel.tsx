"use client";

import React, { useState, useMemo } from 'react';
import { 
  Bell, 
  CheckCheck, 
  Trash2, 
  RefreshCw, 
  Settings,
  Search,
  X
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { 
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { ScrollArea } from '@/components/ui/scroll-area';
import { cn } from '@/lib/utils';
import { useNotifications } from '@/hooks/use-notifications';
import { NotificationItem, NotificationItemSkeleton } from './notification-item';
import { NotificationFilter, Priority, NotificationType } from '@/types/notification';

interface NotificationPanelProps {
  onClose?: () => void;
}

const NOTIFICATION_TYPE_LABELS: Record<NotificationType, string> = {
  'MENTION': 'Mentions',
  'COMMENT_REPLY': 'Comment Replies',
  'TASK_ASSIGNED': 'Task Assignments',
  'TASK_COMPLETED': 'Task Completions',
  'TASK_DEADLINE_APPROACHING': 'Task Deadlines',
  'PROJECT_INVITE': 'Project Invites',
  'PROJECT_MEMBER_JOINED': 'Project Updates',
  'PROJECT_DEADLINE_APPROACHING': 'Project Deadlines',
  'USER_REGISTERED': 'User Updates',
  'PASSWORD_RESET_REQUESTED': 'Security',
  'SYSTEM_MAINTENANCE': 'System Updates',
  'SECURITY_ALERT': 'Security Alerts',
};

export function NotificationPanel({ onClose }: NotificationPanelProps) {
  const {
    notifications,
    unreadCount,
    isLoading,
    error,
    selectedIds,
    filter,
    markAsRead,
    markAllAsRead,
    dismissNotification,
    performBulkAction,
    toggleSelection,
    clearSelection,
    setFilter,
    refresh,
    reconnect,
  } = useNotifications();

  const [searchQuery, setSearchQuery] = useState('');
  const [showBulkActions, setShowBulkActions] = useState(false);

  // Filter notifications based on search query
  const filteredNotifications = useMemo(() => {
    if (!searchQuery) return notifications;
    
    const query = searchQuery.toLowerCase();
    return notifications.filter(notification => 
      notification.subject.toLowerCase().includes(query) ||
      notification.message.toLowerCase().includes(query) ||
      notification.notificationType.toLowerCase().includes(query)
    );
  }, [notifications, searchQuery]);

  const selectedCount = selectedIds.size;
  const allSelected = filteredNotifications.length > 0 && 
    filteredNotifications.every(n => selectedIds.has(n.notificationId));

  const handleSelectAll = () => {
    if (allSelected) {
      clearSelection();
    } else {
      filteredNotifications.forEach(n => {
        if (!selectedIds.has(n.notificationId)) {
          toggleSelection(n.notificationId);
        }
      });
    }
  };

  const handleBulkMarkAsRead = async () => {
    await performBulkAction({
      type: 'markAsRead',
      notificationIds: Array.from(selectedIds)
    });
    clearSelection();
  };

  const handleBulkDismiss = async () => {
    await performBulkAction({
      type: 'dismiss',
      notificationIds: Array.from(selectedIds)
    });
    clearSelection();
  };

  const handleFilterChange = (key: keyof NotificationFilter, value: any) => {
    setFilter({ ...filter, [key]: value });
  };

  const handleNavigate = (notificationId: number, link: string | null) => {
    if (link) {
      // Close the notification panel first
      if (onClose) {
        onClose();
      }
      // Navigate to the link using window.location or router
      window.location.href = link;
    }
  };

  return (
    <div className="w-full max-w-md">
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b">
        <div className="flex items-center space-x-2">
          <Bell className="h-5 w-5" />
          <h2 className="font-semibold text-lg">Notifications</h2>
          {unreadCount > 0 && (
            <Badge variant="destructive" className="ml-2">
              {unreadCount}
            </Badge>
          )}
        </div>
        
        <div className="flex items-center space-x-1">
          <Button
            variant="ghost"
            size="icon"
            onClick={refresh}
            disabled={isLoading}
            title="Refresh"
          >
            <RefreshCw className={cn("h-4 w-4", isLoading && "animate-spin")} />
          </Button>

          <Button
            variant="ghost"
            size="icon"
            onClick={() => setShowBulkActions(!showBulkActions)}
            title="Selection mode"
          >
            <Settings className="h-4 w-4" />
          </Button>
          
          {onClose && (
            <Button variant="ghost" size="icon" onClick={onClose}>
              <X className="h-4 w-4" />
            </Button>
          )}
        </div>
      </div>

      {/* Search and Filters */}
      <div className="p-4 space-y-3 border-b">
        {/* Search */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search notifications..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>

        {/* Filters */}
        <div className="flex items-center space-x-2">
          <Button
            variant={filter.unreadOnly ? "default" : "outline"}
            size="sm"
            onClick={() => handleFilterChange('unreadOnly', !filter.unreadOnly)}
          >
            Unread only
          </Button>

          <Select
            value={filter.priority || "all"}
            onValueChange={(value) => 
              handleFilterChange('priority', value === "all" ? undefined : value as Priority)
            }
          >
            <SelectTrigger className="w-32">
              <SelectValue placeholder="Priority" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All</SelectItem>
              <SelectItem value="HIGH">High</SelectItem>
              <SelectItem value="MEDIUM">Medium</SelectItem>
              <SelectItem value="LOW">Low</SelectItem>
            </SelectContent>
          </Select>

          <Select
            value={filter.type || "all"}
            onValueChange={(value) => 
              handleFilterChange('type', value === "all" ? undefined : value as NotificationType)
            }
          >
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Type" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">All Types</SelectItem>
              {Object.entries(NOTIFICATION_TYPE_LABELS).map(([key, label]) => (
                <SelectItem key={key} value={key}>
                  {label}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Bulk Actions */}
      {(showBulkActions || selectedCount > 0) && (
        <div className="p-4 bg-muted/50 border-b">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-2">
              <input
                type="checkbox"
                checked={allSelected}
                onChange={handleSelectAll}
                className="rounded border-gray-300 text-primary focus:ring-primary"
              />
              <span className="text-sm font-medium">
                {selectedCount > 0 ? `${selectedCount} selected` : 'Select all'}
              </span>
            </div>

            {selectedCount > 0 && (
              <div className="flex items-center space-x-1">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleBulkMarkAsRead}
                  className="text-xs"
                >
                  <CheckCheck className="h-3 w-3 mr-1" />
                  Mark as Read
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={handleBulkDismiss}
                  className="text-xs text-destructive hover:text-destructive"
                >
                  <Trash2 className="h-3 w-3 mr-1" />
                  Dismiss
                </Button>
              </div>
            )}
          </div>
        </div>
      )}

      {/* Quick Actions */}
      {!showBulkActions && unreadCount > 0 && (
        <div className="p-4 border-b">
          <Button
            variant="outline"
            size="sm"
            onClick={markAllAsRead}
            className="w-full"
          >
            <CheckCheck className="h-4 w-4 mr-2" />
            Mark all as read ({unreadCount})
          </Button>
        </div>
      )}

      {/* Error State */}
      {error && (
        <div className="p-4 border-b">
          <div className="bg-destructive/10 text-destructive text-sm p-3 rounded-md">
            <div className="flex items-center justify-between">
              <span>{error}</span>
              <Button
                variant="ghost"
                size="sm"
                onClick={reconnect}
                className="text-destructive hover:text-destructive"
              >
                Retry
              </Button>
            </div>
          </div>
        </div>
      )}

      {/* Notifications List */}
      <ScrollArea className="h-96">
        <div className="p-4 space-y-2">
          {isLoading ? (
            // Loading skeleton
            Array.from({ length: 3 }).map((_, i) => (
              <NotificationItemSkeleton key={i} />
            ))
          ) : filteredNotifications.length === 0 ? (
            // Empty state
            <div className="text-center py-8">
              <Bell className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
              <p className="text-muted-foreground font-medium">
                {searchQuery ? 'No notifications match your search' : 'No notifications'}
              </p>
              <p className="text-sm text-muted-foreground mt-1">
                {searchQuery ? 'Try adjusting your search terms' : 'You\'re all caught up!'}
              </p>
            </div>
          ) : (
            // Notifications list
            filteredNotifications.map((notification) => (
              <NotificationItem
                key={notification.notificationId}
                notification={notification}
                isSelected={selectedIds.has(notification.notificationId)}
                showCheckbox={showBulkActions || selectedCount > 0}
                onSelect={toggleSelection}
                onMarkAsRead={markAsRead}
                onDismiss={dismissNotification}
                onNavigate={handleNavigate}
              />
            ))
          )}
        </div>
      </ScrollArea>

      {/* Footer */}
      <div className="p-4 border-t text-center">
        <p className="text-xs text-muted-foreground">
          {filteredNotifications.length} notification{filteredNotifications.length !== 1 ? 's' : ''}
          {searchQuery && ` matching "${searchQuery}"`}
        </p>
      </div>
    </div>
  );
}