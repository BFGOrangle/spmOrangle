package com.spmorangle.crm.taskmanagement.dto;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateSubtaskDto {
    private final String title;
    private final String details;
    private final Status status;
    private final TaskType taskType;
}
