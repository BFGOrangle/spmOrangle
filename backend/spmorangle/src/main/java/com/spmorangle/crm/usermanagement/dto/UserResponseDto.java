package com.spmorangle.crm.usermanagement.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponseDto(
        Long id,
        String fullName,
        String email,
        String roleType,
        UUID cognitoSub
) {
}
