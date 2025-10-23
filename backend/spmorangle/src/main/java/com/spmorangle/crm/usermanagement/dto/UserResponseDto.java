package com.spmorangle.crm.usermanagement.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponseDto(
        Long id,
        String username,
        String email,
        String roleType,
        Boolean isActive,
        String department,
        UUID cognitoSub
) {
}
