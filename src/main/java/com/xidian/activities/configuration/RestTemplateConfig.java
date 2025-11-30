package com.xidian.activities.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate配置
 * 用于HTTP客户端请求（如调用AI API）
 *
 * @author
 * @since
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(30)) // 连接超时30秒
                .readTimeout(Duration.ofSeconds(120)) // 读取超时120秒（AI生成需要时间）
                .build();
    }
}
