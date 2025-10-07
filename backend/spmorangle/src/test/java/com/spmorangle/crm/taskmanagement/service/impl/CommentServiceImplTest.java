package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.dto.CommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.CreateCommentDto;
import com.spmorangle.crm.taskmanagement.dto.CreateCommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateCommentDto;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskComment;
import com.spmorangle.crm.taskmanagement.model.Subtask;
import com.spmorangle.crm.taskmanagement.repository.TaskCommentRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.SubtaskRepository;
import com.spmorangle.crm.taskmanagement.util.CommentPermissionHelper;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Tests")
class CommentServiceImplTest {

    @Mock
    private NotificationMessagePublisher notificationMessagePublisher;

    @Mock
    private CommentPermissionHelper permissionHelper;

    @Mock
    private TaskCommentRepository taskCommentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private TaskComment testComment;
    private Task testTask;
    private Subtask testSubtask;
    private CreateCommentDto createCommentDto;
    private UpdateCommentDto updateCommentDto;
    private UserResponseDto testUser;
    private OffsetDateTime fixedDateTime;

    @BeforeEach
    void setUp() {
        fixedDateTime = OffsetDateTime.now();

        // Setup test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setProjectId(100L);

        // Setup test subtask
        testSubtask = new Subtask();
        testSubtask.setId(10L);
        testSubtask.setProjectId(100L);

        // Setup test comment
        testComment = new TaskComment();
        testComment.setId(1L);
        testComment.setTaskId(1L);
        testComment.setProjectId(100L);
        testComment.setContent("Test comment");
        testComment.setCreatedBy(10L);
        testComment.setUpdatedBy(10L);
        testComment.setCreatedAt(fixedDateTime);
        testComment.setMentionedUserIds(Arrays.asList(20L, 30L));
        testComment.setDeleted(false);
        testComment.setEdited(false);

        // Setup test DTOs
        createCommentDto = CreateCommentDto.builder()
                .taskId(1L)
                .content("Test comment")
                .mentionedUserIds(Arrays.asList(20L, 30L))
                .build();

        updateCommentDto = UpdateCommentDto.builder()
                .commentId(1L)
                .content("Updated comment")
                .mentionedUserIds(Arrays.asList(40L, 50L))
                .build();

        // Setup test user
        testUser = new UserResponseDto(10L, "testuser", "test@example.com", "USER", UUID.randomUUID());
    }

    @Nested
    @DisplayName("createComment Tests")
    class CreateCommentTests {

        @Test
        @DisplayName("Should create task comment successfully")
        void createComment_ForTask_ShouldCreateSuccessfully() {
            // Arrange
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(testComment);
            // Mock project members to include mentioned users
            UserResponseDto user20 = new UserResponseDto(20L, "user20", "user20@example.com", "USER", UUID.randomUUID());
            UserResponseDto user30 = new UserResponseDto(30L, "user30", "user30@example.com", "USER", UUID.randomUUID());
            when(userManagementService.getProjectMembers(100L)).thenReturn(Arrays.asList(user20, user30));

            // Act
            CreateCommentResponseDto result = commentService.createComment(createCommentDto, 10L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTaskId()).isEqualTo(1L);
            assertThat(result.getContent()).isEqualTo("Test comment");
            assertThat(result.getAuthorId()).isEqualTo(10L);

            verify(taskCommentRepository).save(any(TaskComment.class));
            // Verify notification publishing instead of events
            verify(notificationMessagePublisher, atLeastOnce()).publishCommentNotification(any());
        }

