package com.spmorangle.crm.projectmanagement.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProjectResponseDto {
    private final Long id;
    private final String name;
    private final String description;
    private final Long ownerId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
    private final Integer taskCount;
    private final Integer completedTaskCount;
}
