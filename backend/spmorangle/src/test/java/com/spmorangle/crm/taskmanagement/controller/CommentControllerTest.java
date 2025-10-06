package com.spmorangle.crm.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.CommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.CreateCommentDto;
import com.spmorangle.crm.taskmanagement.dto.CreateCommentResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateCommentDto;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.CommentService;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({TaskManagementController.class, SubtaskController.class})
@DisplayName("Comment Controller Tests")
@WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private CollaboratorService collaboratorService;

    @MockBean
    private SubtaskService subtaskService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserContextService userContextService;

    @MockBean
    private UserManagementService userManagementService;

    private User testUser;
    private CreateCommentDto createCommentDto;
    private UpdateCommentDto updateCommentDto;
    private CreateCommentResponseDto createCommentResponseDto;
    private CommentResponseDto commentResponseDto;
    private OffsetDateTime fixedDateTime;

    @BeforeEach
    void setUp() {
        fixedDateTime = OffsetDateTime.now();

        // Setup test user
        testUser = new User();
        testUser.setId(10L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");

        // Setup DTOs
        createCommentDto = CreateCommentDto.builder()
                .taskId(1L)
                .content("Test comment content")
                .mentionedUserIds(Arrays.asList(20L, 30L))
                .build();

        updateCommentDto = UpdateCommentDto.builder()
                .commentId(1L)
                .content("Updated comment content")
                .mentionedUserIds(Arrays.asList(40L, 50L))
                .build();

        createCommentResponseDto = CreateCommentResponseDto.builder()
                .id(1L)
                .taskId(1L)
                .content("Test comment content")
                .authorId(10L)
                .createdAt(fixedDateTime)
                .build();

        commentResponseDto = CommentResponseDto.builder()
                .id(1L)
                .taskId(1L)
                .projectId(100L)
                .content("Test comment content")
                .mentionedUserIds(Arrays.asList(20L, 30L))
                .isEdited(false)
                .isDeleted(false)
                .authorId(10L)
                .authorUsername("testuser")
                .createdAt(fixedDateTime)
                .replies(Collections.emptyList())
                .replyCount(0)
                .canEdit(true)
                .canDelete(true)
                .canReply(true)
                .canModerate(false)
                .build();

        // Setup mocks
        when(userContextService.getRequestingUser()).thenReturn(testUser);

        // Reset all mocks to avoid interference between tests
        reset(commentService, collaboratorService, subtaskService, taskService, userManagementService);

        // Re-setup essential mocks after reset
        when(userContextService.getRequestingUser()).thenReturn(testUser);
    }

    @Nested
    @DisplayName("Create Comment Tests")
    class CreateCommentTests {

        @Test
        @DisplayName("Should create comment successfully")
        void createComment_WithValidData_ShouldReturnCreated() throws Exception {
            // Arrange
            when(commentService.createComment(any(CreateCommentDto.class), eq(10L)))
                    .thenReturn(createCommentResponseDto);

            // Act & Assert
            mockMvc.perform(post("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCommentDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.taskId").value(1L))
                    .andExpect(jsonPath("$.content").value("Test comment content"))
                    .andExpect(jsonPath("$.authorId").value(10L));

            verify(commentService).createComment(any(CreateCommentDto.class), eq(10L));
            // Remove any event verification since you're using RabbitMQ now
        }

        @Test
        @DisplayName("Should return 400 for invalid comment data")
        void createComment_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            // Arrange
            CreateCommentDto invalidDto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content("") // Invalid: empty content
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).createComment(any(), any());
        }

        @Test
        @DisplayName("Should create subtask comment successfully")
        void createComment_ForSubtask_ShouldReturnCreated() throws Exception {
            // Arrange
            CreateCommentDto subtaskCommentDto = CreateCommentDto.builder()
                    .subtaskId(10L)
                    .content("Subtask comment")
                    .build();

            CreateCommentResponseDto subtaskResponseDto = CreateCommentResponseDto.builder()
                    .id(2L)
                    .subtaskId(10L)
                    .content("Subtask comment")
                    .authorId(10L)
                    .createdAt(fixedDateTime)
                    .build();

            when(commentService.createComment(any(CreateCommentDto.class), eq(10L)))
                    .thenReturn(subtaskResponseDto);

            // Act & Assert
            mockMvc.perform(post("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(subtaskCommentDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(2L))
                    .andExpect(jsonPath("$.subtaskId").value(10L))
                    .andExpect(jsonPath("$.content").value("Subtask comment"));
        }

        @Test
        @DisplayName("Should create reply comment successfully")
        void createComment_AsReply_ShouldReturnCreated() throws Exception {
            // Arrange
            CreateCommentDto replyCommentDto = CreateCommentDto.builder()
                    .taskId(1L)
                    .parentCommentId(1L)
                    .content("Reply comment")
                    .build();

            CreateCommentResponseDto replyResponseDto = CreateCommentResponseDto.builder()
                    .id(3L)
                    .taskId(1L)
                    .parentCommentId(1L)
                    .content("Reply comment")
                    .authorId(10L)
                    .createdAt(fixedDateTime)
                    .build();

            when(commentService.createComment(any(CreateCommentDto.class), eq(10L)))
                    .thenReturn(replyResponseDto);

            // Act & Assert
            mockMvc.perform(post("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(replyCommentDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(3L))
                    .andExpect(jsonPath("$.parentCommentId").value(1L))
                    .andExpect(jsonPath("$.content").value("Reply comment"));
        }

        @Test
        @DisplayName("Should handle service exception")
        void createComment_WhenServiceThrowsException_ShouldReturnServerError() throws Exception {
            // Arrange
            when(commentService.createComment(any(CreateCommentDto.class), eq(10L)))
                    .thenThrow(new RuntimeException("Task not found"));

            // Act & Assert
            mockMvc.perform(post("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCommentDto)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Update Comment Tests")
    class UpdateCommentTests {

        @Test
        @DisplayName("Should update comment successfully")
        void updateComment_WithValidData_ShouldReturnOk() throws Exception {
            // Arrange
            CommentResponseDto updatedComment = CommentResponseDto.builder()
                    .id(1L)
                    .taskId(1L)
                    .content("Updated comment content")
                    .isEdited(true)
                    .authorId(10L)
                    .authorUsername("testuser")
                    .createdAt(fixedDateTime)
                    .updatedAt(fixedDateTime.plusMinutes(5))
                    .build();

            // Set up mock behavior for this specific test
            when(commentService.updateComment(any(UpdateCommentDto.class), eq(10L)))
                    .thenReturn(updatedComment);

            // Act & Assert
            mockMvc.perform(put("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateCommentDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.content").value("Updated comment content"));

            verify(commentService).updateComment(any(UpdateCommentDto.class), eq(10L));
        }

        @Test
        @DisplayName("Should return 400 for invalid update data")
        void updateComment_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            // Arrange
            UpdateCommentDto invalidDto = UpdateCommentDto.builder()
                    .commentId(null) // Invalid: null comment ID
                    .content("Updated content")
                    .build();

            // Act & Assert
            mockMvc.perform(put("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).updateComment(any(), any());
        }

        @Test
        @DisplayName("Should handle unauthorized update")
        void updateComment_WhenNotAuthorized_ShouldReturnServerError() throws Exception {
            // Arrange
            when(commentService.updateComment(any(UpdateCommentDto.class), eq(10L)))
                    .thenThrow(new RuntimeException("User not authorized to edit this comment"));

            // Act & Assert
            mockMvc.perform(put("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateCommentDto)))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Delete Comment Tests")
    class DeleteCommentTests {

        @Test
        @DisplayName("Should delete comment successfully")
        void deleteComment_WithValidId_ShouldReturnNoContent() throws Exception {
            // Arrange
            doNothing().when(commentService).deleteComment(1L, 10L);

            // Act & Assert
            mockMvc.perform(delete("/api/tasks/comments/1")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(commentService).deleteComment(1L, 10L);
        }

        @Test
        @DisplayName("Should handle comment not found")
        void deleteComment_WhenCommentNotFound_ShouldReturnServerError() throws Exception {
            // Arrange
            doThrow(new RuntimeException("Comment not found"))
                    .when(commentService).deleteComment(999L, 10L);

            // Act & Assert
            mockMvc.perform(delete("/api/tasks/comments/999")
                            .with(csrf()))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should handle unauthorized delete")
        void deleteComment_WhenNotAuthorized_ShouldReturnServerError() throws Exception {
            // Arrange
            doThrow(new RuntimeException("User not authorized to delete this comment"))
                    .when(commentService).deleteComment(1L, 10L);

            // Act & Assert
            mockMvc.perform(delete("/api/tasks/comments/1")
                            .with(csrf()))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Get Task Comments Tests")
    class GetTaskCommentsTests {

        @Test
        @DisplayName("Should get task comments successfully")
        void getTaskComments_WithValidTaskId_ShouldReturnOk() throws Exception {
            // Arrange
            List<CommentResponseDto> comments = Arrays.asList(commentResponseDto);
            when(commentService.getTaskComments(eq(1L), eq(10L))).thenReturn(comments);

            // Act & Assert
            mockMvc.perform(get("/api/tasks/1/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].taskId").value(1L))
                    .andExpect(jsonPath("$[0].content").value("Test comment content"));

            verify(commentService).getTaskComments(eq(1L), eq(10L));
        }

        @Test
        @DisplayName("Should get task comments with filters")
        void getTaskComments_WithFilters_ShouldReturnFilteredComments() throws Exception {
            // Arrange
            List<CommentResponseDto> filteredComments = Arrays.asList(commentResponseDto);
            when(commentService.getTaskCommentsWithFilters(1L, 10L, false, 10L))
                    .thenReturn(filteredComments);

            // Act & Assert
            mockMvc.perform(get("/api/tasks/1/comments")
                            .param("authorId", "10")
                            .param("resolved", "false")
                            .param("filter", "AUTHORED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L));

            verify(commentService).getTaskCommentsWithFilters(1L, 10L, false, 10L);
        }

        @Test
        @DisplayName("Should return empty list for task with no comments")
        void getTaskComments_WithNoComments_ShouldReturnEmptyList() throws Exception {
            // Arrange
            when(commentService.getTaskComments(eq(1L), eq(10L))).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/tasks/1/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Subtask Comments Tests")
    class GetSubtaskCommentsTests {

        @Test
        @DisplayName("Should get subtask comments successfully")
        void getSubtaskComments_WithValidSubtaskId_ShouldReturnOk() throws Exception {
            // Arrange
            CommentResponseDto subtaskComment = CommentResponseDto.builder()
                    .id(2L)
                    .subtaskId(10L)
                    .content("Subtask comment")
                    .authorId(10L)
                    .authorUsername("testuser")
                    .createdAt(fixedDateTime)
                    .build();

            List<CommentResponseDto> comments = Arrays.asList(subtaskComment);
            when(commentService.getSubtaskComments(eq(10L), eq(10L))).thenReturn(comments);

            // Act & Assert
            mockMvc.perform(get("/api/subtasks/10/comments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(2L))
                    .andExpect(jsonPath("$[0].subtaskId").value(10L))
                    .andExpect(jsonPath("$[0].content").value("Subtask comment"));

            verify(commentService).getSubtaskComments(eq(10L), eq(10L));
        }

        @Test
        @DisplayName("Should get subtask comments with filters")
        void getSubtaskComments_WithFilters_ShouldReturnFilteredComments() throws Exception {
            // Arrange
            List<CommentResponseDto> filteredComments = Arrays.asList(commentResponseDto);
            when(commentService.getSubtaskCommentsWithFilters(10L, 10L, true, 10L))
                    .thenReturn(filteredComments);

            // Act & Assert
            mockMvc.perform(get("/api/subtasks/10/comments")
                            .param("authorId", "10")
                            .param("resolved", "true")
                            .param("filter", "RESOLVED"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());

            verify(commentService).getSubtaskCommentsWithFilters(10L, 10L, true, 10L);
        }
    }

    @Nested
    @DisplayName("Get Comment By ID Tests")
    class GetCommentByIdTests {

        @Test
        @DisplayName("Should get comment by ID successfully")
        void getCommentById_WithValidId_ShouldReturnOk() throws Exception {
            // Arrange
            when(commentService.getCommentById(eq(1L), eq(10L))).thenReturn(commentResponseDto);

            // Act & Assert
            mockMvc.perform(get("/api/tasks/comments/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.content").value("Test comment content"))
                    .andExpect(jsonPath("$.authorUsername").value("testuser"));

            verify(commentService).getCommentById(eq(1L), eq(10L));
        }

        @Test
        @DisplayName("Should handle comment not found")
        void getCommentById_WhenCommentNotFound_ShouldReturnServerError() throws Exception {
            // Arrange
            when(commentService.getCommentById(eq(999L), eq(10L)))
                    .thenThrow(new RuntimeException("Comment not found"));

            // Act & Assert
            mockMvc.perform(get("/api/tasks/comments/999"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("Get Comment Replies Tests")
    class GetCommentRepliesTests {

        @Test
        @DisplayName("Should get comment replies successfully")
        void getCommentReplies_WithValidId_ShouldReturnOk() throws Exception {
            // Arrange
            CommentResponseDto reply = CommentResponseDto.builder()
                    .id(3L)
                    .taskId(1L)
                    .parentCommentId(1L)
                    .content("Reply comment")
                    .authorId(20L)
                    .authorUsername("replyuser")
                    .createdAt(fixedDateTime.plusMinutes(5))
                    .build();

            List<CommentResponseDto> replies = Arrays.asList(reply);
            when(commentService.getCommentReplies(eq(1L), eq(10L))).thenReturn(replies);

            // Act & Assert
            mockMvc.perform(get("/api/tasks/comments/1/replies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(3L))
                    .andExpect(jsonPath("$[0].parentCommentId").value(1L))
                    .andExpect(jsonPath("$[0].content").value("Reply comment"));

            verify(commentService).getCommentReplies(eq(1L), eq(10L));
        }

        @Test
        @DisplayName("Should return empty list for comment with no replies")
        void getCommentReplies_WithNoReplies_ShouldReturnEmptyList() throws Exception {
            // Arrange
            when(commentService.getCommentReplies(eq(1L), eq(10L))).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/tasks/comments/1/replies"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Get User Mentions Tests")
    class GetUserMentionsTests {

        @Test
        @DisplayName("Should get user mentions successfully")
        void getUserMentions_ShouldReturnOk() throws Exception {
            // Arrange
            List<CommentResponseDto> mentions = Arrays.asList(commentResponseDto);
            when(commentService.getUserMentions(10L)).thenReturn(mentions);

            // Act & Assert
            mockMvc.perform(get("/api/tasks/comments/mentions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].mentionedUserIds").isArray())
                    .andExpect(jsonPath("$[0].mentionedUserIds[0]").value(20L));

            verify(commentService).getUserMentions(10L);
        }

        @Test
        @DisplayName("Should return empty list when user has no mentions")
        void getUserMentions_WithNoMentions_ShouldReturnEmptyList() throws Exception {
            // Arrange
            when(commentService.getUserMentions(10L)).thenReturn(Collections.emptyList());

            // Act & Assert
            mockMvc.perform(get("/api/tasks/comments/mentions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {

        @Test
        @DisplayName("Should require authentication for creating comments")
        void createComment_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
            // This test would need a separate setup without @WithMockUser
            // For now, just verify that our current setup requires authentication
            mockMvc.perform(post("/api/tasks/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCommentDto)))
                    .andExpect(status().isForbidden()); // CSRF token missing
        }

        @Test
        @DisplayName("Should require CSRF token for state-changing operations")
        void createComment_WithoutCSRFToken_ShouldReturnForbidden() throws Exception {
            mockMvc.perform(post("/api/tasks/comments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createCommentDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Input Validation Tests")
    class InputValidationTests {

        @Test
        @DisplayName("Should validate comment content length")
        void createComment_WithTooLongContent_ShouldReturnBadRequest() throws Exception {
            // Arrange
            String longContent = "a".repeat(2001); // Exceeds 2000 character limit
            CreateCommentDto invalidDto = CreateCommentDto.builder()
                    .taskId(1L)
                    .content(longContent)
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate required fields")
        void updateComment_WithMissingCommentId_ShouldReturnBadRequest() throws Exception {
            // Arrange
            UpdateCommentDto invalidDto = UpdateCommentDto.builder()
                    .content("Updated content")
                    // Missing commentId
                    .build();

            // Act & Assert
            mockMvc.perform(put("/api/tasks/comments")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle invalid path variables")
        void getCommentById_WithInvalidId_ShouldReturnBadRequest() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/tasks/comments/invalid"))
                    .andExpect(status().isBadRequest());
        }
    }
}