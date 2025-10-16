package com.spmorangle.crm.fileupload.service;

import com.spmorangle.crm.fileupload.dto.CreateFileDTO;
import com.spmorangle.crm.fileupload.model.File;
import java.util.List;
import java.util.Optional;

public interface FileService {
    
    /**
     * Create a new file record in the database
     * @param taskId the task ID
     * @param projectId the project ID  
     * @param fileUrl the S3 file URL
     * @param createdBy the user who uploaded the file
     * @return the created File entity
     */
    CreateFileDTO createFile(Long taskId, Long projectId, String fileUrl, Long createdBy);

    /**
     * Get all files for a specific task and project
     * @param taskId the task ID
     * @param projectId the project ID
     * @return list of files associated with the task and project
     */
    List<File> getFiles(Long taskId, Long projectId);

    /**
     * Get a file by project ID
     * @param projectId the file ID
     * @return list of files associated with the project
     */
    List<File> getFilesByProjectId(Long projectId);

    /**
     * Update a file record in the database
     * @param fileId the file ID to update
     * @param fileUrl the new file URL (optional)
     * @param updatedBy the user who updated the file
     * @return the updated File entity
     */
    File updateFile(Long fileId, String fileUrl, Long updatedBy);

    /**
     * Delete a file record from the database
     * @param fileId the file ID to delete
     * @param deletedBy the user who deleted the file
     * @return true if deletion was successful
     */
    boolean deleteFile(Long fileId, Long deletedBy);

    /**
     * Get a file by ID
     * @param fileId the file ID
     * @return the file entity if found
     */
    Optional<File> getFileById(Long fileId);

}
