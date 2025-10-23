package com.spmorangle.crm.notification.dto;

import java.util.List;

import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class DailyDigestDto {
    private final UserResponseDto user;
    private final List<TaskResponseDto> tasks;
    private final String frontendBaseUrl;
    private final long todoCount;
    private final long inProgressCount;
    private final long blockedCount;
    private final long totalPending;
}
