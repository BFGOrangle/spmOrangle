package com.spmorangle.crm.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAlreadyExistsException;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAssignmentNotFoundException;
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
import org.springframework.test.context.ActiveProfiles;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TaskManagementController.class, GlobalExceptionHandler.class})
@DisplayName("TaskManagementController Tests")
@WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
public class TaskManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollaboratorService collaboratorService;

    @MockBean
    private TaskService taskService;

    private AddCollaboratorRequestDto validAddRequest;
    private RemoveCollaboratorRequestDto validRemoveRequest;
    private AddCollaboratorResponseDto responseDto;

    @BeforeEach
    void setUp() {
        validAddRequest = new AddCollaboratorRequestDto(1L, 2L, 3L);
        validRemoveRequest = new RemoveCollaboratorRequestDto(1L, 2L, 3L);
        responseDto = AddCollaboratorResponseDto.builder()
                .taskId(1L)
                .collaboratorId(2L)
                .assignedById(3L)
                .assignedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Add Collaborator Tests")
    class AddCollaboratorTests {

        @Test
        @DisplayName("Should successfully add collaborator and return 201 with response")
        void addCollaborator_ValidRequest_ReturnsCreatedWithResponse() throws Exception {
            // Given
            when(collaboratorService.addCollaborator(any(AddCollaboratorRequestDto.class)))
                    .thenReturn(responseDto);

            // When & Then
            mockMvc.perform(post("/api/tasks/collaborator")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAddRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.taskId").value(1L))
                    .andExpect(jsonPath("$.collaboratorId").value(2L))
                    .andExpect(jsonPath("$.assignedById").value(3L))
                    .andExpect(jsonPath("$.assignedAt").exists());
        }

        @Test
        @DisplayName("Should return 409 when collaborator already exists")
        void addCollaborator_CollaboratorAlreadyExists_ReturnsConflict() throws Exception {
            // Given
            when(collaboratorService.addCollaborator(any(AddCollaboratorRequestDto.class)))
                    .thenThrow(new CollaboratorAlreadyExistsException(1L, 2L));

            // When & Then
            mockMvc.perform(post("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validAddRequest)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 when task ID is null")
        void addCollaborator_NullTaskId_ReturnsBadRequest() throws Exception {
            // Given
            AddCollaboratorRequestDto invalidRequest = new AddCollaboratorRequestDto(null, 2L, 3L);

            // When & Then
            mockMvc.perform(post("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when collaborator ID is null")
        void addCollaborator_NullCollaboratorId_ReturnsBadRequest() throws Exception {
            // Given
            AddCollaboratorRequestDto invalidRequest = new AddCollaboratorRequestDto(1L, null, 3L);

            // When & Then
            mockMvc.perform(post("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when assigned by ID is null")
        void addCollaborator_NullAssignedById_ReturnsBadRequest() throws Exception {
            // Given
            AddCollaboratorRequestDto invalidRequest = new AddCollaboratorRequestDto(1L, 2L, null);

            // When & Then
            mockMvc.perform(post("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when task ID is zero")
        void addCollaborator_ZeroTaskId_ReturnsBadRequest() throws Exception {
            // Given
            AddCollaboratorRequestDto invalidRequest = new AddCollaboratorRequestDto(0L, 2L, 3L);

            // When & Then
            mockMvc.perform(post("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void addCollaborator_EmptyRequestBody_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed")
        void addCollaborator_MalformedRequestBody_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Remove Collaborator Tests")
    class RemoveCollaboratorTests {

        @Test
        @DisplayName("Should successfully remove collaborator and return 204")
        void removeCollaborator_ValidRequest_ReturnsNoContent() throws Exception {
            // Given
            doNothing().when(collaboratorService).removeCollaborator(any(RemoveCollaboratorRequestDto.class));

            // When & Then
            mockMvc.perform(delete("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRemoveRequest)))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when collaborator assignment not found")
        void removeCollaborator_AssignmentNotFound_ReturnsNotFound() throws Exception {
            // Given
            doThrow(new CollaboratorAssignmentNotFoundException(1L, 2L))
                    .when(collaboratorService).removeCollaborator(any(RemoveCollaboratorRequestDto.class));

            // When & Then
            mockMvc.perform(delete("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRemoveRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 400 when task ID is null")
        void removeCollaborator_NullTaskId_ReturnsBadRequest() throws Exception {
            // Given
            RemoveCollaboratorRequestDto invalidRequest = new RemoveCollaboratorRequestDto(null, 2L, 3L);

            // When & Then
            mockMvc.perform(delete("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when collaborator ID is null")
        void removeCollaborator_NullCollaboratorId_ReturnsBadRequest() throws Exception {
            // Given
            RemoveCollaboratorRequestDto invalidRequest = new RemoveCollaboratorRequestDto(1L, null, 3L);

            // When & Then
            mockMvc.perform(delete("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when assigned by ID is null")
        void removeCollaborator_NullAssignedById_ReturnsBadRequest() throws Exception {
            // Given
            RemoveCollaboratorRequestDto invalidRequest = new RemoveCollaboratorRequestDto(1L, 2L, null);

            // When & Then
            mockMvc.perform(delete("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when collaborator ID is zero")
        void removeCollaborator_ZeroCollaboratorId_ReturnsBadRequest() throws Exception {
            // Given
            RemoveCollaboratorRequestDto invalidRequest = new RemoveCollaboratorRequestDto(1L, 0L, 3L);

            // When & Then
            mockMvc.perform(delete("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void removeCollaborator_EmptyRequestBody_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/tasks/collaborator").with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }
}
