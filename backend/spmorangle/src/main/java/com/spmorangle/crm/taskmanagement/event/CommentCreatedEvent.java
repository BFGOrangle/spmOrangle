package com.spmorangle.crm.taskmanagement.event;

public class CommentCreatedEvent extends CommentEvent {

    public CommentCreatedEvent(Long commentId, Long authorId) {
        super(commentId, authorId, "COMMENT_CREATED");
    }
}