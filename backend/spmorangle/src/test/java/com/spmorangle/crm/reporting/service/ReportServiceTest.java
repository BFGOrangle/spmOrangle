package com.spmorangle.crm.reporting.service;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.StaffBreakdownDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.repository.ReportingRepository;
import com.spmorangle.crm.reporting.repository.TaskTimeTrackingRepository;
import com.spmorangle.crm.reporting.service.impl.ReportServiceImpl;
import com.spmorangle.crm.taskmanagement.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.spmorangle.crm.reporting.model.TaskTimeTracking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportingRepository reportingRepository;

    @Mock
    private TaskTimeTrackingRepository taskTimeTrackingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private com.spmorangle.crm.taskmanagement.repository.TaskRepository taskRepository;

    @Mock
    private com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository taskAssigneeRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User hrUser;
    private User managerUser;
    private User staffUser;

    @BeforeEach
    void setUp() {
        hrUser = new User();
        hrUser.setId(1L);
        hrUser.setRoleType(UserType.HR.getCode());
        hrUser.setDepartment("HR");

        managerUser = new User();
        managerUser.setId(2L);
        managerUser.setRoleType(UserType.MANAGER.getCode());
        managerUser.setDepartment("Engineering");

        staffUser = new User();
        staffUser.setId(3L);
        staffUser.setRoleType(UserType.STAFF.getCode());
        staffUser.setDepartment("Engineering");
    }

    @Test
    void testGenerateTaskSummaryReport_HRUser_Success() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
        
        List<Object[]> mockResults = Arrays.asList(
            new Object[]{Status.COMPLETED, 10L},
            new Object[]{Status.IN_PROGRESS, 5L},
            new Object[]{Status.TODO, 8L},
            new Object[]{Status.BLOCKED, 2L}
        );
        
        when(reportingRepository.getTaskCountsByStatus(any(), any(), any()))
            .thenReturn(mockResults);

        ReportFilterDto filters = ReportFilterDto.builder()
            .department("Engineering")
            .build();

        // Act
        TaskSummaryReportDto result = reportService.generateTaskSummaryReport(filters, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(25L, result.getTotalTasks());
        assertEquals(10L, result.getCompletedTasks());
        assertEquals(5L, result.getInProgressTasks());
        assertEquals(8L, result.getTodoTasks());
        assertEquals(2L, result.getBlockedTasks());
        assertEquals(40.0, result.getCompletedPercentage());
        assertEquals(20.0, result.getInProgressPercentage());
        assertEquals(32.0, result.getTodoPercentage());
        assertEquals(8.0, result.getBlockedPercentage());
    }

    @Test
    void testGenerateTaskSummaryReport_ManagerUser_AccessDenied() {
        // Arrange - Manager users should be denied access to reports (HR-only feature)
        when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));

        ReportFilterDto filters = ReportFilterDto.builder()
            .department("HR") // Manager tries to access any department
            .build();

        // Act & Assert - Should throw exception denying access
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.generateTaskSummaryReport(filters, 2L);
        });

        assertEquals("Access denied: Only HR users can access reports", exception.getMessage());
    }

    @Test
    void testGenerateTaskSummaryReport_StaffUser_AccessDenied() {
        // Arrange - Staff users should be denied access to reports (HR-only feature)
        when(userRepository.findById(3L)).thenReturn(Optional.of(staffUser));

        ReportFilterDto filters = ReportFilterDto.builder()
            .department("Engineering")
            .build();

        // Act & Assert - Should throw exception denying access
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.generateTaskSummaryReport(filters, 3L);
        });

        assertEquals("Access denied: Only HR users can access reports", exception.getMessage());
    }

    @Test
    void testGetAvailableDepartments_HRUser_AllDepartments() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
        when(reportingRepository.getAllDepartments())
            .thenReturn(Arrays.asList("HR", "Engineering", "Marketing"));

        // Act
        List<String> result = reportService.getAvailableDepartments(1L);

        // Assert
        assertEquals(3, result.size());
        assertTrue(result.contains("HR"));
        assertTrue(result.contains("Engineering"));
        assertTrue(result.contains("Marketing"));
    }

    @Test
    void testGetAvailableDepartments_ManagerUser_EmptyList() {
        // Arrange - Manager users cannot access reports (HR-only feature)
        when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));
        when(reportingRepository.getAllDepartments())
            .thenReturn(Arrays.asList("HR", "Engineering", "Marketing"));

        // Act
        List<String> result = reportService.getAvailableDepartments(2L);

        // Assert - Should return empty list (no access to reports)
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAvailableDepartments_StaffUser_EmptyList() {
        // Arrange
        when(userRepository.findById(3L)).thenReturn(Optional.of(staffUser));

        // Act
        List<String> result = reportService.getAvailableDepartments(3L);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Nested
    class StaffBreakdownTests {

        @Test
        void testGenerateStaffBreakdown_HRUser_NoFilters_ReturnsAllStaff() {
            // Scenario: HR user, no filters (company-wide data)
            // Expected: All 11 staff from all departments
            
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
            
            List<Object[]> allStaff = Arrays.asList(
                new Object[]{9L, "Ian Thompson", "Software"},
                new Object[]{10L, "Julia Adams", "Software"},
                new Object[]{11L, "OrangleManagerTestUser", "Software"},
                new Object[]{1L, "HR User 1", "HR"},
                new Object[]{2L, "HR User 2", "HR"},
                new Object[]{4L, "Engineering User 1", "Engineering"},
                new Object[]{5L, "Engineering User 2", "Engineering"},
                new Object[]{6L, "Engineering User 3", "Engineering"},
                new Object[]{7L, "Marketing User 1", "Marketing"},
                new Object[]{8L, "Marketing User 2", "Marketing"},
                new Object[]{12L, "Sales User 1", "Sales"}
            );
            
            when(reportingRepository.getUsersForStaffBreakdown("", null))
                .thenReturn(allStaff);
            
            // Mock task counts (return empty for all)
            when(reportingRepository.getTaskCountsByStatusForUser(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
            
            // Mock logged hours
            when(reportingRepository.getLoggedHoursForUser(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
            
            ReportFilterDto filters = ReportFilterDto.builder()
                .department("")
                .projectIds(null)
                .build();
            
            // Act
            List<StaffBreakdownDto> result = reportService.generateStaffBreakdown(filters, 1L);
            
            // Assert
            assertEquals(11, result.size());
            assertTrue(result.stream().anyMatch(s -> "Ian Thompson".equals(s.getUserName())));
            assertTrue(result.stream().anyMatch(s -> "HR User 1".equals(s.getUserName())));
            assertTrue(result.stream().anyMatch(s -> "Marketing User 1".equals(s.getUserName())));
        }

        @Test
        void testGenerateStaffBreakdown_HRUser_DepartmentFilter_ReturnsDepartmentStaff() {
            // Scenario: HR user, department filter = "Engineering"
            // Expected: Only Engineering staff
            
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
            
            List<Object[]> engineeringStaff = Arrays.asList(
                new Object[]{4L, "Engineering User 1", "Engineering"},
                new Object[]{5L, "Engineering User 2", "Engineering"},
                new Object[]{6L, "Engineering User 3", "Engineering"}
            );
            
            when(reportingRepository.getUsersForStaffBreakdown("Engineering", null))
                .thenReturn(engineeringStaff);
            
            when(reportingRepository.getTaskCountsByStatusForUser(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
            
            when(reportingRepository.getLoggedHoursForUser(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
            
            ReportFilterDto filters = ReportFilterDto.builder()
                .department("Engineering")
                .projectIds(null)
                .build();
            
            // Act
            List<StaffBreakdownDto> result = reportService.generateStaffBreakdown(filters, 1L);
            
            // Assert
            assertEquals(3, result.size());
            assertTrue(result.stream().allMatch(s -> "Engineering".equals(s.getDepartment())));
        }

        @Test
        void testGenerateStaffBreakdown_HRUser_ProjectFilter_ReturnsProjectMembers() {
            // Scenario: HR user, project filter = [100, 101]
            // Expected: Only project members of these projects
            
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
            
            List<Object[]> projectMembers = Arrays.asList(
                new Object[]{9L, "Ian Thompson", "Software"},
                new Object[]{10L, "Julia Adams", "Software"},
                new Object[]{4L, "Engineering User 1", "Engineering"}
            );
            
            when(reportingRepository.getUsersForStaffBreakdown("", Arrays.asList(100L, 101L)))
                .thenReturn(projectMembers);
            
            when(reportingRepository.getTaskCountsByStatusForUser(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
            
            when(reportingRepository.getLoggedHoursForUser(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
            
            ReportFilterDto filters = ReportFilterDto.builder()
                .department(null)
                .projectIds(Arrays.asList(100L, 101L))
                .build();
            
            // Act
            List<StaffBreakdownDto> result = reportService.generateStaffBreakdown(filters, 1L);
            
            // Assert
            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(s -> 9L == s.getUserId()));
            assertTrue(result.stream().anyMatch(s -> 10L == s.getUserId()));
            assertTrue(result.stream().anyMatch(s -> 4L == s.getUserId()));
        }

        @Test
        void testGenerateStaffBreakdown_HRUser_BothFilters_ReturnsDepartmentAndProjectMembers() {
            // Scenario: HR user, department = "Engineering" AND projects [100, 101]
            // Expected: Engineering staff who are also members of projects 100 or 101
            
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
            
            List<Object[]> filteredStaff = Arrays.asList(
                new Object[]{4L, "Engineering User 1", "Engineering"},
                new Object[]{5L, "Engineering User 2", "Engineering"}
            );
            
            when(reportingRepository.getUsersForStaffBreakdown("Engineering", Arrays.asList(100L, 101L)))
                .thenReturn(filteredStaff);
            
            when(reportingRepository.getTaskCountsByStatusForUser(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
            
            when(reportingRepository.getLoggedHoursForUser(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
            
            ReportFilterDto filters = ReportFilterDto.builder()
                .department("Engineering")
                .projectIds(Arrays.asList(100L, 101L))
                .build();
            
            // Act
            List<StaffBreakdownDto> result = reportService.generateStaffBreakdown(filters, 1L);
            
            // Assert
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(s -> "Engineering".equals(s.getDepartment())));
            assertTrue(result.stream().allMatch(s -> s.getUserId() >= 4L && s.getUserId() <= 5L));
        }

        @Test
        void testGenerateStaffBreakdown_ManagerUser_AccessDenied() {
            // Scenario: Manager users cannot access staff breakdown reports (HR-only feature)

            // Arrange
            when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));

            ReportFilterDto filters = ReportFilterDto.builder()
                .department("")
                .projectIds(null)
                .build();

            // Act & Assert - Should throw exception denying access
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                reportService.generateStaffBreakdown(filters, 2L);
            });

            assertEquals("Access denied: Only HR users can access reports", exception.getMessage());
        }

        @Test
        void testGenerateStaffBreakdown_ManagerUser_WithProjectFilter_AccessDenied() {
            // Scenario: Manager users cannot access staff breakdown reports (HR-only feature)

            // Arrange
            when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));

            ReportFilterDto filters = ReportFilterDto.builder()
                .department("Engineering")
                .projectIds(Arrays.asList(100L, 101L))
                .build();

            // Act & Assert - Should throw exception denying access
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                reportService.generateStaffBreakdown(filters, 2L);
            });

            assertEquals("Access denied: Only HR users can access reports", exception.getMessage());
        }

        @Test
        void testGenerateStaffBreakdown_IncludesUsersWithZeroActivity() {
            // Scenario: HR user, staff should be included even with zero tasks/hours
            // Expected: All staff included with zero task counts and zero logged hours
            
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
            
            List<Object[]> allStaff = Arrays.asList(
                new Object[]{9L, "Ian Thompson", "Software"},
                new Object[]{10L, "Julia Adams", "Software"}
            );
            
            when(reportingRepository.getUsersForStaffBreakdown("", null))
                .thenReturn(allStaff);
            
            when(reportingRepository.getTaskCountsByStatusForUser(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
            
            when(reportingRepository.getLoggedHoursForUser(any(), any(), any(), any()))
                .thenReturn(null); // Simulate null from DB
            
            ReportFilterDto filters = ReportFilterDto.builder()
                .department("")
                .projectIds(null)
                .build();
            
            // Act
            List<StaffBreakdownDto> result = reportService.generateStaffBreakdown(filters, 1L);
            
            // Assert
            assertEquals(2, result.size());
            result.forEach(staff -> {
                assertEquals(0L, staff.getTodoTasks());
                assertEquals(0L, staff.getInProgressTasks());
                assertEquals(0L, staff.getCompletedTasks());
                assertEquals(0L, staff.getBlockedTasks());
                assertEquals(BigDecimal.ZERO, staff.getLoggedHours());
            });
        }

        @Test
        void testGenerateStaffBreakdown_WithTaskAndHourData() {
            // Scenario: Staff with actual task and hour data
            // Expected: Correct aggregation of tasks and hours
            
            // Arrange
            when(userRepository.findById(1L)).thenReturn(Optional.of(hrUser));
            
            List<Object[]> staff = Arrays.<Object[]>asList(
                new Object[]{11L, "OrangleManagerTestUser", "Software"}
            );
            
            when(reportingRepository.getUsersForStaffBreakdown("", null))
                .thenReturn(staff);
            
            List<Object[]> taskCounts = Arrays.asList(
                new Object[]{Status.COMPLETED, 4L},
                new Object[]{Status.IN_PROGRESS, 5L},
                new Object[]{Status.TODO, 3L},
                new Object[]{Status.BLOCKED, 2L}
            );
            
            when(reportingRepository.getTaskCountsByStatusForUser(eq(11L), isNull(), any(), any()))
                .thenReturn(taskCounts);
            
            when(reportingRepository.getLoggedHoursForUser(eq(11L), any(), any(), any()))
                .thenReturn(new BigDecimal("41.02"));
            
            ReportFilterDto filters = ReportFilterDto.builder()
                .department("")
                .projectIds(null)
                .build();
            
            // Act
            List<StaffBreakdownDto> result = reportService.generateStaffBreakdown(filters, 1L);
            
            // Assert
            assertEquals(1, result.size());
            StaffBreakdownDto breakdown = result.get(0);
            assertEquals(11L, breakdown.getUserId());
            assertEquals(4L, breakdown.getCompletedTasks());
            assertEquals(5L, breakdown.getInProgressTasks());
            assertEquals(3L, breakdown.getTodoTasks());
            assertEquals(2L, breakdown.getBlockedTasks());
            assertEquals(new BigDecimal("41.02"), breakdown.getLoggedHours());
        }
    }

    @Nested
    class TimeTrackingTests {

        @Test
        void testStartTimeTracking_CreatesNewTrackingRecord() {
            // Arrange
            Long taskId = 100L;
            Long ownerId = 1L;

            com.spmorangle.crm.taskmanagement.model.Task task = new com.spmorangle.crm.taskmanagement.model.Task();
            task.setId(taskId);
            task.setOwnerId(ownerId);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(taskId)).thenReturn(Collections.emptyList());
            when(taskTimeTrackingRepository.findByTaskIdAndUserId(taskId, ownerId))
                .thenReturn(Optional.empty());

            // Act
            reportService.startTimeTracking(taskId, ownerId);

            // Assert
            verify(taskTimeTrackingRepository).save(argThat(tracking ->
                tracking.getTaskId().equals(taskId) &&
                tracking.getUserId().equals(ownerId) &&
                tracking.getStartedAt() != null &&
                tracking.getCompletedAt() == null &&
                tracking.getTotalHours() == null
            ));
        }

        @Test
        void testStartTimeTracking_CompletedRecordExists_ResetsRecord() {
            // Arrange
            Long taskId = 100L;
            Long ownerId = 1L;

            com.spmorangle.crm.taskmanagement.model.Task task = new com.spmorangle.crm.taskmanagement.model.Task();
            task.setId(taskId);
            task.setOwnerId(ownerId);

            TaskTimeTracking completedTracking = new TaskTimeTracking();
            completedTracking.setTaskId(taskId);
            completedTracking.setUserId(ownerId);
            completedTracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            completedTracking.setCompletedAt(OffsetDateTime.now().minusHours(1));
            completedTracking.setTotalHours(new BigDecimal("1.00"));

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(taskId)).thenReturn(Collections.emptyList());
            when(taskTimeTrackingRepository.findByTaskIdAndUserId(taskId, ownerId))
                .thenReturn(Optional.of(completedTracking));

            // Act
            reportService.startTimeTracking(taskId, ownerId);

            // Assert - Should reset the existing completed record
            verify(taskTimeTrackingRepository).save(argThat(tracking ->
                tracking.getTaskId().equals(taskId) &&
                tracking.getUserId().equals(ownerId) &&
                tracking.getStartedAt() != null &&
                tracking.getCompletedAt() == null &&
                tracking.getTotalHours() == null
            ));
        }

        @Test
        void testStartTimeTracking_ActiveRecordExists_DoesNotModify() {
            // Arrange
            Long taskId = 100L;
            Long ownerId = 1L;

            com.spmorangle.crm.taskmanagement.model.Task task = new com.spmorangle.crm.taskmanagement.model.Task();
            task.setId(taskId);
            task.setOwnerId(ownerId);

            TaskTimeTracking activeTracking = new TaskTimeTracking();
            activeTracking.setTaskId(taskId);
            activeTracking.setUserId(ownerId);
            activeTracking.setStartedAt(OffsetDateTime.now().minusMinutes(30));
            activeTracking.setCompletedAt(null);

            when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
            when(taskAssigneeRepository.findAssigneeIdsByTaskId(taskId)).thenReturn(Collections.emptyList());
            when(taskTimeTrackingRepository.findByTaskIdAndUserId(taskId, ownerId))
                .thenReturn(Optional.of(activeTracking));

            // Act
            reportService.startTimeTracking(taskId, ownerId);

            // Assert - Should not save when record is already active
            verify(taskTimeTrackingRepository, never()).save(any());
        }

        @Test
        void testEndTimeTracking_CalculatesHoursCorrectly() {
            // Arrange
            Long taskId = 100L;
            Long userId = 1L;

            OffsetDateTime startTime = OffsetDateTime.now().minusHours(2).minusMinutes(30);

            TaskTimeTracking activeTracking = new TaskTimeTracking();
            activeTracking.setId(1L);
            activeTracking.setTaskId(taskId);
            activeTracking.setUserId(userId);
            activeTracking.setStartedAt(startTime);
            activeTracking.setCompletedAt(null);

            when(taskTimeTrackingRepository.findByTaskId(taskId))
                .thenReturn(Collections.singletonList(activeTracking));

            // Act
            reportService.endTimeTracking(taskId, userId);

            // Assert
            verify(taskTimeTrackingRepository).save(argThat(tracking -> {
                assertNotNull(tracking.getCompletedAt());
                assertNotNull(tracking.getTotalHours());

                // Should be approximately 2.5 hours
                assertTrue(tracking.getTotalHours().compareTo(new BigDecimal("2.4")) > 0);
                assertTrue(tracking.getTotalHours().compareTo(new BigDecimal("2.6")) < 0);

                return true;
            }));
        }

        @Test
        void testEndTimeTracking_MultipleCollaborators_CalculatesFromEarliestStart() {
            // Arrange
            Long taskId = 100L;
            Long userId = 1L;

            OffsetDateTime earliestStart = OffsetDateTime.now().minusHours(3);
            OffsetDateTime laterStart = OffsetDateTime.now().minusHours(2);

            TaskTimeTracking tracking1 = new TaskTimeTracking();
            tracking1.setId(1L);
            tracking1.setTaskId(taskId);
            tracking1.setUserId(userId);
            tracking1.setStartedAt(earliestStart);
            tracking1.setCompletedAt(null);

            TaskTimeTracking tracking2 = new TaskTimeTracking();
            tracking2.setId(2L);
            tracking2.setTaskId(taskId);
            tracking2.setUserId(2L); // Different user
            tracking2.setStartedAt(laterStart);
            tracking2.setCompletedAt(null);

            when(taskTimeTrackingRepository.findByTaskId(taskId))
                .thenReturn(Arrays.asList(tracking1, tracking2));

            // Act
            reportService.endTimeTracking(taskId, userId);

            // Assert - Should use earliest start time for all collaborators
            verify(taskTimeTrackingRepository, times(2)).save(argThat(tracking -> {
                assertNotNull(tracking.getCompletedAt());
                assertNotNull(tracking.getTotalHours());

                // Both should have the same total hours (from earliest start)
                assertTrue(tracking.getTotalHours().compareTo(new BigDecimal("2.9")) > 0);

                return true;
            }));
        }

        @Test
        void testEndTimeTracking_NoActiveRecords_LogsWarningAndReturns() {
            // Arrange
            Long taskId = 100L;
            Long userId = 1L;

            when(taskTimeTrackingRepository.findByTaskId(taskId))
                .thenReturn(Collections.emptyList());

            // Act
            reportService.endTimeTracking(taskId, userId);

            // Assert - Should not attempt to save
            verify(taskTimeTrackingRepository, never()).save(any());
        }

        @Test
        void testEndTimeTracking_AllRecordsAlreadyCompleted_LogsWarningAndReturns() {
            // Arrange
            Long taskId = 100L;
            Long userId = 1L;

            TaskTimeTracking completedTracking = new TaskTimeTracking();
            completedTracking.setId(1L);
            completedTracking.setTaskId(taskId);
            completedTracking.setUserId(userId);
            completedTracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            completedTracking.setCompletedAt(OffsetDateTime.now().minusHours(1));
            completedTracking.setTotalHours(new BigDecimal("1.00"));

            when(taskTimeTrackingRepository.findByTaskId(taskId))
                .thenReturn(Collections.singletonList(completedTracking));

            // Act
            reportService.endTimeTracking(taskId, userId);

            // Assert - Should not save anything
            verify(taskTimeTrackingRepository, never()).save(any());
        }

        @Test
        void testEndTimeTracking_OnlyUpdatesActiveRecords() {
            // Arrange
            Long taskId = 100L;
            Long userId = 1L;

            TaskTimeTracking activeTracking = new TaskTimeTracking();
            activeTracking.setId(1L);
            activeTracking.setTaskId(taskId);
            activeTracking.setUserId(userId);
            activeTracking.setStartedAt(OffsetDateTime.now().minusHours(2));
            activeTracking.setCompletedAt(null);

            TaskTimeTracking completedTracking = new TaskTimeTracking();
            completedTracking.setId(2L);
            completedTracking.setTaskId(taskId);
            completedTracking.setUserId(userId);
            completedTracking.setStartedAt(OffsetDateTime.now().minusHours(5));
            completedTracking.setCompletedAt(OffsetDateTime.now().minusHours(3));
            completedTracking.setTotalHours(new BigDecimal("2.00"));

            when(taskTimeTrackingRepository.findByTaskId(taskId))
                .thenReturn(Arrays.asList(activeTracking, completedTracking));

            // Act
            reportService.endTimeTracking(taskId, userId);

            // Assert - Should only save the active record
            verify(taskTimeTrackingRepository, times(1)).save(argThat(tracking ->
                tracking.getId().equals(1L) &&
                tracking.getCompletedAt() != null &&
                tracking.getTotalHours() != null
            ));
        }

        @Test
        void testEndTimeTracking_RoundingToTwoDecimalPlaces() {
            // Arrange
            Long taskId = 100L;
            Long userId = 1L;

            // Create a time that will result in a precise calculation
            OffsetDateTime startTime = OffsetDateTime.now().minusMinutes(90); // 1.5 hours

            TaskTimeTracking activeTracking = new TaskTimeTracking();
            activeTracking.setId(1L);
            activeTracking.setTaskId(taskId);
            activeTracking.setUserId(userId);
            activeTracking.setStartedAt(startTime);
            activeTracking.setCompletedAt(null);

            when(taskTimeTrackingRepository.findByTaskId(taskId))
                .thenReturn(Collections.singletonList(activeTracking));

            // Act
            reportService.endTimeTracking(taskId, userId);

            // Assert
            verify(taskTimeTrackingRepository).save(argThat(tracking -> {
                BigDecimal totalHours = tracking.getTotalHours();
                assertNotNull(totalHours);

                // Check that it's rounded to 2 decimal places
                assertTrue(totalHours.scale() <= 2);

                // Should be approximately 1.5 hours
                assertTrue(totalHours.compareTo(new BigDecimal("1.4")) > 0);
                assertTrue(totalHours.compareTo(new BigDecimal("1.6")) < 0);

                return true;
            }));
        }
    }
}

