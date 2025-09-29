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
    @DisplayName("IsRequestingUserTaskCollaborator Tests")
    class IsRequestingUserTaskCollaboratorTests {

        @Test
        @DisplayName("Should return true when user is task collaborator")
        void isRequestingUserTaskCollaborator_UserIsCollaborator_ReturnsTrue() {
            // Given
            Long taskId = 123L;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId())).thenReturn(true);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertTrue(result);
                verify(userRepository).findByCognitoSub(testCognitoSub);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should return false when user is not task collaborator")
        void isRequestingUserTaskCollaborator_UserIsNotCollaborator_ReturnsFalse() {
            // Given
            Long taskId = 123L;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId())).thenReturn(false);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertFalse(result);
                verify(userRepository).findByCognitoSub(testCognitoSub);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when cognito sub not available")
        void isRequestingUserTaskCollaborator_CognitoSubNotAvailable_ThrowsRuntimeException() {
            // Given
            Long taskId = 123L;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.empty());

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userContextService.isRequestingUserTaskCollaborator(taskId));

                assertEquals("Requesting User Cognito sub not available", exception.getMessage());
                verify(userRepository, never()).findByCognitoSub(any());
                verify(taskAssigneeRepository, never()).existsByTaskIdAndUserId(any(), any());
            }
        }

        @Test
        @DisplayName("Should throw RuntimeException when user not found")
        void isRequestingUserTaskCollaborator_UserNotFound_ThrowsRuntimeException() {
            // Given
            Long taskId = 123L;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.empty());

                // When & Then
                RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userContextService.isRequestingUserTaskCollaborator(taskId));

                assertEquals("Requesting user not found", exception.getMessage());
                verify(userRepository).findByCognitoSub(testCognitoSub);
                verify(taskAssigneeRepository, never()).existsByTaskIdAndUserId(any(), any());
            }
        }

        @Test
        @DisplayName("Should handle null task ID gracefully")
        void isRequestingUserTaskCollaborator_NullTaskId_DelegatesToRepository() {
            // Given
            Long taskId = null;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId())).thenReturn(false);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertFalse(result);
                verify(userRepository).findByCognitoSub(testCognitoSub);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should handle different task IDs correctly")
        void isRequestingUserTaskCollaborator_DifferentTaskIds_HandlesCorrectly() {
            // Given
            Long[] taskIds = {1L, 999L, Long.MAX_VALUE, 0L};

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(testUser));

                for (Long taskId : taskIds) {
                    // Reset mocks for each iteration
                    reset(taskAssigneeRepository);
                    when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId())).thenReturn(true);

                    // When
                    boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                    // Then
                    assertTrue(result);
                    verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
                }
            }
        }

        @Test
        @DisplayName("Should handle different user IDs correctly")
        void isRequestingUserTaskCollaborator_DifferentUserIds_HandlesCorrectly() {
            // Given
            Long taskId = 123L;
            Long[] userIds = {1L, 999L, Long.MAX_VALUE};

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                for (Long userId : userIds) {
                    // Reset mocks for each iteration
                    reset(userRepository, taskAssigneeRepository);

                    User userWithDifferentId = new User();
                    userWithDifferentId.setId(userId);
                    userWithDifferentId.setCognitoSub(testCognitoSub);

                    when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(userWithDifferentId));
                    when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, userId)).thenReturn(true);

                    // When
                    boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                    // Then
                    assertTrue(result);
                    verify(userRepository).findByCognitoSub(testCognitoSub);
                    verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, userId);
                }
            }
        }

        @Test
        @DisplayName("Should verify correct sequence of method calls")
        void isRequestingUserTaskCollaborator_VerifyMethodCallSequence() {
            // Given
            Long taskId = 123L;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId())).thenReturn(true);

                // When
                userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then - Verify the sequence of calls
                verify(userRepository).findByCognitoSub(testCognitoSub);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should handle edge case with negative task ID")
        void isRequestingUserTaskCollaborator_NegativeTaskId_HandlesCorrectly() {
            // Given
            Long taskId = -1L;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId())).thenReturn(false);

                // When
                boolean result = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertFalse(result);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should handle repository returning both true and false correctly")
        void isRequestingUserTaskCollaborator_RepositoryDifferentResults_HandlesCorrectly() {
            // Given
            Long taskId1 = 123L;
            Long taskId2 = 456L;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId1, testUser.getId())).thenReturn(true);
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId2, testUser.getId())).thenReturn(false);

                // When
                boolean result1 = userContextService.isRequestingUserTaskCollaborator(taskId1);
                boolean result2 = userContextService.isRequestingUserTaskCollaborator(taskId2);

                // Then
                assertTrue(result1);
                assertFalse(result2);
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId1, testUser.getId());
                verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId2, testUser.getId());
            }
        }

        @Test
        @DisplayName("Should handle multiple consecutive calls efficiently")
        void isRequestingUserTaskCollaborator_MultipleCalls_HandlesEfficiently() {
            // Given
            Long taskId = 123L;

            try (MockedStatic<SecurityContextUtil> mockedSecurityContextUtil = mockStatic(SecurityContextUtil.class)) {
                mockedSecurityContextUtil.when(SecurityContextUtil::getCurrentCognitoSubUUID)
                        .thenReturn(Optional.of(testCognitoSub));

                when(userRepository.findByCognitoSub(testCognitoSub)).thenReturn(Optional.of(testUser));
                when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, testUser.getId())).thenReturn(true);

                // When - Multiple calls
                boolean result1 = userContextService.isRequestingUserTaskCollaborator(taskId);
                boolean result2 = userContextService.isRequestingUserTaskCollaborator(taskId);
                boolean result3 = userContextService.isRequestingUserTaskCollaborator(taskId);

                // Then
                assertTrue(result1);
                assertTrue(result2);
                assertTrue(result3);
                verify(userRepository, times(3)).findByCognitoSub(testCognitoSub);
                verify(taskAssigneeRepository, times(3)).existsByTaskIdAndUserId(taskId, testUser.getId());
            }
        }
    }
}
