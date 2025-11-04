# Export System Architecture

## 🏗️ System Diagram

```
┌──────────────────────────────────────────────────────────────┐
│                        Client Request                         │
│   POST /api/reports/generate                                 │
│   { department, startDate, endDate, timeRange, exportFormat } │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                     ReportController                          │
│  - Validates request                                         │
│  - Gets user context (role-based access)                     │
│  - Generates report data:                                    │
│    • taskSummary (overall metrics)                           │
│    • timeAnalytics (time tracking)                           │
│    • timeSeriesData (period breakdown)                       │
└───────────────────────────┬──────────────────────────────────┘
                            │
                            ▼
┌──────────────────────────────────────────────────────────────┐
│                  ReportExportService (Factory)                │
│  - Routes to correct exporter based on exportFormat          │
│  - Generates descriptive filename                            │
│  - Sets HTTP headers (Content-Type, Content-Disposition)     │
└────────────┬──────────────┬──────────────┬──────────────────┘
             │              │              │
   ┌─────────┘              │              └─────────┐
   ▼                        ▼                        ▼
┌─────────────┐      ┌─────────────┐       ┌─────────────┐
│    JSON     │      │     CSV     │       │     PDF     │
│  Exporter   │      │  Exporter   │       │  Exporter   │
│             │      │             │       │             │
│ ✅ DONE     │      │ ⏳ TODO     │       │ ⏳ TODO     │
│             │      │             │       │             │
│ Uses:       │      │ Uses:       │       │ Uses:       │
│ - Jackson   │      │ - String    │       │ - iText 7   │
│ - Pretty    │      │ - Builder   │       │ - Tables    │
│   Print     │      │ - CSV       │       │ - Charts    │
│             │      │   Format    │       │   (optional)│
└─────────────┘      └─────────────┘       └─────────────┘
       │                    │                      │
       └────────────────────┴──────────────────────┘
                            │
                            ▼
                   ┌──────────────┐
                   │   Response   │
                   │              │
                   │ If JSON &    │
                   │ no format:   │
                   │ → JSON body  │
                   │              │
                   │ If CSV/PDF:  │
                   │ → File       │
                   │   download   │
                   └──────────────┘
```

---

## 🔄 Request Flow

### Scenario 1: JSON Response (No Download)

```
1. Client → POST /api/reports/generate { "department": "Software" }
2. ReportController → Generate data
3. ReportController → ReportExportService.exportReport(data, filters)
4. ReportExportService → filters.exportFormat = null
5. ReportExportService → Return ResponseEntity.ok(reportData)
6. Client ← JSON in response body
```

### Scenario 2: File Download (CSV/PDF)

```
1. Client → POST /api/reports/generate { "exportFormat": "CSV" }
2. ReportController → Generate data
3. ReportController → ReportExportService.exportReport(data, filters)
4. ReportExportService → filters.exportFormat = CSV
5. ReportExportService → selectExporter(CSV) → CsvReportExporter
6. CsvReportExporter → export(data) → byte[]
7. ReportExportService → Add headers:
   - Content-Disposition: attachment; filename="report.csv"
   - Content-Type: text/csv
8. Client ← File download initiated
```

---

## 📦 Package Structure

```
com.spmorangle.crm.reporting.export/
├── ReportExporter.java              # Interface
├── JsonReportExporter.java          # ✅ Implemented
├── CsvReportExporter.java           # ⏳ To implement
├── PdfReportExporter.java           # ⏳ To implement
├── ReportExportService.java         # Factory/Router
├── README.md                        # Implementation guide
├── IMPLEMENTATION_SUMMARY.md        # What was done
├── TESTING_GUIDE.md                 # How to test
└── ARCHITECTURE.md                  # This file
```

---

## 🎯 Design Patterns Used

### 1. **Strategy Pattern**

Each export format is a strategy:

```java
public interface ReportExporter {
    byte[] export(...);
    String getContentType();
    String getFileExtension();
}
```

