package com.spmorangle.crm.reporting.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.spmorangle.crm.reporting.dto.ReportFilterDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * JSON report exporter
 * Exports report data as formatted JSON
 */
@Slf4j
@Component
public class JsonReportExporter implements ReportExporter {
    
    private final ObjectMapper objectMapper;
    
    public JsonReportExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true); // Pretty print
    }
    
    @Override
    public byte[] export(Map<String, Object> reportData, ReportFilterDto filters) {
        try {
            log.debug("Exporting report as JSON");
            return objectMapper.writeValueAsBytes(reportData);
        } catch (Exception e) {
            log.error("Error exporting report as JSON", e);
            throw new RuntimeException("Failed to export report as JSON: " + e.getMessage(), e);
        }
    }
    
    @Override
    public String getContentType() {
        return "application/json";
    }
    
    @Override
    public String getFileExtension() {
        return "json";
    }
}

