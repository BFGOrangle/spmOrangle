package com.spmorangle.crm.taskmanagement.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AddCollaboratorResponseDto {

    private final long taskId;

    private final long collaboratorId;

    private final long assignedById;

    private final OffsetDateTime assignedAt;
}
