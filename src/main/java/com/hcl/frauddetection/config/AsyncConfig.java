package com.hcl.frauddetection.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "ruleExecutor")
    public Executor ruleExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Core threads: number of threads always kept alive
        executor.setCorePoolSize(10);
        // Max threads: maximum allowed threads under heavy load
        executor.setMaxPoolSize(50);
        // Queue capacity: how many tasks wait before creating new threads
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("RiskRule-");
        executor.initialize();
        return executor;
    }
}
