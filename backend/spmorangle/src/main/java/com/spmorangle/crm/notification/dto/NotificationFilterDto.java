package com.spmorangle.crm.notification.dto;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.enums.Priority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationFilterDto {

    @Builder.Default
    private boolean unreadOnly = false;

    @Builder.Default
    private boolean activeOnly = true; // Non-dismissed by default

    private NotificationType notificationType;

    private Priority priority;

    private Long authorId;

    private String searchQuery; // For future text search implementation

    // Helper methods
    public static NotificationFilterDto unreadOnly() {
        return NotificationFilterDto.builder()
                .unreadOnly(true)
                .activeOnly(true)
                .build();
    }

    public static NotificationFilterDto activeOnly() {
        return NotificationFilterDto.builder()
                .unreadOnly(false)
                .activeOnly(true)
                .build();
    }

    public static NotificationFilterDto byType(NotificationType type) {
        return NotificationFilterDto.builder()
                .notificationType(type)
                .activeOnly(true)
                .build();
    }

    public static NotificationFilterDto byPriority(Priority priority) {
        return NotificationFilterDto.builder()
                .priority(priority)
                .activeOnly(true)
                .build();
    }

    public static NotificationFilterDto all() {
        return NotificationFilterDto.builder()
                .unreadOnly(false)
                .activeOnly(false)
                .build();
    }
}