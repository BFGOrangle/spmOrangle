package com.spmorangle.crm.usermanagement.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserResponseDtoTest {

    @Test
    void userResponseDto_WithValidFields_ShouldCreateCorrectly() {
        // Arrange
        Long id = 1L;
        String username = "John Doe";
        String email = "john.doe@example.com";
        String roleType = "DIRECTOR";
        UUID cognitoSub = UUID.randomUUID();

        // Act
        UserResponseDto dto = UserResponseDto.builder()
                .id(id)
                .username(username)
                .email(email)
                .roleType(roleType)
                .cognitoSub(cognitoSub)
                .build();

        // Assert
        assertEquals(id, dto.id());
        assertEquals(username, dto.username());
        assertEquals(email, dto.email());
        assertEquals(roleType, dto.roleType());
        assertEquals(cognitoSub, dto.cognitoSub());
    }

    @Test
    void userResponseDto_WithNullFields_ShouldHandleGracefully() {
        // Act
        UserResponseDto dto = UserResponseDto.builder()
                .id(null)
                .username(null)
                .email(null)
                .roleType(null)
                .cognitoSub(null)
                .build();

        // Assert
        assertNull(dto.id());
        assertNull(dto.username());
        assertNull(dto.email());
        assertNull(dto.roleType());
        assertNull(dto.cognitoSub());
    }

    @Test
    void userResponseDto_Builder_ShouldWorkCorrectly() {
        // Arrange
        UUID cognitoSub = UUID.randomUUID();

        // Act
        UserResponseDto dto = UserResponseDto.builder()
                .id(2L)
                .username("Jane Smith")
                .email("jane.smith@example.com")
                .roleType("MANAGER")
                .cognitoSub(cognitoSub)
                .build();

        // Assert
        assertNotNull(dto);
        assertEquals(2L, dto.id());
        assertEquals("Jane Smith", dto.username());
        assertEquals("jane.smith@example.com", dto.email());
        assertEquals("MANAGER", dto.roleType());
        assertEquals(cognitoSub, dto.cognitoSub());
    }

    @Test
    void userResponseDto_ToString_ShouldNotExposePasswordInfo() {
        // Arrange
        UserResponseDto dto = UserResponseDto.builder()
                .id(1L)
                .username("Test User")
                .email("test@example.com")
                .roleType("STAFF")
                .cognitoSub(UUID.randomUUID())
                .build();

        // Act
        String toString = dto.toString();

        // Assert
        assertNotNull(toString);
        assertFalse(toString.toLowerCase().contains("password"));
    }
}
