package com.spmorangle.crm.reporting.export;

import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonReportExporterTest {

    private JsonReportExporter jsonExporter;
    private Map<String, Object> reportData;
    private ReportFilterDto filters;

    @BeforeEach
    void setUp() {
        jsonExporter = new JsonReportExporter();

        reportData = new HashMap<>();
        reportData.put("taskSummary", TaskSummaryReportDto.builder()
                .totalTasks(100L)
                .completedTasks(60L)
                .inProgressTasks(25L)
                .todoTasks(10L)
                .blockedTasks(5L)
                .completedPercentage(60.0)
                .build());
        
        reportData.put("timeAnalytics", TimeAnalyticsReportDto.builder()
                .totalHours(BigDecimal.valueOf(120.5))
                .hoursByDepartment(Map.of("Engineering", BigDecimal.valueOf(120.5)))
                .build());
        
        reportData.put("generatedAt", OffsetDateTime.now());

        filters = ReportFilterDto.builder()
                .department("Engineering")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .build();
    }

    @Test
    void testExport_Success() {
        // Act
        byte[] result = jsonExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 0);
        
        // Convert to string and verify it's valid JSON
        String jsonString = new String(result);
        assertTrue(jsonString.contains("taskSummary"));
        assertTrue(jsonString.contains("timeAnalytics"));
        assertTrue(jsonString.contains("generatedAt"));
    }

    @Test
    void testExport_EmptyData() {
        // Arrange
        Map<String, Object> emptyData = new HashMap<>();

        // Act
        byte[] result = jsonExporter.export(emptyData, filters);

        // Assert
        assertNotNull(result);
        String jsonString = new String(result).trim();
        // Pretty-printed JSON may have whitespace inside braces
        assertTrue(jsonString.equals("{}") || jsonString.equals("{ }"));
    }

    @Test
    void testExport_WithNullValues() {
        // Arrange
        reportData.put("timeAnalytics", null);

        // Act
        byte[] result = jsonExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        String jsonString = new String(result);
        assertTrue(jsonString.contains("\"timeAnalytics\" : null") || 
                   jsonString.contains("\"timeAnalytics\":null"));
    }

    @Test
    void testExport_LargeDataset() {
        // Arrange
        Map<String, BigDecimal> largeHoursByDepartment = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            largeHoursByDepartment.put("Department" + i, BigDecimal.valueOf(i * 10.5));
        }
        
        reportData.put("timeAnalytics", TimeAnalyticsReportDto.builder()
                .totalHours(BigDecimal.valueOf(10000))
                .hoursByDepartment(largeHoursByDepartment)
                .build());

        // Act
        byte[] result = jsonExporter.export(reportData, filters);

        // Assert
        assertNotNull(result);
        assertTrue(result.length > 1000); // Should be a large JSON
        String jsonString = new String(result);
        assertTrue(jsonString.contains("Department0"));
        assertTrue(jsonString.contains("Department99"));
    }

    @Test
    void testGetContentType() {
        // Act
        String contentType = jsonExporter.getContentType();

        // Assert
        assertEquals("application/json", contentType);
    }

    @Test
    void testGetFileExtension() {
        // Act
        String extension = jsonExporter.getFileExtension();

        // Assert
        assertEquals("json", extension);
    }

    @Test
    void testExport_DateSerialization() {
        // Ensure dates are properly serialized
        byte[] result = jsonExporter.export(reportData, filters);
        String jsonString = new String(result);

        // Should contain ISO date format
        assertTrue(jsonString.contains("generatedAt"));
        // Date should be in ISO format (not timestamp)
        assertFalse(jsonString.matches(".*\"generatedAt\"\\s*:\\s*\\d+.*")); // Not a numeric timestamp
    }

    @Test
    void testExport_PrettyPrint() {
        // Verify the JSON is pretty-printed (has indentation)
        byte[] result = jsonExporter.export(reportData, filters);
        String jsonString = new String(result);

        // Pretty-printed JSON should contain newlines and indentation
        assertTrue(jsonString.contains("\n"));
        assertTrue(jsonString.contains("  ")); // Indentation spaces
    }
}

