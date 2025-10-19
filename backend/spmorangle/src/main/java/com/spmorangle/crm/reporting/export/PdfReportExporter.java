package com.spmorangle.crm.reporting.export;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import com.spmorangle.crm.reporting.dto.StaffBreakdownDto;
import com.spmorangle.crm.reporting.dto.TaskSummaryReportDto;
import com.spmorangle.crm.reporting.dto.TimeAnalyticsReportDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * PDF report exporter
 * Exports report data as a professionally formatted PDF document
 */
@Slf4j
@Component
public class PdfReportExporter implements ReportExporter {

    private static final DeviceRgb HEADER_BACKGROUND = new DeviceRgb(41, 98, 255);
    private static final DeviceRgb SECTION_BACKGROUND = new DeviceRgb(240, 242, 245);
    private static final float TITLE_FONT_SIZE = 24f;
    private static final float SECTION_FONT_SIZE = 16f;
    private static final float NORMAL_FONT_SIZE = 10f;

    @Override
    public byte[] export(Map<String, Object> reportData, ReportFilterDto filters) {
        try {
            log.debug("Exporting report as PDF");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Add title and metadata
            addReportHeader(document, reportData, filters);

            // Add Task Summary section
            TaskSummaryReportDto taskSummary = (TaskSummaryReportDto) reportData.get("taskSummary");
            if (taskSummary != null) {
                addTaskSummarySection(document, taskSummary);
            }

            // Add Time Analytics section
            TimeAnalyticsReportDto timeAnalytics = (TimeAnalyticsReportDto) reportData.get("timeAnalytics");
            if (timeAnalytics != null) {
                addTimeAnalyticsSection(document, timeAnalytics);
            }

            // Add Staff Breakdown section
            @SuppressWarnings("unchecked")
            List<StaffBreakdownDto> staffBreakdown = (List<StaffBreakdownDto>) reportData.get("staffBreakdown");
            if (staffBreakdown != null && !staffBreakdown.isEmpty()) {
                addStaffBreakdownSection(document, staffBreakdown);
            }

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error exporting report as PDF", e);
            throw new RuntimeException("Failed to export report as PDF: " + e.getMessage(), e);
        }
    }

    private void addReportHeader(Document document, Map<String, Object> reportData, ReportFilterDto filters) {
        // Main title
        Paragraph title = new Paragraph("Project Management Report")
                .setFontSize(TITLE_FONT_SIZE)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Metadata table
        Table metadataTable = new Table(UnitValue.createPercentArray(new float[]{30, 70}))
                .useAllAvailableWidth()
                .setMarginBottom(20);

        if (reportData.get("generatedAt") != null) {
            addMetadataRow(metadataTable, "Generated At", reportData.get("generatedAt").toString());
        }

        if (filters.getDepartment() != null && !filters.getDepartment().isEmpty()) {
            addMetadataRow(metadataTable, "Department", filters.getDepartment());
        }

        if (filters.getStartDate() != null && filters.getEndDate() != null) {
            String dateRange = filters.getStartDate().format(DateTimeFormatter.ISO_DATE) +
                    " to " + filters.getEndDate().format(DateTimeFormatter.ISO_DATE);
            addMetadataRow(metadataTable, "Date Range", dateRange);
        }

        if (filters.getTimeRange() != null) {
            addMetadataRow(metadataTable, "Time Range", filters.getTimeRange().toString());
        }

        document.add(metadataTable);
    }

    private void addMetadataRow(Table table, String label, String value) {
        table.addCell(createCell(label, true));
        table.addCell(createCell(value, false));
    }

