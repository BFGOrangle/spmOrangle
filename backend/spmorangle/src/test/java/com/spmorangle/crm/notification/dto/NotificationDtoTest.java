package com.spmorangle.crm.notification.dto;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DisplayName("NotificationDto Test Cases")
class NotificationDtoTest {

    @Test
    @DisplayName("Should create NotificationDto with all fields")
    void testCreateNotificationDtoWithAllFields() {
        // Arrange
        Long notificationId = 1L;
        Long authorId = 100L;
        Long targetId = 200L;
        NotificationType type = NotificationType.MENTION;
        String subject = "Test Subject";
        String message = "Test Message";
        List<Channel> channels = List.of(Channel.IN_APP, Channel.EMAIL);
        Boolean readStatus = false;
        Boolean dismissedStatus = false;
        Priority priority = Priority.HIGH;
        String link = "/tasks/123";
        Instant createdAt = Instant.now();
        Instant readAt = null;

        // Act
        NotificationDto dto = NotificationDto.builder()
            .notificationId(notificationId)
            .authorId(authorId)
            .targetId(targetId)
            .notificationType(type)
            .subject(subject)
            .message(message)
            .channels(channels)
            .readStatus(readStatus)
            .dismissedStatus(dismissedStatus)
            .priority(priority)
            .link(link)
            .createdAt(createdAt)
            .readAt(readAt)
            .build();

        // Assert
        assertNotNull(dto);
        assertEquals(notificationId, dto.getNotificationId());
        assertEquals(authorId, dto.getAuthorId());
        assertEquals(targetId, dto.getTargetId());
        assertEquals(type, dto.getNotificationType());
        assertEquals(subject, dto.getSubject());
        assertEquals(message, dto.getMessage());
        assertEquals(channels, dto.getChannels());
        assertEquals(readStatus, dto.getReadStatus());
        assertEquals(dismissedStatus, dto.getDismissedStatus());
        assertEquals(priority, dto.getPriority());
        assertEquals(link, dto.getLink());
        assertEquals(createdAt, dto.getCreatedAt());
        assertNull(dto.getReadAt());
    }

    @Test
    @DisplayName("Should create NotificationDto with read status true and readAt timestamp")
    void testCreateReadNotificationDto() {
        // Arrange
        Instant createdAt = Instant.now().minusSeconds(3600);
        Instant readAt = Instant.now();

        // Act
        NotificationDto dto = NotificationDto.builder()
            .notificationId(1L)
            .authorId(100L)
            .targetId(200L)
            .notificationType(NotificationType.TASK_ASSIGNED)
            .subject("Task Assigned")
            .message("You have been assigned a task")
            .channels(List.of(Channel.IN_APP))
            .readStatus(true)
            .dismissedStatus(false)
            .priority(Priority.MEDIUM)
            .link("/tasks/456")
            .createdAt(createdAt)
            .readAt(readAt)
            .build();

        // Assert
        assertTrue(dto.getReadStatus());
        assertNotNull(dto.getReadAt());
        assertEquals(readAt, dto.getReadAt());
        assertTrue(dto.getCreatedAt().isBefore(dto.getReadAt()));
    }

    @Test
    @DisplayName("Should create NotificationDto with multiple channels")
    void testCreateNotificationDtoWithMultipleChannels() {
        // Arrange
        List<Channel> channels = List.of(Channel.IN_APP, Channel.EMAIL, Channel.SMS);

        // Act
        NotificationDto dto = NotificationDto.builder()
            .notificationId(1L)
            .targetId(200L)
            .notificationType(NotificationType.SECURITY_ALERT)
            .subject("Security Alert")
            .message("Suspicious login detected")
            .channels(channels)
            .readStatus(false)
            .dismissedStatus(false)
            .priority(Priority.HIGH)
            .createdAt(Instant.now())
            .build();

        // Assert
        assertEquals(3, dto.getChannels().size());
        assertTrue(dto.getChannels().contains(Channel.IN_APP));
        assertTrue(dto.getChannels().contains(Channel.EMAIL));
        assertTrue(dto.getChannels().contains(Channel.SMS));
    }

    @Test
    @DisplayName("Should create NotificationDto with dismissed status")
    void testCreateDismissedNotificationDto() {
        // Act
        NotificationDto dto = NotificationDto.builder()
            .notificationId(1L)
            .targetId(200L)
            .notificationType(NotificationType.COMMENT_REPLY)
            .subject("Comment Reply")
            .message("Someone replied to your comment")
            .channels(List.of(Channel.IN_APP))
            .readStatus(true)
            .dismissedStatus(true)
            .priority(Priority.LOW)
            .createdAt(Instant.now())
            .readAt(Instant.now())
            .build();

        // Assert
        assertTrue(dto.getDismissedStatus());
        assertTrue(dto.getReadStatus());
    }

    @Test
    @DisplayName("Should create NotificationDto with null author (system notification)")
    void testCreateSystemNotificationDto() {
        // Act
        NotificationDto dto = NotificationDto.builder()
            .notificationId(1L)
            .authorId(null)
            .targetId(200L)
            .notificationType(NotificationType.SYSTEM_MAINTENANCE)
            .subject("System Maintenance")
            .message("System will be down for maintenance")
            .channels(List.of(Channel.IN_APP, Channel.EMAIL))
            .readStatus(false)
            .dismissedStatus(false)
            .priority(Priority.HIGH)
            .createdAt(Instant.now())
            .build();

        // Assert
        assertNull(dto.getAuthorId());
        assertEquals(NotificationType.SYSTEM_MAINTENANCE, dto.getNotificationType());
    }

    @Test
    @DisplayName("Should handle all priority levels")
    void testAllPriorityLevels() {
        // Test LOW
        NotificationDto lowPriority = NotificationDto.builder()
            .notificationId(1L)
            .targetId(200L)
            .notificationType(NotificationType.COMMENT_REPLY)
            .subject("Low Priority")
            .message("Message")
            .channels(List.of(Channel.IN_APP))
            .readStatus(false)
            .dismissedStatus(false)
            .priority(Priority.LOW)
            .createdAt(Instant.now())
            .build();

        // Test MEDIUM
        NotificationDto mediumPriority = NotificationDto.builder()
            .notificationId(2L)
            .targetId(200L)
            .notificationType(NotificationType.TASK_ASSIGNED)
            .subject("Medium Priority")
            .message("Message")
            .channels(List.of(Channel.IN_APP))
            .readStatus(false)
            .dismissedStatus(false)
            .priority(Priority.MEDIUM)
            .createdAt(Instant.now())
            .build();

        // Test HIGH
        NotificationDto highPriority = NotificationDto.builder()
            .notificationId(3L)
            .targetId(200L)
            .notificationType(NotificationType.MENTION)
            .subject("High Priority")
            .message("Message")
            .channels(List.of(Channel.IN_APP))
            .readStatus(false)
            .dismissedStatus(false)
            .priority(Priority.HIGH)
            .createdAt(Instant.now())
            .build();

        // Assert
        assertEquals(Priority.LOW, lowPriority.getPriority());
        assertEquals(Priority.MEDIUM, mediumPriority.getPriority());
        assertEquals(Priority.HIGH, highPriority.getPriority());
    }
}
