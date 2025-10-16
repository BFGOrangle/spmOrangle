package com.spmorangle.crm.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilterDto {
    private String department;
    private List<Long> projectIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private TimeRange timeRange;
    private ExportFormat exportFormat;
    
    public enum TimeRange {
        WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
    }
    
    public enum ExportFormat {
        PDF, CSV, JSON
    }
}

