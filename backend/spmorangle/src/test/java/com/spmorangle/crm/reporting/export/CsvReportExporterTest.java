package com.spmorangle.crm.reporting.export;

import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.StaffBreakdownDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvReportExporterTest {

    private CsvReportExporter csvExporter;

    @BeforeEach
    void setUp() {
        csvExporter = new CsvReportExporter();
    }

    @Test
    void testGetContentType() {
        assertEquals("text/csv", csvExporter.getContentType());
    }

    @Test
    void testGetFileExtension() {
        assertEquals("csv", csvExporter.getFileExtension());
    }

    @Test
    void testExportWithCompleteData() {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = csvExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);

        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Verify content contains expected sections
        assertTrue(csvContent.contains("TASK SUMMARY"));
        assertTrue(csvContent.contains("TIME ANALYTICS"));
        assertTrue(csvContent.contains("STAFF BREAKDOWN"));
        assertTrue(csvContent.contains("Report Generated"));
        assertTrue(csvContent.contains("Engineering"));
    }

    @Test
    void testExportWithEmptyStaffBreakdown() {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        reportData.put("staffBreakdown", new ArrayList<>());
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = csvExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String csvContent = new String(result, StandardCharsets.UTF_8);
        assertFalse(csvContent.contains("STAFF BREAKDOWN"));
    }

    @Test
    void testExportWithNullTimeAnalytics() {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        reportData.put("timeAnalytics", null);
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = csvExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String csvContent = new String(result, StandardCharsets.UTF_8);
        assertFalse(csvContent.contains("TIME ANALYTICS"));
    }

    @Test
    void testExportWithSpecialCharacters() {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();

        // Add staff with special characters in name
        List<StaffBreakdownDto> staffBreakdown = new ArrayList<>();
        StaffBreakdownDto staff = StaffBreakdownDto.builder()
                .userId(1L)
                .userName("O'Brien, Patrick")
                .department("Engineering & Design")
                .todoTasks(5L)
                .inProgressTasks(3L)
                .completedTasks(10L)
                .blockedTasks(0L)
                .loggedHours(new BigDecimal("45.5"))
                .build();
        staffBreakdown.add(staff);
        reportData.put("staffBreakdown", staffBreakdown);

        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = csvExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String csvContent = new String(result, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("O'Brien"));
    }

    @Test
    void testExportWithDepartmentBreakdown() {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        TaskSummaryReportDto taskSummary = (TaskSummaryReportDto) reportData.get("taskSummary");

        // Add department breakdown
        Map<String, TaskSummaryReportDto.TaskStatusCounts> deptBreakdown = new HashMap<>();
        TaskSummaryReportDto.TaskStatusCounts counts = TaskSummaryReportDto.TaskStatusCounts.builder()
                .total(50L)
                .completed(30L)
                .inProgress(10L)
                .todo(8L)
                .blocked(2L)
                .build();
        deptBreakdown.put("Engineering", counts);
        taskSummary.setDepartmentBreakdown(deptBreakdown);

        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = csvExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String csvContent = new String(result, StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("TASKS BY DEPARTMENT"));
    }

    @Test
    void testExportFormatsNumbersCorrectly() {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = csvExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String csvContent = new String(result, StandardCharsets.UTF_8);

        // Check percentage formatting
        assertTrue(csvContent.contains("%"));

        // Check hours formatting (should have one decimal place)
        assertTrue(csvContent.matches("(?s).*\\d+\\.\\d.*"));
    }

    // Helper methods

    private Map<String, Object> createSampleReportData() {
        Map<String, Object> reportData = new HashMap<>();

        // Task Summary
        TaskSummaryReportDto taskSummary = TaskSummaryReportDto.builder()
                .totalTasks(100L)
                .completedTasks(60L)
                .inProgressTasks(25L)
                .todoTasks(10L)
                .blockedTasks(5L)
                .completedPercentage(60.0)
                .inProgressPercentage(25.0)
                .todoPercentage(10.0)
                .blockedPercentage(5.0)
                .build();

        // Time Analytics
        TimeAnalyticsReportDto timeAnalytics = TimeAnalyticsReportDto.builder()
                .totalHours(new BigDecimal("450.5"))
                .hoursByDepartment(Map.of(
                        "Engineering", new BigDecimal("300.0"),
                        "Design", new BigDecimal("150.5")))
                .hoursByProject(Map.of(
                        "Project A", new BigDecimal("200.0"),
                        "Project B", new BigDecimal("250.5")))
                .build();

        // Staff Breakdown
        List<StaffBreakdownDto> staffBreakdown = new ArrayList<>();
        StaffBreakdownDto staff1 = StaffBreakdownDto.builder()
                .userId(1L)
                .userName("John Doe")
                .department("Engineering")
                .todoTasks(5L)
                .inProgressTasks(3L)
                .completedTasks(20L)
                .blockedTasks(1L)
                .loggedHours(new BigDecimal("120.5"))
                .build();
        StaffBreakdownDto staff2 = StaffBreakdownDto.builder()
                .userId(2L)
                .userName("Jane Smith")
                .department("Design")
                .todoTasks(5L)
                .inProgressTasks(2L)
                .completedTasks(40L)
                .blockedTasks(4L)
                .loggedHours(new BigDecimal("150.0"))
                .build();
        staffBreakdown.add(staff1);
        staffBreakdown.add(staff2);

        reportData.put("taskSummary", taskSummary);
        reportData.put("timeAnalytics", timeAnalytics);
        reportData.put("staffBreakdown", staffBreakdown);
        reportData.put("generatedAt", OffsetDateTime.now());

        return reportData;
    }

    private ReportFilterDto createSampleFilters() {
        return ReportFilterDto.builder()
                .department("Engineering")
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .timeRange(ReportFilterDto.TimeRange.YEARLY)
                .exportFormat(ReportFilterDto.ExportFormat.CSV)
                .build();
    }
}
