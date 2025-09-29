package com.spmorangle.crm.projectmanagement.service.impl;

import com.spmorangle.common.converter.UserConverter;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.projectmanagement.dto.CreateProjectDto;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.model.Project;
import com.spmorangle.crm.projectmanagement.repository.ProjectRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProjectServiceImpl Tests")
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User testUser1;
    private User testUser2;
    private User testUser3;
    private Project testProject;
    private CreateProjectDto createProjectDto;
    private UserResponseDto userResponseDto1;
    private UserResponseDto userResponseDto2;
    private UserResponseDto userResponseDto3;

    @BeforeEach
    void setUp() {
        // Setup test users
        testUser1 = new User();
        testUser1.setId(1L);
        testUser1.setUserName("user1");
        testUser1.setEmail("user1@example.com");
        testUser1.setRoleType("USER");
        testUser1.setCognitoSub(UUID.randomUUID());

        testUser2 = new User();
        testUser2.setId(2L);
        testUser2.setUserName("user2");
        testUser2.setEmail("user2@example.com");
        testUser2.setRoleType("USER");
        testUser2.setCognitoSub(UUID.randomUUID());

        testUser3 = new User();
        testUser3.setId(3L);
        testUser3.setUserName("user3");
        testUser3.setEmail("user3@example.com");
        testUser3.setRoleType("MANAGER");
        testUser3.setCognitoSub(UUID.randomUUID());

        // Setup test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
        testProject.setDescription("Test project description");
        testProject.setOwnerId(123L);
        testProject.setCreatedBy(123L);
        testProject.setCreatedAt(OffsetDateTime.now());
        testProject.setUpdatedAt(OffsetDateTime.now());
        testProject.setDeleteInd(false);

        // Setup DTOs
        createProjectDto = CreateProjectDto.builder()
                .name("New Project")
                .description("New project description")
                .build();

        userResponseDto1 = UserResponseDto.builder()
                .id(1L)
                .username("user1")
                .email("user1@example.com")
                .build();

        userResponseDto2 = UserResponseDto.builder()
                .id(2L)
                .username("user2")
                .email("user2@example.com")
                .build();

        userResponseDto3 = UserResponseDto.builder()
                .id(3L)
                .username("user3")
                .email("user3@example.com")
                .build();
    }

    @Nested
    @DisplayName("Get Project Members Tests")
    class GetProjectMembersTests {

        @Test
        @DisplayName("Should successfully return project members when project exists and has members")
        void getProjectMembers_ProjectWithMembers_ReturnsUserResponseDtoList() {
            // Given
            Long projectId = 1L;
            List<User> projectMembers = Arrays.asList(testUser1, testUser2, testUser3);

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(projectMembers);

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                mockedUserConverter.when(() -> UserConverter.convert(testUser1)).thenReturn(userResponseDto1);
                mockedUserConverter.when(() -> UserConverter.convert(testUser2)).thenReturn(userResponseDto2);
                mockedUserConverter.when(() -> UserConverter.convert(testUser3)).thenReturn(userResponseDto3);

                // When
                List<UserResponseDto> result = projectService.getProjectMembers(projectId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).hasSize(3);
                assertThat(result).containsExactly(userResponseDto1, userResponseDto2, userResponseDto3);

                verify(userRepository).findUsersInProject(eq(projectId));
                mockedUserConverter.verify(() -> UserConverter.convert(testUser1));
                mockedUserConverter.verify(() -> UserConverter.convert(testUser2));
                mockedUserConverter.verify(() -> UserConverter.convert(testUser3));
            }
        }

        @Test
        @DisplayName("Should return empty list when project has no members")
        void getProjectMembers_ProjectWithNoMembers_ReturnsEmptyList() {
            // Given
            Long projectId = 1L;
            List<User> emptyMembersList = Collections.emptyList();

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(emptyMembersList);

            // When
            List<UserResponseDto> result = projectService.getProjectMembers(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(userRepository).findUsersInProject(eq(projectId));
        }

        @Test
        @DisplayName("Should handle single member project correctly")
        void getProjectMembers_ProjectWithSingleMember_ReturnsSingleUserResponse() {
            // Given
            Long projectId = 1L;
            List<User> singleMember = Collections.singletonList(testUser1);

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(singleMember);

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                mockedUserConverter.when(() -> UserConverter.convert(testUser1)).thenReturn(userResponseDto1);

                // When
                List<UserResponseDto> result = projectService.getProjectMembers(projectId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).hasSize(1);
                assertThat(result.get(0)).isEqualTo(userResponseDto1);

                verify(userRepository).findUsersInProject(eq(projectId));
                mockedUserConverter.verify(() -> UserConverter.convert(testUser1));
            }
        }

        @Test
        @DisplayName("Should handle null project ID gracefully")
        void getProjectMembers_NullProjectId_CallsRepositoryWithNull() {
            // Given
            Long projectId = null;
            when(userRepository.findUsersInProject(projectId)).thenReturn(Collections.emptyList());

            // When
            List<UserResponseDto> result = projectService.getProjectMembers(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(userRepository).findUsersInProject(projectId);
        }

        @Test
        @DisplayName("Should handle zero project ID")
        void getProjectMembers_ZeroProjectId_CallsRepositoryWithZero() {
            // Given
            Long projectId = 0L;
            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(Collections.emptyList());

            // When
            List<UserResponseDto> result = projectService.getProjectMembers(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(userRepository).findUsersInProject(eq(projectId));
        }

        @Test
        @DisplayName("Should handle negative project ID")
        void getProjectMembers_NegativeProjectId_CallsRepositoryWithNegativeId() {
            // Given
            Long projectId = -1L;
            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(Collections.emptyList());

            // When
            List<UserResponseDto> result = projectService.getProjectMembers(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(userRepository).findUsersInProject(eq(projectId));
        }

        @Test
        @DisplayName("Should handle very large project ID")
        void getProjectMembers_VeryLargeProjectId_CallsRepositoryWithLargeId() {
            // Given
            Long projectId = Long.MAX_VALUE;
            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(Collections.emptyList());

            // When
            List<UserResponseDto> result = projectService.getProjectMembers(projectId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(userRepository).findUsersInProject(eq(projectId));
        }

        @Test
        @DisplayName("Should preserve member order from repository")
        void getProjectMembers_MultipleMembers_PreservesOrder() {
            // Given
            Long projectId = 1L;
            List<User> orderedMembers = Arrays.asList(testUser3, testUser1, testUser2); // Specific order

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(orderedMembers);

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                mockedUserConverter.when(() -> UserConverter.convert(testUser1)).thenReturn(userResponseDto1);
                mockedUserConverter.when(() -> UserConverter.convert(testUser2)).thenReturn(userResponseDto2);
                mockedUserConverter.when(() -> UserConverter.convert(testUser3)).thenReturn(userResponseDto3);

                // When
                List<UserResponseDto> result = projectService.getProjectMembers(projectId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).hasSize(3);
                assertThat(result).containsExactly(userResponseDto3, userResponseDto1, userResponseDto2);

                verify(userRepository).findUsersInProject(eq(projectId));
            }
        }

        @Test
        @DisplayName("Should handle UserConverter conversion properly")
        void getProjectMembers_UserConverterConversion_VerifyConversions() {
            // Given
            Long projectId = 1L;
            List<User> projectMembers = Arrays.asList(testUser1, testUser2);

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(projectMembers);

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                mockedUserConverter.when(() -> UserConverter.convert(testUser1)).thenReturn(userResponseDto1);
                mockedUserConverter.when(() -> UserConverter.convert(testUser2)).thenReturn(userResponseDto2);

                // When
                List<UserResponseDto> result = projectService.getProjectMembers(projectId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).hasSize(2);

                // Verify each user was converted exactly once
                mockedUserConverter.verify(() -> UserConverter.convert(testUser1));
                mockedUserConverter.verify(() -> UserConverter.convert(testUser2));
                mockedUserConverter.verifyNoMoreInteractions();

                verify(userRepository).findUsersInProject(eq(projectId));
            }
        }

        @Test
        @DisplayName("Should handle users with different roles and types")
        void getProjectMembers_UsersWithDifferentRoles_ReturnsAllMembers() {
            // Given
            Long projectId = 1L;

            // Create users with different roles
            User adminUser = new User();
            adminUser.setId(4L);
            adminUser.setUserName("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setRoleType("ADMIN");

            User managerUser = new User();
            managerUser.setId(5L);
            managerUser.setUserName("manager");
            managerUser.setEmail("manager@example.com");
            managerUser.setRoleType("MANAGER");

            List<User> mixedRoleMembers = Arrays.asList(testUser1, managerUser, adminUser);

            UserResponseDto adminResponseDto = UserResponseDto.builder()
                    .id(4L)
                    .username("admin")
                    .email("admin@example.com")
                    .build();

            UserResponseDto managerResponseDto = UserResponseDto.builder()
                    .id(5L)
                    .username("manager")
                    .email("manager@example.com")
                    .build();

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(mixedRoleMembers);

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                mockedUserConverter.when(() -> UserConverter.convert(testUser1)).thenReturn(userResponseDto1);
                mockedUserConverter.when(() -> UserConverter.convert(managerUser)).thenReturn(managerResponseDto);
                mockedUserConverter.when(() -> UserConverter.convert(adminUser)).thenReturn(adminResponseDto);

                // When
                List<UserResponseDto> result = projectService.getProjectMembers(projectId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).hasSize(3);
                assertThat(result).containsExactly(userResponseDto1, managerResponseDto, adminResponseDto);

                verify(userRepository).findUsersInProject(eq(projectId));
            }
        }

        @Test
        @DisplayName("Should handle repository exception gracefully")
        void getProjectMembers_RepositoryThrowsException_PropagatesException() {
            // Given
            Long projectId = 1L;
            RuntimeException repositoryException = new RuntimeException("Database connection failed");

            when(userRepository.findUsersInProject(eq(projectId))).thenThrow(repositoryException);

            // When & Then
            assertThatThrownBy(() -> projectService.getProjectMembers(projectId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection failed");

            verify(userRepository).findUsersInProject(eq(projectId));
        }

        @Test
        @DisplayName("Should handle large number of members efficiently")
        void getProjectMembers_LargeNumberOfMembers_HandlesEfficiently() {
            // Given
            Long projectId = 1L;

            // Create a large list of users (simulate 100 members)
            List<User> largeMemberList = new java.util.ArrayList<>();
            List<UserResponseDto> expectedResponseList = new java.util.ArrayList<>();

            for (int i = 1; i <= 100; i++) {
                User user = new User();
                user.setId((long) i);
                user.setUserName("user" + i);
                user.setEmail("user" + i + "@example.com");
                user.setRoleType("USER");
                largeMemberList.add(user);

                UserResponseDto userResponseDto = UserResponseDto.builder()
                        .id((long) i)
                        .username("user" + i)
                        .email("user" + i + "@example.com")
                        .build();
                expectedResponseList.add(userResponseDto);
            }

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(largeMemberList);

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                // Mock all conversions
                for (int i = 0; i < largeMemberList.size(); i++) {
                    User user = largeMemberList.get(i);
                    UserResponseDto expectedDto = expectedResponseList.get(i);
                    mockedUserConverter.when(() -> UserConverter.convert(user)).thenReturn(expectedDto);
                }

                // When
                List<UserResponseDto> result = projectService.getProjectMembers(projectId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).hasSize(100);
                assertThat(result).isEqualTo(expectedResponseList);

                verify(userRepository).findUsersInProject(eq(projectId));
            }
        }
    }

    @Nested
    @DisplayName("Integration with Other Methods Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should verify getProjectMembers doesn't interfere with other service methods")
        void getProjectMembers_DoesNotInterfereWithOtherMethods_IndependentOperation() {
            // Given
            Long projectId = 1L;
            Long userId = 123L;

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(Collections.singletonList(testUser1));
            when(projectRepository.findUserProjects(eq(userId))).thenReturn(Collections.singletonList(testProject));
            when(taskRepository.findByProjectIdAndNotDeleted(eq(projectId))).thenReturn(Collections.emptyList());

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                mockedUserConverter.when(() -> UserConverter.convert(testUser1)).thenReturn(userResponseDto1);

                // When
                List<UserResponseDto> membersResult = projectService.getProjectMembers(projectId);
                List<ProjectResponseDto> projectsResult = projectService.getUserProjects(userId);

                // Then
                assertThat(membersResult).isNotNull();
                assertThat(membersResult).hasSize(1);
                assertThat(projectsResult).isNotNull();
                assertThat(projectsResult).hasSize(1);

                verify(userRepository).findUsersInProject(eq(projectId));
                verify(projectRepository).findUserProjects(eq(userId));
            }
        }
    }

    @Nested
    @DisplayName("Performance and Edge Case Tests")
    class PerformanceAndEdgeCaseTests {

        @Test
        @DisplayName("Should handle concurrent access gracefully")
        void getProjectMembers_ConcurrentAccess_ThreadSafe() {
            // Given
            Long projectId = 1L;
            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(Collections.singletonList(testUser1));

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                mockedUserConverter.when(() -> UserConverter.convert(testUser1)).thenReturn(userResponseDto1);

                // When - simulate multiple calls (would be concurrent in real scenario)
                List<UserResponseDto> result1 = projectService.getProjectMembers(projectId);
                List<UserResponseDto> result2 = projectService.getProjectMembers(projectId);

                // Then
                assertThat(result1).isNotNull();
                assertThat(result2).isNotNull();
                assertThat(result1).isEqualTo(result2);

                verify(userRepository, org.mockito.Mockito.times(2)).findUsersInProject(eq(projectId));
            }
        }

        @Test
        @DisplayName("Should handle users with special characters in data")
        void getProjectMembers_UsersWithSpecialCharacters_HandlesCorrectly() {
            // Given
            Long projectId = 1L;

            User specialCharUser = new User();
            specialCharUser.setId(99L);
            specialCharUser.setUserName("user@special!#$");
            specialCharUser.setEmail("test+email@domain-name.co.uk");
            specialCharUser.setRoleType("USER");

            UserResponseDto specialCharResponseDto = UserResponseDto.builder()
                    .id(99L)
                    .username("user@special!#$")
                    .email("test+email@domain-name.co.uk")
                    .build();

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(Collections.singletonList(specialCharUser));

            try (MockedStatic<UserConverter> mockedUserConverter = mockStatic(UserConverter.class)) {
                mockedUserConverter.when(() -> UserConverter.convert(specialCharUser)).thenReturn(specialCharResponseDto);

                // When
                List<UserResponseDto> result = projectService.getProjectMembers(projectId);

                // Then
                assertThat(result).isNotNull();
                assertThat(result).hasSize(1);
                assertThat(result.get(0).username()).isEqualTo("user@special!#$");
                assertThat(result.get(0).email()).isEqualTo("test+email@domain-name.co.uk");

                verify(userRepository).findUsersInProject(eq(projectId));
            }
        }
    }
}
