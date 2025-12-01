package com.xidian.activities.configuration.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO配置属性
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO服务地址（后端内部访问）
     */
    private String endpoint;

    /**
     * MinIO外部访问地址（用于前端/移动端访问，如局域网IP）
     * 如果不配置，则使用endpoint
     */
    private String externalEndpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    private String bucketName;
}
