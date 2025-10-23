package com.spmorangle.crm.notification.service.impl;

import com.spmorangle.crm.notification.dto.DailyDigestDto;
import com.spmorangle.crm.notification.service.EmailTemplateService;
import com.spmorangle.crm.taskmanagement.dto.TaskResponseDto;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("EmailTemplateService Tests")
class EmailTemplateServiceImplTest {

    private EmailTemplateService emailTemplateService;

    @BeforeEach
    void setUp() {
        emailTemplateService = new EmailTemplateServiceImpl();
    }

    @Test
    @DisplayName("Should generate daily digest email with multiple tasks")
    void shouldGenerateDailyDigestWithMultipleTasks() {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .username("John Doe")
                .email("john@example.com")
                .build();

        TaskResponseDto task1 = TaskResponseDto.builder()
                .id(1L)
                .title("Complete project report")
                .description("Finish the quarterly report")
                .status(Status.TODO)
                .taskType(TaskType.FEATURE)
                .dueDateTime(OffsetDateTime.now().plusDays(1))
                .projectName("Q4 Project")
                .build();

        TaskResponseDto task2 = TaskResponseDto.builder()
                .id(2L)
                .title("Review code changes")
                .description("Review PR #123")
                .status(Status.IN_PROGRESS)
                .taskType(TaskType.CHORE)
                .dueDateTime(OffsetDateTime.now().plusDays(1))
                .build();

        TaskResponseDto task3 = TaskResponseDto.builder()
                .id(3L)
                .title("Fix critical bug")
                .description("Bug in payment system")
                .status(Status.BLOCKED)
                .taskType(TaskType.BUG)
                .dueDateTime(OffsetDateTime.now().plusDays(1))
                .build();

        List<TaskResponseDto> tasks = Arrays.asList(task1, task2, task3);

        DailyDigestDto digestDto = DailyDigestDto.builder()
                .user(user)
                .tasks(tasks)
                .frontendBaseUrl("http://localhost:3000")
                .todoCount(1L)
                .inProgressCount(1L)
                .blockedCount(1L)
                .totalPending(3L)
                .build();

        String result = emailTemplateService.generateDailyDigestEmail(digestDto);

        assertNotNull(result);
        assertTrue(result.contains("<!DOCTYPE html>"));
        assertTrue(result.contains("Daily Task Digest"));
        assertTrue(result.contains("John Doe"));
        assertTrue(result.contains("3 pending task"));
        assertTrue(result.contains("Complete project report"));
        assertTrue(result.contains("Review code changes"));
        assertTrue(result.contains("Fix critical bug"));
        assertTrue(result.contains("TODO"));
        assertTrue(result.contains("IN PROGRESS"));
        assertTrue(result.contains("BLOCKED"));
        assertTrue(result.contains("http://localhost:3000/tasks/1"));
        assertTrue(result.contains("Q4 Project"));
    }

    @Test
    @DisplayName("Should generate daily digest with single task")
    void shouldGenerateDailyDigestWithSingleTask() {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .username("Jane Smith")
                .email("jane@example.com")
                .build();

        TaskResponseDto task = TaskResponseDto.builder()
                .id(5L)
                .title("Update documentation")
                .status(Status.TODO)
                .taskType(TaskType.CHORE)
                .dueDateTime(OffsetDateTime.now().plusDays(1))
                .build();

        DailyDigestDto digestDto = DailyDigestDto.builder()
                .user(user)
                .tasks(Collections.singletonList(task))
                .frontendBaseUrl("http://localhost:3000")
                .todoCount(1L)
                .inProgressCount(0L)
                .blockedCount(0L)
                .totalPending(1L)
                .build();

        String result = emailTemplateService.generateDailyDigestEmail(digestDto);

        assertNotNull(result);
        assertTrue(result.contains("Jane Smith"));
        assertTrue(result.contains("1 pending task"));
        assertTrue(result.contains("Update documentation"));
        assertTrue(result.contains("http://localhost:3000/tasks/5"));
    }

    @Test
    @DisplayName("Should properly display summary statistics")
    void shouldDisplaySummaryStatistics() {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .username("Test User")
                .email("test@example.com")
                .build();

        DailyDigestDto digestDto = DailyDigestDto.builder()
                .user(user)
                .tasks(Collections.emptyList())
                .frontendBaseUrl("http://localhost:3000")
                .todoCount(5L)
                .inProgressCount(3L)
                .blockedCount(2L)
                .totalPending(10L)
                .build();

        String result = emailTemplateService.generateDailyDigestEmail(digestDto);

        assertTrue(result.contains("10 pending task"));
        assertTrue(result.contains(">5</div>"));
        assertTrue(result.contains(">3</div>"));
        assertTrue(result.contains(">2</div>"));
        assertTrue(result.contains("To Do"));
        assertTrue(result.contains("In Progress"));
        assertTrue(result.contains("Blocked"));
    }

