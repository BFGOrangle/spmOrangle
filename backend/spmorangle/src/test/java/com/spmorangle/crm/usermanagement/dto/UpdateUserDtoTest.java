package com.spmorangle.crm.usermanagement.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserRoleDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void updateUserRoleDto_WithValidFields_ShouldPassValidation() {
        // Arrange
        UpdateUserRoleDto dto = new UpdateUserRoleDto(
                1L,
                "john.doe@example.com",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<UpdateUserRoleDto>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals(1L, dto.userId());
        assertEquals("john.doe@example.com", dto.email());
        assertEquals("DIRECTOR", dto.roleType());
    }

    @Test
    void updateUserRoleDto_WithNullUserId_ShouldFailValidation() {
        // Arrange
        UpdateUserRoleDto dto = new UpdateUserRoleDto(
                null,
                "john.doe@example.com",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<UpdateUserRoleDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userId")));
    }

    @Test
    void updateUserRoleDto_WithInvalidEmail_ShouldFailValidation() {
        // Arrange
        UpdateUserRoleDto dto = new UpdateUserRoleDto(
                1L,
                "invalid-email",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<UpdateUserRoleDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void updateUserRoleDto_WithBlankEmail_ShouldFailValidation() {
        // Arrange
        UpdateUserRoleDto dto = new UpdateUserRoleDto(
                1L,
                "",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<UpdateUserRoleDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void updateUserRoleDto_WithBlankRoleType_ShouldFailValidation() {
        // Arrange
        UpdateUserRoleDto dto = new UpdateUserRoleDto(
                1L,
                "john.doe@example.com",
                ""
        );

        // Act
        Set<ConstraintViolation<UpdateUserRoleDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("roleType")));
    }

    @Test
    void updateUserRoleDto_WithSpecialCharacters_ShouldPassValidation() {
        // Arrange
        UpdateUserRoleDto dto = new UpdateUserRoleDto(
                2L,
                "josé.maría@example-domain.com",
                "HR"
        );

        // Act
        Set<ConstraintViolation<UpdateUserRoleDto>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }
}
