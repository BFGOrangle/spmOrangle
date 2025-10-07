package com.spmorangle.crm.notification.dto;

import java.util.List;

import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationDto {

    @NotNull(message = "Author ID is required")
    private Long authorId;

    @NotNull(message = "Target ID is required")
    private Long targetId;

    @NotNull(message = "Notification type is required")
    private NotificationType notificationType;

    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject cannot exceed 255 characters")
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message cannot exceed 2000 characters")
    private String message;

    @Builder.Default
    private List<Channel> channels = List.of(Channel.IN_APP);

    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    private String link;

    private String metadata;

    // Helper methods
    public static CreateNotificationDto forMention(Long authorId, Long targetId, String subject, String message, String link) {
        return CreateNotificationDto.builder()
                .authorId(authorId)
                .targetId(targetId)
                .notificationType(NotificationType.MENTION)
                .subject(subject)
                .message(message)
                .link(link)
                .priority(Priority.HIGH)
                .channels(List.of(Channel.IN_APP))
                .build();
    }

    public static CreateNotificationDto forTaskAssignment(Long authorId, Long targetId, String subject, String message, String link) {
        return CreateNotificationDto.builder()
                .authorId(authorId)
                .targetId(targetId)
                .notificationType(NotificationType.TASK_ASSIGNED)
                .subject(subject)
                .message(message)
                .link(link)
                .priority(Priority.MEDIUM)
                .channels(List.of(Channel.IN_APP))
                .build();
    }

    public static CreateNotificationDto forCommentReply(Long authorId, Long targetId, String subject, String message, String link) {
        return CreateNotificationDto.builder()
                .authorId(authorId)
                .targetId(targetId)
                .notificationType(NotificationType.COMMENT_REPLY)
                .subject(subject)
                .message(message)
                .link(link)
                .priority(Priority.MEDIUM)
                .channels(List.of(Channel.IN_APP))
                .build();
    }
}