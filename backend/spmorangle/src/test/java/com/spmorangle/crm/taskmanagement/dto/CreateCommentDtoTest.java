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

@DisplayName("CreateCommentDto Tests")
class CreateCommentDtoTest {

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
        @DisplayName("Should pass validation with valid task comment data")
        void createCommentDto_WithValidTaskCommentData_ShouldPassValidation() {
            // Arrange
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content("This is a valid comment")
                    .mentionedUserIds(Arrays.asList(2L, 3L))
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(1L, dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertNull(dto.getParentCommentId());
            assertEquals("This is a valid comment", dto.getContent());
            assertEquals(Arrays.asList(2L, 3L), dto.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should pass validation with valid subtask comment data")
        void createCommentDto_WithValidSubtaskCommentData_ShouldPassValidation() {
            // Arrange
            CreateCommentDto dto = CreateCommentDto.builder()
                    .subtaskId(10L)
                    .content("This is a subtask comment")
                    .mentionedUserIds(Collections.singletonList(5L))
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertNull(dto.getTaskId());
            assertEquals(10L, dto.getSubtaskId());
            assertEquals("This is a subtask comment", dto.getContent());
        }

        @Test
        @DisplayName("Should pass validation with valid reply comment data")
        void createCommentDto_WithValidReplyCommentData_ShouldPassValidation() {
            // Arrange
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .parentCommentId(5L)
                    .content("This is a reply to another comment")
                    .mentionedUserIds(Collections.emptyList())
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(1L, dto.getTaskId());
            assertEquals(5L, dto.getParentCommentId());
            assertEquals("This is a reply to another comment", dto.getContent());
        }

        @Test
        @DisplayName("Should fail validation with blank content")
        void createCommentDto_WithBlankContent_ShouldFailValidation() {
            // Arrange
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content("")
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("content") &&
                v.getMessage().contains("Comment content is required")));
        }

        @Test
        @DisplayName("Should fail validation with null content")
        void createCommentDto_WithNullContent_ShouldFailValidation() {
            // Arrange
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content(null)
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("content") &&
                v.getMessage().contains("Comment content is required")));
        }

        @Test
        @DisplayName("Should fail validation with content exceeding 2000 characters")
        void createCommentDto_WithContentExceeding2000Characters_ShouldFailValidation() {
            // Arrange
            String longContent = "a".repeat(2001);
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content(longContent)
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertFalse(violations.isEmpty());
            assertTrue(violations.stream().anyMatch(v ->
                v.getPropertyPath().toString().equals("content") &&
                v.getMessage().contains("Comment content cannot exceed 2000 characters")));
        }

        @Test
        @DisplayName("Should pass validation with content exactly 2000 characters")
        void createCommentDto_WithContentExactly2000Characters_ShouldPassValidation() {
            // Arrange
            String maxContent = "a".repeat(2000);
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content(maxContent)
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(2000, dto.getContent().length());
        }

        @Test
        @DisplayName("Should pass validation with whitespace-only content that gets trimmed")
        void createCommentDto_WithWhitespaceContent_ShouldHandleCorrectly() {
            // Arrange
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content("   ")
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert - This should fail because whitespace-only is considered blank
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
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .subtaskId(2L)
                    .parentCommentId(3L)
                    .content("Test comment content")
                    .mentionedUserIds(Arrays.asList(4L, 5L, 6L))
                    .build();

            // Assert
            assertEquals(1L, dto.getTaskId());
            assertEquals(2L, dto.getSubtaskId());
            assertEquals(3L, dto.getParentCommentId());
            assertEquals("Test comment content", dto.getContent());
            assertEquals(Arrays.asList(4L, 5L, 6L), dto.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should build correctly with minimal required fields")
        void builder_WithMinimalFields_ShouldBuildCorrectly() {
            // Arrange & Act
            CreateCommentDto dto = CreateCommentDto.builder()
                    .content("Minimal comment")
                    .build();

            // Assert
            assertNull(dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertNull(dto.getParentCommentId());
            assertEquals("Minimal comment", dto.getContent());
            assertNull(dto.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should build correctly with empty mentioned users list")
        void builder_WithEmptyMentionedUsers_ShouldBuildCorrectly() {
            // Arrange & Act
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content("Comment without mentions")
                    .mentionedUserIds(Collections.emptyList())
                    .build();

            // Assert
            assertEquals(1L, dto.getTaskId());
            assertEquals("Comment without mentions", dto.getContent());
            assertTrue(dto.getMentionedUserIds().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle special characters in content")
        void createCommentDto_WithSpecialCharacters_ShouldHandleCorrectly() {
            // Arrange
            String specialContent = "Comment with émojis 🚀 and symbols @#$%^&*()";
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content(specialContent)
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(specialContent, dto.getContent());
        }

        @Test
        @DisplayName("Should handle newlines and formatting in content")
        void createCommentDto_WithFormattedContent_ShouldHandleCorrectly() {
            // Arrange
            String formattedContent = "Line 1\nLine 2\n\tIndented line\n• Bullet point";
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content(formattedContent)
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(formattedContent, dto.getContent());
        }

        @Test
        @DisplayName("Should handle zero and negative IDs")
        void createCommentDto_WithZeroAndNegativeIds_ShouldBuildCorrectly() {
            // Arrange & Act
            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(0L)
                    .subtaskId(-1L)
                    .parentCommentId(-5L)
                    .content("Comment with edge case IDs")
                    .mentionedUserIds(Arrays.asList(0L, -1L))
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert - No validation constraints on ID values
            assertTrue(violations.isEmpty());
            assertEquals(0L, dto.getTaskId());
            assertEquals(-1L, dto.getSubtaskId());
            assertEquals(-5L, dto.getParentCommentId());
            assertEquals(Arrays.asList(0L, -1L), dto.getMentionedUserIds());
        }

        @Test
        @DisplayName("Should handle large mentioned users list")
        void createCommentDto_WithLargeMentionedUsersList_ShouldHandleCorrectly() {
            // Arrange
            java.util.List<Long> largeMentionsList = java.util.stream.IntStream.range(1, 101)
                    .mapToLong(i -> (long) i)
                    .boxed()
                    .collect(java.util.stream.Collectors.toList());

            CreateCommentDto dto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content("Comment mentioning many users")
                    .mentionedUserIds(largeMentionsList)
                    .build();

            // Act
            Set<ConstraintViolation<CreateCommentDto>> violations = validator.validate(dto);

            // Assert
            assertTrue(violations.isEmpty());
            assertEquals(100, dto.getMentionedUserIds().size());
        }
    }
}