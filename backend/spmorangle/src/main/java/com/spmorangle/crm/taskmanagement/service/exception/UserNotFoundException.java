package com.spmorangle.crm.taskmanagement.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(long userId) {
        super(String.format("User with ID %d not found", userId));
    }

    public UserNotFoundException(long userId, String role) {
        super(String.format("User with ID %d not found (role: %s)", userId, role));
    }
}
