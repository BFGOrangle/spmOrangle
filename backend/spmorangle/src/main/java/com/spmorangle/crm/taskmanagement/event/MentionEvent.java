package com.spmorangle.crm.taskmanagement.event;

import java.util.List;

import lombok.Getter;

@Getter
public class MentionEvent extends CommentEvent {
    private final List<Long> mentionedUserIds;

    public MentionEvent(Long commentId, Long authorId, List<Long> mentionedUserIds) {
        super(commentId, authorId, "MENTION");
        this.mentionedUserIds = mentionedUserIds;
    }
}