package com.spmorangle.common.converter;

import com.spmorangle.common.model.User;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class UserConverterTest {
    @Test
    void convert_WithValidUser_ShouldReturnUserResponseDto() {
        // Arrange
        UUID cognitoSub = UUID.randomUUID();
        User user = new User();
        user.setId(1L);
        user.setUserName("John Doe");
        user.setEmail("john.doe@example.com");
        user.setRoleType("DIRECTOR");
        user.setCognitoSub(cognitoSub);
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());

        // Act
        UserResponseDto result = UserConverter.convert(user);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("John Doe", result.username());
        assertEquals("john.doe@example.com", result.email());
        assertEquals("DIRECTOR", result.roleType());
        assertEquals(cognitoSub, result.cognitoSub());
    }

    @Test
    void convert_WithNullUser_ShouldReturnNull() {
        // Act
        UserResponseDto result = UserConverter.convert(null);

        // Assert
        assertNull(result);
    }

    @Test
    void convert_WithUserHavingNullFields_ShouldHandleGracefully() {
        // Arrange
        User user = new User();
        user.setId(2L);
        // Other fields are null

        // Act
        UserResponseDto result = UserConverter.convert(user);

        // Assert
        assertNotNull(result);
        assertEquals(2L, result.id());
        assertNull(result.username());
        assertNull(result.email());
        assertNull(result.roleType());
        assertNull(result.cognitoSub());
    }
}

