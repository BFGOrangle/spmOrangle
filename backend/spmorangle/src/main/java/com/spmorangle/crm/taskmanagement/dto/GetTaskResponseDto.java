package com.spmorangle.crm.taskmanagement.dto;

import com.spmorangle.crm.taskmanagement.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetTaskResponseDto {
    private final Long id;
    private final Long projectId;
    private final Long ownerId;
    private final String title;
    private final String description;
    private final Status status;
    private final List<String> tags;
    private final Long createdBy;
    private final OffsetDateTime createdAt;
    private final List<Long> assignedUserIds;
}
