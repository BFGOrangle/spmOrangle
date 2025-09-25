package com.spmorangle.crm.taskmanagement.dto;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CreateTaskDto {

    private final Long projectId;

    @NotNull(message = "Owner ID is required")
    private final Long ownerId;

    @NotNull(message = "Title is required")
    private final String title;

    private final String description;

    @Builder.Default
    private final Status status = Status.TODO;

    @NotNull(message = "Task Type is required")
    private final TaskType taskType;


    private final List<String> tags;

    private final List<Long> assignedUserIds;

}
