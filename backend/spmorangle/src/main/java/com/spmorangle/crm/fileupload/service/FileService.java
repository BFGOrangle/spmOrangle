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

}
