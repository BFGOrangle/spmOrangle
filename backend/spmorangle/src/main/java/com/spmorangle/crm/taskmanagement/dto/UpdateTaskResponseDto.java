package com.spmorangle.crm.taskmanagement.dto;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UpdateTaskResponseDto {
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
    private final OffsetDateTime updatedAt;
    private final Long updatedBy;
    private final OffsetDateTime dueDateTime;
    private final Boolean isRecurring;
    private final String recurrenceRuleStr;
    private final OffsetDateTime startDate;
    private final OffsetDateTime endDate;
}