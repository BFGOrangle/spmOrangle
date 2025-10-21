package com.spmorangle.crm.notification.messaging.consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.spmorangle.common.config.FrontendConfig;
import com.spmorangle.common.enums.NotificationType;
import com.spmorangle.crm.notification.dto.CreateNotificationDto;
import com.spmorangle.crm.notification.dto.NotificationDto;
import com.spmorangle.crm.notification.enums.Channel;
import com.spmorangle.crm.notification.enums.Priority;
import com.spmorangle.crm.notification.messaging.dto.TaskNotificationMessageDto;
import com.spmorangle.crm.notification.service.EmailService;
import com.spmorangle.crm.notification.service.NotificationService;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;

@ExtendWith(MockitoExtension.class)
class TaskNotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserManagementService userManagementService;

    @Mock
    private EmailService emailService;

    @Mock(lenient = true)
    private FrontendConfig frontendConfig;

    @InjectMocks
    private TaskNotificationConsumer taskNotificationConsumer;

    private TaskNotificationMessageDto taskCreatedMessage;
    private TaskNotificationMessageDto taskAssignedMessage;
    private TaskNotificationMessageDto taskCompletedMessage;
    private TaskNotificationMessageDto taskUpdatedMessage;
    private TaskNotificationMessageDto taskUnassignedMessage;
    private TaskNotificationMessageDto statusUpdatedMessage;

    @BeforeEach
    void setUp() {
        // Mock frontend config
        when(frontendConfig.getBaseUrl()).thenReturn("http://localhost:3000");

        // Setup task created message
        taskCreatedMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-123")
                .eventType("TASK_CREATED")
                .taskId(1L)
                .authorId(100L)
                .projectId(10L)
                .taskTitle("Test Task")
                .taskDescription("Test Description")
                .assignedUserIds(List.of(200L, 300L))
                .timestamp(Instant.now())
                .build();

        // Setup task assigned message
        taskAssignedMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-456")
                .eventType("TASK_ASSIGNED")
                .taskId(2L)
                .authorId(100L)
                .projectId(10L)
                .taskTitle("Assigned Task")
                .taskDescription("Assignment Description")
                .assignedUserIds(List.of(200L))
                .timestamp(Instant.now())
                .build();

        // Setup task completed message
        taskCompletedMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-789")
                .eventType("TASK_COMPLETED")
                .taskId(3L)
                .authorId(100L)
                .projectId(10L)
                .taskTitle("Completed Task")
                .assignedUserIds(List.of(200L, 300L))
                .timestamp(Instant.now())
                .build();

        // Setup task updated message
        taskUpdatedMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-012")
                .eventType("TASK_UPDATED")
                .taskId(4L)
                .authorId(100L)
                .projectId(10L)
                .taskTitle("Updated Task")
                .taskStatus("IN_PROGRESS")
                .assignedUserIds(List.of(200L))
                .timestamp(Instant.now())
                .build();

        // Setup task unassigned message
        taskUnassignedMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-345")
                .eventType("TASK_UNASSIGNED")
                .taskId(5L)
                .authorId(100L)
                .projectId(10L)
                .taskTitle("Unassigned Task")
                .taskDescription("Removal Description")
                .assignedUserIds(List.of(200L))
                .timestamp(Instant.now())
                .build();

        // Setup status updated message
        statusUpdatedMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-678")
                .eventType("STATUS_UPDATED")
                .taskId(6L)
                .authorId(100L)
                .projectId(10L)
                .taskTitle("Status Changed Task")
                .prevTaskStatus("TODO")
                .taskStatus("IN_PROGRESS")
                .assignedUserIds(List.of(200L, 300L))
                .timestamp(Instant.now())
                .build();
    }

    @Test
    void testHandleTaskCreatedNotification() {
        // Arrange
        NotificationDto notification1 = createMockNotificationDto(1L, 200L, "Test notification 1");
        NotificationDto notification2 = createMockNotificationDto(2L, 300L, "Test notification 2");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification1, notification2));

        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", "STAFF", true, UUID.randomUUID()));
        when(userManagementService.getUserById(300L))
                .thenReturn(new UserResponseDto(300L, "User2", "user2@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskCreatedMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 2;
        assert capturedNotifications.get(0).getTargetId().equals(200L);
        assert capturedNotifications.get(1).getTargetId().equals(300L);

        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleTaskCreatedNotification_NoAssignees() {
        // Arrange
        TaskNotificationMessageDto messageWithNoAssignees = TaskNotificationMessageDto.builder()
                .messageId("msg-empty")
                .eventType("TASK_CREATED")
                .taskId(1L)
                .authorId(100L)
                .projectId(10L)
                .taskTitle("Test Task")
                .taskDescription("Test Description")
                .assignedUserIds(new ArrayList<>())
                .timestamp(Instant.now())
                .build();

        // Act
        taskNotificationConsumer.handleTaskNotification(messageWithNoAssignees);

        // Assert
        verify(notificationService, never()).createBulkNotifications(anyList());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleTaskAssignedNotification() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Task assigned");
        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskAssignedMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 1;
        assert capturedNotifications.get(0).getSubject().equals("Task assigned to you");
        assert capturedNotifications.get(0).getPriority() == Priority.HIGH;

        verify(emailService).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleTaskCompletedNotification() {
        // Arrange
        NotificationDto notification1 = createMockNotificationDto(1L, 200L, "Task completed");
        NotificationDto notification2 = createMockNotificationDto(2L, 300L, "Task completed");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification1, notification2));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskCompletedMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 2;
        assert capturedNotifications.get(0).getSubject().equals("Task completed");
        assert capturedNotifications.get(0).getPriority() == Priority.LOW;
        assert capturedNotifications.get(0).getChannels().contains(Channel.IN_APP);
        assert !capturedNotifications.get(0).getChannels().contains(Channel.EMAIL);
    }

    @Test
    void testHandleTaskUpdatedNotification() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Task updated");
        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskUpdatedMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 1;
        assert capturedNotifications.get(0).getSubject().equals("Task updated");
        assert capturedNotifications.get(0).getMessage().contains("IN_PROGRESS");
    }

    @Test
    void testHandleUnknownEventType() {
        // Arrange
        TaskNotificationMessageDto unknownMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-unknown")
                .eventType("UNKNOWN_EVENT")
                .taskId(1L)
                .authorId(100L)
                .timestamp(Instant.now())
                .build();

        // Act
        taskNotificationConsumer.handleTaskNotification(unknownMessage);

        // Assert
        verify(notificationService, never()).createBulkNotifications(anyList());
    }

    @Test
    void testEmailNotification_WithFullUrl() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Test notification");

        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User", "user@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskCreatedMessage);

        // Assert
        ArgumentCaptor<String> emailBodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(eq("user@test.com"), anyString(), emailBodyCaptor.capture());

        String emailBody = emailBodyCaptor.getValue();
        assert emailBody.contains("http://localhost:3000/tasks/123");
    }

    @Test
    void testEmailNotification_UserWithNoEmail() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Test notification");

        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User", "", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskCreatedMessage);

        // Assert - Email should not be sent for empty email address
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testEmailNotification_UserNotFound() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Test notification");

        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(200L)).thenThrow(new RuntimeException("User not found"));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskCreatedMessage);

        // Assert - Should not throw exception, email service should not be called
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testNotificationProcessing_WithException() {
        // Arrange
        when(notificationService.createBulkNotifications(anyList()))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        try {
            taskNotificationConsumer.handleTaskNotification(taskCreatedMessage);
            assert false : "Expected exception to be thrown";
        } catch (RuntimeException e) {
            assert e.getMessage().equals("Database error");
        }
    }

    @Test
    void testHandleTaskUnassignedNotification() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Removed from task");
        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskUnassignedMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 1;
        assert capturedNotifications.get(0).getSubject().equals("Removed from task");
        assert capturedNotifications.get(0).getMessage().contains("You've been removed from task");
        assert capturedNotifications.get(0).getMessage().contains("Unassigned Task");
        assert capturedNotifications.get(0).getPriority() == Priority.MEDIUM;
        assert capturedNotifications.get(0).getChannels().contains(Channel.IN_APP);
        assert capturedNotifications.get(0).getChannels().contains(Channel.EMAIL);

        verify(emailService).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleTaskUnassignedNotification_SelfRemoval() {
        // Arrange - User removes themselves (authorId == assignedUserId)
        TaskNotificationMessageDto selfRemovalMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-self")
                .eventType("TASK_UNASSIGNED")
                .taskId(5L)
                .authorId(200L)
                .projectId(10L)
                .taskTitle("Self Removal Task")
                .assignedUserIds(List.of(200L))
                .timestamp(Instant.now())
                .build();

        // Act
        taskNotificationConsumer.handleTaskNotification(selfRemovalMessage);

        // Assert - Should not create notification for self-removal
        verify(notificationService, never()).createBulkNotifications(anyList());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleStatusUpdatedNotification() {
        // Arrange
        NotificationDto notification1 = createMockNotificationDto(1L, 200L, "Task status updated");
        NotificationDto notification2 = createMockNotificationDto(2L, 300L, "Task status updated");

        when(notificationService.createBulkNotifications(anyList()))
                .thenReturn(List.of(notification1, notification2));
        when(userManagementService.getUserById(100L))
                .thenReturn(new UserResponseDto(100L, "Editor", "editor@test.com", "STAFF", true, UUID.randomUUID()));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", "STAFF", true, UUID.randomUUID()));
        when(userManagementService.getUserById(300L))
                .thenReturn(new UserResponseDto(300L, "User2", "user2@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(statusUpdatedMessage);

        // Assert
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 2;
        assert capturedNotifications.get(0).getMessage().contains("Status Changed Task");
        assert capturedNotifications.get(0).getMessage().contains("TODO");
        assert capturedNotifications.get(0).getMessage().contains("IN_PROGRESS");
        assert capturedNotifications.get(0).getMessage().contains("Editor");
        assert capturedNotifications.get(0).getChannels().contains(Channel.IN_APP);
        assert capturedNotifications.get(0).getChannels().contains(Channel.EMAIL);

        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testHandleStatusUpdatedNotification_EditorIsAssignee() {
        // Arrange - Editor is also an assignee, should not receive notification
        TaskNotificationMessageDto editorIsAssigneeMessage = TaskNotificationMessageDto.builder()
                .messageId("msg-editor")
                .eventType("STATUS_UPDATED")
                .taskId(6L)
                .authorId(100L)
                .projectId(10L)
                .taskTitle("Status Changed Task")
                .prevTaskStatus("TODO")
                .taskStatus("DONE")
                .assignedUserIds(List.of(100L, 200L))
                .timestamp(Instant.now())
                .build();

        NotificationDto notification = createMockNotificationDto(1L, 200L, "Task status updated");
        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(100L))
                .thenReturn(new UserResponseDto(100L, "Editor", "editor@test.com", "STAFF", true, UUID.randomUUID()));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(editorIsAssigneeMessage);

        // Assert - Should only notify user 200, not the editor (100)
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.size() == 1;
        assert capturedNotifications.get(0).getTargetId().equals(200L);

        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testNotificationLinks_WithHighlightContext() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Task status updated");
        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(100L))
                .thenReturn(new UserResponseDto(100L, "Editor", "editor@test.com", "STAFF", true, UUID.randomUUID()));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(statusUpdatedMessage);

        // Assert - Check that link contains highlight parameter
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.get(0).getLink().contains("?highlight=status");
    }

    @Test
    void testNotificationLinks_AssigneeHighlightContext() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Task assigned");
        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskAssignedMessage);

        // Assert - Check that link contains assignees highlight parameter
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.get(0).getLink().contains("?highlight=assignees");
    }

    @Test
    void testNotificationLinks_UnassignedHighlightContext() {
        // Arrange
        NotificationDto notification = createMockNotificationDto(1L, 200L, "Removed from task");
        when(notificationService.createBulkNotifications(anyList())).thenReturn(List.of(notification));
        when(userManagementService.getUserById(200L))
                .thenReturn(new UserResponseDto(200L, "User1", "user1@test.com", "STAFF", true, UUID.randomUUID()));

        // Act
        taskNotificationConsumer.handleTaskNotification(taskUnassignedMessage);

        // Assert - Check that link contains assignees highlight parameter
        ArgumentCaptor<List<CreateNotificationDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationService).createBulkNotifications(captor.capture());

        List<CreateNotificationDto> capturedNotifications = captor.getValue();
        assert capturedNotifications.get(0).getLink().contains("?highlight=assignees");
    }

    private NotificationDto createMockNotificationDto(Long id, Long targetId, String subject) {
        return NotificationDto.builder()
                .notificationId(id)
                .authorId(100L)
                .targetId(targetId)
                .notificationType(NotificationType.TASK_ASSIGNED)
                .subject(subject)
                .message("Test message")
                .channels(List.of(Channel.IN_APP, Channel.EMAIL))
                .readStatus(false)
                .dismissedStatus(false)
                .priority(Priority.MEDIUM)
                .link("/tasks/123")
                .createdAt(Instant.now())
                .readAt(null)
                .build();
    }
}
