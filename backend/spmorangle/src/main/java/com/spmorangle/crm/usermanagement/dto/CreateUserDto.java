package com.spmorangle.crm.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDto(
        @NotBlank
        String userName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String password,

        @NotBlank
        String roleType
) {
}
