"use client";

import React from 'react';
import { useRouter } from 'next/navigation';
import { formatDistanceToNow } from 'date-fns';
import { 
  Clock, 
  CheckCircle, 
  X, 
  MessageSquare, 
  UserPlus, 
  Bell,
  Calendar,
  Shield,
  Settings,
  ExternalLink
} from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Card } from '@/components/ui/card';
import { cn } from '@/lib/utils';
import { NotificationDto, NotificationType, Priority } from '@/types/notification';

interface NotificationItemProps {
  notification: NotificationDto;
  isSelected?: boolean;
  showCheckbox?: boolean;
  onSelect?: (notificationId: number) => void;
  onMarkAsRead?: (notificationId: number) => void;
  onDismiss?: (notificationId: number) => void;
  onNavigate?: (notificationId: number, link: string | null) => void;
}

// Get icon for notification type
function getNotificationIcon(type: NotificationType) {
  switch (type) {
    case 'MENTION':
    case 'COMMENT_REPLY':
      return MessageSquare;
    case 'TASK_ASSIGNED':
    case 'TASK_COMPLETED':
      return CheckCircle;
    case 'TASK_DEADLINE_APPROACHING':
    case 'PROJECT_DEADLINE_APPROACHING':
      return Calendar;
    case 'PROJECT_INVITE':
    case 'PROJECT_MEMBER_JOINED':
      return UserPlus;
    case 'USER_REGISTERED':
    case 'PASSWORD_RESET_REQUESTED':
      return Settings;
    case 'SYSTEM_MAINTENANCE':
      return Settings;
    case 'SECURITY_ALERT':
      return Shield;
    default:
      return Bell;
  }
}

// Get priority badge variant
function getPriorityVariant(priority: Priority): 'default' | 'secondary' | 'destructive' | 'outline' {
  switch (priority) {
    case 'HIGH':
      return 'destructive';
    case 'MEDIUM':
      return 'default';
    case 'LOW':
      return 'secondary';
    default:
      return 'outline';
  }
}

// Get priority color for the unread indicator
function getPriorityColor(priority: Priority): string {
  switch (priority) {
    case 'HIGH':
      return 'bg-red-500';
    case 'MEDIUM':
      return 'bg-blue-500';
    case 'LOW':
      return 'bg-gray-400';
    default:
      return 'bg-blue-500';
  }
}

// Get border color based on priority
function getPriorityBorderColor(priority: Priority): string {
  switch (priority) {
    case 'HIGH':
      return 'border-l-red-500';
    case 'MEDIUM':
      return 'border-l-blue-500';
    case 'LOW':
      return 'border-l-gray-300';
    default:
      return 'border-l-primary';
  }
}

