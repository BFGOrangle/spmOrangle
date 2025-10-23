package com.spmorangle.crm.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeAnalyticsReportDto {
    private BigDecimal totalHours;
    private Map<String, BigDecimal> hoursByDepartment;
    private Map<String, BigDecimal> hoursByProject;
    private Map<String, ProjectTimeDetails> projectDetails;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectTimeDetails {
        private String projectName;
        private String department;
        private BigDecimal totalHours;
        private Long completedTasks;
        private Long inProgressTasks;
        private BigDecimal averageHoursPerTask;
    }
}

