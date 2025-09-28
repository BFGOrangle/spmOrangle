package com.spmorangle.crm.taskmanagement.dto;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponseDto {
    private final Long id;
    private final Long taskId;
    private final Long subtaskId;
    private final Long projectId;
    private final Long parentCommentId;
    private final String content;
    private final List<Long> mentionedUserIds;
    private final boolean isEdited;
    private final boolean isDeleted;
    private final Long authorId;
    private final String authorUsername;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final List<CommentResponseDto> replies;
    private final int replyCount;
    private final boolean canEdit;
    private final boolean canDelete;
    private final boolean canReply;
    private final boolean canModerate;
}