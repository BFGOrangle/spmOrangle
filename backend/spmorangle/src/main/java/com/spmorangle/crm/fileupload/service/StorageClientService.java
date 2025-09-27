package com.spmorangle.crm.fileupload.service;

import java.io.InputStream;

public interface StorageClientService {
    /**
     * Uploads and returns the path (e.g., "tasks/123/uuid-filename.png")
     */
    String upload(String bucket, String path, InputStream data, long contentLength);

    void delete(String bucket, String path);
}
