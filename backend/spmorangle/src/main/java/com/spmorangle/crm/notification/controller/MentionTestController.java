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
@RequestMapping("/api/test/mentions")
@RequiredArgsConstructor
@Tag(name = "Mention Test", description = "Test mention functionality")
public class MentionTestController {

    private final NotificationMessagePublisher notificationPublisher;

    @PostMapping("/send")
    @Operation(summary = "Send test mention notification", description = "Send a test mention notification to verify mention processing")
    public ResponseEntity<TestResponse> sendTestMention(@RequestBody TestMentionRequest request) {
        try {
            log.info("🧪 Creating test mention notification for users: {}", request.getMentionedUserIds());
            
            // Create a test mention message
            CommentNotificationMessageDto mentionMessage = CommentNotificationMessageDto.forMention(
                    request.getCommentId() != null ? request.getCommentId() : 999L,
                    request.getAuthorId() != null ? request.getAuthorId() : 1L,
                    request.getContent() != null ? request.getContent() : "This is a test mention: @user",
                    request.getTaskId() != null ? request.getTaskId() : 1L,
                    request.getTaskTitle() != null ? request.getTaskTitle() : "Test Task",
                    request.getMentionedUserIds()
            );
            
            log.info("📤 Publishing mention message: {}", mentionMessage);
            log.info("📋 Message details - Event Type: {}, Mentions: {}, Has Mentions: {}", 
                    mentionMessage.getEventType(), 
                    mentionMessage.getMentionedUserIds(),
                    mentionMessage.hasMentions());
            
            // Publish the message
            notificationPublisher.publishCommentNotification(mentionMessage);
            
            return ResponseEntity.ok(TestResponse.builder()
                .success(true)
                .message("Test mention notification sent successfully for users: " + request.getMentionedUserIds())
                .build());
                
        } catch (Exception e) {
            log.error("❌ Failed to send test mention: {}", e.getMessage(), e);
            
            return ResponseEntity.status(500)
                .body(TestResponse.builder()
                    .success(false)
                    .message("Failed to send mention: " + e.getMessage())
                    .build());
        }
    }

    @PostMapping("/send-comment-with-mentions")
    @Operation(summary = "Send test comment with mentions", description = "Send a test comment creation notification with mentions")
    public ResponseEntity<TestResponse> sendTestCommentWithMentions(@RequestBody TestMentionRequest request) {
        try {
            log.info("🧪 Creating test comment with mentions for users: {}", request.getMentionedUserIds());
            
            // Create a test comment created message with mentions
            CommentNotificationMessageDto commentMessage = CommentNotificationMessageDto.forCommentCreated(
                    request.getCommentId() != null ? request.getCommentId() : 999L,
                    request.getAuthorId() != null ? request.getAuthorId() : 1L,
                    request.getContent() != null ? request.getContent() : "This is a test comment with mentions: @user",
                    request.getTaskId() != null ? request.getTaskId() : 1L,
                    request.getTaskTitle() != null ? request.getTaskTitle() : "Test Task",
                    request.getMentionedUserIds(),
                    List.of(2L, 3L) // Test assignees
            );
            
            log.info("📤 Publishing comment message: {}", commentMessage);
            log.info("📋 Message details - Event Type: {}, Mentions: {}, Has Mentions: {}", 
                    commentMessage.getEventType(), 
                    commentMessage.getMentionedUserIds(),
                    commentMessage.hasMentions());
            
            // Publish the message
            notificationPublisher.publishCommentNotification(commentMessage);
            
            return ResponseEntity.ok(TestResponse.builder()
                .success(true)
                .message("Test comment with mentions sent successfully for users: " + request.getMentionedUserIds())
                .build());
                
        } catch (Exception e) {
            log.error("❌ Failed to send test comment with mentions: {}", e.getMessage(), e);
            
            return ResponseEntity.status(500)
                .body(TestResponse.builder()
                    .success(false)
                    .message("Failed to send comment with mentions: " + e.getMessage())
                    .build());
        }
    }

    // DTO Classes
    public static class TestMentionRequest {
        private Long commentId;
        private Long authorId;
        private String content;
        private Long taskId;
        private String taskTitle;
        private List<Long> mentionedUserIds;

        // Constructors
        public TestMentionRequest() {}

        // Getters and Setters
        public Long getCommentId() { return commentId; }
        public void setCommentId(Long commentId) { this.commentId = commentId; }

        public Long getAuthorId() { return authorId; }
        public void setAuthorId(Long authorId) { this.authorId = authorId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Long getTaskId() { return taskId; }
        public void setTaskId(Long taskId) { this.taskId = taskId; }

        public String getTaskTitle() { return taskTitle; }
        public void setTaskTitle(String taskTitle) { this.taskTitle = taskTitle; }

        public List<Long> getMentionedUserIds() { return mentionedUserIds; }
        public void setMentionedUserIds(List<Long> mentionedUserIds) { this.mentionedUserIds = mentionedUserIds; }
    }

    @lombok.Builder
    public static class TestResponse {
        private boolean success;
        private String message;

        public TestResponse() {}

        public TestResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}