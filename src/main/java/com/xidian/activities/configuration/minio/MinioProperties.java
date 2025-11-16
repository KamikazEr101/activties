package com.xidian.activities.configuration.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * MinIO配置属性
 *
 * @author
 * @since
 */
@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO服务地址
     */
    private String endpoint;

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
