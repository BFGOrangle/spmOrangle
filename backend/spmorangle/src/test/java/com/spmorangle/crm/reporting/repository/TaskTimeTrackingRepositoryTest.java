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
@DisplayName("TaskTimeTrackingRepository - In-Progress Hours Calculation Tests")
class TaskTimeTrackingRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskTimeTrackingRepository taskTimeTrackingRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    private User engineeringUser;
    private User marketingUser;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        // Create engineering user
        engineeringUser = new User();
        engineeringUser.setUserName("engineer");
        engineeringUser.setEmail("engineer@example.com");
        engineeringUser.setDepartment("Engineering");
        engineeringUser.setRoleType(UserType.STAFF.getCode());
        engineeringUser.setCognitoSub(UUID.randomUUID());
        engineeringUser.setIsActive(true);
        engineeringUser = entityManager.persistAndFlush(engineeringUser);

        // Create marketing user
        marketingUser = new User();
        marketingUser.setUserName("marketer");
        marketingUser.setEmail("marketer@example.com");
        marketingUser.setDepartment("Marketing");
        marketingUser.setRoleType(UserType.STAFF.getCode());
        marketingUser.setCognitoSub(UUID.randomUUID());
        marketingUser.setIsActive(true);
        marketingUser = entityManager.persistAndFlush(marketingUser);

        // Create test project
        testProject = new Project();
        testProject.setName("Test Project");
        testProject.setOwnerId(engineeringUser.getId());
        testProject.setDeleteInd(false);
        testProject.setCreatedAt(OffsetDateTime.now());
        testProject.setUpdatedAt(OffsetDateTime.now());
        testProject.setCreatedBy(engineeringUser.getId());
        testProject.setUpdatedBy(engineeringUser.getId());
        testProject = entityManager.persistAndFlush(testProject);

        // Create test task
        testTask = new Task();
        testTask.setTitle("Test Task");
        testTask.setOwnerId(engineeringUser.getId());
        testTask.setProjectId(testProject.getId());
        testTask.setStatus(Status.IN_PROGRESS);
        testTask.setDeleteInd(false);
        testTask.setTaskType(TaskType.FEATURE);
        testTask.setCreatedAt(OffsetDateTime.now());
        testTask.setCreatedBy(engineeringUser.getId());
        testTask = entityManager.persistAndFlush(testTask);
    }

    @Nested
    @DisplayName("getHoursByDepartment Tests")
    class GetHoursByDepartmentTests {

        @Test
        @DisplayName("Should include in-progress hours in department totals")
        void testGetHoursByDepartment_IncludesInProgressHours() {
            // Arrange - Create completed tracking (2 hours)
            TaskTimeTracking completedTracking = new TaskTimeTracking();
            completedTracking.setTaskId(testTask.getId());
            completedTracking.setUserId(engineeringUser.getId());
            completedTracking.setStartedAt(OffsetDateTime.now().minusHours(4));
            completedTracking.setCompletedAt(OffsetDateTime.now().minusHours(2));
            completedTracking.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(completedTracking);

            // Create in-progress task
            Task inProgressTask = new Task();
            inProgressTask.setTitle("In Progress Task");
            inProgressTask.setOwnerId(engineeringUser.getId());
            inProgressTask.setProjectId(testProject.getId());
            inProgressTask.setStatus(Status.IN_PROGRESS);
            inProgressTask.setDeleteInd(false);
            inProgressTask.setTaskType(TaskType.FEATURE);
            inProgressTask.setCreatedAt(OffsetDateTime.now());
            inProgressTask.setCreatedBy(engineeringUser.getId());
            inProgressTask = entityManager.persistAndFlush(inProgressTask);

            // Create in-progress tracking (1 hour)
            TaskTimeTracking inProgressTracking = new TaskTimeTracking();
            inProgressTracking.setTaskId(inProgressTask.getId());
            inProgressTracking.setUserId(engineeringUser.getId());
            inProgressTracking.setStartedAt(OffsetDateTime.now().minusHours(1));
            inProgressTracking.setCompletedAt(null);
            inProgressTracking.setTotalHours(null);
            entityManager.persistAndFlush(inProgressTracking);

            // Act
            List<Object[]> results = taskTimeTrackingRepository.getHoursByDepartment(
                "", // all departments
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert
            assertThat(results).isNotEmpty();
            Object[] engineeringResult = results.stream()
                .filter(row -> "Engineering".equals(row[0]))
                .findFirst()
                .orElse(null);

            assertThat(engineeringResult).isNotNull();
            BigDecimal totalHours = (BigDecimal) engineeringResult[1];
            assertThat(totalHours).isGreaterThan(new BigDecimal("2.95")); // ~3 hours (2 + 1)
            assertThat(totalHours).isLessThan(new BigDecimal("3.05"));
        }

        @Test
        @DisplayName("Should group by department correctly")
        void testGetHoursByDepartment_GroupsByDepartment() {
            // Arrange - Engineering task
            TaskTimeTracking engTracking = new TaskTimeTracking();
            engTracking.setTaskId(testTask.getId());
            engTracking.setUserId(engineeringUser.getId());
            engTracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            engTracking.setCompletedAt(OffsetDateTime.now());
            engTracking.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(engTracking);

            // Create marketing task
            Task marketingTask = new Task();
            marketingTask.setTitle("Marketing Task");
            marketingTask.setOwnerId(marketingUser.getId());
            marketingTask.setProjectId(testProject.getId());
            marketingTask.setStatus(Status.COMPLETED);
            marketingTask.setDeleteInd(false);
            marketingTask.setTaskType(TaskType.FEATURE);
            marketingTask.setCreatedAt(OffsetDateTime.now());
            marketingTask.setCreatedBy(marketingUser.getId());
            marketingTask = entityManager.persistAndFlush(marketingTask);

            TaskTimeTracking mktTracking = new TaskTimeTracking();
            mktTracking.setTaskId(marketingTask.getId());
            mktTracking.setUserId(marketingUser.getId());
            mktTracking.setStartedAt(OffsetDateTime.now().minusHours(3));
            mktTracking.setCompletedAt(OffsetDateTime.now());
            mktTracking.setTotalHours(new BigDecimal("3.00"));
            entityManager.persistAndFlush(mktTracking);

            // Act
            List<Object[]> results = taskTimeTrackingRepository.getHoursByDepartment(
                "",
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert
            assertThat(results).hasSize(2);
            assertThat(results).anyMatch(row -> "Engineering".equals(row[0]));
            assertThat(results).anyMatch(row -> "Marketing".equals(row[0]));
        }

        @Test
        @DisplayName("Should filter by department when specified")
        void testGetHoursByDepartment_FiltersSpecificDepartment() {
            // Arrange - Create tasks for both departments
            TaskTimeTracking engTracking = new TaskTimeTracking();
            engTracking.setTaskId(testTask.getId());
            engTracking.setUserId(engineeringUser.getId());
            engTracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            engTracking.setCompletedAt(OffsetDateTime.now());
            engTracking.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(engTracking);

            Task marketingTask = new Task();
            marketingTask.setTitle("Marketing Task");
            marketingTask.setOwnerId(marketingUser.getId());
            marketingTask.setProjectId(testProject.getId());
            marketingTask.setStatus(Status.COMPLETED);
            marketingTask.setDeleteInd(false);
            marketingTask.setTaskType(TaskType.FEATURE);
            marketingTask.setCreatedAt(OffsetDateTime.now());
            marketingTask.setCreatedBy(marketingUser.getId());
            marketingTask = entityManager.persistAndFlush(marketingTask);

            TaskTimeTracking mktTracking = new TaskTimeTracking();
            mktTracking.setTaskId(marketingTask.getId());
            mktTracking.setUserId(marketingUser.getId());
            mktTracking.setStartedAt(OffsetDateTime.now().minusHours(3));
            mktTracking.setCompletedAt(OffsetDateTime.now());
            mktTracking.setTotalHours(new BigDecimal("3.00"));
            entityManager.persistAndFlush(mktTracking);

            // Act - Filter by Engineering only
            List<Object[]> results = taskTimeTrackingRepository.getHoursByDepartment(
                "Engineering",
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should only return Engineering
            assertThat(results).hasSize(1);
            assertThat(results.get(0)[0]).isEqualTo("Engineering");
        }
    }

    @Nested
    @DisplayName("getHoursByProject Tests")
    class GetHoursByProjectTests {

        @Test
        @DisplayName("Should include in-progress hours in project totals")
        void testGetHoursByProject_IncludesInProgressHours() {
            // Arrange - Completed task
            TaskTimeTracking completedTracking = new TaskTimeTracking();
            completedTracking.setTaskId(testTask.getId());
            completedTracking.setUserId(engineeringUser.getId());
            completedTracking.setStartedAt(OffsetDateTime.now().minusHours(3));
            completedTracking.setCompletedAt(OffsetDateTime.now());
            completedTracking.setTotalHours(new BigDecimal("3.00"));
            entityManager.persistAndFlush(completedTracking);

            // In-progress task
            Task inProgressTask = new Task();
            inProgressTask.setTitle("In Progress Task");
            inProgressTask.setOwnerId(engineeringUser.getId());
            inProgressTask.setProjectId(testProject.getId());
            inProgressTask.setStatus(Status.IN_PROGRESS);
            inProgressTask.setDeleteInd(false);
            inProgressTask.setTaskType(TaskType.FEATURE);
            inProgressTask.setCreatedAt(OffsetDateTime.now());
            inProgressTask.setCreatedBy(engineeringUser.getId());
            inProgressTask = entityManager.persistAndFlush(inProgressTask);

            TaskTimeTracking inProgressTracking = new TaskTimeTracking();
            inProgressTracking.setTaskId(inProgressTask.getId());
            inProgressTracking.setUserId(engineeringUser.getId());
            inProgressTracking.setStartedAt(OffsetDateTime.now().minusMinutes(30));
            inProgressTracking.setCompletedAt(null);
            inProgressTracking.setTotalHours(null);
            entityManager.persistAndFlush(inProgressTracking);

            // Act
            List<Object[]> results = taskTimeTrackingRepository.getHoursByProject(
                "",
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert
            assertThat(results).isNotEmpty();
            Object[] projectResult = results.stream()
                .filter(row -> "Test Project".equals(row[0]))
                .findFirst()
                .orElse(null);

            assertThat(projectResult).isNotNull();
            BigDecimal totalHours = (BigDecimal) projectResult[1];
            assertThat(totalHours).isGreaterThan(new BigDecimal("3.45")); // ~3.5 hours (3 + 0.5)
            assertThat(totalHours).isLessThan(new BigDecimal("3.55"));
        }

        @Test
        @DisplayName("Should filter by project IDs")
        void testGetHoursByProject_FiltersByProjectIds() {
            // Create second project
            Project project2 = new Project();
            project2.setName("Second Project");
            project2.setOwnerId(engineeringUser.getId());
            project2.setDeleteInd(false);
            project2.setCreatedAt(OffsetDateTime.now());
            project2.setUpdatedAt(OffsetDateTime.now());
            project2.setCreatedBy(engineeringUser.getId());
            project2.setUpdatedBy(engineeringUser.getId());
            project2 = entityManager.persistAndFlush(project2);

            // Task in project 1
            TaskTimeTracking tracking1 = new TaskTimeTracking();
            tracking1.setTaskId(testTask.getId());
            tracking1.setUserId(engineeringUser.getId());
            tracking1.setStartedAt(OffsetDateTime.now().minusHours(2));
            tracking1.setCompletedAt(OffsetDateTime.now());
            tracking1.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(tracking1);

            // Task in project 2
            Task task2 = new Task();
            task2.setTitle("Task in Project 2");
            task2.setOwnerId(engineeringUser.getId());
            task2.setProjectId(project2.getId());
            task2.setStatus(Status.COMPLETED);
            task2.setDeleteInd(false);
            task2.setTaskType(TaskType.FEATURE);
            task2.setCreatedAt(OffsetDateTime.now());
            task2.setCreatedBy(engineeringUser.getId());
            task2 = entityManager.persistAndFlush(task2);

            TaskTimeTracking tracking2 = new TaskTimeTracking();
            tracking2.setTaskId(task2.getId());
            tracking2.setUserId(engineeringUser.getId());
            tracking2.setStartedAt(OffsetDateTime.now().minusHours(1));
            tracking2.setCompletedAt(OffsetDateTime.now());
            tracking2.setTotalHours(new BigDecimal("1.00"));
            entityManager.persistAndFlush(tracking2);

            // Act - Filter by project 1 only
            List<Object[]> results = taskTimeTrackingRepository.getHoursByProject(
                "",
                Collections.singletonList(testProject.getId()),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should only include project 1
            assertThat(results).hasSize(1);
            assertThat(results.get(0)[0]).isEqualTo("Test Project");
            BigDecimal hours = (BigDecimal) results.get(0)[1];
            assertThat(hours).isEqualByComparingTo(new BigDecimal("2.00"));
        }
    }

    @Nested
    @DisplayName("getProjectDetails Tests")
    class GetProjectDetailsTests {

        @Test
        @DisplayName("Should include in-progress hours in project details")
        void testGetProjectDetails_IncludesInProgressHours() {
            // Arrange - Completed task
            testTask.setStatus(Status.COMPLETED);
            entityManager.persistAndFlush(testTask);

            TaskTimeTracking completedTracking = new TaskTimeTracking();
            completedTracking.setTaskId(testTask.getId());
            completedTracking.setUserId(engineeringUser.getId());
            completedTracking.setStartedAt(OffsetDateTime.now().minusHours(4));
            completedTracking.setCompletedAt(OffsetDateTime.now().minusHours(2));
            completedTracking.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(completedTracking);

            // In-progress task
            Task inProgressTask = new Task();
            inProgressTask.setTitle("In Progress Task");
            inProgressTask.setOwnerId(engineeringUser.getId());
            inProgressTask.setProjectId(testProject.getId());
            inProgressTask.setStatus(Status.IN_PROGRESS);
            inProgressTask.setDeleteInd(false);
            inProgressTask.setTaskType(TaskType.FEATURE);
            inProgressTask.setCreatedAt(OffsetDateTime.now());
            inProgressTask.setCreatedBy(engineeringUser.getId());
            inProgressTask = entityManager.persistAndFlush(inProgressTask);

            TaskTimeTracking inProgressTracking = new TaskTimeTracking();
            inProgressTracking.setTaskId(inProgressTask.getId());
            inProgressTracking.setUserId(engineeringUser.getId());
            inProgressTracking.setStartedAt(OffsetDateTime.now().minusHours(1));
            inProgressTracking.setCompletedAt(null);
            inProgressTracking.setTotalHours(null);
            entityManager.persistAndFlush(inProgressTracking);

            // Act
            List<Object[]> results = taskTimeTrackingRepository.getProjectDetails(
                "",
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert
            assertThat(results).isNotEmpty();
            Object[] projectDetail = results.get(0);
            
            String projectName = (String) projectDetail[0];
            String department = (String) projectDetail[1];
            BigDecimal totalHours = (BigDecimal) projectDetail[2];
            Long completedCount = (Long) projectDetail[3];
            Long inProgressCount = (Long) projectDetail[4];

            assertThat(projectName).isEqualTo("Test Project");
            assertThat(department).isEqualTo("Engineering");
            assertThat(totalHours).isGreaterThan(new BigDecimal("2.95")); // ~3 hours (2 + 1)
            assertThat(totalHours).isLessThan(new BigDecimal("3.05"));
            assertThat(completedCount).isEqualTo(1L);
            assertThat(inProgressCount).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return correct task counts by status")
        void testGetProjectDetails_CorrectTaskCounts() {
            // Arrange - Mark test task as completed
            testTask.setStatus(Status.COMPLETED);
            entityManager.persistAndFlush(testTask);

            TaskTimeTracking tracking1 = new TaskTimeTracking();
            tracking1.setTaskId(testTask.getId());
            tracking1.setUserId(engineeringUser.getId());
            tracking1.setStartedAt(OffsetDateTime.now().minusHours(2));
            tracking1.setCompletedAt(OffsetDateTime.now());
            tracking1.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(tracking1);

            // Create multiple in-progress tasks
            for (int i = 0; i < 3; i++) {
                Task inProgressTask = new Task();
                inProgressTask.setTitle("In Progress Task " + i);
                inProgressTask.setOwnerId(engineeringUser.getId());
                inProgressTask.setProjectId(testProject.getId());
                inProgressTask.setStatus(Status.IN_PROGRESS);
                inProgressTask.setDeleteInd(false);
                inProgressTask.setTaskType(TaskType.FEATURE);
                inProgressTask.setCreatedAt(OffsetDateTime.now());
                inProgressTask.setCreatedBy(engineeringUser.getId());
                inProgressTask = entityManager.persistAndFlush(inProgressTask);

                TaskTimeTracking tracking = new TaskTimeTracking();
                tracking.setTaskId(inProgressTask.getId());
                tracking.setUserId(engineeringUser.getId());
                tracking.setStartedAt(OffsetDateTime.now().minusHours(1));
                tracking.setCompletedAt(null);
                tracking.setTotalHours(null);
                entityManager.persistAndFlush(tracking);
            }

            // Act
            List<Object[]> results = taskTimeTrackingRepository.getProjectDetails(
                "",
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert
            assertThat(results).isNotEmpty();
            Object[] projectDetail = results.get(0);
            
            Long completedCount = (Long) projectDetail[3];
            Long inProgressCount = (Long) projectDetail[4];

            assertThat(completedCount).isEqualTo(1L);
            assertThat(inProgressCount).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should group by project and department")
        void testGetProjectDetails_GroupsByProjectAndDepartment() {
            // Arrange - Engineering task
            TaskTimeTracking engTracking = new TaskTimeTracking();
            engTracking.setTaskId(testTask.getId());
            engTracking.setUserId(engineeringUser.getId());
            engTracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            engTracking.setCompletedAt(OffsetDateTime.now());
            engTracking.setTotalHours(new BigDecimal("2.00"));
            entityManager.persistAndFlush(engTracking);

            // Marketing user's task in same project
            Task marketingTask = new Task();
            marketingTask.setTitle("Marketing Task");
            marketingTask.setOwnerId(marketingUser.getId());
            marketingTask.setProjectId(testProject.getId());
            marketingTask.setStatus(Status.COMPLETED);
            marketingTask.setDeleteInd(false);
            marketingTask.setTaskType(TaskType.FEATURE);
            marketingTask.setCreatedAt(OffsetDateTime.now());
            marketingTask.setCreatedBy(marketingUser.getId());
            marketingTask = entityManager.persistAndFlush(marketingTask);

            TaskTimeTracking mktTracking = new TaskTimeTracking();
            mktTracking.setTaskId(marketingTask.getId());
            mktTracking.setUserId(marketingUser.getId());
            mktTracking.setStartedAt(OffsetDateTime.now().minusHours(3));
            mktTracking.setCompletedAt(OffsetDateTime.now());
            mktTracking.setTotalHours(new BigDecimal("3.00"));
            entityManager.persistAndFlush(mktTracking);

            // Act
            List<Object[]> results = taskTimeTrackingRepository.getProjectDetails(
                "",
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should have 2 rows (same project, different departments)
            assertThat(results).hasSize(2);
            assertThat(results).anyMatch(row -> 
                "Test Project".equals(row[0]) && "Engineering".equals(row[1]));
            assertThat(results).anyMatch(row -> 
                "Test Project".equals(row[0]) && "Marketing".equals(row[1]));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle tasks with null completedAt in date range")
        void testGetHoursByDepartment_HandlesNullCompletedAt() {
            // Arrange - In-progress task started within date range
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(testTask.getId());
            tracking.setUserId(engineeringUser.getId());
            tracking.setStartedAt(OffsetDateTime.now().minusHours(1));
            tracking.setCompletedAt(null);
            tracking.setTotalHours(null);
            entityManager.persistAndFlush(tracking);

            // Act
            List<Object[]> results = taskTimeTrackingRepository.getHoursByDepartment(
                "",
                null,
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(1)
            );

            // Assert - Should include the in-progress task
            assertThat(results).isNotEmpty();
            BigDecimal totalHours = (BigDecimal) results.get(0)[1];
            assertThat(totalHours).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should return empty list when no data matches filters")
        void testGetHoursByProject_EmptyResultWhenNoMatch() {
            // Act - Query with date range that has no data
            List<Object[]> results = taskTimeTrackingRepository.getHoursByProject(
                "",
                null,
                LocalDate.now().minusDays(30),
                LocalDate.now().minusDays(20)
            );

            // Assert
            assertThat(results).isEmpty();
        }
    }
}