Allows runtime selection of export strategy without changing client code.

### 2. **Factory Pattern**

`ReportExportService` acts as a factory:

```java
private ReportExporter selectExporter(ExportFormat format) {
    return switch (format) {
        case JSON -> jsonExporter;
        case CSV -> csvExporter;
        case PDF -> pdfExporter;
    };
}
```

Centralizes object creation logic.

### 3. **Dependency Injection**

All exporters are Spring `@Component`s:

```java
@Component
public class JsonReportExporter implements ReportExporter { ... }
```

Spring automatically injects them into `ReportExportService`.

---

## 🔌 Integration Points

### Input: ReportFilterDto

```java
public class ReportFilterDto {
    private String department;
    private List<Long> projectIds;
    private LocalDate startDate;
    private LocalDate endDate;
    private TimeRange timeRange;  // WEEKLY, MONTHLY, QUARTERLY, YEARLY
    private ExportFormat exportFormat;  // JSON, CSV, PDF
}
```

### Output: Map<String, Object>

```java
Map<String, Object> reportData = {
    "taskSummary": TaskSummaryReportDto,
    "timeAnalytics": TimeAnalyticsReportDto,
    "timeSeriesData": List<TimeSeriesDataPoint>,
    "filters": ReportFilterDto,
    "generatedAt": OffsetDateTime
}
```

### Response Types

```java
// JSON Response
ResponseEntity<Map<String, Object>>

// File Download
ResponseEntity<byte[]>
    .header("Content-Disposition", "attachment; filename=...")
    .contentType(MediaType.parseMediaType(...))
```

---

## 🛠️ Extension Points

To add a new export format (e.g., Excel):

### 1. Create Exporter

```java
@Component
public class ExcelReportExporter implements ReportExporter {
    @Override
    public byte[] export(Map<String, Object> reportData, ReportFilterDto filters) {
        // Use Apache POI to create Excel file
    }
    
    @Override
    public String getContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }
    
    @Override
    public String getFileExtension() {
        return "xlsx";
    }
}
```

### 2. Add to ExportFormat Enum

```java
// In ReportFilterDto.java
public enum ExportFormat {
    JSON, CSV, PDF, EXCEL  // ← Add this
}
```

### 3. Inject into Service

```java
@Service
public class ReportExportService {
    private final JsonReportExporter jsonExporter;
    private final CsvReportExporter csvExporter;
    private final PdfReportExporter pdfExporter;
    private final ExcelReportExporter excelExporter;  // ← Add this
}
```

### 4. Update Switch

```java
private ReportExporter selectExporter(ExportFormat format) {
    return switch (format) {
        case JSON -> jsonExporter;
        case CSV -> csvExporter;
        case PDF -> pdfExporter;
        case EXCEL -> excelExporter;  // ← Add this
    };
}
```

**That's it!** The system automatically routes requests to your new exporter.

---

## 📊 Data Flow Diagram

```
┌─────────────────────────────────────────────────────┐
│              Report Data Generation                  │
│  (ReportService - existing implementation)          │
│                                                      │
│  1. Query database                                  │
│  2. Aggregate task counts by status                 │
│  3. Calculate time analytics                        │
│  4. Generate time-series data (if timeRange set)    │
│  5. Apply role-based filtering (HR vs Manager)      │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│            Report Data Structure                     │
│                                                      │
│  {                                                   │
│    "taskSummary": {                                 │
│      "totalTasks": 7,                               │
│      "completedTasks": 3,                           │
│      "departmentBreakdown": { ... },                │
│      "projectBreakdown": { ... }                    │
│    },                                                │
│    "timeAnalytics": {                               │
│      "totalHours": 0.02,                            │
│      "hoursByDepartment": { ... },                  │
│      "projectDetails": { ... }                      │
│    },                                                │
│    "timeSeriesData": [                              │
│      { "period": "2025-01", ... },                  │
│      { "period": "2025-02", ... }                   │
│    ]                                                 │
│  }                                                   │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│           Export Transformation                      │
│                                                      │
│  JSON:  → Jackson serialization                     │
│  CSV:   → Flatten to rows and columns               │
│  PDF:   → Layout with tables and formatting         │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│              HTTP Response                           │
│                                                      │
│  JSON (no format): ResponseEntity<Map>              │
│  File download:    ResponseEntity<byte[]>           │
│    + Content-Disposition header                     │
│    + Content-Type header                            │
└─────────────────────────────────────────────────────┘
```

