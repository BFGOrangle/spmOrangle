package com.spmorangle.crm.notification.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.enums.Priority;
import com.spmorangle.crm.notification.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific target user
     */
    List<Notification> findByTargetIdOrderByCreatedAtDesc(Long targetId);

    /**
     * Find all notifications for a target user with pagination
     */
    Page<Notification> findByTargetIdOrderByCreatedAtDesc(Long targetId, Pageable pageable);

    /**
     * Find unread notifications for a target user
     */
    List<Notification> findByTargetIdAndReadStatusFalseOrderByCreatedAtDesc(Long targetId);

    /**
     * Find unread notifications for a target user with pagination
     */
    Page<Notification> findByTargetIdAndReadStatusFalseOrderByCreatedAtDesc(Long targetId, Pageable pageable);

    /**
     * Find active (non-dismissed) notifications for a target user
     */
    List<Notification> findByTargetIdAndDismissedStatusFalseOrderByCreatedAtDesc(Long targetId);

    /**
     * Find active (non-dismissed) notifications for a target user with pagination
     */
    Page<Notification> findByTargetIdAndDismissedStatusFalseOrderByCreatedAtDesc(Long targetId, Pageable pageable);

    /**
     * Find notifications by type for a target user
     */
    List<Notification> findByTargetIdAndNotificationTypeOrderByCreatedAtDesc(Long targetId, NotificationType notificationType);

    /**
     * Find notifications by priority for a target user
     */
    List<Notification> findByTargetIdAndPriorityOrderByCreatedAtDesc(Long targetId, Priority priority);

    /**
     * Count unread notifications for a target user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.targetId = :targetId AND n.readStatus = false")
    long countUnreadByTargetId(@Param("targetId") Long targetId);

    /**
     * Count active notifications for a target user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.targetId = :targetId AND n.dismissedStatus = false")
    long countActiveByTargetId(@Param("targetId") Long targetId);

    /**
     * Mark all notifications as read for a target user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true, n.readAt = :readAt, n.updatedAt = :updatedAt " +
           "WHERE n.targetId = :targetId AND n.readStatus = false")
    int markAllAsReadByTargetId(@Param("targetId") Long targetId, 
                               @Param("readAt") Instant readAt, 
                               @Param("updatedAt") Instant updatedAt);

    /**
     * Mark specific notifications as read
     */
    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true, n.readAt = :readAt, n.updatedAt = :updatedAt " +
           "WHERE n.notificationId IN :notificationIds AND n.targetId = :targetId")
    int markAsReadByIds(@Param("notificationIds") List<Long> notificationIds, 
                       @Param("targetId") Long targetId,
                       @Param("readAt") Instant readAt, 
                       @Param("updatedAt") Instant updatedAt);

    /**
     * Delete old read notifications (cleanup)
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.readStatus = true AND n.createdAt < :cutoffDate")
    int deleteOldReadNotifications(@Param("cutoffDate") Instant cutoffDate);

    /**
     * Find recent notifications by author and type (for deduplication)
     */
    @Query("SELECT n FROM Notification n WHERE n.authorId = :authorId AND n.targetId = :targetId " +
           "AND n.notificationType = :notificationType AND n.createdAt >= :since " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findRecentByAuthorAndType(@Param("authorId") Long authorId,
                                                @Param("targetId") Long targetId,
                                                @Param("notificationType") NotificationType notificationType,
                                                @Param("since") Instant since);

    /**
     * Find notifications with filters
     */
    @Query("SELECT n FROM Notification n WHERE n.targetId = :targetId " +
           "AND (:unreadOnly = false OR n.readStatus = false) " +
           "AND (:activeOnly = false OR n.dismissedStatus = false) " +
           "AND (:notificationType IS NULL OR n.notificationType = :notificationType) " +
           "AND (:priority IS NULL OR n.priority = :priority) " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findWithFilters(@Param("targetId") Long targetId,
                                      @Param("unreadOnly") boolean unreadOnly,
                                      @Param("activeOnly") boolean activeOnly,
                                      @Param("notificationType") NotificationType notificationType,
                                      @Param("priority") Priority priority,
                                      Pageable pageable);
}