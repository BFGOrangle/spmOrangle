package com.spmorangle.crm.notification.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.dto.CreateNotificationDto;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.dto.NotificationFilterDto;
import com.spmorangle.crm.notification.dto.UnreadCountDto;
import com.spmorangle.crm.notification.enums.Priority;

public interface NotificationService {

    /**
     * Create a new notification
     */
    NotificationDto createNotification(CreateNotificationDto createDto);

    /**
     * Get all notifications for a user
     */
    List<NotificationDto> getUserNotifications(Long userId);

    /**
     * Get notifications for a user with pagination
     */
    Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable);

    /**
     * Get unread notifications for a user
     */
    List<NotificationDto> getUnreadNotifications(Long userId);

    /**
     * Get unread notifications for a user with pagination
     */
    Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable);

    /**
     * Get notifications with filters
     */
    Page<NotificationDto> getNotificationsWithFilters(Long userId, NotificationFilterDto filters, Pageable pageable);

    /**
     * Get notification by ID (with user validation)
     */
    NotificationDto getNotificationById(Long notificationId, Long userId);

    /**
     * Mark a notification as read
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * Mark multiple notifications as read
     */
    void markAsRead(List<Long> notificationIds, Long userId);

    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(Long userId);

    /**
     * Dismiss a notification
     */
    void dismissNotification(Long notificationId, Long userId);

    /**
     * Get unread count for a user
     */
    UnreadCountDto getUnreadCount(Long userId);

    /**
     * Delete a notification (admin only)
     */
    void deleteNotification(Long notificationId);

    /**
     * Cleanup old read notifications
     */
    int cleanupOldNotifications(int daysToKeep);

    /**
     * Check if a similar notification exists recently (for deduplication)
     */
    boolean hasRecentSimilarNotification(Long authorId, Long targetId, NotificationType type, int minutesBack);

    /**
     * Bulk create notifications (for system events)
     */
    List<NotificationDto> createBulkNotifications(List<CreateNotificationDto> createDtos);

    /**
     * Get notifications by type for a user
     */
    List<NotificationDto> getNotificationsByType(Long userId, NotificationType type);

    /**
     * Get notifications by priority for a user
     */
    List<NotificationDto> getNotificationsByPriority(Long userId, Priority priority);
}