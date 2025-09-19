package com.spmorangle.crm.taskmanagement.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CollaboratorAssignmentNotFoundException extends RuntimeException {

    public CollaboratorAssignmentNotFoundException(long taskId, long collaboratorId) {
        super(String.format("Collaborator %d not assigned to task %d", collaboratorId, taskId));
    }
}
