package com.spmorangle.crm.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Period {
    private String period;        // e.g., "2025-W01" or "2025-01"
    private String periodLabel;   // e.g., "Week 1 (Jan 1-7)" or "January 2025"
    private LocalDate startDate;
    private LocalDate endDate;
}

