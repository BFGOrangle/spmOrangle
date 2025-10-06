package com.spmorangle.crm.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.spmorangle.crm.notification.messaging.dto.CommentNotificationMessageDto;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/test/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Test", description = "Test notification functionality")
public class NotificationTestController {

    private final NotificationMessagePublisher notificationPublisher;

    @PostMapping("/trigger-comment")
    @Operation(summary = "Trigger test comment notification", description = "Manually trigger a comment notification to test RabbitMQ")
    public ResponseEntity<String> triggerCommentNotification(
            @RequestParam(defaultValue = "1") Long commentId,
            @RequestParam(defaultValue = "10") Long authorId,
            @RequestParam(defaultValue = "100") Long taskId,
            @RequestParam(defaultValue = "Test notification content") String content) {
        
        try {
            log.info("🧪 TRIGGERING test comment notification...");
            
            CommentNotificationMessageDto message = CommentNotificationMessageDto.forCommentCreated(
                commentId,
                authorId,
                content,
                taskId,
                "Test Task Title",
                List.of(20L, 30L), // mentioned users
                List.of(40L, 50L)  // assignees
            );
            
            log.info("📤 About to publish message: {}", message);
            notificationPublisher.publishCommentNotification(message);
            
            return ResponseEntity.ok("✅ Test notification triggered successfully! " +
                "Check logs for consumer activity. MessageId: " + message.getMessageId());
                
        } catch (Exception e) {
            log.error("❌ Failed to trigger test notification: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body("❌ Failed to trigger notification: " + e.getMessage());
        }
    }

    @PostMapping("/trigger-mention")
    @Operation(summary = "Trigger test mention notification", description = "Manually trigger a mention notification")
    public ResponseEntity<String> triggerMentionNotification(
            @RequestParam(defaultValue = "2") Long commentId,
            @RequestParam(defaultValue = "11") Long authorId,
            @RequestParam(defaultValue = "101") Long taskId) {
        
        try {
            log.info("🧪 TRIGGERING test mention notification...");
            
            CommentNotificationMessageDto message = CommentNotificationMessageDto.forMention(
                commentId,
                authorId,
                "Hey @user, check this out!",
                taskId,
                "Important Task",
                List.of(21L, 31L) // mentioned users
            );
            
            notificationPublisher.publishCommentNotification(message);
            
            return ResponseEntity.ok("✅ Test mention notification triggered! MessageId: " + message.getMessageId());
                
        } catch (Exception e) {
            log.error("❌ Failed to trigger test mention: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                .body("❌ Failed to trigger mention: " + e.getMessage());
        }
    }
}