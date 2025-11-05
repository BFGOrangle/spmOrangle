package com.spmorangle.crm.departmentmgmt.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamLoadEntryDto {
    private final Long userId;
    private final String fullName;
    private final String department;
    private final int taskCount;
    private final int blockedTaskCount;
}
