package com.example.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache configuration for application-level caching.
 *
 * <p>Supports multiple profiles:
 * <ul>
 *   <li>default/dev/prod: Redis-based distributed caching</li>
 *   <li>test: In-memory caching for tests</li>
 * </ul>
 *
 * <p>Redis provides:
 * <ul>
 *   <li>Distributed caching across multiple application instances</li>
 *   <li>Cache persistence across application restarts</li>
 *   <li>Configurable TTL per cache</li>
 * </ul>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCTS_CACHE = "products";
    public static final String PRODUCT_BY_ID_CACHE = "productById";

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:#{null}}")
    private String redisPassword;

    @Value("${spring.cache.redis.time-to-live:3600000}")
    private long defaultTtl; // Default 1 hour in milliseconds

    /**
     * Redis connection factory for connecting to Redis server.
     *
     * <p>Uses Lettuce client (recommended over Jedis for better performance
     * and native async support).</p>
     *
     * @return RedisConnectionFactory
     */
    @Bean
    @Profile("!test")
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);

        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.setPassword(redisPassword);
        }

        return new LettuceConnectionFactory(config);
    }

    /**
     * Redis template for manual Redis operations.
     *
     * <p>Configured with Jackson serialization for proper object handling.</p>
     *
     * @param connectionFactory the Redis connection factory
     * @return RedisTemplate
     */
    @Bean
    @Profile("!test")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serialization for keys
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Use JSON serialization for values
        GenericJackson2JsonRedisSerializer jsonSerializer = createJsonSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis-based cache manager for Spring Cache abstraction.
     *
     * <p>Configures different TTLs for different caches:
     * <ul>
     *   <li>products: 30 minutes (frequently changing)</li>
     *   <li>productById: 1 hour (individual products change less)</li>
     * </ul>
     *
     * @param connectionFactory the Redis connection factory
     * @return CacheManager
     */
    @Bean
    @Profile("!test")
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(defaultTtl))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(createJsonSerializer())
                )
                .disableCachingNullValues();

        // Custom TTL for specific caches
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Products cache: 30 minutes (list changes frequently)
        cacheConfigurations.put(PRODUCTS_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Product by ID cache: 1 hour (individual products are more stable)
        cacheConfigurations.put(PRODUCT_BY_ID_CACHE,
                defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }

    /**
     * Creates JSON serializer for Redis values.
     *
     * <p>Configured with polymorphic type handling to properly
     * serialize/deserialize complex objects.</p>
     *
     * @return GenericJackson2JsonRedisSerializer
     */
    private GenericJackson2JsonRedisSerializer createJsonSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Enable type information for polymorphic deserialization
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /**
     * In-memory cache manager for test profile.
     *
     * <p>Uses simple ConcurrentHashMap for fast, isolated testing.</p>
     *
     * @return CacheManager
     */
    @Bean
    @Profile("test")
    public CacheManager testCacheManager() {
        return new org.springframework.cache.concurrent.ConcurrentMapCacheManager(
                PRODUCTS_CACHE,
                PRODUCT_BY_ID_CACHE
        );
    }
}
