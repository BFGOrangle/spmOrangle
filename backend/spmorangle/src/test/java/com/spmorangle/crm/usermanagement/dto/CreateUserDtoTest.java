package com.spmorangle.crm.usermanagement.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CreateUserDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    void createUserDto_WithValidFields_ShouldPassValidation() {
        // Arrange
        CreateUserDto dto = new CreateUserDto(
                "John Doe",
                "john.doe@example.com",
                "Password123!",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
        assertEquals("John Doe", dto.userName());
        assertEquals("john.doe@example.com", dto.email());
        assertEquals("Password123!", dto.password());
        assertEquals("DIRECTOR", dto.roleType());
    }

    @Test
    void createUserDto_WithBlankUserName_ShouldFailValidation() {
        // Arrange
        CreateUserDto dto = new CreateUserDto(
                "",
                "john.doe@example.com",
                "Password123!",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userName")));
    }

    @Test
    void createUserDto_WithNullUserName_ShouldFailValidation() {
        // Arrange
        CreateUserDto dto = new CreateUserDto(
                null,
                "john.doe@example.com",
                "Password123!",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("userName")));
    }

    @Test
    void createUserDto_WithInvalidEmail_ShouldFailValidation() {
        // Arrange
        CreateUserDto dto = new CreateUserDto(
                "John Doe",
                "invalid-email",
                "Password123!",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void createUserDto_WithBlankEmail_ShouldFailValidation() {
        // Arrange
        CreateUserDto dto = new CreateUserDto(
                "John Doe",
                "",
                "Password123!",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void createUserDto_WithBlankPassword_ShouldFailValidation() {
        // Arrange
        CreateUserDto dto = new CreateUserDto(
                "John Doe",
                "john.doe@example.com",
                "",
                "DIRECTOR"
        );

        // Act
        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void createUserDto_WithBlankRoleType_ShouldFailValidation() {
        // Arrange
        CreateUserDto dto = new CreateUserDto(
                "John Doe",
                "john.doe@example.com",
                "Password123!",
                ""
        );

        // Act
        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(dto);

        // Assert
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("roleType")));
    }

    @Test
    void createUserDto_WithSpecialCharacters_ShouldPassValidation() {
        // Arrange
        CreateUserDto dto = new CreateUserDto(
                "José María O'Connor-Smith",
                "josé.maría@example-domain.com",
                "P@ssw0rd!123",
                "HR"
        );

        // Act
        Set<ConstraintViolation<CreateUserDto>> violations = validator.validate(dto);

        // Assert
        assertTrue(violations.isEmpty());
    }
}
