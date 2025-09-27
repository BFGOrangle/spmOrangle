package com.spmorangle.crm.fileupload.service;

import com.spmorangle.crm.fileupload.dto.CreateFileDTO;
import com.spmorangle.crm.fileupload.model.File;
import com.spmorangle.crm.fileupload.repository.FileRepository;
import com.spmorangle.crm.fileupload.service.exception.TaskNotFoundException;
import com.spmorangle.crm.fileupload.service.impl.FileServiceImpl;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FileService Test Cases")
class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    private File testFile;
    private OffsetDateTime testTime;

    @BeforeEach
    void setUp() {
        testTime = OffsetDateTime.now();

        testFile = new File();
        testFile.setId(1L);
        testFile.setTaskId(123L);
        testFile.setProjectId(456L);
        testFile.setFileUrl("https://example.com/files/test-file.pdf");
        testFile.setCreatedBy(789L);
        testFile.setUpdatedBy(789L);
        testFile.setCreatedAt(testTime);
        testFile.setUpdatedAt(testTime);
    }

    @Test
    @DisplayName("Should create file successfully when task exists")
    void testCreateFileSuccess() {
        // Given
        Long taskId = 123L;
        Long projectId = 456L;
        String fileUrl = "https://example.com/files/test-file.pdf";
        Long createdBy = 789L;

        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(fileRepository.save(any(File.class))).thenReturn(testFile);

        // When
        CreateFileDTO result = fileService.createFile(taskId, projectId, fileUrl, createdBy);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTaskId()).isEqualTo(taskId);
        assertThat(result.getProjectId()).isEqualTo(projectId);
        assertThat(result.getFileUrl()).isEqualTo(fileUrl);
        assertThat(result.getCreatedBy()).isEqualTo(createdBy);

        verify(taskRepository).existsById(taskId);
        verify(fileRepository).save(any(File.class));
    }

    @Test
    @DisplayName("Should throw TaskNotFoundException when task does not exist")
    void testCreateFileTaskNotFound() {
        // Given
        Long taskId = 999L;
        Long projectId = 456L;
        String fileUrl = "https://example.com/files/test-file.pdf";
        Long createdBy = 789L;

        when(taskRepository.existsById(taskId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> fileService.createFile(taskId, projectId, fileUrl, createdBy))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("Task with ID 999 not found");

        verify(taskRepository).existsById(taskId);
        verify(fileRepository, never()).save(any(File.class));
    }

    @Test
    @DisplayName("Should set timestamps correctly when creating file")
    void testCreateFileTimestamps() {
        // Given
        Long taskId = 123L;
        Long projectId = 456L;
        String fileUrl = "https://example.com/files/test-file.pdf";
        Long createdBy = 789L;

        when(taskRepository.existsById(taskId)).thenReturn(true);
        when(fileRepository.save(any(File.class))).thenAnswer(invocation -> {
            File fileArg = invocation.getArgument(0);
            fileArg.setId(1L);
            return fileArg;
        });

        // When
        fileService.createFile(taskId, projectId, fileUrl, createdBy);

        // Then
        verify(fileRepository).save(argThat(file -> {
            assertThat(file.getCreatedAt()).isNotNull();
            assertThat(file.getUpdatedAt()).isNotNull();
            assertThat(file.getCreatedBy()).isEqualTo(createdBy);
            assertThat(file.getUpdatedBy()).isEqualTo(createdBy);
            return true;
        }));
    }

    @Test
    @DisplayName("Should get files by task ID and project ID")
    void testGetFiles() {
        // Given
        Long taskId = 123L;
        Long projectId = 456L;
        List<File> expectedFiles = Arrays.asList(testFile);

        when(fileRepository.findByTaskIdAndProjectId(taskId, projectId)).thenReturn(expectedFiles);

        // When
        List<File> result = fileService.getFiles(taskId, projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testFile);

        verify(fileRepository).findByTaskIdAndProjectId(taskId, projectId);
    }

    @Test
    @DisplayName("Should return empty list when no files found for task and project")
    void testGetFilesEmpty() {
        // Given
        Long taskId = 999L;
        Long projectId = 888L;

        when(fileRepository.findByTaskIdAndProjectId(taskId, projectId)).thenReturn(Collections.emptyList());

        // When
        List<File> result = fileService.getFiles(taskId, projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(fileRepository).findByTaskIdAndProjectId(taskId, projectId);
    }

    @Test
    @DisplayName("Should get files by project ID")
    void testGetFilesByProjectId() {
        // Given
        Long projectId = 456L;

        File file2 = new File();
        file2.setId(2L);
        file2.setTaskId(124L);
        file2.setProjectId(projectId);
        file2.setFileUrl("https://example.com/files/test-file2.docx");

        List<File> expectedFiles = Arrays.asList(testFile, file2);

        when(fileRepository.findByProjectId(projectId)).thenReturn(expectedFiles);

        // When
        List<File> result = fileService.getFilesByProjectId(projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(testFile, file2);

        verify(fileRepository).findByProjectId(projectId);
    }

    @Test
    @DisplayName("Should return empty list when no files found for project")
    void testGetFilesByProjectIdEmpty() {
        // Given
        Long projectId = 999L;

        when(fileRepository.findByProjectId(projectId)).thenReturn(Collections.emptyList());

        // When
        List<File> result = fileService.getFilesByProjectId(projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(fileRepository).findByProjectId(projectId);
    }

    @Test
    @DisplayName("Should handle multiple files for same task and project")
    void testGetFilesMultiple() {
        // Given
        Long taskId = 123L;
        Long projectId = 456L;

        File file2 = new File();
        file2.setId(2L);
        file2.setTaskId(taskId);
        file2.setProjectId(projectId);
        file2.setFileUrl("https://example.com/files/test-file2.xlsx");

        List<File> expectedFiles = Arrays.asList(testFile, file2);

        when(fileRepository.findByTaskIdAndProjectId(taskId, projectId)).thenReturn(expectedFiles);

        // When
        List<File> result = fileService.getFiles(taskId, projectId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).extracting(File::getFileUrl)
                .containsExactlyInAnyOrder(
                        "https://example.com/files/test-file.pdf",
                        "https://example.com/files/test-file2.xlsx"
                );

        verify(fileRepository).findByTaskIdAndProjectId(taskId, projectId);
    }

    @Test
    @DisplayName("Should handle null parameters gracefully")
    void testCreateFileWithNullParameters() {
        // Given
        when(taskRepository.existsById(any())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> fileService.createFile(null, 456L, "url", 789L))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should validate task existence before saving")
    void testTaskValidationOrder() {
        // Given
        Long nonExistentTaskId = 999L;
        when(taskRepository.existsById(nonExistentTaskId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() ->
            fileService.createFile(nonExistentTaskId, 456L, "https://example.com/file.pdf", 789L))
                .isInstanceOf(TaskNotFoundException.class);

        // Verify task existence is checked first
        verify(taskRepository).existsById(nonExistentTaskId);
        verify(fileRepository, never()).save(any());
    }
}
