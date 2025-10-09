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
public class UpdateTaskDto {

    @NotNull(message = "Task ID is required")
    private final Long taskId;

    private final String title;
    private final String description;
    private final Status status;
    private final TaskType taskType;
    private final List<String> tags;
    private final OffsetDateTime dueDateTime;
}