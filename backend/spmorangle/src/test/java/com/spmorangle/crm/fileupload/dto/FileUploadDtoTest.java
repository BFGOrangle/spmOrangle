package com.spmorangle.crm.fileupload.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

@DisplayName("File Upload DTO Test Cases")
class FileUploadDtoTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create valid CreateFileDTO with all required fields")
    void testCreateFileDtoWithValidFields() {
        // Given
        Long id = 1L;
        Long taskId = 123L;
        Long projectId = 456L;
        String fileUrl = "https://example.com/files/test-file.pdf";
        Long createdBy = 789L;

        // When
        CreateFileDTO dto = CreateFileDTO.builder()
                .id(id)
                .taskId(taskId)
                .projectId(projectId)
                .fileUrl(fileUrl)
                .createdBy(createdBy)
                .build();

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(taskId, dto.getTaskId());
        assertEquals(projectId, dto.getProjectId());
        assertEquals(fileUrl, dto.getFileUrl());
        assertEquals(createdBy, dto.getCreatedBy());

        // Validate constraints
        Set<ConstraintViolation<CreateFileDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Should fail validation when taskId is null")
    void testCreateFileDtoWithNullTaskId() {
        // Given
        CreateFileDTO dto = CreateFileDTO.builder()
                .id(1L)
                .taskId(null)
                .projectId(456L)
                .fileUrl("https://example.com/files/test-file.pdf")
                .createdBy(789L)
                .build();

        // When
        Set<ConstraintViolation<CreateFileDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Task ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when projectId is null")
    void testCreateFileDtoWithNullProjectId() {
        // Given
        CreateFileDTO dto = CreateFileDTO.builder()
                .id(1L)
                .taskId(123L)
                .projectId(null)
                .fileUrl("https://example.com/files/test-file.pdf")
                .createdBy(789L)
                .build();

        // When
        Set<ConstraintViolation<CreateFileDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Project ID is required")));
    }

    @Test
    @DisplayName("Should fail validation when required fields are null")
    void testCreateFileDtoWithNullFields() {
        // Given
        CreateFileDTO dto = CreateFileDTO.builder()
                .id(null)
                .taskId(null)
                .projectId(null)
                .fileUrl(null)
                .createdBy(null)
                .build();

        // When
        Set<ConstraintViolation<CreateFileDTO>> violations = validator.validate(dto);

        // Then
        assertFalse(violations.isEmpty());
        assertEquals(5, violations.size()); // All 5 @NotNull fields should fail
    }

    @Test
    @DisplayName("Should create CreateFileResponseDTO with all fields")
    void testCreateFileResponseDto() {
        // Given
        Long id = 1L;
        Long taskId = 123L;
        Long projectId = 456L;
        String fileUrl = "https://example.com/files/test-file.pdf";
        Long createdBy = 789L;
        java.time.OffsetDateTime createdAt = java.time.OffsetDateTime.now();

        // When
        CreateFileResponseDTO responseDto = CreateFileResponseDTO.builder()
                .id(id)
                .taskId(taskId)
                .projectId(projectId)
                .fileUrl(fileUrl)
                .createdBy(createdBy)
                .createdAt(createdAt)
                .build();

        // Then
        assertNotNull(responseDto);
        assertEquals(id, responseDto.getId());
        assertEquals(taskId, responseDto.getTaskId());
        assertEquals(projectId, responseDto.getProjectId());
        assertEquals(fileUrl, responseDto.getFileUrl());
        assertEquals(createdBy, responseDto.getCreatedBy());
        assertEquals(createdAt, responseDto.getCreatedAt());
    }

    @Test
    @DisplayName("Should handle different file URL formats")
    void testDifferentFileUrlFormats() {
        // Test different URL formats
        String[] fileUrls = {
            "https://storage.supabase.co/bucket/files/test.pdf",
            "https://example.com/files/document.docx",
            "http://localhost:8080/files/image.png",
            "https://cdn.example.com/uploads/spreadsheet.xlsx"
        };

        for (String fileUrl : fileUrls) {
            // When
            CreateFileDTO dto = CreateFileDTO.builder()
                    .id(1L)
                    .taskId(123L)
                    .projectId(456L)
                    .fileUrl(fileUrl)
                    .createdBy(789L)
                    .build();

            // Then
            assertEquals(fileUrl, dto.getFileUrl());
            Set<ConstraintViolation<CreateFileDTO>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty());
        }
    }

    @Test
    @DisplayName("Should handle large ID values")
    void testLargeIdValues() {
        // Given
        Long largeId = Long.MAX_VALUE;
        Long largeTaskId = Long.MAX_VALUE - 1;
        Long largeProjectId = Long.MAX_VALUE - 2;
        Long largeCreatedBy = Long.MAX_VALUE - 3;

        // When
        CreateFileDTO dto = CreateFileDTO.builder()
                .id(largeId)
                .taskId(largeTaskId)
                .projectId(largeProjectId)
                .fileUrl("https://example.com/files/test-file.pdf")
                .createdBy(largeCreatedBy)
                .build();

        // Then
        assertEquals(largeId, dto.getId());
        assertEquals(largeTaskId, dto.getTaskId());
        assertEquals(largeProjectId, dto.getProjectId());
        assertEquals(largeCreatedBy, dto.getCreatedBy());
    }
}
