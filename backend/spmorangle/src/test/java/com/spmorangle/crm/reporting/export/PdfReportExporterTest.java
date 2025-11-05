package com.spmorangle.crm.reporting.export;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.StaffBreakdownDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PdfReportExporterTest {

    private PdfReportExporter pdfExporter;

    @BeforeEach
    void setUp() {
        pdfExporter = new PdfReportExporter();
    }

    @Test
    void testGetContentType() {
        assertEquals("application/pdf", pdfExporter.getContentType());
    }

    @Test
    void testGetFileExtension() {
        assertEquals("pdf", pdfExporter.getFileExtension());
    }

    @Test
    void testExportWithCompleteData() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify it's a valid PDF by reading it
        String pdfContent = extractTextFromPdf(result);

        // Verify content contains expected sections
        assertTrue(pdfContent.contains("Project Management Report"));
        assertTrue(pdfContent.contains("Task Summary"));
        assertTrue(pdfContent.contains("Time Analytics"));
        assertTrue(pdfContent.contains("Staff Breakdown"));
    }

    @Test
    void testExportWithEmptyStaffBreakdown() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        reportData.put("staffBreakdown", new ArrayList<>());
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String pdfContent = extractTextFromPdf(result);
        assertTrue(pdfContent.contains("Task Summary"));
        assertTrue(pdfContent.contains("Time Analytics"));
    }

    @Test
    void testExportWithNullTimeAnalytics() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        reportData.put("timeAnalytics", null);
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String pdfContent = extractTextFromPdf(result);
        assertTrue(pdfContent.contains("Task Summary"));
        assertFalse(pdfContent.contains("Task Status Distribution"));
        assertFalse(pdfContent.contains("Hours by Department"));
    }

    @Test
    void testExportWithNullTaskSummary() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        reportData.put("taskSummary", null);
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String pdfContent = extractTextFromPdf(result);
        assertTrue(pdfContent.contains("Project Management Report"));
        assertFalse(pdfContent.contains("Task Summary"));
    }

    @Test
    void testExportIncludesMetadata() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String pdfContent = extractTextFromPdf(result);

        // Verify metadata is included
        assertTrue(pdfContent.contains("Engineering"));
        assertTrue(pdfContent.contains("2024-01-01"));
        assertTrue(pdfContent.contains("2024-12-31"));
        assertTrue(pdfContent.contains("YEARLY"));
    }

    @Test
    void testExportWithDepartmentBreakdown() throws Exception {
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
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String pdfContent = extractTextFromPdf(result);
        assertTrue(pdfContent.contains("Tasks by Department"));
    }

    @Test
    void testExportWithProjectBreakdown() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        TaskSummaryReportDto taskSummary = (TaskSummaryReportDto) reportData.get("taskSummary");

        // Add project breakdown
        Map<String, TaskSummaryReportDto.TaskStatusCounts> projectBreakdown = new HashMap<>();
        TaskSummaryReportDto.TaskStatusCounts counts = TaskSummaryReportDto.TaskStatusCounts.builder()
                .total(50L)
                .completed(30L)
                .inProgress(10L)
                .todo(8L)
                .blocked(2L)
                .build();
        projectBreakdown.put("Project Alpha", counts);
        taskSummary.setProjectBreakdown(projectBreakdown);

        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String pdfContent = extractTextFromPdf(result);
        assertTrue(pdfContent.contains("Tasks by Project"));
        assertTrue(pdfContent.contains("Project Alpha"));
    }

    @Test
    void testExportWithHoursByDepartment() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String pdfContent = extractTextFromPdf(result);
        assertTrue(pdfContent.contains("Hours by Department"));
        assertTrue(pdfContent.contains("300.0"));
    }

    @Test
    void testExportFormatsNumbersCorrectly() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String pdfContent = extractTextFromPdf(result);

        // Check percentage formatting
        assertTrue(pdfContent.contains("%"));

        // Check hours formatting
        assertTrue(pdfContent.contains("450.5"));
    }

    @Test
    void testExportIsValidPdf() throws Exception {
        // Arrange
        Map<String, Object> reportData = createSampleReportData();
        ReportFilterDto filters = createSampleFilters();

        // Act
        byte[] result = pdfExporter.export(reportData, filters);

        // Assert - should not throw exception when reading as PDF
        assertNotNull(result);
        assertTrue(result.length > 0);

        // Verify PDF header
        String header = new String(result, 0, Math.min(8, result.length));
        assertTrue(header.startsWith("%PDF"), "Should start with PDF header");
    }

    // Helper methods

    private String extractTextFromPdf(byte[] pdfBytes) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
        PdfReader reader = new PdfReader(inputStream);
        PdfDocument pdfDoc = new PdfDocument(reader);

        StringBuilder text = new StringBuilder();
        for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
            text.append(PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)));
        }

        pdfDoc.close();
        return text.toString();
    }

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
                .departmentId(100L)
                .startDate(LocalDate.of(2024, 1, 1))
                .endDate(LocalDate.of(2024, 12, 31))
                .timeRange(ReportFilterDto.TimeRange.YEARLY)
                .exportFormat(ReportFilterDto.ExportFormat.PDF)
                .build();
    }
}
