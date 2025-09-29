package com.spmorangle.crm.taskmanagement.util;

import com.spmorangle.crm.taskmanagement.model.TaskComment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CommentPermissionHelper Tests")
class CommentPermissionHelperTest {

    private CommentPermissionHelper permissionHelper;
    private TaskComment comment;

    @BeforeEach
    void setUp() {
        permissionHelper = new CommentPermissionHelper();

        // Create a test comment
        comment = new TaskComment();
        comment.setId(1L);
        comment.setTaskId(100L);
        comment.setProjectId(200L);
        comment.setContent("Test comment");
        comment.setCreatedBy(10L);
        
    }

    @Nested
    @DisplayName("canEditComment Tests")
    class CanEditCommentTests {

        @Test
        @DisplayName("Should allow author to edit comment when permissions disabled")
        void canEditComment_AuthorWithPermissionsDisabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canEdit = permissionHelper.canEditComment(10L, comment);

            // Assert
            assertTrue(canEdit);
        }

        @Test
        @DisplayName("Should deny non-author to edit comment when permissions disabled")
        void canEditComment_NonAuthorWithPermissionsDisabled_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canEdit = permissionHelper.canEditComment(20L, comment);

            // Assert
            assertFalse(canEdit);
        }

        @Test
        @DisplayName("Should allow author to edit comment when permissions enabled")
        void canEditComment_AuthorWithPermissionsEnabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", true);

            // Act
            boolean canEdit = permissionHelper.canEditComment(10L, comment);

            // Assert
            assertTrue(canEdit);
        }

        @Test
        @DisplayName("Should deny non-author to edit comment when permissions enabled")
        void canEditComment_NonAuthorWithPermissionsEnabled_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", true);

            // Act
            boolean canEdit = permissionHelper.canEditComment(20L, comment);

            // Assert
            assertFalse(canEdit);
        }

        @Test
        @DisplayName("Should handle null userId")
        void canEditComment_WithNullUserId_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canEdit = permissionHelper.canEditComment(null, comment);

            // Assert
            assertFalse(canEdit);
        }

        @Test
        @DisplayName("Should handle comment with null createdBy")
        void canEditComment_CommentWithNullCreatedBy_ShouldHandleGracefully() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);
            comment.setCreatedBy(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                permissionHelper.canEditComment(10L, comment));
        }

        @Test
        @DisplayName("Should handle zero and negative user IDs")
        void canEditComment_WithZeroAndNegativeUserIds_ShouldWorkCorrectly() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);
            comment.setCreatedBy(0L);

            // Act
            boolean zeroCanEdit = permissionHelper.canEditComment(0L, comment);
            boolean negativeCannotEdit = permissionHelper.canEditComment(-1L, comment);

            // Assert
            assertTrue(zeroCanEdit);
            assertFalse(negativeCannotEdit);
        }
    }

    @Nested
    @DisplayName("canDeleteComment Tests")
    class CanDeleteCommentTests {

        @Test
        @DisplayName("Should allow author to delete comment when permissions disabled")
        void canDeleteComment_AuthorWithPermissionsDisabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canDelete = permissionHelper.canDeleteComment(10L, comment);

            // Assert
            assertTrue(canDelete);
        }

        @Test
        @DisplayName("Should deny non-author to delete comment when permissions disabled")
        void canDeleteComment_NonAuthorWithPermissionsDisabled_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canDelete = permissionHelper.canDeleteComment(20L, comment);

            // Assert
            assertFalse(canDelete);
        }

        @Test
        @DisplayName("Should allow author to delete comment when permissions enabled")
        void canDeleteComment_AuthorWithPermissionsEnabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", true);

            // Act
            boolean canDelete = permissionHelper.canDeleteComment(10L, comment);

            // Assert
            assertTrue(canDelete);
        }

        @Test
        @DisplayName("Should deny non-author to delete comment when permissions enabled")
        void canDeleteComment_NonAuthorWithPermissionsEnabled_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", true);

            // Act
            boolean canDelete = permissionHelper.canDeleteComment(20L, comment);

            // Assert
            assertFalse(canDelete);
        }

        @Test
        @DisplayName("Should handle null userId")
        void canDeleteComment_WithNullUserId_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canDelete = permissionHelper.canDeleteComment(null, comment);

            // Assert
            assertFalse(canDelete);
        }

        @Test
        @DisplayName("Should handle comment with null createdBy")
        void canDeleteComment_CommentWithNullCreatedBy_ShouldHandleGracefully() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);
            comment.setCreatedBy(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                permissionHelper.canDeleteComment(10L, comment));
        }
    }

    @Nested
    @DisplayName("canModerateComment Tests")
    class CanModerateCommentTests {

        @Test
        @DisplayName("Should deny moderation when permissions disabled")
        void canModerateComment_WithPermissionsDisabled_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canModerate = permissionHelper.canModerateComment(10L, 200L);

            // Assert
            assertFalse(canModerate);
        }

        @Test
        @DisplayName("Should deny moderation when permissions enabled (not implemented)")
        void canModerateComment_WithPermissionsEnabled_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", true);

            // Act
            boolean canModerate = permissionHelper.canModerateComment(10L, 200L);

            // Assert
            // Currently returns false as not implemented
            assertFalse(canModerate);
        }

        @Test
        @DisplayName("Should handle null userId")
        void canModerateComment_WithNullUserId_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canModerate = permissionHelper.canModerateComment(null, 200L);

            // Assert
            assertFalse(canModerate);
        }

        @Test
        @DisplayName("Should handle null projectId")
        void canModerateComment_WithNullProjectId_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canModerate = permissionHelper.canModerateComment(10L, null);

            // Assert
            assertFalse(canModerate);
        }

        @Test
        @DisplayName("Should handle zero and negative IDs")
        void canModerateComment_WithZeroAndNegativeIds_ShouldReturnFalse() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean zeroUser = permissionHelper.canModerateComment(0L, 200L);
            boolean negativeUser = permissionHelper.canModerateComment(-1L, 200L);
            boolean zeroProject = permissionHelper.canModerateComment(10L, 0L);
            boolean negativeProject = permissionHelper.canModerateComment(10L, -1L);

            // Assert
            assertFalse(zeroUser);
            assertFalse(negativeUser);
            assertFalse(zeroProject);
            assertFalse(negativeProject);
        }
    }

    @Nested
    @DisplayName("canReplyToComment Tests")
    class CanReplyToCommentTests {

        @Test
        @DisplayName("Should allow reply when permissions disabled")
        void canReplyToComment_WithPermissionsDisabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canReply = permissionHelper.canReplyToComment(10L, 200L);

            // Assert
            assertTrue(canReply);
        }

        @Test
        @DisplayName("Should allow reply when permissions enabled")
        void canReplyToComment_WithPermissionsEnabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", true);

            // Act
            boolean canReply = permissionHelper.canReplyToComment(10L, 200L);

            // Assert
            assertTrue(canReply);
        }

        @Test
        @DisplayName("Should allow reply with null userId when permissions disabled")
        void canReplyToComment_WithNullUserIdAndPermissionsDisabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canReply = permissionHelper.canReplyToComment(null, 200L);

            // Assert
            assertTrue(canReply);
        }

        @Test
        @DisplayName("Should allow reply with null projectId")
        void canReplyToComment_WithNullProjectId_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canReply = permissionHelper.canReplyToComment(10L, null);

            // Assert
            assertTrue(canReply);
        }

        @Test
        @DisplayName("Should allow reply with zero and negative IDs")
        void canReplyToComment_WithZeroAndNegativeIds_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean zeroUser = permissionHelper.canReplyToComment(0L, 200L);
            boolean negativeUser = permissionHelper.canReplyToComment(-1L, 200L);
            boolean zeroProject = permissionHelper.canReplyToComment(10L, 0L);
            boolean negativeProject = permissionHelper.canReplyToComment(10L, -1L);

            // Assert
            assertTrue(zeroUser);
            assertTrue(negativeUser);
            assertTrue(zeroProject);
            assertTrue(negativeProject);
        }
    }

    @Nested
    @DisplayName("canReadComments Tests")
    class CanReadCommentsTests {

        @Test
        @DisplayName("Should allow reading when permissions disabled")
        void canReadComments_WithPermissionsDisabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canRead = permissionHelper.canReadComments(10L, 200L);

            // Assert
            assertTrue(canRead);
        }

        @Test
        @DisplayName("Should allow reading when permissions enabled")
        void canReadComments_WithPermissionsEnabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", true);

            // Act
            boolean canRead = permissionHelper.canReadComments(10L, 200L);

            // Assert
            assertTrue(canRead);
        }

        @Test
        @DisplayName("Should allow reading with null userId when permissions disabled")
        void canReadComments_WithNullUserIdAndPermissionsDisabled_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canRead = permissionHelper.canReadComments(null, 200L);

            // Assert
            assertTrue(canRead);
        }

        @Test
        @DisplayName("Should allow reading with null projectId")
        void canReadComments_WithNullProjectId_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean canRead = permissionHelper.canReadComments(10L, null);

            // Assert
            assertTrue(canRead);
        }

        @Test
        @DisplayName("Should allow reading with zero and negative IDs")
        void canReadComments_WithZeroAndNegativeIds_ShouldReturnTrue() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean zeroUser = permissionHelper.canReadComments(0L, 200L);
            boolean negativeUser = permissionHelper.canReadComments(-1L, 200L);
            boolean zeroProject = permissionHelper.canReadComments(10L, 0L);
            boolean negativeProject = permissionHelper.canReadComments(10L, -1L);

            // Assert
            assertTrue(zeroUser);
            assertTrue(negativeUser);
            assertTrue(zeroProject);
            assertTrue(negativeProject);
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("Should use default value false for permissions when not configured")
        void defaultConfiguration_ShouldHavePermissionsDisabled() {
            // Arrange
            CommentPermissionHelper defaultHelper = new CommentPermissionHelper();
            // No explicit configuration - should use default value false

            // Act
            boolean canModerate = defaultHelper.canModerateComment(10L, 200L);

            // Assert
            assertFalse(canModerate); // Should be false by default
        }

        @Test
        @DisplayName("Should work correctly when permissions explicitly enabled")
        void explicitlyEnabledPermissions_ShouldWorkCorrectly() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", true);

            // Act
            boolean authorCanEdit = permissionHelper.canEditComment(10L, comment);
            boolean nonAuthorCannotEdit = permissionHelper.canEditComment(20L, comment);
            boolean canReply = permissionHelper.canReplyToComment(10L, 200L);
            boolean canRead = permissionHelper.canReadComments(10L, 200L);
            boolean cannotModerate = permissionHelper.canModerateComment(10L, 200L);

            // Assert
            assertTrue(authorCanEdit);
            assertFalse(nonAuthorCannotEdit);
            assertTrue(canReply);
            assertTrue(canRead);
            assertFalse(cannotModerate); // Not implemented yet
        }

        @Test
        @DisplayName("Should work correctly when permissions explicitly disabled")
        void explicitlyDisabledPermissions_ShouldWorkCorrectly() {
            // Arrange
            ReflectionTestUtils.setField(permissionHelper, "permissionsEnabled", false);

            // Act
            boolean authorCanEdit = permissionHelper.canEditComment(10L, comment);
            boolean nonAuthorCannotEdit = permissionHelper.canEditComment(20L, comment);
            boolean canReply = permissionHelper.canReplyToComment(10L, 200L);
            boolean canRead = permissionHelper.canReadComments(10L, 200L);
            boolean cannotModerate = permissionHelper.canModerateComment(10L, 200L);

            // Assert
            assertTrue(authorCanEdit);
            assertFalse(nonAuthorCannotEdit);
            assertTrue(canReply);
            assertTrue(canRead);
            assertFalse(cannotModerate);
        }
    }
}