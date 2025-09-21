package com.spmorangle.common.model;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
class UserTest {
    @Test
    void user_SettersAndGetters_ShouldWorkCorrectly() {
        // Arrange
        User user = new User();
        Long id = 1L;
        String userName = "John Doe";
        String email = "john.doe@example.com";
        String roleType = "DIRECTOR";
        UUID cognitoSub = UUID.randomUUID();
        OffsetDateTime now = OffsetDateTime.now();

        // Act
        user.setId(id);
        user.setUserName(userName);
        user.setEmail(email);
        user.setRoleType(roleType);
        user.setCognitoSub(cognitoSub);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Assert
        assertEquals(id, user.getId());
        assertEquals(userName, user.getUserName());
        assertEquals(email, user.getEmail());
        assertEquals(roleType, user.getRoleType());
        assertEquals(cognitoSub, user.getCognitoSub());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());
    }

    @Test
    void user_DefaultConstructor_ShouldCreateEmptyUser() {
        // Act
        User user = new User();

        // Assert
        assertNull(user.getId());
        assertNull(user.getUserName());
        assertNull(user.getEmail());
        assertNull(user.getRoleType());
        assertNull(user.getCognitoSub());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void user_WithNullValues_ShouldHandleGracefully() {
        // Arrange
        User user = new User();

        // Act & Assert
        assertDoesNotThrow(() -> {
            user.setId(null);
            user.setUserName(null);
            user.setEmail(null);
            user.setRoleType(null);
            user.setCognitoSub(null);
            user.setCreatedAt(null);
            user.setUpdatedAt(null);
        });

        assertNull(user.getId());
        assertNull(user.getUserName());
        assertNull(user.getEmail());
        assertNull(user.getRoleType());
        assertNull(user.getCognitoSub());
        assertNull(user.getCreatedAt());
        assertNull(user.getUpdatedAt());
    }

    @Test
    void user_WithSpecialCharactersInFields_ShouldHandleCorrectly() {
        // Arrange
        User user = new User();
        String specialName = "José María O'Connor-Smith";
        String specialEmail = "josé.maría@example-domain.com";

        // Act
        user.setUserName(specialName);
        user.setEmail(specialEmail);

        // Assert
        assertEquals(specialName, user.getUserName());
        assertEquals(specialEmail, user.getEmail());
    }
}

