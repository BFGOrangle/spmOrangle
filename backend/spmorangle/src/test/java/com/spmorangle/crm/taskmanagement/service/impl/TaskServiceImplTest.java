package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.projectmanagement.dto.ProjectResponseDto;
import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Tag;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;
import com.spmorangle.crm.taskmanagement.service.TagService;
import com.spmorangle.crm.taskmanagement.service.RecurrenceService;
import com.spmorangle.crm.reporting.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskServiceImpl Tests")
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private SubtaskService subtaskService;

    @Mock
    private ProjectService projectService;

    @Mock
    private TagService tagService;

    @Mock
    private NotificationMessagePublisher notificationPublisher;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @Mock
    private RecurrenceService recurrenceService;

    @Mock
    private ReportService reportService;

    @Mock
    private DepartmentQueryService departmentQueryService;

    @Mock
    private DepartmentalVisibilityService departmentalVisibilityService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private OffsetDateTime fixedDateTime;

    @BeforeEach
    void setUp() {
        fixedDateTime = OffsetDateTime.now();

        testTask1 = createTestTask(1L, 101L, 201L, "Task 1", "Description 1",
                                  Status.TODO, Arrays.asList("tag1", "tag2"));

        testTask2 = createTestTask(2L, 102L, 201L, "Task 2", "Description 2",
                                  Status.IN_PROGRESS, Collections.singletonList("tag3"));

        testTask3 = createTestTask(3L, 103L, 201L, "Task 3", null,
                                  Status.COMPLETED, Collections.emptyList());

        // Mock subtaskService to return empty lists for all task IDs (lenient for tests that don't use it)
        lenient().when(subtaskService.getSubtasksByTaskId(anyLong(), anyLong())).thenReturn(Collections.emptyList());

        lenient().when(projectService.getProjectOwners(any())).thenReturn(Collections.emptyMap());
        // Mock isUserProjectMember to return true by default (user is a member of the project)
        lenient().when(projectService.isUserProjectMember(anyLong(), anyLong())).thenReturn(true);
        lenient().when(projectService.getProjectsByIds(any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            Set<Long> ids = (Set<Long>) invocation.getArgument(0);
            if (ids == null) {
                return Collections.emptyList();
            }
            return ids.stream()
                    .map(id -> ProjectResponseDto.builder()
                            .id(id)
                            .name("Project " + id)
                            .description(null)
                            .ownerId(0L)
                            .createdAt(null)
                            .updatedAt(null)
                            .taskCount(0)
                            .completedTaskCount(0)
                            .build())
                    .toList();
        });

        lenient().when(userRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Optional.of(createUser(id));
        });

        lenient().when(userRepository.findAllById(any())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            if (ids == null) {
                return Collections.emptyList();
            }
            List<User> users = new ArrayList<>();
            for (Long id : ids) {
                users.add(createUser(id));
            }
            return users;
        });

        // Mock findById to support department visibility filtering
        lenient().when(userRepository.findById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return Optional.of(createUser(id));
        });

        // Mock DepartmentQueryService to return department names
        lenient().when(departmentQueryService.getById(anyLong())).thenAnswer(invocation -> {
            Long deptId = invocation.getArgument(0);
            return Optional.of(DepartmentDto.builder()
                .id(deptId)
                .name("Dept " + deptId)
                .build());
        });

        // Mock DepartmentalVisibilityService
        lenient().when(departmentalVisibilityService.visibleDepartmentsForAssignedDept(anyLong())).thenAnswer(invocation -> {
            Long deptId = invocation.getArgument(0);
            // Return the department itself and potentially child departments
            return deptId != null ? Set.of(deptId) : Set.of();
        });

        lenient().when(departmentalVisibilityService.canUserSeeTask(any(), anyLong())).thenReturn(true);
    }

    private Task createTestTask(Long id, Long projectId, Long ownerId, String title,
                               String description, Status status, List<String> tagNames) {
        Task task = new Task();
        task.setId(id);
        task.setProjectId(projectId);
        task.setOwnerId(ownerId);
        task.setTaskType(TaskType.FEATURE);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setTags(createTagsFromNames(tagNames));
        task.setCreatedBy(ownerId);
        task.setCreatedAt(fixedDateTime);
        task.setUpdatedAt(fixedDateTime);
        task.setUpdatedBy(ownerId);
        return task;
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUserName("Owner " + id);
        user.setDepartmentId((id % 3) * 100L); // Department IDs: 0, 100, 200
        user.setRoleType("STAFF");
        user.setCognitoSub(UUID.randomUUID());
        return user;
    }

    private Set<Tag> createTagsFromNames(List<String> tagNames) {
        if (tagNames == null) {
            return null;
        }
        return tagNames.stream().map(name -> {
            Tag tag = new Tag();
            tag.setTagName(name);
            return tag;
        }).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    @Nested
    @DisplayName("getTasks Tests")
    class GetTasksTests {

        @Test
        @DisplayName("Should return list of tasks for valid user ID")
        void getTasks_ValidUserId_ReturnsTaskList() {
            // Given
            Long userId = 201L;
            List<Task> expectedTasks = Arrays.asList(testTask1, testTask2, testTask3);
            when(taskRepository.findUserTasks(userId)).thenReturn(expectedTasks);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);

            // Verify first task
            TaskResponseDto firstTask = result.get(0);
            assertThat(firstTask.getId()).isEqualTo(1L);
            assertThat(firstTask.getProjectId()).isEqualTo(101L);
            assertThat(firstTask.getProjectName()).isEqualTo("Project 101");
            assertThat(firstTask.getOwnerId()).isEqualTo(201L);
            assertThat(firstTask.getOwnerName()).isEqualTo("Owner 201");
            assertThat(firstTask.getOwnerDepartment()).isEqualTo("Dept " + (201L % 3));
            assertThat(firstTask.getTitle()).isEqualTo("Task 1");
            assertThat(firstTask.getDescription()).isEqualTo("Description 1");
            assertThat(firstTask.getStatus()).isEqualTo(Status.TODO);
            assertThat(firstTask.getTags()).containsExactlyInAnyOrder("tag1", "tag2");
            assertThat(firstTask.getCreatedBy()).isEqualTo(201L);
            assertThat(firstTask.getCreatedAt()).isEqualTo(fixedDateTime);

            // Verify second task
            TaskResponseDto secondTask = result.get(1);
            assertThat(secondTask.getId()).isEqualTo(2L);
            assertThat(secondTask.getProjectId()).isEqualTo(102L);
            assertThat(secondTask.getProjectName()).isEqualTo("Project 102");
            assertThat(secondTask.getOwnerName()).isEqualTo("Owner 201");
            assertThat(secondTask.getStatus()).isEqualTo(Status.IN_PROGRESS);
            assertThat(secondTask.getTags()).containsExactlyInAnyOrder("tag3");

            // Verify third task
            TaskResponseDto thirdTask = result.get(2);
            assertThat(thirdTask.getId()).isEqualTo(3L);
            assertThat(thirdTask.getProjectName()).isEqualTo("Project 103");
            assertThat(thirdTask.getDescription()).isNull();
            assertThat(thirdTask.getStatus()).isEqualTo(Status.COMPLETED);
            assertThat(thirdTask.getTags()).isEmpty();
            assertThat(thirdTask.getOwnerDepartment()).isEqualTo("Dept " + (201L % 3));

            verify(taskRepository).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should return empty list when user has no tasks")
        void getTasks_UserWithNoTasks_ReturnsEmptyList() {
            // Given
            Long userId = 999L;
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.emptyList());

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(taskRepository).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should handle single task correctly")
        void getTasks_SingleTask_ReturnsSingleTaskList() {
            // Given
            Long userId = 201L;
            List<Task> singleTaskList = Collections.singletonList(testTask1);
            when(taskRepository.findUserTasks(userId)).thenReturn(singleTaskList);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(1);
            TaskResponseDto task = result.get(0);
            assertThat(task.getId()).isEqualTo(1L);
            assertThat(task.getTitle()).isEqualTo("Task 1");
            assertThat(task.getOwnerId()).isEqualTo(userId);
            verify(taskRepository).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should handle tasks with null descriptions")
        void getTasks_TasksWithNullDescription_HandlesCorrectly() {
            // Given
            Long userId = 201L;
            Task taskWithNullDescription = createTestTask(4L, 104L, 201L, "Task with null desc",
                                                         null, Status.TODO, Collections.emptyList());
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.singletonList(taskWithNullDescription));

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(1);
            TaskResponseDto task = result.get(0);
            assertThat(task.getDescription()).isNull();
            assertThat(task.getTitle()).isEqualTo("Task with null desc");
        }

        @Test
        @DisplayName("Should handle tasks with empty tags")
        void getTasks_TasksWithEmptyTags_HandlesCorrectly() {
            // Given
            Long userId = 201L;
            Task taskWithEmptyTags = createTestTask(5L, 105L, 201L, "Task with empty tags",
                                                   "Description", Status.BLOCKED, Collections.emptyList());
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.singletonList(taskWithEmptyTags));

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(1);
            TaskResponseDto task = result.get(0);
            assertThat(task.getTags()).isNotNull().isEmpty();
            assertThat(task.getStatus()).isEqualTo(Status.BLOCKED);
        }

        @Test
        @DisplayName("Should handle tasks with null tags list")
        void getTasks_TasksWithNullTagsList_HandlesCorrectly() {
            // Given
            Long userId = 201L;
            Task taskWithNullTags = createTestTask(6L, 106L, 201L, "Task with null tags",
                                                  "Description", Status.TODO, null);
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.singletonList(taskWithNullTags));

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(1);
            TaskResponseDto task = result.get(0);
            assertThat(task.getTags()).isNull();
        }

        @Test
        @DisplayName("Should handle all status types correctly")
        void getTasks_TasksWithAllStatusTypes_HandlesCorrectly() {
            // Given
            Long userId = 201L;
            Task todoTask = createTestTask(1L, 101L, userId, "TODO Task", "Desc", Status.TODO, null);
            Task inProgressTask = createTestTask(2L, 102L, userId, "In Progress Task", "Desc", Status.IN_PROGRESS, null);
            Task completedTask = createTestTask(3L, 103L, userId, "Completed Task", "Desc", Status.COMPLETED, null);
            Task blockedTask = createTestTask(4L, 104L, userId, "Blocked Task", "Desc", Status.BLOCKED, null);

            List<Task> allStatusTasks = Arrays.asList(todoTask, inProgressTask, completedTask, blockedTask);
            when(taskRepository.findUserTasks(userId)).thenReturn(allStatusTasks);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(4);
            assertThat(result.get(0).getStatus()).isEqualTo(Status.TODO);
            assertThat(result.get(1).getStatus()).isEqualTo(Status.IN_PROGRESS);
            assertThat(result.get(2).getStatus()).isEqualTo(Status.COMPLETED);
            assertThat(result.get(3).getStatus()).isEqualTo(Status.BLOCKED);
        }

        @Test
        @DisplayName("Should handle edge case with user ID 0")
        void getTasks_UserIdZero_CallsRepositoryCorrectly() {
            // Given
            Long userId = 0L;
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.emptyList());

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).isNotNull().isEmpty();
            verify(taskRepository).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should handle large list of tasks efficiently")
        void getTasks_LargeTaskList_HandlesEfficiently() {
            // Given
            Long userId = 201L;
            List<Task> largeTasks = Collections.nCopies(100, testTask1);
            when(taskRepository.findUserTasks(userId)).thenReturn(largeTasks);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(100);
            result.forEach(task -> {
                assertThat(task.getId()).isEqualTo(1L);
                assertThat(task.getTitle()).isEqualTo("Task 1");
            });
            verify(taskRepository).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should handle negative user ID gracefully")
        void getTasks_NegativeUserId_CallsRepositoryCorrectly() {
            // Given
            Long userId = -1L;
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.emptyList());

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).isNotNull().isEmpty();
            verify(taskRepository).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should handle maximum Long value user ID")
        void getTasks_MaxLongUserId_CallsRepositoryCorrectly() {
            // Given
            Long userId = Long.MAX_VALUE;
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.emptyList());

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).isNotNull().isEmpty();
            verify(taskRepository).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should preserve task order from repository")
        void getTasks_MultipleTasksFromRepository_PreservesOrder() {
            // Given
            Long userId = 201L;
            List<Task> orderedTasks = Arrays.asList(testTask3, testTask1, testTask2); // Different order
            when(taskRepository.findUserTasks(userId)).thenReturn(orderedTasks);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).getId()).isEqualTo(3L);
            assertThat(result.get(1).getId()).isEqualTo(1L);
            assertThat(result.get(2).getId()).isEqualTo(2L);
            verify(taskRepository).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should handle tasks with very long titles and descriptions")
        void getTasks_TasksWithLongContent_HandlesCorrectly() {
            // Given
            Long userId = 201L;
            String longTitle = "A".repeat(500);
            String longDescription = "B".repeat(2000);
            Task taskWithLongContent = createTestTask(7L, 107L, 201L, longTitle, longDescription, Status.TODO, null);
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.singletonList(taskWithLongContent));

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(1);
            TaskResponseDto task = result.get(0);
            assertThat(task.getTitle()).isEqualTo(longTitle);
            assertThat(task.getDescription()).isEqualTo(longDescription);
        }

        @Test
        @DisplayName("Should handle tasks with special characters in content")
        void getTasks_TasksWithSpecialCharacters_HandlesCorrectly() {
            // Given
            Long userId = 201L;
            String specialTitle = "Task with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?`~";
            String specialDescription = "Description with unicode: 你好世界 🚀💯 ñáéíóú";
            Task taskWithSpecialChars = createTestTask(8L, 108L, 201L, specialTitle, specialDescription, Status.IN_PROGRESS, null);
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.singletonList(taskWithSpecialChars));

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(1);
            TaskResponseDto task = result.get(0);
            assertThat(task.getTitle()).isEqualTo(specialTitle);
            assertThat(task.getDescription()).isEqualTo(specialDescription);
        }

        @Test
        @DisplayName("Should handle tasks with very large tag lists")
        void getTasks_TasksWithManyTags_HandlesCorrectly() {
            // Given
            Long userId = 201L;
            List<String> manyTags = new ArrayList<>();
            for (int i = 0; i < 50; i++) {
                manyTags.add("tag" + i);
            }
            Task taskWithManyTags = createTestTask(9L, 109L, 201L, "Task with many tags", "Description", Status.BLOCKED, manyTags);
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.singletonList(taskWithManyTags));

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(1);
            TaskResponseDto task = result.get(0);
            assertThat(task.getTags()).hasSize(50);
            assertThat(task.getTags()).containsExactlyInAnyOrderElementsOf(manyTags);
        }

        @Test
        @DisplayName("Should handle tasks with duplicate tags in list")
        void getTasks_TasksWithDuplicateTags_HandlesCorrectly() {
            // Given
            Long userId = 201L;
            List<String> duplicateTags = Arrays.asList("duplicate", "unique", "duplicate", "another", "duplicate");
            Task taskWithDuplicates = createTestTask(10L, 110L, 201L, "Task with duplicates", "Description", Status.TODO, duplicateTags);
            when(taskRepository.findUserTasks(userId)).thenReturn(Collections.singletonList(taskWithDuplicates));

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(1);
            TaskResponseDto task = result.get(0);
            // Sets automatically remove duplicates, so only unique values remain
            assertThat(task.getTags()).containsExactlyInAnyOrder("duplicate", "unique", "another");
        }

        @Test
        @DisplayName("Should handle mixed data scenarios with various edge cases")
        void getTasks_MixedDataScenarios_HandlesAllCasesCorrectly() {
            // Given
            Long userId = 201L;
            List<Task> mixedTasks = Arrays.asList(
                testTask1, // Normal task
                testTask3, // Null description, empty tags
                createTestTask(11L, 111L, 201L, "", "Empty title task", Status.COMPLETED, null), // Empty title
                createTestTask(12L, 112L, 201L, "Whitespace task", "   ", Status.IN_PROGRESS, Arrays.asList("", "   ", "valid")) // Whitespace and empty tags
            );
            when(taskRepository.findUserTasks(userId)).thenReturn(mixedTasks);

            // When
            List<TaskResponseDto> result = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result).hasSize(4);

            // Verify normal task
            assertThat(result.get(0).getTitle()).isEqualTo("Task 1");
            assertThat(result.get(0).getDescription()).isEqualTo("Description 1");

            // Verify null description task
            assertThat(result.get(1).getDescription()).isNull();

            // Verify empty title task
            assertThat(result.get(2).getTitle()).isEqualTo("");

            // Verify whitespace scenarios
            assertThat(result.get(3).getDescription()).isEqualTo("   ");
            assertThat(result.get(3).getTags()).containsExactlyInAnyOrder("", "   ", "valid");
        }

        @Test
        @DisplayName("Should verify repository method is called exactly once")
        void getTasks_AnyValidUserId_CallsRepositoryExactlyOnce() {
            // Given
            Long userId = 201L;
            when(taskRepository.findUserTasks(userId)).thenReturn(Arrays.asList(testTask1));

            // When
            taskService.getAllUserTasks(userId);

            // Then
            verify(taskRepository, org.mockito.Mockito.times(1)).findUserTasks(userId);
        }

        @Test
        @DisplayName("Should handle concurrent access scenarios gracefully")
        void getTasks_ConcurrentAccess_ThreadSafe() {
            // Given
            Long userId = 201L;
            when(taskRepository.findUserTasks(userId)).thenReturn(Arrays.asList(testTask1, testTask2));

            // When - Simulate multiple calls (in real scenario these would be from different threads)
            List<TaskResponseDto> result1 = taskService.getAllUserTasks(userId);
            List<TaskResponseDto> result2 = taskService.getAllUserTasks(userId);

            // Then
            assertThat(result1).hasSize(2);
            assertThat(result2).hasSize(2);
            assertThat(result1.get(0).getId()).isEqualTo(result2.get(0).getId());
            verify(taskRepository, org.mockito.Mockito.times(2)).findUserTasks(userId);
        }
    }

    @Nested
    @DisplayName("canUserUpdateTask Tests")
    class CanUserUpdateOrDeleteTaskTests {

        @Test
        @DisplayName("Should return true when user is task owner")
        void canUserUpdateTask_UserIsOwner_ReturnsTrue() {
            // Given
            Long taskId = 1L;
            Long userId = 201L; // Same as testTask1 owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should return true when user is collaborator but not owner")
        void canUserUpdateTask_UserIsCollaborator_ReturnsTrue() {
            // Given
            Long taskId = 1L;
            Long userId = 999L; // Different from testTask1 owner (201L)
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(true);

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should return false when user is neither owner nor collaborator")
        void canUserUpdateTask_UserIsNeitherOwnerNorCollaborator_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long userId = 999L; // Different from testTask1 owner (201L)
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(false);

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should throw RuntimeException when task is not found")
        void canUserUpdateTask_TaskNotFound_ThrowsRuntimeException() {
            // Given
            Long taskId = 999L;
            Long userId = 201L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.canUserUpdateTask(taskId, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Task not found");

            verify(taskRepository).findById(taskId);
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should handle different task and user ID combinations")
        void canUserUpdateTask_DifferentIds_HandlesCorrectly() {
            // Given
            Long taskId = 2L;
            Long userId = 201L; // Same as testTask2 owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask2));

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should handle null task ID gracefully")
        void canUserUpdateTask_NullTaskId_DelegatesToRepository() {
            // Given
            Long taskId = null;
            Long userId = 201L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.canUserUpdateTask(taskId, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Task not found");

            verify(taskRepository).findById(taskId);
        }

        @Test
        @DisplayName("Should handle null user ID gracefully")
        void canUserUpdateTask_NullUserId_HandlesCorrectly() {
            // Given
            Long taskId = 1L;
            Long userId = null;
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(false);

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isFalse(); // null userId won't equal task owner ID
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should verify repository and service interactions for owner scenario")
        void canUserUpdateTask_OwnerScenario_VerifyInteractions() {
            // Given
            Long taskId = 3L;
            Long userId = 201L; // Same as testTask3 owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask3));

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            // Collaborator service should not be called when user is owner
            verify(collaboratorService, never()).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should verify repository and service interactions for collaborator scenario")
        void canUserUpdateTask_CollaboratorScenario_VerifyInteractions() {
            // Given
            Long taskId = 1L;
            Long userId = 500L; // Different from testTask1 owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(true);

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should handle edge case with user ID 0")
        void canUserUpdateTask_UserIdZero_HandlesCorrectly() {
            // Given
            Long taskId = 1L;
            Long userId = 0L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(false);

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should handle task with different owner ID correctly")
        void canUserUpdateTask_TaskWithDifferentOwner_HandlesCorrectly() {
            // Given
            Long taskId = 10L;
            Long userId = 999L;
            Long differentOwnerId = 888L;

            Task taskWithDifferentOwner = createTestTask(10L, 100L, differentOwnerId,
                    "Different Owner Task", "Description", Status.TODO, Collections.emptyList());

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithDifferentOwner));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(true);

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isTrue(); // User is collaborator
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should handle maximum Long values for IDs")
        void canUserUpdateTask_MaxLongValues_HandlesCorrectly() {
            // Given
            Long taskId = Long.MAX_VALUE;
            Long userId = Long.MAX_VALUE - 1;

            Task maxIdTask = createTestTask(Long.MAX_VALUE, 100L, Long.MAX_VALUE - 1,
                    "Max ID Task", "Description", Status.TODO, Collections.emptyList());

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(maxIdTask));

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isTrue(); // User is owner
            verify(taskRepository).findById(taskId);
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should handle collaborator service returning false correctly")
        void canUserUpdateTask_CollaboratorServiceReturnsFalse_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long userId = 300L; // Different from owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(false);

            // When
            boolean result = taskService.canUserUpdateTask(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }
    }

    @Nested
    @DisplayName("Create Task with Specified Owner Tests")
    class CreateTaskWithSpecifiedOwnerTests {

        private CreateTaskDto validCreateTaskDto;
        private Task savedTask;
        private OffsetDateTime fixedDateTime;

        @BeforeEach
        void setUp() {
            fixedDateTime = OffsetDateTime.now();

            validCreateTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(456L)
                    .title("Test Task with Specified Owner")
                    .description("Test Description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .tags(Arrays.asList("tag1", "tag2"))
                    .assignedUserIds(Arrays.asList(789L, 101L))
                    .build();

            savedTask = new Task();
            savedTask.setId(1L);
            savedTask.setProjectId(101L);
            savedTask.setOwnerId(456L);
            savedTask.setTaskType(TaskType.FEATURE);
            savedTask.setTitle("Test Task with Specified Owner");
            savedTask.setDescription("Test Description");
            savedTask.setStatus(Status.TODO);
            savedTask.setTags(createTagsFromNames(Arrays.asList("tag1", "tag2")));
            savedTask.setCreatedBy(456L);
            savedTask.setCreatedAt(fixedDateTime);

            // Mock tagService to return Tag entities (lenient for tests that don't use tags)
            lenient().when(tagService.findOrCreateTags(Arrays.asList("tag1", "tag2")))
                    .thenReturn(createTagsFromNames(Arrays.asList("tag1", "tag2")));

            // Mock isUserProjectMember to return true by default (user is a member of the project)
            // Use nullable() to handle both null and non-null projectIds
            lenient().when(projectService.isUserProjectMember(anyLong(), nullable(Long.class))).thenReturn(true);
        }

        @Test
        @DisplayName("Should successfully create task with specified owner and current user")
        void createTask_ValidSpecifiedOwner_ReturnsCreateTaskResponseDto() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(savedTask));
            // When
            CreateTaskResponseDto result = taskService.createTask(validCreateTaskDto, specifiedOwnerId, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getProjectId()).isEqualTo(101L);
            assertThat(result.getOwnerId()).isEqualTo(456L);
            assertThat(result.getTitle()).isEqualTo("Test Task with Specified Owner");
            assertThat(result.getDescription()).isEqualTo("Test Description");
            assertThat(result.getStatus()).isEqualTo(Status.TODO);
            assertThat(result.getTaskType()).isEqualTo(TaskType.FEATURE);
            assertThat(result.getTags()).containsExactlyInAnyOrder("tag1", "tag2");
            assertThat(result.getCreatedBy()).isEqualTo(456L);
            assertThat(result.getCreatedAt()).isEqualTo(fixedDateTime);

            verify(taskRepository).save(argThat(task ->
                Objects.equals(task.getProjectId(), 101L) &&
                Objects.equals(task.getOwnerId(), 456L) &&
                Objects.equals(task.getCreatedBy(), 456L) &&
                Objects.equals(task.getTitle(), "Test Task with Specified Owner") &&
                Objects.equals(task.getDescription(), "Test Description") &&
                Objects.equals(task.getStatus(), Status.TODO) &&
                Objects.equals(task.getTaskType(), TaskType.FEATURE) &&
                task.getTags() != null &&
                task.getTags().stream().map(com.spmorangle.crm.taskmanagement.model.Tag::getTagName).collect(Collectors.toSet())
                    .equals(Set.of("tag1", "tag2"))
            ));
        }

        @Test
        @DisplayName("Should assign collaborators when assignedUserIds provided")
        void createTask_WithAssignedUserIds_AssignsCollaborators() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(savedTask.getId())).thenReturn(Optional.of(savedTask));
            when(collaboratorService.addCollaborator(any(AddCollaboratorRequestDto.class), anyLong()))
                    .thenReturn(AddCollaboratorResponseDto.builder()
                            .taskId(1L)
                            .collaboratorId(789L)
                            .assignedById(123L)
                            .assignedAt(fixedDateTime)
                            .build());

            // When
            CreateTaskResponseDto result = taskService.createTask(validCreateTaskDto, specifiedOwnerId, currentUserId);

            // Then
            // Owner (456L) is now automatically added as a collaborator
            assertThat(result.getAssignedUserIds()).containsExactlyInAnyOrder(456L, 789L, 101L);

            verify(collaboratorService, times(3)).addCollaborator(argThat(request ->
                request.getTaskId().equals(1L) &&
                (request.getCollaboratorId().equals(456L) || request.getCollaboratorId().equals(789L) || request.getCollaboratorId().equals(101L))
            ), eq(123L));
        }

        @Test
        @DisplayName("Should handle empty assigned user IDs list")
        void createTask_EmptyAssignedUserIds_DoesNotAssignCollaborators() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            CreateTaskDto dtoWithEmptyAssignedUserIds = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(456L)
                    .title("Test Task")
                    .description("Test Description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .assignedUserIds(Collections.emptyList())
                    .build();

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(savedTask));

            // When
            CreateTaskResponseDto result = taskService.createTask(dtoWithEmptyAssignedUserIds, specifiedOwnerId, currentUserId);

            // Then
            // Owner (456L) is now automatically added as a collaborator even with empty assignedUserIds
            assertThat(result.getAssignedUserIds()).containsExactly(456L);
            verify(collaboratorService, times(1)).addCollaborator(argThat(request ->
                request.getTaskId().equals(1L) && request.getCollaboratorId().equals(456L)
            ), eq(123L));
        }

        @Test
        @DisplayName("Should handle null assigned user IDs list")
        void createTask_NullAssignedUserIds_DoesNotAssignCollaborators() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            CreateTaskDto dtoWithNullAssignedUserIds = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(456L)
                    .title("Test Task")
                    .description("Test Description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .assignedUserIds(null)
                    .build();

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(savedTask));

            // When
            CreateTaskResponseDto result = taskService.createTask(dtoWithNullAssignedUserIds, specifiedOwnerId, currentUserId);

            // Then
            // Owner (456L) is now automatically added as a collaborator even with null assignedUserIds
            assertThat(result.getAssignedUserIds()).containsExactly(456L);
            verify(collaboratorService, times(1)).addCollaborator(argThat(request ->
                request.getTaskId().equals(1L) && request.getCollaboratorId().equals(456L)
            ), eq(123L));
        }

        @Test
        @DisplayName("Should continue when collaborator assignment fails for some users")
        void createTask_CollaboratorAssignmentFails_ContinuesWithOtherAssignments() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(savedTask.getId())).thenReturn(Optional.of(savedTask));

            // Mock successful owner assignment
            when(collaboratorService.addCollaborator(argThat(request ->
                    request != null && request.getCollaboratorId().equals(456L)), anyLong()))
                    .thenReturn(AddCollaboratorResponseDto.builder()
                            .taskId(1L)
                            .collaboratorId(456L)
                            .assignedById(456L)
                            .assignedAt(fixedDateTime)
                            .build());
            // Mock failure for user 789L
            when(collaboratorService.addCollaborator(argThat(request ->
                    request != null && request.getCollaboratorId().equals(789L)), anyLong()))
                    .thenThrow(new RuntimeException("Assignment failed for user 789"));
            // Mock success for user 101L
            when(collaboratorService.addCollaborator(argThat(request ->
                    request != null && request.getCollaboratorId().equals(101L)), anyLong()))
                    .thenReturn(AddCollaboratorResponseDto.builder()
                            .taskId(1L)
                            .collaboratorId(101L)
                            .assignedById(123L)
                            .assignedAt(fixedDateTime)
                            .build());

            // When
            CreateTaskResponseDto result = taskService.createTask(validCreateTaskDto, specifiedOwnerId, currentUserId);

            // Then
            // Owner (456L) is auto-assigned successfully, 789L fails, 101L succeeds
            assertThat(result.getAssignedUserIds()).containsExactlyInAnyOrder(456L, 101L);
            verify(collaboratorService, times(3)).addCollaborator(any(), anyLong());
        }

        @Test
        @DisplayName("Should create task with minimal required fields")
        void createTask_MinimalFields_CreatesTaskSuccessfully() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            CreateTaskDto minimalDto = CreateTaskDto.builder()
                    .title("Minimal Task")
                    .taskType(TaskType.BUG)
                    .build();

            Task minimalSavedTask = new Task();
            minimalSavedTask.setId(1L);
            minimalSavedTask.setOwnerId(456L);
            minimalSavedTask.setTitle("Minimal Task");
            minimalSavedTask.setTaskType(TaskType.BUG);
            minimalSavedTask.setStatus(Status.TODO);
            minimalSavedTask.setCreatedBy(456L);
            minimalSavedTask.setCreatedAt(fixedDateTime);

            when(taskRepository.save(any(Task.class))).thenReturn(minimalSavedTask);
            when(taskRepository.findById(minimalSavedTask.getId())).thenReturn(Optional.of(minimalSavedTask));

            // When
            CreateTaskResponseDto result = taskService.createTask(minimalDto, specifiedOwnerId, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOwnerId()).isEqualTo(456L);
            assertThat(result.getTitle()).isEqualTo("Minimal Task");
            assertThat(result.getTaskType()).isEqualTo(TaskType.BUG);
            assertThat(result.getCreatedBy()).isEqualTo(456L);

            verify(taskRepository).save(argThat(task ->
                Objects.equals(task.getOwnerId(), 456L) &&
                Objects.equals(task.getCreatedBy(), 456L) &&
                Objects.equals(task.getTitle(), "Minimal Task") &&
                Objects.equals(task.getTaskType(), TaskType.BUG)
            ));
        }

        @Test
        @DisplayName("Should set correct timestamps on task creation")
        void createTask_SetsCorrectTimestamps() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(savedTask.getId())).thenReturn(Optional.of(savedTask));

            // When
            taskService.createTask(validCreateTaskDto, specifiedOwnerId, currentUserId);

            // Then
            verify(taskRepository).save(argThat(task ->
                task.getCreatedAt() != null &&
                task.getCreatedAt().isBefore(OffsetDateTime.now().plusSeconds(1)) &&
                task.getCreatedAt().isAfter(OffsetDateTime.now().minusSeconds(10))
            ));
        }

        @Test
        @DisplayName("Should handle different task types correctly")
        void createTask_DifferentTaskTypes_CreatesCorrectly() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            TaskType[] taskTypes = {TaskType.FEATURE, TaskType.BUG, TaskType.CHORE};

            for (TaskType taskType : taskTypes) {
                CreateTaskDto dto = CreateTaskDto.builder()
                        .title("Task of type " + taskType)
                        .taskType(taskType)
                        .build();

                Task task = new Task();
                task.setId(1L);
                task.setOwnerId(456L);
                task.setTitle("Task of type " + taskType);
                task.setTaskType(taskType);
                task.setCreatedBy(456L);
                task.setCreatedAt(fixedDateTime);

                when(taskRepository.save(any(Task.class))).thenReturn(task);
                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

                // When
                CreateTaskResponseDto result = taskService.createTask(dto, specifiedOwnerId, currentUserId);

                // Then
                assertThat(result.getTaskType()).isEqualTo(taskType);
            }
        }

        @Test
        @DisplayName("Should handle different status types correctly")
        void createTask_DifferentStatuses_CreatesCorrectly() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            Status[] statuses = {Status.TODO, Status.IN_PROGRESS, Status.COMPLETED, Status.BLOCKED};

            for (Status status : statuses) {
                CreateTaskDto dto = CreateTaskDto.builder()
                        .title("Task with status " + status)
                        .taskType(TaskType.FEATURE)
                        .status(status)
                        .build();

                Task task = new Task();
                task.setId(1L);
                task.setOwnerId(456L);
                task.setTitle("Task with status " + status);
                task.setTaskType(TaskType.FEATURE);
                task.setStatus(status);
                task.setCreatedBy(456L);
                task.setCreatedAt(fixedDateTime);

                when(taskRepository.save(any(Task.class))).thenReturn(task);
                when(taskRepository.findById(task.getId())).thenReturn(Optional.of(task));

                // When
                CreateTaskResponseDto result = taskService.createTask(dto, specifiedOwnerId, currentUserId);

                // Then
                assertThat(result.getStatus()).isEqualTo(status);
            }
        }

        @Test
        @DisplayName("Should handle null and empty tags correctly")
        void createTask_NullAndEmptyTags_HandlesCorrectly() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            // Test with null tags
            CreateTaskDto dtoWithNullTags = CreateTaskDto.builder()
                    .title("Task with null tags")
                    .taskType(TaskType.FEATURE)
                    .tags(null)
                    .build();

            Task taskWithNullTags = new Task();
            taskWithNullTags.setId(1L);
            taskWithNullTags.setOwnerId(456L);
            taskWithNullTags.setTitle("Task with null tags");
            taskWithNullTags.setTaskType(TaskType.FEATURE);
            taskWithNullTags.setTags(null);
            taskWithNullTags.setCreatedBy(456L);
            taskWithNullTags.setCreatedAt(fixedDateTime);

            when(taskRepository.save(any(Task.class))).thenReturn(taskWithNullTags);
            when(taskRepository.findById(taskWithNullTags.getId())).thenReturn(Optional.of(taskWithNullTags));

            // When
            CreateTaskResponseDto result = taskService.createTask(dtoWithNullTags, specifiedOwnerId, currentUserId);

            // Then
            assertThat(result.getTags()).isNull();

            // Test with empty tags
            CreateTaskDto dtoWithEmptyTags = CreateTaskDto.builder()
                    .title("Task with empty tags")
                    .taskType(TaskType.FEATURE)
                    .tags(Collections.emptyList())
                    .build();

            Task taskWithEmptyTags = new Task();
            taskWithEmptyTags.setId(2L);
            taskWithEmptyTags.setOwnerId(456L);
            taskWithEmptyTags.setTitle("Task with empty tags");
            taskWithEmptyTags.setTaskType(TaskType.FEATURE);
            taskWithEmptyTags.setTags(new HashSet<>());
            taskWithEmptyTags.setCreatedBy(456L);
            taskWithEmptyTags.setCreatedAt(fixedDateTime);

            when(taskRepository.save(any(Task.class))).thenReturn(taskWithEmptyTags);
            when(taskRepository.findById(taskWithEmptyTags.getId())).thenReturn(Optional.of(taskWithEmptyTags));

            // When
            CreateTaskResponseDto resultEmpty = taskService.createTask(dtoWithEmptyTags, specifiedOwnerId, currentUserId);

            // Then
            assertThat(resultEmpty.getTags()).isEmpty();
        }

        @Test
        @DisplayName("Should verify transaction annotation is applied")
        void createTask_TransactionAnnotation_IsApplied() {
            // This test verifies that the @Transactional annotation is properly configured
            // The actual transaction behavior would be tested in integration tests

            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(savedTask.getId())).thenReturn(Optional.of(savedTask));

            // When & Then - Should not throw any exception
            assertThatCode(() -> taskService.createTask(validCreateTaskDto, specifiedOwnerId, currentUserId))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Create Task with Current User as Owner Tests")
    class CreateTaskWithCurrentUserAsOwnerTests {

        private CreateTaskDto validCreateTaskDto;
        private Task savedTask;
        private OffsetDateTime fixedDateTime;

        @BeforeEach
        void setUp() {
            fixedDateTime = OffsetDateTime.now();

            validCreateTaskDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .title("Test Task with Current User as Owner")
                    .description("Test Description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .tags(Arrays.asList("tag1", "tag2"))
                    .assignedUserIds(Arrays.asList(789L, 101L))
                    .build();

            savedTask = new Task();
            savedTask.setId(1L);
            savedTask.setProjectId(101L);
            savedTask.setOwnerId(123L);
            savedTask.setTaskType(TaskType.FEATURE);
            savedTask.setTitle("Test Task with Current User as Owner");
            savedTask.setDescription("Test Description");
            savedTask.setStatus(Status.TODO);
            savedTask.setTags(createTagsFromNames(Arrays.asList("tag1", "tag2")));
            savedTask.setCreatedBy(123L);
            savedTask.setCreatedAt(fixedDateTime);

            // Mock tagService to return Tag entities (lenient for tests that don't use tags)
            lenient().when(tagService.findOrCreateTags(Arrays.asList("tag1", "tag2")))
                    .thenReturn(createTagsFromNames(Arrays.asList("tag1", "tag2")));

            // Mock isUserProjectMember to return true by default (user is a member of the project)
            // Use nullable() to handle both null and non-null projectIds
            lenient().when(projectService.isUserProjectMember(anyLong(), nullable(Long.class))).thenReturn(true);
        }

        @Test
        @DisplayName("Should successfully create task with current user as owner")
        void createTask_CurrentUserAsOwner_ReturnsCreateTaskResponseDto() {
            // Given
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(savedTask.getId())).thenReturn(Optional.of(savedTask));

            // When
            CreateTaskResponseDto result = taskService.createTask(validCreateTaskDto, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getProjectId()).isEqualTo(101L);
            assertThat(result.getOwnerId()).isEqualTo(123L); // Current user becomes owner
            assertThat(result.getTitle()).isEqualTo("Test Task with Current User as Owner");
            assertThat(result.getDescription()).isEqualTo("Test Description");
            assertThat(result.getStatus()).isEqualTo(Status.TODO);
            assertThat(result.getTaskType()).isEqualTo(TaskType.FEATURE);
            assertThat(result.getTags()).containsExactlyInAnyOrder("tag1", "tag2");
            assertThat(result.getCreatedBy()).isEqualTo(123L);
            assertThat(result.getCreatedAt()).isEqualTo(fixedDateTime);

            verify(taskRepository).save(argThat(task ->
                Objects.equals(task.getProjectId(), 101L) &&
                Objects.equals(task.getOwnerId(), 123L) &&
                Objects.equals(task.getCreatedBy(), 123L) &&
                Objects.equals(task.getTitle(), "Test Task with Current User as Owner") &&
                Objects.equals(task.getDescription(), "Test Description") &&
                Objects.equals(task.getStatus(), Status.TODO) &&
                Objects.equals(task.getTaskType(), TaskType.FEATURE) &&
                task.getTags() != null &&
                task.getTags().stream().map(com.spmorangle.crm.taskmanagement.model.Tag::getTagName).collect(Collectors.toSet())
                    .equals(Set.of("tag1", "tag2"))
            ));
        }

        @Test
        @DisplayName("Should assign collaborators when creating task with current user as owner")
        void createTask_CurrentUserAsOwnerWithAssignedUserIds_AssignsCollaborators() {
            // Given
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(savedTask.getId())).thenReturn(Optional.of(savedTask));

            // When
            CreateTaskResponseDto result = taskService.createTask(validCreateTaskDto, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOwnerId()).isEqualTo(currentUserId);

            verify(taskRepository).save(any(Task.class));
            // Owner (123L) is now automatically added + 2 additional collaborators (789L, 101L)
            verify(collaboratorService, times(3)).addCollaborator(any(AddCollaboratorRequestDto.class), anyLong());

            // Verify the specific collaborator requests including the owner
            verify(collaboratorService).addCollaborator(argThat(request ->
                request.getTaskId().equals(1L) &&
                request.getCollaboratorId().equals(123L) // Owner auto-assigned
            ), eq(currentUserId));
            verify(collaboratorService).addCollaborator(argThat(request ->
                request.getTaskId().equals(1L) &&
                request.getCollaboratorId().equals(789L)
            ), eq(currentUserId));
            verify(collaboratorService).addCollaborator(argThat(request ->
                request.getTaskId().equals(1L) &&
                request.getCollaboratorId().equals(101L)
            ), eq(currentUserId));
        }

        @Test
        @DisplayName("Should not assign collaborators when assignedUserIds is empty")
        void createTask_CurrentUserAsOwnerEmptyAssignedUserIds_DoesNotAssignCollaborators() {
            // Given
            Long currentUserId = 123L;
            CreateTaskDto dtoWithEmptyAssignedUserIds = CreateTaskDto.builder()
                    .projectId(101L)
                    .title("Test Task")
                    .description("Test Description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .tags(Arrays.asList("tag1", "tag2"))
                    .assignedUserIds(Collections.emptyList())
                    .build();

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(savedTask.getId())).thenReturn(Optional.of(savedTask));

            // When
            CreateTaskResponseDto result = taskService.createTask(dtoWithEmptyAssignedUserIds, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOwnerId()).isEqualTo(currentUserId);

            verify(taskRepository).save(any(Task.class));
            // Owner (123L) is now automatically added as a collaborator even with empty assignedUserIds
            verify(collaboratorService, times(1)).addCollaborator(argThat(request ->
                request.getTaskId().equals(1L) && request.getCollaboratorId().equals(123L)
            ), eq(currentUserId));
        }

        @Test
        @DisplayName("Should ignore ownerId from DTO and use current user as owner")
        void createTask_CurrentUserAsOwnerIgnoresDtoOwnerId_UsesCurrentUserAsOwner() {
            // Given
            Long currentUserId = 123L;
            CreateTaskDto dtoWithDifferentOwnerId = CreateTaskDto.builder()
                    .projectId(101L)
                    .ownerId(999L) // This should be ignored
                    .title("Test Task")
                    .description("Test Description")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .tags(Arrays.asList("tag1", "tag2"))
                    .assignedUserIds(Collections.emptyList())
                    .build();

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(taskRepository.findById(savedTask.getId())).thenReturn(Optional.of(savedTask));

            // When
            CreateTaskResponseDto result = taskService.createTask(dtoWithDifferentOwnerId, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOwnerId()).isEqualTo(currentUserId); // Should use current user, not DTO's ownerId

            verify(taskRepository).save(argThat(task ->
                Objects.equals(task.getOwnerId(), currentUserId) &&
                Objects.equals(task.getCreatedBy(), currentUserId)
            ));
        }

        @Test
        @DisplayName("Should handle minimal fields correctly when current user is owner")
        void createTask_CurrentUserAsOwnerMinimalFields_CreatesTaskSuccessfully() {
            // Given
            Long currentUserId = 123L;
            CreateTaskDto minimalDto = CreateTaskDto.builder()
                    .projectId(101L)
                    .title("Minimal Task")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .build();

            Task minimalSavedTask = new Task();
            minimalSavedTask.setId(2L);
            minimalSavedTask.setProjectId(101L);
            minimalSavedTask.setOwnerId(currentUserId);
            minimalSavedTask.setTitle("Minimal Task");
            minimalSavedTask.setStatus(Status.TODO);
            minimalSavedTask.setTaskType(TaskType.FEATURE);
            minimalSavedTask.setCreatedBy(currentUserId);

            when(taskRepository.save(any(Task.class))).thenReturn(minimalSavedTask);
            when(taskRepository.findById(minimalSavedTask.getId())).thenReturn(Optional.of(minimalSavedTask));

            // When
            CreateTaskResponseDto result = taskService.createTask(minimalDto, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getOwnerId()).isEqualTo(currentUserId);
            assertThat(result.getTitle()).isEqualTo("Minimal Task");
            assertThat(result.getDescription()).isNull();
            assertThat(result.getTags()).isNullOrEmpty();

            verify(taskRepository).save(argThat(task ->
                Objects.equals(task.getOwnerId(), currentUserId) &&
                Objects.equals(task.getCreatedBy(), currentUserId) &&
                Objects.equals(task.getTitle(), "Minimal Task")
            ));
        }

        @Test
        @DisplayName("Should set updatedBy and updatedAt correctly")
        void updateTask_UpdatedByAndUpdatedAt_SetCorrectly() {
            // Given
            Long taskId = 1L;
            Long userId = 201L;
            OffsetDateTime beforeUpdate = OffsetDateTime.now();

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .title("Updated Title")
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(taskRepository).save(argThat(task -> {
                assertThat(task.getUpdatedBy()).isEqualTo(userId);
                assertThat(task.getUpdatedAt()).isNotNull();
                assertThat(task.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
                assertThat(task.getUpdatedAt()).isBeforeOrEqualTo(OffsetDateTime.now());
                return true;
            }));
        }

        @Test
        @DisplayName("Should handle all status transitions correctly")
        void updateTask_AllStatusTransitions_HandlesCorrectly() {
            // Given
            Long taskId = 1L;
            Long userId = 201L;

            Status[] allStatuses = {Status.TODO, Status.IN_PROGRESS, Status.COMPLETED, Status.BLOCKED};

            for (Status newStatus : allStatuses) {
                UpdateTaskDto statusDto = UpdateTaskDto.builder()
                    .taskId(taskId)
                    .status(newStatus)
                    .build();

                when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(projectService.getOwnerId(101L)).thenReturn(userId);

                // When
                UpdateTaskResponseDto result = taskService.updateTask(statusDto, userId);

                // Then
                assertThat(result.getStatus()).isEqualTo(newStatus);
            }
        }

        @Test
        @DisplayName("Should handle all task type transitions correctly")
        void updateTask_AllTaskTypeTransitions_HandlesCorrectly() {
            // Given
            Long taskId = 1L;
            Long userId = 201L;

            TaskType[] allTaskTypes = {TaskType.FEATURE, TaskType.BUG, TaskType.CHORE};

            for (TaskType newType : allTaskTypes) {
                UpdateTaskDto typeDto = UpdateTaskDto.builder()
                    .taskId(taskId)
                    .taskType(newType)
                    .build();

                when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
                when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(projectService.getOwnerId(101L)).thenReturn(userId);

                // When
                UpdateTaskResponseDto result = taskService.updateTask(typeDto, userId);

                // Then
                assertThat(result.getTaskType()).isEqualTo(newType);
            }
        }

        @Test
        @DisplayName("Should handle updates with special characters in content")
        void updateTask_SpecialCharactersInContent_HandlesCorrectly() {
            // Given
            Long taskId = 1L;
            Long userId = 201L;

            String specialTitle = "Title with special chars: @#$%^&*()_+-=[]{}|;':\",./<>?`~";
            String specialDescription = "Description with unicode: 你好世界 🚀💯 ñáéíóú";

            UpdateTaskDto specialDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .title(specialTitle)
                .description(specialDescription)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            UpdateTaskResponseDto result = taskService.updateTask(specialDto, userId);

            // Then
            assertThat(result.getTitle()).isEqualTo(specialTitle);
            assertThat(result.getDescription()).isEqualTo(specialDescription);
        }
    }

    @Nested
    @DisplayName("Delete Task Tests")
    class DeleteTaskTests {

        private Task projectTask;
        private Task personalTask;

        @BeforeEach
        void setUp() {
            projectTask = createTestTask(1L, 101L, 201L, "Project Task",
                "Description", Status.TODO, Collections.emptyList());

            personalTask = createTestTask(2L, null, 202L, "Personal Task",
                "Description", Status.TODO, Collections.emptyList());
        }

        @Test
        @DisplayName("Should successfully delete task when user is project owner")
        void deleteTask_ProjectOwner_DeletesSuccessfully() {
            // Given
            Long taskId = 1L;
            Long projectOwnerId = 300L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
            when(projectService.getOwnerId(101L)).thenReturn(projectOwnerId);
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            assertThatCode(() -> taskService.deleteTask(taskId, projectOwnerId))
                .doesNotThrowAnyException();

            // Then
            verify(taskRepository).save(argThat(task -> {
                assertThat(task.isDeleteInd()).isTrue();
                assertThat(task.getUpdatedBy()).isEqualTo(projectOwnerId);
                assertThat(task.getUpdatedAt()).isNotNull();
                return true;
            }));
        }

        @Test
        @DisplayName("Should successfully delete personal task when user is task owner")
        void deleteTask_PersonalTaskOwner_DeletesSuccessfully() {
            // Given
            Long taskId = 2L;
            Long taskOwnerId = 202L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            assertThatCode(() -> taskService.deleteTask(taskId, taskOwnerId))
                .doesNotThrowAnyException();

            // Then
            verify(taskRepository).save(argThat(task -> {
                assertThat(task.isDeleteInd()).isTrue();
                assertThat(task.getUpdatedBy()).isEqualTo(taskOwnerId);
                assertThat(task.getUpdatedAt()).isNotNull();
                return true;
            }));
        }

        @Test
        @DisplayName("Should throw exception when user is not project owner")
        void deleteTask_NotProjectOwner_ThrowsException() {
            // Given
            Long taskId = 1L;
            Long unauthorizedUserId = 999L;
            Long actualProjectOwnerId = 300L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
            when(projectService.getOwnerId(101L)).thenReturn(actualProjectOwnerId);

            // When & Then
            assertThatThrownBy(() -> taskService.deleteTask(taskId, unauthorizedUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only project owner or collaborators can delete the task");

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when task is not found")
        void deleteTask_TaskNotFound_ThrowsException() {
            // Given
            Long taskId = 999L;
            Long userId = 201L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.deleteTask(taskId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Task not found");

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should set delete indicator to true")
        void deleteTask_SetsDeleteIndTrue() {
            // Given
            Long taskId = 2L;
            Long taskOwnerId = 202L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            assertThat(personalTask.isDeleteInd()).isFalse(); // Initially false

            // When
            taskService.deleteTask(taskId, taskOwnerId);

            // Then
            verify(taskRepository).save(argThat(task -> {
                assertThat(task.isDeleteInd()).isTrue();
                return true;
            }));
        }

        @Test
        @DisplayName("Should set updatedBy and updatedAt on delete")
        void deleteTask_SetsUpdatedByAndUpdatedAt() {
            // Given
            Long taskId = 2L;
            Long userId = 202L;
            OffsetDateTime beforeDelete = OffsetDateTime.now();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            taskService.deleteTask(taskId, userId);

            // Then
            verify(taskRepository).save(argThat(task -> {
                assertThat(task.getUpdatedBy()).isEqualTo(userId);
                assertThat(task.getUpdatedAt()).isNotNull();
                assertThat(task.getUpdatedAt()).isAfterOrEqualTo(beforeDelete);
                assertThat(task.getUpdatedAt()).isBeforeOrEqualTo(OffsetDateTime.now());
                return true;
            }));
        }

        @Test
        @DisplayName("Should throw exception when collaborator tries to delete project task")
        void deleteTask_ProjectTaskByCollaborator_ThrowsException() {
            // Given
            Long taskId = 1L;
            Long collaboratorId = 400L;
            Long projectOwnerId = 300L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
            when(projectService.getOwnerId(101L)).thenReturn(projectOwnerId);

            // When & Then
            assertThatThrownBy(() -> taskService.deleteTask(taskId, collaboratorId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only project owner or collaborators can delete the task");

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should throw exception when non-owner tries to delete personal task")
        void deleteTask_PersonalTaskByNonOwner_ThrowsException() {
            // Given
            Long taskId = 2L;
            Long unauthorizedUserId = 999L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));

            // When & Then
            assertThatThrownBy(() -> taskService.deleteTask(taskId, unauthorizedUserId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Only project owner or collaborators can delete the task");

            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should handle null project ID correctly")
        void deleteTask_NullProjectId_UsesTaskOwnerPermission() {
            // Given
            Long taskId = 2L;
            Long taskOwnerId = 202L;

            assertThat(personalTask.getProjectId()).isNull();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            assertThatCode(() -> taskService.deleteTask(taskId, taskOwnerId))
                .doesNotThrowAnyException();

            // Then
            verify(taskRepository).save(any(Task.class));
            verify(projectService, never()).getOwnerId(any());
        }

        @Test
        @DisplayName("Should verify deletion is soft delete, not hard delete")
        void deleteTask_SoftDelete_DoesNotDeleteFromDatabase() {
            // Given
            Long taskId = 2L;
            Long taskOwnerId = 202L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            taskService.deleteTask(taskId, taskOwnerId);

            // Then
            verify(taskRepository).save(any(Task.class)); // Saves with delete flag
            verify(taskRepository, never()).delete(any(Task.class)); // Does not hard delete
            verify(taskRepository, never()).deleteById(any(Long.class)); // Does not hard delete
        }
    }

    @Nested
    @DisplayName("canUserDeleteTask Tests")
    class CanUserDeleteTaskTests {

        private Task projectTask;
        private Task personalTask;

        @BeforeEach
        void setUp() {
            projectTask = createTestTask(1L, 101L, 201L, "Project Task",
                "Description", Status.TODO, Collections.emptyList());

            personalTask = createTestTask(2L, null, 202L, "Personal Task",
                "Description", Status.TODO, Collections.emptyList());
        }

        @Test
        @DisplayName("Should return true when user is project owner")
        void canUserDeleteTask_ProjectOwner_ReturnsTrue() {
            // Given
            Long taskId = 1L;
            Long projectOwnerId = 300L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
            when(projectService.getOwnerId(101L)).thenReturn(projectOwnerId);

            // When
            boolean result = taskService.canUserDeleteTask(taskId, projectOwnerId);

            // Then
            assertThat(result).isTrue();
            verify(projectService).getOwnerId(101L);
        }

        @Test
        @DisplayName("Should return false when user is not project owner")
        void canUserDeleteTask_NotProjectOwner_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long userId = 999L;
            Long actualProjectOwnerId = 300L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
            when(projectService.getOwnerId(101L)).thenReturn(actualProjectOwnerId);

            // When
            boolean result = taskService.canUserDeleteTask(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(projectService).getOwnerId(101L);
        }

        @Test
        @DisplayName("Should return true when user is owner of personal task")
        void canUserDeleteTask_PersonalTaskOwner_ReturnsTrue() {
            // Given
            Long taskId = 2L;
            Long taskOwnerId = 202L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));

            // When
            boolean result = taskService.canUserDeleteTask(taskId, taskOwnerId);

            // Then
            assertThat(result).isTrue();
            verify(projectService, never()).getOwnerId(any());
        }

        @Test
        @DisplayName("Should return false when user is not owner of personal task")
        void canUserDeleteTask_PersonalTaskNotOwner_ReturnsFalse() {
            // Given
            Long taskId = 2L;
            Long userId = 999L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));

            // When
            boolean result = taskService.canUserDeleteTask(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(projectService, never()).getOwnerId(any());
        }

        @Test
        @DisplayName("Should check task owner when project ID is null")
        void canUserDeleteTask_NullProjectId_ChecksTaskOwner() {
            // Given
            Long taskId = 2L;
            Long userId = 202L;

            assertThat(personalTask.getProjectId()).isNull();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(personalTask));

            // When
            boolean result = taskService.canUserDeleteTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(projectService, never()).getOwnerId(any());
        }

        @Test
        @DisplayName("Should throw exception when task is not found")
        void canUserDeleteTask_TaskNotFound_ThrowsException() {
            // Given
            Long taskId = 999L;
            Long userId = 201L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.canUserDeleteTask(taskId, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Task not found");

            verify(taskRepository).findById(taskId);
        }

        @Test
        @DisplayName("Should return false when user is task owner but not project owner")
        void canUserDeleteTask_TaskOwnerButNotProjectOwner_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long taskOwnerId = 201L;
            Long projectOwnerId = 300L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
            when(projectService.getOwnerId(101L)).thenReturn(projectOwnerId);

            // When
            boolean result = taskService.canUserDeleteTask(taskId, taskOwnerId);

            // Then
            assertThat(result).isFalse();
            verify(projectService).getOwnerId(101L);
        }

        @Test
        @DisplayName("Should handle null user ID gracefully")
        void canUserDeleteTask_NullUserId_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long userId = null;
            Long projectOwnerId = 300L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
            when(projectService.getOwnerId(101L)).thenReturn(projectOwnerId);

            // When
            boolean result = taskService.canUserDeleteTask(taskId, userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle different task and owner ID combinations")
        void canUserDeleteTask_DifferentIdCombinations_HandlesCorrectly() {
            // Test case 1: Project task with matching project owner
            Long taskId1 = 10L;
            Long projectOwnerId1 = 500L;
            Task task1 = createTestTask(taskId1, 105L, 400L, "Task 1", "Desc", Status.TODO, null);

            when(taskRepository.findById(taskId1)).thenReturn(Optional.of(task1));
            when(projectService.getOwnerId(105L)).thenReturn(projectOwnerId1);

            boolean result1 = taskService.canUserDeleteTask(taskId1, projectOwnerId1);
            assertThat(result1).isTrue();

            // Test case 2: Personal task with matching owner
            Long taskId2 = 11L;
            Long taskOwnerId2 = 600L;
            Task task2 = createTestTask(taskId2, null, taskOwnerId2, "Task 2", "Desc", Status.TODO, null);

            when(taskRepository.findById(taskId2)).thenReturn(Optional.of(task2));

            boolean result2 = taskService.canUserDeleteTask(taskId2, taskOwnerId2);
            assertThat(result2).isTrue();

            // Test case 3: Project task with non-matching user
            boolean result3 = taskService.canUserDeleteTask(taskId1, 999L);
            assertThat(result3).isFalse();
        }

        @Test
        @DisplayName("Should return false when collaborator checks delete permission")
        void canUserDeleteTask_Collaborator_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long collaboratorId = 400L;
            Long projectOwnerId = 300L;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(projectTask));
            when(projectService.getOwnerId(101L)).thenReturn(projectOwnerId);

            // When
            boolean result = taskService.canUserDeleteTask(taskId, collaboratorId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getProjectTasks Additional Tests")
    class GetProjectTasksAdditionalTests {

        @Test
        @DisplayName("Should return empty list when project has no tasks")
        void getProjectTasks_EmptyProject_ReturnsEmptyList() {
            // Given
            Long userId = 201L;
            Long projectId = 999L;

            when(taskRepository.findByProjectIdAndNotDeleted(projectId)).thenReturn(Collections.emptyList());

            // When
            List<TaskResponseDto> result = taskService.getProjectTasks(userId, projectId);

            // Then
            assertThat(result).isEmpty();
            verify(taskRepository).findByProjectIdAndNotDeleted(projectId);
        }

        @Test
        @DisplayName("Should set correct permissions for task owner")
        void getProjectTasks_TaskOwner_SetsCorrectPermissions() {
            // Given
            Long userId = 201L;
            Long projectId = 101L;
            Long projectOwnerId = 300L;

            Task ownedTask = createTestTask(1L, projectId, userId, "Owned Task",
                "Description", Status.TODO, Collections.emptyList());

            when(taskRepository.findByProjectIdAndNotDeleted(projectId))
                .thenReturn(Collections.singletonList(ownedTask));
            when(collaboratorService.getTasksForWhichUserIsCollaborator(userId))
                .thenReturn(Collections.emptyList());
            when(projectService.getOwnerId(projectId)).thenReturn(projectOwnerId);

            // When
            List<TaskResponseDto> result = taskService.getProjectTasks(userId, projectId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isUserHasEditAccess()).isTrue(); // Owner has edit access
            assertThat(result.get(0).isUserHasDeleteAccess()).isFalse(); // Not project owner
        }

        @Test
        @DisplayName("Should set correct permissions for project owner")
        void getProjectTasks_ProjectOwner_SetsCorrectPermissions() {
            // Given
            Long userId = 300L;
            Long projectId = 101L;

            Task task = createTestTask(1L, projectId, 201L, "Task",
                "Description", Status.TODO, Collections.emptyList());

            when(taskRepository.findByProjectIdAndNotDeleted(projectId))
                .thenReturn(Collections.singletonList(task));
            when(collaboratorService.getTasksForWhichUserIsCollaborator(userId))
                .thenReturn(Collections.emptyList());
            when(projectService.getOwnerId(projectId)).thenReturn(userId);

            // When
            List<TaskResponseDto> result = taskService.getProjectTasks(userId, projectId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isUserHasDeleteAccess()).isTrue(); // Project owner can delete
        }

        @Test
        @DisplayName("Should set correct permissions for collaborator")
        void getProjectTasks_Collaborator_SetsCorrectPermissions() {
            // Given
            Long userId = 400L;
            Long projectId = 101L;
            Long projectOwnerId = 300L;
            Long taskId = 1L;

            Task task = createTestTask(taskId, projectId, 201L, "Task",
                "Description", Status.TODO, Collections.emptyList());

            when(taskRepository.findByProjectIdAndNotDeleted(projectId))
                .thenReturn(Collections.singletonList(task));
            when(collaboratorService.getTasksForWhichUserIsCollaborator(userId))
                .thenReturn(Collections.singletonList(taskId));
            when(projectService.getOwnerId(projectId)).thenReturn(projectOwnerId);

            // When
            List<TaskResponseDto> result = taskService.getProjectTasks(userId, projectId);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isUserHasEditAccess()).isTrue(); // Collaborator has edit access
            assertThat(result.get(0).isUserHasDeleteAccess()).isFalse(); // Not project owner
        }

        @Test
        @DisplayName("Should handle multiple tasks with mixed permissions")
        void getProjectTasks_MultipleTasks_SetsMixedPermissions() {
            // Given
            Long userId = 201L;
            Long projectId = 101L;
            Long projectOwnerId = 300L;

            Task ownedTask = createTestTask(1L, projectId, userId, "Owned Task",
                "Description", Status.TODO, Collections.emptyList());
            Task collaboratorTask = createTestTask(2L, projectId, 202L, "Collaborator Task",
                "Description", Status.TODO, Collections.emptyList());
            Task otherTask = createTestTask(3L, projectId, 203L, "Other Task",
                "Description", Status.TODO, Collections.emptyList());

            when(taskRepository.findByProjectIdAndNotDeleted(projectId))
                .thenReturn(Arrays.asList(ownedTask, collaboratorTask, otherTask));
            when(collaboratorService.getTasksForWhichUserIsCollaborator(userId))
                .thenReturn(Collections.singletonList(2L));
            when(projectService.getOwnerId(projectId)).thenReturn(projectOwnerId);

            // When
            List<TaskResponseDto> result = taskService.getProjectTasks(userId, projectId);

            // Then
            assertThat(result).hasSize(3);
            assertThat(result.get(0).isUserHasEditAccess()).isTrue(); // Owned task
            assertThat(result.get(1).isUserHasEditAccess()).isTrue(); // Collaborator task
            assertThat(result.get(2).isUserHasEditAccess()).isFalse(); // Other task
        }

        @Test
        @DisplayName("Manager cannot access project outside department scope")
        void getProjectTasks_ManagerOutsideScope_ThrowsAccessDenied() {
            Long userId = 500L;
            Long projectId = 808L;

            User manager = createUser(userId);
            manager.setRoleType(UserType.MANAGER.getCode());
            manager.setDepartment("Marketing");

            when(userRepository.findById(userId)).thenReturn(Optional.of(manager));

            Department department = new Department();
            department.setId(50L);
            department.setName("Marketing");

            when(departmentRepository.findByNameIgnoreCase("Marketing"))
                .thenReturn(Optional.of(department));
            when(departmentQueryService.getDescendants(50L, true))
                .thenReturn(List.of(DepartmentDto.builder().id(50L).name("Marketing").parentId(null).build()));
            when(userRepository.findActiveUsersByDepartmentsIgnoreCase(Set.of("marketing")))
                .thenReturn(List.of(manager));

            Task externalTask = createTestTask(1L, projectId, 900L, "External", "Desc", Status.TODO, Collections.emptyList());
            externalTask.setOwnerId(888L);

            when(taskRepository.findByProjectIdAndNotDeleted(projectId))
                .thenReturn(List.of(externalTask));
            when(collaboratorService.getTasksForWhichUserIsCollaborator(userId))
                .thenReturn(Collections.emptyList());
            when(projectService.getOwnerId(projectId)).thenReturn(777L);
            when(taskAssigneeRepository.findByTaskIdIn(Set.of(1L))).thenReturn(Collections.emptyList());

            assertThatThrownBy(() -> taskService.getProjectTasks(userId, projectId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("departmental scope");
        }
    }

    @Nested
    @DisplayName("Time Tracking Integration Tests")
    class TimeTrackingIntegrationTests {

        private Task task;
        private Long taskId;
        private Long userId;

        @BeforeEach
        void setUp() {
            taskId = 1L;
            userId = 201L;
            task = createTestTask(taskId, 101L, userId, "Test Task", "Description", Status.TODO, Collections.emptyList());
        }

        @Test
        @DisplayName("Should start time tracking when status changes from TODO to IN_PROGRESS")
        void updateTask_TodoToInProgress_StartsTimeTracking() {
            // Given
            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.IN_PROGRESS)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(reportService).startTimeTracking(taskId, userId);
            verify(reportService, never()).endTimeTracking(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should end time tracking when status changes from IN_PROGRESS to COMPLETED")
        void updateTask_InProgressToCompleted_EndsTimeTracking() {
            // Given
            task.setStatus(Status.IN_PROGRESS);

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(reportService).endTimeTracking(taskId, userId);
            verify(reportService, never()).startTimeTracking(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should restart time tracking when status changes from COMPLETED to IN_PROGRESS")
        void updateTask_CompletedToInProgress_RestartsTimeTracking() {
            // Given
            task.setStatus(Status.COMPLETED);

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.IN_PROGRESS)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(reportService).startTimeTracking(taskId, userId);
            verify(reportService, never()).endTimeTracking(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should not trigger time tracking when status is unchanged")
        void updateTask_SameStatus_DoesNotTriggerTimeTracking() {
            // Given
            task.setStatus(Status.TODO);

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .title("Updated Title")
                .build(); // No status change

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(reportService, never()).startTimeTracking(anyLong(), anyLong());
            verify(reportService, never()).endTimeTracking(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should end time tracking for recurring task completion before creating next instance")
        void updateTask_RecurringTaskCompleted_EndsTimeTrackingBeforeCreatingNextInstance() {
            // Given
            task.setStatus(Status.IN_PROGRESS);
            task.setIsRecurring(true);
            task.setRecurrenceRuleStr("FREQ=DAILY;COUNT=5");
            task.setDueDateTime(OffsetDateTime.now());
            task.setStartDate(OffsetDateTime.now());
            task.setEndDate(OffsetDateTime.now().plusDays(10));

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);
            lenient().when(recurrenceService.generateOccurrence(anyString(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(Collections.singletonList(OffsetDateTime.now().plusDays(1)));
            lenient().when(collaboratorService.getCollaboratorIdsByTaskId(taskId)).thenReturn(Collections.emptyList());

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(reportService).endTimeTracking(taskId, userId);
            // Verify at least one save happened (the original task update)
            // The new recurring instance is created via createTask which also calls save
            verify(taskRepository, atLeastOnce()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should track time for recurring task edit with status change")
        void updateTask_RecurringEditWithStatusChange_TracksTime() {
            // Given
            task.setStatus(Status.TODO);
            task.setIsRecurring(true);
            task.setRecurrenceRuleStr("FREQ=DAILY;COUNT=5");
            task.setDueDateTime(OffsetDateTime.now());

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.IN_PROGRESS)
                .recurrenceEditMode(com.spmorangle.crm.taskmanagement.enums.RecurrenceEditMode.ALL_FUTURE_INSTANCES)
                .instanceDate(OffsetDateTime.now())
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(reportService).startTimeTracking(taskId, userId);
        }

        @Test
        @DisplayName("Should handle time tracking errors gracefully without failing task update")
        void updateTask_TimeTrackingFails_ContinuesTaskUpdate() {
            // Given
            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.IN_PROGRESS)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);
            // Simulate time tracking failure - this should be caught and logged
            lenient().doThrow(new RuntimeException("Time tracking service unavailable"))
                .when(reportService).startTimeTracking(anyLong(), anyLong());

            // When & Then - Should not throw exception
            assertThatCode(() -> taskService.updateTask(updateDto, userId))
                .doesNotThrowAnyException();

            // Task should still be saved
            verify(taskRepository).save(argThat(t -> t.getStatus() == Status.IN_PROGRESS));
        }

        @Test
        @DisplayName("Should end time tracking when non-recurring task moves to COMPLETED from TODO")
        void updateTask_TodoToCompleted_EndsTimeTracking() {
            // Given
            task.setStatus(Status.TODO);

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(reportService).endTimeTracking(taskId, userId);
        }

        @Test
        @DisplayName("Should not trigger time tracking when changing from TODO to BLOCKED")
        void updateTask_TodoToBlocked_DoesNotTriggerTimeTracking() {
            // Given
            task.setStatus(Status.TODO);

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.BLOCKED)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(reportService, never()).startTimeTracking(anyLong(), anyLong());
            verify(reportService, never()).endTimeTracking(anyLong(), anyLong());
        }
    }

    @Nested
    @DisplayName("Task Recurrence Tests")
    class TaskRecurrenceTests {

        private Task recurringTask;
        private Long taskId;
        private Long userId;
        private OffsetDateTime now;

        @BeforeEach
        void setUp() {
            taskId = 1L;
            userId = 201L;
            now = OffsetDateTime.now();

            recurringTask = createTestTask(taskId, 101L, userId, "Recurring Task", "Description", Status.TODO, Collections.emptyList());
            recurringTask.setIsRecurring(true);
            recurringTask.setRecurrenceRuleStr("FREQ=DAILY;COUNT=5");
            recurringTask.setDueDateTime(now);
            recurringTask.setStartDate(now);
            recurringTask.setEndDate(now.plusDays(10));
        }

        @Test
        @DisplayName("Should create next instance when recurring task is completed")
        void updateTask_RecurringTaskCompleted_CreatesNextInstance() {
            // Given
            recurringTask.setStatus(Status.IN_PROGRESS);
            recurringTask.setTags(new HashSet<>()); // Set empty tags set

            // Include dueDateTime and recurrence fields to prevent them from being nulled out by applyFieldUpdates
            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .dueDateTime(recurringTask.getDueDateTime())
                .isRecurring(recurringTask.getIsRecurring())
                .recurrenceRuleStr(recurringTask.getRecurrenceRuleStr())
                .startDate(recurringTask.getStartDate())
                .endDate(recurringTask.getEndDate())
                .build();

            OffsetDateTime nextStart = recurringTask.getDueDateTime().plusDays(1);
            OffsetDateTime nextOccurrence = nextStart; // The next occurrence date

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(recurringTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);
            
            // Mock validation call in applyFieldUpdates (uses task's original startDate and endDate)
            lenient().when(recurrenceService.generateOccurrence(
                eq("FREQ=DAILY;COUNT=5"),
                eq(recurringTask.getStartDate()),
                eq(recurringTask.getEndDate())
            )).thenReturn(Collections.singletonList(recurringTask.getStartDate()));
            
            // Mock generateOccurrence to return next occurrence based on nextStart (dueDate + 1 day)
            when(recurrenceService.generateOccurrence(eq("FREQ=DAILY;COUNT=5"), eq(nextStart), eq(recurringTask.getEndDate())))
                .thenReturn(Collections.singletonList(nextOccurrence));
            when(collaboratorService.getCollaboratorIdsByTaskId(taskId)).thenReturn(Collections.emptyList());
            // Mock dependencies for createTask call inside the recurrence logic
            lenient().when(tagService.findOrCreateTags(any())).thenReturn(Collections.emptySet());
            lenient().when(taskAssigneeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            UpdateTaskResponseDto result = taskService.updateTask(updateDto, userId);

            // Then
            assertThat(result.getStatus()).isEqualTo(Status.COMPLETED);
            // Verify recurrence service was called with nextStart = dueDate + 1 day
            verify(recurrenceService, atLeastOnce()).generateOccurrence(
                eq("FREQ=DAILY;COUNT=5"),
                eq(nextStart),
                eq(recurringTask.getEndDate())
            );
            // Verify task creation was attempted (createTask also saves)
            verify(taskRepository, atLeastOnce()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should not create next instance when no more occurrences exist")
        void updateTask_RecurringTaskCompleted_NoMoreOccurrences_DoesNotCreateInstance() {
            // Given
            recurringTask.setStatus(Status.IN_PROGRESS);

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(recurringTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);
            lenient().when(recurrenceService.generateOccurrence(anyString(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(Collections.emptyList()); // No more occurrences

            // When
            UpdateTaskResponseDto result = taskService.updateTask(updateDto, userId);

            // Then
            assertThat(result.getStatus()).isEqualTo(Status.COMPLETED);
            // Only the original task should be saved, no new instance created
            verify(taskRepository, times(1)).save(any(Task.class));
        }

        @Test
        @DisplayName("Should preserve task properties in next recurring instance")
        void updateTask_RecurringTaskCompleted_PreservesTaskProperties() {
            // Given
            recurringTask.setStatus(Status.IN_PROGRESS);
            recurringTask.setTitle("Weekly Report");
            recurringTask.setDescription("Submit weekly report");
            recurringTask.setTaskType(TaskType.CHORE);

            List<Tag> tags = new ArrayList<>();
            Tag tag1 = new Tag();
            tag1.setTagName("important");
            Tag tag2 = new Tag();
            tag2.setTagName("weekly");
            tags.add(tag1);
            tags.add(tag2);
            recurringTask.setTags(new HashSet<>(tags));

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .build();

            OffsetDateTime nextOccurrence = now.plusDays(7);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(recurringTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);
            lenient().when(recurrenceService.generateOccurrence(anyString(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenReturn(Collections.singletonList(nextOccurrence));
            lenient().when(collaboratorService.getCollaboratorIdsByTaskId(taskId)).thenReturn(Arrays.asList(301L, 302L));

            // When
            taskService.updateTask(updateDto, userId);

            // Then - Verify createTask is called (which internally saves)
            // The new instance should have the same properties but with Status.TODO
            verify(taskRepository, atLeastOnce()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should handle recurring task creation failure gracefully")
        void updateTask_RecurringTaskCompleted_CreationFails_DoesNotThrowException() {
            // Given
            recurringTask.setStatus(Status.IN_PROGRESS);

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(recurringTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);
            lenient().when(recurrenceService.generateOccurrence(anyString(), any(OffsetDateTime.class), any(OffsetDateTime.class)))
                .thenThrow(new RuntimeException("Invalid recurrence rule"));

            // When & Then - Should not throw exception
            assertThatCode(() -> taskService.updateTask(updateDto, userId))
                .doesNotThrowAnyException();

            // Original task should still be marked as completed
            verify(taskRepository).save(argThat(task ->
                task.getStatus() == Status.COMPLETED
            ));
        }

        @Test
        @DisplayName("Should not create next instance if recurring task missing required fields")
        void updateTask_RecurringTaskCompleted_MissingRequiredFields_DoesNotCreateInstance() {
            // Given
            recurringTask.setStatus(Status.IN_PROGRESS);
            recurringTask.setRecurrenceRuleStr(null); // Missing recurrence rule

            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(recurringTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);

            // When
            UpdateTaskResponseDto result = taskService.updateTask(updateDto, userId);

            // Then
            assertThat(result.getStatus()).isEqualTo(Status.COMPLETED);
            // Should not call recurrenceService since recurrence rule is missing
            verify(recurrenceService, never()).generateOccurrence(anyString(), any(), any());
        }

        @Test
        @DisplayName("Should replicate collaborators to next recurring instance")
        void updateTask_RecurringTaskCompleted_ReplicatesCollaborators() {
            // Given
            recurringTask.setStatus(Status.IN_PROGRESS);
            List<Long> collaboratorIds = Arrays.asList(301L, 302L, 303L);

            // Include dueDateTime and recurrence fields to prevent them from being nulled out by applyFieldUpdates
            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .dueDateTime(recurringTask.getDueDateTime())
                .isRecurring(recurringTask.getIsRecurring())
                .recurrenceRuleStr(recurringTask.getRecurrenceRuleStr())
                .startDate(recurringTask.getStartDate())
                .endDate(recurringTask.getEndDate())
                .build();

            OffsetDateTime nextStart = recurringTask.getDueDateTime().plusDays(1);
            OffsetDateTime nextOccurrence = nextStart;

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(recurringTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);
            
            // Mock validation call in applyFieldUpdates (uses task's original startDate and endDate)
            lenient().when(recurrenceService.generateOccurrence(
                eq("FREQ=DAILY;COUNT=5"),
                eq(recurringTask.getStartDate()),
                eq(recurringTask.getEndDate())
            )).thenReturn(Collections.singletonList(recurringTask.getStartDate()));
            
            when(recurrenceService.generateOccurrence(eq("FREQ=DAILY;COUNT=5"), eq(nextStart), eq(recurringTask.getEndDate())))
                .thenReturn(Collections.singletonList(nextOccurrence));
            when(collaboratorService.getCollaboratorIdsByTaskId(taskId)).thenReturn(collaboratorIds);
            // Mock dependencies for createTask call inside the recurrence logic
            lenient().when(tagService.findOrCreateTags(any())).thenReturn(Collections.emptySet());
            lenient().when(taskAssigneeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            taskService.updateTask(updateDto, userId);

            // Then
            verify(collaboratorService, atLeastOnce()).getCollaboratorIdsByTaskId(taskId);
            verify(taskRepository, atLeastOnce()).save(any(Task.class));
        }

        @Test
        @DisplayName("Should calculate next occurrence starting from day after current due date")
        void updateTask_RecurringTaskCompleted_CalculatesNextOccurrenceCorrectly() {
            // Given
            OffsetDateTime specificDueDate = OffsetDateTime.parse("2025-10-20T10:00:00+08:00");
            recurringTask.setDueDateTime(specificDueDate);
            recurringTask.setStatus(Status.IN_PROGRESS);

            OffsetDateTime expectedNextStart = specificDueDate.plusDays(1);

            // Include dueDateTime and recurrence fields to prevent them from being nulled out by applyFieldUpdates
            UpdateTaskDto updateDto = UpdateTaskDto.builder()
                .taskId(taskId)
                .status(Status.COMPLETED)
                .dueDateTime(specificDueDate)
                .isRecurring(recurringTask.getIsRecurring())
                .recurrenceRuleStr(recurringTask.getRecurrenceRuleStr())
                .startDate(recurringTask.getStartDate())
                .endDate(recurringTask.getEndDate())
                .build();

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(recurringTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(projectService.getOwnerId(101L)).thenReturn(userId);
            
            // Mock validation call in applyFieldUpdates (uses task's original startDate and endDate)
            lenient().when(recurrenceService.generateOccurrence(
                eq("FREQ=DAILY;COUNT=5"),
                eq(recurringTask.getStartDate()),
                eq(recurringTask.getEndDate())
            )).thenReturn(Collections.singletonList(recurringTask.getStartDate()));
            
            when(recurrenceService.generateOccurrence(eq("FREQ=DAILY;COUNT=5"), eq(expectedNextStart), eq(recurringTask.getEndDate())))
                .thenReturn(Collections.singletonList(expectedNextStart));
            when(collaboratorService.getCollaboratorIdsByTaskId(taskId)).thenReturn(Collections.emptyList());
            // Mock dependencies for createTask call inside the recurrence logic
            lenient().when(tagService.findOrCreateTags(any())).thenReturn(Collections.emptySet());
            lenient().when(taskAssigneeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            // When
            taskService.updateTask(updateDto, userId);

            // Then - Verify generateOccurrence is called with nextStart = dueDate + 1 day
            // Note: Called twice - once for validation in applyFieldUpdates, once for next occurrence calculation
            verify(recurrenceService, times(2)).generateOccurrence(
                eq("FREQ=DAILY;COUNT=5"),
                argThat(nextStart -> nextStart.isEqual(expectedNextStart)),
                eq(recurringTask.getEndDate())
            );
        }
    }
}
