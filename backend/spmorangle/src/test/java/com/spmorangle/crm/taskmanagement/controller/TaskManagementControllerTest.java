package com.spmorangle.crm.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateTaskResponseDto;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.CommentService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAlreadyExistsException;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAssignmentNotFoundException;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

@WebMvcTest({TaskManagementController.class, GlobalExceptionHandler.class})
@EnableMethodSecurity
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

    @MockBean
    private UserContextService userContextService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserManagementService userManagementService;

    private AddCollaboratorRequestDto validAddRequest;
    private RemoveCollaboratorRequestDto validRemoveRequest;
    private AddCollaboratorResponseDto responseDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        validAddRequest = AddCollaboratorRequestDto.builder()
                .taskId(1L)
                .collaboratorId(2L)
                .assignedById(3L)
                .build();
        validRemoveRequest = new RemoveCollaboratorRequestDto(1L, 2L, 3L);
        responseDto = AddCollaboratorResponseDto.builder()
                .taskId(1L)
                .collaboratorId(2L)
                .assignedById(3L)
                .assignedAt(OffsetDateTime.now())
                .build();

        testUser = new User();
        testUser.setId(123L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoleType("USER");
        testUser.setCognitoSub(UUID.randomUUID());
    }

    @Nested
    @DisplayName("Get Tasks Tests")
    class GetTasksTests {

        @Test
        @DisplayName("Should successfully return tasks for authenticated user")
        void getTasks_AuthenticatedUser_ReturnsTasksWithFound() throws Exception {
            // Given
            List<TaskResponseDto> expectedTasks = Arrays.asList(
                    TaskResponseDto.builder()
                            .id(1L)
                            .projectId(101L)
                            .ownerId(123L)
                            .title("Task 1")
                            .description("Description 1")
                            .status(Status.TODO)
                            .tags(Arrays.asList("tag1", "tag2"))
                            .createdBy(123L)
                            .createdAt(OffsetDateTime.now())

                            .build(),
                    TaskResponseDto.builder()
                            .id(2L)
                            .projectId(102L)
                            .ownerId(123L)
                            .title("Task 2")
                            .description("Description 2")
                            .status(Status.IN_PROGRESS)
                            .tags(Collections.singletonList("tag3"))
                            .createdBy(123L)
                            .createdAt(OffsetDateTime.now())

                            .build()
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(expectedTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].projectId").value(101L))
                    .andExpect(jsonPath("$[0].ownerId").value(123L))
                    .andExpect(jsonPath("$[0].title").value("Task 1"))
                    .andExpect(jsonPath("$[0].description").value("Description 1"))
                    .andExpect(jsonPath("$[0].status").value("TODO"))
                    .andExpect(jsonPath("$[0].tags").isArray())
                    .andExpect(jsonPath("$[0].tags.length()").value(2))
                    .andExpect(jsonPath("$[0].tags[0]").value("tag1"))
                    .andExpect(jsonPath("$[0].tags[1]").value("tag2"))
                    .andExpect(jsonPath("$[0].createdBy").value(123L))
                    .andExpect(jsonPath("$[0].createdAt").exists())
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].title").value("Task 2"))
                    .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$[1].tags.length()").value(1));
        }

        @Test
        @DisplayName("Should return empty array when user has no tasks")
        void getTasks_UserWithNoTasks_ReturnsEmptyArrayWithFound() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should handle single task correctly")
        void getTasks_SingleTask_ReturnsSingleTaskArray() throws Exception {
            // Given
            List<TaskResponseDto> singleTask = Collections.singletonList(
                    TaskResponseDto.builder()
                            .id(1L)
                            .projectId(101L)
                            .ownerId(123L)
                            .title("Single Task")
                            .description("Single Description")
                            .status(Status.COMPLETED)
                            .tags(Collections.emptyList())
                            .createdBy(123L)
                            .createdAt(OffsetDateTime.now())

                            .build()
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(singleTask);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Single Task"))
                    .andExpect(jsonPath("$[0].status").value("COMPLETED"));
        }

        @Test
        @DisplayName("Should handle tasks with null description correctly")
        void getTasks_TaskWithNullDescription_HandlesCorrectly() throws Exception {
            // Given
            List<TaskResponseDto> taskWithNullDesc = Collections.singletonList(
                    TaskResponseDto.builder()
                            .id(1L)
                            .projectId(101L)
                            .ownerId(123L)
                            .title("Task with null desc")
                            .description(null)
                            .status(Status.TODO)
                            .tags(Collections.emptyList())
                            .createdBy(123L)
                            .createdAt(OffsetDateTime.now())

                            .build()
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(taskWithNullDesc);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].title").value("Task with null desc"))
                    .andExpect(jsonPath("$[0].description").doesNotExist());
        }

        @Test
        @DisplayName("Should handle tasks with all status types")
        void getTasks_TasksWithAllStatuses_HandlesCorrectly() throws Exception {
            // Given
            List<TaskResponseDto> tasksWithAllStatuses = Arrays.asList(
                    createTaskResponseDto(1L, "TODO Task", Status.TODO),
                    createTaskResponseDto(2L, "In Progress Task", Status.IN_PROGRESS),
                    createTaskResponseDto(3L, "Completed Task", Status.COMPLETED),
                    createTaskResponseDto(4L, "Blocked Task", Status.BLOCKED)
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(tasksWithAllStatuses);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(4))
                    .andExpect(jsonPath("$[0].status").value("TODO"))
                    .andExpect(jsonPath("$[1].status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$[2].status").value("COMPLETED"))
                    .andExpect(jsonPath("$[3].status").value("BLOCKED"));
        }

        @Test
        @DisplayName("Should verify service is called with correct user ID")
        void getTasks_VerifyServiceCall_CallsWithCorrectUserId() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(Collections.emptyList());

            // When
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then
            verify(userContextService).getRequestingUser();
            verify(taskService).getAllUserTasks(eq(123L));
        }

        @Test
        @DisplayName("Should return 200 OK status")
        void getTasks_StatusCode_ReturnsOk() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        private TaskResponseDto createTaskResponseDto(Long id, String title, Status status) {
            return TaskResponseDto.builder()
                    .id(id)
                    .projectId(101L)
                    .ownerId(123L)
                    .title(title)
                    .description("Description for " + title)
                    .status(status)
                    .tags(Collections.emptyList())
                    .createdBy(123L)
                    .createdAt(OffsetDateTime.now())
                    .build();
        }
    }

    @Nested
    @DisplayName("Get Tasks Content Validation Tests")
    class GetTasksContentValidationTests {

        @Test
        @DisplayName("Should handle tasks with special characters in JSON response")
        void getTasks_TasksWithSpecialCharacters_ReturnsValidJson() throws Exception {
            // Given
            String titleWithSpecialChars = "Task with \"quotes\" and \\backslashes\\ and /slashes/ and \n newlines";
            String descriptionWithUnicode = "Description with émojis 🚀 and unicode characters: 你好世界";

            List<TaskResponseDto> tasksWithSpecialContent = Collections.singletonList(
                    TaskResponseDto.builder()
                            .id(1L)
                            .projectId(101L)
                            .ownerId(123L)
                            .title(titleWithSpecialChars)
                            .description(descriptionWithUnicode)
                            .status(Status.TODO)
                            .tags(Arrays.asList("tag with spaces", "tag\"with\"quotes", "tag\\with\\backslashes"))
                            .createdBy(123L)
                            .createdAt(OffsetDateTime.now())

                            .build()
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(tasksWithSpecialContent);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].title").value(titleWithSpecialChars))
                    .andExpect(jsonPath("$[0].description").value(descriptionWithUnicode))
                    .andExpect(jsonPath("$[0].tags[0]").value("tag with spaces"))
                    .andExpect(jsonPath("$[0].tags[1]").value("tag\"with\"quotes"))
                    .andExpect(jsonPath("$[0].tags[2]").value("tag\\with\\backslashes"));
        }

        @Test
        @DisplayName("Should handle empty string values correctly")
        void getTasks_TasksWithEmptyStringValues_HandlesCorrectly() throws Exception {
            // Given
            List<TaskResponseDto> tasksWithEmptyStrings = Collections.singletonList(
                    TaskResponseDto.builder()
                            .id(1L)
                            .projectId(101L)
                            .ownerId(123L)
                            .title("")
                            .description("")
                            .status(Status.TODO)
                            .tags(Arrays.asList("", "valid", ""))
                            .createdBy(123L)
                            .createdAt(OffsetDateTime.now())

                            .build()
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(tasksWithEmptyStrings);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].title").value(""))
                    .andExpect(jsonPath("$[0].description").value(""))
                    .andExpect(jsonPath("$[0].tags[0]").value(""))
                    .andExpect(jsonPath("$[0].tags[1]").value("valid"))
                    .andExpect(jsonPath("$[0].tags[2]").value(""));
        }

        @Test
        @DisplayName("Should validate response structure matches DTO contract")
        void getTasks_ResponseStructure_MatchesDtoContract() throws Exception {
            // Given
            OffsetDateTime fixedTime = OffsetDateTime.now();
            List<TaskResponseDto> validTask = Collections.singletonList(
                    TaskResponseDto.builder()
                            .id(1L)
                            .projectId(101L)
                            .ownerId(123L)
                            .title("Valid Task")
                            .description("Valid Description")
                            .status(Status.TODO)
                            .tags(Arrays.asList("tag1", "tag2"))
                            .createdBy(123L)
                            .createdAt(fixedTime)
                            .build()
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(validTask);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0]").exists())
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].projectId").isNumber())
                    .andExpect(jsonPath("$[0].ownerId").isNumber())
                    .andExpect(jsonPath("$[0].title").isString())
                    .andExpect(jsonPath("$[0].description").isString())
                    .andExpect(jsonPath("$[0].status").isString())
                    .andExpect(jsonPath("$[0].tags").isArray())
                    .andExpect(jsonPath("$[0].createdBy").isNumber())
                    .andExpect(jsonPath("$[0].createdAt").isString())
;
        }
    }

    @Nested
    @DisplayName("Create Task Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should successfully create task and return 201 with response")
        void createTask_ValidRequest_ReturnsCreatedWithResponse() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(123L)
                    .title("New Task")
                    .description("Task description")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .tags(Arrays.asList("tag1", "tag2"))
                    .build();

            CreateTaskResponseDto responseDto = CreateTaskResponseDto.builder()
                    .id(1L)
                    .projectId(101L)
                    .ownerId(123L)
                    .title("New Task")
                    .description("Task description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .createdBy(123L)
                    .createdAt(OffsetDateTime.now())
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.createTask(any(CreateTaskDto.class), eq(123L))).thenReturn(responseDto);

            // When & Then
            mockMvc.perform(post("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.projectId").value(101L))
                    .andExpect(jsonPath("$.title").value("New Task"))
                    .andExpect(jsonPath("$.description").value("Task description"))
                    .andExpect(jsonPath("$.status").value("TODO"));
        }

        @Test
        @DisplayName("Should return 400 when task title is null")
        void createTask_NullTitle_ReturnsBadRequest() throws Exception {
            // Given
            CreateTaskDto invalidRequest = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(123L)
                    .title(null)
                    .description("Description")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(post("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Create Task with Specified Owner Tests")
    @WithMockUser(username = "test@example.com", authorities = {"ROLE_MANAGER"})
    class CreateTaskWithSpecifiedOwnerTests {

        @Test
        @DisplayName("Should successfully create task with specified owner when user has MANAGER role")
        void createTaskWithSpecifiedOwner_ValidRequest_ReturnsCreatedWithResponse() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(456L) // Specified owner
                    .title("Task with Specified Owner")
                    .description("Task description")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .tags(Arrays.asList("tag1", "tag2"))
                    .assignedUserIds(Arrays.asList(789L, 101L))
                    .build();

            CreateTaskResponseDto responseDto = CreateTaskResponseDto.builder()
                    .id(1L)
                    .projectId(101L)
                    .ownerId(456L) // Owner is the specified user
                    .title("Task with Specified Owner")
                    .description("Task description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .createdBy(456L)
                    .createdAt(OffsetDateTime.now())
                    .assignedUserIds(Arrays.asList(789L, 101L))
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.createTask(any(CreateTaskDto.class), eq(456L), eq(123L))).thenReturn(responseDto);

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.projectId").value(101L))
                    .andExpect(jsonPath("$.ownerId").value(456L))
                    .andExpect(jsonPath("$.title").value("Task with Specified Owner"))
                    .andExpect(jsonPath("$.description").value("Task description"))
                    .andExpect(jsonPath("$.status").value("TODO"))
                    .andExpect(jsonPath("$.taskType").value("FEATURE"))
                    .andExpect(jsonPath("$.createdBy").value(456L))
                    .andExpect(jsonPath("$.assignedUserIds[0]").value(789L))
                    .andExpect(jsonPath("$.assignedUserIds[1]").value(101L));

            verify(taskService).createTask(any(CreateTaskDto.class), eq(456L), eq(123L));
        }

        @Test
        @DisplayName("Should return 400 when ownerId is null in request")
        void createTaskWithSpecifiedOwner_NullOwnerId_ReturnsBadRequest() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(null) // Null owner ID should cause validation error
                    .title("Task with Null Owner")
                    .description("Task description")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isBadRequest());

            verify(taskService, never()).createTask(any(CreateTaskDto.class), any(Long.class), any(Long.class));
        }

        @Test
        @DisplayName("Should return 400 when ownerId is missing in request")
        void createTaskWithSpecifiedOwner_MissingOwnerId_ReturnsBadRequest() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    // ownerId is missing
                    .title("Task without Owner")
                    .description("Task description")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isBadRequest());

            verify(taskService, never()).createTask(any(CreateTaskDto.class), any(Long.class), any(Long.class));
        }

        @Test
        @DisplayName("Should return 403 when user does not have MANAGER role")
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
        void createTaskWithSpecifiedOwner_NonManagerRole_ReturnsForbidden() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(456L)
                    .title("Task with Specified Owner")
                    .description("Task description")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isForbidden());

            verify(taskService, never()).createTask(any(CreateTaskDto.class), any(Long.class), any(Long.class));
            verify(userContextService, never()).getRequestingUser();
        }

        @Test
        @DisplayName("Should handle service exception gracefully")
        void createTaskWithSpecifiedOwner_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(456L)
                    .title("Task with Specified Owner")
                    .description("Task description")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.createTask(any(CreateTaskDto.class), eq(456L), eq(123L)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isInternalServerError());

            verify(taskService).createTask(any(CreateTaskDto.class), eq(456L), eq(123L));
        }

        @Test
        @DisplayName("Should handle invalid JSON in request body")
        void createTaskWithSpecifiedOwner_InvalidJson_ReturnsBadRequest() throws Exception {
            // Given
            String invalidJson = "{ invalid json }";

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest());

            verify(taskService, never()).createTask(any(CreateTaskDto.class), any(Long.class), any(Long.class));
            verify(userContextService, never()).getRequestingUser();
        }

        @Test
        @DisplayName("Should validate required fields in CreateTaskDto")
        void createTaskWithSpecifiedOwner_MissingRequiredFields_ReturnsBadRequest() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .ownerId(456L)
                    // Missing required fields like title, taskType
                    .description("Task description")
                    .status(Status.TODO)
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isBadRequest());

            verify(taskService, never()).createTask(any(CreateTaskDto.class), any(Long.class), any(Long.class));
        }

        @Test
        @DisplayName("Should handle minimal valid request correctly")
        void createTaskWithSpecifiedOwner_MinimalValidRequest_ReturnsCreated() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .ownerId(456L)
                    .title("Minimal Task")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .build();

            CreateTaskResponseDto responseDto = CreateTaskResponseDto.builder()
                    .id(1L)
                    .ownerId(456L)
                    .title("Minimal Task")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .createdBy(456L)
                    .createdAt(OffsetDateTime.now())
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.createTask(any(CreateTaskDto.class), eq(456L), eq(123L))).thenReturn(responseDto);

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.ownerId").value(456L))
                    .andExpect(jsonPath("$.title").value("Minimal Task"))
                    .andExpect(jsonPath("$.taskType").value("FEATURE"))
                    .andExpect(jsonPath("$.status").value("TODO"));

            verify(taskService).createTask(any(CreateTaskDto.class), eq(456L), eq(123L));
        }

        @Test
        @DisplayName("Should log task creation with correct user ID")
        void createTaskWithSpecifiedOwner_ValidRequest_LogsCorrectUserId() throws Exception {
            // Given
            CreateTaskDto createTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(456L)
                    .title("Logged Task")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .build();

            CreateTaskResponseDto responseDto = CreateTaskResponseDto.builder()
                    .id(1L)
                    .ownerId(456L)
                    .title("Logged Task")
                    .taskType(TaskType.FEATURE)
                    .status(Status.TODO)
                    .createdBy(456L)
                    .createdAt(OffsetDateTime.now())
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.createTask(any(CreateTaskDto.class), eq(456L), eq(123L))).thenReturn(responseDto);

            // When & Then
            mockMvc.perform(post("/api/tasks/with-owner-id")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createTaskDto)))
                    .andExpect(status().isCreated());

            // Verify that the correct user ID (123L from testUser) is passed to the service
            verify(taskService).createTask(any(CreateTaskDto.class), eq(456L), eq(123L));
            verify(userContextService).getRequestingUser();
        }
    }

    @Nested
    @DisplayName("Get Project Tasks Tests")
    class GetProjectTasksTests {

        private TaskResponseDto createTaskResponseDto(Long id, String title, Status status) {
            return TaskResponseDto.builder()
                    .id(id)
                    .projectId(101L)
                    .ownerId(123L)
                    .title(title)
                    .description("Description for " + title)
                    .status(status)
                    .tags(Collections.emptyList())
                    .createdBy(123L)
                    .createdAt(OffsetDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Should return tasks for valid project ID")
        void getProjectTasks_ValidProjectId_ReturnsTaskList() throws Exception {
            // Given
            Long projectId = 101L;
            List<TaskResponseDto> projectTasks = Arrays.asList(
                    createTaskResponseDto(1L, "Project Task 1", Status.TODO),
                    createTaskResponseDto(2L, "Project Task 2", Status.IN_PROGRESS)
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getProjectTasks(eq(testUser.getId()), eq(projectId))).thenReturn(projectTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/project/{projectId}", projectId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].title").value("Project Task 1"))
                    .andExpect(jsonPath("$[1].title").value("Project Task 2"));
        }
    }

    @Nested
    @DisplayName("Get Personal Tasks Tests")
    class GetPersonalTasksTests {

        private TaskResponseDto createTaskResponseDto(Long id, String title, Status status) {
            return TaskResponseDto.builder()
                    .id(id)
                    .projectId(101L)
                    .ownerId(123L)
                    .title(title)
                    .description("Description for " + title)
                    .status(status)
                    .tags(Collections.emptyList())
                    .createdBy(123L)
                    .createdAt(OffsetDateTime.now())
                    .build();
        }

        @Test
        @DisplayName("Should return personal tasks for authenticated user")
        void getPersonalTasks_AuthenticatedUser_ReturnsPersonalTasks() throws Exception {
            // Given
            List<TaskResponseDto> personalTasks = Arrays.asList(
                    createTaskResponseDto(1L, "Personal Task 1", Status.TODO)
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getPersonalTasks(eq(123L))).thenReturn(personalTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/personal")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].title").value("Personal Task 1"));
        }
    }

    @Nested
    @DisplayName("Delete Task Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should successfully delete task and return 204")
        void deleteTask_ValidTaskId_ReturnsNoContent() throws Exception {
            // Given
            Long taskId = 1L;
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            doNothing().when(taskService).deleteTask(eq(taskId), eq(123L));

            // When & Then
            mockMvc.perform(delete("/api/tasks/{taskId}", taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(taskService).deleteTask(eq(taskId), eq(123L));
        }
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
            AddCollaboratorRequestDto invalidRequest = AddCollaboratorRequestDto.builder()
                    .taskId(null)
                    .collaboratorId(2L)
                    .assignedById(3L)
                    .build();

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
            AddCollaboratorRequestDto invalidRequest = AddCollaboratorRequestDto.builder()
                    .taskId(1L)
                    .collaboratorId(null)
                    .assignedById(3L)
                    .build();

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
            AddCollaboratorRequestDto invalidRequest = AddCollaboratorRequestDto.builder()
                    .taskId(1L)
                    .collaboratorId(2L)
                    .assignedById(null)
                    .build();

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
            AddCollaboratorRequestDto invalidRequest = AddCollaboratorRequestDto.builder()
                    .taskId(0L)
                    .collaboratorId(2L)
                    .assignedById(3L)
                    .build();

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
                            .content("{\"invalid\": \"json\"}"))
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
            RemoveCollaboratorRequestDto invalidRequest = new RemoveCollaboratorRequestDto();
            invalidRequest.setTaskId(null);
            invalidRequest.setCollaboratorId(2L);
            invalidRequest.setAssignedById(3L);

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
            RemoveCollaboratorRequestDto invalidRequest = new RemoveCollaboratorRequestDto();
            invalidRequest.setTaskId(1L);
            invalidRequest.setCollaboratorId(null);
            invalidRequest.setAssignedById(3L);

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
            RemoveCollaboratorRequestDto invalidRequest = new RemoveCollaboratorRequestDto();
            invalidRequest.setTaskId(1L);
            invalidRequest.setCollaboratorId(2L);
            invalidRequest.setAssignedById(null);

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
            RemoveCollaboratorRequestDto invalidRequest = new RemoveCollaboratorRequestDto();
            invalidRequest.setTaskId(1L);
            invalidRequest.setCollaboratorId(0L);
            invalidRequest.setAssignedById(3L);

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

    @Nested
    @DisplayName("Update Task Tests")
    class UpdateTaskTests {

        @Test
        @DisplayName("Should successfully update task and return 200")
        void updateTask_ValidRequest_ReturnsOk() throws Exception {
            // Given
            UpdateTaskDto updateTaskDto = UpdateTaskDto.builder()
                    .taskId(1L)
                    .title("Updated Title")
                    .description("Updated Description")
                    .status(Status.IN_PROGRESS)
                    .taskType(TaskType.BUG)
                    .tags(Arrays.asList("tag1", "tag2"))
                    .build();

            UpdateTaskResponseDto responseDto = UpdateTaskResponseDto.builder()
                    .id(1L)
                    .projectId(101L)
                    .ownerId(123L)
                    .title("Updated Title")
                    .description("Updated Description")
                    .status(Status.IN_PROGRESS)
                    .taskType(TaskType.BUG)
                    .tags(Arrays.asList("tag1", "tag2"))
                    .userHasEditAccess(true)
                    .userHasDeleteAccess(false)
                    .updatedBy(123L)
                    .updatedAt(OffsetDateTime.now())
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.updateTask(any(UpdateTaskDto.class), eq(123L))).thenReturn(responseDto);

            // When & Then
            mockMvc.perform(put("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateTaskDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.title").value("Updated Title"))
                    .andExpect(jsonPath("$.description").value("Updated Description"))
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.taskType").value("BUG"))
                    .andExpect(jsonPath("$.updatedBy").value(123L));

            verify(taskService).updateTask(any(UpdateTaskDto.class), eq(123L));
        }

        @Test
        @DisplayName("Should return 400 when task ID is null")
        void updateTask_NullTaskId_ReturnsBadRequest() throws Exception {
            // Given
            UpdateTaskDto invalidRequest = UpdateTaskDto.builder()
                    .taskId(null)
                    .title("Updated Title")
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(put("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle partial update successfully")
        void updateTask_PartialUpdate_ReturnsOk() throws Exception {
            // Given
            UpdateTaskDto partialUpdateDto = UpdateTaskDto.builder()
                    .taskId(1L)
                    .title("Only Title Updated")
                    .build();

            UpdateTaskResponseDto responseDto = UpdateTaskResponseDto.builder()
                    .id(1L)
                    .projectId(101L)
                    .ownerId(123L)
                    .title("Only Title Updated")
                    .description("Original Description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .userHasEditAccess(true)
                    .userHasDeleteAccess(false)
                    .updatedBy(123L)
                    .updatedAt(OffsetDateTime.now())
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.updateTask(any(UpdateTaskDto.class), eq(123L))).thenReturn(responseDto);

            // When & Then
            mockMvc.perform(put("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(partialUpdateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Only Title Updated"))
                    .andExpect(jsonPath("$.description").value("Original Description"));
        }

        @Test
        @DisplayName("Should return 500 when service throws exception")
        void updateTask_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
            // Given
            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                    .taskId(1L)
                    .title("Updated Title")
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.updateTask(any(UpdateTaskDto.class), eq(123L)))
                    .thenThrow(new RuntimeException("Update failed"));

            // When & Then
            mockMvc.perform(put("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("Should handle tag updates correctly")
        void updateTask_WithTags_ReturnsUpdatedTags() throws Exception {
            // Given
            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                    .taskId(1L)
                    .tags(Arrays.asList("new-tag1", "new-tag2", "new-tag3"))
                    .build();

            UpdateTaskResponseDto responseDto = UpdateTaskResponseDto.builder()
                    .id(1L)
                    .projectId(101L)
                    .ownerId(123L)
                    .title("Task Title")
                    .description("Task Description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .tags(Arrays.asList("new-tag1", "new-tag2", "new-tag3"))
                    .userHasEditAccess(true)
                    .userHasDeleteAccess(false)
                    .updatedBy(123L)
                    .updatedAt(OffsetDateTime.now())
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.updateTask(any(UpdateTaskDto.class), eq(123L))).thenReturn(responseDto);

            // When & Then
            mockMvc.perform(put("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.tags.length()").value(3))
                    .andExpect(jsonPath("$.tags[0]").value("new-tag1"))
                    .andExpect(jsonPath("$.tags[1]").value("new-tag2"))
                    .andExpect(jsonPath("$.tags[2]").value("new-tag3"));
        }

        @Test
        @DisplayName("Should return 400 for invalid JSON")
        void updateTask_InvalidJson_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json}"))
                    .andExpect(status().isBadRequest());

            verify(taskService, never()).updateTask(any(UpdateTaskDto.class), any(Long.class));
        }

        @Test
        @DisplayName("Should verify service is called with correct user ID")
        void updateTask_VerifyServiceCall_UsesCorrectUserId() throws Exception {
            // Given
            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                    .taskId(1L)
                    .title("Updated Title")
                    .build();

            UpdateTaskResponseDto responseDto = UpdateTaskResponseDto.builder()
                    .id(1L)
                    .projectId(101L)
                    .ownerId(123L)
                    .title("Updated Title")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .updatedBy(123L)
                    .updatedAt(OffsetDateTime.now())
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.updateTask(any(UpdateTaskDto.class), eq(123L))).thenReturn(responseDto);

            // When
            mockMvc.perform(put("/api/tasks")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk());

            // Then
            verify(userContextService).getRequestingUser();
            verify(taskService).updateTask(any(UpdateTaskDto.class), eq(123L));
        }
    }

    @Nested
    @DisplayName("Get Collaborators Tests")
    class GetCollaboratorsTests {

        @Test
        @DisplayName("Should successfully return list of collaborators")
        void getCollaborators_ValidRequest_ReturnsCollaboratorsList() throws Exception {
            // Given
            List<UserResponseDto> collaborators = Arrays.asList(
                    UserResponseDto.builder()
                            .id(1L)
                            .username("user1")
                            .email("user1@example.com")
                            .roleType("STAFF")
                            .build(),
                    UserResponseDto.builder()
                            .id(2L)
                            .username("user2")
                            .email("user2@example.com")
                            .roleType("STAFF")
                            .build()
            );

            when(userManagementService.getCollaborators()).thenReturn(collaborators);

            // When & Then
            mockMvc.perform(get("/api/tasks/collaborators")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].username").value("user1"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].username").value("user2"));

            verify(userManagementService).getCollaborators();
        }

        @Test
        @DisplayName("Should return empty array when no collaborators")
        void getCollaborators_NoCollaborators_ReturnsEmptyArray() throws Exception {
            // Given
            when(userManagementService.getCollaborators()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/tasks/collaborators")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(userManagementService).getCollaborators();
        }
    }
}
