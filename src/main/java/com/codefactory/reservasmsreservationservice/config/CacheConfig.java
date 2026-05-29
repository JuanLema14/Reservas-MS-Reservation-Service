package com.codefactory.reservasmsreservationservice.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching to reduce redundant external service calls.
 * Uses Caffeine cache for in-memory caching with TTL.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CLIENT_CACHE = "clientCache";
    public static final String SERVICE_CACHE = "serviceCache";
    public static final String PROVIDER_CACHE = "providerCache";
    public static final String EMPLOYEE_CACHE = "employeeCache";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                CLIENT_CACHE,
                SERVICE_CACHE,
                PROVIDER_CACHE,
                EMPLOYEE_CACHE
        );

        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(1000)                    // Max 1000 entries per cache
                .expireAfterWrite(5, TimeUnit.MINUTES) // TTL: 5 minutes
                .recordStats());                      // Enable stats for monitoring

        return cacheManager;
    }
}
