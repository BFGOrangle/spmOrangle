package com.spmorangle.crm.notification.service.impl;

import com.spmorangle.crm.notification.service.EmailService;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import com.spmorangle.crm.usermanagement.service.UserManagementService;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OverdueTaskEmailService Tests")
class OverdueTaskEmailServiceImplTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private OverdueTaskEmailServiceImpl overdueTaskEmailService;

    private Task testTask;
    private TaskAssignee testAssignee;
    private UserResponseDto testUser;

    @BeforeEach
    void setUp() {
        // Setup test task
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Important Task");
        testTask.setDescription("This is an important task that is overdue");
        testTask.setStatus(Status.TODO);
        testTask.setDueDateTime(OffsetDateTime.of(2025, 10, 22, 21, 7, 0, 0, ZoneOffset.UTC));

        // Setup test assignee
        testAssignee = new TaskAssignee();
        testAssignee.setTaskId(1L);
        testAssignee.setUserId(100L);

        // Setup test user
        testUser = UserResponseDto.builder()
                .id(100L)
                .username("John Doe")
                .email("john.doe@example.com")
                .roleType("EMPLOYEE")
                .isActive(true)
                .department("Engineering")
                .cognitoSub(UUID.randomUUID())
                .build();

        // Mock user management service with lenient stubbing
        lenient().when(userManagementService.getUserById(100L)).thenReturn(testUser);

        // Mock email service to return completed future (since sendHtmlEmail is async)
        lenient().when(emailService.sendHtmlEmail(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    @DisplayName("Should send overdue task email successfully")
    void shouldSendOverdueTaskEmail() {
        // Act
        overdueTaskEmailService.sendOverdueTaskEmail(testTask, testAssignee);

        // Assert
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(1)).sendHtmlEmail(
                emailCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture()
        );

        // Verify email recipient
        assertEquals("john.doe@example.com", emailCaptor.getValue());

        // Verify subject
        assertEquals("Overdue Task: Important Task", subjectCaptor.getValue());

        // Verify body contains expected content
        String body = bodyCaptor.getValue();
        assertTrue(body.contains("John Doe"), "Email body should contain assignee name");
        assertTrue(body.contains("Important Task"), "Email body should contain task title");
        assertTrue(body.contains("This is an important task that is overdue"), "Email body should contain task description");
        assertTrue(body.contains("TODO"), "Email body should contain task status");
        // UTC 2025-10-22 21:07:00 converts to SGT 2025-10-23 05:07:00 (UTC+8)
        assertTrue(body.contains("Oct 23, 2025 05:07:00"), "Email body should contain formatted due date");

        // Verify user management service was called
        verify(userManagementService, times(2)).getUserById(100L); // Once for email, once for name
    }

    @Test
    @DisplayName("Should send overdue task email for task without description")
    void shouldSendOverdueTaskEmailWithoutDescription() {
        // Arrange
        testTask.setDescription(null);

        // Act
        overdueTaskEmailService.sendOverdueTaskEmail(testTask, testAssignee);

        // Assert
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).sendHtmlEmail(
                eq("john.doe@example.com"),
                anyString(),
                bodyCaptor.capture()
        );

        String body = bodyCaptor.getValue();
        assertFalse(body.contains("<p><strong>Description:</strong>"),
                "Email body should not contain description section when description is null");
    }

    @Test
    @DisplayName("Should not send email when task list is empty")
    void shouldNotSendEmailWhenTaskListIsEmpty() {
        // Arrange
        List<Task> emptyList = new ArrayList<>();

        // Act
        overdueTaskEmailService.sendMultipleOverdueTasksEmail(emptyList, testAssignee);

        // Assert
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString());
        verify(userManagementService, never()).getUserById(anyLong());
    }

    @Test
    @DisplayName("Should send summary email for multiple overdue tasks")
    void shouldSendSummaryEmailForMultipleTasks() {
        // Arrange
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setStatus(Status.TODO);
        task1.setDueDateTime(OffsetDateTime.of(2025, 10, 15, 10, 0, 0, 0, ZoneOffset.UTC));

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setStatus(Status.IN_PROGRESS);
        task2.setDueDateTime(OffsetDateTime.of(2025, 10, 20, 14, 30, 0, 0, ZoneOffset.UTC));

        Task task3 = new Task();
        task3.setId(3L);
        task3.setTitle("Task 3");
        task3.setStatus(Status.TODO);
        task3.setDueDateTime(OffsetDateTime.of(2025, 10, 22, 9, 15, 0, 0, ZoneOffset.UTC));

        List<Task> tasks = List.of(task1, task2, task3);

        // Act
        overdueTaskEmailService.sendMultipleOverdueTasksEmail(tasks, testAssignee);

        // Assert
        ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService, times(1)).sendHtmlEmail(
                emailCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture()
        );

        // Verify email recipient
        assertEquals("john.doe@example.com", emailCaptor.getValue());

        // Verify subject
        assertEquals("You have 3 overdue tasks", subjectCaptor.getValue());

        // Verify body contains all tasks
        String body = bodyCaptor.getValue();
        assertTrue(body.contains("John Doe"), "Email body should contain assignee name");
        assertTrue(body.contains("3 overdue tasks"), "Email body should mention the count");
        assertTrue(body.contains("Task 1"), "Email body should contain first task");
        assertTrue(body.contains("Task 2"), "Email body should contain second task");
        assertTrue(body.contains("Task 3"), "Email body should contain third task");
        // UTC times converted to SGT (UTC+8):
        // 2025-10-15 10:00:00 UTC -> 2025-10-15 18:00:00 SGT
        assertTrue(body.contains("Oct 15, 2025 18:00:00"), "Email body should contain first task due date");
        // 2025-10-20 14:30:00 UTC -> 2025-10-20 22:30:00 SGT
        assertTrue(body.contains("Oct 20, 2025 22:30:00"), "Email body should contain second task due date");
        // 2025-10-22 09:15:00 UTC -> 2025-10-22 17:15:00 SGT
        assertTrue(body.contains("Oct 22, 2025 17:15:00"), "Email body should contain third task due date");
    }

    @Test
    @DisplayName("Should send summary email for single overdue task")
    void shouldSendSummaryEmailForSingleTask() {
        // Arrange
        List<Task> tasks = List.of(testTask);

        // Act
        overdueTaskEmailService.sendMultipleOverdueTasksEmail(tasks, testAssignee);

        // Assert
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService, times(1)).sendHtmlEmail(
                eq("john.doe@example.com"),
                subjectCaptor.capture(),
                anyString()
        );

        // Verify subject for single task
        assertEquals("You have 1 overdue tasks", subjectCaptor.getValue());
    }

    @Test
    @DisplayName("Should get assignee email correctly")
    void shouldGetAssigneeEmail() {
        // Act
        String email = overdueTaskEmailService.getAssigneeEmail(testAssignee);

        // Assert
        assertEquals("john.doe@example.com", email);
        verify(userManagementService, times(1)).getUserById(100L);
    }

    @Test
    @DisplayName("Should get assignee name correctly")
    void shouldGetAssigneeName() {
        // Act
        String name = overdueTaskEmailService.getAssigneeName(testAssignee);

        // Assert
        assertEquals("John Doe", name);
        verify(userManagementService, times(1)).getUserById(100L);
    }

    @Test
    @DisplayName("Should handle different timezones in due date formatting")
    void shouldHandleDifferentTimezones() {
        // Arrange - Create task with different timezone offset
        testTask.setDueDateTime(OffsetDateTime.of(2025, 12, 25, 15, 30, 45, 0, ZoneOffset.ofHours(8)));

        // Act
        overdueTaskEmailService.sendOverdueTaskEmail(testTask, testAssignee);

        // Assert
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendHtmlEmail(anyString(), anyString(), bodyCaptor.capture());

        String body = bodyCaptor.getValue();
        assertTrue(body.contains("Dec 25, 2025 15:30:45"),
                "Email body should contain formatted due date with time");
    }

    @Test
    @DisplayName("Should handle task with very long title")
    void shouldHandleTaskWithLongTitle() {
        // Arrange
        String longTitle = "A".repeat(500);
        testTask.setTitle(longTitle);

        // Act
        overdueTaskEmailService.sendOverdueTaskEmail(testTask, testAssignee);

        // Assert
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailService).sendHtmlEmail(
                anyString(),
                subjectCaptor.capture(),
                bodyCaptor.capture()
        );

        assertTrue(subjectCaptor.getValue().contains(longTitle));
        assertTrue(bodyCaptor.getValue().contains(longTitle));
    }

    @Test
    @DisplayName("Should handle HTML special characters in task description")
    void shouldHandleHtmlSpecialCharactersInDescription() {
        // Arrange
        testTask.setDescription("Task with <script>alert('xss')</script> and & special chars");

        // Act
        overdueTaskEmailService.sendOverdueTaskEmail(testTask, testAssignee);

        // Assert
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendHtmlEmail(anyString(), anyString(), bodyCaptor.capture());

        String body = bodyCaptor.getValue();
        // Note: The current implementation doesn't escape HTML, but the test documents the behavior
        assertTrue(body.contains("<script>alert('xss')</script>"),
                "Email body contains the description as-is");
    }
}
