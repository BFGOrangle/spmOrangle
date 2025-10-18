package com.spmorangle.crm.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffBreakdownDto {
    private Long userId;
    private String userName;
    private String department;
    private Long todoTasks;
    private Long inProgressTasks;
    private Long completedTasks;
    private Long blockedTasks;
    private BigDecimal loggedHours;
}


