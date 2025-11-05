package com.spmorangle.crm.reporting.repository;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.projectmanagement.model.Project;
import com.spmorangle.crm.projectmanagement.repository.ProjectRepository;
import com.spmorangle.crm.reporting.model.TaskTimeTracking;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("ReportingRepository - In-Progress Hours Calculation Tests")
class ReportingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReportingRepository reportingRepository;

    @Autowired
    private TaskTimeTrackingRepository taskTimeTrackingRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User testUser;
    private User testUser2;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new User();
        testUser.setUserName("testuser");
        testUser.setEmail("test@example.com");
        testUser.setDepartmentId(100L); // Engineering department
        testUser.setRoleType(UserType.STAFF.getCode());
        testUser.setCognitoSub(UUID.randomUUID());
        testUser.setIsActive(true);
        testUser = entityManager.persistAndFlush(testUser);

        // Create second test user
        testUser2 = new User();
        testUser2.setUserName("testuser2");
        testUser2.setEmail("test2@example.com");
        testUser2.setDepartmentId(200L); // Marketing department
        testUser2.setRoleType(UserType.STAFF.getCode());
        testUser2.setCognitoSub(UUID.randomUUID());
        testUser2.setIsActive(true);
        testUser2 = entityManager.persistAndFlush(testUser2);

        // Create test project
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setOwnerId(testUser.getId());
        testProject.setDeleteInd(false);
        testProject.setCreatedAt(OffsetDateTime.now());
        testProject.setUpdatedAt(OffsetDateTime.now());
        testProject.setCreatedBy(testUser.getId());
        testProject.setUpdatedBy(testUser.getId());
        testProject = entityManager.persistAndFlush(testProject);

        // Create test task
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setOwnerId(testUser.getId());
        testTask.setProjectId(testProject.getId());
        testTask.setStatus(Status.IN_PROGRESS);
        testTask.setDeleteInd(false);
        testTask.setTaskType(TaskType.FEATURE);
        testTask.setCreatedAt(OffsetDateTime.now());
        testTask.setCreatedBy(testUser.getId());
        testTask = entityManager.persistAndFlush(testTask);
    }

    @Nested
    @DisplayName("getLoggedHoursForUser - Completed Tasks")
    class CompletedTasksTests {

        @Test
        @DisplayName("Should calculate hours for completed tasks using stored totalHours")
        void testGetLoggedHoursForUser_CompletedTask() {
            // Arrange - Create completed time tracking
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(testUser.getId());
            tracking.setStartedAt(OffsetDateTime.now().minusHours(3));
            tracking.setCompletedAt(OffsetDateTime.now());
            tracking.setTotalHours(new BigDecimal("3.00"));
            entityManager.persistAndFlush(tracking);

            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null, // all projects
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isEqualByComparingTo(new BigDecimal("3.00"));
        }

        @Test
        @DisplayName("Should sum multiple completed tasks correctly")
        void testGetLoggedHoursForUser_MultipleCompletedTasks() {
            // Arrange - Create first completed task tracking
            TaskTimeTracking tracking1 = new TaskTimeTracking();
            tracking1.setTaskId(testTask.getId());
            tracking1.setUserId(testUser.getId());
            tracking1.setStartedAt(OffsetDateTime.now().minusHours(5));
            tracking1.setCompletedAt(OffsetDateTime.now().minusHours(2));
            tracking1.setTotalHours(new BigDecimal("3.00"));
            entityManager.persistAndFlush(tracking1);

            // Create second task
            Task task2 = new Task();
            task2.setTitle("Test Task 2");
            task2.setOwnerId(testUser.getId());
            task2.setProjectId(testProject.getId());
            task2.setStatus(Status.COMPLETED);
            task2.setDeleteInd(false);
            task2.setTaskType(TaskType.FEATURE);
            task2.setCreatedAt(OffsetDateTime.now());
            task2.setCreatedBy(testUser.getId());
            task2 = entityManager.persistAndFlush(task2);

            // Create second completed task tracking
            TaskTimeTracking tracking2 = new TaskTimeTracking();
            tracking2.setTaskId(task2.getId());
            tracking2.setUserId(testUser.getId());
            tracking2.setStartedAt(OffsetDateTime.now().minusHours(4));
            tracking2.setCompletedAt(OffsetDateTime.now().minusHours(2));
            tracking2.setTotalHours(new BigDecimal("2.50"));
            entityManager.persistAndFlush(tracking2);

            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should sum both tasks
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isEqualByComparingTo(new BigDecimal("5.50"));
        }
    }

    @Nested
    @DisplayName("getLoggedHoursForUser - In-Progress Tasks")
    class InProgressTasksTests {

        @Test
        @DisplayName("Should calculate hours for in-progress tasks dynamically (current_time - started_at)")
        void testGetLoggedHoursForUser_InProgressTask() {
            // Arrange - Create in-progress time tracking (started 2 hours ago)
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(testUser.getId());
            tracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            tracking.setCompletedAt(null); // IN PROGRESS
            tracking.setTotalHours(null);   // Not yet calculated
            entityManager.persistAndFlush(tracking);

            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should be approximately 2 hours (with small tolerance for execution time)
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isGreaterThan(new BigDecimal("1.95"));
            assertThat(loggedHours).isLessThan(new BigDecimal("2.05"));
        }

        @Test
        @DisplayName("Should calculate hours for recently started in-progress task")
        void testGetLoggedHoursForUser_RecentlyStartedTask() {
            // Arrange - Task started 30 minutes ago
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(testUser.getId());
            tracking.setStartedAt(OffsetDateTime.now().minusMinutes(30));
            tracking.setCompletedAt(null);
            tracking.setTotalHours(null);
            entityManager.persistAndFlush(tracking);

            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should be approximately 0.5 hours
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isGreaterThan(new BigDecimal("0.45"));
            assertThat(loggedHours).isLessThan(new BigDecimal("0.55"));
        }

        @Test
        @DisplayName("Should calculate hours for long-running in-progress task")
        void testGetLoggedHoursForUser_LongRunningTask() {
            // Arrange - Task started 8 hours ago
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(testUser.getId());
            tracking.setStartedAt(OffsetDateTime.now().minusHours(8));
            tracking.setCompletedAt(null);
            tracking.setTotalHours(null);
            entityManager.persistAndFlush(tracking);

            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should be approximately 8 hours
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isGreaterThan(new BigDecimal("7.95"));
            assertThat(loggedHours).isLessThan(new BigDecimal("8.05"));
        }
    }

    @Nested
    @DisplayName("getLoggedHoursForUser - Mixed Tasks")
    class MixedTasksTests {

        @Test
        @DisplayName("Should sum both completed and in-progress hours")
        void testGetLoggedHoursForUser_MixedTasks() {
            // Arrange - Create completed tracking (3 hours)
            TaskTimeTracking completedTracking = new TaskTimeTracking();
            completedTracking.setTaskId(testTask.getId());
            completedTracking.setUserId(testUser.getId());
            completedTracking.setStartedAt(OffsetDateTime.now().minusHours(5));
            completedTracking.setCompletedAt(OffsetDateTime.now().minusHours(2));
            completedTracking.setTotalHours(new BigDecimal("3.00"));
            entityManager.persistAndFlush(completedTracking);

            // Create second task for in-progress tracking
            Task inProgressTask = new Task();
            inProgressTask.setTitle("In Progress Task");
            inProgressTask.setOwnerId(testUser.getId());
            inProgressTask.setProjectId(testProject.getId());
            inProgressTask.setStatus(Status.IN_PROGRESS);
            inProgressTask.setDeleteInd(false);
            inProgressTask.setTaskType(TaskType.FEATURE);
            inProgressTask.setCreatedAt(OffsetDateTime.now());
            inProgressTask.setCreatedBy(testUser.getId());
            inProgressTask = entityManager.persistAndFlush(inProgressTask);

            // Create in-progress tracking (1 hour)
            TaskTimeTracking inProgressTracking = new TaskTimeTracking();
            inProgressTracking.setTaskId(inProgressTask.getId());
            inProgressTracking.setUserId(testUser.getId());
            inProgressTracking.setStartedAt(OffsetDateTime.now().minusHours(1));
            inProgressTracking.setCompletedAt(null);
            inProgressTracking.setTotalHours(null);
            entityManager.persistAndFlush(inProgressTracking);

            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should be approximately 4 hours total (3 + 1)
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isGreaterThan(new BigDecimal("3.95"));
            assertThat(loggedHours).isLessThan(new BigDecimal("4.05"));
        }

        @Test
        @DisplayName("Should handle multiple in-progress and completed tasks")
        void testGetLoggedHoursForUser_ComplexMixedScenario() {
            // Create completed task 1: 2.5 hours
            TaskTimeTracking completed1 = new TaskTimeTracking();
            completed1.setTaskId(testTask.getId());
            completed1.setUserId(testUser.getId());
            completed1.setStartedAt(OffsetDateTime.now().minusHours(6));
            completed1.setCompletedAt(OffsetDateTime.now().minusHours(3).minusMinutes(30));
            completed1.setTotalHours(new BigDecimal("2.50"));
            entityManager.persistAndFlush(completed1);

            // Create completed task 2: 1.5 hours
            Task task2 = new Task();
            task2.setTitle("Completed Task 2");
            task2.setOwnerId(testUser.getId());
            task2.setProjectId(testProject.getId());
            task2.setStatus(Status.COMPLETED);
            task2.setDeleteInd(false);
            task2.setTaskType(TaskType.FEATURE);
            task2.setCreatedAt(OffsetDateTime.now());
            task2.setCreatedBy(testUser.getId());
            task2 = entityManager.persistAndFlush(task2);

            TaskTimeTracking completed2 = new TaskTimeTracking();
            completed2.setTaskId(task2.getId());
            completed2.setUserId(testUser.getId());
            completed2.setStartedAt(OffsetDateTime.now().minusHours(7));
            completed2.setCompletedAt(OffsetDateTime.now().minusHours(5).minusMinutes(30));
            completed2.setTotalHours(new BigDecimal("1.50"));
            entityManager.persistAndFlush(completed2);

            // Create in-progress task 1: ~2 hours
            Task inProgress1 = new Task();
            inProgress1.setTitle("In Progress 1");
            inProgress1.setOwnerId(testUser.getId());
            inProgress1.setProjectId(testProject.getId());
            inProgress1.setStatus(Status.IN_PROGRESS);
            inProgress1.setDeleteInd(false);
            inProgress1.setTaskType(TaskType.FEATURE);
            inProgress1.setCreatedAt(OffsetDateTime.now());
            inProgress1.setCreatedBy(testUser.getId());
            inProgress1 = entityManager.persistAndFlush(inProgress1);

            TaskTimeTracking inProgressTracking1 = new TaskTimeTracking();
            inProgressTracking1.setTaskId(inProgress1.getId());
            inProgressTracking1.setUserId(testUser.getId());
            inProgressTracking1.setStartedAt(OffsetDateTime.now().minusHours(2));
            inProgressTracking1.setCompletedAt(null);
            inProgressTracking1.setTotalHours(null);
            entityManager.persistAndFlush(inProgressTracking1);

            // Create in-progress task 2: ~0.5 hours
            Task inProgress2 = new Task();
            inProgress2.setTitle("In Progress 2");
            inProgress2.setOwnerId(testUser.getId());
            inProgress2.setProjectId(testProject.getId());
            inProgress2.setStatus(Status.IN_PROGRESS);
            inProgress2.setDeleteInd(false);
            inProgress2.setTaskType(TaskType.FEATURE);
            inProgress2.setCreatedAt(OffsetDateTime.now());
            inProgress2.setCreatedBy(testUser.getId());
            inProgress2 = entityManager.persistAndFlush(inProgress2);

            TaskTimeTracking inProgressTracking2 = new TaskTimeTracking();
            inProgressTracking2.setTaskId(inProgress2.getId());
            inProgressTracking2.setUserId(testUser.getId());
            inProgressTracking2.setStartedAt(OffsetDateTime.now().minusMinutes(30));
            inProgressTracking2.setCompletedAt(null);
            inProgressTracking2.setTotalHours(null);
            entityManager.persistAndFlush(inProgressTracking2);

            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should be approximately 6.5 hours (2.5 + 1.5 + 2 + 0.5)
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isGreaterThan(new BigDecimal("6.40"));
            assertThat(loggedHours).isLessThan(new BigDecimal("6.60"));
        }
    }

    @Nested
    @DisplayName("getLoggedHoursForUser - Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should return 0 when no time tracking exists")
        void testGetLoggedHoursForUser_NoTracking() {
            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return 0 for user with no tasks")
        void testGetLoggedHoursForUser_UserWithNoTasks() {
            // Arrange - Create tracking for testUser, query for testUser2
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(testUser.getId());
            tracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            tracking.setCompletedAt(null);
            tracking.setTotalHours(null);
            entityManager.persistAndFlush(tracking);

            // Act - Query for different user
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser2.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should handle tasks with startedAt but no tracking record started yet")
        void testGetLoggedHoursForUser_TaskWithNullStartedAt() {
            // Arrange - Create tracking with null startedAt (edge case)
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(testUser.getId());
            tracking.setStartedAt(null);
            tracking.setCompletedAt(null);
            tracking.setTotalHours(null);
            entityManager.persistAndFlush(tracking);

            // Act
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should be 0 since startedAt is null
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getLoggedHoursForUser - Filtering")
    class FilteringTests {

        @Test
        @DisplayName("Should filter by project IDs correctly")
        void testGetLoggedHoursForUser_FiltersByProject() {
            // Arrange - Create tracking for test project
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(testUser.getId());
            tracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            tracking.setCompletedAt(null);
            tracking.setTotalHours(null);
            entityManager.persistAndFlush(tracking);

            // Act - Query with project filter (should include)
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                Collections.singletonList(testProject.getId()),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should include hours from the filtered project
            assertThat(loggedHours).isNotNull();
            assertThat(loggedHours).isGreaterThan(BigDecimal.ZERO);

            // Act - Query with different project ID (should exclude)
            BigDecimal noHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                Collections.singletonList(99999L), // Non-existent project
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should return 0 for non-matching project
            assertThat(noHours).isNotNull();
            assertThat(noHours).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should filter by date range correctly")
        void testGetLoggedHoursForUser_FiltersByDateRange() {
            // Arrange - Create tracking that started yesterday
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(testUser.getId());
            tracking.setStartedAt(OffsetDateTime.now().minusDays(1));
            tracking.setCompletedAt(OffsetDateTime.now().minusDays(1).plusHours(2));
            tracking.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(tracking);

            // Act - Query with date range that includes yesterday
            BigDecimal includedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now().minusDays(2),
                LocalDate.now()
            );

            // Assert - Should include the tracking
            assertThat(includedHours).isNotNull();
            assertThat(includedHours).isEqualByComparingTo(new BigDecimal("2.00"));

            // Act - Query with date range that excludes yesterday
            BigDecimal excludedHours = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                null,
                LocalDate.now(),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should not include the tracking from yesterday
            assertThat(excludedHours).isNotNull();
            assertThat(excludedHours).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should filter by multiple project IDs")
        void testGetLoggedHoursForUser_MultipleProjectFilter() {
            // Create second project
            Project project2 = new Project();
            project2.setName("Test Project 2");
            project2.setOwnerId(testUser.getId());
            project2.setDeleteInd(false);
            project2.setCreatedAt(OffsetDateTime.now());
            project2.setUpdatedAt(OffsetDateTime.now());
            project2.setCreatedBy(testUser.getId());
            project2.setUpdatedBy(testUser.getId());
            project2 = entityManager.persistAndFlush(project2);

            // Create task in project 1
            TaskTimeTracking tracking1 = new TaskTimeTracking();
            tracking1.setTaskId(testTask.getId());
            tracking1.setUserId(testUser.getId());
            tracking1.setStartedAt(OffsetDateTime.now().minusHours(2));
            tracking1.setCompletedAt(OffsetDateTime.now());
            tracking1.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(tracking1);

            // Create task in project 2
            Task task2 = new Task();
            task2.setTitle("Task in Project 2");
            task2.setOwnerId(testUser.getId());
            task2.setProjectId(project2.getId());
            task2.setStatus(Status.COMPLETED);
            task2.setDeleteInd(false);
            task2.setTaskType(TaskType.FEATURE);
            task2.setCreatedAt(OffsetDateTime.now());
            task2.setCreatedBy(testUser.getId());
            task2 = entityManager.persistAndFlush(task2);

            TaskTimeTracking tracking2 = new TaskTimeTracking();
            tracking2.setTaskId(task2.getId());
            tracking2.setUserId(testUser.getId());
            tracking2.setStartedAt(OffsetDateTime.now().minusHours(3));
            tracking2.setCompletedAt(OffsetDateTime.now());
            tracking2.setTotalHours(new BigDecimal("3.00"));
            entityManager.persistAndFlush(tracking2);

            // Act - Filter by both projects
            BigDecimal bothProjects = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                List.of(testProject.getId(), project2.getId()),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should sum both projects
            assertThat(bothProjects).isNotNull();
            assertThat(bothProjects).isEqualByComparingTo(new BigDecimal("5.00"));

            // Act - Filter by only project 1
            BigDecimal onlyProject1 = reportingRepository.getLoggedHoursForUser(
                testUser.getId(),
                Collections.singletonList(testProject.getId()),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should only include project 1
            assertThat(onlyProject1).isNotNull();
            assertThat(onlyProject1).isEqualByComparingTo(new BigDecimal("2.00"));
        }
    }
}

