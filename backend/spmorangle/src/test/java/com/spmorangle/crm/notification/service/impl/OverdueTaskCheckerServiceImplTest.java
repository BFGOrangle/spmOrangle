package com.spmorangle.crm.notification.service.impl;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OverdueTaskCheckerService Tests")
class OverdueTaskCheckerServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskAssigneeRepository taskAssigneeRepository;

    @Mock
    private OverdueTaskEmailServiceImpl overdueTaskEmailService;

    @InjectMocks
    private OverdueTaskCheckerServiceImpl checkerService;

    private Task overdueTask;
    private TaskAssignee assignee1;
    private TaskAssignee assignee2;

    @BeforeEach
    void setUp() {
        // Setup overdue task
        overdueTask = new Task();
        overdueTask.setId(100L);
        overdueTask.setTitle("Overdue Task");
        overdueTask.setStatus(Status.TODO);
        overdueTask.setDueDateTime(OffsetDateTime.now(ZoneOffset.UTC).minusHours(48));
        overdueTask.setDeleteInd(false);

        // Setup assignees
        assignee1 = new TaskAssignee();
        assignee1.setTaskId(100L);
        assignee1.setUserId(1L);

        assignee2 = new TaskAssignee();
        assignee2.setTaskId(100L);
        assignee2.setUserId(2L);
    }

    @Test
    @DisplayName("Should not call email service when no overdue tasks found")
    void shouldNotCallEmailWhenNoOverdueTasks() {
        // Arrange
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class))).thenReturn(new ArrayList<>());

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verifyNoInteractions(taskAssigneeRepository);
        verifyNoInteractions(overdueTaskEmailService);
    }

    @Test
    @DisplayName("Should send email to all assignees for overdue task")
    void shouldSendEmailToAllAssignees() {
        // Arrange
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));
        when(taskAssigneeRepository.findByTaskId(100L))
                .thenReturn(List.of(assignee1, assignee2));

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verify(taskAssigneeRepository, times(1)).findByTaskId(100L);
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(overdueTask, assignee1);
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(overdueTask, assignee2);
    }

    @Test
    @DisplayName("Should skip task when it has no assignees")
    void shouldSkipTaskWithNoAssignees() {
        // Arrange
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));
        when(taskAssigneeRepository.findByTaskId(100L))
                .thenReturn(new ArrayList<>());

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verify(taskAssigneeRepository, times(1)).findByTaskId(100L);
        verifyNoInteractions(overdueTaskEmailService);
    }

    @Test
    @DisplayName("Should skip task with COMPLETED status")
    void shouldSkipCompletedTask() {
        // Arrange
        overdueTask.setStatus(Status.COMPLETED);
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verifyNoInteractions(taskAssigneeRepository);
        verifyNoInteractions(overdueTaskEmailService);
    }

    @Test
    @DisplayName("Should process task with TODO status")
    void shouldProcessTodoTask() {
        // Arrange
        overdueTask.setStatus(Status.TODO);
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));
        when(taskAssigneeRepository.findByTaskId(100L))
                .thenReturn(List.of(assignee1));

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verify(taskAssigneeRepository, times(1)).findByTaskId(100L);
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(overdueTask, assignee1);
    }

    @Test
    @DisplayName("Should process task with IN_PROGRESS status")
    void shouldProcessInProgressTask() {
        // Arrange
        overdueTask.setStatus(Status.IN_PROGRESS);
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));
        when(taskAssigneeRepository.findByTaskId(100L))
                .thenReturn(List.of(assignee1));

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verify(taskAssigneeRepository, times(1)).findByTaskId(100L);
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(overdueTask, assignee1);
    }

    @Test
    @DisplayName("Should process task with null status")
    void shouldProcessTaskWithNullStatus() {
        // Arrange
        overdueTask.setStatus(null);
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));
        when(taskAssigneeRepository.findByTaskId(100L))
                .thenReturn(List.of(assignee1));

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verify(taskAssigneeRepository, times(1)).findByTaskId(100L);
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(overdueTask, assignee1);
    }

    @Test
    @DisplayName("Should handle multiple overdue tasks correctly")
    void shouldHandleMultipleOverdueTasks() {
        // Arrange
        Task task1 = new Task();
        task1.setId(100L);
        task1.setStatus(Status.TODO);

        Task task2 = new Task();
        task2.setId(200L);
        task2.setStatus(Status.IN_PROGRESS);

        Task task3 = new Task();
        task3.setId(300L);
        task3.setStatus(Status.COMPLETED); // Should be skipped

        TaskAssignee assignee100 = new TaskAssignee();
        assignee100.setTaskId(100L);
        assignee100.setUserId(1L);

        TaskAssignee assignee200 = new TaskAssignee();
        assignee200.setTaskId(200L);
        assignee200.setUserId(2L);

        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(task1, task2, task3));
        when(taskAssigneeRepository.findByTaskId(100L)).thenReturn(List.of(assignee100));
        when(taskAssigneeRepository.findByTaskId(200L)).thenReturn(List.of(assignee200));

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verify(taskAssigneeRepository, times(1)).findByTaskId(100L);
        verify(taskAssigneeRepository, times(1)).findByTaskId(200L);
        verify(taskAssigneeRepository, never()).findByTaskId(300L); // COMPLETED task should be skipped
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(task1, assignee100);
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(task2, assignee200);
        verify(overdueTaskEmailService, times(2)).sendOverdueTaskEmail(any(Task.class), any(TaskAssignee.class));
    }

    @Test
    @DisplayName("Should continue processing other tasks when email sending fails for one assignee")
    void shouldContinueWhenEmailFails() {
        // Arrange
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));
        when(taskAssigneeRepository.findByTaskId(100L))
                .thenReturn(List.of(assignee1, assignee2));

        // First email fails, second should still be attempted
        doThrow(new RuntimeException("Email service error"))
                .when(overdueTaskEmailService).sendOverdueTaskEmail(overdueTask, assignee1);

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verify(taskAssigneeRepository, times(1)).findByTaskId(100L);
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(overdueTask, assignee1);
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(overdueTask, assignee2);
    }

    @Test
    @DisplayName("Should use correct threshold time (24 hours before now)")
    void shouldUseCorrectThreshold() {
        // Arrange
        ArgumentCaptor<OffsetDateTime> thresholdCaptor = ArgumentCaptor.forClass(OffsetDateTime.class);
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(new ArrayList<>());

        // Act
        OffsetDateTime beforeCall = OffsetDateTime.now(ZoneOffset.UTC).minusHours(24);
        checkerService.checkAndNotifyOverdueTasks();
        OffsetDateTime afterCall = OffsetDateTime.now(ZoneOffset.UTC).minusHours(24);

        // Assert
        verify(taskRepository).findByDueDateTimeBefore(thresholdCaptor.capture());
        OffsetDateTime capturedThreshold = thresholdCaptor.getValue();

        // The threshold should be approximately 24 hours ago (within a few seconds tolerance)
        assertTrue(capturedThreshold.isAfter(beforeCall.minusSeconds(5)),
                "Threshold should be after beforeCall minus tolerance");
        assertTrue(capturedThreshold.isBefore(afterCall.plusSeconds(5)),
                "Threshold should be before afterCall plus tolerance");
    }

    @Test
    @DisplayName("Should handle null assignees list")
    void shouldHandleNullAssigneesList() {
        // Arrange
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));
        when(taskAssigneeRepository.findByTaskId(100L))
                .thenReturn(null);

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(taskRepository, times(1)).findByDueDateTimeBefore(any(OffsetDateTime.class));
        verify(taskAssigneeRepository, times(1)).findByTaskId(100L);
        verifyNoInteractions(overdueTaskEmailService);
    }

    @Test
    @DisplayName("Should process only non-deleted tasks from repository")
    void shouldProcessOnlyNonDeletedTasks() {
        // Note: The repository query already filters deleteInd = false
        // This test verifies the integration point

        // Arrange
        overdueTask.setDeleteInd(false);
        when(taskRepository.findByDueDateTimeBefore(any(OffsetDateTime.class)))
                .thenReturn(List.of(overdueTask));
        when(taskAssigneeRepository.findByTaskId(100L))
                .thenReturn(List.of(assignee1));

        // Act
        checkerService.checkAndNotifyOverdueTasks();

        // Assert
        verify(overdueTaskEmailService, times(1)).sendOverdueTaskEmail(overdueTask, assignee1);
    }
}

