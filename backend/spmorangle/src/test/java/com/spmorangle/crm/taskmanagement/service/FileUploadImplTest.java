package com.spmorangle.crm.taskmanagement.service;

import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.model.TaskAssigneeCK;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAlreadyExistsException;
import com.spmorangle.crm.taskmanagement.service.exception.CollaboratorAssignmentNotFoundException;
import com.spmorangle.crm.taskmanagement.service.impl.CollaboratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollaboratorService Tests")
class FileUploadImplTest {

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CollaboratorServiceImpl collaboratorService;

    private AddCollaboratorRequestDto addRequest;
    private RemoveCollaboratorRequestDto removeRequest;
    private TaskAssignee taskAssignee;

    @BeforeEach
    void setUp() {
        addRequest = AddCollaboratorRequestDto.builder()
                .taskId(1L)
                .collaboratorId(2L)
                .build();
        removeRequest = new RemoveCollaboratorRequestDto(1L, 2L);

        taskAssignee = new TaskAssignee();
        taskAssignee.setTaskId(1L);
        taskAssignee.setUserId(2L);
        taskAssignee.setAssignedId(3L);
        taskAssignee.setAssignedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("Add Collaborator Tests")
    class AddCollaboratorTests {

        @Test
        @DisplayName("Should successfully add collaborator when assignment doesn't exist")
        void addCollaborator_ValidRequest_ReturnsResponseDto() {
            // Given
            when(taskRepository.existsById(1L)).thenReturn(true);
            when(userRepository.existsById(2L)).thenReturn(true);
            when(userRepository.existsById(3L)).thenReturn(true);
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(false);
            when(taskAssigneeRepository.save(any(TaskAssignee.class)))
                    .thenReturn(taskAssignee);

            // When
            AddCollaboratorResponseDto result = collaboratorService.addCollaborator(addRequest, 3L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTaskId()).isEqualTo(1L);
            assertThat(result.getCollaboratorId()).isEqualTo(2L);
            assertThat(result.getAssignedById()).isEqualTo(3L);
            assertThat(result.getAssignedAt()).isNotNull();

            // Verify repository interactions
            verify(taskAssigneeRepository).existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L);
            verify(taskAssigneeRepository).save(any(TaskAssignee.class));
        }

        @Test
        @DisplayName("Should save TaskAssignee with correct properties")
        void addCollaborator_ValidRequest_SavesCorrectTaskAssignee() {
            // Given
            when(taskRepository.existsById(anyLong())).thenReturn(true);
            when(userRepository.existsById(anyLong())).thenReturn(true);
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(false);
            when(taskAssigneeRepository.save(any(TaskAssignee.class)))
                    .thenReturn(taskAssignee);

            ArgumentCaptor<TaskAssignee> taskAssigneeCaptor = ArgumentCaptor.forClass(TaskAssignee.class);

            // When
            collaboratorService.addCollaborator(addRequest, 3L);

            // Then
            verify(taskAssigneeRepository).save(taskAssigneeCaptor.capture());
            TaskAssignee savedTaskAssignee = taskAssigneeCaptor.getValue();

            assertThat(savedTaskAssignee.getTaskId()).isEqualTo(1L);
            assertThat(savedTaskAssignee.getUserId()).isEqualTo(2L);
            assertThat(savedTaskAssignee.getAssignedId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should throw CollaboratorAlreadyExistsException when assignment already exists")
        void addCollaborator_CollaboratorAlreadyExists_ThrowsException() {
            // Given
            when(taskRepository.existsById(1L)).thenReturn(true);
            when(userRepository.existsById(2L)).thenReturn(true);
            when(userRepository.existsById(3L)).thenReturn(true);
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> collaboratorService.addCollaborator(addRequest, 3L))
                    .isInstanceOf(CollaboratorAlreadyExistsException.class)
                    .hasMessage("Collaborator 2 already assigned to task 1");

            // Verify save is never called
            verify(taskAssigneeRepository, never()).save(any(TaskAssignee.class));
        }

        @Test
        @DisplayName("Should handle different task and collaborator IDs correctly")
        void addCollaborator_DifferentIds_HandlesCorrectly() {
            // Given
            AddCollaboratorRequestDto differentRequest = AddCollaboratorRequestDto.builder()
                    .taskId(100L)
                    .collaboratorId(200L)
                    .build();
            TaskAssignee differentTaskAssignee = new TaskAssignee();
            differentTaskAssignee.setTaskId(100L);
            differentTaskAssignee.setUserId(200L);
            differentTaskAssignee.setAssignedId(300L);
            differentTaskAssignee.setAssignedAt(OffsetDateTime.now());

            when(taskRepository.existsById(100L)).thenReturn(true);
            when(userRepository.existsById(200L)).thenReturn(true);
            when(userRepository.existsById(300L)).thenReturn(true);
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(100L, 200L, 300L))
                    .thenReturn(false);
            when(taskAssigneeRepository.save(any(TaskAssignee.class)))
                    .thenReturn(differentTaskAssignee);

            // When
            AddCollaboratorResponseDto result = collaboratorService.addCollaborator(differentRequest, 300L);

            // Then
            assertThat(result.getTaskId()).isEqualTo(100L);
            assertThat(result.getCollaboratorId()).isEqualTo(200L);
            assertThat(result.getAssignedById()).isEqualTo(300L);

            verify(taskAssigneeRepository).existsByTaskIdAndUserIdAndAssignedId(100L, 200L, 300L);
        }
    }

