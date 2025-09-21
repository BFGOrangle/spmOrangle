package com.spmorangle.crm.taskmanagement.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CollaboratorAlreadyExistsException extends RuntimeException {

    public CollaboratorAlreadyExistsException(long taskId, long collaboratorId) {
        super(String.format("Collaborator %d already assigned to task %d", collaboratorId, taskId));
    }
}
