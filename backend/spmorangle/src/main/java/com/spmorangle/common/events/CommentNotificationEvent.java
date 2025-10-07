package com.spmorangle.common.events;

import java.time.LocalDateTime;
import java.util.List;

import com.spmorangle.common.enums.NotificationType;

import lombok.Getter;

@Getter
public class CommentNotificationEvent extends BaseNotificationEvent {
    
    // Core comment data
    private final Long commentId;
    private final Long authorId;
    private final String commentContent;

    // Task context
    private final Long taskId;
    private final String taskTitle;

    // Reply context
    private final Long parentCommentId;
    private final boolean isReply;

    // Multiple recipients (ENHANCED)
    private final List<Long> mentionedUserIds;
    private final List<Long> assigneeIds;
    private final Long parentCommentAuthorId;

    // Time stamp
    private final LocalDateTime commentTimestamp;

    public CommentNotificationEvent(NotificationType notificationType, Long recipientId,
                                   Long commentId, Long authorId, String commentContent,
                                   Long taskId, String taskTitle,
                                   Long parentCommentId, LocalDateTime commentTimestamp,
                                   List<Long> mentionedUserIds, List<Long> assigneeIds, Long parentCommentAuthorId) {
        super(notificationType, recipientId);
        this.commentId = commentId;
        this.authorId = authorId;
        this.commentContent = commentContent;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.parentCommentId = parentCommentId;
        this.isReply = parentCommentId != null;
        this.commentTimestamp = commentTimestamp;
        this.mentionedUserIds = mentionedUserIds != null ? mentionedUserIds : List.of();
        this.assigneeIds = assigneeIds != null ? assigneeIds : List.of();
        this.parentCommentAuthorId = parentCommentAuthorId;
    }
    /**
     * Gets comment snippet (first ~200 chars or first line) as per AC#1
     */
    public String getCommentSnippet() {
        if (commentContent == null) return "";
        
        // Get first line or first 200 characters, whichever is shorter
        String firstLine = commentContent.split("\n")[0];
        if (firstLine.length() <= 200) {
            return firstLine;
        }
        return commentContent.substring(0, 200) + "...";
    }

    /**
     * Check if author should be excluded from notifications (AC#4)
     */
    public boolean shouldNotifyRecipient() {
        return !authorId.equals(getRecipientId());
    }

    /**
     * Get notification title based on reply status (AC#3)
     */
    public String getNotificationTitle() {
        if (isReply) {
            return "New reply on task: " + taskTitle;
        }
        return "New comment on task: " + taskTitle;
    }

    /**
     * Get deep link URL for clickthrough (AC#2)
     */
    public String getDeepLinkUrl() {
        return "/tasks/" + taskId + "/comments#comment-" + commentId;
    }

    // Create event for new comment on tasks
    public static CommentNotificationEvent createNewComment(Long recipientId, Long commentId, 
                                                    Long authorId, String commentContent,
                                                    Long taskId, String taskTitle) {
        return new CommentNotificationEvent(
            NotificationType.COMMENT_ADDED,
            recipientId,
            commentId,
            authorId,
            commentContent,
            taskId,
            taskTitle,
            null, // not a reply
            java.time.LocalDateTime.now(),
            null, // no mentions
            null, // no assignees
            null  // no parent author
        );
    }

    // Create event for reply to existing comment
    public static CommentNotificationEvent createNewReply(Long recipientId, Long commentId,
                                               Long authorId, String commentContent,
                                               Long taskId, String taskTitle,
                                               Long parentCommentId) {
        return new CommentNotificationEvent(
            NotificationType.COMMENT_REPLY,
            recipientId,
            commentId,
            authorId,
            commentContent,
            taskId,
            taskTitle,
            parentCommentId,
            java.time.LocalDateTime.now(),
            null, // no mentions
            null, // no assignees  
            null  // parent author would be set separately
        );
    }
}
