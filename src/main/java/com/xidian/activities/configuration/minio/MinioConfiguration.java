package com.xidian.activities.configuration.minio;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 *
 * @author
 * @since
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
@ConditionalOnProperty(name = "minio.endpoint")
public class MinioConfiguration {

    @Autowired
    private MinioProperties properties;

    /**
     * 创建MinIO客户端Bean
     *
     * @return MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
