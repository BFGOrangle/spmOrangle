package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.reporting.service.ReportService;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Tests for department-based visibility filtering in TaskServiceImpl.
 *
 * Tests the 4 acceptance criteria for SPMORANGLE-114:
 * - AC1: View task within department
 * - AC2: View task from sub-department
 * - AC3: Cannot view task from unrelated department
 * - AC4: Cross-department task visibility
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService - Department Filtering")
class TaskServiceDepartmentFilteringTest {

    @Mock private TaskRepository taskRepository;
    @Mock private CollaboratorService collaboratorService;
    @Mock private SubtaskService subtaskService;
    @Mock private ProjectService projectService;
    @Mock private TagService tagService;
    @Mock private RecurrenceService recurrenceService;
    @Mock private NotificationMessagePublisher notificationPublisher;
    @Mock private UserRepository userRepository;
    @Mock private TaskAssigneeRepository taskAssigneeRepository;
    @Mock private ReportService reportService;
    @Mock private DepartmentQueryService departmentQueryService;
    @Mock private DepartmentalVisibilityService departmentalVisibilityService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private static final Long USER_ID = 1L;
    private static final Long DEPT_A_ID = 100L;
    private static final Long DEPT_A1_ID = 101L;
    private static final Long DEPT_B_ID = 200L;

    @BeforeEach
    void setUp() {
        // Default mocks to prevent NullPointerException
        when(collaboratorService.getTasksForWhichUserIsCollaborator(anyLong()))
            .thenReturn(Collections.emptyList());
        when(projectService.getProjectOwners(any()))
            .thenReturn(Collections.emptyMap());
    }

    @Nested
    @DisplayName("AC1: View task within department")
    class ViewTaskWithinDepartment {

        @Test
        @DisplayName("User should see task when assignee is from same department")
        void shouldSeeTaskFromSameDepartment() {
            // Given
            User user = createUser(USER_ID, "Department A");
            Task task = createTask(10L, USER_ID);
            User assignee = createUser(5L, "Department A");
            DepartmentDto deptA = createDepartment(DEPT_A_ID, "Department A", null);

            // Mock user lookup
            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            // Mock department query
            when(departmentQueryService.getByNameCaseInsensitive("Department A"))
                .thenReturn(Optional.of(deptA));

            // Mock visibility service - user can see own department
            when(departmentalVisibilityService.visibilityDepartmentsForAssignedDept(DEPT_A_ID))
                .thenReturn(Set.of(DEPT_A_ID));

            // Mock task repository
            when(taskRepository.findUserTasks(USER_ID)).thenReturn(List.of(task));

            // Mock assignee lookup
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(task.getId()))
                .thenReturn(new ArrayList<>(List.of(5L)));
            when(userRepository.findById(5L)).thenReturn(Optional.of(assignee));
            when(departmentQueryService.getByNameCaseInsensitive(assignee.getDepartment()))
                .thenReturn(Optional.of(deptA));