    @Nested
    @DisplayName("Remove Collaborator Tests")
    class RemoveCollaboratorTests {

        @Test
        @DisplayName("Should successfully remove collaborator when assignment exists")
        void removeCollaborator_ValidRequest_RemovesCollaborator() {
            // Given
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(true);

            // When
            collaboratorService.removeCollaborator(removeRequest, 3L);

            // Then
            verify(taskAssigneeRepository).existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L);
            verify(taskAssigneeRepository).deleteById(any(TaskAssigneeCK.class));
        }

        @Test
        @DisplayName("Should delete with correct composite key")
        void removeCollaborator_ValidRequest_DeletesWithCorrectKey() {
            // Given
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(true);

            ArgumentCaptor<TaskAssigneeCK> keyCaptor = ArgumentCaptor.forClass(TaskAssigneeCK.class);

            // When
            collaboratorService.removeCollaborator(removeRequest, 3L);

            // Then
            verify(taskAssigneeRepository).deleteById(keyCaptor.capture());
            TaskAssigneeCK deletedKey = keyCaptor.getValue();

            assertThat(deletedKey.getTaskId()).isEqualTo(1L);
            assertThat(deletedKey.getUserId()).isEqualTo(2L);
            assertThat(deletedKey.getAssignedId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should throw CollaboratorAssignmentNotFoundException when assignment doesn't exist")
        void removeCollaborator_AssignmentNotFound_ThrowsException() {
            // Given
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> collaboratorService.removeCollaborator(removeRequest, 3L))
                    .isInstanceOf(CollaboratorAssignmentNotFoundException.class)
                    .hasMessage("Collaborator 2 not assigned to task 1");

            // Verify delete is never called
            verify(taskAssigneeRepository, never()).deleteById(any(TaskAssigneeCK.class));
        }

        @Test
        @DisplayName("Should handle different task and collaborator IDs correctly")
        void removeCollaborator_DifferentIds_HandlesCorrectly() {
            // Given
            RemoveCollaboratorRequestDto differentRequest = new RemoveCollaboratorRequestDto(500L, 600L);
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(500L, 600L, 700L))
                    .thenReturn(true);

            ArgumentCaptor<TaskAssigneeCK> keyCaptor = ArgumentCaptor.forClass(TaskAssigneeCK.class);

            // When
            collaboratorService.removeCollaborator(differentRequest, 700L);

            // Then
            verify(taskAssigneeRepository).existsByTaskIdAndUserIdAndAssignedId(500L, 600L, 700L);
            verify(taskAssigneeRepository).deleteById(keyCaptor.capture());

            TaskAssigneeCK deletedKey = keyCaptor.getValue();
            assertThat(deletedKey.getTaskId()).isEqualTo(500L);
            assertThat(deletedKey.getUserId()).isEqualTo(600L);
            assertThat(deletedKey.getAssignedId()).isEqualTo(700L);
        }
    }

    @Nested
    @DisplayName("Is User Task Collaborator Tests")
    class IsUserTaskCollaboratorTests {

        @Test
        @DisplayName("Should return true when user is a collaborator for the task")
        void isUserTaskCollaborator_UserIsCollaborator_ReturnsTrue() {
            // Given
            Long taskId = 1L;
            Long userId = 2L;
            when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, userId))
                    .thenReturn(true);

            // When
            boolean result = collaboratorService.isUserTaskCollaborator(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, userId);
        }

        @Test
        @DisplayName("Should return false when user is not a collaborator for the task")
        void isUserTaskCollaborator_UserIsNotCollaborator_ReturnsFalse() {
            // Given
            Long taskId = 1L;
            Long userId = 2L;
            when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, userId))
                    .thenReturn(false);

            // When
            boolean result = collaboratorService.isUserTaskCollaborator(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, userId);
        }

        @Test
        @DisplayName("Should handle different task and user ID combinations")
        void isUserTaskCollaborator_DifferentIds_HandlesCorrectly() {
            // Given
            Long taskId = 100L;
            Long userId = 200L;
            when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, userId))
                    .thenReturn(true);

            // When
            boolean result = collaboratorService.isUserTaskCollaborator(taskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, userId);
        }

        @Test
        @DisplayName("Should handle null values gracefully")
        void isUserTaskCollaborator_NullValues_DelegatesToRepository() {
            // Given
            Long taskId = null;
            Long userId = null;
            when(taskAssigneeRepository.existsByTaskIdAndUserId(taskId, userId))
                    .thenReturn(false);

            // When
            boolean result = collaboratorService.isUserTaskCollaborator(taskId, userId);

            // Then
            assertThat(result).isFalse();
            verify(taskAssigneeRepository).existsByTaskIdAndUserId(taskId, userId);
        }
    }

    @Nested
    @DisplayName("Get Tasks For Which User Is Collaborator Tests")
    class GetTasksForWhichUserIsCollaboratorTests {

        @Test
        @DisplayName("Should return list of task IDs when user is collaborator on multiple tasks")
        void getTasksForWhichUserIsCollaborator_UserHasMultipleTasks_ReturnsTaskIds() {
            // Given
            Long userId = 1L;
            List<Long> expectedTaskIds = Arrays.asList(10L, 20L, 30L);
            when(taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(userId))
                    .thenReturn(expectedTaskIds);

            // When
            List<Long> result = collaboratorService.getTasksForWhichUserIsCollaborator(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result).containsExactlyElementsOf(expectedTaskIds);
            verify(taskAssigneeRepository).findTaskIdsUserIsAssigneeFor(userId);
        }

        @Test
        @DisplayName("Should return empty list when user is not collaborator on any tasks")
        void getTasksForWhichUserIsCollaborator_UserHasNoTasks_ReturnsEmptyList() {
            // Given
            Long userId = 1L;
            List<Long> emptyList = Collections.emptyList();
            when(taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(userId))
                    .thenReturn(emptyList);

            // When
            List<Long> result = collaboratorService.getTasksForWhichUserIsCollaborator(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(taskAssigneeRepository).findTaskIdsUserIsAssigneeFor(userId);
        }

        @Test
        @DisplayName("Should return single task ID when user is collaborator on one task")
        void getTasksForWhichUserIsCollaborator_UserHasSingleTask_ReturnsSingleTaskId() {
            // Given
            Long userId = 2L;
            List<Long> singleTaskList = Arrays.asList(42L);
            when(taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(userId))
                    .thenReturn(singleTaskList);

            // When
            List<Long> result = collaboratorService.getTasksForWhichUserIsCollaborator(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result).contains(42L);
            verify(taskAssigneeRepository).findTaskIdsUserIsAssigneeFor(userId);
        }

        @Test
        @DisplayName("Should handle different user IDs correctly")
        void getTasksForWhichUserIsCollaborator_DifferentUserId_HandlesCorrectly() {
            // Given
            Long userId = 999L;
            List<Long> expectedTaskIds = Arrays.asList(100L, 200L);
            when(taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(userId))
                    .thenReturn(expectedTaskIds);

            // When
            List<Long> result = collaboratorService.getTasksForWhichUserIsCollaborator(userId);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(100L, 200L);
            verify(taskAssigneeRepository).findTaskIdsUserIsAssigneeFor(userId);
        }

        @Test
        @DisplayName("Should handle null user ID gracefully")
        void getTasksForWhichUserIsCollaborator_NullUserId_DelegatesToRepository() {
            // Given
            Long userId = null;
            List<Long> emptyList = Collections.emptyList();
            when(taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(userId))
                    .thenReturn(emptyList);

            // When
            List<Long> result = collaboratorService.getTasksForWhichUserIsCollaborator(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(taskAssigneeRepository).findTaskIdsUserIsAssigneeFor(userId);
        }

        @Test
        @DisplayName("Should preserve order of task IDs returned by repository")
        void getTasksForWhichUserIsCollaborator_PreservesOrder_ReturnsInSameOrder() {
            // Given
            Long userId = 5L;
            List<Long> orderedTaskIds = Arrays.asList(3L, 1L, 4L, 2L);
            when(taskAssigneeRepository.findTaskIdsUserIsAssigneeFor(userId))
                    .thenReturn(orderedTaskIds);

            // When
            List<Long> result = collaboratorService.getTasksForWhichUserIsCollaborator(userId);

            // Then
            assertThat(result).containsExactly(3L, 1L, 4L, 2L);
            verify(taskAssigneeRepository).findTaskIdsUserIsAssigneeFor(userId);
        }
    }
}