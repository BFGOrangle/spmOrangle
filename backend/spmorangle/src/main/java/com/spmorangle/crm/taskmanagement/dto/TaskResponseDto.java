package com.spmorangle.crm.taskmanagement.dto;

import java.time.OffsetDateTime;
import java.util.List;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TaskResponseDto {
    private final Long id;
    private final Long projectId;
    private final Long ownerId;
    private final TaskType taskType;
    private final String title;
    private final String description;
    private final Status status;
    private final List<String> tags;
    private final boolean userHasEditAccess;
    private final boolean userHasDeleteAccess;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final Long createdBy;
    private final Long updatedBy;
    private final List<SubtaskResponseDto> subtasks;
    private final OffsetDateTime dueDateTime;
}