        @Test
        @DisplayName("Should create subtask comment successfully")
        void createComment_ForSubtask_ShouldCreateSuccessfully() {
            // Arrange
            CreateCommentDto subtaskCommentDto = CreateCommentDto.builder()
                    .subtaskId(10L)
                    .content("Subtask comment")
                    .build();

            TaskComment subtaskComment = new TaskComment();
            subtaskComment.setId(2L);
            subtaskComment.setSubtaskId(10L);
            subtaskComment.setProjectId(100L);
            subtaskComment.setContent("Subtask comment");
            subtaskComment.setCreatedBy(10L);
            subtaskComment.setUpdatedBy(10L);

            when(subtaskRepository.findById(10L)).thenReturn(Optional.of(testSubtask));
            when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(subtaskComment);

            // Act
            CreateCommentResponseDto result = commentService.createComment(subtaskCommentDto, 10L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getSubtaskId()).isEqualTo(10L);
            assertThat(result.getContent()).isEqualTo("Subtask comment");

            verify(subtaskRepository, atLeastOnce()).findById(10L);
            verify(taskCommentRepository).save(any(TaskComment.class));
            verify(notificationMessagePublisher).publishCommentNotification(any());
        }

        @Test
        @DisplayName("Should create reply comment successfully")
        void createComment_AsReply_ShouldCreateSuccessfully() {
            // Arrange
            CreateCommentDto replyDto = CreateCommentDto.builder()
                    .taskId(1L)
                    .parentCommentId(1L)
                    .content("Reply comment")
                    .build();

            TaskComment replyComment = new TaskComment();
            replyComment.setId(3L);
            replyComment.setTaskId(1L);
            replyComment.setParentCommentId(1L);
            replyComment.setProjectId(100L);
            replyComment.setContent("Reply comment");
            replyComment.setCreatedBy(10L);
            replyComment.setUpdatedBy(10L);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(replyComment);

            // Act
            CreateCommentResponseDto result = commentService.createComment(replyDto, 10L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getParentCommentId()).isEqualTo(1L);
            assertThat(result.getContent()).isEqualTo("Reply comment");

            verify(taskCommentRepository).save(any(TaskComment.class));
            verify(notificationMessagePublisher).publishCommentNotification(any());
        }

        @Test
        @DisplayName("Should not publish mention event when no mentions")
        void createComment_WithoutMentions_ShouldNotPublishMentionEvent() {
            // Arrange
            CreateCommentDto noMentionsDto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content("Comment without mentions")
                    .mentionedUserIds(Collections.emptyList())
                    .build();

            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(testComment);

            // Act
            commentService.createComment(noMentionsDto, 10L);

            // Assert
            verify(notificationMessagePublisher).publishCommentNotification(any());
        }

