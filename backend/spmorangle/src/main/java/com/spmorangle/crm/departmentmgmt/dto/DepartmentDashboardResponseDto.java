package com.spmorangle.crm.departmentmgmt.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DepartmentDashboardResponseDto {
    private final String department;
    private final List<String> includedDepartments;
    private final DashboardMetricsDto metrics;
    private final List<ProjectHealthCardDto> projects;
    private final List<TaskDashboardItemDto> upcomingCommitments;
    private final List<TaskDashboardItemDto> priorityQueue;
    private final List<TeamLoadEntryDto> teamLoad;
}
