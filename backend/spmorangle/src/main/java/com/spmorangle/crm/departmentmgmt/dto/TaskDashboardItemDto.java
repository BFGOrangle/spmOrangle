package com.spmorangle.crm.departmentmgmt.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class TaskDashboardItemDto {
    private final Long id;
    private final String title;
    private final String status;
    private final String taskType;
    private final Integer priority;
    private final Long ownerId;
    private final String ownerName;
    private final String ownerDepartment;
    private final Long projectId;
    private final String projectName;
    private final OffsetDateTime dueDateTime;
    private final OffsetDateTime updatedAt;
    private final OffsetDateTime createdAt;
    private final List<Long> assigneeIds;
}
