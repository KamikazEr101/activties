package com.xidian.activities.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis配置类
 *
 * @author KamikazEr101
 * @since 2025/11/20
 */
@Configuration
@EnableCaching
public class RedisConfiguration {

        /**
         * Redis模板配置
         */
        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
                RedisTemplate<String, Object> template = new RedisTemplate<>();
                template.setConnectionFactory(connectionFactory);

                // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                // 解决jackson2无法反序列化LocalDateTime的问题
                objectMapper.registerModule(new JavaTimeModule());
                // 启用默认类型，以便在反序列化时知道具体的类
                objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL);

                Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                                objectMapper, Object.class);

                // 使用StringRedisSerializer来序列化和反序列化redis的key值
                StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

                // key采用String的序列化方式
                template.setKeySerializer(stringRedisSerializer);
                template.setHashKeySerializer(stringRedisSerializer);

                // value序列化方式采用jackson
                template.setValueSerializer(jackson2JsonRedisSerializer);
                template.setHashValueSerializer(jackson2JsonRedisSerializer);

                template.afterPropertiesSet();
                return template;
        }

        /**
         * 缓存管理器配置
         */
        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
                // 配置序列化
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL);

                Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
                                objectMapper, Object.class);

                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .entryTtl(Duration.ofMinutes(30)) // 默认30分钟过期
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(jackson2JsonRedisSerializer))
                                .disableCachingNullValues(); // 不缓存空值

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(config)
                                .transactionAware()
                                .build();
        }
}