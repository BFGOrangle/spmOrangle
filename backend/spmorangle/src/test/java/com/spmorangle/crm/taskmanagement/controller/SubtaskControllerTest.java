package com.spmorangle.crm.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.CreateSubtaskDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateSubtaskDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.service.CommentService;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SubtaskController.class)
@DisplayName("SubtaskController Tests")
@WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
public class SubtaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private SubtaskService subtaskService;

    @MockBean
    private UserContextService userContextService;

    private User testUser;
    private CreateSubtaskDto createSubtaskDto;
    private UpdateSubtaskDto updateSubtaskDto;
    private SubtaskResponseDto subtaskResponseDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(123L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoleType("USER");
        testUser.setCognitoSub(UUID.randomUUID());

        createSubtaskDto = CreateSubtaskDto.builder()
                .taskId(1L)
                .projectId(101L)
                .title("Test Subtask")
                .details("Test subtask details")
                .status(Status.TODO)
                .taskType(TaskType.FEATURE)
                .build();

        updateSubtaskDto = UpdateSubtaskDto.builder()
                .title("Updated Subtask")
                .details("Updated details")
                .status(Status.IN_PROGRESS)
                .taskType(TaskType.FEATURE)
                .build();

        subtaskResponseDto = SubtaskResponseDto.builder()
                .id(1L)
                .taskId(1L)
                .projectId(101L)
                .title("Test Subtask")
                .details("Test subtask details")
                .status(Status.TODO)
                .taskType(TaskType.FEATURE)
                .createdBy(123L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .updatedBy(123L)
                .build();
    }

    @Nested
    @DisplayName("Create Subtask Tests")
    class CreateSubtaskTests {

        @Test
        @DisplayName("Should successfully create subtask and return 201 with response")
        void createSubtask_ValidRequest_ReturnsCreatedWithResponse() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(subtaskService.createSubtask(any(CreateSubtaskDto.class), eq(123L)))
                    .thenReturn(subtaskResponseDto);

            // When & Then
            mockMvc.perform(post("/api/subtasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSubtaskDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.taskId").value(1L))
                    .andExpect(jsonPath("$.title").value("Test Subtask"))
                    .andExpect(jsonPath("$.details").value("Test subtask details"))
                    .andExpect(jsonPath("$.status").value("TODO"))
                    .andExpect(jsonPath("$.createdBy").value(123L));
        }

        @Test
        @DisplayName("Should return 400 when subtask title is null")
        void createSubtask_NullTitle_ReturnsBadRequest() throws Exception {
            // Given
            CreateSubtaskDto invalidRequest = CreateSubtaskDto.builder()
                    .taskId(1L)
                    .projectId(101L)
                    .title(null)
                    .details("Details")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(post("/api/subtasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when task ID is null")
        void createSubtask_NullTaskId_ReturnsBadRequest() throws Exception {
            // Given
            CreateSubtaskDto invalidRequest = CreateSubtaskDto.builder()
                    .taskId(null)
                    .projectId(101L)
                    .title("Title")
                    .details("Details")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(post("/api/subtasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Get Subtask Tests")
    class GetSubtaskTests {

        @Test
        @DisplayName("Should return subtask for valid subtask ID")
        void getSubtask_ValidSubtaskId_ReturnsSubtask() throws Exception {
            // Given
            Long subtaskId = 1L;
            when(subtaskService.getSubtaskById(eq(subtaskId))).thenReturn(subtaskResponseDto);

            // When & Then
            mockMvc.perform(get("/api/subtasks/{subtaskId}", subtaskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Test Subtask"));
        }
    }

    @Nested
    @DisplayName("Get Subtasks By Task Tests")
    class GetSubtasksByTaskTests {

        @Test
        @DisplayName("Should return subtasks for valid task ID")
        void getSubtasksByTask_ValidTaskId_ReturnsSubtaskList() throws Exception {
            // Given
            Long taskId = 1L;
            List<SubtaskResponseDto> subtasks = Arrays.asList(
                    subtaskResponseDto,
                    SubtaskResponseDto.builder()
                            .id(2L)
                            .taskId(1L)
                            .projectId(101L)
                            .title("Subtask 2")
                            .details("Details 2")
                            .status(Status.IN_PROGRESS)
                            .taskType(TaskType.FEATURE)
                            .createdBy(123L)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .updatedBy(123L)
                            .build()
            );

            when(subtaskService.getSubtasksByTaskId(eq(taskId))).thenReturn(subtasks);

            // When & Then
            mockMvc.perform(get("/api/subtasks/task/{taskId}", taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Test Subtask"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].title").value("Subtask 2"));
        }

        @Test
        @DisplayName("Should return empty array when task has no subtasks")
        void getSubtasksByTask_TaskWithNoSubtasks_ReturnsEmptyArray() throws Exception {
            // Given
            Long taskId = 1L;
            when(subtaskService.getSubtasksByTaskId(eq(taskId))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/subtasks/task/{taskId}", taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    @Nested
    @DisplayName("Get Subtasks By Project Tests")
    class GetSubtasksByProjectTests {

        @Test
        @DisplayName("Should return subtasks for valid project ID")
        void getSubtasksByProject_ValidProjectId_ReturnsSubtaskList() throws Exception {
            // Given
            Long projectId = 101L;
            List<SubtaskResponseDto> subtasks = Collections.singletonList(subtaskResponseDto);

            when(subtaskService.getSubtasksByProjectId(eq(projectId))).thenReturn(subtasks);

            // When & Then
            mockMvc.perform(get("/api/subtasks/project/{projectId}", projectId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L));
        }
    }

    @Nested
    @DisplayName("Update Subtask Tests")
    class UpdateSubtaskTests {

        @Test
        @DisplayName("Should successfully update subtask and return 200 with response")
        void updateSubtask_ValidRequest_ReturnsOkWithResponse() throws Exception {
            // Given
            Long subtaskId = 1L;
            SubtaskResponseDto updatedSubtask = SubtaskResponseDto.builder()
                    .id(1L)
                    .taskId(1L)
                    .projectId(101L)
                    .title("Updated Subtask")
                    .details("Updated details")
                    .status(Status.IN_PROGRESS)
                    .taskType(TaskType.FEATURE)
                    .createdBy(123L)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .updatedBy(123L)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(subtaskService.updateSubtask(eq(subtaskId), any(UpdateSubtaskDto.class), eq(123L)))
                    .thenReturn(updatedSubtask);

            // When & Then
            mockMvc.perform(put("/api/subtasks/{subtaskId}", subtaskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateSubtaskDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Updated Subtask"))
                    .andExpect(jsonPath("$.details").value("Updated details"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        }

        @Test
        @DisplayName("Should handle update with empty title - validation depends on DTO")
        void updateSubtask_EmptyTitle_ValidationBehavior() throws Exception {
            // Given
            Long subtaskId = 1L;
            UpdateSubtaskDto requestWithEmptyTitle = UpdateSubtaskDto.builder()
                    .title("")
                    .details("Details")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .build();

            SubtaskResponseDto updatedSubtask = SubtaskResponseDto.builder()
                    .id(1L)
                    .taskId(1L)
                    .projectId(101L)
                    .title("")
                    .details("Details")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .createdBy(123L)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .updatedBy(123L)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(subtaskService.updateSubtask(eq(subtaskId), any(UpdateSubtaskDto.class), eq(123L)))
                    .thenReturn(updatedSubtask);

            // When & Then - The actual validation behavior depends on DTO validation annotations
            mockMvc.perform(put("/api/subtasks/{subtaskId}", subtaskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithEmptyTitle)))
                    .andExpect(status().isOk()); // Adjust based on actual validation behavior
        }
    }

    @Nested
    @DisplayName("Delete Subtask Tests")
    class DeleteSubtaskTests {

        @Test
        @DisplayName("Should successfully delete subtask and return 204")
        void deleteSubtask_ValidSubtaskId_ReturnsNoContent() throws Exception {
            // Given
            Long subtaskId = 1L;
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            doNothing().when(subtaskService).deleteSubtask(eq(subtaskId), eq(123L));

            // When & Then
            mockMvc.perform(delete("/api/subtasks/{subtaskId}", subtaskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(subtaskService).deleteSubtask(eq(subtaskId), eq(123L));
        }
    }

    @Nested
    @DisplayName("Security and Authorization Tests")
    class SecurityAndAuthorizationTests {

        @Test
        @DisplayName("Should verify user context service is called for endpoints requiring user")
        void endpointsRequiringUser_VerifyUserContextService_CallsGetRequestingUser() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(subtaskService.createSubtask(any(CreateSubtaskDto.class), eq(123L)))
                    .thenReturn(subtaskResponseDto);
            when(subtaskService.updateSubtask(eq(1L), any(UpdateSubtaskDto.class), eq(123L)))
                    .thenReturn(subtaskResponseDto);
            doNothing().when(subtaskService).deleteSubtask(eq(1L), eq(123L));

            // Test POST
            mockMvc.perform(post("/api/subtasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createSubtaskDto)))
                    .andExpect(status().isCreated());

            // Test PUT
            mockMvc.perform(put("/api/subtasks/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateSubtaskDto)))
                    .andExpect(status().isOk());

            // Test DELETE
            mockMvc.perform(delete("/api/subtasks/1").with(csrf()))
                    .andExpect(status().isNoContent());

            // Verify user context service was called for each endpoint that needs user
            verify(userContextService, org.mockito.Mockito.times(3)).getRequestingUser();
        }
    }

    @Nested
    @DisplayName("Content Validation Tests")
    class ContentValidationTests {

        @Test
        @DisplayName("Should handle subtasks with special characters in JSON response")
        void getSubtask_SubtaskWithSpecialCharacters_ReturnsValidJson() throws Exception {
            // Given
            String titleWithSpecialChars = "Subtask with \"quotes\" and \\backslashes\\ and /slashes/";
            String descriptionWithUnicode = "Description with émojis 🚀 and unicode characters: 你好世界";

            SubtaskResponseDto subtaskWithSpecialContent = SubtaskResponseDto.builder()
                    .id(1L)
                    .taskId(1L)
                    .projectId(101L)
                    .title(titleWithSpecialChars)
                    .details(descriptionWithUnicode)
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .createdBy(123L)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .updatedBy(123L)
                    .build();

            when(subtaskService.getSubtaskById(eq(1L))).thenReturn(subtaskWithSpecialContent);

            // When & Then
            mockMvc.perform(get("/api/subtasks/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.title").value(titleWithSpecialChars))
                    .andExpect(jsonPath("$.details").value(descriptionWithUnicode));
        }

        @Test
        @DisplayName("Should validate response structure matches DTO contract")
        void getSubtask_ResponseStructure_MatchesDtoContract() throws Exception {
            // Given
            when(subtaskService.getSubtaskById(eq(1L))).thenReturn(subtaskResponseDto);

            // When & Then
            mockMvc.perform(get("/api/subtasks/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.taskId").isNumber())
                    .andExpect(jsonPath("$.title").isString())
                    .andExpect(jsonPath("$.details").isString())
                    .andExpect(jsonPath("$.status").isString())
                    .andExpect(jsonPath("$.createdBy").isNumber())
                    .andExpect(jsonPath("$.createdAt").isString())
                    .andExpect(jsonPath("$.updatedAt").isString());
        }
    }
}
