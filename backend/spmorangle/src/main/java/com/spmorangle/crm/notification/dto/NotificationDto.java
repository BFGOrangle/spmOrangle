package com.spmorangle.crm.notification.dto;
import java.time.Instant;
import java.util.List;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class NotificationDto {

    // Notification id
    private final Long notificationId;

    // Author id
    private final Long authorId;

    // Target id
    private final Long targetId;

    // Notification type
    private NotificationType notificationType;

    // Header of notification
    private String subject;

    // Content of notification
    private String message;

    // In-app, Email, SMS etc.
    private List<Channel> channels;

    // Read status
    private Boolean readStatus;

    // Dismissed status
    private Boolean dismissedStatus;

    // Low, Medium, High
    private Priority priority;

    // Link to contents
    private String link;

    // Time of creation of notification
    private Instant createdAt;

    // Time where notification is read
    private Instant readAt;
}
