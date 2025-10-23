# Report Export System

## 📦 Overview

This package provides an extensible export system for generating reports in multiple formats (JSON, CSV, PDF).

**Current Status:**
- ✅ JSON Export: **Fully Implemented**
- ⏳ CSV Export: **To Be Implemented**
- ⏳ PDF Export: **To Be Implemented**

---

## 🏗️ Architecture

### **Strategy Pattern**

```
ReportExportService (Factory)
    ├── JsonReportExporter (✅ Implemented)
    ├── CsvReportExporter  (⏳ To Implement)
    └── PdfReportExporter  (⏳ To Implement)
```

All exporters implement the `ReportExporter` interface.

---

## 🔌 How to Add a New Export Format

### Step 1: Create Your Exporter Class

Create a new file: `CsvReportExporter.java` or `PdfReportExporter.java`

```java
package com.spmorangle.crm.reporting.export;

import com.spmorangle.crm.reporting.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CsvReportExporter implements ReportExporter {
    
    @Override
    public byte[] export(Map<String, Object> reportData, ReportFilterDto filters) {
        try {
            log.debug("Exporting report as CSV");
            StringBuilder csv = new StringBuilder();
            
            // 1. Add header with metadata
            csv.append("Report Generated,").append(reportData.get("generatedAt")).append("\n");
            csv.append("Department,").append(filters.getDepartment()).append("\n\n");
            
            // 2. Add overall task summary
            TaskSummaryReportDto summary = (TaskSummaryReportDto) reportData.get("taskSummary");
            csv.append("Overall Task Summary\n");
            csv.append("Metric,Value,Percentage\n");
            csv.append("Total Tasks,").append(summary.getTotalTasks()).append(",100%\n");
            csv.append("Completed,").append(summary.getCompletedTasks()).append(",")
               .append(summary.getCompletedPercentage()).append("%\n");
            // ... add more metrics
            
            // 3. Add time-series data if exists
            @SuppressWarnings("unchecked")
            List<TimeSeriesDataPoint> timeSeries = 
                (List<TimeSeriesDataPoint>) reportData.get("timeSeriesData");
            
            if (timeSeries != null && !timeSeries.isEmpty()) {
                csv.append("\nTime Series Breakdown\n");
                csv.append("Period,Total Tasks,Completed,In Progress,To Do,Blocked,Hours\n");
                
                for (TimeSeriesDataPoint point : timeSeries) {
                    csv.append(point.getPeriodLabel()).append(",")
                       .append(point.getTaskSummary().getTotalTasks()).append(",")
                       .append(point.getTaskSummary().getCompletedTasks()).append(",")
                       .append(point.getTaskSummary().getInProgressTasks()).append(",")
                       .append(point.getTaskSummary().getTodoTasks()).append(",")
                       .append(point.getTaskSummary().getBlockedTasks()).append(",")
                       .append(point.getTimeAnalytics().getTotalHours()).append("\n");
                }
            }
            
            // 4. Add department breakdown
            if (summary.getDepartmentBreakdown() != null) {
                csv.append("\nDepartment Breakdown\n");
                csv.append("Department,Total,Completed,In Progress,To Do,Blocked\n");
                
                summary.getDepartmentBreakdown().forEach((dept, counts) -> {
                    csv.append(dept).append(",")
                       .append(counts.getTotal()).append(",")
                       .append(counts.getCompleted()).append(",")
                       .append(counts.getInProgress()).append(",")
                       .append(counts.getTodo()).append(",")
                       .append(counts.getBlocked()).append("\n");
                });
            }
            
            return csv.toString().getBytes(StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            log.error("Error exporting report as CSV", e);
            throw new RuntimeException("Failed to export report as CSV: " + e.getMessage(), e);
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
```

### Step 2: Update ReportExportService

In `ReportExportService.java`, inject your new exporter:

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {
    
    private final JsonReportExporter jsonExporter;
    private final CsvReportExporter csvExporter;  // ← ADD THIS
    // private final PdfReportExporter pdfExporter;  // ← ADD THIS LATER
    
    // ... rest of code
}
```

### Step 3: Update the Switch Statement

In the `selectExporter()` method, replace the exception with your exporter:

```java
private ReportExporter selectExporter(ReportFilterDto.ExportFormat format) {
    return switch (format) {
        case JSON -> jsonExporter;
        case CSV -> csvExporter;  // ← CHANGE THIS (was throwing exception)
        case PDF -> throw new UnsupportedOperationException(
                "PDF export is not yet implemented. Please implement PdfReportExporter.");
    };
}
```

### Step 4: Test Your Exporter

```bash
POST /api/reports/generate
{
  "department": "Software",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "timeRange": "MONTHLY",
  "exportFormat": "CSV"  // ← Your new format
}

# Should download a file: report_software_2025-01-01_to_2025-12-31_monthly.csv
```

---

## 📄 For PDF Export (Using iText 7)

### Add Dependency to `pom.xml`

```xml
<dependency>
    <groupId>com.itextpdf</groupId>
    <artifactId>itext7-core</artifactId>
    <version>7.2.5</version>
    <type>pom</type>
