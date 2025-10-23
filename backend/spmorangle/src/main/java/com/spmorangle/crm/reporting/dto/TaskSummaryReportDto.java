package com.spmorangle.crm.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskSummaryReportDto {
    private Long totalTasks;
    private Long completedTasks;
    private Long inProgressTasks;
    private Long todoTasks;
    private Long blockedTasks;
    
    // Percentages
    private Double completedPercentage;
    private Double inProgressPercentage;
    private Double todoPercentage;
    private Double blockedPercentage;
    
    // Additional breakdown by department/project if needed
    private Map<String, TaskStatusCounts> departmentBreakdown;
    private Map<String, TaskStatusCounts> projectBreakdown;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskStatusCounts {
        private Long total;
        private Long completed;
        private Long inProgress;
        private Long todo;
        private Long blocked;
    }
}

