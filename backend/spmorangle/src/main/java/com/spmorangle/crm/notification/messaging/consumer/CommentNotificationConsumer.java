package com.spmorangle.crm.notification.messaging.consumer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.spmorangle.common.config.RabbitMQConfig;
import com.spmorangle.crm.notification.dto.CreateNotificationDto;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.messaging.dto.CommentNotificationMessageDto;
import com.spmorangle.crm.notification.service.EmailService;
import com.spmorangle.crm.notification.service.NotificationService;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.usermanagement.service.UserManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationConsumer {

    private final NotificationService notificationService;
    private final UserManagementService userManagementService; // To get user email
    private final EmailService emailService;
    private final TaskAssigneeRepository taskAssigneeRepository; // To get current assignees

    @RabbitListener(queues = RabbitMQConfig.COMMENT_QUEUE)
    public void handleCommentNotification(CommentNotificationMessageDto message) {
        log.info("🔔 RECEIVED comment notification message: {} for comment: {}", 
                message.getEventType(), message.getCommentId());
        log.info("📋 Message details - Has Mentions: {}, Mentioned Users: {}, Author: {}", 
                message.hasMentions(), message.getMentionedUserIds(), message.getAuthorId());

        try {
            // Step 1: Create in-app notifications (existing logic)
            List<CreateNotificationDto> notificationsToCreate = new ArrayList<>();
            
            switch (message.getEventType()) {
                case "COMMENT_CREATED":
                    notificationsToCreate.addAll(processCommentCreated(message));
                    break;
                case "COMMENT_REPLY":
                    notificationsToCreate.addAll(processCommentReply(message));
                    break;
                case "MENTION":
                    notificationsToCreate.addAll(processMentions(message));
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

            // Step 3: Send external notifications (EMAIL, SMS)
            sendExternalNotifications(createdNotifications);

            log.info("Successfully processed comment notification message: {}", message.getMessageId());

        } catch (Exception e) {
            log.error("Error processing comment notification message {}: {}", 
                    message.getMessageId(), e.getMessage(), e);
            throw e;
        }
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
                    // Don't fail the entire process if one channel fails
                }
            }
        }
    }

    private void sendEmailNotification(NotificationDto notification) {
        log.info("Sending email notification to user: {}", notification.getTargetId());
        
        try {
            // Get user email address
            String userEmail = getUserEmail(notification.getTargetId());
            if (userEmail == null || userEmail.trim().isEmpty()) {
                log.warn("No email address found for user: {}", notification.getTargetId());
                return;
            }

            // Format email content
            String emailSubject = notification.getSubject();
            String emailBody = formatEmailBody(notification);

            // Send email (you'd implement this)
            emailService.sendEmail(userEmail, emailSubject, emailBody);
            
            // For now, just log
            log.info("Email notification sent to {}: Subject='{}', Body='{}'", 
                    userEmail, emailSubject, emailBody.substring(0, Math.min(50, emailBody.length())));
            
        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage(), e);
            throw e;
        }
    }

    private String getUserEmail(Long userId) {
        try {
            var user = userManagementService.getUserById(userId);
            return user.email(); // UserResponseDto is a record with email() method
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
            emailBody.append("Click here to view: ").append(notification.getLink()).append("\n\n");
        }
        
        emailBody.append("Best regards,\n");
        emailBody.append("SPM Orange Team");
        
        return emailBody.toString();
    }

    // Your existing processing methods stay the same, but with multi-channel support
    private List<CreateNotificationDto> processCommentCreated(CommentNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();
        log.info("🔄 Processing COMMENT_CREATED with {} mentions",
                message.hasMentions() ? message.getMentionedUserIds().size() : 0);

        // Notify mentioned users (HIGH priority = EMAIL + IN_APP)
        if (message.hasMentions()) {
            log.info("👥 Processing {} mentioned users: {}",
                    message.getMentionedUserIds().size(), message.getMentionedUserIds());

            for (Long mentionedUserId : message.getMentionedUserIds()) {
                if (!mentionedUserId.equals(message.getAuthorId())) {
                    log.info("📝 Creating mention notification for user: {}", mentionedUserId);
                    notifications.add(CreateNotificationDto.builder()
                            .authorId(message.getAuthorId())
                            .targetId(mentionedUserId)
                            .notificationType(com.spmorangle.common.enums.NotificationType.MENTION)
                            .subject("You were mentioned in a comment")
                            .message(String.format("You were mentioned: \"%s\"", message.getCommentSnippet(100)))
                            .link(message.generateNotificationLink())
                            .priority(com.spmorangle.crm.notification.enums.Priority.HIGH)
                            .channels(List.of(Channel.IN_APP, Channel.EMAIL)) // ← Multi-channel!
                            .build());
                } else {
                    log.info("⏭️ Skipping mention notification for author: {}", mentionedUserId);
                }
            }
        } else {
            log.info("👥 No mentions found in message");
        }

        // Notify task assignees (MEDIUM priority = IN_APP + EMAIL)
        // IMPORTANT: Query CURRENT assignees from database, not the stale list from message
        // This ensures unassigned users don't receive notifications (AC#5)
        if (message.getTaskId() != null) {
            List<Long> currentAssigneeIds = taskAssigneeRepository.findAssigneeIdsByTaskId(message.getTaskId());
            log.info("📋 Found {} CURRENT assignees for task {}", currentAssigneeIds.size(), message.getTaskId());

            for (Long assigneeId : currentAssigneeIds) {
                // Skip if author or already notified via mention
                if (!assigneeId.equals(message.getAuthorId()) &&
                    (message.getMentionedUserIds() == null || !message.getMentionedUserIds().contains(assigneeId))) {
                    log.info("📬 Creating assignee notification for user: {}", assigneeId);
                    notifications.add(CreateNotificationDto.builder()
                            .authorId(message.getAuthorId())
                            .targetId(assigneeId)
                            .notificationType(com.spmorangle.common.enums.NotificationType.COMMENT_REPLY)
                            .subject("New comment on your task")
                            .message(String.format("New comment on \"%s\": \"%s\"",
                                                message.getTaskTitle(), message.getCommentSnippet(100)))
                            .link(message.generateNotificationLink())
                            .priority(com.spmorangle.crm.notification.enums.Priority.MEDIUM)
                            .channels(List.of(Channel.IN_APP, Channel.EMAIL))
                            .build());
                } else {
                    log.info("⏭️ Skipping assignee notification for user: {} (author or already mentioned)", assigneeId);
                }
            }
        }

        return notifications;
    }

    private List<CreateNotificationDto> processCommentReply(CommentNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();

        // Notify parent comment author
        if (message.getParentCommentAuthorId() != null && 
            !message.getParentCommentAuthorId().equals(message.getAuthorId())) {
            notifications.add(CreateNotificationDto.forCommentReply(
                    message.getAuthorId(),
                    message.getParentCommentAuthorId(),
                    "Reply to your comment",
                    String.format("New reply: \"%s\"", message.getCommentSnippet(100)),
                    message.generateNotificationLink()
            ));
        }

        // Also process as regular comment (for mentions and assignees)
        notifications.addAll(processCommentCreated(message));

        return notifications;
    }

    private List<CreateNotificationDto> processMentions(CommentNotificationMessageDto message) {
        List<CreateNotificationDto> notifications = new ArrayList<>();
        log.info("🔄 Processing MENTION event with {} mentions", 
                message.hasMentions() ? message.getMentionedUserIds().size() : 0);

        if (message.hasMentions()) {
            log.info("👥 Processing {} mentioned users: {}", 
                    message.getMentionedUserIds().size(), message.getMentionedUserIds());
                    
            for (Long mentionedUserId : message.getMentionedUserIds()) {
                if (!mentionedUserId.equals(message.getAuthorId())) {
                    log.info("📝 Creating MENTION notification for user: {}", mentionedUserId);
                    notifications.add(CreateNotificationDto.forMention(
                            message.getAuthorId(),
                            mentionedUserId,
                            "You were mentioned",
                            String.format("You were mentioned in \"%s\": \"%s\"", 
                                        message.getTaskTitle(), message.getCommentSnippet(100)),
                            message.generateNotificationLink()
                    ));
                } else {
                    log.info("⏭️ Skipping mention notification for author: {}", mentionedUserId);
                }
            }
        } else {
            log.info("👥 No mentions found in MENTION event message");
        }

        return notifications;
    }
}