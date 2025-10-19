package com.spmorangle.crm.usermanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spmorangle.common.config.SecurityConfig;
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
import org.springframework.context.annotation.Import;
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
@Import({SecurityConfig.class})
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

        userResponseDto = new UserResponseDto(1L, "John Doe", "john.doe@example.com", "STAFF", true, testCognitoSub);
    }

    @Nested
    @DisplayName("Create User Tests")
    class CreateUserTests {

        @Test
        @DisplayName("Should successfully create user and return 200")
        void createUser_ValidRequest_ReturnsOk() throws Exception {
            // Given
            doNothing().when(userManagementService).createUser(any(CreateUserDto.class), eq("STAFF"), eq(false));

            // When & Then
            mockMvc.perform(post("/api/user/create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isOk());

            verify(userManagementService).createUser(any(CreateUserDto.class), eq("STAFF"), eq(false));
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
            doNothing().when(userManagementService).createUser(any(CreateUserDto.class), eq("STAFF"), eq(false));

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

            UserResponseDto userWithSpecialContent = new UserResponseDto(1L, usernameWithSpecialChars, emailWithSpecialChars, "STAFF", true, testCognitoSub);

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

    @Nested
    @DisplayName("Admin Create User Tests")
    class AdminCreateUserTests {

        @Test
        @DisplayName("Should successfully create user with admin permissions and return 200")
        @WithMockUser(username = "admin@example.com", authorities = {"ROLE_MANAGER"})
        void adminCreateUser_ValidRequest_ReturnsOk() throws Exception {
            // Given
            doNothing().when(userManagementService).createUser(any(CreateUserDto.class), eq(true));

            // When & Then
            mockMvc.perform(post("/api/user/admin-create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isOk());

            verify(userManagementService).createUser(any(CreateUserDto.class), eq(true));
        }

        @Test
        @DisplayName("Should return 403 when non-admin tries to admin create user")
        @WithMockUser(username = "staff@example.com", authorities = {"ROLE_STAFF"})
        void adminCreateUser_NonAdminRole_ReturnsForbidden() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/user/admin-create")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createUserDto)))
                    .andExpect(status().isForbidden());

            verify(userManagementService, never()).createUser(any(CreateUserDto.class), eq(true));
        }
    }

    @Nested
    @DisplayName("Deactivate User Tests")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should successfully deactivate user and return 200")
        @WithMockUser(username = "admin@example.com", authorities = {"ROLE_MANAGER"})
        void deactivateUser_ValidRequest_ReturnsOk() throws Exception {
            // Given
            Long userId = 1L;
            doNothing().when(userManagementService).toggleUserStatus(userId, false);

            // When & Then
            mockMvc.perform(post("/api/user/deactivate/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(userManagementService).toggleUserStatus(userId, false);
        }

        @Test
        @DisplayName("Should return 403 when non-authorized user tries to deactivate")
        @WithMockUser(username = "staff@example.com", authorities = {"ROLE_STAFF"})
        void deactivateUser_NonAuthorizedRole_ReturnsForbidden() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(post("/api/user/deactivate/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(userManagementService, never()).toggleUserStatus(userId, false);
        }

        @Test
        @DisplayName("Should allow HR role to deactivate users")
        @WithMockUser(username = "hr@example.com", authorities = {"ROLE_HR"})
        void deactivateUser_HrRole_AllowsAccess() throws Exception {
            // Given
            Long userId = 1L;
            doNothing().when(userManagementService).toggleUserStatus(userId, false);

            // When & Then
            mockMvc.perform(post("/api/user/deactivate/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(userManagementService).toggleUserStatus(userId, false);
        }
    }

    @Nested
    @DisplayName("Reactivate User Tests")
    class ReactivateUserTests {

        @Test
        @DisplayName("Should successfully reactivate user and return 200")
        @WithMockUser(username = "admin@example.com", authorities = {"ROLE_MANAGER"})
        void reactivateUser_ValidRequest_ReturnsOk() throws Exception {
            // Given
            Long userId = 1L;
            doNothing().when(userManagementService).toggleUserStatus(userId, true);

            // When & Then
            mockMvc.perform(post("/api/user/reactivate/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(userManagementService).toggleUserStatus(userId, true);
        }

        @Test
        @DisplayName("Should return 403 when non-authorized user tries to reactivate")
        @WithMockUser(username = "staff@example.com", authorities = {"ROLE_STAFF"})
        void reactivateUser_NonAuthorizedRole_ReturnsForbidden() throws Exception {
            // Given
            Long userId = 1L;

            // When & Then
            mockMvc.perform(post("/api/user/reactivate/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(userManagementService, never()).toggleUserStatus(userId, true);
        }

        @Test
        @DisplayName("Should allow HR role to reactivate users")
        @WithMockUser(username = "hr@example.com", authorities = {"ROLE_HR"})
        void reactivateUser_HrRole_AllowsAccess() throws Exception {
            // Given
            Long userId = 1L;
            doNothing().when(userManagementService).toggleUserStatus(userId, true);

            // When & Then
            mockMvc.perform(post("/api/user/reactivate/{userId}", userId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            verify(userManagementService).toggleUserStatus(userId, true);
        }
    }

    @Nested
    @DisplayName("Get All Users Tests")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should successfully return all users")
        @WithMockUser(username = "admin@example.com", authorities = {"ROLE_MANAGER"})
        void getAllUsers_ValidRequest_ReturnsUserList() throws Exception {
            // Given
            List<UserResponseDto> users = Arrays.asList(
                    new UserResponseDto(1L, "John Doe", "john@example.com", "STAFF", true, UUID.randomUUID()),
                    new UserResponseDto(2L, "Jane Smith", "jane@example.com", "MANAGER", true, UUID.randomUUID())
            );
            when(userManagementService.getAllUsers()).thenReturn(users);

            // When & Then
            mockMvc.perform(get("/api/user/")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1L))
                    .andExpect(jsonPath("$[0].username").value("John Doe"))
                    .andExpect(jsonPath("$[0].isActive").value(true))
                    .andExpect(jsonPath("$[1].id").value(2L))
                    .andExpect(jsonPath("$[1].username").value("Jane Smith"))
                    .andExpect(jsonPath("$[1].isActive").value(true));
        }

        @Test
        @DisplayName("Should return 403 when non-authorized user tries to get all users")
        @WithMockUser(username = "staff@example.com", authorities = {"ROLE_STAFF"})
        void getAllUsers_NonAuthorizedRole_ReturnsForbidden() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/user/")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());

            verify(userManagementService, never()).getAllUsers();
        }

        @Test
        @DisplayName("Should allow HR role to get all users")
        @WithMockUser(username = "hr@example.com", authorities = {"ROLE_HR"})
        void getAllUsers_HrRole_AllowsAccess() throws Exception {
            // Given
            List<UserResponseDto> users = Arrays.asList(
                    new UserResponseDto(1L, "John Doe", "john@example.com", "STAFF", true, UUID.randomUUID())
            );
            when(userManagementService.getAllUsers()).thenReturn(users);

            // When & Then
            mockMvc.perform(get("/api/user/")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        @WithMockUser(username = "admin@example.com", authorities = {"ROLE_MANAGER"})
        void getAllUsers_NoUsers_ReturnsEmptyList() throws Exception {
            // Given
            when(userManagementService.getAllUsers()).thenReturn(Arrays.asList());

            // When & Then
            mockMvc.perform(get("/api/user/")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }
}
