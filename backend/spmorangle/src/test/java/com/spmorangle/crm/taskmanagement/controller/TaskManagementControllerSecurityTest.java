package com.spmorangle.crm.taskmanagement.controller;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TaskManagementController.class})
@DisplayName("TaskManagementController Security Tests")
class TaskManagementControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollaboratorService collaboratorService;

    @MockBean
    private TaskService taskService;

    @MockBean
    private UserContextService userContextService;

    private User testUser;
    private User anotherUser;
    private List<TaskResponseDto> testTasks;

    @BeforeEach
    void setUp() {
        testUser = createUser(123L, "testuser", "test@example.com", "USER");
        anotherUser = createUser(456L, "anotheruser", "another@example.com", "USER");

        testTasks = Arrays.asList(
            createTaskDto(1L, 123L, "User 123 Task 1", Status.TODO),
            createTaskDto(2L, 123L, "User 123 Task 2", Status.IN_PROGRESS)
        );
    }

    private User createUser(Long id, String username, String email, String roleType) {
        User user = new User();
        user.setId(id);
        user.setUserName(username);
        user.setEmail(email);
        user.setRoleType(roleType);
        user.setCognitoSub(UUID.randomUUID());
        return user;
    }


    private TaskResponseDto createTaskDto(Long id, Long ownerId, String title, Status status) {
        return TaskResponseDto.builder()
                .id(id)
                .projectId(101L)
                .ownerId(ownerId)
                .taskType(TaskType.FEATURE)
                .title(title)
                .description("Description for " + title)
                .status(status)
                .tags(Collections.emptyList())
                .createdBy(ownerId)
                .createdAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Authentication Tests")
    class AuthenticationTests {

        @Test
        @WithAnonymousUser
        @DisplayName("Should reject unauthenticated requests")
        void getTasks_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            // Verify services are never called
            verify(userContextService, never()).getRequestingUser();
            verify(taskService, never()).getAllUserTasks(eq(123L));
        }

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
        @DisplayName("Should accept authenticated user requests")
        void getTasks_AuthenticatedUser_ReturnsOk() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(testTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2));

            verify(userContextService).getRequestingUser();
            verify(taskService).getAllUserTasks(eq(123L));
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
        @DisplayName("Should return tasks only for the authenticated user")
        void getTasks_AuthenticatedUser_ReturnsOnlyOwnTasks() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(testTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].ownerId").value(123L))
                    .andExpect(jsonPath("$[1].ownerId").value(123L));

            // Verify service is called with correct user ID
            verify(taskService).getAllUserTasks(eq(123L));
        }

        @Test
        @WithMockUser(username = "another@example.com", authorities = {"ROLE_USER"})
        @DisplayName("Should call service with correct user ID based on context")
        void getTasks_DifferentAuthenticatedUser_CallsServiceWithCorrectUserId() throws Exception {
            // Given
            List<TaskResponseDto> anotherUserTasks = Collections.singletonList(
                createTaskDto(3L, 456L, "User 456 Task", Status.COMPLETED)
            );

            when(userContextService.getRequestingUser()).thenReturn(anotherUser);
            when(taskService.getAllUserTasks(eq(456L))).thenReturn(anotherUserTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].ownerId").value(456L));

            // Verify service is called with the correct different user ID
            verify(taskService).getAllUserTasks(eq(456L));
            verify(taskService, never()).getAllUserTasks(eq(123L));
        }

        @Test
        @WithMockUser(username = "admin@example.com", authorities = {"ROLE_ADMIN"})
        @DisplayName("Should work for admin users")
        void getTasks_AdminUser_WorksCorrectly() throws Exception {
            // Given
            User adminUser = createUser(999L, "admin", "admin@example.com", "ADMIN");
            List<TaskResponseDto> adminTasks = Collections.singletonList(
                createTaskDto(99L, 999L, "Admin Task", Status.TODO)
            );

            when(userContextService.getRequestingUser()).thenReturn(adminUser);
            when(taskService.getAllUserTasks(eq(999L))).thenReturn(adminTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].ownerId").value(999L));

            verify(taskService).getAllUserTasks(eq(999L));
        }
    }

    @Nested
    @DisplayName("User Context Service Integration")
    class UserContextServiceIntegrationTests {

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
        @DisplayName("Should handle UserContextService returning user correctly")
        void getTasks_UserContextServiceReturnsUser_WorksCorrectly() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify the flow
            verify(userContextService).getRequestingUser();
            verify(taskService).getAllUserTasks(eq(123L));
        }

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
        @DisplayName("Should handle case when user has no tasks")
        void getTasks_UserWithNoTasks_ReturnsEmptyArray() throws Exception {
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

            verify(taskService).getAllUserTasks(eq(123L));
        }
    }

    @Nested
    @DisplayName("CSRF Protection Tests")
    class CSRFProtectionTests {

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
        @DisplayName("Should work with CSRF token for GET request")
        void getTasks_WithCSRFToken_WorksCorrectly() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(testTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf()) // CSRF token included
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
        @DisplayName("Should work without CSRF token for GET request")
        void getTasks_WithoutCSRFToken_WorksCorrectly() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(testTasks);

            // When & Then
            // GET requests typically don't require CSRF tokens
            mockMvc.perform(get("/api/tasks/user")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Role-Based Access Tests")
    class RoleBasedAccessTests {

        @Test
        @WithMockUser(username = "user@example.com", authorities = {"ROLE_USER"})
        @DisplayName("Should allow USER role to access getTasks")
        void getTasks_UserRole_AllowsAccess() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(taskService.getAllUserTasks(eq(123L))).thenReturn(testTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "manager@example.com", authorities = {"ROLE_MANAGER"})
        @DisplayName("Should allow MANAGER role to access getTasks")
        void getTasks_ManagerRole_AllowsAccess() throws Exception {
            // Given
            User managerUser = createUser(789L, "manager", "manager@example.com", "MANAGER");
            List<TaskResponseDto> managerTasks = Collections.singletonList(
                createTaskDto(88L, 789L, "Manager Task", Status.IN_PROGRESS)
            );

            when(userContextService.getRequestingUser()).thenReturn(managerUser);
            when(taskService.getAllUserTasks(eq(789L))).thenReturn(managerTasks);

            // When & Then
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].ownerId").value(789L));
        }

        @Test
        @WithMockUser(username = "guest@example.com", authorities = {"ROLE_GUEST"})
        @DisplayName("Should handle GUEST role appropriately")
        void getTasks_GuestRole_HandlesAppropriately() throws Exception {
            // Given
            User guestUser = createUser(111L, "guest", "guest@example.com", "GUEST");
            when(userContextService.getRequestingUser()).thenReturn(guestUser);
            when(taskService.getAllUserTasks(eq(111L))).thenReturn(Collections.emptyList());

            // When & Then
            // Depending on your security configuration, this might be allowed or forbidden
            // Adjust the expectation based on your security requirements
            mockMvc.perform(get("/api/tasks/user")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk()); // or .andExpect(status().isForbidden());
        }
    }
}