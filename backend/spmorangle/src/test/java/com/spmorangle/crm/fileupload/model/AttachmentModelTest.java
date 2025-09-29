package com.spmorangle.crm.fileupload.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;

@ActiveProfiles("test")
@DisplayName("Attachment Model Test Cases")
class AttachmentModelTest {

    private File file;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        file = new File();
        testTime = OffsetDateTime.now();
    }

    @Test
    @DisplayName("Should store and retrieve attachment metadata")
    void testAttachmentModel() {
        // Given
        Long taskId = 123L;
        Long projectId = 456L;
        String fileUrl = "https://example.com/files/test-file.pdf";
        Long createdBy = 789L;
        Long updatedBy = 789L;

        // When
        file.setTaskId(taskId);
        file.setProjectId(projectId);
        file.setFileUrl(fileUrl);
        file.setCreatedBy(createdBy);
        file.setUpdatedBy(updatedBy);
        file.setCreatedAt(testTime);
        file.setUpdatedAt(testTime);

        // Then
        assertEquals(taskId, file.getTaskId());
        assertEquals(projectId, file.getProjectId());
        assertEquals(fileUrl, file.getFileUrl());
        assertEquals(createdBy, file.getCreatedBy());
        assertEquals(updatedBy, file.getUpdatedBy());
        assertEquals(testTime, file.getCreatedAt());
        assertEquals(testTime, file.getUpdatedAt());
    }

    @Test
    @DisplayName("Should handle file URL properly")
    void testFileUrlHandling() {
        // Given
        String fileUrl = "https://storage.supabase.co/bucket/project/1/tasks/1/uuid-document.pdf";

        // When
        file.setFileUrl(fileUrl);

        // Then
        assertEquals(fileUrl, file.getFileUrl());
        assertNotNull(file.getFileUrl());
        assertTrue(file.getFileUrl().contains("project"));
        assertTrue(file.getFileUrl().contains("tasks"));
    }

    @Test
    @DisplayName("Should handle timestamp fields correctly")
    void testTimestampFields() {
        // Given
        OffsetDateTime createdTime = OffsetDateTime.now().minusHours(1);
        OffsetDateTime updatedTime = OffsetDateTime.now();

        // When
        file.setCreatedAt(createdTime);
        file.setUpdatedAt(updatedTime);

        // Then
        assertEquals(createdTime, file.getCreatedAt());
        assertEquals(updatedTime, file.getUpdatedAt());
        assertTrue(file.getUpdatedAt().isAfter(file.getCreatedAt()));
    }

    @Test
    @DisplayName("Should handle user ID fields")
    void testUserIdFields() {
        // Given
        Long userId = 100L;
        Long differentUserId = 200L;

        // When
        file.setCreatedBy(userId);
        file.setUpdatedBy(differentUserId);

        // Then
        assertEquals(userId, file.getCreatedBy());
        assertEquals(differentUserId, file.getUpdatedBy());
    }

    @Test
    @DisplayName("Should handle ID field properly")
    void testIdField() {
        // Given
        Long id = 1L;

        // When
        file.setId(id);

        // Then
        assertEquals(id, file.getId());
    }

    @Test
    @DisplayName("Should create new instance with default values")
    void testDefaultConstructor() {
        // When
        File newFile = new File();

        // Then
        assertNotNull(newFile);
        assertEquals(0L, newFile.getId()); // primitive long defaults to 0
    }
}
