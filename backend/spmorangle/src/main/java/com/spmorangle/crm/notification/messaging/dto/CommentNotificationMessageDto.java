package com.spmorangle.crm.notification.messaging.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentNotificationMessageDto {
    private String messageId;
    private String eventType;
    private Long commentId;
    private Long authorId;
    private Long taskId;
    private Long subtaskId;
    private Long projectId;
    private String taskTitle;
    private String content;
    private List<Long> mentionedUserIds;
    private List<Long> assigneeIds;
    private Long parentCommentAuthorId;
    private String type; // COMMENT_CREATED, COMMENT_EDITED, MENTION
    private String priority; // HIGH, MEDIUM, LOW
    private OffsetDateTime timestamp;

    @JsonCreator
    public CommentNotificationMessageDto(
            @JsonProperty("messageId") String messageId,
            @JsonProperty("eventType") String eventType,
            @JsonProperty("commentId") Long commentId,
            @JsonProperty("authorId") Long authorId,
            @JsonProperty("taskId") Long taskId,
            @JsonProperty("subtaskId") Long subtaskId,
            @JsonProperty("projectId") Long projectId,
            @JsonProperty("taskTitle") String taskTitle,
            @JsonProperty("content") String content,
            @JsonProperty("mentionedUserIds") List<Long> mentionedUserIds,
            @JsonProperty("assigneeIds") List<Long> assigneeIds,
            @JsonProperty("parentCommentAuthorId") Long parentCommentAuthorId,
            @JsonProperty("type") String type,
            @JsonProperty("priority") String priority,
            @JsonProperty("timestamp") OffsetDateTime timestamp) {
        this.messageId = messageId;
        this.eventType = eventType;
        this.commentId = commentId;
        this.authorId = authorId;
        this.taskId = taskId;
        this.subtaskId = subtaskId;
        this.projectId = projectId;
        this.taskTitle = taskTitle;
        this.content = content;
        this.mentionedUserIds = mentionedUserIds;
        this.assigneeIds = assigneeIds;
        this.parentCommentAuthorId = parentCommentAuthorId;
        this.type = type;
        this.priority = priority;
        this.timestamp = timestamp;
    }

    // Static factory method for comment creation
    public static CommentNotificationMessageDto forCommentCreated(
            Long commentId,
            Long authorId,
            String content,
            Long taskId,
            String taskTitle,
            List<Long> mentionedUserIds,
            List<Long> assigneeIds) {
        return CommentNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("COMMENT_CREATED")
                .commentId(commentId)
                .authorId(authorId)
                .content(content)
                .taskId(taskId)
                .taskTitle(taskTitle)
                .mentionedUserIds(mentionedUserIds)
                .assigneeIds(assigneeIds)
                .type("COMMENT_CREATED")
                .priority("MEDIUM")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    // Static factory method for comment reply
    public static CommentNotificationMessageDto forCommentReply(
            Long commentId,
            Long authorId,
            String content,
            Long parentCommentId,
            Long parentAuthorId,
            Long taskId,
            String taskTitle,
            List<Long> mentionedUserIds,
            List<Long> assigneeIds) {
        return CommentNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("COMMENT_REPLY")
                .commentId(commentId)
                .authorId(authorId)
                .content(content)
                .taskId(taskId)
                .taskTitle(taskTitle)
                .mentionedUserIds(mentionedUserIds)
                .assigneeIds(assigneeIds)
                .parentCommentAuthorId(parentAuthorId)
                .type("COMMENT_REPLY")
                .priority("HIGH")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    // Static factory method for mentions
    public static CommentNotificationMessageDto forMention(
            Long commentId,
            Long authorId,
            String content,
            Long taskId,
            String taskTitle,
            List<Long> mentionedUserIds) {
        return CommentNotificationMessageDto.builder()
                .messageId(UUID.randomUUID().toString())
                .eventType("MENTION")
                .commentId(commentId)
                .authorId(authorId)
                .content(content)
                .taskId(taskId)
                .taskTitle(taskTitle)
                .mentionedUserIds(mentionedUserIds)
                .type("MENTION")
                .priority("HIGH")
                .timestamp(OffsetDateTime.now())
                .build();
    }

    // Helper methods
    public String getRoutingKey() {
        if (eventType == null) {
            return "notification.comment.unknown";
        }
        return switch (eventType) {
            case "COMMENT_CREATED" -> "notification.comment.created";
            case "COMMENT_REPLY" -> "notification.comment.reply";
            case "MENTION" -> "notification.comment.mention";
            case "COMMENT_EDITED" -> "notification.comment.edited";
            default -> "notification.comment.unknown";
        };
    }

    public boolean hasMentions() {
        return mentionedUserIds != null && !mentionedUserIds.isEmpty();
    }

    public boolean hasAssignees() {
        return assigneeIds != null && !assigneeIds.isEmpty();
    }

    public String getCommentSnippet(int maxLength) {
        if (content == null) {
            return "";
        }
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    public String generateNotificationLink() {
        if (taskId != null) {
            return "/tasks/" + taskId;
        } else if (subtaskId != null) {
            return "/subtasks/" + subtaskId;
        }
        return "/";
    }
}