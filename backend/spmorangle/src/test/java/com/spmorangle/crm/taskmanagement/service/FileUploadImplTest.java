package com.spmorangle.crm.taskmanagement.service;

import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorResponseDto;
import com.spmorangle.crm.taskmanagement.dto.RemoveCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.model.TaskAssigneeCK;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CollaboratorService Tests")
class FileUploadImplTest {

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @InjectMocks
    private CollaboratorServiceImpl collaboratorService;

    private AddCollaboratorRequestDto addRequest;
    private RemoveCollaboratorRequestDto removeRequest;
    private TaskAssignee taskAssignee;

    @BeforeEach
    void setUp() {
        addRequest = new AddCollaboratorRequestDto(1L, 2L, 3L);
        removeRequest = new RemoveCollaboratorRequestDto(1L, 2L, 3L);

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
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(false);
            when(taskAssigneeRepository.save(any(TaskAssignee.class)))
                    .thenReturn(taskAssignee);

            // When
            AddCollaboratorResponseDto result = collaboratorService.addCollaborator(addRequest);

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
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(false);
            when(taskAssigneeRepository.save(any(TaskAssignee.class)))
                    .thenReturn(taskAssignee);

            ArgumentCaptor<TaskAssignee> taskAssigneeCaptor = ArgumentCaptor.forClass(TaskAssignee.class);

            // When
            collaboratorService.addCollaborator(addRequest);

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
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L))
                    .thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> collaboratorService.addCollaborator(addRequest))
                    .isInstanceOf(CollaboratorAlreadyExistsException.class)
                    .hasMessage("Collaborator 2 already assigned to task 1");

            // Verify save is never called
            verify(taskAssigneeRepository, never()).save(any(TaskAssignee.class));
        }

        @Test
        @DisplayName("Should handle different task and collaborator IDs correctly")
        void addCollaborator_DifferentIds_HandlesCorrectly() {
            // Given
            AddCollaboratorRequestDto differentRequest = new AddCollaboratorRequestDto(100L, 200L, 300L);
            TaskAssignee differentTaskAssignee = new TaskAssignee();
            differentTaskAssignee.setTaskId(100L);
            differentTaskAssignee.setUserId(200L);
            differentTaskAssignee.setAssignedId(300L);
            differentTaskAssignee.setAssignedAt(OffsetDateTime.now());

            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(100L, 200L, 300L))
                    .thenReturn(false);
            when(taskAssigneeRepository.save(any(TaskAssignee.class)))
                    .thenReturn(differentTaskAssignee);

            // When
            AddCollaboratorResponseDto result = collaboratorService.addCollaborator(differentRequest);

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
            collaboratorService.removeCollaborator(removeRequest);

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
            collaboratorService.removeCollaborator(removeRequest);

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
            assertThatThrownBy(() -> collaboratorService.removeCollaborator(removeRequest))
                    .isInstanceOf(CollaboratorAssignmentNotFoundException.class)
                    .hasMessage("Collaborator 2 not assigned to task 1");

            // Verify delete is never called
            verify(taskAssigneeRepository, never()).deleteById(any(TaskAssigneeCK.class));
        }

        @Test
        @DisplayName("Should handle different task and collaborator IDs correctly")
        void removeCollaborator_DifferentIds_HandlesCorrectly() {
            // Given
            RemoveCollaboratorRequestDto differentRequest = new RemoveCollaboratorRequestDto(500L, 600L, 700L);
            when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(500L, 600L, 700L))
                    .thenReturn(true);

            ArgumentCaptor<TaskAssigneeCK> keyCaptor = ArgumentCaptor.forClass(TaskAssigneeCK.class);

            // When
            collaboratorService.removeCollaborator(differentRequest);

            // Then
            verify(taskAssigneeRepository).existsByTaskIdAndUserIdAndAssignedId(500L, 600L, 700L);
            verify(taskAssigneeRepository).deleteById(keyCaptor.capture());

            TaskAssigneeCK deletedKey = keyCaptor.getValue();
            assertThat(deletedKey.getTaskId()).isEqualTo(500L);
            assertThat(deletedKey.getUserId()).isEqualTo(600L);
            assertThat(deletedKey.getAssignedId()).isEqualTo(700L);
        }
    }
}