package com.spmorangle.crm.taskmanagement.service.impl;

import com.spmorangle.crm.projectmanagement.service.ProjectService;
import com.spmorangle.crm.taskmanagement.dto.CreateSubtaskDto;
import com.spmorangle.crm.taskmanagement.dto.SubtaskResponseDto;
import com.spmorangle.crm.taskmanagement.dto.UpdateSubtaskDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Subtask;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.SubtaskRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.CollaboratorService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubtaskServiceImpl Tests")
class SubtaskServiceImplTest {

    @Mock
    private SubtaskRepository subtaskRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private CollaboratorService collaboratorService;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SubtaskServiceImpl subtaskService;

    private Subtask testSubtask1;
    private Subtask testSubtask2;
    private Subtask testSubtask3;
    private Task testTask1;
    private Task testTask2;
    private OffsetDateTime fixedDateTime;

    @BeforeEach
    void setUp() {
        fixedDateTime = OffsetDateTime.now();

        testSubtask1 = createTestSubtask(1L, 100L, 200L, "Subtask 1", "Details 1", 
                                        Status.TODO, TaskType.FEATURE, 301L);
        
        testSubtask2 = createTestSubtask(2L, 100L, 200L, "Subtask 2", "Details 2", 
                                        Status.IN_PROGRESS, TaskType.BUG, 301L);
        
        testSubtask3 = createTestSubtask(3L, 101L, 201L, "Subtask 3", null, 
                                        Status.COMPLETED, TaskType.CHORE, 302L);
        
        testTask1 = createTestTask(100L, 200L, 301L);
        testTask2 = createTestTask(101L, 201L, 302L);
    }

    private Subtask createTestSubtask(Long id, Long taskId, Long projectId, String title,
                                     String details, Status status, TaskType taskType, Long createdBy) {
        Subtask subtask = new Subtask();
        subtask.setId(id);
        subtask.setTaskId(taskId);
        subtask.setProjectId(projectId);
        subtask.setTitle(title);
        subtask.setDetails(details);
        subtask.setStatus(status);
        subtask.setTaskType(taskType);
        subtask.setDeleteInd(false);
        subtask.setCreatedBy(createdBy);
        subtask.setCreatedAt(fixedDateTime);
        return subtask;
    }

    private Task createTestTask(Long taskId, Long projectId, Long ownerId) {
        Task task = new Task();
        task.setId(taskId);
        task.setProjectId(projectId);
        task.setOwnerId(ownerId);
        task.setTitle("Test Task " + taskId);
        task.setDescription("Test Description");
        task.setStatus(Status.TODO);
        task.setTaskType(TaskType.FEATURE);
        task.setCreatedBy(ownerId);
        task.setCreatedAt(fixedDateTime);
        return task;
    }

    @Nested
    @DisplayName("Create Subtask Tests")
    class CreateSubtaskTests {

        @Test
        @DisplayName("Should successfully create subtask with all required fields")
        void createSubtask_ValidData_ReturnsSubtaskResponseDto() {
            // Given
            CreateSubtaskDto createDto = CreateSubtaskDto.builder()
                    .taskId(100L)
                    .projectId(200L)
                    .title("New Subtask")
                    .details("New Details")
                    .status(Status.TODO)
                    .taskType(TaskType.FEATURE)
                    .build();

            Subtask savedSubtask = createTestSubtask(10L, 100L, 200L, "New Subtask", 
                                                    "New Details", Status.TODO, TaskType.FEATURE, 301L);
            
            when(subtaskRepository.save(any(Subtask.class))).thenReturn(savedSubtask);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(subtaskRepository.findByIdAndNotDeleted(10L)).thenReturn(savedSubtask);
            when(projectService.getOwnerId(200L)).thenReturn(301L);

            // When
            SubtaskResponseDto result = subtaskService.createSubtask(createDto, 301L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getTitle()).isEqualTo("New Subtask");
            assertThat(result.getDetails()).isEqualTo("New Details");
            assertThat(result.getTaskId()).isEqualTo(100L);
            assertThat(result.getProjectId()).isEqualTo(200L);
            assertThat(result.getStatus()).isEqualTo(Status.TODO);
            assertThat(result.getTaskType()).isEqualTo(TaskType.FEATURE);
            assertThat(result.getCreatedBy()).isEqualTo(301L);

            // Verify repository interaction
            ArgumentCaptor<Subtask> subtaskCaptor = ArgumentCaptor.forClass(Subtask.class);
            verify(subtaskRepository).save(subtaskCaptor.capture());
            
            Subtask capturedSubtask = subtaskCaptor.getValue();
            assertThat(capturedSubtask.getTitle()).isEqualTo("New Subtask");
            assertThat(capturedSubtask.getCreatedBy()).isEqualTo(301L);
        }
    }