---

## 🔒 Security Considerations

### 1. Role-Based Access Control

All export endpoints require `@PreAuthorize("hasRole('HR')")`:

- **HR**: Can export reports for all departments
- **Manager**: No access (403 Forbidden)
- **Staff**: No access (403 Forbidden)

### 2. Data Filtering

`ReportService` applies filtering based on user role:

```java
private String applyDepartmentFilter(String requestedDepartment, User user) {
    if (user.getRole() == Role.HR) {
        return requestedDepartment;  // HR can see all
    }
    throw new RuntimeException("Access denied: Only HR users can access reports");
}
```

### 3. File Size Limits

Consider adding limits to prevent memory issues:

```java
public byte[] export(Map<String, Object> reportData, ReportFilterDto filters) {
    // Check data size before processing
    int taskCount = getTotalTaskCount(reportData);
    if (taskCount > 10000) {
        throw new IllegalArgumentException("Report too large. Please narrow filters.");
    }
    // ... proceed with export
}
```

---

## 📈 Performance Considerations

### 1. Lazy Loading

Time-series data is only generated if `timeRange` is specified:

```java
if (filters.getTimeRange() != null) {
    timeSeriesData = reportService.generateTimeSeriesData(filters, userId);
}
```

### 2. Streaming for Large Files

For very large exports, consider streaming:

```java
@GetMapping("/export/large")
public StreamingResponseBody exportLarge() {
    return outputStream -> {
        // Write data in chunks
        csvExporter.exportStreaming(reportData, outputStream);
    };
}
```

### 3. Caching

Cache frequently requested reports:

```java
@Cacheable(value = "reports", key = "#filters")
public Map<String, Object> generateReport(ReportFilterDto filters) {
    // Expensive operation
}
```

---

## 🧪 Testing Strategy

### Unit Tests

Test each exporter independently:

```java
@Test
void testJsonExport() {
    Map<String, Object> data = createTestData();
    byte[] result = jsonExporter.export(data, filters);
    assertThat(result).isNotEmpty();
    // Verify JSON structure
}
```

### Integration Tests

Test the full flow:

```java
@Test
void testReportGenerationWithCsvExport() {
    ReportFilterDto filters = new ReportFilterDto();
    filters.setExportFormat(ExportFormat.CSV);
    
    ResponseEntity<?> response = reportController.generateReport(filters);
    
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().get("Content-Disposition"))
        .contains("attachment");
}
```

---

## 📚 Related Documentation

- [Implementation Summary](./IMPLEMENTATION_SUMMARY.md) - What was built
- [README](./README.md) - How to extend the system
- [Testing Guide](./TESTING_GUIDE.md) - How to test exports

---

## 🎯 Summary

**Key Strengths:**
- ✅ Extensible design (Strategy + Factory patterns)
- ✅ Clean separation of concerns
- ✅ Easy to add new formats
- ✅ Consistent error handling
- ✅ Role-based security built-in
- ✅ Works with existing report data structure

**What's Next:**
- Implement CSV exporter (2-3 hours)
- Implement PDF exporter (4-6 hours)
- Add unit tests
- Add integration tests
- Consider Excel export for advanced users

---

**Architecture Status**: ✅ Production-ready foundation  
**Extensibility**: ✅ New formats can be added in < 1 hour  
**Maintainability**: ✅ Clear structure, well-documented

---

Happy architecting! 🏗️

