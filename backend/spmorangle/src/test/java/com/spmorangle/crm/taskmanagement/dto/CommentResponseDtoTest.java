package com.spmorangle.crm.taskmanagement.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommentResponseDto Tests")
class CommentResponseDtoTest {

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should build correctly with all fields")
        void builder_WithAllFields_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime updated = now.plusMinutes(5);

            List<CommentResponseDto> replies = Arrays.asList(
                CommentResponseDto.builder()
                    .id(10L)
                    .content("Reply 1")
                    .authorId(3L)
                    .authorUsername("user3")
                    .createdAt(now.plusMinutes(1))
                    .build(),
                CommentResponseDto.builder()
                    .id(11L)
                    .content("Reply 2")
                    .authorId(4L)
                    .authorUsername("user4")
                    .createdAt(now.plusMinutes(2))
                    .build()
            );

            // Act
            CommentResponseDto dto = CommentResponseDto.builder()
                    .id(1L)
                    .taskId(100L)
                    .subtaskId(200L)
                    .projectId(300L)
                    .parentCommentId(5L)
                    .content("This is a test comment")
                    .mentionedUserIds(Arrays.asList(2L, 3L, 4L))
                    .isEdited(true)
                    .isDeleted(false)
                    .authorId(1L)
                    .authorUsername("testuser")
                    .createdAt(now)
                    .updatedAt(updated)
                    .replies(replies)
                    .replyCount(2)
                    .canEdit(true)
                    .canDelete(true)
                    .canReply(true)
                    .canModerate(false)
                    .build();

