package com.spmorangle.crm.projectmanagement.service.impl;

import com.spmorangle.common.converter.UserConverter;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.model.Department;
import com.spmorangle.crm.departmentmgmt.repository.DepartmentRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
import com.spmorangle.crm.projectmanagement.dto.CreateProjectDto;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.model.Project;
import com.spmorangle.crm.projectmanagement.repository.ProjectMemberRepository;
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
import static org.mockito.Mockito.lenient;
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

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private DepartmentQueryService departmentQueryService;

    @Mock
    private DepartmentalVisibilityService departmentalVisibilityService;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

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

        // Mock DepartmentQueryService to return department names (lenient for tests that don't use it)
        lenient().when(departmentQueryService.getById(any())).thenAnswer(invocation -> {
            Long deptId = invocation.getArgument(0);
            String deptName = switch (deptId.intValue()) {
                case 100 -> "Engineering";
                case 200 -> "HR";
                case 300 -> "Marketing";
                default -> "Dept " + deptId;
            };
            return Optional.of(DepartmentDto.builder()
                .id(deptId)
                .name(deptName)
                .build());
        });

        // Mock DepartmentalVisibilityService
        lenient().when(departmentalVisibilityService.visibleDepartmentsForAssignedDept(any())).thenAnswer(invocation -> {
            Long deptId = invocation.getArgument(0);
            if (deptId == null) {
                // Users with no department can see all departments (0L as wildcard)
                return java.util.Set.of(0L, 100L, 200L, 300L);
            }
            return java.util.Set.of(deptId);
        });

        // Mock canUserSeeTask to return true by default (lenient for tests that don't use it)
        lenient().when(departmentalVisibilityService.canUserSeeTask(any(), any())).thenReturn(true);

        // Mock ProjectMemberRepository to return empty list by default (lenient for tests that don't use it)
        lenient().when(projectMemberRepository.findByProjectId(any())).thenReturn(Collections.emptyList());

        // Mock userRepository.findById to return a user for any ID (lenient for tests that don't use it)
        // This is needed for getUserProjects which checks project owner/member departments
        lenient().when(userRepository.findById(any())).thenAnswer(invocation -> {
            Long userId = invocation.getArgument(0);
            User user = new User();
            user.setId(userId);
            user.setUserName("user" + userId);
            user.setEmail("user" + userId + "@example.com");
            user.setRoleType("STAFF");
            user.setDepartmentId(100L); // Default to department 100L
            user.setIsActive(true);
            return Optional.of(user);
        });
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

            // Create user for getUserProjects (needs role and department)
            User testUserForGetProjects = new User();
            testUserForGetProjects.setId(userId);
            testUserForGetProjects.setUserName("testuser");
            testUserForGetProjects.setEmail("test@example.com");
            testUserForGetProjects.setRoleType("STAFF");
            testUserForGetProjects.setDepartmentId(100L); // Engineering department

            when(userRepository.findUsersInProject(eq(projectId))).thenReturn(Collections.singletonList(testUser1));
            when(userRepository.findById(eq(userId))).thenReturn(Optional.of(testUserForGetProjects));
            when(projectRepository.findUserProjects(eq(userId))).thenReturn(Collections.singletonList(testProject));
            when(projectRepository.findById(eq(projectId))).thenReturn(Optional.of(testProject));
            when(projectMemberRepository.findByProjectId(eq(projectId))).thenReturn(Collections.emptyList());
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());

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

    @Nested
    @DisplayName("Get User Projects with Permissions Tests")
    class GetUserProjectsWithPermissionsTests {

        private User staffUser;
        private User managerUser;
        private User hrUser;
        private Project ownedProject;
        private Project memberProject;
        private Project crossDeptProject;
        private Project unrelatedProject;

        @BeforeEach
        void setUpPermissionTests() {
            // Setup STAFF user
            staffUser = new User();
            staffUser.setId(100L);
            staffUser.setUserName("staff_user");
            staffUser.setEmail("staff@example.com");
            staffUser.setRoleType("STAFF");
            staffUser.setDepartmentId(100L); // Engineering department
            staffUser.setIsActive(true);

            // Setup MANAGER user
            managerUser = new User();
            managerUser.setId(200L);
            managerUser.setUserName("manager_user");
            managerUser.setEmail("manager@example.com");
            managerUser.setRoleType("MANAGER");
            managerUser.setDepartmentId(100L); // Engineering department
            managerUser.setIsActive(true);

            // Setup HR user
            hrUser = new User();
            hrUser.setId(300L);
            hrUser.setUserName("hr_user");
            hrUser.setEmail("hr@example.com");
            hrUser.setRoleType("HR");
            hrUser.setDepartmentId(200L); // HR department
            hrUser.setIsActive(true);

            // Setup project owned by manager
            ownedProject = new Project();
            ownedProject.setId(1L);
            ownedProject.setName("Owned Project");
            ownedProject.setDescription("Project owned by manager");
            ownedProject.setOwnerId(200L); // Manager owns this
            ownedProject.setCreatedBy(200L);
            ownedProject.setDeleteInd(false);

            // Setup project where user is member
            memberProject = new Project();
            memberProject.setId(2L);
            memberProject.setName("Member Project");
            memberProject.setDescription("Project where user is member");
            memberProject.setOwnerId(999L); // Someone else owns this
            memberProject.setCreatedBy(999L);
            memberProject.setDeleteInd(false);

            // Setup cross-department project (Engineering staff is member, but it's owned by Marketing)
            crossDeptProject = new Project();
            crossDeptProject.setId(3L);
            crossDeptProject.setName("Cross Dept Project");
            crossDeptProject.setDescription("Marketing project with Engineering staff");
            crossDeptProject.setOwnerId(888L); // Marketing manager owns this
            crossDeptProject.setCreatedBy(888L);
            crossDeptProject.setDeleteInd(false);

            // Setup lenient mocks for findById to support isUserProjectMember checks
            lenient().when(projectRepository.findById(1L)).thenReturn(Optional.of(ownedProject));
            lenient().when(projectRepository.findById(2L)).thenReturn(Optional.of(memberProject));
            lenient().when(projectRepository.findById(3L)).thenReturn(Optional.of(crossDeptProject));

            // Setup unrelated project (no connection to user or department)
            unrelatedProject = new Project();
            unrelatedProject.setId(4L);
            unrelatedProject.setName("Unrelated Project");
            unrelatedProject.setDescription("Sales project");
            unrelatedProject.setOwnerId(777L);
            unrelatedProject.setCreatedBy(777L);
            unrelatedProject.setDeleteInd(false);

            // Setup department mocks
            Department engineeringDept = new Department();
            engineeringDept.setId(1L);
            engineeringDept.setName("Engineering");
            
            Department hrDept = new Department();
            hrDept.setId(2L);
            hrDept.setName("HR");
            
            DepartmentDto engineeringDeptDto = DepartmentDto.builder()
                    .id(1L)
                    .name("Engineering")
                    .build();
                    
            DepartmentDto hrDeptDto = DepartmentDto.builder()
                    .id(2L)
                    .name("HR")
                    .build();
            
            lenient().when(departmentRepository.findByNameIgnoreCase("Engineering"))
                    .thenReturn(Optional.of(engineeringDept));
            lenient().when(departmentRepository.findByNameIgnoreCase("HR"))
                    .thenReturn(Optional.of(hrDept));
            lenient().when(departmentQueryService.getDescendants(1L, true))
                    .thenReturn(Collections.singletonList(engineeringDeptDto));
            lenient().when(departmentQueryService.getDescendants(2L, true))
                    .thenReturn(Collections.singletonList(hrDeptDto));
        }

        @Test
        @DisplayName("STAFF should return only projects where they are members")
        void getUserProjects_Staff_ReturnsOnlyMemberProjects() {
            // Given
            Long staffUserId = 100L;
            List<Project> expectedProjects = Arrays.asList(memberProject, ownedProject);

            when(userRepository.findById(staffUserId)).thenReturn(Optional.of(staffUser));
            when(projectRepository.findUserProjects(staffUserId)).thenReturn(expectedProjects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(staffUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.stream().map(ProjectResponseDto::getId))
                    .containsExactlyInAnyOrder(1L, 2L);

            verify(projectRepository).findUserProjects(staffUserId);
        }

        @Test
        @DisplayName("STAFF should see related cross-department projects where colleagues are members")
        void getUserProjects_Staff_IncludesRelatedProjects() {
            // Given
            Long staffUserId = 100L;
            List<Project> memberProjects = Collections.singletonList(memberProject);
            List<Project> relatedProjects = Collections.singletonList(crossDeptProject); // Staff should now see this

            when(userRepository.findById(staffUserId)).thenReturn(Optional.of(staffUser));
            when(projectRepository.findUserProjects(staffUserId)).thenReturn(memberProjects);
            when(projectRepository.findProjectsWithDepartmentStaff(eq(staffUserId), any()))
                    .thenReturn(relatedProjects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());
            when(departmentQueryService.getById(100L))
                    .thenReturn(Optional.of(DepartmentDto.builder().id(100L).name("Engineering").build()));
            when(departmentalVisibilityService.visibleDepartmentsForAssignedDept(100L))
                    .thenReturn(Collections.singleton(100L));
            when(projectMemberRepository.findByProjectId(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(staffUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2); // memberProject + crossDeptProject
            assertThat(result.stream().map(ProjectResponseDto::getId))
                    .containsExactlyInAnyOrder(2L, 3L);

            // Verify related project is marked with isRelated = true
            ProjectResponseDto relatedProjectDto = result.stream()
                    .filter(p -> p.getId() == 3L)
                    .findFirst()
                    .orElse(null);
            assertThat(relatedProjectDto).isNotNull();
            assertThat(relatedProjectDto.getIsRelated()).isTrue();
        }

        @Test
        @DisplayName("MANAGER should return projects where they are members")
        void getUserProjects_Manager_ReturnsMemberProjects() {
            // Given
            Long managerId = 200L;
            List<Project> memberProjects = Arrays.asList(ownedProject, memberProject);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(managerUser));
            when(projectRepository.findUserProjects(managerId)).thenReturn(memberProjects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(managerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSizeGreaterThanOrEqualTo(2);
            assertThat(result.stream().map(ProjectResponseDto::getId))
                    .contains(1L, 2L);
        }

        @Test
        @DisplayName("MANAGER should return related cross-department projects")
        void getUserProjects_Manager_ReturnsRelatedCrossDeptProjects() {
            // Given
            Long managerId = 200L;
            Long deptId = 100L;
            List<Project> memberProjects = Collections.singletonList(ownedProject);
            List<Project> relatedProjects = Collections.singletonList(crossDeptProject);

            // Mock department visibility
            DepartmentDto managerDept = DepartmentDto.builder()
                    .id(deptId)
                    .name("Engineering")
                    .build();
            when(departmentQueryService.getById(deptId)).thenReturn(Optional.of(managerDept));
            when(departmentalVisibilityService.visibleDepartmentsForAssignedDept(deptId))
                    .thenReturn(Collections.singleton(deptId));
            when(departmentalVisibilityService.canUserSeeTask(any(), any())).thenReturn(true);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(managerUser));
            when(projectRepository.findUserProjects(managerId)).thenReturn(memberProjects);
            when(projectRepository.findProjectsWithDepartmentStaff(eq(managerId), eq(Collections.singleton(deptId))))
                    .thenReturn(relatedProjects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());
            when(projectMemberRepository.findByProjectId(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(managerId);

            // Then
            assertThat(result).isNotNull();
            // Should contain both member projects and related cross-dept projects
            assertThat(result.stream().map(ProjectResponseDto::getId))
                    .contains(1L, 3L);
        }

        @Test
        @DisplayName("MANAGER should NOT see unrelated cross-department projects")
        void getUserProjects_Manager_ExcludesUnrelatedCrossDeptProjects() {
            // Given
            Long managerId = 200L;
            Long deptId = 100L;
            List<Project> memberProjects = Collections.singletonList(ownedProject);
            List<Project> relatedProjects = Collections.emptyList(); // No staff from Engineering in other projects

            // Mock department visibility
            DepartmentDto managerDept = DepartmentDto.builder()
                    .id(deptId)
                    .name("Engineering")
                    .build();
            when(departmentQueryService.getById(deptId)).thenReturn(Optional.of(managerDept));
            when(departmentalVisibilityService.visibleDepartmentsForAssignedDept(deptId))
                    .thenReturn(Collections.singleton(deptId));

            when(userRepository.findById(managerId)).thenReturn(Optional.of(managerUser));
            when(projectRepository.findUserProjects(managerId)).thenReturn(memberProjects);
            when(projectRepository.findProjectsWithDepartmentStaff(eq(managerId), eq(Collections.singleton(deptId))))
                    .thenReturn(relatedProjects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(managerId);

            // Then
            assertThat(result).isNotNull();
            // Should NOT contain unrelated project (Sales project)
            assertThat(result.stream().map(ProjectResponseDto::getId))
                    .doesNotContain(4L);
        }

        @Test
        @DisplayName("HR should return only member projects, not all projects")
        void getUserProjects_HR_ReturnsMemberProjectsOnly() {
            // Given
            Long hrUserId = 300L;
            List<Project> hrMemberProjects = Collections.singletonList(memberProject);

            when(userRepository.findById(hrUserId)).thenReturn(Optional.of(hrUser));
            when(projectRepository.findUserProjects(hrUserId)).thenReturn(hrMemberProjects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(hrUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(2L);

            // HR should NOT see all projects through this endpoint
            assertThat(result.stream().map(ProjectResponseDto::getId))
                    .doesNotContain(1L, 3L, 4L);
        }

        @Test
        @DisplayName("Should correctly set isOwner flag when user owns project")
        void getUserProjects_VerifyIsOwnerFlag() {
            // Given
            Long managerId = 200L;
            List<Project> projects = Arrays.asList(ownedProject, memberProject);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(managerUser));
            when(projectRepository.findUserProjects(managerId)).thenReturn(projects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(managerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);

            // Find owned project in results
            ProjectResponseDto ownedResult = result.stream()
                    .filter(p -> p.getId().equals(1L))
                    .findFirst()
                    .orElseThrow();

            // Find member project in results
            ProjectResponseDto memberResult = result.stream()
                    .filter(p -> p.getId().equals(2L))
                    .findFirst()
                    .orElseThrow();

            // Owned project should have isOwner = true
            assertThat(ownedResult.getIsOwner()).isTrue();

            // Member project should have isOwner = false
            assertThat(memberResult.getIsOwner()).isFalse();
        }

        @Test
        @DisplayName("Should correctly set isRelated flag for cross-department projects")
        void getUserProjects_VerifyIsRelatedFlag() {
            // Given
            Long managerId = 200L;
            Long deptId = 100L;
            List<Project> memberProjects = Collections.singletonList(ownedProject);
            List<Project> relatedProjects = Collections.singletonList(crossDeptProject);

            // Mock department visibility
            DepartmentDto managerDept = DepartmentDto.builder()
                    .id(deptId)
                    .name("Engineering")
                    .build();
            when(departmentQueryService.getById(deptId)).thenReturn(Optional.of(managerDept));
            when(departmentalVisibilityService.visibleDepartmentsForAssignedDept(deptId))
                    .thenReturn(Collections.singleton(deptId));
            when(departmentalVisibilityService.canUserSeeTask(any(), any())).thenReturn(true);

            when(userRepository.findById(managerId)).thenReturn(Optional.of(managerUser));
            when(projectRepository.findUserProjects(managerId)).thenReturn(memberProjects);
            when(projectRepository.findProjectsWithDepartmentStaff(eq(managerId), eq(Collections.singleton(deptId))))
                    .thenReturn(relatedProjects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());
            when(projectMemberRepository.findByProjectId(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(managerId);

            // Then
            assertThat(result).isNotNull();

            // Find member project (should have isRelated = false)
            ProjectResponseDto memberResult = result.stream()
                    .filter(p -> p.getId().equals(1L))
                    .findFirst()
                    .orElseThrow();

            // Find cross-dept project (should have isRelated = true)
            ProjectResponseDto relatedResult = result.stream()
                    .filter(p -> p.getId().equals(3L))
                    .findFirst()
                    .orElseThrow();

            assertThat(memberResult.getIsRelated()).isFalse();
            assertThat(relatedResult.getIsRelated()).isTrue();
        }

        @Test
        @DisplayName("Should include department name in project response")
        void getUserProjects_VerifyDepartmentNameIncluded() {
            // Given
            Long managerId = 200L;
            List<Project> projects = Collections.singletonList(ownedProject);

            // Mock owner user with department
            User ownerUser = new User();
            ownerUser.setId(200L);
            ownerUser.setDepartmentId(100L); // Engineering department

            when(userRepository.findById(managerId)).thenReturn(Optional.of(managerUser));
            when(userRepository.findById(200L)).thenReturn(Optional.of(ownerUser));
            when(projectRepository.findUserProjects(managerId)).thenReturn(projects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(managerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getDepartmentName()).isEqualTo("Engineering");
        }

        @Test
        @DisplayName("Should handle user with no department gracefully")
        void getUserProjects_UserWithNoDepartment() {
            // Given
            User noDeptUser = new User();
            noDeptUser.setId(400L);
            noDeptUser.setUserName("no_dept_user");
            noDeptUser.setRoleType("STAFF");
            noDeptUser.setDepartmentId(null); // No department
            noDeptUser.setIsActive(true);

            List<Project> projects = Collections.singletonList(memberProject);

            when(userRepository.findById(400L)).thenReturn(Optional.of(noDeptUser));
            when(projectRepository.findUserProjects(400L)).thenReturn(projects);
            when(taskRepository.findByProjectIdAndNotDeleted(any())).thenReturn(Collections.emptyList());

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(400L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            // Should handle null department gracefully
        }

        @Test
        @DisplayName("Should return empty list when user has no projects")
        void getUserProjects_EmptyResults() {
            // Given
            Long staffUserId = 100L;
            List<Project> emptyProjects = Collections.emptyList();

            when(userRepository.findById(staffUserId)).thenReturn(Optional.of(staffUser));
            when(projectRepository.findUserProjects(staffUserId)).thenReturn(emptyProjects);

            // When
            List<ProjectResponseDto> result = projectService.getUserProjects(staffUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }
    }
}
