package com.xidian.activities.util;

import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * MinIO工具类
 *
 * @author
 * @since
 */
@Slf4j
@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name:activities}")
    private String defaultBucketName;

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
                            .build()
            );

            log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);

        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取预签名文件URL（安全方式）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 预签名URL
     */
    public String getFileUrl(String bucketName, String objectName) {
        // 私有桶必须使用预签名URL
        return getPresignedObjectUrl(bucketName, objectName, 24); // 默认24小时过期
    }

    /**
     * 获取临时访问URL（带自定义过期时间）
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expireHours 过期时间（小时）
     * @return 预签名URL
     */
    public String getTemporaryUrl(String bucketName, String objectName, int expireHours) {
        return getPresignedObjectUrl(bucketName, objectName, expireHours);
    }

    /**
     * 获取预签名URL
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expireHours 过期时间（小时）
     * @return 预签名URL
     */
    public String getPresignedObjectUrl(String bucketName, String objectName, int expireHours) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(objectName)
                            .expiry(expireHours, TimeUnit.HOURS)
                            .build()
            );
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取预签名URL失败: " + e.getMessage());
        }
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
                            .build()
            );
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
                            .build()
            );
        } catch (Exception e) {
            log.error("检查存储桶存在失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 创建存储桶（私有，需要预签名URL访问）
     *
     * @param bucketName 存储桶名称
     */
    public void createBucket(String bucketName) {
        try {
            // 创建私有桶
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .region("us-east-1")
                            .build()
            );

            // 设置桶策略为私有访问
            String bucketPolicy = getPrivateBucketPolicy(bucketName);
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(bucketPolicy)
                            .build()
            );

            log.info("私有存储桶创建成功: {}", bucketName);
        } catch (Exception e) {
            log.error("创建私有存储桶失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建私有存储桶失败: " + e.getMessage());
        }
    }

    /**
     * 下载文件
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件流
     */
    public InputStream download(String bucketName, String objectName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            log.error("下载文件失败: {}", e.getMessage(), e);
            throw new RuntimeException("下载文件失败: " + e.getMessage());
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    public boolean objectExists(String bucketName, String objectName) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 生成私有桶访问策略JSON
     *
     * @param bucketName 桶名称
     * @return 策略JSON字符串
     */
    private String getPrivateBucketPolicy(String bucketName) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Deny\",\n" +
                "      \"Principal\": {\"AWS\": \"*\"},\n" +
                "      \"Action\": \"s3:GetObject\",\n" +
                "      \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\"AWS\": \"*\"},\n" +
                "      \"Action\": [\"s3:GetObject\", \"s3:PutObject\", \"s3:DeleteObject\"],\n" +
                "      \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\",\n" +
                "      \"Condition\": {\n" +
                "        \"StringEquals\": {\n" +
                "          \"s3:amz-expires-in\": \"86400\"\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    /**
     * 生成只读桶访问策略JSON（需要预签名URL）
     *
     * @param bucketName 桶名称
     * @return 策略JSON字符串
     */
    private String getReadOnlyBucketPolicy(String bucketName) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Deny\",\n" +
                "      \"Principal\": {\"AWS\": \"*\"},\n" +
                "      \"Action\": [\"s3:PutObject\", \"s3:DeleteObject\"],\n" +
                "      \"Resource\": \"arn:aws:s3:::" + bucketName + "/*\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    /**
     * 设置桶策略为只读模式
     *
     * @param bucketName 桶名称
     */
    public void setBucketReadOnly(String bucketName) {
        try {
            String bucketPolicy = getReadOnlyBucketPolicy(bucketName);
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(bucketPolicy)
                            .build()
            );
            log.info("桶设置为只读模式: {}", bucketName);
        } catch (Exception e) {
            log.error("设置桶只读策略失败: {}", e.getMessage(), e);
            throw new RuntimeException("设置桶只读策略失败: " + e.getMessage());
        }
    }

    /**
     * 设置桶策略为私有模式（需要预签名URL访问）
     *
     * @param bucketName 桶名称
     */
    public void setBucketPrivate(String bucketName) {
        try {
            String bucketPolicy = getPrivateBucketPolicy(bucketName);
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(bucketPolicy)
                            .build()
            );
            log.info("桶设置为私有模式: {}", bucketName);
        } catch (Exception e) {
            log.error("设置桶私有策略失败: {}", e.getMessage(), e);
            throw new RuntimeException("设置桶私有策略失败: " + e.getMessage());
        }
    }
}