    @Nested
    @DisplayName("Get Subtask Tests")
    class GetSubtaskTests {

        @Test
        @DisplayName("Should return subtask by ID when subtask exists")
        void getSubtaskById_ValidId_ReturnsSubtask() {
            // Given
            when(subtaskRepository.findByIdAndNotDeleted(1L)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(collaboratorService.isUserTaskCollaborator(100L, 1L)).thenReturn(true);

            // When
            SubtaskResponseDto result = subtaskService.getSubtaskById(1L, 1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getTitle()).isEqualTo("Subtask 1");
            verify(subtaskRepository, times(3)).findByIdAndNotDeleted(1L);
        }

        @Test
        @DisplayName("Should throw exception when subtask not found")
        void getSubtaskById_InvalidId_ThrowsException() {
            // Given
            when(subtaskRepository.findByIdAndNotDeleted(999L)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> subtaskService.getSubtaskById(999L, 1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Subtask not found with ID: 999");
            
            verify(subtaskRepository).findByIdAndNotDeleted(999L);
        }

        @Test
        @DisplayName("Should return subtasks by task ID")
        void getSubtasksByTaskId_ValidTaskId_ReturnsSubtaskList() {
            // Given
            List<Subtask> subtasks = Arrays.asList(testSubtask1, testSubtask2);
            when(subtaskRepository.findByTaskIdAndNotDeleted(100L)).thenReturn(subtasks);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(collaboratorService.isUserTaskCollaborator(100L, 1L)).thenReturn(true);

            when(subtaskRepository.findByIdAndNotDeleted(1L)).thenReturn(testSubtask1);
            when(subtaskRepository.findByIdAndNotDeleted(2L)).thenReturn(testSubtask2);
            when(projectService.getOwnerId(200L)).thenReturn(1L);

            // When
            List<SubtaskResponseDto> result = subtaskService.getSubtasksByTaskId(100L, 1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(1L);
            assertThat(result.get(1).getId()).isEqualTo(2L);
            verify(subtaskRepository).findByTaskIdAndNotDeleted(100L);
        }

        @Test
        @DisplayName("Should return empty list when task has no subtasks")
        void getSubtasksByTaskId_NoSubtasks_ReturnsEmptyList() {
            // Given
            when(subtaskRepository.findByTaskIdAndNotDeleted(999L)).thenReturn(Collections.emptyList());

            // When
            List<SubtaskResponseDto> result = subtaskService.getSubtasksByTaskId(999L, 1L);

            // Then
            assertThat(result).isEmpty();
            verify(subtaskRepository).findByTaskIdAndNotDeleted(999L);
        }

        @Test
        @DisplayName("Should return subtasks by project ID")
        void getSubtasksByProjectId_ValidProjectId_ReturnsSubtaskList() {
            // Given
            List<Subtask> subtasks = Arrays.asList(testSubtask1, testSubtask2);
            when(subtaskRepository.findByProjectIdAndNotDeleted(200L)).thenReturn(subtasks);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(collaboratorService.isUserTaskCollaborator(100L, 1L)).thenReturn(true);
            when(subtaskRepository.findByIdAndNotDeleted(1L)).thenReturn(testSubtask1);
            when(subtaskRepository.findByIdAndNotDeleted(2L)).thenReturn(testSubtask2);
            when(projectService.getOwnerId(200L)).thenReturn(1L);
            // When
            List<SubtaskResponseDto> result = subtaskService.getSubtasksByProjectId(200L, 1L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getProjectId()).isEqualTo(200L);
            assertThat(result.get(1).getProjectId()).isEqualTo(200L);
            verify(subtaskRepository).findByProjectIdAndNotDeleted(200L);
        }
    }

    @Nested
    @DisplayName("Update Subtask Tests")
    class UpdateSubtaskTests {

        @Test
        @DisplayName("Should successfully update subtask when user is authorized")
        void updateSubtask_AuthorizedUser_UpdatesSuccessfully() {
            // Given
            UpdateSubtaskDto updateDto = UpdateSubtaskDto.builder()
                    .title("Updated Title")
                    .details("Updated Details")
                    .status(Status.COMPLETED)
                    .taskType(TaskType.BUG)
                    .build();

            when(subtaskRepository.findByIdAndNotDeleted(1L)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(subtaskRepository.save(any(Subtask.class))).thenReturn(testSubtask1);

            // When
            SubtaskResponseDto result = subtaskService.updateSubtask(1L, updateDto, 301L);

            // Then
            assertThat(result).isNotNull();
            
            ArgumentCaptor<Subtask> subtaskCaptor = ArgumentCaptor.forClass(Subtask.class);
            verify(subtaskRepository).save(subtaskCaptor.capture());
            
            Subtask updatedSubtask = subtaskCaptor.getValue();
            assertThat(updatedSubtask.getTitle()).isEqualTo("Updated Title");
            assertThat(updatedSubtask.getDetails()).isEqualTo("Updated Details");
            assertThat(updatedSubtask.getStatus()).isEqualTo(Status.COMPLETED);
            assertThat(updatedSubtask.getTaskType()).isEqualTo(TaskType.BUG);
            assertThat(updatedSubtask.getUpdatedBy()).isEqualTo(301L);
        }

        @Test
        @DisplayName("Should throw exception when user is not authorized to update")
        void updateSubtask_UnauthorizedUser_ThrowsException() {
            // Given
            UpdateSubtaskDto updateDto = UpdateSubtaskDto.builder()
                    .title("Updated Title")
                    .build();

            when(subtaskRepository.findByIdAndNotDeleted(1L)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(collaboratorService.isUserTaskCollaborator(100L, 999L)).thenReturn(false);

            // When & Then
            assertThatThrownBy(() -> subtaskService.updateSubtask(1L, updateDto, 999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Only project owner or collaborators can update the subtask");
            
            verify(subtaskRepository, never()).save(any(Subtask.class));
        }

        @Test
        @DisplayName("Should update only provided fields")
        void updateSubtask_PartialUpdate_UpdatesOnlyProvidedFields() {
            // Given
            UpdateSubtaskDto updateDto = UpdateSubtaskDto.builder()
                    .status(Status.IN_PROGRESS)
                    .build();

            String originalTitle = testSubtask1.getTitle();
            String originalDetails = testSubtask1.getDetails();

            when(subtaskRepository.findByIdAndNotDeleted(1L)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(subtaskRepository.save(any(Subtask.class))).thenReturn(testSubtask1);

            // When
            subtaskService.updateSubtask(1L, updateDto, 301L);

            // Then
            ArgumentCaptor<Subtask> subtaskCaptor = ArgumentCaptor.forClass(Subtask.class);
            verify(subtaskRepository).save(subtaskCaptor.capture());
            
            Subtask updatedSubtask = subtaskCaptor.getValue();
            assertThat(updatedSubtask.getStatus()).isEqualTo(Status.IN_PROGRESS);
            assertThat(updatedSubtask.getTitle()).isEqualTo(originalTitle);
            assertThat(updatedSubtask.getDetails()).isEqualTo(originalDetails);
        }

        @Test
        @DisplayName("Should throw exception when subtask not found")
        void updateSubtask_SubtaskNotFound_ThrowsException() {
            // Given
            UpdateSubtaskDto updateDto = UpdateSubtaskDto.builder()
                    .title("Updated Title")
                    .build();

            when(subtaskRepository.findByIdAndNotDeleted(999L)).thenReturn(null);

            // When & Then
            // Since canUserUpdateSubtask returns false when subtask is not found,
            // the permission check fails first with the permission error message
            assertThatThrownBy(() -> subtaskService.updateSubtask(999L, updateDto, 301L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Only project owner or collaborators can update the subtask");
        }
    }

    @Nested
    @DisplayName("Delete Subtask Tests")
    class DeleteSubtaskTests {

        @Test
        @DisplayName("Should successfully delete subtask when user is project owner")
        void deleteSubtask_UserIsProjectOwner_DeletesSuccessfully() {
            // Given
            Long subtaskId = 1L;
            Long projectOwnerId = 301L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When
            subtaskService.deleteSubtask(subtaskId, projectOwnerId);

            // Then
            ArgumentCaptor<Subtask> subtaskCaptor = ArgumentCaptor.forClass(Subtask.class);
            verify(subtaskRepository).save(subtaskCaptor.capture());
            
            Subtask deletedSubtask = subtaskCaptor.getValue();
            assertThat(deletedSubtask.isDeleteInd()).isTrue();
            assertThat(deletedSubtask.getUpdatedBy()).isEqualTo(projectOwnerId);
            assertThat(deletedSubtask.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception when user is not project owner")
        void deleteSubtask_UserIsNotProjectOwner_ThrowsException() {
            // Given
            Long subtaskId = 1L;
            Long unauthorizedUserId = 999L;
            Long projectOwnerId = 301L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When & Then
            assertThatThrownBy(() -> subtaskService.deleteSubtask(subtaskId, unauthorizedUserId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Only project owner can delete the subtask");
            
            verify(subtaskRepository, never()).save(any(Subtask.class));
        }

        @Test
        @DisplayName("Should throw exception when subtask not found")
        void deleteSubtask_SubtaskNotFound_ThrowsException() {
            // Given
            Long nonExistentSubtaskId = 999L;
            Long userId = 301L;

            when(subtaskRepository.findByIdAndNotDeleted(nonExistentSubtaskId)).thenReturn(null);

            // When & Then
            // Since canUserDeleteSubtask returns false when subtask is not found,
            // the permission check fails first with the permission error message
            assertThatThrownBy(() -> subtaskService.deleteSubtask(nonExistentSubtaskId, userId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Only project owner can delete the subtask");

            verify(subtaskRepository, never()).save(any(Subtask.class));
        }

        @Test
        @DisplayName("Should perform soft delete, not hard delete")
        void deleteSubtask_SoftDelete_DoesNotCallRepositoryDelete() {
            // Given
            Long subtaskId = 1L;
            Long projectOwnerId = 301L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When
            subtaskService.deleteSubtask(subtaskId, projectOwnerId);

            // Then
            verify(subtaskRepository).save(any(Subtask.class));
            verify(subtaskRepository, never()).delete(any(Subtask.class));
            verify(subtaskRepository, never()).deleteById(anyLong());
        }

        @Test
        @DisplayName("Should allow creator to delete subtask when no project ID")
        void deleteSubtask_NoProjectId_CreatorCanDelete() {
            // Given
            Long subtaskId = 3L;
            Long creatorId = 302L;
            testSubtask3.setProjectId(null);
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask3);

            // When
            subtaskService.deleteSubtask(subtaskId, creatorId);

            // Then
            ArgumentCaptor<Subtask> subtaskCaptor = ArgumentCaptor.forClass(Subtask.class);
            verify(subtaskRepository).save(subtaskCaptor.capture());
            
            Subtask deletedSubtask = subtaskCaptor.getValue();
            assertThat(deletedSubtask.isDeleteInd()).isTrue();
            assertThat(deletedSubtask.getUpdatedBy()).isEqualTo(creatorId);
        }

        @Test
        @DisplayName("Should throw exception when non-creator tries to delete subtask with no project")
        void deleteSubtask_NoProjectId_NonCreatorCannotDelete() {
            // Given
            Long subtaskId = 3L;
            Long nonCreatorId = 999L;
            testSubtask3.setProjectId(null);
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask3);

            // When & Then
            assertThatThrownBy(() -> subtaskService.deleteSubtask(subtaskId, nonCreatorId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Only project owner can delete the subtask");
            
            verify(subtaskRepository, never()).save(any(Subtask.class));
        }

        @Test
        @DisplayName("Should update timestamp when deleting subtask")
        void deleteSubtask_UpdatesTimestamp_CorrectlySetUpdatedAt() {
            // Given
            Long subtaskId = 1L;
            Long projectOwnerId = 301L;
            OffsetDateTime beforeDelete = OffsetDateTime.now();
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When
            subtaskService.deleteSubtask(subtaskId, projectOwnerId);

            // Then
            ArgumentCaptor<Subtask> subtaskCaptor = ArgumentCaptor.forClass(Subtask.class);
            verify(subtaskRepository).save(subtaskCaptor.capture());
            
            Subtask deletedSubtask = subtaskCaptor.getValue();
            assertThat(deletedSubtask.getUpdatedAt()).isNotNull();
            assertThat(deletedSubtask.getUpdatedAt()).isAfterOrEqualTo(beforeDelete);
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should return true when user can update subtask as collaborator")
        void canUserUpdateSubtask_UserIsCollaborator_ReturnsTrue() {
            // Given
            Long subtaskId = 1L;
            Long userId = 999L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(collaboratorService.isUserTaskCollaborator(100L, userId)).thenReturn(true);

            // When
            boolean result = subtaskService.canUserUpdateSubtask(subtaskId, userId);

            // Then
            assertThat(result).isTrue();
            verify(collaboratorService).isUserTaskCollaborator(100L, userId);
        }

        @Test
        @DisplayName("Should return false when user cannot update subtask")
        void canUserUpdateSubtask_UserIsNotCollaborator_ReturnsFalse() {
            // Given
            Long subtaskId = 1L;
            Long userId = 999L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(collaboratorService.isUserTaskCollaborator(100L, userId)).thenReturn(false);

            // When
            boolean result = subtaskService.canUserUpdateSubtask(subtaskId, userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return true when user is creator and no project ID")
        void canUserUpdateSubtask_UserIsCreatorNoProject_ReturnsTrue() {
            // Given
            Long subtaskId = 3L;
            Long creatorId = 302L;
            testSubtask3.setProjectId(null);
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask3);

            // When
            boolean result = subtaskService.canUserUpdateSubtask(subtaskId, creatorId);

            // Then
            assertThat(result).isTrue();
            verify(collaboratorService, never()).isUserTaskCollaborator(anyLong(), anyLong());
        }

        @Test
        @DisplayName("Should return true when user can delete subtask as project owner")
        void canUserDeleteSubtask_UserIsProjectOwner_ReturnsTrue() {
            // Given
            Long subtaskId = 1L;
            Long projectOwnerId = 301L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When
            boolean result = subtaskService.canUserDeleteSubtask(subtaskId, projectOwnerId);

            // Then
            assertThat(result).isTrue();
            verify(projectService).getOwnerId(200L);
        }

        @Test
        @DisplayName("Should return false when user is not project owner")
        void canUserDeleteSubtask_UserIsNotProjectOwner_ReturnsFalse() {
            // Given
            Long subtaskId = 1L;
            Long userId = 999L;
            Long projectOwnerId = 301L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When
            boolean result = subtaskService.canUserDeleteSubtask(subtaskId, userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when subtask not found for update permission check")
        void canUserUpdateSubtask_SubtaskNotFound_ReturnsFalse() {
            // Given
            Long nonExistentSubtaskId = 999L;
            Long userId = 301L;

            when(subtaskRepository.findByIdAndNotDeleted(nonExistentSubtaskId)).thenReturn(null);

            // When
            boolean result = subtaskService.canUserUpdateSubtask(nonExistentSubtaskId, userId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when subtask not found for delete permission check")
        void canUserDeleteSubtask_SubtaskNotFound_ReturnsFalse() {
            // Given
            Long nonExistentSubtaskId = 999L;
            Long userId = 301L;

            when(subtaskRepository.findByIdAndNotDeleted(nonExistentSubtaskId)).thenReturn(null);

            // When
            boolean result = subtaskService.canUserDeleteSubtask(nonExistentSubtaskId, userId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("Permission Fields in Response DTO Tests")
    class PermissionFieldsTests {

        @Test
        @DisplayName("Should include userHasEditAccess and userHasDeleteAccess in response when user is project owner")
        void getSubtaskById_ProjectOwner_IncludesPermissionFields() {
            // Given
            Long subtaskId = 1L;
            Long projectOwnerId = 301L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When
            SubtaskResponseDto result = subtaskService.getSubtaskById(subtaskId, projectOwnerId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isUserHasEditAccess()).isTrue();
            assertThat(result.isUserHasDeleteAccess()).isTrue();
        }

        @Test
        @DisplayName("Should set userHasEditAccess true and userHasDeleteAccess false when user is collaborator but not owner")
        void getSubtaskById_Collaborator_HasEditButNotDelete() {
            // Given
            Long subtaskId = 1L;
            Long collaboratorId = 999L;
            Long projectOwnerId = 301L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(collaboratorService.isUserTaskCollaborator(100L, collaboratorId)).thenReturn(true);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When
            SubtaskResponseDto result = subtaskService.getSubtaskById(subtaskId, collaboratorId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isUserHasEditAccess()).isTrue();
            assertThat(result.isUserHasDeleteAccess()).isFalse();
        }

        @Test
        @DisplayName("Should set both permissions to false when user is neither owner nor collaborator")
        void getSubtaskById_RegularUser_HasNoPermissions() {
            // Given
            Long subtaskId = 1L;
            Long regularUserId = 999L;
            Long projectOwnerId = 301L;
            
            when(subtaskRepository.findByIdAndNotDeleted(subtaskId)).thenReturn(testSubtask1);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(collaboratorService.isUserTaskCollaborator(100L, regularUserId)).thenReturn(false);
            when(projectService.getOwnerId(200L)).thenReturn(projectOwnerId);

            // When
            SubtaskResponseDto result = subtaskService.getSubtaskById(subtaskId, regularUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isUserHasEditAccess()).isFalse();
            assertThat(result.isUserHasDeleteAccess()).isFalse();
        }

        @Test
        @DisplayName("Should include permission fields in list responses")
        void getSubtasksByTaskId_IncludesPermissionFieldsForEachSubtask() {
            // Given
            Long taskId = 100L;
            Long userId = 301L;
            List<Subtask> subtasks = Arrays.asList(testSubtask1, testSubtask2);
            
            when(subtaskRepository.findByTaskIdAndNotDeleted(taskId)).thenReturn(subtasks);
            when(taskRepository.findByIdAndNotDeleted(100L)).thenReturn(testTask1);
            when(projectService.getOwnerId(200L)).thenReturn(userId);
            when(subtaskRepository.findByIdAndNotDeleted(1L)).thenReturn(testSubtask1);
            when(subtaskRepository.findByIdAndNotDeleted(2L)).thenReturn(testSubtask2);

            // When
            List<SubtaskResponseDto> result = subtaskService.getSubtasksByTaskId(taskId, userId);

            // Then
            assertThat(result).hasSize(2);
            result.forEach(subtask -> {
                assertThat(subtask.isUserHasEditAccess()).isNotNull();
                assertThat(subtask.isUserHasDeleteAccess()).isNotNull();
            });
        }
    }
}

