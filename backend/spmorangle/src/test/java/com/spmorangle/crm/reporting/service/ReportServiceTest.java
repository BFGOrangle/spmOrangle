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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportingRepository reportingRepository;

    @Mock
    private TaskTimeTrackingRepository taskTimeTrackingRepository;

    @Mock
    private UserRepository userRepository;

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
    void testGenerateTaskSummaryReport_ManagerUser_RestrictedToDepartment() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));

        ReportFilterDto filters = ReportFilterDto.builder()
            .department("HR") // Manager tries to access HR department
            .build();

        // Act & Assert - Should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.generateTaskSummaryReport(filters, 2L);
        });

        assertTrue(exception.getMessage().contains("Access denied: Managers can only view reports for their own department"));
        assertTrue(exception.getMessage().contains("Engineering"));
    }

    @Test
    void testGenerateTaskSummaryReport_StaffUser_AccessDenied() {
        // Arrange
        when(userRepository.findById(3L)).thenReturn(Optional.of(staffUser));

        ReportFilterDto filters = ReportFilterDto.builder()
            .department("Engineering")
            .build();

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            reportService.generateTaskSummaryReport(filters, 3L);
        });
        
        assertEquals("Access denied: Staff users cannot access reports", exception.getMessage());
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
    void testGetAvailableDepartments_ManagerUser_OnlyOwnDepartment() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));
        when(reportingRepository.getAllDepartments())
            .thenReturn(Arrays.asList("HR", "Engineering", "Marketing"));

        // Act
        List<String> result = reportService.getAvailableDepartments(2L);

        // Assert
        assertEquals(1, result.size());
        assertEquals("Engineering", result.get(0));
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
        void testGenerateStaffBreakdown_ManagerUser_RestrictedToOwnDepartment() {
            // Scenario: Manager of Engineering dept, no filters
            // Expected: Only Engineering staff (even if requesting empty department)
            
            // Arrange
            when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));
            
            List<Object[]> engineeringStaff = Arrays.asList(
                new Object[]{4L, "Engineering User 1", "Engineering"},
                new Object[]{5L, "Engineering User 2", "Engineering"}
            );
            
            when(reportingRepository.getUsersForStaffBreakdown("Engineering", null))
                .thenReturn(engineeringStaff);
            
            when(reportingRepository.getTaskCountsByStatusForUser(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());
            
            when(reportingRepository.getLoggedHoursForUser(any(), any(), any(), any()))
                .thenReturn(BigDecimal.ZERO);
            
            ReportFilterDto filters = ReportFilterDto.builder()
                .department("")
                .projectIds(null)
                .build();
            
            // Act
            List<StaffBreakdownDto> result = reportService.generateStaffBreakdown(filters, 2L);
            
            // Assert
            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(s -> "Engineering".equals(s.getDepartment())));
        }

        @Test
        void testGenerateStaffBreakdown_ManagerUser_WithProjectFilter_ReturnsDeptAndProjectMembers() {
            // Scenario: Manager of Engineering, project filter = [100, 101]
            // Expected: Engineering staff who are also project members
            
            // Arrange
            when(userRepository.findById(2L)).thenReturn(Optional.of(managerUser));
            
            List<Object[]> filteredStaff = Arrays.<Object[]>asList(
                new Object[]{4L, "Engineering User 1", "Engineering"}
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
            List<StaffBreakdownDto> result = reportService.generateStaffBreakdown(filters, 2L);
            
            // Assert
            assertEquals(1, result.size());
            assertEquals("Engineering", result.get(0).getDepartment());
            assertEquals(4L, result.get(0).getUserId());
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
}

