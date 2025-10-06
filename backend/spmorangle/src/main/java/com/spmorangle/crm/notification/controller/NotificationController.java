package com.spmorangle.crm.notification.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.dto.NotificationFilterDto;
import com.spmorangle.crm.notification.dto.UnreadCountDto;
import com.spmorangle.crm.notification.enums.Priority;
import com.spmorangle.crm.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserContextService userContextService;

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(
            @RequestParam(required = false, defaultValue = "false") boolean unreadOnly,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        
        User user = userContextService.getRequestingUser();
        log.info("Getting notifications for user: {} (unreadOnly: {})", user.getId(), unreadOnly);
        
        // Build filter
        NotificationFilterDto.NotificationFilterDtoBuilder filterBuilder = NotificationFilterDto.builder()
                .unreadOnly(unreadOnly)
                .activeOnly(true); // Always show only active (non-dismissed) notifications
        
        if (type != null) {
            filterBuilder.notificationType(type);
        }
        if (priority != null) {
            filterBuilder.priority(priority);
        }
        
        NotificationFilterDto filters = filterBuilder.build();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<NotificationDto> notificationPage = notificationService.getNotificationsWithFilters(
                user.getId(), filters, pageable);
        
        return ResponseEntity.ok(notificationPage.getContent());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountDto> getUnreadCount() {
        User user = userContextService.getRequestingUser();
        log.debug("Getting unread count for user: {}", user.getId());
        
        UnreadCountDto count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(count);
    }
    
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        User user = userContextService.getRequestingUser();
        log.info("Marking notification {} as read for user: {}", id, user.getId());
        
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        User user = userContextService.getRequestingUser();
        log.info("Marking all notifications as read for user: {}", user.getId());
        
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationDto> getNotificationById(@PathVariable Long id) {
        User user = userContextService.getRequestingUser();
        log.info("Getting notification {} for user: {}", id, user.getId());
        
        NotificationDto notification = notificationService.getNotificationById(id, user.getId());
        return ResponseEntity.ok(notification);
    }

    @PatchMapping("/{id}/dismiss")
    public ResponseEntity<Void> dismissNotification(@PathVariable Long id) {
        User user = userContextService.getRequestingUser();
        log.info("Dismissing notification {} for user: {}", id, user.getId());
        
        notificationService.dismissNotification(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mentions")
    public ResponseEntity<List<NotificationDto>> getMentions() {
        User user = userContextService.getRequestingUser();
        log.info("Getting mention notifications for user: {}", user.getId());
        
        List<NotificationDto> mentions = notificationService.getNotificationsByType(
                user.getId(), NotificationType.MENTION);
        return ResponseEntity.ok(mentions);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByPriority(@PathVariable Priority priority) {
        User user = userContextService.getRequestingUser();
        log.info("Getting {} priority notifications for user: {}", priority, user.getId());
        
        List<NotificationDto> notifications = notificationService.getNotificationsByPriority(
                user.getId(), priority);
        return ResponseEntity.ok(notifications);
    }
}
