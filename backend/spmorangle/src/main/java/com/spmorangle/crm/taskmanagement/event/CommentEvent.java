package com.spmorangle.crm.taskmanagement.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class CommentEvent {
    private final Long commentId;
    private final Long authorId;
    private final String eventType;
}