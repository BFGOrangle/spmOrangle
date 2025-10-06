package com.spmorangle.crm.notification.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spmorangle.crm.notification.messaging.dto.TaskNotificationMessageDto;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Test controller for debugging task notification functionality
 */
@Slf4j
@RestController
@RequestMapping("/api/test/task-notifications")
@RequiredArgsConstructor
public class TaskNotificationTestController {

    private final NotificationMessagePublisher notificationPublisher;

    @PostMapping("/send")
    public ResponseEntity<String> sendTestTaskNotification(@RequestBody TestTaskNotificationRequest request) {
        log.info("📨 Received test task notification request: {}", request);

        try {
            TaskNotificationMessageDto message = TaskNotificationMessageDto.forTaskCreated(
                request.getTaskId(),
                request.getAuthorId(),
                request.getProjectId(),
                request.getTaskTitle(),
                request.getTaskDescription(),
                request.getAssignedUserIds()
            );

            notificationPublisher.publishTaskNotification(message);

            String response = String.format(
                "✅ Successfully published task notification:\n" +
                "- Task ID: %d\n" +
                "- Author ID: %d\n" +
                "- Assignees: %s\n" +
                "- Message ID: %s",
                request.getTaskId(),
                request.getAuthorId(),
                request.getAssignedUserIds(),
                message.getMessageId()
            );

            log.info(response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to send test task notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send notification: " + e.getMessage());
        }
    }

    @PostMapping("/send-assigned")
    public ResponseEntity<String> sendTestTaskAssignedNotification(@RequestBody TestTaskNotificationRequest request) {
        log.info("📨 Received test task assigned notification request: {}", request);

        try {
            TaskNotificationMessageDto message = TaskNotificationMessageDto.forTaskAssigned(
                request.getTaskId(),
                request.getAuthorId(),
                request.getProjectId(),
                request.getTaskTitle(),
                request.getTaskDescription(),
                request.getAssignedUserIds()
            );

            notificationPublisher.publishTaskNotification(message);

            String response = String.format(
                "✅ Successfully published task assignment notification:\n" +
                "- Task ID: %d\n" +
                "- Author ID: %d\n" +
                "- Newly Assigned: %s\n" +
                "- Message ID: %s",
                request.getTaskId(),
                request.getAuthorId(),
                request.getAssignedUserIds(),
                message.getMessageId()
            );

            log.info(response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to send test task assigned notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send notification: " + e.getMessage());
        }
    }

    @PostMapping("/send-completed")
    public ResponseEntity<String> sendTestTaskCompletedNotification(@RequestBody TestTaskNotificationRequest request) {
        log.info("📨 Received test task completed notification request: {}", request);

        try {
            TaskNotificationMessageDto message = TaskNotificationMessageDto.forTaskCompleted(
                request.getTaskId(),
                request.getAuthorId(),
                request.getProjectId(),
                request.getTaskTitle(),
                request.getAssignedUserIds()
            );

            notificationPublisher.publishTaskNotification(message);

            String response = String.format(
                "✅ Successfully published task completion notification:\n" +
                "- Task ID: %d\n" +
                "- Completed By: %d\n" +
                "- Notified Users: %s\n" +
                "- Message ID: %s",
                request.getTaskId(),
                request.getAuthorId(),
                request.getAssignedUserIds(),
                message.getMessageId()
            );

            log.info(response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("❌ Failed to send test task completed notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to send notification: " + e.getMessage());
        }
    }

    @Data
    public static class TestTaskNotificationRequest {
        private Long taskId;
        private Long authorId;
        private Long projectId;
        private String taskTitle;
        private String taskDescription;
        private List<Long> assignedUserIds;
    }
}
