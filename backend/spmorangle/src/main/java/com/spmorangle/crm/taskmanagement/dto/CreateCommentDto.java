package com.spmorangle.crm.taskmanagement.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateCommentDto {

    private final Long taskId;

    private final Long subtaskId;

    private final Long parentCommentId;

    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment content cannot exceed 2000 characters")
    private final String content;

    private final List<Long> mentionedUserIds;
}
