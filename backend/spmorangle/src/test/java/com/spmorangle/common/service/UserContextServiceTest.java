package com.spmorangle.common.service;

import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.common.service.impl.UserContextServiceImpl;
import com.spmorangle.common.util.SecurityContextUtil;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserContextService Tests")
class UserContextServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @InjectMocks
    private UserContextServiceImpl userContextService;

    private User testUser;
    private UUID testCognitoSub;

    @BeforeEach
    void setUp() {
        testCognitoSub = UUID.randomUUID();
        testUser = new User();
        testUser.setId(1L);
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setRoleType("USER");
        testUser.setCognitoSub(testCognitoSub);
    }

    @Nested
    @DisplayName("getRequestingUser Tests")
    class GetRequestingUserTests {

        @Test
        @DisplayName("Should return user when cognito sub is found")
        void shouldReturnUserWhenCognitoSubFound() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));

                // Act
                User result = userContextService.getRequestingUser();

                // Assert
                assertNotNull(result);
                assertEquals(testUser.getId(), result.getId());
                assertEquals(testUser.getUserName(), result.getUserName());
                assertEquals(testUser.getCognitoSub(), result.getCognitoSub());
                verify(userRepository).findByCognitoSub(testCognitoSub);
            }
        }

        @Test
        @DisplayName("Should throw exception when cognito sub is not available")
        void shouldThrowExceptionWhenCognitoSubNotAvailable() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.empty());

                // Act & Assert
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userContextService.getRequestingUser());
                assertEquals("Requesting User Cognito sub not available", exception.getMessage());
                verify(userRepository, never()).findByCognitoSub(any());
            }
        }

        @Test
        @DisplayName("Should throw exception when user is not found")
        void shouldThrowExceptionWhenUserNotFound() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.empty());

                // Act & Assert
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userContextService.getRequestingUser());
                assertEquals("Requesting user not found", exception.getMessage());
                verify(userRepository).findByCognitoSub(testCognitoSub);
            }
        }
    }

    @Nested
    @DisplayName("isRequestingUserSelfCheckByUserId Tests")
    class IsRequestingUserSelfCheckByUserIdTests {

        @Test
        @DisplayName("Should return true when user ID matches requesting user")
        void shouldReturnTrueWhenUserIdMatches() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));

                // Act
                boolean result = userContextService.isRequestingUserSelfCheckByUserId(1L);

                // Assert
                assertTrue(result);
            }
        }

        @Test
        @DisplayName("Should return false when user ID does not match requesting user")
        void shouldReturnFalseWhenUserIdDoesNotMatch() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));

                // Act
                boolean result = userContextService.isRequestingUserSelfCheckByUserId(999L);

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should throw exception when requesting user cannot be retrieved")
        void shouldThrowExceptionWhenRequestingUserCannotBeRetrieved() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(RuntimeException.class,
                    () -> userContextService.isRequestingUserSelfCheckByUserId(1L));
            }
        }
    }

    @Nested
    @DisplayName("isRequestingUserSelfCheckBySub Tests")
    class IsRequestingUserSelfCheckBySubTests {

        @Test
        @DisplayName("Should return true when cognito sub matches requesting user")
        void shouldReturnTrueWhenCognitoSubMatches() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));

                // Act
                boolean result = userContextService.isRequestingUserSelfCheckBySub(testCognitoSub);

                // Assert
                assertTrue(result);
            }
        }

        @Test
        @DisplayName("Should return false when cognito sub does not match requesting user")
        void shouldReturnFalseWhenCognitoSubDoesNotMatch() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                UUID differentCognitoSub = UUID.randomUUID();
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));

                // Act
                boolean result = userContextService.isRequestingUserSelfCheckBySub(differentCognitoSub);

                // Assert
                assertFalse(result);
            }
        }

        @Test
        @DisplayName("Should throw exception when requesting user cannot be retrieved")
        void shouldThrowExceptionWhenRequestingUserCannotBeRetrieved() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Arrange
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.empty());

                // Act & Assert
                assertThrows(RuntimeException.class,
                    () -> userContextService.isRequestingUserSelfCheckBySub(testCognitoSub));
            }
        }
    }

    @Nested
    @DisplayName("isRequestingUserTaskCollaborator Tests")
    class IsRequestingUserTaskCollaboratorTests {

        @Test
        @DisplayName("Should return true when requesting user is collaborator for the task")
        void shouldReturnTrueWhenUserIsTaskCollaborator() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Given
                Long taskId = 10L;
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId()))
                        .thenReturn(true);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertTrue(result);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should return false when requesting user is not collaborator for the task")
        void shouldReturnFalseWhenUserIsNotTaskCollaborator() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Given
                Long taskId = 10L;
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId()))
                        .thenReturn(false);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertFalse(result);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should handle different task IDs correctly")
        void shouldHandleDifferentTaskIdsCorrectly() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Given
                Long taskId = 999L;
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId()))
                        .thenReturn(true);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertTrue(result);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should handle null task ID gracefully")
        void shouldHandleNullTaskIdGracefully() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Given
                Long taskId = null;
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId()))
                        .thenReturn(false);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertFalse(result);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should throw exception when requesting user cognito sub is not available")
        void shouldThrowExceptionWhenCognitoSubNotAvailable() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Given
                Long taskId = 10L;
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.empty());

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userContextService.isRequestingUserTaskCollaborator(taskId));
                assertEquals("Requesting User Cognito sub not available", exception.getMessage());
                verify(taskAssigneeRepository, never()).existsByTaskIdAndUserId(any(), any());
            }
        }

        @Test
        @DisplayName("Should throw exception when requesting user is not found")
        void shouldThrowExceptionWhenRequestingUserNotFound() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Given
                Long taskId = 10L;
                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.empty());

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userContextService.isRequestingUserTaskCollaborator(taskId));
                assertEquals("Requesting user not found", exception.getMessage());
                verify(taskAssigneeRepository, never()).existsByTaskIdAndUserId(any(), any());
            }
        }

        @Test
        @DisplayName("Should verify repository called with correct parameters")
        void shouldVerifyRepositoryCalledWithCorrectParameters() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Given
                Long taskId = 42L;
                Long userId = 123L;
                User userWithDifferentId = new User();
                userWithDifferentId.setId(userId);
                userWithDifferentId.setCognitoSub(testCognitoSub);

                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(userWithDifferentId));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, userId))
                        .thenReturn(false);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertFalse(result);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, userId);
                verify(taskAssigneeRepository, never()).existsByTaskIdAndUserId(eq(taskId), eq(testUser.getId()));
            }
        }

        @Test
        @DisplayName("Should work correctly when user has different ID")
        void shouldWorkCorrectlyWhenUserHasDifferentId() {
            try (MockedStatic<SecurityContextUtil> mockedSecurityUtil = mockStatic(SecurityContextUtil.class)) {
                // Given
                Long taskId = 15L;
                Long differentUserId = 789L;
                User differentUser = new User();
                differentUser.setId(differentUserId);
                differentUser.setUserName("differentuser");
                differentUser.setCognitoSub(testCognitoSub);

                mockedSecurityUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));
                when(userRepository.findByCognitoSub(testCognitoSub))
                        .thenReturn(Optional.of(differentUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, differentUserId))
                        .thenReturn(true);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertTrue(result);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, differentUserId);
            }
        }
    }
}
