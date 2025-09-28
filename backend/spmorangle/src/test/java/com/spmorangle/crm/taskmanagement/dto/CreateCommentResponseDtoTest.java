package com.spmorangle.crm.taskmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CreateCommentResponseDto Tests")
class CreateCommentResponseDtoTest {

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should build correctly with all fields")
        void builder_WithAllFields_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(1L)
                    .taskId(100L)
                    .subtaskId(200L)
                    .parentCommentId(5L)
                    .content("Test comment content")
                    .authorId(10L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(1L, dto.getId());
            assertEquals(100L, dto.getTaskId());
            assertEquals(200L, dto.getSubtaskId());
            assertEquals(5L, dto.getParentCommentId());
            assertEquals("Test comment content", dto.getContent());
            assertEquals(10L, dto.getAuthorId());
            assertEquals(now, dto.getCreatedAt());
        }

        @Test
        @DisplayName("Should build correctly with minimal required fields")
        void builder_WithMinimalFields_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(1L)
                    .content("Minimal comment")
                    .authorId(10L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(1L, dto.getId());
            assertNull(dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertNull(dto.getParentCommentId());
            assertEquals("Minimal comment", dto.getContent());
            assertEquals(10L, dto.getAuthorId());
            assertEquals(now, dto.getCreatedAt());
        }

        @Test
        @DisplayName("Should build task comment response correctly")
        void builder_ForTaskComment_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(1L)
                    .taskId(100L)
                    .content("New task comment")
                    .authorId(10L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(1L, dto.getId());
            assertEquals(100L, dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertNull(dto.getParentCommentId());
            assertEquals("New task comment", dto.getContent());
            assertEquals(10L, dto.getAuthorId());
            assertEquals(now, dto.getCreatedAt());
        }

        @Test
        @DisplayName("Should build subtask comment response correctly")
        void builder_ForSubtaskComment_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(2L)
                    .subtaskId(200L)
                    .content("New subtask comment")
                    .authorId(20L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(2L, dto.getId());
            assertNull(dto.getTaskId());
            assertEquals(200L, dto.getSubtaskId());
            assertNull(dto.getParentCommentId());
            assertEquals("New subtask comment", dto.getContent());
            assertEquals(20L, dto.getAuthorId());
            assertEquals(now, dto.getCreatedAt());
        }

        @Test
        @DisplayName("Should build reply comment response correctly")
        void builder_ForReplyComment_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(3L)
                    .taskId(100L)
                    .parentCommentId(1L)
                    .content("Reply to comment")
                    .authorId(30L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(3L, dto.getId());
            assertEquals(100L, dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertEquals(1L, dto.getParentCommentId());
            assertEquals("Reply to comment", dto.getContent());
            assertEquals(30L, dto.getAuthorId());
            assertEquals(now, dto.getCreatedAt());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle zero and negative IDs")
        void builder_WithZeroAndNegativeIds_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(0L)
                    .taskId(-1L)
                    .subtaskId(-2L)
                    .parentCommentId(-3L)
                    .content("Comment with edge case IDs")
                    .authorId(-4L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(0L, dto.getId());
            assertEquals(-1L, dto.getTaskId());
            assertEquals(-2L, dto.getSubtaskId());
            assertEquals(-3L, dto.getParentCommentId());
            assertEquals("Comment with edge case IDs", dto.getContent());
            assertEquals(-4L, dto.getAuthorId());
            assertEquals(now, dto.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle special characters in content")
        void builder_WithSpecialCharacters_ShouldHandleCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();
            String specialContent = "Comment with émojis 🎉, symbols @#$%^&*(), and Unicode 中文测试";

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(1L)
                    .content(specialContent)
                    .authorId(10L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(specialContent, dto.getContent());
        }

        @Test
        @DisplayName("Should handle very long content")
        void builder_WithVeryLongContent_ShouldHandleCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();
            String longContent = "a".repeat(1999); // Just under the 2000 character limit

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(1L)
                    .content(longContent)
                    .authorId(10L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(longContent, dto.getContent());
            assertEquals(1999, dto.getContent().length());
        }

        @Test
        @DisplayName("Should handle empty content")
        void builder_WithEmptyContent_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(1L)
                    .content("")
                    .authorId(10L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals("", dto.getContent());
        }

        @Test
        @DisplayName("Should handle null values")
        void builder_WithNullValues_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(null)
                    .taskId(null)
                    .subtaskId(null)
                    .parentCommentId(null)
                    .content(null)
                    .authorId(null)
                    .createdAt(now)
                    .build();

            // Assert
            assertNull(dto.getId());
            assertNull(dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertNull(dto.getParentCommentId());
            assertNull(dto.getContent());
            assertNull(dto.getAuthorId());
            assertEquals(now, dto.getCreatedAt());
        }

        @Test
        @DisplayName("Should handle multiline content")
        void builder_WithMultilineContent_ShouldHandleCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();
            String multilineContent = "Line 1\nLine 2\n\tIndented line\n• Bullet point\n\nParagraph after blank line";

            // Act
            CreateCommentResponseDto dto = CreateCommentResponseDto.builder()
                    .id(1L)
                    .content(multilineContent)
                    .authorId(10L)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(multilineContent, dto.getContent());
            assertTrue(dto.getContent().contains("\n"));
            assertTrue(dto.getContent().contains("\t"));
        }

        @Test
        @DisplayName("Should handle past and future timestamps")
        void builder_WithDifferentTimestamps_ShouldHandleCorrectly() {
            // Arrange
            OffsetDateTime pastTime = OffsetDateTime.now().minusDays(30);
            OffsetDateTime futureTime = OffsetDateTime.now().plusDays(30);

            // Act
            CreateCommentResponseDto pastDto = CreateCommentResponseDto.builder()
                    .id(1L)
                    .content("Comment from the past")
                    .authorId(10L)
                    .createdAt(pastTime)
                    .build();

            CreateCommentResponseDto futureDto = CreateCommentResponseDto.builder()
                    .id(2L)
                    .content("Comment from the future")
                    .authorId(20L)
                    .createdAt(futureTime)
                    .build();

            // Assert
            assertEquals(pastTime, pastDto.getCreatedAt());
            assertEquals(futureTime, futureDto.getCreatedAt());
            assertTrue(pastTime.isBefore(futureTime));
        }
    }
}