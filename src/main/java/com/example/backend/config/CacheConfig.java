package com.example.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cache configuration for application-level caching.
 *
 * <p>This configuration uses in-memory caching. For production use,
 * consider integrating Redis or another distributed cache.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PRODUCTS_CACHE = "products";
    public static final String PRODUCT_BY_ID_CACHE = "productById";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                PRODUCTS_CACHE,
                PRODUCT_BY_ID_CACHE
        );
    }
}
