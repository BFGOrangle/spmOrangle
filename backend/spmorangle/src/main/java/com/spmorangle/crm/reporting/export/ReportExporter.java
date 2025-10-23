package com.spmorangle.crm.reporting.export;

import com.spmorangle.crm.reporting.dto.ReportFilterDto;

import java.util.Map;

/**
 * Interface for exporting reports in different formats (JSON, CSV, PDF)
 * Implementations should handle the conversion of report data to the target format
 */
public interface ReportExporter {
    
    /**
     * Export report data to the target format
     * @param reportData Complete report data including taskSummary, timeAnalytics, filters, etc.
     * @param filters The filters used to generate the report
     * @return Byte array of the exported data
     */
    byte[] export(Map<String, Object> reportData, ReportFilterDto filters);
    
    /**
     * Get the content type for HTTP response headers
     * @return Content type string (e.g., "application/json", "text/csv", "application/pdf")
     */
    String getContentType();
    
    /**
     * Get the file extension for the exported file
     * @return File extension without dot (e.g., "json", "csv", "pdf")
     */
    String getFileExtension();
}

