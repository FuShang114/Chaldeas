package com.example.concurrency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.cache.annotation.EnableCaching;

import java.util.concurrent.Executor;

/**
 * Spring Boot 高并发接口演示应用
 * 支持高并发秒杀场景
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
public class ConcurrencyDemoApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ConcurrencyDemoApplication.class, args);
    }
    
    /**
     * 异步任务执行器配置
     * 用于处理高并发场景下的异步任务
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("Concurrency-");
        executor.initialize();
        return executor;
    }
    
    /**
     * 秒杀任务执行器
     * 专门处理秒杀相关的高并发请求
     */
    @Bean(name = "seckillExecutor")
    public Executor seckillExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Seckill-");
        executor.initialize();
        return executor;
    }
}