export function NotificationItem({
  notification,
  isSelected = false,
  showCheckbox = false,
  onSelect,
  onMarkAsRead,
  onDismiss,
  onNavigate,
}: NotificationItemProps) {
  const router = useRouter();
  const IconComponent = getNotificationIcon(notification.notificationType);

  const handleClick = () => {
    // Mark as read if not already read
    if (!notification.readStatus && onMarkAsRead) {
      onMarkAsRead(notification.notificationId);
    }

    // Navigate to the linked content
    if (notification.link) {
      if (onNavigate) {
        onNavigate(notification.notificationId, notification.link);
      } else {
        router.push(notification.link);
      }
    }
  };

  const handleCheckboxChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.stopPropagation();
    if (onSelect) {
      onSelect(notification.notificationId);
    }
  };

  const handleDismiss = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onDismiss) {
      onDismiss(notification.notificationId);
    }
  };

  const handleMarkAsRead = (e: React.MouseEvent) => {
    e.stopPropagation();
    if (onMarkAsRead) {
      onMarkAsRead(notification.notificationId);
    }
  };

  const handleNavigate = (e: React.MouseEvent) => {
    e.stopPropagation();
    
    // Mark as read if not already read
    if (!notification.readStatus && onMarkAsRead) {
      onMarkAsRead(notification.notificationId);
    }

    // Navigate to the linked content
    if (notification.link) {
      if (onNavigate) {
        onNavigate(notification.notificationId, notification.link);
      } else {
        router.push(notification.link);
      }
    }
  };

  return (
    <Card 
      role="article"
      className={cn(
        "p-3 cursor-pointer transition-colors hover:bg-muted/50",
        !notification.readStatus && `bg-muted/30 border-l-4 ${getPriorityBorderColor(notification.priority)}`,
        isSelected && "bg-primary/10 border-primary",
        notification.link && "cursor-pointer"
      )}
      onClick={handleClick}
    >
      <div className="flex items-start space-x-3">
        {/* Checkbox for bulk selection */}
        {showCheckbox && (
          <div className="pt-1">
            <input
              type="checkbox"
              checked={isSelected}
              onChange={handleCheckboxChange}
              className="rounded border-gray-300 text-primary focus:ring-primary"
              aria-label={`Select notification: ${notification.subject}`}
            />
          </div>
        )}

        {/* Unread indicator */}
        {!notification.readStatus && (
          <div 
            className={cn(
              "w-2 h-2 rounded-full mt-2 flex-shrink-0",
              getPriorityColor(notification.priority)
            )}
          />
        )}

        {/* Notification icon */}
        <div className="flex-shrink-0 pt-1">
          <IconComponent className="h-4 w-4 text-muted-foreground" />
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between">
            <div className="flex-1 min-w-0">
              {/* Subject/Title */}
              <p className={cn(
                "text-sm font-medium",
                notification.readStatus ? "text-muted-foreground" : "text-foreground"
              )}>
                {notification.subject}
              </p>

              {/* Message */}
              <p className="text-sm text-muted-foreground mt-1 line-clamp-2">
                {notification.message}
              </p>

              {/* Metadata */}
              <div className="flex items-center space-x-2 mt-2">
                {/* Timestamp */}
                <div className="flex items-center space-x-1 text-xs text-muted-foreground">
                  <Clock className="h-3 w-3" />
                  <span>
                    {formatDistanceToNow(new Date(notification.createdAt), { addSuffix: true })}
                  </span>
                </div>

                {/* Priority badge */}
                <Badge 
                  variant={getPriorityVariant(notification.priority)}
                  className="text-xs"
                >
                  {notification.priority}
                </Badge>

                {/* Notification type */}
                <Badge variant="outline" className="text-xs whitespace-normal break-words">
                  {notification.notificationType.replace(/_/g, ' ')}
                </Badge>
              </div>
            </div>

            {/* Action buttons */}
            <div className="flex items-center space-x-1 ml-2">
              {/* Navigate to link button */}
              {notification.link && (
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-6 w-6 text-muted-foreground hover:text-primary"
                  onClick={handleNavigate}
                  title="Go to link"
                  aria-label="Navigate to related content"
                >
                  <ExternalLink className="h-3 w-3" />
                </Button>
              )}

              {/* Mark as read button */}
              {!notification.readStatus && (
                <Button
                  variant="ghost"
                  size="icon"
                  className="h-6 w-6"
                  onClick={handleMarkAsRead}
                  title="Mark as read"
                  aria-label="Mark as read"
                >
                  <CheckCircle className="h-3 w-3" />
                </Button>
              )}

              {/* Dismiss button */}
              <Button
                variant="ghost"
                size="icon"
                className="h-6 w-6 text-muted-foreground hover:text-destructive"
                onClick={handleDismiss}
                title="Dismiss notification"
              >
                <X className="h-3 w-3" />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </Card>
  );
}

// Skeleton component for loading state
export function NotificationItemSkeleton() {
  return (
    <Card className="p-3">
      <div className="flex items-start space-x-3">
        <div className="w-2 h-2 bg-gray-200 rounded-full mt-2 animate-pulse" />
        <div className="w-4 h-4 bg-gray-200 rounded mt-1 animate-pulse" />
        <div className="flex-1 space-y-2">
          <div className="h-4 bg-gray-200 rounded animate-pulse" />
          <div className="h-3 bg-gray-200 rounded w-3/4 animate-pulse" />
          <div className="flex space-x-2">
            <div className="h-5 w-16 bg-gray-200 rounded animate-pulse" />
            <div className="h-5 w-12 bg-gray-200 rounded animate-pulse" />
          </div>
        </div>
      </div>
    </Card>
  );
}