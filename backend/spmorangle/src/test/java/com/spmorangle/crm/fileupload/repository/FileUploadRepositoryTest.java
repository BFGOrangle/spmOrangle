package com.spmorangle.crm.fileupload.repository;

import com.spmorangle.crm.fileupload.model.File;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("FileUploadRepository Test Cases")
class FileUploadRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private FileRepository fileRepository;

    private File testFile1;
    private File testFile2;
    private File testFile3;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();

        // Create test files with different project and task combinations
        testFile1 = new File();
        testFile1.setTaskId(1L);
        testFile1.setProjectId(100L);
        testFile1.setFileUrl("https://example.com/files/file1.pdf");
        testFile1.setCreatedBy(1L);
        testFile1.setUpdatedBy(1L);
        testFile1.setCreatedAt(now);
        testFile1.setUpdatedAt(now);

        testFile2 = new File();
        testFile2.setTaskId(2L);
        testFile2.setProjectId(100L);
        testFile2.setFileUrl("https://example.com/files/file2.docx");
        testFile2.setCreatedBy(1L);
        testFile2.setUpdatedBy(1L);
        testFile2.setCreatedAt(now);
        testFile2.setUpdatedAt(now);

        testFile3 = new File();
        testFile3.setTaskId(1L);
        testFile3.setProjectId(200L);
        testFile3.setFileUrl("https://example.com/files/file3.xlsx");
        testFile3.setCreatedBy(2L);
        testFile3.setUpdatedBy(2L);
        testFile3.setCreatedAt(now);
        testFile3.setUpdatedAt(now);
    }

    @Test
    @DisplayName("Should save and retrieve file entity")
    void testSaveAndRetrieveFile() {
        // When
        File savedFile = fileRepository.save(testFile1);
        entityManager.flush();
        entityManager.clear();

        // Then
        assertThat(savedFile.getId()).isNotNull();
        assertThat(savedFile.getId()).isGreaterThan(0);

        Optional<File> retrievedFile = fileRepository.findById(savedFile.getId());
        assertThat(retrievedFile).isPresent();
        assertThat(retrievedFile.get().getTaskId()).isEqualTo(1L);
        assertThat(retrievedFile.get().getProjectId()).isEqualTo(100L);
        assertThat(retrievedFile.get().getFileUrl()).isEqualTo("https://example.com/files/file1.pdf");
    }

    @Test
    @DisplayName("Should find files by project ID")
    void testFindByProjectId() {
        // Given
        fileRepository.save(testFile1);
        fileRepository.save(testFile2);
        fileRepository.save(testFile3);
        entityManager.flush();
        entityManager.clear();

        // When
        List<File> filesForProject100 = fileRepository.findByProjectId(100L);
        List<File> filesForProject200 = fileRepository.findByProjectId(200L);
        List<File> filesForNonExistentProject = fileRepository.findByProjectId(999L);

        // Then
        assertThat(filesForProject100).hasSize(2);
        assertThat(filesForProject100)
                .extracting(File::getTaskId)
                .containsExactlyInAnyOrder(1L, 2L);

        assertThat(filesForProject200).hasSize(1);
        assertThat(filesForProject200.get(0).getTaskId()).isEqualTo(1L);

        assertThat(filesForNonExistentProject).isEmpty();
    }

    @Test
    @DisplayName("Should find files by task ID and project ID")
    void testFindByTaskIdAndProjectId() {
        // Given
        fileRepository.save(testFile1);
        fileRepository.save(testFile2);
        fileRepository.save(testFile3);
        entityManager.flush();
        entityManager.clear();

        // When
        List<File> filesForTask1Project100 = fileRepository.findByTaskIdAndProjectId(1L, 100L);
        List<File> filesForTask2Project100 = fileRepository.findByTaskIdAndProjectId(2L, 100L);
        List<File> filesForTask1Project200 = fileRepository.findByTaskIdAndProjectId(1L, 200L);
        List<File> filesForNonExistentTask = fileRepository.findByTaskIdAndProjectId(999L, 100L);

        // Then
        assertThat(filesForTask1Project100).hasSize(1);
        assertThat(filesForTask1Project100.get(0).getFileUrl()).isEqualTo("https://example.com/files/file1.pdf");

        assertThat(filesForTask2Project100).hasSize(1);
        assertThat(filesForTask2Project100.get(0).getFileUrl()).isEqualTo("https://example.com/files/file2.docx");

        assertThat(filesForTask1Project200).hasSize(1);
        assertThat(filesForTask1Project200.get(0).getFileUrl()).isEqualTo("https://example.com/files/file3.xlsx");

        assertThat(filesForNonExistentTask).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple files for same task and project")
    void testMultipleFilesForSameTaskAndProject() {
        // Given
        File additionalFile = new File();
        additionalFile.setTaskId(1L);
        additionalFile.setProjectId(100L);
        additionalFile.setFileUrl("https://example.com/files/additional-file.png");
        additionalFile.setCreatedBy(1L);
        additionalFile.setUpdatedBy(1L);
        additionalFile.setCreatedAt(OffsetDateTime.now());
        additionalFile.setUpdatedAt(OffsetDateTime.now());

        fileRepository.save(testFile1);
        fileRepository.save(additionalFile);
        entityManager.flush();
        entityManager.clear();

        // When
        List<File> files = fileRepository.findByTaskIdAndProjectId(1L, 100L);

        // Then
        assertThat(files).hasSize(2);
        assertThat(files)
                .extracting(File::getFileUrl)
                .containsExactlyInAnyOrder(
                        "https://example.com/files/file1.pdf",
                        "https://example.com/files/additional-file.png"
                );
    }

    @Test
    @DisplayName("Should delete file entity")
    void testDeleteFile() {
        // Given
        File savedFile = fileRepository.save(testFile1);
        Long fileId = savedFile.getId();
        entityManager.flush();
        entityManager.clear();

        // When
        fileRepository.deleteById(fileId);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<File> deletedFile = fileRepository.findById(fileId);
        assertThat(deletedFile).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void testEmptyResults() {
        // When - querying empty database
        List<File> allFiles = fileRepository.findAll();
        List<File> filesByProject = fileRepository.findByProjectId(123L);
        List<File> filesByTaskAndProject = fileRepository.findByTaskIdAndProjectId(123L, 456L);

        // Then
        assertThat(allFiles).isEmpty();
        assertThat(filesByProject).isEmpty();
        assertThat(filesByTaskAndProject).isEmpty();
    }

    @Test
    @DisplayName("Should persist timestamps correctly")
    void testTimestampPersistence() {
        // Given
        OffsetDateTime specificTime = OffsetDateTime.now().minusHours(2);
        testFile1.setCreatedAt(specificTime);
        testFile1.setUpdatedAt(specificTime);

        // When
        File savedFile = fileRepository.save(testFile1);
        entityManager.flush();
        entityManager.clear();

        // Then
        Optional<File> retrievedFile = fileRepository.findById(savedFile.getId());
        assertThat(retrievedFile).isPresent();
        assertThat(retrievedFile.get().getCreatedAt()).isEqualToIgnoringNanos(specificTime);
        assertThat(retrievedFile.get().getUpdatedAt()).isEqualToIgnoringNanos(specificTime);
    }

    @Test
    @DisplayName("Should handle large datasets efficiently")
    void testLargeDatasetHandling() {
        // Given - create multiple files for the same project
        for (int i = 1; i <= 50; i++) {
            File file = new File();
            file.setTaskId((long) i);
            file.setProjectId(100L);
            file.setFileUrl("https://example.com/files/file" + i + ".pdf");
            file.setCreatedBy(1L);
            file.setUpdatedBy(1L);
            file.setCreatedAt(OffsetDateTime.now());
            file.setUpdatedAt(OffsetDateTime.now());
            fileRepository.save(file);
        }
        entityManager.flush();
        entityManager.clear();

        // When
        List<File> allFilesForProject = fileRepository.findByProjectId(100L);

        // Then
        assertThat(allFilesForProject).hasSize(50);
        assertThat(allFilesForProject)
                .extracting(File::getProjectId)
                .containsOnly(100L);
    }
}
