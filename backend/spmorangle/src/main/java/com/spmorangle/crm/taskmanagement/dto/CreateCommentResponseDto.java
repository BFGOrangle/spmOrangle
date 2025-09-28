package com.spmorangle.crm.taskmanagement.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateCommentResponseDto {
    private final Long id;
    private final Long taskId;
    private final Long subtaskId;
    private final Long parentCommentId;
    private final String content;
    private final Long authorId;
    private final OffsetDateTime createdAt;
}