package com.spmorangle.crm.taskmanagement.dto;

import java.time.OffsetDateTime;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SubtaskResponseDto {
    private final Long id;
    private final Long taskId;
    private final Long projectId;
    private final TaskType taskType;
    private final String title;
    private final String details;
    private final Status status;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final Long createdBy;
    private final Long updatedBy;
}
