package com.spmorangle.crm.taskmanagement.dto;

import com.spmorangle.crm.taskmanagement.enums.RecurrenceEditMode;
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
    // Changed to String to support sentinel values:
    // - null: don't update the due date (preserve existing)
    // - "": empty string to clear the due date
    // - ISO 8601 string: parse and set as new due date
    private final String dueDateTime;
    private final Boolean isRecurring;
    private final String recurrenceRuleStr;
    private final OffsetDateTime startDate;
    private final OffsetDateTime endDate;
    private final RecurrenceEditMode recurrenceEditMode;
    private final OffsetDateTime instanceDate;
    private final Integer priority;
}