    @Test
    @DisplayName("Should include proper spacing in summary stats CSS")
    void shouldIncludeProperSpacingInCSS() {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .username("Test User")
                .email("test@example.com")
                .build();

        DailyDigestDto digestDto = DailyDigestDto.builder()
                .user(user)
                .tasks(Collections.emptyList())
                .frontendBaseUrl("http://localhost:3000")
                .todoCount(1L)
                .inProgressCount(1L)
                .blockedCount(1L)
                .totalPending(3L)
                .build();

        String result = emailTemplateService.generateDailyDigestEmail(digestDto);

        assertTrue(result.contains("justify-content: space-around"));
        assertTrue(result.contains("gap: 30px"));
        assertTrue(result.contains("flex: 1"));
    }

    @Test
    @DisplayName("Should escape HTML in task titles and project names")
    void shouldEscapeHtmlInContent() {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .username("Test User")
                .email("test@example.com")
                .build();

        TaskResponseDto task = TaskResponseDto.builder()
                .id(1L)
                .title("Fix <script>alert('xss')</script> vulnerability")
                .description("Security issue")
                .status(Status.TODO)
                .taskType(TaskType.BUG)
                .dueDateTime(OffsetDateTime.now().plusDays(1))
                .projectName("Project & Development")
                .build();

        DailyDigestDto digestDto = DailyDigestDto.builder()
                .user(user)
                .tasks(Collections.singletonList(task))
                .frontendBaseUrl("http://localhost:3000")
                .todoCount(1L)
                .inProgressCount(0L)
                .blockedCount(0L)
                .totalPending(1L)
                .build();

        String result = emailTemplateService.generateDailyDigestEmail(digestDto);

        assertFalse(result.contains("<script>"));
        assertTrue(result.contains("&lt;script&gt;"));
        assertTrue(result.contains("&amp;"));
        assertFalse(result.contains("Project & Development"));
        assertTrue(result.contains("Project &amp; Development"));
    }

    @Test
    @DisplayName("Should handle null project name gracefully")
    void shouldHandleNullProjectName() {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .username("Test User")
                .email("test@example.com")
                .build();

        TaskResponseDto task = TaskResponseDto.builder()
                .id(1L)
                .title("Personal task")
                .status(Status.TODO)
                .taskType(TaskType.CHORE)
                .dueDateTime(OffsetDateTime.now().plusDays(1))
                .projectName(null)
                .build();

        DailyDigestDto digestDto = DailyDigestDto.builder()
                .user(user)
                .tasks(Collections.singletonList(task))
                .frontendBaseUrl("http://localhost:3000")
                .todoCount(1L)
                .inProgressCount(0L)
                .blockedCount(0L)
                .totalPending(1L)
                .build();

        String result = emailTemplateService.generateDailyDigestEmail(digestDto);

        assertNotNull(result);
        assertTrue(result.contains("Personal task"));
    }

    @Test
    @DisplayName("Should include correct status badge classes")
    void shouldIncludeCorrectStatusBadgeClasses() {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .username("Test User")
                .email("test@example.com")
                .build();

        TaskResponseDto todoTask = TaskResponseDto.builder()
                .id(1L)
                .title("Todo Task")
                .status(Status.TODO)
                .taskType(TaskType.FEATURE)
                .build();

        TaskResponseDto inProgressTask = TaskResponseDto.builder()
                .id(2L)
                .title("In Progress Task")
                .status(Status.IN_PROGRESS)
                .taskType(TaskType.CHORE)
                .build();

        TaskResponseDto blockedTask = TaskResponseDto.builder()
                .id(3L)
                .title("Blocked Task")
                .status(Status.BLOCKED)
                .taskType(TaskType.BUG)
                .build();

        DailyDigestDto digestDto = DailyDigestDto.builder()
                .user(user)
                .tasks(Arrays.asList(todoTask, inProgressTask, blockedTask))
                .frontendBaseUrl("http://localhost:3000")
                .todoCount(1L)
                .inProgressCount(1L)
                .blockedCount(1L)
                .totalPending(3L)
                .build();

        String result = emailTemplateService.generateDailyDigestEmail(digestDto);

        assertTrue(result.contains("status-todo"));
        assertTrue(result.contains("status-in-progress"));
        assertTrue(result.contains("status-blocked"));
    }

    @Test
    @DisplayName("Should include footer with correct information")
    void shouldIncludeFooterInformation() {
        UserResponseDto user = UserResponseDto.builder()
                .id(1L)
                .username("Test User")
                .email("test@example.com")
                .build();

        DailyDigestDto digestDto = DailyDigestDto.builder()
                .user(user)
                .tasks(Collections.emptyList())
                .frontendBaseUrl("http://localhost:3000")
                .todoCount(0L)
                .inProgressCount(0L)
                .blockedCount(0L)
                .totalPending(0L)
                .build();

        String result = emailTemplateService.generateDailyDigestEmail(digestDto);

        assertTrue(result.contains("SPM Orangle Team"));
        assertTrue(result.contains("automated daily digest"));
        assertTrue(result.contains("09:00"));
        assertTrue(result.contains("Please do not reply to this email"));
    }
}
