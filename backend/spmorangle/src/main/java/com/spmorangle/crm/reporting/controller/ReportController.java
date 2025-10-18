package com.spmorangle.crm.reporting.controller;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.StaffBreakdownDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import com.spmorangle.crm.reporting.dto.TimeSeriesDataPoint;
import com.spmorangle.crm.reporting.export.ReportExportService;
import com.spmorangle.crm.reporting.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HR') or hasRole('MANAGER')")
public class ReportController {
    
    private final ReportService reportService;
    private final UserContextService userContextService;
    private final ReportExportService reportExportService;
    
    @GetMapping("/task-summary")
    public ResponseEntity<TaskSummaryReportDto> getTaskSummaryReport(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ReportFilterDto.TimeRange timeRange) {
        
        User user = userContextService.getRequestingUser();
        log.info("Getting task summary report for user: {} with department: {}, projectIds: {}, startDate: {}, endDate: {}", 
                 user.getId(), department, projectIds, startDate, endDate);
        
        ReportFilterDto filters = ReportFilterDto.builder()
            .department(department)
            .projectIds(projectIds)
            .startDate(startDate)
            .endDate(endDate)
            .timeRange(timeRange)
            .build();
        
        TaskSummaryReportDto report = reportService.generateTaskSummaryReport(filters, user.getId());
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/time-analytics")
    public ResponseEntity<TimeAnalyticsReportDto> getTimeAnalyticsReport(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ReportFilterDto.TimeRange timeRange) {
        
        User user = userContextService.getRequestingUser();
        log.info("Getting time analytics report for user: {} with department: {}", user.getId(), department);
        
        ReportFilterDto filters = ReportFilterDto.builder()
            .department(department)
            .projectIds(projectIds)
            .startDate(startDate)
            .endDate(endDate)
            .timeRange(timeRange)
            .build();
        
        TimeAnalyticsReportDto report = reportService.generateTimeAnalyticsReport(filters, user.getId());
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getAvailableDepartments() {
        User user = userContextService.getRequestingUser();
        log.info("Getting available departments for user: {}", user.getId());
        
        List<String> departments = reportService.getAvailableDepartments(user.getId());
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/projects")
    public ResponseEntity<Map<String, Object>> getAvailableProjects(
            @RequestParam(required = false) String department) {
        
        User user = userContextService.getRequestingUser();
        log.info("Getting available projects for user: {} in department: {}", user.getId(), department);
        
        List<Object[]> projectData = reportService.getAvailableProjects(department, user.getId());
        
        // Convert to a more frontend-friendly format
        List<Map<String, Object>> projects = projectData.stream()
            .map(row -> Map.of(
                "id", row[0],
                "name", row[1]
            ))
            .toList();
        
        return ResponseEntity.ok(Map.of("projects", projects));
    }
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateReport(@Valid @RequestBody ReportFilterDto filters) {
        User user = userContextService.getRequestingUser();
        log.info("Generating comprehensive report for user: {} with filters: {}", user.getId(), filters);
        
        try {
            // Generate all report components
            TaskSummaryReportDto taskSummary = reportService.generateTaskSummaryReport(filters, user.getId());
            TimeAnalyticsReportDto timeAnalytics = reportService.generateTimeAnalyticsReport(filters, user.getId());
            List<StaffBreakdownDto> staffBreakdown = reportService.generateStaffBreakdown(filters, user.getId());
            List<TimeSeriesDataPoint> timeSeriesData = reportService.generateTimeSeriesData(filters, user.getId());
            
            // Build report data map
            Map<String, Object> reportData = new java.util.HashMap<>();
            reportData.put("taskSummary", taskSummary);
            reportData.put("timeAnalytics", timeAnalytics);
            reportData.put("staffBreakdown", staffBreakdown);
            reportData.put("filters", filters);
            reportData.put("generatedAt", java.time.OffsetDateTime.now());
            
            // Include time-series data only if it exists
            if (timeSeriesData != null && !timeSeriesData.isEmpty()) {
                reportData.put("timeSeriesData", timeSeriesData);
            }
            
            // Use export service to handle formatting and response
            return reportExportService.exportReport(reportData, filters);
            
        } catch (Exception e) {
            log.error("Error generating report for user: {}", user.getId(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to generate report: " + e.getMessage()));
        }
    }
}

