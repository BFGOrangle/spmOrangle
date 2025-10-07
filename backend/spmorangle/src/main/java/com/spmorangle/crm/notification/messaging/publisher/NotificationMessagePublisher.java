package com.spmorangle.crm.notification.messaging.publisher;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.spmorangle.common.config.RabbitMQConfig;
import com.spmorangle.crm.notification.messaging.dto.CommentNotificationMessageDto;
import com.spmorangle.crm.notification.messaging.dto.TaskNotificationMessageDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publish comment notification message to RabbitMQ
     */
    public void publishCommentNotification(CommentNotificationMessageDto message) {
        try {
            String routingKey = message.getRoutingKey();
            log.info("📤 Publishing comment notification message: commentId={}, eventType={}, routingKey={}, recipients={}",
                    message.getCommentId(), message.getEventType(), routingKey,
                    getTotalRecipients(message));

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    routingKey,
                    message
            );

            log.info("✅ Successfully published comment notification message with ID: {}", message.getMessageId());

        } catch (Exception e) {
            log.error("❌ Failed to publish comment notification message: {}", e.getMessage(), e);
            // Consider implementing retry logic or dead letter handling
            throw new RuntimeException("Failed to publish notification message", e);
        }
    }

    /**
     * Publish task notification message to RabbitMQ
     */
    public void publishTaskNotification(TaskNotificationMessageDto message) {
        try {
            String routingKey = message.getRoutingKey();
            log.info("📤 Publishing task notification message: taskId={}, eventType={}, routingKey={}, assignees={}",
                    message.getTaskId(), message.getEventType(), routingKey,
                    message.hasAssignees() ? message.getAssignedUserIds().size() : 0);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    routingKey,
                    message
            );

            log.info("✅ Successfully published task notification message with ID: {}", message.getMessageId());

        } catch (Exception e) {
            log.error("❌ Failed to publish task notification message: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish task notification message", e);
        }
    }

    /**
     * Publish multiple notification messages in batch
     */
    public void publishBatch(CommentNotificationMessageDto... messages) {
        for (CommentNotificationMessageDto message : messages) {
            publishCommentNotification(message);
        }
    }

    private int getTotalRecipients(CommentNotificationMessageDto message) {
        int count = 0;
        if (message.getMentionedUserIds() != null) {
            count += message.getMentionedUserIds().size();
        }
        if (message.getAssigneeIds() != null) {
            count += message.getAssigneeIds().size();
        }
        if (message.getParentCommentAuthorId() != null) {
            count += 1;
        }
        return count;
    }
}