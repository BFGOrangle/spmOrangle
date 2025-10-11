"use client";

import React, { useState } from 'react';
import { Bell, Wifi, WifiOff } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { 
  Popover, 
  PopoverContent, 
  PopoverTrigger 
} from '@/components/ui/popover';
import { Badge } from '@/components/ui/badge';
import { cn } from '@/lib/utils';
import { useNotifications } from '@/hooks/use-notifications';
import { NotificationPanel } from './notification-panel';

interface NotificationBellProps {
  className?: string;
}

export function NotificationBell({ className }: NotificationBellProps) {
  const [isOpen, setIsOpen] = useState(false);
  const { unreadCount, isConnected, error } = useNotifications();

  const handleToggle = (open: boolean) => {
    setIsOpen(open);
  };

  return (
    <Popover open={isOpen} onOpenChange={handleToggle}>
      <PopoverTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className={cn(
            "relative",
            className
          )}
          title={`Notifications ${unreadCount > 0 ? `(${unreadCount} unread)` : ''}`}
        >
          <div className="relative">
            <Bell className="h-5 w-5" />
            
            {/* Unread count badge */}
            {unreadCount > 0 && (
              <Badge 
                variant="destructive" 
                className="absolute -top-2 -right-2 h-3 p-0 flex items-center justify-center text-xs font-bold min-w-[1.25rem]"
              >
                {unreadCount > 99 ? '99+' : unreadCount}
              </Badge>
            )}
            
            {/* Connection status indicator */}
            <div 
              className={cn(
                "absolute -bottom-1 -right-1 h-3 w-3 rounded-full border-2 border-background",
                isConnected ? "bg-green-500" : "bg-red-500"
              )}
              title={isConnected ? "Connected" : "Disconnected"}
            >
              <div className="sr-only">
                {isConnected ? "Connected to notifications" : "Disconnected from notifications"}
              </div>
            </div>
          </div>
        </Button>
      </PopoverTrigger>
      
      <PopoverContent 
        className="w-96 p-0 mx-3"
        align="end"
        sideOffset={8}
      >
        <NotificationPanel onClose={() => setIsOpen(false)} />
      </PopoverContent>
    </Popover>
  );
}

// Alternative compact version for mobile or small spaces
export function NotificationBellCompact({ className }: NotificationBellProps) {
  const { unreadCount, isConnected } = useNotifications();

  return (
    <div className={cn("relative", className)}>
      <Bell className="h-4 w-4" />
      
      {unreadCount > 0 && (
        <div className="absolute -top-1 -right-1 h-3 w-3 bg-red-500 rounded-full flex items-center justify-center">
          <span className="text-white text-xs font-bold">
            {unreadCount > 9 ? '9+' : unreadCount}
          </span>
        </div>
      )}
      
      {!isConnected && (
        <WifiOff className="absolute -bottom-1 -right-1 h-2 w-2 text-red-500" />
      )}
    </div>
  );
}

// Status indicator component for debugging
export function NotificationStatus() {
  const { isConnected, error, unreadCount } = useNotifications();

  return (
    <div className="flex items-center space-x-2 text-sm">
      <div className="flex items-center space-x-1">
        {isConnected ? (
          <Wifi className="h-4 w-4 text-green-500" />
        ) : (
          <WifiOff className="h-4 w-4 text-red-500" />
        )}
        <span className={isConnected ? "text-green-600" : "text-red-600"}>
          {isConnected ? "Connected" : "Disconnected"}
        </span>
      </div>
      
      <div className="text-muted-foreground">
        {unreadCount} unread
      </div>
      
      {error && (
        <div className="text-red-500 text-xs">
          {error}
        </div>
      )}
    </div>
  );
}