            // Assert
            assertEquals(1L, dto.getId());
            assertEquals(100L, dto.getTaskId());
            assertEquals(200L, dto.getSubtaskId());
            assertEquals(300L, dto.getProjectId());
            assertEquals(5L, dto.getParentCommentId());
            assertEquals("This is a test comment", dto.getContent());
            assertEquals(Arrays.asList(2L, 3L, 4L), dto.getMentionedUserIds());
            assertTrue(dto.isEdited());
            assertFalse(dto.isDeleted());
            assertEquals(1L, dto.getAuthorId());
            assertEquals("testuser", dto.getAuthorUsername());
            assertEquals(now, dto.getCreatedAt());
            assertEquals(updated, dto.getUpdatedAt());
            assertEquals(replies, dto.getReplies());
            assertEquals(2, dto.getReplyCount());
            assertTrue(dto.isCanEdit());
            assertTrue(dto.isCanDelete());
            assertTrue(dto.isCanReply());
            assertFalse(dto.isCanModerate());
        }

        @Test
        @DisplayName("Should build correctly with minimal fields")
        void builder_WithMinimalFields_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CommentResponseDto dto = CommentResponseDto.builder()
                    .id(1L)
                    .content("Minimal comment")
                    .authorId(1L)
                    .authorUsername("user")
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(1L, dto.getId());
            assertNull(dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertNull(dto.getProjectId());
            assertNull(dto.getParentCommentId());
            assertEquals("Minimal comment", dto.getContent());
            assertNull(dto.getMentionedUserIds());
            assertFalse(dto.isEdited()); // default false
            assertFalse(dto.isDeleted()); // default false
            assertEquals(1L, dto.getAuthorId());
            assertEquals("user", dto.getAuthorUsername());
            assertEquals(now, dto.getCreatedAt());
            assertNull(dto.getUpdatedAt());
            assertNull(dto.getReplies());
            assertEquals(0, dto.getReplyCount()); // default 0
            assertFalse(dto.isCanEdit()); // default false
            assertFalse(dto.isCanDelete()); // default false
            assertFalse(dto.isCanReply()); // default false
            assertFalse(dto.isCanModerate()); // default false
        }

        @Test
        @DisplayName("Should build task comment correctly")
        void builder_ForTaskComment_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CommentResponseDto dto = CommentResponseDto.builder()
                    .id(1L)
                    .taskId(100L)
                    .projectId(300L)
                    .content("Task comment")
                    .mentionedUserIds(Arrays.asList(2L, 3L))
                    .authorId(1L)
                    .authorUsername("taskuser")
                    .createdAt(now)
                    .canEdit(true)
                    .canReply(true)
                    .build();

            // Assert
            assertEquals(1L, dto.getId());
            assertEquals(100L, dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertEquals(300L, dto.getProjectId());
            assertNull(dto.getParentCommentId());
            assertEquals("Task comment", dto.getContent());
            assertEquals(Arrays.asList(2L, 3L), dto.getMentionedUserIds());
            assertEquals(1L, dto.getAuthorId());
            assertEquals("taskuser", dto.getAuthorUsername());
            assertTrue(dto.isCanEdit());
            assertTrue(dto.isCanReply());
            assertFalse(dto.isCanDelete());
            assertFalse(dto.isCanModerate());
        }

        @Test
        @DisplayName("Should build subtask comment correctly")
        void builder_ForSubtaskComment_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CommentResponseDto dto = CommentResponseDto.builder()
                    .id(2L)
                    .subtaskId(200L)
                    .projectId(300L)
                    .content("Subtask comment")
                    .mentionedUserIds(Collections.singletonList(4L))
                    .authorId(2L)
                    .authorUsername("subtaskuser")
                    .createdAt(now)
                    .canDelete(true)
                    .canModerate(true)
                    .build();

            // Assert
            assertEquals(2L, dto.getId());
            assertNull(dto.getTaskId());
            assertEquals(200L, dto.getSubtaskId());
            assertEquals(300L, dto.getProjectId());
            assertNull(dto.getParentCommentId());
            assertEquals("Subtask comment", dto.getContent());
            assertEquals(Collections.singletonList(4L), dto.getMentionedUserIds());
            assertEquals(2L, dto.getAuthorId());
            assertEquals("subtaskuser", dto.getAuthorUsername());
            assertFalse(dto.isCanEdit());
            assertFalse(dto.isCanReply());
            assertTrue(dto.isCanDelete());
            assertTrue(dto.isCanModerate());
        }

        @Test
        @DisplayName("Should build reply comment correctly")
        void builder_ForReplyComment_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CommentResponseDto dto = CommentResponseDto.builder()
                    .id(3L)
                    .taskId(100L)
                    .projectId(300L)
                    .parentCommentId(1L)
                    .content("This is a reply")
                    .mentionedUserIds(Collections.emptyList())
                    .authorId(3L)
                    .authorUsername("replyuser")
                    .createdAt(now)
                    .replies(Collections.emptyList())
                    .replyCount(0)
                    .canEdit(true)
                    .canReply(true)
                    .build();

            // Assert
            assertEquals(3L, dto.getId());
            assertEquals(100L, dto.getTaskId());
            assertNull(dto.getSubtaskId());
            assertEquals(300L, dto.getProjectId());
            assertEquals(1L, dto.getParentCommentId());
            assertEquals("This is a reply", dto.getContent());
            assertTrue(dto.getMentionedUserIds().isEmpty());
            assertEquals(3L, dto.getAuthorId());
            assertEquals("replyuser", dto.getAuthorUsername());
            assertTrue(dto.getReplies().isEmpty());
            assertEquals(0, dto.getReplyCount());
            assertTrue(dto.isCanEdit());
            assertTrue(dto.isCanReply());
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle empty and null collections")
        void builder_WithEmptyAndNullCollections_ShouldHandleCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CommentResponseDto dtoWithEmptyCollections = CommentResponseDto.builder()
                    .id(1L)
                    .content("Comment with empty collections")
                    .mentionedUserIds(Collections.emptyList())
                    .authorId(1L)
                    .authorUsername("user")
                    .createdAt(now)
                    .replies(Collections.emptyList())
                    .build();

            CommentResponseDto dtoWithNullCollections = CommentResponseDto.builder()
                    .id(2L)
                    .content("Comment with null collections")
                    .mentionedUserIds(null)
                    .authorId(1L)
                    .authorUsername("user")
                    .createdAt(now)
                    .replies(null)
                    .build();

            // Assert
            assertTrue(dtoWithEmptyCollections.getMentionedUserIds().isEmpty());
            assertTrue(dtoWithEmptyCollections.getReplies().isEmpty());
            assertNull(dtoWithNullCollections.getMentionedUserIds());
            assertNull(dtoWithNullCollections.getReplies());
        }

        @Test
        @DisplayName("Should handle zero and negative IDs")
        void builder_WithZeroAndNegativeIds_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            // Act
            CommentResponseDto dto = CommentResponseDto.builder()
                    .id(0L)
                    .taskId(-1L)
                    .subtaskId(-2L)
                    .projectId(-3L)
                    .parentCommentId(-4L)
                    .content("Comment with edge case IDs")
                    .mentionedUserIds(Arrays.asList(0L, -1L, -2L))
                    .authorId(-5L)
                    .authorUsername("edgeuser")
                    .createdAt(now)
                    .replyCount(-1)
                    .build();

            // Assert
            assertEquals(0L, dto.getId());
            assertEquals(-1L, dto.getTaskId());
            assertEquals(-2L, dto.getSubtaskId());
            assertEquals(-3L, dto.getProjectId());
            assertEquals(-4L, dto.getParentCommentId());
            assertEquals(Arrays.asList(0L, -1L, -2L), dto.getMentionedUserIds());
            assertEquals(-5L, dto.getAuthorId());
            assertEquals(-1, dto.getReplyCount());
        }

        @Test
        @DisplayName("Should handle special characters in content and username")
        void builder_WithSpecialCharacters_ShouldHandleCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();
            String specialContent = "Comment with émojis 🚀, symbols @#$%^&*(), and Unicode 中文";
            String specialUsername = "user@domain.com";

            // Act
            CommentResponseDto dto = CommentResponseDto.builder()
                    .id(1L)
                    .content(specialContent)
                    .authorId(1L)
                    .authorUsername(specialUsername)
                    .createdAt(now)
                    .build();

            // Assert
            assertEquals(specialContent, dto.getContent());
            assertEquals(specialUsername, dto.getAuthorUsername());
        }

        @Test
        @DisplayName("Should handle nested replies structure")
        void builder_WithNestedReplies_ShouldBuildCorrectly() {
            // Arrange
            OffsetDateTime now = OffsetDateTime.now();

            CommentResponseDto nestedReply = CommentResponseDto.builder()
                    .id(100L)
                    .content("Nested reply")
                    .authorId(3L)
                    .authorUsername("user3")
                    .createdAt(now.plusMinutes(2))
                    .replies(Collections.emptyList())
                    .replyCount(0)
                    .build();

            CommentResponseDto reply = CommentResponseDto.builder()
                    .id(10L)
                    .content("First level reply")
                    .authorId(2L)
                    .authorUsername("user2")
                    .createdAt(now.plusMinutes(1))
                    .replies(Collections.singletonList(nestedReply))
                    .replyCount(1)
                    .build();

            // Act
            CommentResponseDto parentComment = CommentResponseDto.builder()
                    .id(1L)
                    .content("Parent comment")
                    .authorId(1L)
                    .authorUsername("user1")
                    .createdAt(now)
                    .replies(Collections.singletonList(reply))
                    .replyCount(1)
                    .build();

            // Assert
            assertEquals(1, parentComment.getReplies().size());
            assertEquals(1, parentComment.getReplyCount());

            CommentResponseDto firstReply = parentComment.getReplies().get(0);
            assertEquals(10L, firstReply.getId());
            assertEquals(1, firstReply.getReplies().size());
            assertEquals(1, firstReply.getReplyCount());

            CommentResponseDto secondLevelReply = firstReply.getReplies().get(0);
            assertEquals(100L, secondLevelReply.getId());
            assertTrue(secondLevelReply.getReplies().isEmpty());
            assertEquals(0, secondLevelReply.getReplyCount());
        }
    }
}