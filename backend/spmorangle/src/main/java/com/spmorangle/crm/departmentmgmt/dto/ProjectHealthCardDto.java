package com.spmorangle.crm.departmentmgmt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjectHealthCardDto {
    private final Long projectId;
    private final String projectName;
    private final String status;
    private final int completionPercentage;
    private final int totalTasks;
    private final int completedTasks;
    private final int blockedTasks;
}