</dependency>
```

### PDF Exporter Template

```java
@Slf4j
@Component
public class PdfReportExporter implements ReportExporter {
    
    @Override
    public byte[] export(Map<String, Object> reportData, ReportFilterDto filters) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {
            
            // 1. Add title
            document.add(new Paragraph("Task Summary Report")
                .setFontSize(20)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("Generated: " + reportData.get("generatedAt"))
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("\n")); // Spacing
            
            // 2. Add filters section
            document.add(new Paragraph("Filters Applied")
                .setFontSize(14)
                .setBold());
            
            document.add(new Paragraph("Department: " + 
                (filters.getDepartment() != null ? filters.getDepartment() : "All")));
            document.add(new Paragraph("Date Range: " + 
                filters.getStartDate() + " to " + filters.getEndDate()));
            
            document.add(new Paragraph("\n"));
            
            // 3. Add overall summary table
            TaskSummaryReportDto summary = 
                (TaskSummaryReportDto) reportData.get("taskSummary");
            
            document.add(new Paragraph("Overall Summary")
                .setFontSize(14)
                .setBold());
            
            Table summaryTable = new Table(3);
            summaryTable.setWidth(UnitValue.createPercentValue(100));
            
            // Headers
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("Metric").setBold()));
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("Count").setBold()));
            summaryTable.addHeaderCell(new Cell().add(new Paragraph("Percentage").setBold()));
            
            // Data rows
            summaryTable.addCell("Total Tasks");
            summaryTable.addCell(String.valueOf(summary.getTotalTasks()));
            summaryTable.addCell("100%");
            
            summaryTable.addCell("Completed");
            summaryTable.addCell(String.valueOf(summary.getCompletedTasks()));
            summaryTable.addCell(summary.getCompletedPercentage() + "%");
            
            summaryTable.addCell("In Progress");
            summaryTable.addCell(String.valueOf(summary.getInProgressTasks()));
            summaryTable.addCell(summary.getInProgressPercentage() + "%");
            
            // Add more rows...
            
            document.add(summaryTable);
            
            // 4. Add time-series chart/table if exists
            @SuppressWarnings("unchecked")
            List<TimeSeriesDataPoint> timeSeries = 
                (List<TimeSeriesDataPoint>) reportData.get("timeSeriesData");
            
            if (timeSeries != null && !timeSeries.isEmpty()) {
                document.add(new Paragraph("\n"));
                document.add(new Paragraph("Time Series Breakdown")
                    .setFontSize(14)
                    .setBold());
                
                Table timeSeriesTable = new Table(7);
                timeSeriesTable.setWidth(UnitValue.createPercentValue(100));
                
                // Headers
                timeSeriesTable.addHeaderCell("Period");
                timeSeriesTable.addHeaderCell("Total");
                timeSeriesTable.addHeaderCell("Completed");
                timeSeriesTable.addHeaderCell("In Progress");
                timeSeriesTable.addHeaderCell("To Do");
                timeSeriesTable.addHeaderCell("Blocked");
                timeSeriesTable.addHeaderCell("Hours");
                
                // Data
                for (TimeSeriesDataPoint point : timeSeries) {
                    timeSeriesTable.addCell(point.getPeriodLabel());
                    timeSeriesTable.addCell(String.valueOf(
                        point.getTaskSummary().getTotalTasks()));
                    timeSeriesTable.addCell(String.valueOf(
                        point.getTaskSummary().getCompletedTasks()));
                    timeSeriesTable.addCell(String.valueOf(
                        point.getTaskSummary().getInProgressTasks()));
                    timeSeriesTable.addCell(String.valueOf(
                        point.getTaskSummary().getTodoTasks()));
                    timeSeriesTable.addCell(String.valueOf(
                        point.getTaskSummary().getBlockedTasks()));
                    timeSeriesTable.addCell(String.valueOf(
                        point.getTimeAnalytics().getTotalHours()));
                }
                
                document.add(timeSeriesTable);
            }
            
            // Close and return
            document.close();
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error exporting report as PDF", e);
            throw new RuntimeException("Failed to export report as PDF: " + e.getMessage(), e);
        }
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
```

---

## 🧪 Testing Your Export

### Test JSON (Already Works)

```bash
POST /api/reports/generate
{
  "department": "Software",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "exportFormat": null  # or "JSON"
}

# Returns JSON response directly
```

### Test CSV (After Implementation)

```bash
POST /api/reports/generate
{
  "department": "Software",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "exportFormat": "CSV"
}

# Downloads file: report_software_2025-01-01_to_2025-12-31.csv
```

### Test PDF (After Implementation)

```bash
POST /api/reports/generate
{
  "department": "Software",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "timeRange": "QUARTERLY",
  "exportFormat": "PDF"
}

