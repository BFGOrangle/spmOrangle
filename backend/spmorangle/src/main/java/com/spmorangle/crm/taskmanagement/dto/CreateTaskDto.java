package com.spmorangle.crm.taskmanagement.dto;

import com.spmorangle.crm.taskmanagement.enums.Status;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateTaskDto {

    @NotNull
    private final int projectId;

    @NotNull
    private final int ownerId;

    @NotNull
    private final int taskType;

    @NotNull
    private final String title;

    private final String description;

    @NotNull
    private final Status status;

    private final Long assignedToId;

    @NotNull
    private final Long createdById;
}
