package com.spmorangle.crm.reporting.controller;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
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
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HR')")
public class ReportController {
    
    private final ReportService reportService;
    private final UserContextService userContextService;
    private final ReportExportService reportExportService;
    private final DepartmentQueryService departmentQueryService;
    
    @GetMapping("/task-summary")
    public ResponseEntity<TaskSummaryReportDto> getTaskSummaryReport(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ReportFilterDto.TimeRange timeRange) {

        User user = userContextService.getRequestingUser();
        log.info("Getting task summary report for user: {} with departmentId: {}, projectIds: {}, startDate: {}, endDate: {}",
                 user.getId(), departmentId, projectIds, startDate, endDate);

        ReportFilterDto filters = ReportFilterDto.builder()
            .departmentId(departmentId)
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
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) List<Long> projectIds,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) ReportFilterDto.TimeRange timeRange) {

        User user = userContextService.getRequestingUser();
        log.info("Getting time analytics report for user: {} with departmentId: {}", user.getId(), departmentId);

        ReportFilterDto filters = ReportFilterDto.builder()
            .departmentId(departmentId)
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
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String department) {

        User user = userContextService.getRequestingUser();
        
        // Convert department name to ID if provided
        Long finalDepartmentId = departmentId;
        if (finalDepartmentId == null && department != null && !department.isEmpty()) {
            Optional<DepartmentDto> deptOpt = departmentQueryService.getByNameCaseInsensitive(department);
            if (deptOpt.isPresent()) {
                finalDepartmentId = deptOpt.get().getId();
                log.info("Converted department name '{}' to ID: {}", department, finalDepartmentId);
            } else {
                log.warn("Department not found: {}", department);
            }
        }
        
        log.info("Getting available projects for user: {} in departmentId: {}", user.getId(), finalDepartmentId);

        List<Object[]> projectData = reportService.getAvailableProjects(finalDepartmentId, user.getId());
        
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
            // Convert department name to ID if provided
            Long departmentId = filters.getDepartmentId();
            if (departmentId == null && filters.getDepartment() != null && !filters.getDepartment().isEmpty()) {
                Optional<DepartmentDto> deptOpt = departmentQueryService.getByNameCaseInsensitive(filters.getDepartment());
                if (deptOpt.isPresent()) {
                    departmentId = deptOpt.get().getId();
                    log.info("Converted department name '{}' to ID: {}", filters.getDepartment(), departmentId);
                } else {
                    log.warn("Department not found: {}", filters.getDepartment());
                }
            }
            
            // Create final filters with departmentId set
            ReportFilterDto finalFilters = ReportFilterDto.builder()
                .departmentId(departmentId)
                .projectIds(filters.getProjectIds())
                .startDate(filters.getStartDate())
                .endDate(filters.getEndDate())
                .timeRange(filters.getTimeRange())
                .exportFormat(filters.getExportFormat())
                .build();
            
            // Generate all report components
            TaskSummaryReportDto taskSummary = reportService.generateTaskSummaryReport(finalFilters, user.getId());
            TimeAnalyticsReportDto timeAnalytics = reportService.generateTimeAnalyticsReport(finalFilters, user.getId());
            List<StaffBreakdownDto> staffBreakdown = reportService.generateStaffBreakdown(finalFilters, user.getId());
            List<TimeSeriesDataPoint> timeSeriesData = reportService.generateTimeSeriesData(finalFilters, user.getId());
            
            // Build report data map
            Map<String, Object> reportData = new java.util.HashMap<>();
            reportData.put("taskSummary", taskSummary);
            reportData.put("timeAnalytics", timeAnalytics);
            reportData.put("staffBreakdown", staffBreakdown);
            reportData.put("filters", finalFilters);
            reportData.put("generatedAt", java.time.OffsetDateTime.now());
            
            // Include time-series data only if it exists
            if (timeSeriesData != null && !timeSeriesData.isEmpty()) {
                reportData.put("timeSeriesData", timeSeriesData);
            }
            
            // Use export service to handle formatting and response
            return reportExportService.exportReport(reportData, finalFilters);
            
        } catch (Exception e) {
            log.error("Error generating report for user: {}", user.getId(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Failed to generate report: " + e.getMessage()));
        }
    }
}