# Downloads file: report_software_2025-01-01_to_2025-12-31_quarterly.pdf
```

---

## 📊 Data Structure Reference

### Report Data Map

```java
Map<String, Object> reportData = {
    "taskSummary": TaskSummaryReportDto,      // Overall task metrics
    "timeAnalytics": TimeAnalyticsReportDto,  // Overall time metrics
    "timeSeriesData": List<TimeSeriesDataPoint>,  // Period-by-period (optional)
    "filters": ReportFilterDto,               // Request filters
    "generatedAt": OffsetDateTime             // Timestamp
}
```

### Available Data Points

- `taskSummary.getTotalTasks()` - Total task count
- `taskSummary.getCompletedTasks()` - Completed count
- `taskSummary.getDepartmentBreakdown()` - Map<String, TaskStatusCounts>
- `taskSummary.getProjectBreakdown()` - Map<String, TaskStatusCounts>
- `timeAnalytics.getTotalHours()` - Sum of logged hours
- `timeAnalytics.getHoursByDepartment()` - Map<String, BigDecimal>
- `timeAnalytics.getProjectDetails()` - Map<String, ProjectTimeDetails>
- `timeSeriesData[i].getPeriod()` - Period ID ("2025-W01", "2025-01")
- `timeSeriesData[i].getPeriodLabel()` - Human-readable ("Week 1", "January 2025")
- `timeSeriesData[i].getTaskSummary()` - TaskSummaryReportDto for that period
- `timeSeriesData[i].getTimeAnalytics()` - TimeAnalyticsReportDto for that period

---

## 🎨 Tips for Implementation

### CSV Best Practices

1. **Include metadata at top**: Date generated, filters applied
2. **Use clear section headers**: "Overall Summary", "Time Series Breakdown"
3. **Add blank lines** between sections for readability
4. **Escape commas**: Wrap fields with commas in quotes
5. **Handle nulls**: Show "N/A" or "-" instead of "null"

### PDF Best Practices

1. **Use tables** for structured data
2. **Add page breaks** between major sections
3. **Include charts/graphs** if possible (using JFreeChart + iText)
4. **Add footer** with page numbers
5. **Use colors** to highlight key metrics (green for good, red for blocked)
6. **Include logo** if available

---

## 🚀 Advanced: Adding Charts to PDF

For visual reports, you can combine JFreeChart with iText:

```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.jfree</groupId>
    <artifactId>jfreechart</artifactId>
    <version>1.5.4</version>
</dependency>
```

```java
// Create a chart
DefaultPieDataset dataset = new DefaultPieDataset();
dataset.setValue("Completed", summary.getCompletedTasks());
dataset.setValue("In Progress", summary.getInProgressTasks());
dataset.setValue("To Do", summary.getTodoTasks());

JFreeChart chart = ChartFactory.createPieChart(
    "Task Status Distribution",
    dataset,
    true, true, false
);

// Convert to image
BufferedImage chartImage = chart.createBufferedImage(500, 300);
ByteArrayOutputStream chartStream = new ByteArrayOutputStream();
ImageIO.write(chartImage, "PNG", chartStream);

// Add to PDF
ImageData imageData = ImageDataFactory.create(chartStream.toByteArray());
document.add(new Image(imageData));
```

---

## 📝 Checklist for New Export Format

- [ ] Create new exporter class implementing `ReportExporter`
- [ ] Add `@Component` annotation
- [ ] Implement `export()` method
- [ ] Implement `getContentType()` method
- [ ] Implement `getFileExtension()` method
- [ ] Inject exporter in `ReportExportService`
- [ ] Update `selectExporter()` switch statement
- [ ] Test with various filter combinations
- [ ] Test with time-series data (weekly/monthly/quarterly)
- [ ] Test with empty data (no tasks/hours)
- [ ] Add logging for debugging
- [ ] Handle exceptions gracefully

---

## 🐛 Troubleshooting

### "UnsupportedOperationException" Error

**Cause**: Exporter not implemented yet  
**Fix**: Implement the exporter class and update `selectExporter()`

### File Not Downloading

**Cause**: Missing `Content-Disposition` header  
**Fix**: Ensure `ReportExportService.exportReport()` sets the header correctly

### Empty/Corrupted File

**Cause**: Exception during export but not caught  
**Fix**: Add try-catch in your export() method and log errors

### Wrong Content Type

**Cause**: Browser doesn't recognize format  
**Fix**: Check `getContentType()` returns correct MIME type

---

## 📚 Resources

- **iText 7 Documentation**: https://itextpdf.com/en/resources/api-documentation
- **Apache Commons CSV**: https://commons.apache.org/proper/commons-csv/
- **JFreeChart**: https://www.jfree.org/jfreechart/

---

## ✅ Current Implementation Status

| Format | Status | Exporter Class | Notes |
|--------|--------|---------------|-------|
| JSON | ✅ Complete | `JsonReportExporter` | Uses Jackson, pretty-printed |
| CSV | ⏳ Pending | `CsvReportExporter` | Template provided above |
| PDF | ⏳ Pending | `PdfReportExporter` | Template provided above, requires iText dependency |

---

**Ready to implement CSV or PDF?** Just follow the steps above! The architecture is designed to be plug-and-play. 🚀

