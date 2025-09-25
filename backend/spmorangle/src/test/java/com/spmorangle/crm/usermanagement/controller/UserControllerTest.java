package com.spmorangle.crm.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.usermanagement.dto.CreateUserDto;
import com.spmorangle.crm.usermanagement.dto.UpdateUserRoleDto;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

@WebMvcTest(UserController.class)
@DisplayName("UserController Tests")
@WithMockUser(username = "test@example.com", authorities = {"ROLE_MANAGER"})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserManagementService userManagementService;

    @MockBean
    private UserContextService userContextService;

    private User testUser;
    private CreateUserDto createUserDto;
    private UpdateUserRoleDto updateUserRoleDto;
    private UserResponseDto userResponseDto;
    private UUID testCognitoSub;

    @BeforeEach
    void setUp() {
        testCognitoSub = UUID.randomUUID();

        testUser = new User();
        testUser.setId(123L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoleType("MANAGER");
        testUser.setCognitoSub(testCognitoSub);

        createUserDto = new CreateUserDto(
                "John Doe",
                "john.doe@example.com",
                "Password123!",
                "STAFF"
        );

        updateUserRoleDto = new UpdateUserRoleDto(
                1L,
                "jane.smith@example.com",
                "MANAGER"
        );

        userResponseDto = UserResponseDto.builder()
                .id(1L)
                .username("John Doe")
                .email("john.doe@example.com")
                .roleType("STAFF")
                .cognitoSub(testCognitoSub)
                .build();
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should successfully create user and return 200")
        void createUser_ValidRequest_ReturnsOk() throws Exception {
            // Given
            doNothing().when(userManagementService).createUser(any(CreateUserDto.class));

            // When & Then
            mockMvc.perform(post("/api/user/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isOk());

            verify(userManagementService).createUser(any(CreateUserDto.class));
        }

        @Test
        @DisplayName("Should return 400 when email is null")
        void createUser_NullEmail_ReturnsBadRequest() throws Exception {
            // Given
            CreateUserDto invalidRequest = new CreateUserDto(
                    "John Doe",
                    null,
                    "Password123!",
                    "STAFF"
            );

            // When & Then
            mockMvc.perform(post("/api/user/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is null")
        void createUser_NullPassword_ReturnsBadRequest() throws Exception {
            // Given
            CreateUserDto invalidRequest = new CreateUserDto(
                    "John Doe",
                    "john.doe@example.com",
                    null,
                    "STAFF"
            );

            // When & Then
            mockMvc.perform(post("/api/user/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when role type is null")
        void createUser_NullRoleType_ReturnsBadRequest() throws Exception {
            // Given
            CreateUserDto invalidRequest = new CreateUserDto(
                    "John Doe",
                    "john.doe@example.com",
                    "Password123!",
                    null
            );

            // When & Then
            mockMvc.perform(post("/api/user/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should allow user creation with proper authentication")
        void createUser_WithAuthentication_AllowsAccess() throws Exception {
            // Given
            doNothing().when(userManagementService).createUser(any(CreateUserDto.class));

            // When & Then - The @PreAuthorize("permitAll()") should allow this
            mockMvc.perform(post("/api/user/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Get User By ID Tests")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should successfully return user by ID")
        void getUserById_ValidUserId_ReturnsUser() throws Exception {
            // Given
            Long userId = 1L;
            when(userManagementService.getUserById(eq(userId))).thenReturn(userResponseDto);

            // When & Then
            mockMvc.perform(get("/api/user/user-id/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.username").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.roleType").value("STAFF"))
                    .andExpect(jsonPath("$.cognitoSub").exists());
        }

        @Test
        @DisplayName("Should handle non-existent user ID")
        void getUserById_NonExistentUserId_ReturnsNull() throws Exception {
            // Given
            Long userId = 999L;
            when(userManagementService.getUserById(eq(userId))).thenReturn(null);

            // When & Then
            mockMvc.perform(get("/api/user/user-id/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Get User By Cognito Sub Tests")
    class GetUserByCognitoSubTests {

        @Test
        @DisplayName("Should successfully return user by Cognito Sub")
        void getUserByCognitoSub_ValidCognitoSub_ReturnsUser() throws Exception {
            // Given
            String cognitoSubString = testCognitoSub.toString();
            when(userManagementService.getUserByCognitoSub(eq(testCognitoSub))).thenReturn(userResponseDto);

            // When & Then
            mockMvc.perform(get("/api/user/sub/{cognitoSub}", cognitoSubString)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.cognitoSub").value(cognitoSubString));
        }

        @Test
        @DisplayName("Should return 400 when Cognito Sub is invalid UUID")
        void getUserByCognitoSub_InvalidUUID_ReturnsBadRequest() throws Exception {
            // Given
            String invalidUUID = "invalid-uuid";

            // When & Then - Invalid UUID parsing causes IllegalArgumentException -> 500
            mockMvc.perform(get("/api/user/sub/{cognitoSub}", invalidUUID)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update User Role Tests")
    class UpdateUserRoleTests {

        @Test
        @DisplayName("Should successfully update user role and return 200")
        void updateUserRole_ValidRequest_ReturnsOk() throws Exception {
            // Given
            doNothing().when(userManagementService).updateUserRole(any(UpdateUserRoleDto.class));

            // When & Then
            mockMvc.perform(put("/api/user/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRoleDto)))
                    .andExpect(status().isOk());

            verify(userManagementService).updateUserRole(any(UpdateUserRoleDto.class));
        }

        @Test
        @DisplayName("Should return 400 when user ID is null")
        void updateUserRole_NullUserId_ReturnsBadRequest() throws Exception {
            // Given
            UpdateUserRoleDto invalidRequest = new UpdateUserRoleDto(
                    null,
                    "jane.smith@example.com",
                    "MANAGER"
            );

            // When & Then
            mockMvc.perform(put("/api/user/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should allow MANAGER role to update user roles")
        void updateUserRole_ManagerRole_AllowsAccess() throws Exception {
            // Given - MANAGER role should have access to update user roles
            doNothing().when(userManagementService).updateUserRole(any(UpdateUserRoleDto.class));

            // When & Then
            mockMvc.perform(put("/api/user/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRoleDto)))
                    .andExpect(status().isOk());

            verify(userManagementService).updateUserRole(any(UpdateUserRoleDto.class));
        }
    }

    @Nested
    @DisplayName("Delete User Tests")
    class DeleteUserTests {

        @Test
        @DisplayName("Should successfully delete user when user deletes themselves")
        void deleteUser_SelfDelete_ReturnsNoContent() throws Exception {
            // Given
            Long userId = 123L;
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(userContextService.isRequestingUserSelfCheckByUserId(eq(userId))).thenReturn(true);
            doNothing().when(userManagementService).deleteUser(eq(userId));

            // When & Then
            mockMvc.perform(delete("/api/user/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent());

            verify(userManagementService).deleteUser(eq(userId));
        }

        @Test
        @DisplayName("Should return 403 when user tries to delete another user")
        void deleteUser_DeleteOtherUser_ReturnsForbidden() throws Exception {
            // Given
            Long otherUserId = 456L;
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(userContextService.isRequestingUserSelfCheckByUserId(eq(otherUserId))).thenReturn(false);

            // When & Then
            mockMvc.perform(delete("/api/user/{userId}", otherUserId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(userManagementService, never()).deleteUser(eq(otherUserId));
        }
    }

    @Nested
    @DisplayName("Get User Types Tests")
    class GetUserTypesTests {

        @Test
        @DisplayName("Should successfully return list of user types")
        void getUserTypes_ValidRequest_ReturnsUserTypes() throws Exception {
            // Given
            List<String> userTypes = Arrays.asList("DIRECTOR", "MANAGER", "HR", "STAFF");
            when(userManagementService.getUserTypes()).thenReturn(userTypes);

            // When & Then
            mockMvc.perform(get("/api/user/user-types")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(4))
                    .andExpect(jsonPath("$[0]").value("DIRECTOR"))
                    .andExpect(jsonPath("$[1]").value("MANAGER"))
                    .andExpect(jsonPath("$[2]").value("HR"))
                    .andExpect(jsonPath("$[3]").value("STAFF"));
        }
    }

    @Nested
    @DisplayName("Security and Authorization Tests")
    class SecurityAndAuthorizationTests {

        @Test
        @WithAnonymousUser
        @DisplayName("Should reject unauthenticated requests to protected endpoints")
        void protectedEndpoints_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
            // Test GET user by ID
            mockMvc.perform(get("/api/user/user-id/1")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Test GET user types
            mockMvc.perform(get("/api/user/user-types")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());

            // Test PUT role update
            mockMvc.perform(put("/api/user/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRoleDto)))
                    .andExpect(status().isUnauthorized());

            // Test DELETE user
            mockMvc.perform(delete("/api/user/1")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_DIRECTOR"})
        @DisplayName("Should allow DIRECTOR role to access all endpoints")
        void allEndpoints_DirectorRole_AllowsAccess() throws Exception {
            // Given
            when(userManagementService.getUserById(eq(1L))).thenReturn(userResponseDto);
            when(userManagementService.getUserTypes()).thenReturn(Arrays.asList("DIRECTOR", "MANAGER"));
            when(userContextService.getRequestingUser()).thenReturn(testUser);
            when(userContextService.isRequestingUserSelfCheckByUserId(eq(123L))).thenReturn(true);
            doNothing().when(userManagementService).updateUserRole(any(UpdateUserRoleDto.class));
            doNothing().when(userManagementService).deleteUser(eq(123L));

            // Test all protected endpoints
            mockMvc.perform(get("/api/user/user-id/1").with(csrf()))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/user/user-types").with(csrf()))
                    .andExpect(status().isOk());

            mockMvc.perform(put("/api/user/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRoleDto)))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/user/123").with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_HR"})
        @DisplayName("Should allow HR role to update user roles")
        void updateUserRole_HrRole_AllowsAccess() throws Exception {
            // Given
            doNothing().when(userManagementService).updateUserRole(any(UpdateUserRoleDto.class));

            // When & Then
            mockMvc.perform(put("/api/user/role")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateUserRoleDto)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Content Validation Tests")
    class ContentValidationTests {

        @Test
        @WithMockUser(username = "test@example.com", authorities = {"ROLE_MANAGER"})
        @DisplayName("Should handle users with special characters in JSON response")
        void getUser_UserWithSpecialCharacters_ReturnsValidJson() throws Exception {
            // Given
            String usernameWithSpecialChars = "User with \"quotes\" and \\backslashes\\";
            String emailWithSpecialChars = "user+test@example.com";

            UserResponseDto userWithSpecialContent = UserResponseDto.builder()
                    .id(1L)
                    .username(usernameWithSpecialChars)
                    .email(emailWithSpecialChars)
                    .roleType("STAFF")
                    .cognitoSub(testCognitoSub)
                    .build();

            when(userManagementService.getUserById(eq(1L))).thenReturn(userWithSpecialContent);

            // When & Then
            mockMvc.perform(get("/api/user/user-id/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value(usernameWithSpecialChars))
                    .andExpect(jsonPath("$.email").value(emailWithSpecialChars));
        }
    }
}
