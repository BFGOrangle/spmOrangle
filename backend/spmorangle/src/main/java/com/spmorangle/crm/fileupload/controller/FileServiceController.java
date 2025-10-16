package com.spmorangle.crm.fileupload.controller;

import com.spmorangle.common.model.User;
import com.spmorangle.common.service.UserContextService;
import com.spmorangle.crm.fileupload.dto.CreateFileDTO;
import com.spmorangle.crm.fileupload.dto.CreateFileResponseDTO;
import com.spmorangle.crm.fileupload.dto.UpdateFileDTO;
import com.spmorangle.crm.fileupload.dto.DeleteFileResponseDTO;
import com.spmorangle.crm.fileupload.service.FileService;
import com.spmorangle.crm.fileupload.service.StorageClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileServiceController {

    private final FileService fileService;

    private final StorageClientService storageClientService;

    private final UserContextService userContextService;

    /**
     * Upload a file to storage and create a database record
     * @param taskId the task ID to associate the file with
     * @param projectId the project ID to associate the file with
     * @param file the multipart file to upload
     * @param bucket optional bucket name, defaults to service default
     * @return CreateFileResponseDTO
     */
    @PostMapping("/upload")
    public ResponseEntity<CreateFileResponseDTO> uploadFile(
            @RequestParam("taskId") Long taskId,
            @RequestParam("projectId") Long projectId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bucket", required = false) String bucket) {

        User user = userContextService.getRequestingUser();
        log.info("Uploading file for user {}: taskId={}, projectId={}, filename={}",
                user.getId(), taskId, projectId, file.getOriginalFilename());

        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File cannot be empty");
            }

            // Generate unique file path
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
            String uniqueFilename = UUID.randomUUID() + fileExtension;
            String path = String.format("project/%d/tasks/%d/%s", projectId, taskId, uniqueFilename);

            // Upload file to storage
            String uploadedUrl = storageClientService.upload(
                bucket,
                path,
                file.getInputStream(),
                file.getSize()
            );

            log.info("File uploaded successfully to storage: {}", uploadedUrl);

            // Create file record in database - using hardcoded user ID for testing
            CreateFileDTO savedFile = fileService.createFile(
                taskId,
                projectId,
                uploadedUrl,
                user.getId()
            );

            // Create response
            CreateFileResponseDTO response = CreateFileResponseDTO.builder()
                .id(savedFile.getId())
                .taskId(taskId)
                .projectId(projectId)
                .fileUrl(uploadedUrl)
                .createdBy(user.getId())
                .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            log.error("Error reading file for task {}: {}", taskId, e.getMessage(), e);
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error uploading file for task {}: {}", taskId, e.getMessage(), e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    /**
     * Get files for a task in a project
     * @param projectId projectId of the task
     * @param taskId taskId to fetch files for
     * @return List<FileResponseDTO>
     */
    @GetMapping("/project/{projectId}/task/{taskId}")
    public ResponseEntity<List<com.spmorangle.crm.fileupload.model.File>> getFiles(
            @PathVariable("taskId") Long taskId,
            @PathVariable("projectId") Long projectId) {

        User user = userContextService.getRequestingUser();

        log.info("Fetching files for taskId={}, projectId={}", taskId, projectId);

        try {
            List<com.spmorangle.crm.fileupload.model.File> files = fileService.getFiles(taskId, projectId);
            log.info("Found {} files for taskId={}, projectId={}", files.size(), taskId, projectId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("Error fetching files for taskId={}, projectId={}: {}", taskId, projectId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch files: " + e.getMessage(), e);
        }
    }

    /**
     * Get files for a task in a project
     * @param projectId projectId of the task
     * @return List<FileResponseDTO>
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<com.spmorangle.crm.fileupload.model.File>> getFilesByProject(
            @PathVariable("projectId") Long projectId) {

        log.info("Fetching files for projectId={}", projectId);

        try {
            List<com.spmorangle.crm.fileupload.model.File> files = fileService.getFilesByProjectId(projectId);
            log.info("Found {} files for projectId={}", files.size(), projectId);
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            log.error("Error fetching files for projectId={}: {}", projectId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch files: " + e.getMessage(), e);
        }
    }

    /**
     * Update a file record
     * @param fileId the file ID to update
     * @param fileUrl optional new file URL
     * @return Updated File
     */
    @PutMapping("/{fileId}")
    public ResponseEntity<com.spmorangle.crm.fileupload.model.File> updateFile(
            @PathVariable("fileId") Long fileId,
            @RequestParam(value = "fileUrl", required = false) String fileUrl) {

        User user = userContextService.getRequestingUser();
        log.info("Updating file for user {}: fileId={}, fileUrl={}",
                user.getId(), fileId, fileUrl);

        try {
            com.spmorangle.crm.fileupload.model.File updatedFile = fileService.updateFile(
                fileId,
                fileUrl,
                user.getId()
            );

            log.info("File updated successfully: {}", updatedFile.getId());
            return ResponseEntity.ok(updatedFile);

        } catch (RuntimeException e) {
            log.error("Error updating file {}: {}", fileId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Delete a file record
     * @param fileId the file ID to delete
     * @return DeleteFileResponseDTO
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<DeleteFileResponseDTO> deleteFile(
            @PathVariable("fileId") Long fileId) {

        User user = userContextService.getRequestingUser();
        log.info("Deleting file for user {}: fileId={}", user.getId(), fileId);

        try {
            boolean deleted = fileService.deleteFile(fileId, user.getId());

            if (deleted) {
                DeleteFileResponseDTO response = DeleteFileResponseDTO.builder()
                    .id(fileId)
                    .message("File deleted successfully")
                    .success(true)
                    .build();
                
                log.info("File deleted successfully: {}", fileId);
                return ResponseEntity.ok(response);
            } else {
                DeleteFileResponseDTO response = DeleteFileResponseDTO.builder()
                    .id(fileId)
                    .message("File not found")
                    .success(false)
                    .build();
                
                log.error("File not found: {}", fileId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

        } catch (Exception e) {
            log.error("Error deleting file {}: {}", fileId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    /**
     * Get a file by ID
     * @param fileId the file ID
     * @return File
     */
    @GetMapping("/{fileId}")
    public ResponseEntity<com.spmorangle.crm.fileupload.model.File> getFileById(
            @PathVariable("fileId") Long fileId) {

        log.info("Fetching file by ID: {}", fileId);

        try {
            return fileService.getFileById(fileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching file {}: {}", fileId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch file: " + e.getMessage(), e);
        }
    }
}
