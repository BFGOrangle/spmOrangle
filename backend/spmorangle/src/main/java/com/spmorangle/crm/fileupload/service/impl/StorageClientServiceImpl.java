package com.spmorangle.crm.fileupload.service.impl;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import com.spmorangle.crm.fileupload.service.StorageClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

@Service
public class StorageClientServiceImpl implements StorageClientService {

    @Value("${supabase.storage.url}")
    private String endpointUrl;

    @Value("${supabase.storage.region}")
    private String region;

    @Value("${supabase.storage.s3_aws_access_key_id}")
    private String accessKeyId;

    @Value("${supabase.storage.s3_aws_secret_access_key}")
    private String secretAccessKey;

    @Value("${supabase.storage.bucket}")
    private String defaultBucket;

    private S3Client s3Client;

    private static final Set<String> ALLOWED_FORMATS = Set.of(
            "text/csv",
            "image/png",
            "image/jpeg",
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );

    @PostConstruct
    public void init() {
        // Create AWS credentials
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        // Build S3 client with custom endpoint for Supabase (matching JS SDK pattern)
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .endpointOverride(URI.create(endpointUrl))
                .forcePathStyle(true)
                .build();
    }

    @Override
    public String upload(String bucket, String path, InputStream data, long contentLength) {
        // Validate file format

        try {
            String targetBucket = bucket != null ? bucket : defaultBucket;

            // Create put request
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(path)
                    .contentLength(contentLength)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            // Upload file
            s3Client.putObject(putRequest, RequestBody.fromInputStream(data, contentLength));

            // Return the correct Supabase public URL format
            // Format: https://{project-ref}.supabase.co/storage/v1/object/public/{bucket}/{path}
            String baseUrl = endpointUrl.replace("/storage/v1/s3", ""); // Remove S3 path
            return String.format("%s/storage/v1/object/public/%s/%s",
                    baseUrl,
                    targetBucket,
                    path);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String bucket, String path) {
        try {
            String targetBucket = bucket != null ? bucket : defaultBucket;

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(targetBucket)
                    .key(path)
                    .build();

            s3Client.deleteObject(deleteRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }
}