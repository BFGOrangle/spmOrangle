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
public class CreateTaskResponseDto {
    private final Long id;
    private final Long projectId;
    private final Long ownerId;
    private final String title;
    private final String description;
    private final Status status;
    private final TaskType taskType;
    private final List<String> tags;
    private final boolean userHasEditAccess;
    private final boolean userHasDeleteAccess;
    private final Long createdBy;
    private final OffsetDateTime createdAt;
    private final List<Long> assignedUserIds;
}