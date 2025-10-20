package com.spmorangle.crm.reporting.controller;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import com.spmorangle.crm.reporting.export.ReportExportService;
import com.spmorangle.crm.reporting.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportControllerSimpleTest {

    @Mock
    private ReportService reportService;

    @Mock
    private UserContextService userContextService;

    @Mock
    private ReportExportService reportExportService;

    @InjectMocks
    private ReportController reportController;

    private User hrUser;
    private TaskSummaryReportDto mockTaskSummary;
    private TimeAnalyticsReportDto mockTimeAnalytics;

    @BeforeEach
    void setUp() {
        hrUser = new User();
        hrUser.setEmail("john@test.com");
        hrUser.setRoleType(UserType.HR.getCode());
        hrUser.setDepartment("HR");
        
        mockTaskSummary = new TaskSummaryReportDto();
        mockTimeAnalytics = new TimeAnalyticsReportDto();
    }

    @Test
    void testGetTaskSummaryReport_Success() {
        // Arrange
        when(userContextService.getRequestingUser()).thenReturn(hrUser);
        when(reportService.generateTaskSummaryReport(any(ReportFilterDto.class), any()))
                .thenReturn(mockTaskSummary);

        // Act
        ResponseEntity<TaskSummaryReportDto> response = reportController.getTaskSummaryReport(
                "Engineering", null, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), null
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reportService, times(1)).generateTaskSummaryReport(any(), any());
    }

    @Test
    void testGetTimeAnalyticsReport_Success() {
        // Arrange
        when(userContextService.getRequestingUser()).thenReturn(hrUser);
        when(reportService.generateTimeAnalyticsReport(any(ReportFilterDto.class), any()))
                .thenReturn(mockTimeAnalytics);

        // Act
        ResponseEntity<TimeAnalyticsReportDto> response = reportController.getTimeAnalyticsReport(
                "Engineering", null, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), null
        );

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(reportService, times(1)).generateTimeAnalyticsReport(any(), any());
    }

    @Test
    void testGenerateReport_Success() {
        // Arrange
        when(userContextService.getRequestingUser()).thenReturn(hrUser);
        when(reportService.generateTaskSummaryReport(any(), any())).thenReturn(mockTaskSummary);
        when(reportService.generateTimeAnalyticsReport(any(), any())).thenReturn(mockTimeAnalytics);
        when(reportService.generateTimeSeriesData(any(), any())).thenReturn(null);
        
        when(reportExportService.exportReport(any(), any())).thenReturn(ResponseEntity.ok().build());

        ReportFilterDto filters = new ReportFilterDto();

        // Act
        ResponseEntity<?> response = reportController.generateReport(filters);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(reportService, times(1)).generateTaskSummaryReport(any(), any());
        verify(reportService, times(1)).generateTimeAnalyticsReport(any(), any());
    }

    @Test
    void testGetAvailableDepartments_Success() {
        // Arrange
        when(userContextService.getRequestingUser()).thenReturn(hrUser);
        when(reportService.getAvailableDepartments(any()))
                .thenReturn(Arrays.asList("HR", "Engineering", "Marketing"));

        // Act
        ResponseEntity<List<String>> response = reportController.getAvailableDepartments();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertTrue(response.getBody().contains("HR"));
    }

    @Test
    void testGetAvailableProjects_Success() {
        // Arrange
        when(userContextService.getRequestingUser()).thenReturn(hrUser);
        
        // Service returns List<Object[]> where each array is [id, name]
        Object[] project1 = {1L, "Project A"};
        List<Object[]> projectList = new java.util.ArrayList<>();
        projectList.add(project1);
        
        when(reportService.getAvailableProjects(any(), any()))
                .thenReturn(projectList);

        // Act
        ResponseEntity<Map<String, Object>> response = reportController.getAvailableProjects("Engineering");

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> projects = (List<Map<String, Object>>) response.getBody().get("projects");
        assertEquals(1, projects.size());
        assertEquals(1L, projects.get(0).get("id"));
        assertEquals("Project A", projects.get(0).get("name"));
    }
}

