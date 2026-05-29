package com.codefactory.reservasmsreservationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async operations (email sending, etc.).
 * Uses a dedicated thread pool to avoid blocking the main request thread.
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * Thread pool for email sending operations.
     * Configured with sensible defaults for email workloads.
     */
    @Bean(name = "emailExecutor")
    public Executor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);           // Always running threads
        executor.setMaxPoolSize(5);            // Max threads under load
        executor.setQueueCapacity(100);        // Queue size for pending tasks
        executor.setThreadNamePrefix("email-");
        executor.setRejectedExecutionHandler((r, e) -> 
            log.warn("Email task rejected - queue full, email may not be sent"));
        executor.initialize();
        log.info("Email async executor initialized: core={}, max={}, queue={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());
        return executor;
    }

    /**
     * Default async executor for general async operations.
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
