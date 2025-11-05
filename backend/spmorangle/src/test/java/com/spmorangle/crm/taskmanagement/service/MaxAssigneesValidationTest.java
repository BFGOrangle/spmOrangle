package com.spmorangle.crm.taskmanagement.service;

import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.notification.messaging.publisher.NotificationMessagePublisher;
import com.spmorangle.crm.reporting.service.ReportService;
import com.spmorangle.crm.taskmanagement.dto.AddCollaboratorRequestDto;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import com.spmorangle.crm.taskmanagement.service.exception.MaxAssigneesExceededException;
import com.spmorangle.crm.taskmanagement.service.impl.CollaboratorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Max Assignees Validation Tests")
class MaxAssigneesValidationTest {

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ReportService reportService;

    @Mock
    private NotificationMessagePublisher notificationPublisher;

    @Mock
    private com.spmorangle.crm.projectmanagement.service.ProjectService projectService;

    @InjectMocks
    private CollaboratorServiceImpl collaboratorService;

    private Task testTask;
    private Long taskId;
    private Long collaboratorId;
    private Long assignedById;

    @BeforeEach
    void setUp() {
        taskId = 1L;
        collaboratorId = 100L;
        assignedById = 200L;

        testTask = new Task();
        testTask.setId(taskId);
        testTask.setProjectId(10L);
    }

    @Test
    @DisplayName("Should throw MaxAssigneesExceededException when task already has 5 assignees")
    void shouldRejectAddingCollaboratorWhenMaxLimitReached() {
        // Given: Task exists and user permissions are valid
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(projectService.isUserProjectMember(assignedById, testTask.getProjectId())).thenReturn(true);
        when(userRepository.existsById(collaboratorId)).thenReturn(true);
        when(userRepository.existsById(assignedById)).thenReturn(true);
        when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(taskId, collaboratorId, assignedById))
                .thenReturn(false);

        // When: Task already has 5 assignees
        when(taskAssigneeRepository.countByTaskId(taskId)).thenReturn(5);

        AddCollaboratorRequestDto request = AddCollaboratorRequestDto.builder()
                .taskId(taskId)
                .collaboratorId(collaboratorId)
                .build();

        // Then: Should throw MaxAssigneesExceededException
        assertThatThrownBy(() -> collaboratorService.addCollaborator(request, assignedById))
                .isInstanceOf(MaxAssigneesExceededException.class)
                .hasMessageContaining("Maximum allowed is 5 assignees per task");
    }

    @Test
    @DisplayName("Should allow adding collaborator when task has less than 5 assignees")
    void shouldAllowAddingCollaboratorWhenBelowMaxLimit() {
        // Given: Task exists and user permissions are valid
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(projectService.isUserProjectMember(assignedById, testTask.getProjectId())).thenReturn(true);
        when(userRepository.existsById(collaboratorId)).thenReturn(true);
        when(userRepository.existsById(assignedById)).thenReturn(true);
        when(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(taskId, collaboratorId, assignedById))
                .thenReturn(false);

        // When: Task has 4 assignees (under limit)
        when(taskAssigneeRepository.countByTaskId(taskId)).thenReturn(4);

        TaskAssignee savedAssignee = new TaskAssignee();
        savedAssignee.setTaskId(taskId);
        savedAssignee.setUserId(collaboratorId);
        savedAssignee.setAssignedId(assignedById);
        when(taskAssigneeRepository.save(any(TaskAssignee.class))).thenReturn(savedAssignee);

        AddCollaboratorRequestDto request = AddCollaboratorRequestDto.builder()
                .taskId(taskId)
                .collaboratorId(collaboratorId)
                .build();

        // Then: Should successfully add collaborator
        var result = collaboratorService.addCollaborator(request, assignedById);

        assertThat(result).isNotNull();
        assertThat(result.getTaskId()).isEqualTo(taskId);
        assertThat(result.getCollaboratorId()).isEqualTo(collaboratorId);
    }

    @Test
    @DisplayName("Should verify MaxAssigneesExceededException has correct max value")
    void shouldVerifyMaxAssigneesConstant() {
        // Then: Max assignees should be 5
        assertThat(MaxAssigneesExceededException.getMaxAssignees()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw MaxAssigneesExceededException with task ID and current count")
    void shouldThrowExceptionWithTaskIdAndCount() {
        // Given
        Long taskId = 123L;
        int currentCount = 5;

        // When/Then: Exception should contain task ID and current count
        assertThatThrownBy(() -> {
            throw new MaxAssigneesExceededException(taskId, currentCount);
        })
                .isInstanceOf(MaxAssigneesExceededException.class)
                .hasMessageContaining("Task 123")
                .hasMessageContaining("already has 5 assignees")
                .hasMessageContaining("Maximum allowed is 5");
    }

    @Test
    @DisplayName("Should throw MaxAssigneesExceededException with requested count")
    void shouldThrowExceptionWithRequestedCount() {
        // Given
        int requestedCount = 6;

        // When/Then: Exception should contain requested count
        assertThatThrownBy(() -> {
            throw new MaxAssigneesExceededException(requestedCount);
        })
                .isInstanceOf(MaxAssigneesExceededException.class)
                .hasMessageContaining("Cannot assign 6 users")
                .hasMessageContaining("Maximum allowed is 5");
    }
}
