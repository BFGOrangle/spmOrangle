package com.spmorangle.crm.fileupload.service.impl;

import com.spmorangle.crm.fileupload.dto.CreateFileDTO;
import com.spmorangle.crm.fileupload.model.File;
import com.spmorangle.crm.fileupload.repository.FileRepository;
import com.spmorangle.crm.fileupload.service.FileService;
import com.spmorangle.crm.fileupload.service.exception.TaskNotFoundException;
import com.spmorangle.crm.taskmanagement.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileRepository fileRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public CreateFileDTO createFile(Long taskId, Long projectId, String fileUrl, Long createdBy) {
        log.info("Creating file record: taskId={}, projectId={}, fileUrl={}, createdBy={}",
                 taskId, projectId, fileUrl, createdBy);

        // Validate that the task exists before creating the file record
        if (!taskRepository.existsById(taskId)) {
            log.error("Task with ID {} not found", taskId);
            throw new TaskNotFoundException(taskId);
        }

        File file = new File();
        file.setTaskId(taskId);
        file.setProjectId(projectId);
        file.setFileUrl(fileUrl);
        file.setCreatedBy(createdBy);
        file.setUpdatedBy(createdBy);

        OffsetDateTime now = OffsetDateTime.now();
        file.setCreatedAt(now);
        file.setUpdatedAt(now);

        File savedFile = fileRepository.save(file);
        log.info("File record created successfully with ID: {}", savedFile.getId());

        // Convert to DTO and return
        return CreateFileDTO.builder()
                .id(savedFile.getId())
                .taskId(savedFile.getTaskId())
                .projectId(savedFile.getProjectId())
                .fileUrl(savedFile.getFileUrl())
                .createdBy(savedFile.getCreatedBy())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<File> getFiles(Long taskId, Long projectId) {
        log.info("Fetching files for taskId={}, projectId={}", taskId, projectId);
        List<File> files = fileRepository.findByTaskIdAndProjectId(taskId, projectId);
        log.info("Found {} files for taskId={}, projectId={}", files.size(), taskId, projectId);
        return files;
    }

    @Override
    @Transactional(readOnly = true)
    public List<File> getFilesByProjectId(Long projectId) {
        log.info("Fetching files for projectId={}", projectId);
        List<File> files = fileRepository.findByProjectId(projectId);
        log.info("Found {} files for projectId={}", files.size(), projectId);
        return files;
    }

    @Override
    @Transactional
    public File updateFile(Long fileId, String fileUrl, Long updatedBy) {
        log.info("Updating file record: fileId={}, fileUrl={}, updatedBy={}",
                 fileId, fileUrl, updatedBy);

        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));

        if (fileUrl != null && !fileUrl.isEmpty()) {
            file.setFileUrl(fileUrl);
        }
        
        file.setUpdatedBy(updatedBy);
        file.setUpdatedAt(OffsetDateTime.now());

        File updatedFile = fileRepository.save(file);
        log.info("File record updated successfully with ID: {}", updatedFile.getId());
        
        return updatedFile;
    }

    @Override
    @Transactional
    public boolean deleteFile(Long fileId, Long deletedBy) {
        log.info("Deleting file record: fileId={}, deletedBy={}", fileId, deletedBy);

        if (!fileRepository.existsById(fileId)) {
            log.error("File with ID {} not found", fileId);
            return false;
        }

        fileRepository.deleteById(fileId);
        log.info("File record deleted successfully with ID: {}", fileId);
        
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<File> getFileById(Long fileId) {
        log.info("Fetching file by ID: {}", fileId);
        return fileRepository.findById(fileId);
    }
}
