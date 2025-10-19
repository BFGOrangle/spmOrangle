package com.spmorangle.crm.reporting.service;

import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.StaffBreakdownDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import com.spmorangle.crm.reporting.dto.TimeSeriesDataPoint;

import java.util.List;

public interface ReportService {
    
    /**
     * Generate task summary report with role-based access control
     * @param filters Report filters (department, project, date range)
     * @param userId Current user ID for permission checking
     * @return Task summary report
     */
    TaskSummaryReportDto generateTaskSummaryReport(ReportFilterDto filters, Long userId);
    
    /**
     * Generate time analytics report with role-based access control
     * @param filters Report filters (department, project, date range)
     * @param userId Current user ID for permission checking
     * @return Time analytics report
     */
    TimeAnalyticsReportDto generateTimeAnalyticsReport(ReportFilterDto filters, Long userId);
    
    /**
     * Get available departments based on user role
     * @param userId Current user ID for permission checking
     * @return List of departments the user can access
     */
    List<String> getAvailableDepartments(Long userId);
    
    /**
     * Get available projects based on user role and department filter
     * @param department Department filter (optional)
     * @param userId Current user ID for permission checking
     * @return List of projects the user can access
     */
    List<Object[]> getAvailableProjects(String department, Long userId);
    
    /**
     * Start time tracking when task moves to IN_PROGRESS
     * @param taskId Task ID
     * @param userId User ID who changed the status
     */
    void startTimeTracking(Long taskId, Long userId);
    
    /**
     * End time tracking when task moves to COMPLETED
     * @param taskId Task ID
     * @param userId User ID who changed the status
     */
    void endTimeTracking(Long taskId, Long userId);
    
    /**
     * Generate time-series data based on time range (weekly/monthly/quarterly/yearly)
     * Includes all periods even if they have no data (zero values)
     * @param filters Report filters including timeRange
     * @param userId Current user ID for permission checking
     * @return List of time-series data points, null if timeRange is not specified or CUSTOM
     */
    List<TimeSeriesDataPoint> generateTimeSeriesData(ReportFilterDto filters, Long userId);
    
    /**
     * Generate staff breakdown report showing task counts by status and logged hours per user
     * Includes tasks where user is owner OR assignee
     * @param filters Report filters (department, project, date range)
     * @param userId Current user ID for permission checking
     * @return List of staff breakdown data
     */
    List<StaffBreakdownDto> generateStaffBreakdown(ReportFilterDto filters, Long userId);
    
    /**
     * Sync time tracking when an assignee is added to a task during IN_PROGRESS
     * @param taskId Task ID
     * @param userId User ID of the new assignee
     */
    void syncTimeTrackingOnAssigneeAdd(Long taskId, Long userId);
    
    /**
     * Sync time tracking when an assignee is removed from a task during IN_PROGRESS
     * @param taskId Task ID
     * @param userId User ID of the removed assignee
     */
    void syncTimeTrackingOnAssigneeRemove(Long taskId, Long userId);
    
}

