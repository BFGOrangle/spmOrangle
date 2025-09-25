package com.spmorangle.crm.taskmanagement.dto;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CreateSubtaskDto {

    @NotNull(message = "Task ID is required")
    private final Long taskId;

    @NotNull(message = "Project ID is required")
    private final Long projectId;

    @NotNull(message = "Title is required")
    private final String title;

    private final String details;

    @Builder.Default
    private final Status status = Status.TODO;

    @NotNull(message = "Task Type is required")
    private final TaskType taskType;
}
