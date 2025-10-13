package com.spmorangle.crm.taskmanagement.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(long taskId) {
        super(String.format("Task with ID %d not found", taskId));
    }
}
