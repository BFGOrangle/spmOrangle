package com.spmorangle.crm.reporting.export;

import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Service for exporting reports in different formats
 * Acts as a factory to select the appropriate exporter based on the export format
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {
    
    private final JsonReportExporter jsonExporter;
    // Future exporters will be injected here:
    // private final CsvReportExporter csvExporter;
    // private final PdfReportExporter pdfExporter;
    
    /**
     * Export report data in the requested format
     * @param reportData Complete report data
     * @param filters Report filters (includes exportFormat)
     * @return ResponseEntity with appropriate content type and file attachment
     */
    public ResponseEntity<?> exportReport(Map<String, Object> reportData, ReportFilterDto filters) {
        ReportFilterDto.ExportFormat format = filters.getExportFormat();
        
        // If no export format specified or JSON, return as regular JSON response
        if (format == null || format == ReportFilterDto.ExportFormat.JSON) {
            log.debug("Returning report as JSON response (no download)");
            return ResponseEntity.ok(reportData);
        }
        
        // Select appropriate exporter based on format
        ReportExporter exporter = selectExporter(format);
        
        // Export the data
        byte[] exportedData = exporter.export(reportData, filters);
        
        // Generate filename with timestamp
        String filename = generateFilename(filters, exporter.getFileExtension());
        
        log.info("Exporting report as {} (filename: {})", format, filename);
        
        // Return as file download
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(exporter.getContentType()))
                .contentLength(exportedData.length)
                .body(exportedData);
    }
    
    /**
     * Select the appropriate exporter based on the export format
     * @param format Export format
     * @return ReportExporter implementation
     */
    private ReportExporter selectExporter(ReportFilterDto.ExportFormat format) {
        return switch (format) {
            case JSON -> jsonExporter;
            case CSV -> throw new UnsupportedOperationException(
                    "CSV export is not yet implemented. Please implement CsvReportExporter.");
            case PDF -> throw new UnsupportedOperationException(
                    "PDF export is not yet implemented. Please implement PdfReportExporter.");
        };
        
        // When CSV and PDF are implemented, replace above with:
        /*
        return switch (format) {
            case JSON -> jsonExporter;
            case CSV -> csvExporter;
            case PDF -> pdfExporter;
        };
        */
    }
    
    /**
     * Generate a descriptive filename for the exported report
     * @param filters Report filters
     * @param extension File extension
     * @return Generated filename
     */
    private String generateFilename(ReportFilterDto filters, String extension) {
        StringBuilder filename = new StringBuilder("report");
        
        // Add department if specified
        if (filters.getDepartment() != null && !filters.getDepartment().isEmpty()) {
            filename.append("_").append(filters.getDepartment().toLowerCase().replaceAll("\\s+", "-"));
        }
        
        // Add date range if specified
        if (filters.getStartDate() != null || filters.getEndDate() != null) {
            filename.append("_");
            if (filters.getStartDate() != null) {
                filename.append(filters.getStartDate().format(DateTimeFormatter.ISO_DATE));
            }
            filename.append("_to_");
            if (filters.getEndDate() != null) {
                filename.append(filters.getEndDate().format(DateTimeFormatter.ISO_DATE));
            }
        } else {
            // Add current date if no date range specified
            filename.append("_").append(LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        }
        
        // Add time range if specified
        if (filters.getTimeRange() != null) {
            filename.append("_").append(filters.getTimeRange().name().toLowerCase());
        }
        
        filename.append(".").append(extension);
        
        return filename.toString();
    }
}

