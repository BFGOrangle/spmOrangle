"use client";

import React from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Separator } from '@/components/ui/separator';
import { 
  Bell, 
  CheckCircle, 
  Wifi, 
  WifiOff, 
  RefreshCw,
  Settings
} from 'lucide-react';
import { useNotifications } from '@/hooks/use-notifications';
import { NotificationStatus } from '@/components/notification-bell';
import { WebSocketTester } from '@/components/websocket-tester';

export default function NotificationsTestPage() {
  const {
    notifications,
    unreadCount,
    isConnected,
    isLoading,
    error,
    selectedIds,
    filter,
    markAsRead,
    markAllAsRead,
    dismissNotification,
    refresh,
    reconnect,
  } = useNotifications();

  const handleTestMarkAsRead = () => {
    if (notifications.length > 0) {
      const firstUnread = notifications.find(n => !n.readStatus);
      if (firstUnread) {
        markAsRead(firstUnread.notificationId);
      }
    }
  };

  const handleTestDismiss = () => {
    if (notifications.length > 0) {
      dismissNotification(notifications[0].notificationId);
    }
  };

  return (
    <div className="container mx-auto p-6 space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Notification System Test</h1>
          <p className="text-muted-foreground mt-2">
            Test and monitor the notification system implementation
          </p>
        </div>
        
        <div className="flex items-center space-x-2">
          <NotificationStatus />
        </div>
      </div>

      <Separator />

      {/* WebSocket Connection Tester */}
      <WebSocketTester />

      <Separator />

      {/* Connection Status */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            {isConnected ? (
              <Wifi className="h-5 w-5 text-green-500" />
            ) : (
              <WifiOff className="h-5 w-5 text-red-500" />
            )}
            <span>WebSocket Connection</span>
          </CardTitle>
          <CardDescription>
            Real-time notification connection status
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex items-center justify-between">
            <div>
              <Badge 
                variant={isConnected ? "default" : "destructive"}
                className="mb-2"
              >
                {isConnected ? "Connected" : "Disconnected"}
              </Badge>
              
              {error && (
                <p className="text-sm text-destructive">{error}</p>
              )}
            </div>
            
            <Button
              onClick={reconnect}
              variant="outline"
              size="sm"
              disabled={isConnected}
            >
              <RefreshCw className="h-4 w-4 mr-2" />
              Reconnect
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Notification Statistics */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Total Notifications</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{notifications.length}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Unread Count</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-blue-600">{unreadCount}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm font-medium">Selected Count</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold text-purple-600">{selectedIds.size}</div>
          </CardContent>
        </Card>
      </div>

      {/* Filter Status */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center space-x-2">
            <Settings className="h-5 w-5" />
            <span>Active Filters</span>
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-2">
            {filter.unreadOnly && (
              <Badge variant="outline">Unread Only</Badge>
            )}
            {filter.priority && (
              <Badge variant="outline">Priority: {filter.priority}</Badge>
            )}
            {filter.type && (
              <Badge variant="outline">Type: {filter.type}</Badge>
            )}
            {!filter.unreadOnly && !filter.priority && !filter.type && (
              <span className="text-muted-foreground text-sm">No filters active</span>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Test Actions */}
      <Card>
        <CardHeader>
          <CardTitle>Test Actions</CardTitle>
          <CardDescription>
            Test notification system functionality
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-2">
            <Button
              onClick={refresh}
              variant="outline"
              disabled={isLoading}
            >
              <RefreshCw className={`h-4 w-4 mr-2 ${isLoading ? 'animate-spin' : ''}`} />
              Refresh Notifications
            </Button>

            <Button
              onClick={markAllAsRead}
              variant="outline"
              disabled={unreadCount === 0}
            >
              <CheckCircle className="h-4 w-4 mr-2" />
              Mark All as Read ({unreadCount})
            </Button>

            <Button
              onClick={handleTestMarkAsRead}
              variant="outline"
              disabled={notifications.filter(n => !n.readStatus).length === 0}
            >
              Mark First Unread as Read
            </Button>

            <Button
              onClick={handleTestDismiss}
              variant="outline"
              disabled={notifications.length === 0}
            >
              Dismiss First Notification
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Recent Notifications Preview */}
      <Card>
        <CardHeader>
          <CardTitle>Recent Notifications Preview</CardTitle>
          <CardDescription>
            Latest 5 notifications (full list available in notification panel)
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-4">
              <RefreshCw className="h-6 w-6 animate-spin mx-auto mb-2" />
              <p className="text-sm text-muted-foreground">Loading notifications...</p>
            </div>
          ) : notifications.length === 0 ? (
            <div className="text-center py-4">
              <Bell className="h-6 w-6 mx-auto mb-2 text-muted-foreground" />
              <p className="text-sm text-muted-foreground">No notifications found</p>
            </div>
          ) : (
            <div className="space-y-2">
              {notifications.slice(0, 5).map((notification) => (
                <div
                  key={notification.notificationId}
                  className="flex items-start space-x-3 p-3 border rounded-lg"
                >
                  <div className={`w-2 h-2 rounded-full mt-2 ${
                    notification.readStatus ? 'bg-gray-300' : 'bg-blue-500'
                  }`} />
                  
                  <div className="flex-1 min-w-0">
                    <p className={`text-sm font-medium ${
                      notification.readStatus ? 'text-muted-foreground' : 'text-foreground'
                    }`}>
                      {notification.subject}
                    </p>
                    <p className="text-xs text-muted-foreground mt-1">
                      {notification.message}
                    </p>
                    <div className="flex items-center space-x-2 mt-2">
                      <Badge variant="outline" className="text-xs">
                        {notification.priority}
                      </Badge>
                      <Badge variant="secondary" className="text-xs">
                        {notification.notificationType}
                      </Badge>
                    </div>
                  </div>
                </div>
              ))}
              
              {notifications.length > 5 && (
                <p className="text-xs text-muted-foreground text-center pt-2">
                  ... and {notifications.length - 5} more notifications
                </p>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Implementation Notes */}
      <Card>
        <CardHeader>
          <CardTitle>Implementation Notes</CardTitle>
        </CardHeader>
        <CardContent className="prose prose-sm max-w-none">
          <ul className="text-sm space-y-1">
            <li>✅ <strong>Panel listing & order:</strong> Notifications sorted newest first</li>
            <li>✅ <strong>Read state persistence:</strong> State persists across sessions</li>
            <li>✅ <strong>Dismiss persistence:</strong> Dismissed notifications removed from view</li>
            <li>✅ <strong>Clickthrough to context:</strong> Navigate to linked content</li>
            <li>✅ <strong>Badge count & auto-updates:</strong> Real-time WebSocket updates</li>
            <li>✅ <strong>Bulk actions:</strong> Multi-select with bulk operations</li>
          </ul>
          
          <div className="mt-4 p-3 bg-muted rounded-lg">
            <p className="text-xs font-medium mb-1">Note:</p>
            <p className="text-xs text-muted-foreground">
              This test page demonstrates the notification system functionality. 
              Click the notification bell in the sidebar to access the full notification panel 
              with filtering, search, and bulk selection features.
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}