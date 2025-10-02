package com.spmorangle.crm.notification.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.dto.UnreadCountDto;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    public final List<NotificationDto> mockNotifications = createMockData();

    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(@RequestParam(required = false, defaultValue = "false") boolean unreadOnly) {
         if (unreadOnly) {
            return ResponseEntity.ok(
                mockNotifications.stream()
                    .filter(n -> !n.getReadStatus())
                    .toList()
            );
        }
        return ResponseEntity.ok(mockNotifications);
    }


    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountDto> getUnreadCount() {
        long count = mockNotifications.stream()
            .filter(n -> !n.getReadStatus())
            .count();
        return ResponseEntity.ok(new UnreadCountDto((int) count));
    }
    
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        log.info("Marking notification {} as read", id);
        // Mock: just return success
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        log.info("Marking all notifications as read");
        return ResponseEntity.noContent().build();
    }
    
    private List<NotificationDto> createMockData() {
        List<NotificationDto> notifications = new ArrayList<>();
        
        notifications.add(NotificationDto.builder()
            .notificationId(1L)
            .authorId(100L)
            .targetId(1L)
            .notificationType(NotificationType.MENTION)
            .subject("You were mentioned")
            .message("Sarah mentioned you in a comment")
            .channels(List.of(Channel.IN_APP))
            .readStatus(false)
            .dismissedStatus(false)
            .priority(Priority.HIGH)
            .link("/tasks/123/comments")
            .createdAt(LocalDateTime.now().minusHours(1).atZone(java.time.ZoneId.systemDefault()).toInstant())
            .build());
        
        notifications.add(NotificationDto.builder()
            .notificationId(2L)
            .authorId(101L)
            .targetId(1L)
            .notificationType(NotificationType.TASK_ASSIGNED)
            .subject("New task assigned")
            .message("You have been assigned to 'Fix login bug'")
            .channels(List.of(Channel.IN_APP))
            .readStatus(true)
            .dismissedStatus(false)
            .priority(Priority.MEDIUM)
            .link("/tasks/456")
            .createdAt(LocalDateTime.now().minusDays(1).atZone(java.time.ZoneId.systemDefault()).toInstant())
            .build());
        
        return notifications;
    }
}
