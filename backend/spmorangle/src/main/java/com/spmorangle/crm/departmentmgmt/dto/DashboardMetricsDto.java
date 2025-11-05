package com.spmorangle.crm.departmentmgmt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardMetricsDto {
    private final int activeProjects;
    private final int totalTasks;
    private final int completedTasks;
    private final int blockedTasks;
    private final int highPriorityTasks;
    private final double completionRate;
}
