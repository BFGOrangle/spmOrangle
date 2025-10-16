package com.spmorangle.crm.reporting.service;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.repository.ReportingRepository;
import com.spmorangle.crm.reporting.repository.TaskTimeTrackingRepository;
import com.spmorangle.crm.reporting.service.impl.ReportServiceImpl;
import com.spmorangle.crm.taskmanagement.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        
        List<Object[]> mockResults = Arrays.asList(
            new Object[]{Status.COMPLETED, 5L},
            new Object[]{Status.IN_PROGRESS, 3L}
        );
        
        when(reportingRepository.getTaskCountsByStatus(eq("Engineering"), any(), any()))
            .thenReturn(mockResults);

        ReportFilterDto filters = ReportFilterDto.builder()
            .department("HR") // Manager tries to access HR department
            .build();

        // Act
        TaskSummaryReportDto result = reportService.generateTaskSummaryReport(filters, 2L);

        // Assert
        assertNotNull(result);
        assertEquals(8L, result.getTotalTasks());
        assertEquals(5L, result.getCompletedTasks());
        assertEquals(3L, result.getInProgressTasks());
        assertEquals(0L, result.getTodoTasks());
        assertEquals(0L, result.getBlockedTasks());
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
}

