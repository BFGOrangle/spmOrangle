package com.spmorangle.crm.reporting.service.impl;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.reporting.dto.Period;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import com.spmorangle.crm.reporting.dto.TimeSeriesDataPoint;
import com.spmorangle.crm.reporting.model.TaskTimeTracking;
import com.spmorangle.crm.reporting.repository.ReportingRepository;
import com.spmorangle.crm.reporting.repository.TaskTimeTrackingRepository;
import com.spmorangle.crm.reporting.service.ReportService;
import com.spmorangle.crm.taskmanagement.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    
    private final ReportingRepository reportingRepository;
    private final TaskTimeTrackingRepository taskTimeTrackingRepository;
    private final UserRepository userRepository;
    
    @Override
    public TaskSummaryReportDto generateTaskSummaryReport(ReportFilterDto filters, Long userId) {
        log.info("Generating task summary report for user: {}", userId);
        
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Apply role-based filtering
        String departmentFilter = applyDepartmentFilter(filters.getDepartment(), currentUser);
        
        // Convert null to empty string for department
        String dept = departmentFilter != null ? departmentFilter : "";
        
        // Use very old/future dates if not specified
        LocalDate startDate = filters.getStartDate() != null ? filters.getStartDate() : LocalDate.of(1900, 1, 1);
        LocalDate endDate = filters.getEndDate() != null ? filters.getEndDate() : LocalDate.of(2099, 12, 31);
        
        // Get task counts by status - choose the right method based on filters
        List<Object[]> statusCounts;
        boolean hasProjectFilter = filters.getProjectIds() != null && !filters.getProjectIds().isEmpty();
        
        if (hasProjectFilter) {
            // Use project-specific query when project filtering is requested
            log.info("Filtering by projects: {}", filters.getProjectIds());
            statusCounts = reportingRepository.getTaskCountsByProjectAndStatus(
                dept, filters.getProjectIds(), startDate, endDate);
        } else {
            // Use general query when no project filtering
            statusCounts = reportingRepository.getTaskCountsByStatus(
                dept, startDate, endDate);
        }
        
        // Initialize counters
        long totalTasks = 0;
        long completedTasks = 0;
        long inProgressTasks = 0;
        long todoTasks = 0;
        long blockedTasks = 0;
        
        // Build project breakdown if filtering by projects
        Map<String, TaskSummaryReportDto.TaskStatusCounts> projectBreakdown = new HashMap<>();
        
        // Process results
        for (Object[] row : statusCounts) {
            Status status;
            Long count;
            
            if (hasProjectFilter) {
                // For project query: [projectName, status, count]
                String projectName = (String) row[0];
                status = (Status) row[1];
                count = (Long) row[2];
                
                // Build project breakdown
                projectBreakdown.putIfAbsent(projectName, TaskSummaryReportDto.TaskStatusCounts.builder()
                    .total(0L).completed(0L).inProgress(0L).todo(0L).blocked(0L).build());
                
                TaskSummaryReportDto.TaskStatusCounts projectCounts = projectBreakdown.get(projectName);
                projectCounts.setTotal(projectCounts.getTotal() + count);
                
                switch (status) {
                    case COMPLETED -> projectCounts.setCompleted(projectCounts.getCompleted() + count);
                    case IN_PROGRESS -> projectCounts.setInProgress(projectCounts.getInProgress() + count);
                    case TODO -> projectCounts.setTodo(projectCounts.getTodo() + count);
                    case BLOCKED -> projectCounts.setBlocked(projectCounts.getBlocked() + count);
                }
            } else {
                // For general query: [status, count]
                status = (Status) row[0];
                count = (Long) row[1];
            }
            
            totalTasks += count;
            
            switch (status) {
                case COMPLETED -> completedTasks += count;
                case IN_PROGRESS -> inProgressTasks += count;
                case TODO -> todoTasks += count;
                case BLOCKED -> blockedTasks += count;
            }
        }
        
        // Get department breakdown
        Map<String, TaskSummaryReportDto.TaskStatusCounts> departmentBreakdown = new HashMap<>();
        List<Object[]> deptCounts = reportingRepository.getTaskCountsByDepartmentAndStatus(startDate, endDate);
        
        for (Object[] row : deptCounts) {
            String department = (String) row[0];
            Status status = (Status) row[1];
            Long count = (Long) row[2];
            
            // Apply role-based filtering
            if (canAccessDepartment(department, currentUser)) {
                departmentBreakdown.putIfAbsent(department, TaskSummaryReportDto.TaskStatusCounts.builder()
                    .total(0L).completed(0L).inProgress(0L).todo(0L).blocked(0L).build());
                
                TaskSummaryReportDto.TaskStatusCounts deptStatusCounts = departmentBreakdown.get(department);
                deptStatusCounts.setTotal(deptStatusCounts.getTotal() + count);
                
                switch (status) {
                    case COMPLETED -> deptStatusCounts.setCompleted(deptStatusCounts.getCompleted() + count);
                    case IN_PROGRESS -> deptStatusCounts.setInProgress(deptStatusCounts.getInProgress() + count);
                    case TODO -> deptStatusCounts.setTodo(deptStatusCounts.getTodo() + count);
                    case BLOCKED -> deptStatusCounts.setBlocked(deptStatusCounts.getBlocked() + count);
                }
            }
        }
        
        // Calculate percentages
        double completedPercentage = totalTasks > 0 ? (completedTasks * 100.0) / totalTasks : 0;
        double inProgressPercentage = totalTasks > 0 ? (inProgressTasks * 100.0) / totalTasks : 0;
        double todoPercentage = totalTasks > 0 ? (todoTasks * 100.0) / totalTasks : 0;
        double blockedPercentage = totalTasks > 0 ? (blockedTasks * 100.0) / totalTasks : 0;
        
        return TaskSummaryReportDto.builder()
            .totalTasks(totalTasks)
            .completedTasks(completedTasks)
            .inProgressTasks(inProgressTasks)
            .todoTasks(todoTasks)
            .blockedTasks(blockedTasks)
            .completedPercentage(Math.round(completedPercentage * 100.0) / 100.0)
            .inProgressPercentage(Math.round(inProgressPercentage * 100.0) / 100.0)
            .todoPercentage(Math.round(todoPercentage * 100.0) / 100.0)
            .blockedPercentage(Math.round(blockedPercentage * 100.0) / 100.0)
            .departmentBreakdown(departmentBreakdown.isEmpty() ? null : departmentBreakdown)
            .projectBreakdown(projectBreakdown.isEmpty() ? null : projectBreakdown)
            .build();
    }
    
    @Override
    public TimeAnalyticsReportDto generateTimeAnalyticsReport(ReportFilterDto filters, Long userId) {
        log.info("Generating time analytics report for user: {}", userId);
        
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Apply role-based filtering
        String departmentFilter = applyDepartmentFilter(filters.getDepartment(), currentUser);
        
        // Convert null to empty string for department
        String dept = departmentFilter != null ? departmentFilter : "";
        
        // Use very old/future dates if not specified
        LocalDate startDate = filters.getStartDate() != null ? filters.getStartDate() : LocalDate.of(1900, 1, 1);
        LocalDate endDate = filters.getEndDate() != null ? filters.getEndDate() : LocalDate.of(2099, 12, 31);
        
        // Get hours by department
        List<Object[]> departmentHours = taskTimeTrackingRepository.getHoursByDepartment(
            startDate, endDate);
        
        Map<String, BigDecimal> hoursByDepartment = new HashMap<>();
        BigDecimal totalHours = BigDecimal.ZERO;
        
        for (Object[] row : departmentHours) {
            String department = (String) row[0];
            BigDecimal hours = (BigDecimal) row[1];
            
            // Apply department filtering based on user role
            if (canAccessDepartment(department, currentUser)) {
                hoursByDepartment.put(department, hours != null ? hours : BigDecimal.ZERO);
                totalHours = totalHours.add(hours != null ? hours : BigDecimal.ZERO);
            }
        }
        
        // Get hours by project
        List<Object[]> projectHours = taskTimeTrackingRepository.getHoursByProject(
            dept, filters.getProjectIds(), startDate, endDate);
        
        Map<String, BigDecimal> hoursByProject = new HashMap<>();
        for (Object[] row : projectHours) {
            String projectName = (String) row[0];
            BigDecimal hours = (BigDecimal) row[1];
            hoursByProject.put(projectName, hours != null ? hours : BigDecimal.ZERO);
        }
        
        // Get project details (name, department, hours, completed tasks, in-progress tasks)
        List<Object[]> projectDetailsData = taskTimeTrackingRepository.getProjectDetails(
            dept, filters.getProjectIds(), startDate, endDate);
        
        Map<String, TimeAnalyticsReportDto.ProjectTimeDetails> projectDetails = new HashMap<>();
        for (Object[] row : projectDetailsData) {
            String projectName = (String) row[0];
            String projectDept = (String) row[1];
            BigDecimal projectTotalHours = (BigDecimal) row[2];
            Long completedTasks = (Long) row[3];
            Long inProgressTasks = (Long) row[4];
            
            // Calculate average hours per task
            Long totalProjectTasks = completedTasks + inProgressTasks;
            BigDecimal avgHoursPerTask = totalProjectTasks > 0 && projectTotalHours != null
                ? projectTotalHours.divide(BigDecimal.valueOf(totalProjectTasks), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
            
            projectDetails.put(projectName, TimeAnalyticsReportDto.ProjectTimeDetails.builder()
                .projectName(projectName)
                .department(projectDept)
                .totalHours(projectTotalHours != null ? projectTotalHours : BigDecimal.ZERO)
                .completedTasks(completedTasks)
                .inProgressTasks(inProgressTasks)
                .averageHoursPerTask(avgHoursPerTask)
                .build());
        }
        
        return TimeAnalyticsReportDto.builder()
            .totalHours(totalHours)
            .hoursByDepartment(hoursByDepartment)
            .hoursByProject(hoursByProject)
            .projectDetails(projectDetails.isEmpty() ? null : projectDetails)
            .build();
    }
    
    @Override
    public List<String> getAvailableDepartments(Long userId) {
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<String> allDepartments = reportingRepository.getAllDepartments();
        
        // HR can see all departments, Managers only their own
        if (UserType.HR.getCode().equals(currentUser.getRoleType())) {
            return allDepartments;
        } else if (UserType.MANAGER.getCode().equals(currentUser.getRoleType())) {
            return allDepartments.stream()
                .filter(dept -> dept.equals(currentUser.getDepartment()))
                .toList();
        }
        
        return List.of(); // Staff cannot access reports
    }
    
    @Override
    public List<Object[]> getAvailableProjects(String department, Long userId) {
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        String departmentFilter = applyDepartmentFilter(department, currentUser);
        String dept = departmentFilter != null ? departmentFilter : "";
        return reportingRepository.getProjectsByDepartment(dept);
    }
    
    @Override
    @Transactional
    public void startTimeTracking(Long taskId, Long userId) {
        log.info("Starting time tracking for task: {} by user: {}", taskId, userId);
        
        Optional<TaskTimeTracking> existingTracking = taskTimeTrackingRepository.findByTaskIdAndUserId(taskId, userId);
        
        if (existingTracking.isPresent()) {
            TaskTimeTracking tracking = existingTracking.get();
            if (tracking.getStartedAt() == null) {
                tracking.setStartedAt(OffsetDateTime.now());
                tracking.setCompletedAt(null);
                tracking.setTotalHours(null);
                taskTimeTrackingRepository.save(tracking);
                log.info("Updated existing time tracking record for task: {}", taskId);
            }
        } else {
            TaskTimeTracking tracking = new TaskTimeTracking();
            tracking.setTaskId(taskId);
            tracking.setUserId(userId);
            tracking.setStartedAt(OffsetDateTime.now());
            taskTimeTrackingRepository.save(tracking);
            log.info("Created new time tracking record for task: {}", taskId);
        }
    }
    
    @Override
    @Transactional
    public void endTimeTracking(Long taskId, Long userId) {
        log.info("Ending time tracking for task: {} by user: {}", taskId, userId);
        
        Optional<TaskTimeTracking> existingTracking = taskTimeTrackingRepository.findByTaskIdAndUserId(taskId, userId);
        
        if (existingTracking.isPresent()) {
            TaskTimeTracking tracking = existingTracking.get();
            if (tracking.getStartedAt() != null && tracking.getCompletedAt() == null) {
                OffsetDateTime completedAt = OffsetDateTime.now();
                tracking.setCompletedAt(completedAt);
                
                // Calculate total hours
                Duration duration = Duration.between(tracking.getStartedAt(), completedAt);
                BigDecimal totalHours = BigDecimal.valueOf(duration.toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
                tracking.setTotalHours(totalHours);
                
                taskTimeTrackingRepository.save(tracking);
                log.info("Completed time tracking for task: {} with {} hours", taskId, totalHours);
            }
        }
    }
    
    private String applyDepartmentFilter(String requestedDepartment, User currentUser) {
        if (UserType.HR.getCode().equals(currentUser.getRoleType())) {
            // HR can filter by any department or see all
            return requestedDepartment;
        } else if (UserType.MANAGER.getCode().equals(currentUser.getRoleType())) {
            // Managers can only see their own department
            return currentUser.getDepartment();
        }
        
        // Staff cannot access reports
        throw new RuntimeException("Access denied: Staff users cannot access reports");
    }
    
    private boolean canAccessDepartment(String department, User currentUser) {
        if (UserType.HR.getCode().equals(currentUser.getRoleType())) {
            return true;
        } else if (UserType.MANAGER.getCode().equals(currentUser.getRoleType())) {
            return department.equals(currentUser.getDepartment());
        }
        return false;
    }
    
    /**
     * Generate time-series data based on the time range
     * Handles periods without data by including them with zero values
     */
    public List<TimeSeriesDataPoint> generateTimeSeriesData(ReportFilterDto filters, Long userId) {
        if (filters.getTimeRange() == null || 
            filters.getTimeRange() == ReportFilterDto.TimeRange.CUSTOM) {
            return null;
        }
        
        LocalDate startDate = filters.getStartDate() != null ? 
            filters.getStartDate() : LocalDate.of(1900, 1, 1);
        LocalDate endDate = filters.getEndDate() != null ? 
            filters.getEndDate() : LocalDate.of(2099, 12, 31);
        
        // Generate all periods (including empty ones)
        List<Period> periods = generatePeriods(startDate, endDate, filters.getTimeRange());
        
        // Generate data for each period
        return periods.stream()
            .map(period -> {
                // Create filter for this specific period
                ReportFilterDto periodFilter = ReportFilterDto.builder()
                    .department(filters.getDepartment())
                    .projectIds(filters.getProjectIds())
                    .startDate(period.getStartDate())
                    .endDate(period.getEndDate())
                    .timeRange(null) // Prevent recursion
                    .build();
                
                // Generate reports for this period
                TaskSummaryReportDto periodTaskSummary = generateTaskSummaryReport(periodFilter, userId);
                TimeAnalyticsReportDto periodTimeAnalytics = generateTimeAnalyticsReport(periodFilter, userId);
                
                return TimeSeriesDataPoint.builder()
                    .period(period.getPeriod())
                    .periodLabel(period.getPeriodLabel())
                    .startDate(period.getStartDate())
                    .endDate(period.getEndDate())
                    .taskSummary(periodTaskSummary)
                    .timeAnalytics(periodTimeAnalytics)
                    .build();
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Generate list of periods based on time range
     * Ensures all periods are included, even if they have no data
     */
    private List<Period> generatePeriods(LocalDate start, LocalDate end, ReportFilterDto.TimeRange timeRange) {
        List<Period> periods = new ArrayList<>();
        
        switch (timeRange) {
            case WEEKLY -> {
                LocalDate current = start;
                int weekNum = 1;
                
                while (!current.isAfter(end)) {
                    // Get the end of the week (Sunday)
                    LocalDate periodEnd = current.plusWeeks(1).minusDays(1);
                    if (periodEnd.isAfter(end)) {
                        periodEnd = end;
                    }
                    
                    // Format: "2025-W01"
                    String period = current.getYear() + "-W" + String.format("%02d", weekNum);
                    String periodLabel = "Week " + weekNum + " (" + 
                        current.format(DateTimeFormatter.ofPattern("MMM d")) + " - " +
                        periodEnd.format(DateTimeFormatter.ofPattern("MMM d, yyyy")) + ")";
                    
                    periods.add(Period.builder()
                        .period(period)
                        .periodLabel(periodLabel)
                        .startDate(current)
                        .endDate(periodEnd)
                        .build());
                    
                    current = current.plusWeeks(1);
                    weekNum++;
                }
            }
            
            case MONTHLY -> {
                LocalDate current = start.withDayOfMonth(1); // Start at beginning of month
                
                while (!current.isAfter(end)) {
                    // Get the last day of the month
                    LocalDate periodEnd = current.withDayOfMonth(current.lengthOfMonth());
                    
                    // Adjust if period end is after the requested end date
                    if (periodEnd.isAfter(end)) {
                        periodEnd = end;
                    }
                    
                    // Adjust if period start is before the requested start date
                    LocalDate periodStart = current.isBefore(start) ? start : current;
                    
                    // Format: "2025-01"
                    String period = current.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    String periodLabel = current.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                    
                    periods.add(Period.builder()
                        .period(period)
                        .periodLabel(periodLabel)
                        .startDate(periodStart)
                        .endDate(periodEnd)
                        .build());
                    
                    current = current.plusMonths(1).withDayOfMonth(1);
                }
            }
            
            case QUARTERLY -> {
                LocalDate current = start.withDayOfMonth(1);
                // Adjust to start of quarter
                int startMonth = ((current.getMonthValue() - 1) / 3) * 3 + 1;
                current = current.withMonth(startMonth);
                
                int quarterNum = 1;
                while (!current.isAfter(end)) {
                    // Get the end of the quarter (3 months later)
                    LocalDate periodEnd = current.plusMonths(3).minusDays(1);
                    if (periodEnd.isAfter(end)) {
                        periodEnd = end;
                    }
                    
                    LocalDate periodStart = current.isBefore(start) ? start : current;
                    
                    // Format: "2025-Q1"
                    String period = current.getYear() + "-Q" + quarterNum;
                    String periodLabel = "Q" + quarterNum + " " + current.getYear();
                    
                    periods.add(Period.builder()
                        .period(period)
                        .periodLabel(periodLabel)
                        .startDate(periodStart)
                        .endDate(periodEnd)
                        .build());
                    
                    current = current.plusMonths(3);
                    quarterNum = (quarterNum % 4) + 1;
                }
            }
            
            case YEARLY -> {
                LocalDate current = start.withDayOfYear(1);
                
                while (!current.isAfter(end)) {
                    // Get the end of the year
                    LocalDate periodEnd = current.withDayOfYear(current.lengthOfYear());
                    if (periodEnd.isAfter(end)) {
                        periodEnd = end;
                    }
                    
                    LocalDate periodStart = current.isBefore(start) ? start : current;
                    
                    // Format: "2025"
                    String period = String.valueOf(current.getYear());
                    String periodLabel = String.valueOf(current.getYear());
                    
                    periods.add(Period.builder()
                        .period(period)
                        .periodLabel(periodLabel)
                        .startDate(periodStart)
                        .endDate(periodEnd)
                        .build());
                    
                    current = current.plusYears(1).withDayOfYear(1);
                }
            }
            
            case CUSTOM -> {
                // CUSTOM timeRange doesn't split into periods
                // handled by returning null in generateTimeSeriesData
            }
        }
        
        return periods;
    }
}

