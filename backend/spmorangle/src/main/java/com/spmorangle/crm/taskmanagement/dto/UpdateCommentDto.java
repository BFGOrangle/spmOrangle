package com.spmorangle.crm.taskmanagement.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateCommentDto {

    @NotNull(message = "Comment ID is required")
    private final Long commentId;

    @NotBlank(message = "Comment content is required")
    @Size(max = 2000, message = "Comment content cannot exceed 2000 characters")
    private final String content;

    private final List<Long> mentionedUserIds;
}