    private void addTaskSummarySection(Document document, TaskSummaryReportDto taskSummary) {
        // Section title
        addSectionTitle(document, "Task Summary");

        // Summary statistics table
        Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{40, 30, 30}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        // Header row
        summaryTable.addHeaderCell(createHeaderCell("Status"));
        summaryTable.addHeaderCell(createHeaderCell("Count"));
        summaryTable.addHeaderCell(createHeaderCell("Percentage"));

        // Data rows
        addSummaryRow(summaryTable, "Total Tasks", taskSummary.getTotalTasks(), 100.0);
        addSummaryRow(summaryTable, "Completed", taskSummary.getCompletedTasks(), taskSummary.getCompletedPercentage());
        addSummaryRow(summaryTable, "In Progress", taskSummary.getInProgressTasks(), taskSummary.getInProgressPercentage());
        addSummaryRow(summaryTable, "To Do", taskSummary.getTodoTasks(), taskSummary.getTodoPercentage());
        addSummaryRow(summaryTable, "Blocked", taskSummary.getBlockedTasks(), taskSummary.getBlockedPercentage());

        document.add(summaryTable);

        // Department breakdown if available
        if (taskSummary.getDepartmentBreakdown() != null && !taskSummary.getDepartmentBreakdown().isEmpty()) {
            addSubsectionTitle(document, "Tasks by Department");
            Table deptTable = new Table(UnitValue.createPercentArray(new float[]{30, 14, 14, 14, 14, 14}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            deptTable.addHeaderCell(createHeaderCell("Department"));
            deptTable.addHeaderCell(createHeaderCell("Total"));
            deptTable.addHeaderCell(createHeaderCell("Completed"));
            deptTable.addHeaderCell(createHeaderCell("In Progress"));
            deptTable.addHeaderCell(createHeaderCell("To Do"));
            deptTable.addHeaderCell(createHeaderCell("Blocked"));

            taskSummary.getDepartmentBreakdown().forEach((dept, counts) -> {
                deptTable.addCell(createCell(dept, false));
                deptTable.addCell(createCell(String.valueOf(counts.getTotal()), false));
                deptTable.addCell(createCell(String.valueOf(counts.getCompleted()), false));
                deptTable.addCell(createCell(String.valueOf(counts.getInProgress()), false));
                deptTable.addCell(createCell(String.valueOf(counts.getTodo()), false));
                deptTable.addCell(createCell(String.valueOf(counts.getBlocked()), false));
            });

            document.add(deptTable);
        }

        // Project breakdown if available
        if (taskSummary.getProjectBreakdown() != null && !taskSummary.getProjectBreakdown().isEmpty()) {
            addSubsectionTitle(document, "Tasks by Project");
            Table projectTable = new Table(UnitValue.createPercentArray(new float[]{30, 14, 14, 14, 14, 14}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            projectTable.addHeaderCell(createHeaderCell("Project"));
            projectTable.addHeaderCell(createHeaderCell("Total"));
            projectTable.addHeaderCell(createHeaderCell("Completed"));
            projectTable.addHeaderCell(createHeaderCell("In Progress"));
            projectTable.addHeaderCell(createHeaderCell("To Do"));
            projectTable.addHeaderCell(createHeaderCell("Blocked"));

            taskSummary.getProjectBreakdown().forEach((project, counts) -> {
                projectTable.addCell(createCell(project, false));
                projectTable.addCell(createCell(String.valueOf(counts.getTotal()), false));
                projectTable.addCell(createCell(String.valueOf(counts.getCompleted()), false));
                projectTable.addCell(createCell(String.valueOf(counts.getInProgress()), false));
                projectTable.addCell(createCell(String.valueOf(counts.getTodo()), false));
                projectTable.addCell(createCell(String.valueOf(counts.getBlocked()), false));
            });

            document.add(projectTable);
        }
    }

    private void addTimeAnalyticsSection(Document document, TimeAnalyticsReportDto timeAnalytics) {
        addSectionTitle(document, "Time Analytics");

        // Total hours
        String totalHoursText = timeAnalytics.getTotalHours() != null ?
                String.format("Total Hours: %.1f", timeAnalytics.getTotalHours().doubleValue()) :
                "Total Hours: 0.0";
        Paragraph totalHours = new Paragraph(totalHoursText)
                .setFontSize(12)
                .setBold()
                .setMarginBottom(10);
        document.add(totalHours);

        // Hours by department
        if (timeAnalytics.getHoursByDepartment() != null && !timeAnalytics.getHoursByDepartment().isEmpty()) {
            addSubsectionTitle(document, "Hours by Department");
            Table deptTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            deptTable.addHeaderCell(createHeaderCell("Department"));
            deptTable.addHeaderCell(createHeaderCell("Hours"));

            timeAnalytics.getHoursByDepartment().forEach((dept, hours) -> {
                deptTable.addCell(createCell(dept, false));
                String hoursStr = hours != null ?
                        String.format("%.1f", hours.doubleValue()) : "0.0";
                deptTable.addCell(createCell(hoursStr, false));
            });

            document.add(deptTable);
        }

        // Hours by project
        if (timeAnalytics.getHoursByProject() != null && !timeAnalytics.getHoursByProject().isEmpty()) {
            addSubsectionTitle(document, "Hours by Project");
            Table projectTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .useAllAvailableWidth()
                    .setMarginBottom(15);

            projectTable.addHeaderCell(createHeaderCell("Project"));
            projectTable.addHeaderCell(createHeaderCell("Hours"));

            timeAnalytics.getHoursByProject().forEach((project, hours) -> {
                projectTable.addCell(createCell(project, false));
                String hoursStr = hours != null ?
                        String.format("%.1f", hours.doubleValue()) : "0.0";
                projectTable.addCell(createCell(hoursStr, false));
            });

            document.add(projectTable);
        }
    }

    private void addStaffBreakdownSection(Document document, List<StaffBreakdownDto> staffBreakdown) {
        addSectionTitle(document, "Staff Breakdown");

        Table staffTable = new Table(UnitValue.createPercentArray(new float[]{20, 15, 10, 13, 13, 10, 12}))
                .useAllAvailableWidth()
                .setMarginBottom(15);

        staffTable.addHeaderCell(createHeaderCell("Staff Name"));
        staffTable.addHeaderCell(createHeaderCell("Department"));
        staffTable.addHeaderCell(createHeaderCell("To Do"));
        staffTable.addHeaderCell(createHeaderCell("In Progress"));
        staffTable.addHeaderCell(createHeaderCell("Completed"));
        staffTable.addHeaderCell(createHeaderCell("Blocked"));
        staffTable.addHeaderCell(createHeaderCell("Hours"));

        for (StaffBreakdownDto staff : staffBreakdown) {
            staffTable.addCell(createCell(staff.getUserName(), false));
            staffTable.addCell(createCell(staff.getDepartment(), false));
            staffTable.addCell(createCell(String.valueOf(staff.getTodoTasks()), false));
            staffTable.addCell(createCell(String.valueOf(staff.getInProgressTasks()), false));
            staffTable.addCell(createCell(String.valueOf(staff.getCompletedTasks()), false));
            staffTable.addCell(createCell(String.valueOf(staff.getBlockedTasks()), false));
            String hoursStr = staff.getLoggedHours() != null ?
                    String.format("%.1f", staff.getLoggedHours().doubleValue()) : "0.0";
            staffTable.addCell(createCell(hoursStr, false));
        }

        document.add(staffTable);
    }

    private void addSectionTitle(Document document, String title) {
        Paragraph section = new Paragraph(title)
                .setFontSize(SECTION_FONT_SIZE)
                .setBold()
                .setMarginTop(20)
                .setMarginBottom(10);
        document.add(section);
    }

    private void addSubsectionTitle(Document document, String title) {
        Paragraph subsection = new Paragraph(title)
                .setFontSize(12)
                .setBold()
                .setMarginTop(10)
                .setMarginBottom(5);
        document.add(subsection);
    }

    private Cell createHeaderCell(String content) {
        return new Cell()
                .add(new Paragraph(content).setBold().setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(HEADER_BACKGROUND)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(NORMAL_FONT_SIZE)
                .setPadding(5);
    }

    private Cell createCell(String content, boolean isBold) {
        Paragraph paragraph = new Paragraph(content).setFontSize(NORMAL_FONT_SIZE);
        if (isBold) {
            paragraph.setBold();
        }
        return new Cell()
                .add(paragraph)
                .setPadding(5);
    }

    private void addSummaryRow(Table table, String status, Long count, Double percentage) {
        table.addCell(createCell(status, false));
        table.addCell(createCell(String.valueOf(count), false));
        table.addCell(createCell(String.format("%.1f%%", percentage), false));
    }

    @Override
    public String getContentType() {
        return "application/pdf";
    }

    @Override
    public String getFileExtension() {
        return "pdf";
    }
}
