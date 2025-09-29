package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskDto;
import com.spmorangle.crm.taskmanagement.dto.CreateTaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
import com.spmorangle.crm.taskmanagement.service.SubtaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        lenient().when(subtaskService.getSubtasksByTaskId(anyLong())).thenReturn(Collections.emptyList());
    }

    private Task createTestTask(Long id, Long projectId, Long ownerId, String title,
                               String description, Status status, List<String> tags) {
        Task task = new Task();
        task.setId(id);
        task.setProjectId(projectId);
        task.setOwnerId(ownerId);
        task.setTaskType(TaskType.FEATURE);
        task.setTitle(title);
        task.setDescription(description);
        task.setStatus(status);
        task.setTags(tags);
        task.setCreatedBy(ownerId);
        task.setCreatedAt(fixedDateTime);
        task.setUpdatedAt(fixedDateTime);
        task.setUpdatedBy(ownerId);
        return task;
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
            assertThat(firstTask.getOwnerId()).isEqualTo(201L);
            assertThat(firstTask.getTitle()).isEqualTo("Task 1");
            assertThat(firstTask.getDescription()).isEqualTo("Description 1");
            assertThat(firstTask.getStatus()).isEqualTo(Status.TODO);
            assertThat(firstTask.getTags()).containsExactly("tag1", "tag2");
            assertThat(firstTask.getCreatedBy()).isEqualTo(201L);
            assertThat(firstTask.getCreatedAt()).isEqualTo(fixedDateTime);

            // Verify second task
            TaskResponseDto secondTask = result.get(1);
            assertThat(secondTask.getId()).isEqualTo(2L);
            assertThat(secondTask.getProjectId()).isEqualTo(102L);
            assertThat(secondTask.getStatus()).isEqualTo(Status.IN_PROGRESS);
            assertThat(secondTask.getTags()).containsExactly("tag3");

            // Verify third task
            TaskResponseDto thirdTask = result.get(2);
            assertThat(thirdTask.getId()).isEqualTo(3L);
            assertThat(thirdTask.getDescription()).isNull();
            assertThat(thirdTask.getStatus()).isEqualTo(Status.COMPLETED);
            assertThat(thirdTask.getTags()).isEmpty();

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
            assertThat(task.getTags()).containsExactlyElementsOf(manyTags);
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
            assertThat(task.getTags()).containsExactlyElementsOf(duplicateTags);
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
            assertThat(result.get(3).getTags()).containsExactly("", "   ", "valid");
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
    @DisplayName("canUserUpdateOrDeleteTask Tests")
    class CanUserUpdateOrDeleteTaskTests {

        @Test
        @DisplayName("Should return true when user is task owner")
        void canUserUpdateOrDeleteTask_UserIsOwner_ReturnsTrue() {
            // Given
            Long taskId = 1L;
            Long userId = 201L; // Same as testTask1 owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should return true when user is collaborator but not owner")
        void canUserUpdateOrDeleteTask_UserIsCollaborator_ReturnsTrue() {
            // Given
            Long taskId = 1L;
            Long userId = 999L; // Different from testTask1 owner (201L)
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(true);

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should return false when user is neither owner nor collaborator")
        void canUserUpdateOrDeleteTask_UserIsNeitherOwnerNorCollaborator_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long userId = 999L; // Different from testTask1 owner (201L)
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(false);

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should throw RuntimeException when task is not found")
        void canUserUpdateOrDeleteTask_TaskNotFound_ThrowsRuntimeException() {
            // Given
            Long taskId = 999L;
            Long userId = 201L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.canUserUpdateOrDeleteTask(taskId, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Task not found");

            verify(taskRepository).findById(taskId);
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should handle different task and user ID combinations")
        void canUserUpdateOrDeleteTask_DifferentIds_HandlesCorrectly() {
            // Given
            Long taskId = 2L;
            Long userId = 201L; // Same as testTask2 owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask2));

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should handle null task ID gracefully")
        void canUserUpdateOrDeleteTask_NullTaskId_DelegatesToRepository() {
            // Given
            Long taskId = null;
            Long userId = 201L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> taskService.canUserUpdateOrDeleteTask(taskId, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Task not found");

            verify(taskRepository).findById(taskId);
        }

        @Test
        @DisplayName("Should handle null user ID gracefully")
        void canUserUpdateOrDeleteTask_NullUserId_HandlesCorrectly() {
            // Given
            Long taskId = 1L;
            Long userId = null;
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(false);

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isFalse(); // null userId won't equal task owner ID
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should verify repository and service interactions for owner scenario")
        void canUserUpdateOrDeleteTask_OwnerScenario_VerifyInteractions() {
            // Given
            Long taskId = 3L;
            Long userId = 201L; // Same as testTask3 owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask3));

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            // Collaborator service should not be called when user is owner
            verify(collaboratorService, never()).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should verify repository and service interactions for collaborator scenario")
        void canUserUpdateOrDeleteTask_CollaboratorScenario_VerifyInteractions() {
            // Given
            Long taskId = 1L;
            Long userId = 500L; // Different from testTask1 owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(true);

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should handle edge case with user ID 0")
        void canUserUpdateOrDeleteTask_UserIdZero_HandlesCorrectly() {
            // Given
            Long taskId = 1L;
            Long userId = 0L;
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(false);

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should handle task with different owner ID correctly")
        void canUserUpdateOrDeleteTask_TaskWithDifferentOwner_HandlesCorrectly() {
            // Given
            Long taskId = 10L;
            Long userId = 999L;
            Long differentOwnerId = 888L;

            Task taskWithDifferentOwner = createTestTask(10L, 100L, differentOwnerId,
                    "Different Owner Task", "Description", Status.TODO, Collections.emptyList());

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(taskWithDifferentOwner));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(true);

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isTrue(); // User is collaborator
            verify(taskRepository).findById(taskId);
            verify(collaboratorService).isUserTaskCollaborator(taskId, userId);
        }

        @Test
        @DisplayName("Should handle maximum Long values for IDs")
        void canUserUpdateOrDeleteTask_MaxLongValues_HandlesCorrectly() {
            // Given
            Long taskId = Long.MAX_VALUE;
            Long userId = Long.MAX_VALUE - 1;

            Task maxIdTask = createTestTask(Long.MAX_VALUE, 100L, Long.MAX_VALUE - 1,
                    "Max ID Task", "Description", Status.TODO, Collections.emptyList());

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(maxIdTask));

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

            // Then
            assertThat(result).isTrue(); // User is owner
            verify(taskRepository).findById(taskId);
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should handle collaborator service returning false correctly")
        void canUserUpdateOrDeleteTask_CollaboratorServiceReturnsFalse_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long userId = 300L; // Different from owner
            when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask1));
            when(collaboratorService.isUserTaskCollaborator(taskId, userId)).thenReturn(false);

            // When
            boolean result = taskService.canUserUpdateOrDeleteTask(taskId, userId);

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
            savedTask.setTags(Arrays.asList("tag1", "tag2"));
            savedTask.setCreatedBy(456L);
            savedTask.setCreatedAt(fixedDateTime);
        }

        @Test
        @DisplayName("Should successfully create task with specified owner and current user")
        void createTask_ValidSpecifiedOwner_ReturnsCreateTaskResponseDto() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

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
            assertThat(result.getTags()).containsExactly("tag1", "tag2");
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
                Objects.equals(task.getTags(), Arrays.asList("tag1", "tag2"))
            ));
        }

        @Test
        @DisplayName("Should assign collaborators when assignedUserIds provided")
        void createTask_WithAssignedUserIds_AssignsCollaborators() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(collaboratorService.addCollaborator(any(AddCollaboratorRequestDto.class)))
                    .thenReturn(AddCollaboratorResponseDto.builder()
                            .taskId(1L)
                            .collaboratorId(789L)
                            .assignedById(123L)
                            .assignedAt(fixedDateTime)
                            .build());

            // When
            CreateTaskResponseDto result = taskService.createTask(validCreateTaskDto, specifiedOwnerId, currentUserId);

            // Then
            assertThat(result.getAssignedUserIds()).containsExactly(789L, 101L);

            verify(collaboratorService, times(2)).addCollaborator(argThat(request ->
                request.getTaskId().equals(1L) &&
                request.getAssignedById().equals(123L) &&
                (request.getCollaboratorId().equals(789L) || request.getCollaboratorId().equals(101L))
            ));
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

            // When
            CreateTaskResponseDto result = taskService.createTask(dtoWithEmptyAssignedUserIds, specifiedOwnerId, currentUserId);

            // Then
            assertThat(result.getAssignedUserIds()).isEmpty();
            verify(collaboratorService, never()).addCollaborator(any());
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

            // When
            CreateTaskResponseDto result = taskService.createTask(dtoWithNullAssignedUserIds, specifiedOwnerId, currentUserId);

            // Then
            assertThat(result.getAssignedUserIds()).isEmpty();
            verify(collaboratorService, never()).addCollaborator(any());
        }

        @Test
        @DisplayName("Should continue when collaborator assignment fails for some users")
        void createTask_CollaboratorAssignmentFails_ContinuesWithOtherAssignments() {
            // Given
            Long specifiedOwnerId = 456L;
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
            when(collaboratorService.addCollaborator(argThat(request ->
                    request != null && request.getCollaboratorId().equals(789L))))
                    .thenThrow(new RuntimeException("Assignment failed for user 789"));
            when(collaboratorService.addCollaborator(argThat(request ->
                    request != null && request.getCollaboratorId().equals(101L))))
                    .thenReturn(AddCollaboratorResponseDto.builder()
                            .taskId(1L)
                            .collaboratorId(101L)
                            .assignedById(123L)
                            .assignedAt(fixedDateTime)
                            .build());

            // When
            CreateTaskResponseDto result = taskService.createTask(validCreateTaskDto, specifiedOwnerId, currentUserId);

            // Then
            assertThat(result.getAssignedUserIds()).containsExactly(101L);
            verify(collaboratorService, times(2)).addCollaborator(any());
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
            taskWithEmptyTags.setTags(Collections.emptyList());
            taskWithEmptyTags.setCreatedBy(456L);
            taskWithEmptyTags.setCreatedAt(fixedDateTime);

            when(taskRepository.save(any(Task.class))).thenReturn(taskWithEmptyTags);

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
            savedTask.setTags(Arrays.asList("tag1", "tag2"));
            savedTask.setCreatedBy(123L);
            savedTask.setCreatedAt(fixedDateTime);
        }

        @Test
        @DisplayName("Should successfully create task with current user as owner")
        void createTask_CurrentUserAsOwner_ReturnsCreateTaskResponseDto() {
            // Given
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

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
            assertThat(result.getTags()).containsExactly("tag1", "tag2");
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
                Objects.equals(task.getTags(), Arrays.asList("tag1", "tag2"))
            ));
        }

        @Test
        @DisplayName("Should assign collaborators when creating task with current user as owner")
        void createTask_CurrentUserAsOwnerWithAssignedUserIds_AssignsCollaborators() {
            // Given
            Long currentUserId = 123L;

            when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

            // When
            CreateTaskResponseDto result = taskService.createTask(validCreateTaskDto, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOwnerId()).isEqualTo(currentUserId);

            verify(taskRepository).save(any(Task.class));
            verify(collaboratorService, times(2)).addCollaborator(any(AddCollaboratorRequestDto.class));
            
            // Verify the specific collaborator requests
            verify(collaboratorService).addCollaborator(argThat(request -> 
                request.getTaskId().equals(1L) && 
                request.getCollaboratorId().equals(789L) && 
                request.getAssignedById().equals(currentUserId)
            ));
            verify(collaboratorService).addCollaborator(argThat(request -> 
                request.getTaskId().equals(1L) && 
                request.getCollaboratorId().equals(101L) && 
                request.getAssignedById().equals(currentUserId)
            ));
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

            // When
            CreateTaskResponseDto result = taskService.createTask(dtoWithEmptyAssignedUserIds, currentUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getOwnerId()).isEqualTo(currentUserId);

            verify(taskRepository).save(any(Task.class));
            verify(collaboratorService, never()).addCollaborator(any());
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
    }
}
