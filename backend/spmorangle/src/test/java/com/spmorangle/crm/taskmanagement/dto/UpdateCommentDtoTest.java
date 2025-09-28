package com.spmorangle.crm.taskmanagement.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UpdateCommentDto Tests")
class UpdateCommentDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should pass validation with valid data")
        void updateCommentDto_WithValidData_ShouldPassValidation() {
            // Arrange
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content("Updated comment content")
                    .mentionedUserIds(Arrays.asList(2L, 3L))
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(1L, dto.getCommentId());
            assertEquals("Updated comment content", dto.getContent());
            assertEquals(Arrays.asList(2L, 3L), dto.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should fail validation with null comment ID")
        void updateCommentDto_WithNullCommentId_ShouldFailValidation() {
            // Arrange
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(null)
                    .content("Updated content")
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("commentId") &&
                v.getMessage().contains("Comment ID is required")));
        }

        @Test
        @DisplayName("Should fail validation with blank content")
        void updateCommentDto_WithBlankContent_ShouldFailValidation() {
            // Arrange
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content("")
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("content") &&
                v.getMessage().contains("Comment content is required")));
        }

        @Test
        @DisplayName("Should fail validation with null content")
        void updateCommentDto_WithNullContent_ShouldFailValidation() {
            // Arrange
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content(null)
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("content") &&
                v.getMessage().contains("Comment content is required")));
        }

        @Test
        @DisplayName("Should fail validation with content exceeding 2000 characters")
        void updateCommentDto_WithContentExceeding2000Characters_ShouldFailValidation() {
            // Arrange
            String longContent = "a".repeat(2001);
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content(longContent)
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("content") &&
                v.getMessage().contains("Comment content cannot exceed 2000 characters")));
        }

        @Test
        @DisplayName("Should pass validation with content exactly 2000 characters")
        void updateCommentDto_WithContentExactly2000Characters_ShouldPassValidation() {
            // Arrange
            String maxContent = "a".repeat(2000);
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content(maxContent)
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(2000, dto.getContent().length());
        }

        @Test
        @DisplayName("Should fail validation with whitespace-only content")
        void updateCommentDto_WithWhitespaceOnlyContent_ShouldFailValidation() {
            // Arrange
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content("   ")
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("content")));
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should build correctly with all fields")
        void builder_WithAllFields_ShouldBuildCorrectly() {
            // Arrange & Act
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(42L)
                    .content("Complete updated content")
                    .mentionedUserIds(Arrays.asList(10L, 20L, 30L))
                    .build();

            // Assert
            assertEquals(42L, dto.getCommentId());
            assertEquals("Complete updated content", dto.getContent());
            assertEquals(Arrays.asList(10L, 20L, 30L), dto.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should build correctly with minimal required fields")
        void builder_WithMinimalRequiredFields_ShouldBuildCorrectly() {
            // Arrange & Act
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content("Minimal update")
                    .build();

            // Assert
            assertEquals(1L, dto.getCommentId());
            assertEquals("Minimal update", dto.getContent());
            assertNull(dto.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should build correctly with empty mentioned users list")
        void builder_WithEmptyMentionedUsers_ShouldBuildCorrectly() {
            // Arrange & Act
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content("Update without mentions")
                    .mentionedUserIds(Collections.emptyList())
                    .build();

            // Assert
            assertEquals(1L, dto.getCommentId());
            assertEquals("Update without mentions", dto.getContent());
            assertTrue(dto.getMentionedUserIds().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle special characters in content")
        void updateCommentDto_WithSpecialCharacters_ShouldHandleCorrectly() {
            // Arrange
            String specialContent = "Updated comment with émojis 🎯 and symbols @#$%^&*()";
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content(specialContent)
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(specialContent, dto.getContent());
        }

        @Test
        @DisplayName("Should handle zero and negative comment ID")
        void updateCommentDto_WithZeroAndNegativeCommentId_ShouldBuildCorrectly() {
            // Arrange & Act
            UpdateCommentDto zeroIdDto = UpdateCommentDto.builder()
                    .commentId(0L)
                    .content("Comment with zero ID")
                    .build();

            UpdateCommentDto negativeIdDto = UpdateCommentDto.builder()
                    .commentId(-1L)
                    .content("Comment with negative ID")
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> zeroViolations = validator.validate(zeroIdDto);
            Set<ConstraintViolation<UpdateCommentDto>> negativeViolations = validator.validate(negativeIdDto);

            // Assert - No validation constraints on ID values other than NotNull
            assertTrue(zeroViolations.isEmpty());
            assertTrue(negativeViolations.isEmpty());
            assertEquals(0L, zeroIdDto.getCommentId());
            assertEquals(-1L, negativeIdDto.getCommentId());
        }

        @Test
        @DisplayName("Should handle newlines and formatting in content")
        void updateCommentDto_WithFormattedContent_ShouldHandleCorrectly() {
            // Arrange
            String formattedContent = "Updated content:\n• Point 1\n• Point 2\n\tIndented note";
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content(formattedContent)
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(formattedContent, dto.getContent());
        }

        @Test
        @DisplayName("Should handle duplicate mentioned user IDs")
        void updateCommentDto_WithDuplicateMentionedUserIds_ShouldHandleCorrectly() {
            // Arrange
            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content("Comment with duplicate mentions")
                    .mentionedUserIds(Arrays.asList(2L, 3L, 2L, 3L, 4L))
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(Arrays.asList(2L, 3L, 2L, 3L, 4L), dto.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should handle large mentioned users list")
        void updateCommentDto_WithLargeMentionedUsersList_ShouldHandleCorrectly() {
            // Arrange
            java.util.List<Long> largeMentionsList = java.util.stream.IntStream.range(1, 51)
                    .mapToLong(i -> (long) i)
                    .boxed()
                    .collect(java.util.stream.Collectors.toList());

            UpdateCommentDto dto = UpdateCommentDto.builder()
                    .commentId(1L)
                    .content("Update mentioning many users")
                    .mentionedUserIds(largeMentionsList)
                    .build();

            // Act
            Set<ConstraintViolation<UpdateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(50, dto.getMentionedUserIds().size());
        }
    }
}