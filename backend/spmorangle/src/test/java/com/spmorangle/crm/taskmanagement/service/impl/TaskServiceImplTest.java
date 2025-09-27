package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
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
                                  Status.IN_PROGRESS, Arrays.asList("tag3"));

        testTask3 = createTestTask(3L, 103L, 201L, "Task 3", null,
                                  Status.COMPLETED, Collections.emptyList());

        // Mock subtaskService to return empty lists for all task IDs
//        when(subtaskService.getSubtasksByTaskId(anyLong())).thenReturn(Collections.emptyList());
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
}