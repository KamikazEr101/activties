package com.xidian.activities.util;

import io.minio.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * MinIO工具类（公开访问模式）
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Slf4j
@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.external-endpoint:}")
    private String externalEndpoint;

    /**
     * 上传文件
     *
     * @param bucketName  存储桶名称
     * @param objectName  对象名称
     * @param inputStream 文件流
     * @param contentType 文件类型
     */
    public void upload(String bucketName, String objectName, InputStream inputStream, String contentType) {
        try {
            // 确保存储桶存在
            if (!bucketExists(bucketName)) {
                createBucket(bucketName);
            }

            // 上传文件
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, -1, 10485760)
                            .contentType(contentType)
                            .build());

            log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件URL（公开访问，永久有效）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 公开URL
     */
    public String getFileUrl(String bucketName, String objectName) {
        String baseUrl;

        // 如果配置了外部访问地址，优先使用
        if (externalEndpoint != null && !externalEndpoint.isEmpty() && !externalEndpoint.equals(endpoint)) {
            baseUrl = externalEndpoint;
            log.debug("使用配置的外部访问地址: {}", baseUrl);
        } else {
            // 未配置外部地址，自动检测本机IP
            String localIp = NetworkUtil.getLocalIpAddress();
            if (localIp != null && !localIp.isEmpty()) {
                // 从endpoint中提取端口号
                String port = NetworkUtil.extractPort(endpoint);
                baseUrl = NetworkUtil.buildExternalUrl(localIp, port);
                log.debug("自动检测到本机IP，使用外部访问地址: {}", baseUrl);
            } else {
                // 无法获取本机IP，使用配置的endpoint
                baseUrl = endpoint;
                log.debug("无法检测本机IP，使用内部访问地址: {}", baseUrl);
            }
        }

        return baseUrl + "/" + bucketName + "/" + objectName;
    }

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    public void removeObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
            log.info("文件删除成功: bucket={}, object={}", bucketName, objectName);
        } catch (Exception e) {
            log.error("文件删除失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }

    /**
     * 检查存储桶是否存在
     *
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    public boolean bucketExists(String bucketName) {
        try {
            return minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build());
        } catch (Exception e) {
            log.error("检查存储桶存在失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建存储桶（公开只读访问）
     *
     * @param bucketName 存储桶名称
     */
    public void createBucket(String bucketName) {
        try {
            // 创建桶
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());

            // 设置桶策略为公开只读访问
            String bucketPolicy = getPublicReadBucketPolicy(bucketName);
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(bucketPolicy)
                            .build());

            log.info("公开只读存储桶创建成功: {}", bucketName);
        } catch (Exception e) {
            log.error("创建存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 生成公开只读桶访问策略JSON
     *
     * @param bucketName 桶名称
     * @return 策略JSON字符串
     */
    private String getPublicReadBucketPolicy(String bucketName) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\"AWS\": \"*\"},\n" +
                "      \"Action\": \"s3:GetObject\",\n" +
                "      \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}