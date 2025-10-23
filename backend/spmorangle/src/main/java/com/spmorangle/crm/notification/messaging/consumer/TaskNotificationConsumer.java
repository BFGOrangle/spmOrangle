package com.spmorangle.crm.notification.messaging.consumer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.spmorangle.common.config.FrontendConfig;
import com.spmorangle.common.config.RabbitMQConfig;
import com.spmorangle.crm.notification.dto.CreateNotificationDto;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.messaging.dto.TaskNotificationMessageDto;
import com.spmorangle.crm.notification.service.EmailService;
import com.spmorangle.crm.notification.service.NotificationService;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskNotificationConsumer {

    private final NotificationService notificationService;
    private final UserManagementService userManagementService;
    private final EmailService emailService;
    private final FrontendConfig frontendConfig;

    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void handleTaskNotification(TaskNotificationMessageDto message) {
        log.info("🔔 RECEIVED task notification message: {} for task: {}",
                message.getEventType(), message.getTaskId());
        log.info("📋 Message details - Event: {}, Author: {}, Assignees: {}",
                message.getEventType(), message.getAuthorId(), message.getAssignedUserIds());

        try {
            // Step 1: Create in-app notifications
            List<CreateNotificationDto> notificationsToCreate = new ArrayList<>();

            switch (message.getEventType()) {
                case "TASK_CREATED":
                    notificationsToCreate.addAll(processTaskCreated(message));
                    break;
                case "TASK_ASSIGNED":
                    notificationsToCreate.addAll(processTaskAssigned(message));
                    break;
                case "TASK_COMPLETED":
                    notificationsToCreate.addAll(processTaskCompleted(message));
                    break;
                case "TASK_UPDATED":
                    notificationsToCreate.addAll(processTaskUpdated(message));
                    break;
                case "STATUS_UPDATED":
                    notificationsToCreate.addAll(processStatusUpdated(message));
                    break;
                case "TASK_UNASSIGNED":
                    notificationsToCreate.addAll(processTaskUnassigned(message));
                    break;
                default:
                    log.warn("Unknown event type: {}", message.getEventType());
                    return;
            }

            // Step 2: Save in-app notifications to database
            List<NotificationDto> createdNotifications = List.of();
            if (!notificationsToCreate.isEmpty()) {
                log.info("Creating {} in-app notifications for message: {}",
                        notificationsToCreate.size(), message.getMessageId());
                createdNotifications = notificationService.createBulkNotifications(notificationsToCreate);
            }

            // Step 3: Send external notifications (EMAIL)
            sendExternalNotifications(createdNotifications);

            log.info("Successfully processed task notification message: {}", message.getMessageId());

        } catch (Exception e) {
            log.error("Error processing task notification message {}: {}",
                    message.getMessageId(), e.getMessage(), e);
            throw e;
        }
    }

    private List<CreateNotificationDto> processTaskCreated(TaskNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();
        log.info("🔄 Processing TASK_CREATED for {} assignees", 
                message.hasAssignees() ? message.getAssignedUserIds().size() : 0);

        // Notify assigned users
        if (message.hasAssignees()) {
            for (Long assigneeId : message.getAssignedUserIds()) {
                log.info("📝 Creating task assignment notification for user: {}", assigneeId);
                notifications.add(CreateNotificationDto.builder()
                        .authorId(message.getAuthorId())
                        .targetId(assigneeId)
                        .notificationType(com.spmorangle.common.enums.NotificationType.TASK_ASSIGNED)
                        .subject("New task assigned to you")
                        .message(String.format("You've been assigned to task: \"%s\"", message.getTaskTitle()))
                        .link(message.generateNotificationLinkWithContext("assignees"))
                        .priority(com.spmorangle.crm.notification.enums.Priority.MEDIUM)
                        .channels(List.of(Channel.IN_APP, Channel.EMAIL))
                        .build());
            }
        }

        return notifications;
    }

    private List<CreateNotificationDto> processTaskAssigned(TaskNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();
        log.info("🔄 Processing TASK_ASSIGNED for {} assignees",
                message.hasAssignees() ? message.getAssignedUserIds().size() : 0);

        if (message.hasAssignees()) {
            for (Long assigneeId : message.getAssignedUserIds()) {
                if (!assigneeId.equals(message.getAuthorId())) {
                    notifications.add(CreateNotificationDto.builder()
                            .authorId(message.getAuthorId())
                            .targetId(assigneeId)
                            .notificationType(com.spmorangle.common.enums.NotificationType.TASK_ASSIGNED)
                            .subject("Task assigned to you")
                            .message(String.format("You've been assigned to task: \"%s\"", message.getTaskTitle()))
                            .link(message.generateNotificationLinkWithContext("assignees"))
                            .priority(com.spmorangle.crm.notification.enums.Priority.HIGH)
                            .channels(List.of(Channel.IN_APP, Channel.EMAIL))
                            .build());
                }
            }
        }

        return notifications;
    }

    private List<CreateNotificationDto> processTaskCompleted(TaskNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();
        log.info("🔄 Processing TASK_COMPLETED for {} assignees",
                message.hasAssignees() ? message.getAssignedUserIds().size() : 0);

        if (message.hasAssignees()) {
            for (Long assigneeId : message.getAssignedUserIds()) {
                if (!assigneeId.equals(message.getAuthorId())) {
                    notifications.add(CreateNotificationDto.builder()
                            .authorId(message.getAuthorId())
                            .targetId(assigneeId)
                            .notificationType(com.spmorangle.common.enums.NotificationType.TASK_COMPLETED)
                            .subject("Task completed")
                            .message(String.format("Task \"%s\" has been completed", message.getTaskTitle()))
                            .link(message.generateNotificationLink())
                            .priority(com.spmorangle.crm.notification.enums.Priority.LOW)
                            .channels(List.of(Channel.IN_APP))
                            .build());
                }
            }
        }

        return notifications;
    }

    private List<CreateNotificationDto> processTaskUpdated(TaskNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();
        log.info("🔄 Processing TASK_UPDATED for {} assignees",
                message.hasAssignees() ? message.getAssignedUserIds().size() : 0);

        if (message.hasAssignees()) {
            for (Long assigneeId : message.getAssignedUserIds()) {
                if (!assigneeId.equals(message.getAuthorId())) {
                    String updateMessage = message.getTaskStatus() != null
                            ? String.format("Task \"%s\" status updated to: %s", message.getTaskTitle(), message.getTaskStatus())
                            : String.format("Task \"%s\" has been updated", message.getTaskTitle());

                    notifications.add(CreateNotificationDto.builder()
                            .authorId(message.getAuthorId())
                            .targetId(assigneeId)
                            .notificationType(com.spmorangle.common.enums.NotificationType.TASK_ASSIGNED)
                            .subject("Task updated")
                            .message(updateMessage)
                            .link(message.generateNotificationLink())
                            .priority(com.spmorangle.crm.notification.enums.Priority.LOW)
                            .channels(List.of(Channel.IN_APP))
                            .build());
                }
            }
        }

        return notifications;
    }

    private List<CreateNotificationDto> processStatusUpdated(TaskNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();
        log.info("🔄 Processing STATUS_UPDATED for {} assignees",
                message.hasAssignees() ? message.getAssignedUserIds().size() : 0);

        if (message.hasAssignees()) {
            for (Long assigneeId : message.getAssignedUserIds()) {
                if (!assigneeId.equals(message.getAuthorId())) {
                    String updateMessage = String.format(
                        "Task \"%s\" status changed from %s to %s by %s",
                        message.getTaskTitle(),
                        message.getPrevTaskStatus(),
                        message.getTaskStatus(),
                        getEditorName(message.getAuthorId())
                    );
                    notifications.add(CreateNotificationDto.builder()
                            .authorId(message.getAuthorId())
                            .targetId(assigneeId)
                            .notificationType(com.spmorangle.common.enums.NotificationType.TASK_ASSIGNED)
                            .subject("Task status updated")
                            .message(updateMessage)
                            .link(message.generateNotificationLinkWithContext("status"))
                            .priority(com.spmorangle.crm.notification.enums.Priority.MEDIUM)
                            .channels(List.of(Channel.IN_APP, Channel.EMAIL))
                            .build());
                }
            }
        }

        return notifications;
    }

    private List<CreateNotificationDto> processTaskUnassigned(TaskNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();
        log.info("🔄 Processing TASK_UNASSIGNED for {} removed assignees",
                message.hasAssignees() ? message.getAssignedUserIds().size() : 0);

        if (message.hasAssignees()) {
            for (Long removedUserId : message.getAssignedUserIds()) {
                if (!removedUserId.equals(message.getAuthorId())) {
                    notifications.add(CreateNotificationDto.builder()
                            .authorId(message.getAuthorId())
                            .targetId(removedUserId)
                            .notificationType(com.spmorangle.common.enums.NotificationType.TASK_ASSIGNED)
                            .subject("Removed from task")
                            .message(String.format("You've been removed from task: \"%s\"", message.getTaskTitle()))
                            .link(message.generateNotificationLinkWithContext("assignees"))
                            .priority(com.spmorangle.crm.notification.enums.Priority.MEDIUM)
                            .channels(List.of(Channel.IN_APP, Channel.EMAIL))
                            .build());
                }
            }
        }

        return notifications;
    }

    private void sendExternalNotifications(List<NotificationDto> notifications) {
        for (NotificationDto notification : notifications) {
            for (Channel channel : notification.getChannels()) {
                try {
                    switch (channel) {
                        case IN_APP:
                            // Already handled - stored in database
                            break;

                        case EMAIL:
                            sendEmailNotification(notification);
                            break;

                        default:
                            log.warn("Unsupported channel: {} (only IN_APP and EMAIL are supported)", channel);
                    }
                } catch (Exception e) {
                    log.error("Failed to send {} notification for notification {}: {}",
                            channel, notification.getNotificationId(), e.getMessage(), e);
                }
            }
        }
    }

    private void sendEmailNotification(NotificationDto notification) {
        log.info("Sending email notification to user: {}", notification.getTargetId());

        try {
            String userEmail = getUserEmail(notification.getTargetId());
            if (userEmail == null || userEmail.trim().isEmpty()) {
                log.warn("No email address found for user: {}", notification.getTargetId());
                return;
            }

            String emailSubject = notification.getSubject();
            String emailBody = formatEmailBody(notification);

            emailService.sendEmail(userEmail, emailSubject, emailBody);

            log.info("Email notification sent to {}: Subject='{}'",
                    userEmail, emailSubject);

        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String getUserEmail(Long userId) {
        try {
            var user = userManagementService.getUserById(userId);
            return user.email();
        } catch (Exception e) {
            log.error("Failed to get email for user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    private String formatEmailBody(NotificationDto notification) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Hello,\n\n");
        emailBody.append(notification.getMessage()).append("\n\n");

        if (notification.getLink() != null) {
            String fullUrl = frontendConfig.getBaseUrl() + notification.getLink();
            emailBody.append("Click here to view: ").append(fullUrl).append("\n\n");
        }

        emailBody.append("Best regards,\n");
        emailBody.append("SPM Orangle Team");

        return emailBody.toString();
    }

    private String getEditorName(Long userId) {
        try {
            UserResponseDto user = userManagementService.getUserById(userId);
            String username = user.username();
            return username;
        } catch (Exception e) {
            log.error("Failed to get username for userid {}: {}", userId, e.getMessage());
            return "Unknown User";
        }
    }
}
