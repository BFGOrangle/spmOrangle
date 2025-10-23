package com.spmorangle.crm.reporting.export;

import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.StaffBreakdownDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * CSV report exporter
 * Exports report data as CSV format with multiple sections
 */
@Slf4j
@Component
public class CsvReportExporter implements ReportExporter {

    @Override
    public byte[] export(Map<String, Object> reportData, ReportFilterDto filters) {
        try {
            log.debug("Exporting report as CSV");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            // Use RFC4180 format for CSV
            CSVFormat csvFormat = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();

            try (CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat)) {
                // Add report metadata
                addReportMetadata(csvPrinter, reportData, filters);
                csvPrinter.println();

                // Add Task Summary section
                TaskSummaryReportDto taskSummary = (TaskSummaryReportDto) reportData.get("taskSummary");
                if (taskSummary != null) {
                    addTaskSummarySection(csvPrinter, taskSummary);
                    csvPrinter.println();
                }

                // Add Time Analytics section
                TimeAnalyticsReportDto timeAnalytics = (TimeAnalyticsReportDto) reportData.get("timeAnalytics");
                if (timeAnalytics != null) {
                    addTimeAnalyticsSection(csvPrinter, timeAnalytics);
                    csvPrinter.println();
                }

                // Add Staff Breakdown section
                @SuppressWarnings("unchecked")
                List<StaffBreakdownDto> staffBreakdown = (List<StaffBreakdownDto>) reportData.get("staffBreakdown");
                if (staffBreakdown != null && !staffBreakdown.isEmpty()) {
                    addStaffBreakdownSection(csvPrinter, staffBreakdown);
                }

                csvPrinter.flush();
                writer.flush();
            }

            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting report as CSV", e);
            throw new RuntimeException("Failed to export report as CSV: " + e.getMessage(), e);
        }
    }

    private void addReportMetadata(CSVPrinter csvPrinter, Map<String, Object> reportData, ReportFilterDto filters) throws IOException {
        csvPrinter.printRecord("Report Generated", reportData.get("generatedAt"));

        if (filters.getDepartment() != null && !filters.getDepartment().isEmpty()) {
            csvPrinter.printRecord("Department", filters.getDepartment());
        }

        if (filters.getStartDate() != null && filters.getEndDate() != null) {
            csvPrinter.printRecord("Date Range", filters.getStartDate() + " to " + filters.getEndDate());
        }

        if (filters.getTimeRange() != null) {
            csvPrinter.printRecord("Time Range", filters.getTimeRange());
        }
    }

    private void addTaskSummarySection(CSVPrinter csvPrinter, TaskSummaryReportDto taskSummary) throws IOException {
        csvPrinter.printRecord("TASK SUMMARY");
        csvPrinter.printRecord("Status", "Count", "Percentage");
        csvPrinter.printRecord("Total Tasks", taskSummary.getTotalTasks(), "100%");
        csvPrinter.printRecord("Completed", taskSummary.getCompletedTasks(),
                String.format("%.1f%%", taskSummary.getCompletedPercentage()));
        csvPrinter.printRecord("In Progress", taskSummary.getInProgressTasks(),
                String.format("%.1f%%", taskSummary.getInProgressPercentage()));
        csvPrinter.printRecord("To Do", taskSummary.getTodoTasks(),
                String.format("%.1f%%", taskSummary.getTodoPercentage()));
        csvPrinter.printRecord("Blocked", taskSummary.getBlockedTasks(),
                String.format("%.1f%%", taskSummary.getBlockedPercentage()));

        // Add department breakdown if available
        if (taskSummary.getDepartmentBreakdown() != null && !taskSummary.getDepartmentBreakdown().isEmpty()) {
            csvPrinter.println();
            csvPrinter.printRecord("TASKS BY DEPARTMENT");
            csvPrinter.printRecord("Department", "Total", "Completed", "In Progress", "To Do", "Blocked");

            taskSummary.getDepartmentBreakdown().forEach((dept, counts) -> {
                try {
                    csvPrinter.printRecord(dept, counts.getTotal(), counts.getCompleted(),
                            counts.getInProgress(), counts.getTodo(), counts.getBlocked());
                } catch (IOException e) {
                    log.error("Error writing department breakdown", e);
                }
            });
        }

        // Add project breakdown if available
        if (taskSummary.getProjectBreakdown() != null && !taskSummary.getProjectBreakdown().isEmpty()) {
            csvPrinter.println();
            csvPrinter.printRecord("TASKS BY PROJECT");
            csvPrinter.printRecord("Project", "Total", "Completed", "In Progress", "To Do", "Blocked");

            taskSummary.getProjectBreakdown().forEach((project, counts) -> {
                try {
                    csvPrinter.printRecord(project, counts.getTotal(), counts.getCompleted(),
                            counts.getInProgress(), counts.getTodo(), counts.getBlocked());
                } catch (IOException e) {
                    log.error("Error writing project breakdown", e);
                }
            });
        }
    }

    private void addTimeAnalyticsSection(CSVPrinter csvPrinter, TimeAnalyticsReportDto timeAnalytics) throws IOException {
        csvPrinter.printRecord("TIME ANALYTICS");
        csvPrinter.printRecord("Metric", "Value");
        csvPrinter.printRecord("Total Hours",
                timeAnalytics.getTotalHours() != null ?
                String.format("%.1f", timeAnalytics.getTotalHours().doubleValue()) : "0.0");

        // Add hours by department
        if (timeAnalytics.getHoursByDepartment() != null && !timeAnalytics.getHoursByDepartment().isEmpty()) {
            csvPrinter.println();
            csvPrinter.printRecord("HOURS BY DEPARTMENT");
            csvPrinter.printRecord("Department", "Hours");

            timeAnalytics.getHoursByDepartment().forEach((dept, hours) -> {
                try {
                    csvPrinter.printRecord(dept,
                            hours != null ? String.format("%.1f", hours.doubleValue()) : "0.0");
                } catch (IOException e) {
                    log.error("Error writing hours by department", e);
                }
            });
        }

        // Add hours by project
        if (timeAnalytics.getHoursByProject() != null && !timeAnalytics.getHoursByProject().isEmpty()) {
            csvPrinter.println();
            csvPrinter.printRecord("HOURS BY PROJECT");
            csvPrinter.printRecord("Project", "Hours");

            timeAnalytics.getHoursByProject().forEach((project, hours) -> {
                try {
                    csvPrinter.printRecord(project,
                            hours != null ? String.format("%.1f", hours.doubleValue()) : "0.0");
                } catch (IOException e) {
                    log.error("Error writing hours by project", e);
                }
            });
        }
    }

    private void addStaffBreakdownSection(CSVPrinter csvPrinter, List<StaffBreakdownDto> staffBreakdown) throws IOException {
        csvPrinter.printRecord("STAFF BREAKDOWN");
        csvPrinter.printRecord("Staff Name", "Department", "To Do", "In Progress", "Completed", "Blocked", "Logged Hours");

        for (StaffBreakdownDto staff : staffBreakdown) {
            csvPrinter.printRecord(
                    staff.getUserName(),
                    staff.getDepartment(),
                    staff.getTodoTasks(),
                    staff.getInProgressTasks(),
                    staff.getCompletedTasks(),
                    staff.getBlockedTasks(),
                    staff.getLoggedHours() != null ?
                    String.format("%.1f", staff.getLoggedHours().doubleValue()) : "0.0"
            );
        }
    }

    @Override
    public String getContentType() {
        return "text/csv";
    }

    @Override
    public String getFileExtension() {
        return "csv";
    }
}
