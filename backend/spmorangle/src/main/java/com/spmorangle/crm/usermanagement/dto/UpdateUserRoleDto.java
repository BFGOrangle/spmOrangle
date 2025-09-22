package com.spmorangle.crm.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for updating an existing staff member
 */
public record UpdateUserRoleDto(
    @NotNull(message = "userId must not be blank")
    Long userId,

    @Email
    @NotBlank(message = "email must not be blank")
    String email,

    @NotBlank(message = "roleType must not be blank")
    String roleType
) {}
