package com.spmorangle.crm.notification.service.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.dto.CreateNotificationDto;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.dto.NotificationFilterDto;
import com.spmorangle.crm.notification.dto.UnreadCountDto;
import com.spmorangle.crm.notification.enums.Priority;
import com.spmorangle.crm.notification.model.Notification;
import com.spmorangle.crm.notification.repository.NotificationRepository;
import com.spmorangle.crm.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public NotificationDto createNotification(CreateNotificationDto createDto) {
        log.info("Creating notification for user {} from user {}", createDto.getTargetId(), createDto.getAuthorId());
        
        // Handle metadata - ensure it's null if empty, or valid JSON if provided
        String metadata = createDto.getMetadata();
        if (metadata != null && metadata.trim().isEmpty()) {
            metadata = null;
        }
        
        Notification notification = Notification.builder()
                .authorId(createDto.getAuthorId())
                .targetId(createDto.getTargetId())
                .notificationType(createDto.getNotificationType())
                .subject(createDto.getSubject())
                .message(createDto.getMessage())
                .channels(createDto.getChannels())
                .priority(createDto.getPriority())
                .link(createDto.getLink())
                .metadata(metadata)
                .createdAt(Instant.now())
                .build();

        Notification saved = notificationRepository.save(notification);
        log.info("Created notification with ID: {}", saved.getNotificationId());
        
        return mapToDto(saved);
    }

    @Override
    public List<NotificationDto> getUserNotifications(Long userId) {
        log.info("Getting all notifications for user: {}", userId);
        List<Notification> notifications = notificationRepository.findByTargetIdOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        log.info("Getting notifications for user: {} with pagination", userId);
        Page<Notification> notifications = notificationRepository.findByTargetIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToDto);
    }

    @Override
    public List<NotificationDto> getUnreadNotifications(Long userId) {
        log.info("Getting unread notifications for user: {}", userId);
        List<Notification> notifications = notificationRepository.findByTargetIdAndReadStatusFalseOrderByCreatedAtDesc(userId);
        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable) {
        log.info("Getting unread notifications for user: {} with pagination", userId);
        Page<Notification> notifications = notificationRepository.findByTargetIdAndReadStatusFalseOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToDto);
    }

    @Override
    public Page<NotificationDto> getNotificationsWithFilters(Long userId, NotificationFilterDto filters, Pageable pageable) {
        log.info("Getting filtered notifications for user: {} with filters: {}", userId, filters);
        Page<Notification> notifications = notificationRepository.findWithFilters(
                userId,
                filters.isUnreadOnly(),
                filters.isActiveOnly(),
                filters.getNotificationType(),
                filters.getPriority(),
                pageable
        );
        return notifications.map(this::mapToDto);
    }

    @Override
    public NotificationDto getNotificationById(Long notificationId, Long userId) {
        log.info("Getting notification {} for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        
        // Security check - user can only access their own notifications
        if (!notification.getTargetId().equals(userId)) {
            throw new RuntimeException("User not authorized to access this notification");
        }
        
        return mapToDto(notification);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        
        // Security check
        if (!notification.getTargetId().equals(userId)) {
            throw new RuntimeException("User not authorized to modify this notification");
        }
        
        if (!notification.getReadStatus()) {
            notification.markAsRead();
            notificationRepository.save(notification);
            log.info("Notification {} marked as read", notificationId);
        }
    }

    @Override
    @Transactional
    public void markAsRead(List<Long> notificationIds, Long userId) {
        log.info("Marking {} notifications as read for user: {}", notificationIds.size(), userId);
        Instant now = Instant.now();
        int updated = notificationRepository.markAsReadByIds(notificationIds, userId, now, now);
        log.info("Marked {} notifications as read", updated);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);
        Instant now = Instant.now();
        int updated = notificationRepository.markAllAsReadByTargetId(userId, now, now);
        log.info("Marked {} notifications as read for user: {}", updated, userId);
    }

    @Override
    @Transactional
    public void dismissNotification(Long notificationId, Long userId) {
        log.info("Dismissing notification {} for user: {}", notificationId, userId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + notificationId));
        
        // Security check
        if (!notification.getTargetId().equals(userId)) {
            throw new RuntimeException("User not authorized to modify this notification");
        }
        
        if (!notification.getDismissedStatus()) {
            notification.markAsDismissed();
            notificationRepository.save(notification);
            log.info("Notification {} dismissed", notificationId);
        }
    }

    @Override
    public UnreadCountDto getUnreadCount(Long userId) {
        log.debug("Getting unread count for user: {}", userId);
        long count = notificationRepository.countUnreadByTargetId(userId);
        return new UnreadCountDto((int) count);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        log.info("Deleting notification: {}", notificationId);
        notificationRepository.deleteById(notificationId);
    }

    @Override
    @Transactional
    public int cleanupOldNotifications(int daysToKeep) {
        log.info("Cleaning up notifications older than {} days", daysToKeep);
        Instant cutoffDate = Instant.now().minus(daysToKeep, ChronoUnit.DAYS);
        int deleted = notificationRepository.deleteOldReadNotifications(cutoffDate);
        log.info("Cleaned up {} old notifications", deleted);
        return deleted;
    }

    @Override
    public boolean hasRecentSimilarNotification(Long authorId, Long targetId, NotificationType type, int minutesBack) {
        log.debug("Checking for recent similar notifications from user {} to user {} of type {}", 
                 authorId, targetId, type);
        Instant since = Instant.now().minus(minutesBack, ChronoUnit.MINUTES);
        List<Notification> recent = notificationRepository.findRecentByAuthorAndType(authorId, targetId, type, since);
        return !recent.isEmpty();
    }

    @Override
    @Transactional
    public List<NotificationDto> createBulkNotifications(List<CreateNotificationDto> createDtos) {
        log.info("Creating {} notifications in bulk", createDtos.size());
        
        List<Notification> notifications = createDtos.stream()
                .map(dto -> {
                    // Handle metadata - ensure it's null if empty, or valid JSON if provided
                    String metadata = dto.getMetadata();
                    if (metadata != null && metadata.trim().isEmpty()) {
                        metadata = null;
                    }
                    
                    return Notification.builder()
                            .authorId(dto.getAuthorId())
                            .targetId(dto.getTargetId())
                            .notificationType(dto.getNotificationType())
                            .subject(dto.getSubject())
                            .message(dto.getMessage())
                            .channels(dto.getChannels())
                            .priority(dto.getPriority())
                            .link(dto.getLink())
                            .metadata(metadata)
                            .createdAt(Instant.now())
                            .build();
                })
                .collect(Collectors.toList());
        
        List<Notification> saved = notificationRepository.saveAll(notifications);
        log.info("Created {} notifications in bulk", saved.size());
        
        return saved.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getNotificationsByType(Long userId, NotificationType type) {
        log.info("Getting notifications of type {} for user: {}", type, userId);
        List<Notification> notifications = notificationRepository.findByTargetIdAndNotificationTypeOrderByCreatedAtDesc(userId, type);
        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotificationDto> getNotificationsByPriority(Long userId, Priority priority) {
        log.info("Getting notifications with priority {} for user: {}", priority, userId);
        List<Notification> notifications = notificationRepository.findByTargetIdAndPriorityOrderByCreatedAtDesc(userId, priority);
        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .notificationId(notification.getNotificationId())
                .authorId(notification.getAuthorId())
                .targetId(notification.getTargetId())
                .notificationType(notification.getNotificationType())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .channels(notification.getChannels())
                .readStatus(notification.getReadStatus())
                .dismissedStatus(notification.getDismissedStatus())
                .priority(notification.getPriority())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}