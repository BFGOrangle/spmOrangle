package com.spmorangle.crm.reporting.dto;

import jakarta.validation.constraints.NotNull;
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
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
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

