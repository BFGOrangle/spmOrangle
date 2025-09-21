package com.spmorangle.crm.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating an existing staff member
 */
public record UpdateUserDto(
    @NotNull(message = "id must not be blank")
    Long id,

    @NotBlank(message = "fullName must not be blank")
    String fullName,

    @NotBlank(message = "email must not be blank")
    @Email
    String email,

    @NotBlank(message = "roleType must not be blank")
    String roleType
) {}
