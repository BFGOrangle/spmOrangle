package com.spmorangle.crm.taskmanagement.controller;

import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAlreadyExistsException;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAssignmentNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Void> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(CollaboratorAlreadyExistsException.class)
    public ResponseEntity<Void> handleCollaboratorAlreadyExistsException(CollaboratorAlreadyExistsException ex) {
        log.warn("Collaborator already exists: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(CollaboratorAssignmentNotFoundException.class)
    public ResponseEntity<Void> handleCollaboratorAssignmentNotFoundException(CollaboratorAssignmentNotFoundException ex) {
        log.warn("Collaborator assignment not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument provided: {}", ex.getMessage());
        return ResponseEntity.badRequest().build(); // 400 Bad Request
    }
}