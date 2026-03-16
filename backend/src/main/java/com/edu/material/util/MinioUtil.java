package com.edu.material.util;

import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Component
public class MinioUtil {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @Value("${minio.bucketName}")
    private String bucketName;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            createBucket();
            System.out.println("MinIO初始化成功");
        } catch (Exception e) {
            System.err.println("MinIO初始化失败: " + e.getMessage());
            System.err.println("请确保MinIO服务正在运行，或检查配置是否正确");
            System.err.println("MinIO配置: endpoint=" + endpoint + ", bucketName=" + bucketName);
            // 不抛出异常，让应用继续启动
        }
    }

    public void createBucket() {
        try {
            boolean exists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!exists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            System.err.println("创建MinIO桶失败: " + e.getMessage());
            throw new RuntimeException("创建MinIO桶失败", e);
        }
    }

    public String uploadFile(String objectName, MultipartFile file) {
        try {
            InputStream inputStream = file.getInputStream();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(sanitizeObjectName(objectName))
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return getFileUrl(objectName);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    public InputStream downloadFile(String objectName) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
    }

    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("文件删除失败", e);
        }
    }

    public String getFileUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET)
                    .bucket(bucketName)
                    .object(objectName)
                    .expiry(7, TimeUnit.DAYS)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException("获取文件URL失败", e);
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    private String sanitizeObjectName(String objectName) {
        if (objectName == null || objectName.isEmpty()) {
            return "file";
        }
        
        String sanitized = objectName.replaceAll("[\\\\$%&\\^\\{\\}\\|~`\"';<>=?*]", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("-+", "-");
        sanitized = sanitized.trim().replaceAll("^[._-]+|[._-]+$", "");
        
        if (sanitized.isEmpty()) {
            sanitized = "file";
        }
        
        return sanitized;
    }
}