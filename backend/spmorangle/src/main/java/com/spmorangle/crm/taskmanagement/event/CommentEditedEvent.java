package com.spmorangle.crm.taskmanagement.event;

public class CommentEditedEvent extends CommentEvent {

    public CommentEditedEvent(Long commentId, Long authorId) {
        super(commentId, authorId, "COMMENT_EDITED");
    }
}