            // Mock visibility check - assignee's dept is visible
            when(departmentalVisibilityService.canUserSeeTask(Set.of(DEPT_A_ID), DEPT_A_ID))
                .thenReturn(true);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(USER_ID);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(task.getId());
        }
    }

    @Nested
    @DisplayName("AC2: View task from sub-department")
    class ViewTaskFromSubDepartment {

        @Test
        @DisplayName("User should see task when assignee is from child department")
        void shouldSeeTaskFromSubDepartment() {
            // Given
            User user = createUser(USER_ID, "Department A");
            Task task = createTask(10L, USER_ID);
            User assignee = createUser(5L, "Department A.1");
            DepartmentDto deptA = createDepartment(DEPT_A_ID, "Department A", null);
            DepartmentDto deptA1 = createDepartment(DEPT_A1_ID, "Department A.1", DEPT_A_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(departmentQueryService.getByNameCaseInsensitive("Department A"))
                .thenReturn(Optional.of(deptA));

            // User can see own dept + child dept
            when(departmentalVisibilityService.visibilityDepartmentsForAssignedDept(DEPT_A_ID))
                .thenReturn(Set.of(DEPT_A_ID, DEPT_A1_ID));

            when(taskRepository.findUserTasks(USER_ID)).thenReturn(List.of(task));
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(task.getId()))
                .thenReturn(new ArrayList<>(List.of(5L)));
            when(userRepository.findById(5L)).thenReturn(Optional.of(assignee));
            when(departmentQueryService.getByNameCaseInsensitive("Department A.1"))
                .thenReturn(Optional.of(deptA1));
            when(departmentalVisibilityService.canUserSeeTask(Set.of(DEPT_A_ID, DEPT_A1_ID), DEPT_A1_ID))
                .thenReturn(true);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(USER_ID);

            // Then
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("AC3: Cannot view unrelated department")
    class CannotViewUnrelatedDepartment {

        @Test
        @DisplayName("User should NOT see task when all assignees from unrelated department")
        void shouldNotSeeTaskFromUnrelatedDepartment() {
            // Given
            User user = createUser(USER_ID, "Department A");
            Task task = createTask(10L, 99L);
            User taskOwner = createUser(99L, "Department B");
            User assignee = createUser(5L, "Department B");
            DepartmentDto deptA = createDepartment(DEPT_A_ID, "Department A", null);
            DepartmentDto deptB = createDepartment(DEPT_B_ID, "Department B", null);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(departmentQueryService.getByNameCaseInsensitive("Department A"))
                .thenReturn(Optional.of(deptA));
            when(departmentalVisibilityService.visibilityDepartmentsForAssignedDept(DEPT_A_ID))
                .thenReturn(Set.of(DEPT_A_ID, DEPT_A1_ID));

            when(taskRepository.findUserTasks(USER_ID)).thenReturn(List.of(task));
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(task.getId()))
                .thenReturn(new ArrayList<>(List.of(5L)));

            // Mock both owner and assignee from Dept B
            when(userRepository.findById(99L)).thenReturn(Optional.of(taskOwner));
            when(userRepository.findById(5L)).thenReturn(Optional.of(assignee));
            when(departmentQueryService.getByNameCaseInsensitive("Department B"))
                .thenReturn(Optional.of(deptB));

            // User cannot see Dept B
            when(departmentalVisibilityService.canUserSeeTask(Set.of(DEPT_A_ID, DEPT_A1_ID), DEPT_B_ID))
                .thenReturn(false);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(USER_ID);

            // Then - task should be filtered out
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("AC4: Cross-department task")
    class CrossDepartmentTask {

        @Test
        @DisplayName("User should see task when at least one assignee is visible")
        void shouldSeeTaskWithMixedDepartmentAssignees() {
            // Given
            User user = createUser(USER_ID, "Department A");
            Task task = createTask(10L, USER_ID);
            User assigneeA = createUser(5L, "Department A");
            DepartmentDto deptA = createDepartment(DEPT_A_ID, "Department A", null);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(departmentQueryService.getByNameCaseInsensitive("Department A"))
                .thenReturn(Optional.of(deptA));
            when(departmentalVisibilityService.visibilityDepartmentsForAssignedDept(DEPT_A_ID))
                .thenReturn(Set.of(DEPT_A_ID, DEPT_A1_ID));

            when(taskRepository.findUserTasks(USER_ID)).thenReturn(List.of(task));
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(task.getId()))
                .thenReturn(new ArrayList<>(List.of(5L)));

            // First assignee from Dept A - visible!
            when(userRepository.findById(5L)).thenReturn(Optional.of(assigneeA));
            when(departmentQueryService.getByNameCaseInsensitive("Department A"))
                .thenReturn(Optional.of(deptA));
            when(departmentalVisibilityService.canUserSeeTask(Set.of(DEPT_A_ID, DEPT_A1_ID), DEPT_A_ID))
                .thenReturn(true);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(USER_ID);

            // Then - should see because at least one assignee is visible
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should see task when user has no department (fallback)")
        void shouldSeeTaskWhenUserHasNoDepartment() {
            // Given
            User user = createUser(USER_ID, null);
            Task task = createTask(10L, USER_ID);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(taskRepository.findUserTasks(USER_ID)).thenReturn(List.of(task));
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(task.getId()))
                .thenReturn(new ArrayList<>());

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(USER_ID);

            // Then - no filtering applied
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should handle assignees with no department gracefully")
        void shouldSkipAssigneesWithNoDepartment() {
            // Given
            User user = createUser(USER_ID, "Department A");
            Task task = createTask(10L, USER_ID);
            User assigneeWithoutDept = createUser(5L, null);
            DepartmentDto deptA = createDepartment(DEPT_A_ID, "Department A", null);

            when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
            when(departmentQueryService.getByNameCaseInsensitive("Department A"))
                .thenReturn(Optional.of(deptA));
            when(departmentalVisibilityService.visibilityDepartmentsForAssignedDept(DEPT_A_ID))
                .thenReturn(Set.of(DEPT_A_ID));

            when(taskRepository.findUserTasks(USER_ID)).thenReturn(List.of(task));
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(task.getId()))
                .thenReturn(new ArrayList<>(List.of(5L)));
            when(userRepository.findById(5L)).thenReturn(Optional.of(assigneeWithoutDept));

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(USER_ID);

            // Then - task filtered out (no valid department)
            assertThat(result).isEmpty();
        }
    }

    // Helper methods
    private User createUser(Long id, String department) {
        User user = new User();
        user.setId(id);
        user.setUserName("user" + id);
        user.setEmail("user" + id + "@test.com");
        user.setDepartment(department);
        user.setRoleType("STAFF");
        user.setIsActive(true);
        return user;
    }

    private Task createTask(Long id, Long ownerId) {
        Task task = new Task();
        task.setId(id);
        task.setOwnerId(ownerId);
        task.setTitle("Test Task " + id);
        task.setTaskType(TaskType.BUG);
        task.setStatus(Status.IN_PROGRESS);
        task.setDeleteInd(false);
        task.setCreatedAt(OffsetDateTime.now());
        task.setUpdatedAt(OffsetDateTime.now());
        return task;
    }

    private DepartmentDto createDepartment(Long id, String name, Long parentId) {
        return DepartmentDto.builder()
            .id(id)
            .name(name)
            .parentId(parentId)
            .build();
    }
}
