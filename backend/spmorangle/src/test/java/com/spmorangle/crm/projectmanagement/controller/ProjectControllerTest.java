package com.spmorangle.crm.projectmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.projectmanagement.dto.CreateProjectDto;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@DisplayName("ProjectController Tests")
@WithMockUser(username = "test@example.com", authorities = {"ROLE_USER"})
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProjectService projectService;

    @MockitoBean
    private UserContextService userContextService;

    private User testUser;
    private CreateProjectDto createProjectDto;
    private ProjectResponseDto projectResponseDto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(123L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoleType("USER");
        testUser.setCognitoSub(UUID.randomUUID());

        createProjectDto = CreateProjectDto.builder()
                .name("Test Project")
                .description("Test project description")
                .build();

        projectResponseDto = ProjectResponseDto.builder()
                .id(1L)
                .name("Test Project")
                .description("Test project description")
                .ownerId(123L)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("Get User Projects Tests")
    class GetUserProjectsTests {

        @Test
        @DisplayName("Should successfully return projects for authenticated user")
        void getUserProjects_AuthenticatedUser_ReturnsProjectsWithOk() throws Exception {
            // Given
            List<ProjectResponseDto> expectedProjects = Arrays.asList(
                    ProjectResponseDto.builder()
                            .id(1L)
                            .name("Project 1")
                            .description("Description 1")
                            .ownerId(123L)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build(),
                    ProjectResponseDto.builder()
                            .id(2L)
                            .name("Project 2")
                            .description("Description 2")
                            .ownerId(123L)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build()
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(projectService.getUserProjects(eq(123L))).thenReturn(expectedProjects);

            // When & Then
            mockMvc.perform(get("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].name").value("Project 1"))
                    .andExpect(jsonPath("$[0].description").value("Description 1"))
                    .andExpect(jsonPath("$[0].ownerId").value(123L))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].name").value("Project 2"));
        }

        @Test
        @DisplayName("Should return empty array when user has no projects")
        void getUserProjects_UserWithNoProjects_ReturnsEmptyArrayWithOk() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(projectService.getUserProjects(eq(123L))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("Should verify service is called with correct user ID")
        void getUserProjects_VerifyServiceCall_CallsWithCorrectUserId() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(projectService.getUserProjects(eq(123L))).thenReturn(Collections.emptyList());

            // When
            mockMvc.perform(get("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Then
            verify(userContextService).getRequestingUser();
            verify(projectService).getUserProjects(eq(123L));
        }
    }

    @Nested
    @DisplayName("Create Project Tests")
    class CreateProjectTests {

        @Test
        @DisplayName("Should successfully create project and return 201 with response")
        void createProject_ValidRequest_ReturnsCreatedWithResponse() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(projectService.createProject(any(CreateProjectDto.class), eq(123L)))
                    .thenReturn(projectResponseDto);

            // When & Then
            mockMvc.perform(post("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createProjectDto)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.name").value("Test Project"))
                    .andExpect(jsonPath("$.description").value("Test project description"))
                    .andExpect(jsonPath("$.ownerId").value(123L))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        @DisplayName("Should return 400 when project name is null")
        void createProject_NullName_ReturnsBadRequest() throws Exception {
            // Given
            CreateProjectDto invalidRequest = CreateProjectDto.builder()
                    .name(null)
                    .description("Description")
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);

            // When & Then
            mockMvc.perform(post("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle empty project name - validation depends on DTO validation")
        void createProject_EmptyName_ValidationBehavior() throws Exception {
            // Given
            CreateProjectDto requestWithEmptyName = CreateProjectDto.builder()
                    .name("")
                    .description("Description")
                    .build();

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(projectService.createProject(any(CreateProjectDto.class), eq(123L)))
                    .thenReturn(projectResponseDto);

            // When & Then - The actual validation behavior depends on DTO validation annotations
            mockMvc.perform(post("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestWithEmptyName)))
                    .andExpect(status().isCreated()); // Adjust based on actual validation behavior
        }

        @Test
        @DisplayName("Should return 400 when request body is empty")
        void createProject_EmptyRequestBody_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed")
        void createProject_MalformedRequestBody_ReturnsBadRequest() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Delete Project Tests")
    class DeleteProjectTests {

        @Test
        @DisplayName("Should successfully delete project and return 204")
        void deleteProject_ValidProjectId_ReturnsNoContent() throws Exception {
            // Given
            Long projectId = 1L;
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            doNothing().when(projectService).deleteProject(eq(projectId), eq(123L));

            // When & Then
            mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(projectService).deleteProject(eq(projectId), eq(123L));
        }

        @Test
        @DisplayName("Should handle invalid project ID gracefully")
        void deleteProject_InvalidProjectId_CallsServiceWithId() throws Exception {
            // Given
            Long invalidProjectId = 999L;
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            doNothing().when(projectService).deleteProject(eq(invalidProjectId), eq(123L));

            // When & Then
            mockMvc.perform(delete("/api/projects/{projectId}", invalidProjectId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(projectService).deleteProject(eq(invalidProjectId), eq(123L));
        }
    }

    @Nested
    @DisplayName("Get Project Members Tests")
    class GetProjectMembersTests {

        @Test
        @DisplayName("Should successfully return project members")
        void getProjectMembers_ValidProjectId_ReturnsProjectMembersWithOk() throws Exception {
            // Given
            Long projectId = 1L;
            List<UserResponseDto> expectedMembers = Arrays.asList(
                    UserResponseDto.builder()
                            .id(1L)
                            .username("user1")
                            .email("user1@example.com")
                            .build(),
                    UserResponseDto.builder()
                            .id(2L)
                            .username("user2")
                            .email("user2@example.com")
                            .build()
            );

            when(projectService.getProjectMembers(eq(projectId))).thenReturn(expectedMembers);

            // When & Then
            mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].username").value("user1"))
                    .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].username").value("user2"))
                    .andExpect(jsonPath("$[1].email").value("user2@example.com"));

            verify(projectService).getProjectMembers(eq(projectId));
        }

        @Test
        @DisplayName("Should return empty list when project has no members")
        void getProjectMembers_ProjectWithNoMembers_ReturnsEmptyListWithOk() throws Exception {
            // Given
            Long projectId = 1L;
            List<UserResponseDto> emptyMembers = Collections.emptyList();

            when(projectService.getProjectMembers(eq(projectId))).thenReturn(emptyMembers);

            // When & Then
            mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));

            verify(projectService).getProjectMembers(eq(projectId));
        }

        @Test
        @DisplayName("Should handle invalid project ID")
        void getProjectMembers_InvalidProjectId_CallsServiceWithId() throws Exception {
            // Given
            Long invalidProjectId = 999L;
            when(projectService.getProjectMembers(eq(invalidProjectId))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/projects/{projectId}/members", invalidProjectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(projectService).getProjectMembers(eq(invalidProjectId));
        }

        @Test
        @DisplayName("Should handle service throwing exception")
        void getProjectMembers_ServiceThrowsException_ReturnsInternalServerError() throws Exception {
            // Given
            Long projectId = 1L;
            when(projectService.getProjectMembers(eq(projectId)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            mockMvc.perform(get("/api/projects/{projectId}/members", projectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            verify(projectService).getProjectMembers(eq(projectId));
        }

        @Test
        @DisplayName("Should handle zero project ID")
        void getProjectMembers_ZeroProjectId_CallsServiceWithZero() throws Exception {
            // Given
            Long zeroProjectId = 0L;
            when(projectService.getProjectMembers(eq(zeroProjectId))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/projects/{projectId}/members", zeroProjectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(projectService).getProjectMembers(eq(zeroProjectId));
        }

        @Test
        @DisplayName("Should handle negative project ID")
        void getProjectMembers_NegativeProjectId_CallsServiceWithNegativeId() throws Exception {
            // Given
            Long negativeProjectId = -1L;
            when(projectService.getProjectMembers(eq(negativeProjectId))).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/projects/{projectId}/members", negativeProjectId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(projectService).getProjectMembers(eq(negativeProjectId));
        }
    }

    @Nested
    @DisplayName("Security and Authorization Tests")
    class SecurityAndAuthorizationTests {

        @Test
        @DisplayName("Should verify user context service is called for all endpoints")
        void allEndpoints_VerifyUserContextService_CallsGetRequestingUser() throws Exception {
            // Given
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(projectService.getUserProjects(eq(123L))).thenReturn(Collections.emptyList());
            when(projectService.createProject(any(CreateProjectDto.class), eq(123L)))
                    .thenReturn(projectResponseDto);
            doNothing().when(projectService).deleteProject(eq(1L), eq(123L));

            // When & Then - Test GET
            mockMvc.perform(get("/api/projects").with(csrf()))
                    .andExpect(status().isOk());

            // Test POST
            mockMvc.perform(post("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createProjectDto)))
                    .andExpect(status().isCreated());

            // Test DELETE
            mockMvc.perform(delete("/api/projects/1").with(csrf()))
                    .andExpect(status().isNoContent());

            // Verify user context service was called for each endpoint
            verify(userContextService, org.mockito.Mockito.times(3)).getRequestingUser();
        }
    }

    @Nested
    @DisplayName("Content Validation Tests")
    class ContentValidationTests {

        @Test
        @DisplayName("Should handle projects with special characters in JSON response")
        void getProjects_ProjectsWithSpecialCharacters_ReturnsValidJson() throws Exception {
            // Given
            String nameWithSpecialChars = "Project with \"quotes\" and \\backslashes\\ and /slashes/";
            String descriptionWithUnicode = "Description with émojis 🚀 and unicode characters: 你好世界";

            List<ProjectResponseDto> projectsWithSpecialContent = Collections.singletonList(
                    ProjectResponseDto.builder()
                            .id(1L)
                            .name(nameWithSpecialChars)
                            .description(descriptionWithUnicode)
                            .ownerId(123L)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build()
            );

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(projectService.getUserProjects(eq(123L))).thenReturn(projectsWithSpecialContent);

            // When & Then
            mockMvc.perform(get("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].name").value(nameWithSpecialChars))
                    .andExpect(jsonPath("$[0].description").value(descriptionWithUnicode));
        }

        @Test
        @DisplayName("Should validate response structure matches DTO contract")
        void getProjects_ResponseStructure_MatchesDtoContract() throws Exception {
            // Given
            List<ProjectResponseDto> validProjects = Collections.singletonList(projectResponseDto);

            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(projectService.getUserProjects(eq(123L))).thenReturn(validProjects);

            // When & Then
            mockMvc.perform(get("/api/projects")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0]").exists())
                    .andExpect(jsonPath("$[0].id").isNumber())
                    .andExpect(jsonPath("$[0].name").isString())
                    .andExpect(jsonPath("$[0].description").isString())
                    .andExpect(jsonPath("$[0].ownerId").isNumber())
                    .andExpect(jsonPath("$[0].createdAt").isString())
                    .andExpect(jsonPath("$[0].updatedAt").isString());
        }
    }
}
