package com.spmorangle.crm.fileupload.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long taskId) {
        super(String.format("Task with ID %d not found", taskId));
    }
}