        @Test
        @DisplayName("Should throw exception when neither task nor subtask specified")
        void createComment_WithoutTaskOrSubtask_ShouldThrowException() {
            // Arrange
            CreateCommentDto invalidDto = CreateCommentDto.builder()
                    .content("Invalid comment")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> commentService.createComment(invalidDto, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Comment must be associated with either a task or subtask");
        }

        @Test
        @DisplayName("Should throw exception when both task and subtask specified")
        void createComment_WithBothTaskAndSubtask_ShouldThrowException() {
            // Arrange
            CreateCommentDto invalidDto = CreateCommentDto.builder()
                    .taskId(1L)
                    .subtaskId(10L)
                    .content("Invalid comment")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> commentService.createComment(invalidDto, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Comment cannot be associated with both task and subtask");
        }

        @Test
        @DisplayName("Should throw exception when task not found")
        void createComment_WithNonExistentTask_ShouldThrowException() {
            // Arrange
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            CreateCommentDto invalidDto = CreateCommentDto.builder()
                    .taskId(999L)
                    .content("Comment for non-existent task")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> commentService.createComment(invalidDto, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Task not found");
        }

        @Test
        @DisplayName("Should throw exception when subtask not found")
        void createComment_WithNonExistentSubtask_ShouldThrowException() {
            // Arrange
            when(subtaskRepository.findById(999L)).thenReturn(Optional.empty());

            CreateCommentDto invalidDto = CreateCommentDto.builder()
                    .subtaskId(999L)
                    .content("Comment for non-existent subtask")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> commentService.createComment(invalidDto, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Subtask not found");
        }
    }

    @Nested
    @DisplayName("updateComment Tests")
    class UpdateCommentTests {

        @Test
        @DisplayName("Should update comment successfully when authorized")
        void updateComment_WhenAuthorized_ShouldUpdateSuccessfully() {
            // Arrange
            when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(permissionHelper.canEditComment(10L, testComment)).thenReturn(true);
            when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(testComment);
            when(userManagementService.getUserById(10L)).thenReturn(testUser);
            when(taskCommentRepository.countRepliesByParentCommentId(1L)).thenReturn(0L);
            // Mock project members to include mentioned users
            UserResponseDto user40 = new UserResponseDto(40L, "user40", "user40@example.com", "USER", UUID.randomUUID());
            UserResponseDto user50 = new UserResponseDto(50L, "user50", "user50@example.com", "USER", UUID.randomUUID());
            when(userManagementService.getProjectMembers(100L)).thenReturn(Arrays.asList(user40, user50));
            // Mock for notification publishing
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

            // Act
            CommentResponseDto result = commentService.updateComment(updateCommentDto, 10L);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);

            ArgumentCaptor<TaskComment> commentCaptor = ArgumentCaptor.forClass(TaskComment.class);
            verify(taskCommentRepository).save(commentCaptor.capture());
            TaskComment savedComment = commentCaptor.getValue();
            assertThat(savedComment.getContent()).isEqualTo("Updated comment");
            assertThat(savedComment.isEdited()).isTrue();
            assertThat(savedComment.getUpdatedBy()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should throw exception when user not authorized")
        void updateComment_WhenNotAuthorized_ShouldThrowException() {
            // Arrange
            when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(permissionHelper.canEditComment(20L, testComment)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> commentService.updateComment(updateCommentDto, 20L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not authorized to edit this comment");

            verify(taskCommentRepository, never()).save(any(TaskComment.class));
            verify(notificationMessagePublisher, never()).publishCommentNotification(any());
        }

        @Test
        @DisplayName("Should throw exception when comment not found")
        void updateComment_WhenCommentNotFound_ShouldThrowException() {
            // Arrange
            when(taskCommentRepository.findById(999L)).thenReturn(Optional.empty());

            UpdateCommentDto invalidDto = UpdateCommentDto.builder()
                    .commentId(999L)
                    .content("Updated content")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> commentService.updateComment(invalidDto, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Comment not found");
        }

        @Test
        @DisplayName("Should throw exception when comment is deleted")
        void updateComment_WhenCommentDeleted_ShouldThrowException() {
            // Arrange
            testComment.setDeleted(true);
            when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(permissionHelper.canEditComment(10L, testComment)).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> commentService.updateComment(updateCommentDto, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Cannot edit deleted comment");

            verify(taskCommentRepository, never()).save(any(TaskComment.class));
        }
    }

    @Nested
    @DisplayName("deleteComment Tests")
    class DeleteCommentTests {

        @Test
        @DisplayName("Should delete comment successfully when authorized")
        void deleteComment_WhenAuthorized_ShouldDeleteSuccessfully() {
            // Arrange
            when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(permissionHelper.canDeleteComment(10L, testComment)).thenReturn(true);

            // Act
            commentService.deleteComment(1L, 10L);

            // Assert
            ArgumentCaptor<TaskComment> commentCaptor = ArgumentCaptor.forClass(TaskComment.class);
            verify(taskCommentRepository).save(commentCaptor.capture());
            TaskComment deletedComment = commentCaptor.getValue();
            assertThat(deletedComment.isDeleted()).isTrue();
            assertThat(deletedComment.getUpdatedBy()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should throw exception when user not authorized")
        void deleteComment_WhenNotAuthorized_ShouldThrowException() {
            // Arrange
            when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(permissionHelper.canDeleteComment(20L, testComment)).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> commentService.deleteComment(1L, 20L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not authorized to delete this comment");

            verify(taskCommentRepository, never()).save(any(TaskComment.class));
        }

        @Test
        @DisplayName("Should throw exception when comment not found")
        void deleteComment_WhenCommentNotFound_ShouldThrowException() {
            // Arrange
            when(taskCommentRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> commentService.deleteComment(999L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Comment not found");
        }
    }

    @Nested
    @DisplayName("getTaskComments Tests")
    class GetTaskCommentsTests {

        @Test
        @DisplayName("Should get task comments without user context")
        void getTaskComments_WithoutUserContext_ShouldReturnComments() {
            // Arrange
            List<TaskComment> comments = Arrays.asList(testComment);
            when(taskCommentRepository.findTopLevelCommentsByTaskId(1L)).thenReturn(comments);
            when(taskCommentRepository.findRepliesByParentCommentId(1L)).thenReturn(Collections.emptyList());
            when(userManagementService.getUserById(10L)).thenReturn(testUser);
            when(taskCommentRepository.countRepliesByParentCommentId(1L)).thenReturn(0L);

            // Act
            List<CommentResponseDto> result = commentService.getTaskComments(1L, null);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).isCanEdit()).isFalse(); // No user context
            assertThat(result.get(0).isCanReply()).isTrue(); // Default true
        }

        @Test
        @DisplayName("Should get task comments with user context")
        void getTaskComments_WithUserContext_ShouldReturnCommentsWithPermissions() {
            // Arrange
            List<TaskComment> comments = Arrays.asList(testComment);
            when(taskCommentRepository.findTopLevelCommentsByTaskId(1L)).thenReturn(comments);
            when(taskCommentRepository.findRepliesByParentCommentId(1L)).thenReturn(Collections.emptyList());
            when(userManagementService.getUserById(10L)).thenReturn(testUser);
            when(taskCommentRepository.countRepliesByParentCommentId(1L)).thenReturn(0L);
            when(permissionHelper.canEditComment(10L, testComment)).thenReturn(true);
            when(permissionHelper.canDeleteComment(10L, testComment)).thenReturn(true);
            when(permissionHelper.canReplyToComment(10L, 100L)).thenReturn(true);
            when(permissionHelper.canModerateComment(10L, 100L)).thenReturn(false);

            // Act
            List<CommentResponseDto> result = commentService.getTaskComments(1L, 10L);

            // Assert
            assertThat(result).hasSize(1);
            CommentResponseDto comment = result.get(0);
            assertThat(comment.getId()).isEqualTo(1L);
            assertThat(comment.isCanEdit()).isTrue();
            assertThat(comment.isCanDelete()).isTrue();
            assertThat(comment.isCanReply()).isTrue();
            assertThat(comment.isCanModerate()).isFalse();
        }
    }

    @Nested
    @DisplayName("getSubtaskComments Tests")
    class GetSubtaskCommentsTests {

        @Test
        @DisplayName("Should get subtask comments")
        void getSubtaskComments_ShouldReturnComments() {
            // Arrange
            TaskComment subtaskComment = new TaskComment();
            subtaskComment.setId(2L);
            subtaskComment.setSubtaskId(10L);
            subtaskComment.setProjectId(100L);
            subtaskComment.setContent("Subtask comment");
            subtaskComment.setCreatedBy(10L);
            subtaskComment.setUpdatedBy(10L);

            List<TaskComment> comments = Arrays.asList(subtaskComment);
            when(taskCommentRepository.findTopLevelCommentsBySubtaskId(10L)).thenReturn(comments);
            when(taskCommentRepository.findRepliesByParentCommentId(2L)).thenReturn(Collections.emptyList());
            when(userManagementService.getUserById(10L)).thenReturn(testUser);
            when(taskCommentRepository.countRepliesByParentCommentId(2L)).thenReturn(0L);

            // Act
            List<CommentResponseDto> result = commentService.getSubtaskComments(10L, null);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(2L);
            assertThat(result.get(0).getSubtaskId()).isEqualTo(10L);
        }
    }

    @Nested
    @DisplayName("getCommentReplies Tests")
    class GetCommentRepliesTests {

        @Test
        @DisplayName("Should get comment replies")
        void getCommentReplies_ShouldReturnReplies() {
            // Arrange
            TaskComment reply = new TaskComment();
            reply.setId(3L);
            reply.setTaskId(1L);
            reply.setParentCommentId(1L);
            reply.setContent("Reply comment");
            reply.setCreatedBy(20L);

            List<TaskComment> replies = Arrays.asList(reply);
            when(taskCommentRepository.findRepliesByParentCommentId(1L)).thenReturn(replies);
            when(userManagementService.getUserById(20L)).thenReturn(
                new UserResponseDto(20L, "replyuser", "reply@example.com", "USER", UUID.randomUUID()));
            when(taskCommentRepository.countRepliesByParentCommentId(3L)).thenReturn(0L);

            // Act
            List<CommentResponseDto> result = commentService.getCommentReplies(1L, null);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(3L);
            assertThat(result.get(0).getParentCommentId()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("getCommentById Tests")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Should get comment by ID")
        void getCommentById_WhenCommentExists_ShouldReturnComment() {
            // Arrange
            when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(userManagementService.getUserById(10L)).thenReturn(testUser);
            when(taskCommentRepository.countRepliesByParentCommentId(1L)).thenReturn(2L);

            // Act
            CommentResponseDto result = commentService.getCommentById(1L, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getReplyCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should throw exception when comment not found")
        void getCommentById_WhenCommentNotFound_ShouldThrowException() {
            // Arrange
            when(taskCommentRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> commentService.getCommentById(999L, null))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Comment not found");
        }
    }

    @Nested
    @DisplayName("getUserMentions Tests")
    class GetUserMentionsTests {

        @Test
        @DisplayName("Should get user mentions")
        void getUserMentions_ShouldReturnMentions() {
            // Arrange
            List<TaskComment> mentions = Arrays.asList(testComment);
            when(taskCommentRepository.findByMentionedUserId(20L)).thenReturn(mentions);
            when(userManagementService.getUserById(10L)).thenReturn(testUser);
            when(taskCommentRepository.countRepliesByParentCommentId(1L)).thenReturn(0L);

            // Act
            List<CommentResponseDto> result = commentService.getUserMentions(20L);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(0).getMentionedUserIds()).contains(20L);
        }
    }

    @Nested
    @DisplayName("getTaskCommentsWithFilters Tests")
    class GetTaskCommentsWithFiltersTests {

        @Test
        @DisplayName("Should get filtered task comments")
        void getTaskCommentsWithFilters_ShouldReturnFilteredComments() {
            // Arrange
            List<TaskComment> filteredComments = Arrays.asList(testComment);
            when(taskCommentRepository.findTaskCommentsWithFilters(1L, null, false))
                    .thenReturn(filteredComments);
            when(taskCommentRepository.findRepliesByParentCommentId(1L)).thenReturn(Collections.emptyList());
            when(userManagementService.getUserById(10L)).thenReturn(testUser);
            when(taskCommentRepository.countRepliesByParentCommentId(1L)).thenReturn(0L);
            when(permissionHelper.canReadComments(10L, 1L)).thenReturn(true);

            // Act
            List<CommentResponseDto> result = commentService.getTaskCommentsWithFilters(1L, null, false, 10L);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            verify(taskCommentRepository).findTaskCommentsWithFilters(1L, null, false);
        }
    }

    @Nested
    @DisplayName("getCommentAuthors Tests")
    class GetCommentAuthorsTests {

        @Test
        @DisplayName("Should get comment authors by task ID")
        void getCommentAuthorsByTaskId_ShouldReturnAuthors() {
            // Arrange
            List<Long> authors = Arrays.asList(10L, 20L, 30L);
            when(taskCommentRepository.findCommentAuthorsByTaskId(1L)).thenReturn(authors);

            // Act
            List<Long> result = commentService.getCommentAuthorsByTaskId(1L);

            // Assert
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyInAnyOrder(10L, 20L, 30L);
        }

        @Test
        @DisplayName("Should get comment authors by subtask ID")
        void getCommentAuthorsBySubtaskId_ShouldReturnAuthors() {
            // Arrange
            List<Long> authors = Arrays.asList(10L, 40L);
            when(taskCommentRepository.findCommentAuthorsBySubtaskId(10L)).thenReturn(authors);

            // Act
            List<Long> result = commentService.getCommentAuthorsBySubtaskId(10L);

            // Assert
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(10L, 40L);
        }
    }

    @Nested
    @DisplayName("PermissionAware Interface Tests")
    class PermissionAwareInterfaceTests {

        @Test
        @DisplayName("Should delegate canRead to permission helper")
        void canRead_ShouldDelegateToPermissionHelper() {
            // Arrange
            when(permissionHelper.canReadComments(10L, 100L)).thenReturn(true);

            // Act
            boolean result = commentService.canRead(10L, 100L);

            // Assert
            assertThat(result).isTrue();
            verify(permissionHelper).canReadComments(10L, 100L);
        }

        @Test
        @DisplayName("Should delegate canWrite to permission helper")
        void canWrite_ShouldDelegateToPermissionHelper() {
            // Arrange
            when(permissionHelper.canReplyToComment(10L, 100L)).thenReturn(true);

            // Act
            boolean result = commentService.canWrite(10L, 100L);

            // Assert
            assertThat(result).isTrue();
            verify(permissionHelper).canReplyToComment(10L, 100L);
        }

        @Test
        @DisplayName("Should delegate canModerate to permission helper")
        void canModerate_ShouldDelegateToPermissionHelper() {
            // Arrange
            when(permissionHelper.canModerateComment(10L, 100L)).thenReturn(false);

            // Act
            boolean result = commentService.canModerate(10L, 100L);

            // Assert
            assertThat(result).isFalse();
            verify(permissionHelper).canModerateComment(10L, 100L);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should handle user service failure gracefully")
        void mapToCommentResponseDto_WhenUserServiceFails_ShouldUseUnknownUser() {
            // Arrange
            when(taskCommentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(userManagementService.getUserById(10L)).thenThrow(new RuntimeException("User service error"));
            when(taskCommentRepository.countRepliesByParentCommentId(1L)).thenReturn(0L);

            // Act
            CommentResponseDto result = commentService.getCommentById(1L, null);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getAuthorUsername()).isEqualTo("Unknown User");
        }

        @Test
        @DisplayName("Should handle null notification publisher gracefully")
        void createComment_WithNullNotificationPublisher_ShouldNotThrowException() {
            // Arrange
            commentService = new CommentServiceImpl(
                permissionHelper,
                taskCommentRepository,
                taskRepository,
                taskAssigneeRepository,
                subtaskRepository,
                userManagementService,
                null  // null notification publisher
            );

            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskCommentRepository.save(any(TaskComment.class))).thenReturn(testComment);
            // Mock project members to include mentioned users
            UserResponseDto user20 = new UserResponseDto(20L, "user20", "user20@example.com", "USER", UUID.randomUUID());
            UserResponseDto user30 = new UserResponseDto(30L, "user30", "user30@example.com", "USER", UUID.randomUUID());
            when(userManagementService.getProjectMembers(100L)).thenReturn(Arrays.asList(user20, user30));

            // Act & Assert - Should not throw exception
            CreateCommentResponseDto result = commentService.createComment(createCommentDto, 10L);
            assertThat(result).isNotNull();
        }
    }
}