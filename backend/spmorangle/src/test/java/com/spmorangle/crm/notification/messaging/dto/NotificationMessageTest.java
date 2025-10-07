package com.spmorangle.crm.notification.messaging.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notification Message Tests")
class NotificationMessageTest {

    @Test
    @DisplayName("Should create comment notification message")
    void shouldCreateCommentNotificationMessage() {
        // Arrange
        Long commentId = 1L;
        Long authorId = 10L;
        String content = "Test comment";
        List<Long> mentions = Arrays.asList(20L, 30L);

        // Act
        CommentNotificationMessageDto message = CommentNotificationMessageDto.builder()
                .commentId(commentId)
                .authorId(authorId)
                .content(content)
                .mentionedUserIds(mentions)
                .type("COMMENT_CREATED")
                .priority("HIGH")
                .timestamp(OffsetDateTime.now())
                .build();

        // Assert
        assertEquals(commentId, message.getCommentId());
        assertEquals(authorId, message.getAuthorId());
        assertEquals(content, message.getContent());
        assertEquals(mentions, message.getMentionedUserIds());
        assertEquals("COMMENT_CREATED", message.getType());
    }
}