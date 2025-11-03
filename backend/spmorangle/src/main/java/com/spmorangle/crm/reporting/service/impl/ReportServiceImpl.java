package com.spmorangle.crm.reporting.service.impl;

import com.spmorangle.common.enums.UserType;
import com.spmorangle.common.model.User;
import com.spmorangle.common.repository.UserRepository;
import com.spmorangle.crm.reporting.dto.Period;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.StaffBreakdownDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import com.spmorangle.crm.reporting.dto.TimeSeriesDataPoint;
import com.spmorangle.crm.reporting.model.TaskTimeTracking;
import com.spmorangle.crm.reporting.repository.ReportingRepository;
import com.spmorangle.crm.reporting.repository.TaskTimeTrackingRepository;
import com.spmorangle.crm.reporting.service.ReportService;
import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.repository.TaskAssigneeRepository;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
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
    private final TaskAssigneeRepository taskAssigneeRepository;
    private final TaskRepository taskRepository;
    
    @Override
    public TaskSummaryReportDto generateTaskSummaryReport(ReportFilterDto filters, Long userId) {
        log.info("Generating task summary report for user: {}", userId);
        
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Apply role-based filtering
        String departmentFilter = applyDepartmentFilter(filters.getDepartment(), currentUser);
        
        // Convert null to empty string for department
        String dept = departmentFilter != null ? departmentFilter : "";
        
        // Dates are now required (validated at controller level)
        LocalDate startDate = filters.getStartDate();
        LocalDate endDate = filters.getEndDate();
        
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
        
        // Initialize project breakdown map (will be populated later)
        Map<String, TaskSummaryReportDto.TaskStatusCounts> projectBreakdown = new HashMap<>();
        
        // Process results for overall totals
        for (Object[] row : statusCounts) {
            Status status;
            Long count;
            
            if (hasProjectFilter) {
                // For project query: [projectName, status, count]
                status = (Status) row[1];
                count = (Long) row[2];
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
        
        // Get department breakdown (always show all departments)
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
        
        // Get project breakdown (always show - all projects or filtered projects)
        List<Object[]> projectCounts;
        if (!hasProjectFilter) {
            // No filter - get all projects
            projectCounts = reportingRepository.getAllTaskCountsByProjectAndStatus(dept, startDate, endDate);
        } else {
            // Use the already fetched data from statusCounts (which has project breakdown)
            projectCounts = statusCounts;
        }
        
        // Populate project breakdown
        for (Object[] row : projectCounts) {
            String projectName = (String) row[0];
            Status status = (Status) row[1];
            Long count = (Long) row[2];
            
            projectBreakdown.putIfAbsent(projectName, TaskSummaryReportDto.TaskStatusCounts.builder()
                .total(0L).completed(0L).inProgress(0L).todo(0L).blocked(0L).build());
            
            TaskSummaryReportDto.TaskStatusCounts projStatusCounts = projectBreakdown.get(projectName);
            projStatusCounts.setTotal(projStatusCounts.getTotal() + count);
            
            switch (status) {
                case COMPLETED -> projStatusCounts.setCompleted(projStatusCounts.getCompleted() + count);
                case IN_PROGRESS -> projStatusCounts.setInProgress(projStatusCounts.getInProgress() + count);
                case TODO -> projStatusCounts.setTodo(projStatusCounts.getTodo() + count);
                case BLOCKED -> projStatusCounts.setBlocked(projStatusCounts.getBlocked() + count);
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
        
        // Dates are now required (validated at controller level)
        LocalDate startDate = filters.getStartDate();
        LocalDate endDate = filters.getEndDate();
        
        // Convert empty list to null for query compatibility
        List<Long> projectIds = filters.getProjectIds();
        if (projectIds != null && projectIds.isEmpty()) {
            projectIds = null;
        }
        
        // Get hours by department (with filters applied)
        List<Object[]> departmentHours = taskTimeTrackingRepository.getHoursByDepartment(
            dept, projectIds, startDate, endDate);
        
        Map<String, BigDecimal> hoursByDepartment = new HashMap<>();
        BigDecimal totalHours = BigDecimal.ZERO;
        
        for (Object[] row : departmentHours) {
            String department = (String) row[0];
            BigDecimal hours = (BigDecimal) row[1];
            
            // Apply role-based access control (query already filtered by department and project)
            if (canAccessDepartment(department, currentUser)) {
                hoursByDepartment.put(department, hours != null ? hours : BigDecimal.ZERO);
                totalHours = totalHours.add(hours != null ? hours : BigDecimal.ZERO);
            }
        }
        
        // Get hours by project
        List<Object[]> projectHours = taskTimeTrackingRepository.getHoursByProject(
            dept, projectIds, startDate, endDate);
        
        Map<String, BigDecimal> hoursByProject = new HashMap<>();
        for (Object[] row : projectHours) {
            String projectName = (String) row[0];
            BigDecimal hours = (BigDecimal) row[1];
            hoursByProject.put(projectName, hours != null ? hours : BigDecimal.ZERO);
        }
        
        // Get project details (name, department, hours, completed tasks, in-progress tasks)
        List<Object[]> projectDetailsData = taskTimeTrackingRepository.getProjectDetails(
            dept, projectIds, startDate, endDate);
        
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
            .hoursByDepartment(hoursByDepartment.isEmpty() ? null : hoursByDepartment)
            .hoursByProject(hoursByProject.isEmpty() ? null : hoursByProject)
            .projectDetails(projectDetails.isEmpty() ? null : projectDetails)
            .build();
    }
    
    @Override
    public List<String> getAvailableDepartments(Long userId) {
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<String> allDepartments = reportingRepository.getAllDepartments();

        // Only HR can access reports and see all departments
        if (UserType.HR.getCode().equals(currentUser.getRoleType())) {
            return allDepartments;
        }

        return List.of(); // Non-HR users cannot access reports
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
        
        // Get task owner
        com.spmorangle.crm.taskmanagement.model.Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        Long ownerId = task.getOwnerId();
        
        // Get all assignees
        List<Long> assigneeIds = taskAssigneeRepository.findAssigneeIdsByTaskId(taskId);
        
        // Combine owner + assignees, ensuring no duplicates
        List<Long> allUserIds = new java.util.ArrayList<>();
        allUserIds.add(ownerId);
        for (Long assigneeId : assigneeIds) {
            if (!assigneeId.equals(ownerId)) {
                allUserIds.add(assigneeId);
            }
        }
        
        OffsetDateTime startTime = OffsetDateTime.now();
        
        // Create or update tracking records for all users
        for (Long currentUserId : allUserIds) {
            Optional<TaskTimeTracking> existingTracking = taskTimeTrackingRepository.findByTaskIdAndUserId(taskId, currentUserId);
            
            if (existingTracking.isPresent()) {
                TaskTimeTracking tracking = existingTracking.get();
                if (tracking.getStartedAt() == null || tracking.getCompletedAt() != null) {
                    // Reset tracking for restart scenarios
                    tracking.setStartedAt(startTime);
                    tracking.setCompletedAt(null);
                    tracking.setTotalHours(null);
                    taskTimeTrackingRepository.save(tracking);
                    log.info("Updated existing time tracking record for task: {}, user: {}", taskId, currentUserId);
                }
            } else {
                TaskTimeTracking tracking = new TaskTimeTracking();
                tracking.setTaskId(taskId);
                tracking.setUserId(currentUserId);
                tracking.setStartedAt(startTime);
                taskTimeTrackingRepository.save(tracking);
                log.info("Created new time tracking record for task: {}, user: {}", taskId, currentUserId);
            }
        }
        
        log.info("Started time tracking for task: {} with {} users", taskId, allUserIds.size());
    }
    
    @Override
    @Transactional
    public void endTimeTracking(Long taskId, Long userId) {
        log.info("Ending time tracking for task: {} by user: {}", taskId, userId);
        
        // Get all tracking records for this task
        List<TaskTimeTracking> allTrackingRecords = taskTimeTrackingRepository.findByTaskId(taskId);
        
        if (allTrackingRecords.isEmpty()) {
            log.warn("No time tracking records found for task: {}", taskId);
            return;
        }
        
        // Find the earliest start time among all tracking records
        OffsetDateTime earliestStartTime = null;
        for (TaskTimeTracking record : allTrackingRecords) {
            if (record.getStartedAt() != null && record.getCompletedAt() == null) {
                if (earliestStartTime == null || record.getStartedAt().isBefore(earliestStartTime)) {
                    earliestStartTime = record.getStartedAt();
                }
            }
        }
        
        if (earliestStartTime == null) {
            log.warn("No active time tracking records found for task: {}", taskId);
            return;
        }
        
        // Calculate total hours from earliest start to now
        OffsetDateTime completedAt = OffsetDateTime.now();
        Duration duration = Duration.between(earliestStartTime, completedAt);
        BigDecimal totalHours = BigDecimal.valueOf(duration.toMinutes())
            .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
        
        // Apply the same hours to all tracking records for this task
        int updatedCount = 0;
        for (TaskTimeTracking tracking : allTrackingRecords) {
            if (tracking.getStartedAt() != null && tracking.getCompletedAt() == null) {
                tracking.setCompletedAt(completedAt);
                tracking.setTotalHours(totalHours);
                taskTimeTrackingRepository.save(tracking);
                updatedCount++;
            }
        }
        
        log.info("Completed time tracking for task: {} with {} hours distributed to {} users", 
                 taskId, totalHours, updatedCount);
    }
    
    @Override
    @Transactional
    public void syncTimeTrackingOnAssigneeAdd(Long taskId, Long userId) {
        log.info("Syncing time tracking for new assignee - task: {}, user: {}", taskId, userId);
        
        // Check if task is in IN_PROGRESS status
        com.spmorangle.crm.taskmanagement.model.Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        
        if (task.getStatus() != Status.IN_PROGRESS) {
            log.info("Task {} is not IN_PROGRESS, skipping time tracking sync", taskId);
            return;
        }
        
        // Check if user already has a tracking record
        Optional<TaskTimeTracking> existingTracking = taskTimeTrackingRepository.findByTaskIdAndUserId(taskId, userId);
        
        if (existingTracking.isPresent()) {
            log.info("User {} already has time tracking for task {}", userId, taskId);
            return;
        }
        
        // Check if task owner is the same as the new assignee (avoid duplicate)
        if (task.getOwnerId().equals(userId)) {
            log.info("User {} is the task owner, should already have tracking record", userId);
            return;
        }
        
        // Create new tracking record with current time as start
        TaskTimeTracking tracking = new TaskTimeTracking();
        tracking.setTaskId(taskId);
        tracking.setUserId(userId);
        tracking.setStartedAt(OffsetDateTime.now());
        taskTimeTrackingRepository.save(tracking);
        
        log.info("Created time tracking record for new assignee - task: {}, user: {}", taskId, userId);
    }
    
    @Override
    @Transactional
    public void syncTimeTrackingOnAssigneeRemove(Long taskId, Long userId) {
        log.info("Syncing time tracking for removed assignee - task: {}, user: {}", taskId, userId);
        
        // Check if task is in IN_PROGRESS status
        com.spmorangle.crm.taskmanagement.model.Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));
        
        if (task.getStatus() != Status.IN_PROGRESS) {
            log.info("Task {} is not IN_PROGRESS, skipping time tracking sync", taskId);
            return;
        }
        
        // Don't remove tracking for task owner
        if (task.getOwnerId().equals(userId)) {
            log.info("User {} is the task owner, keeping tracking record", userId);
            return;
        }
        
        // Find and delete the tracking record if it exists and is not completed
        Optional<TaskTimeTracking> existingTracking = taskTimeTrackingRepository.findByTaskIdAndUserId(taskId, userId);
        
        if (existingTracking.isPresent()) {
            TaskTimeTracking tracking = existingTracking.get();
            if (tracking.getCompletedAt() == null) {
                // Only delete if not yet completed
                taskTimeTrackingRepository.delete(tracking);
                log.info("Deleted time tracking record for removed assignee - task: {}, user: {}", taskId, userId);
            } else {
                log.info("Tracking record already completed, keeping it - task: {}, user: {}", taskId, userId);
            }
        } else {
            log.info("No tracking record found for user {} on task {}", userId, taskId);
        }
    }
    
    private String applyDepartmentFilter(String requestedDepartment, User currentUser) {
        if (UserType.HR.getCode().equals(currentUser.getRoleType())) {
            // Only HR can access reports and filter by any department or see all
            return requestedDepartment;
        }

        // Non-HR users cannot access reports
        throw new RuntimeException("Access denied: Only HR users can access reports");
    }
    
    private boolean canAccessDepartment(String department, User currentUser) {
        // Only HR can access reports
        if (UserType.HR.getCode().equals(currentUser.getRoleType())) {
            return true;
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
        
        // Dates are now required (validated at controller level)
        LocalDate startDate = filters.getStartDate();
        LocalDate endDate = filters.getEndDate();
        
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
                List<StaffBreakdownDto> periodStaffBreakdown = generateStaffBreakdown(periodFilter, userId);
                
                return TimeSeriesDataPoint.builder()
                    .period(period.getPeriod())
                    .periodLabel(period.getPeriodLabel())
                    .startDate(period.getStartDate())
                    .endDate(period.getEndDate())
                    .taskSummary(periodTaskSummary)
                    .timeAnalytics(periodTimeAnalytics)
                    .staffBreakdown(periodStaffBreakdown)
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
    
    @Override
    public List<StaffBreakdownDto> generateStaffBreakdown(ReportFilterDto filters, Long userId) {
        log.info("Generating staff breakdown report for user: {}", userId);
        
        User currentUser = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Apply role-based filtering
        String departmentFilter = applyDepartmentFilter(filters.getDepartment(), currentUser);
        
        // Convert null to empty string for department
        String dept = departmentFilter != null ? departmentFilter : "";
        
        // Dates are now required (validated at controller level)
        LocalDate startDate = filters.getStartDate();
        LocalDate endDate = filters.getEndDate();
        
        // Get project filter - convert empty list to null for query compatibility
        List<Long> projectIds = filters.getProjectIds();
        if (projectIds != null && projectIds.isEmpty()) {
            projectIds = null;
        }
        
        // Get all relevant users - single query handles both department-only and project filters
        List<Object[]> users = reportingRepository.getUsersForStaffBreakdown(dept, projectIds);
        
        // For each user, fetch their task counts and logged hours
        List<StaffBreakdownDto> staffBreakdowns = new ArrayList<>();
        for (Object[] userRow : users) {
            Long staffUserId = (Long) userRow[0];
            String userName = (String) userRow[1];
            String userDepartment = (String) userRow[2];
            
            // Get task counts by status for this user
            List<Object[]> taskCounts = reportingRepository.getTaskCountsByStatusForUser(
                staffUserId, projectIds, startDate, endDate);
            
            // Initialize counters
            long todoTasks = 0;
            long inProgressTasks = 0;
            long completedTasks = 0;
            long blockedTasks = 0;
            
            // Process task counts
            for (Object[] countRow : taskCounts) {
                Status status = (Status) countRow[0];
                Long count = (Long) countRow[1];
                
                switch (status) {
                    case TODO -> todoTasks = count;
                    case IN_PROGRESS -> inProgressTasks = count;
                    case COMPLETED -> completedTasks = count;
                    case BLOCKED -> blockedTasks = count;
                }
            }
            
            // Get logged hours for this user (with project filter)
            BigDecimal loggedHours = reportingRepository.getLoggedHoursForUser(
                staffUserId, projectIds, startDate, endDate);
            
            // Always include all users, even with zero activity
            staffBreakdowns.add(StaffBreakdownDto.builder()
                .userId(staffUserId)
                .userName(userName)
                .department(userDepartment)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .completedTasks(completedTasks)
                .blockedTasks(blockedTasks)
                .loggedHours(loggedHours != null ? loggedHours : BigDecimal.ZERO)
                .build());
        }
        
        return staffBreakdowns;
    }
}

