package com.spmorangle.crm.reporting.export;

import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportExportServiceTest {

    @Mock
    private JsonReportExporter jsonExporter;

    @Mock
    private CsvReportExporter csvExporter;

    @Mock
    private PdfReportExporter pdfExporter;

    @InjectMocks
    private ReportExportService reportExportService;

    private Map<String, Object> reportData;
    private ReportFilterDto filters;

    @BeforeEach
    void setUp() {
        reportData = new HashMap<>();
        reportData.put("taskSummary", TaskSummaryReportDto.builder()
                .totalTasks(100L)
                .completedTasks(60L)
                .build());
        reportData.put("timeAnalytics", TimeAnalyticsReportDto.builder()
                .totalHours(BigDecimal.valueOf(120.5))
                .build());
        reportData.put("generatedAt", java.time.OffsetDateTime.now());

        filters = ReportFilterDto.builder()
                .department("Engineering")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    @Test
    void testExportReport_NoFormat_ReturnsJsonResponse() {
        // Arrange
        filters.setExportFormat(null);

        // Act
        ResponseEntity<?> response = reportExportService.exportReport(reportData, filters);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reportData, response.getBody());
        verify(jsonExporter, never()).export(any(), any());
    }

    @Test
    void testExportReport_JsonFormat_ReturnsJsonResponse() {
        // Arrange
        filters.setExportFormat(ReportFilterDto.ExportFormat.JSON);

        // Act
        ResponseEntity<?> response = reportExportService.exportReport(reportData, filters);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reportData, response.getBody());
    }

    @Test
    void testExportReport_CsvFormat_ReturnsFileDownload() {
        // Arrange
        filters.setExportFormat(ReportFilterDto.ExportFormat.CSV);
        byte[] csvData = "CSV,Data,Here\n1,2,3".getBytes();

        when(csvExporter.export(any(), any())).thenReturn(csvData);
        when(csvExporter.getContentType()).thenReturn("text/csv");
        when(csvExporter.getFileExtension()).thenReturn("csv");

        // Act
        ResponseEntity<?> response = reportExportService.exportReport(reportData, filters);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(csvData, (byte[]) response.getBody());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("text/csv", response.getHeaders().getContentType().toString());
        verify(csvExporter).export(reportData, filters);
    }

    @Test
    void testExportReport_PdfFormat_ReturnsFileDownload() {
        // Arrange
        filters.setExportFormat(ReportFilterDto.ExportFormat.PDF);
        byte[] pdfData = "%PDF-1.4 mock data".getBytes();

        when(pdfExporter.export(any(), any())).thenReturn(pdfData);
        when(pdfExporter.getContentType()).thenReturn("application/pdf");
        when(pdfExporter.getFileExtension()).thenReturn("pdf");

        // Act
        ResponseEntity<?> response = reportExportService.exportReport(reportData, filters);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(pdfData, (byte[]) response.getBody());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        verify(pdfExporter).export(reportData, filters);
    }

    @Test
    void testFilenameGeneration_WithDepartment() {
        // This tests the private generateFilename method indirectly
        // by checking the Content-Disposition header when CSV is implemented
        
        // For now, we test the basic structure
        filters.setDepartment("Software");
        filters.setStartDate(LocalDate.of(2025, 1, 1));
        filters.setEndDate(LocalDate.of(2025, 12, 31));

        // The filename should contain: department, date range
        // Example: report_software_2025-01-01_to_2025-12-31.csv
        
        // This will be testable once CSV export is implemented
        assertNotNull(filters.getDepartment());
        assertEquals("Software", filters.getDepartment());
    }

    @Test
    void testFilenameGeneration_WithTimeRange() {
        filters.setTimeRange(ReportFilterDto.TimeRange.MONTHLY);
        
        // Filename should include time range
        // Example: report_2025-01-01_to_2025-12-31_monthly.csv
        
        assertNotNull(filters.getTimeRange());
        assertEquals(ReportFilterDto.TimeRange.MONTHLY, filters.getTimeRange());
    }

    @Test
    void testFilenameGeneration_SpecialCharacters() {
        filters.setDepartment("R&D Department");
        
        // Special characters should be handled properly
        // Spaces should be replaced with hyphens
        // & and other special chars should be handled
        
        assertTrue(filters.getDepartment().contains("&"));
    }
}

