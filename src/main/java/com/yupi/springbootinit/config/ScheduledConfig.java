package com.yupi.springbootinit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ScheduledConfig {
    /**
     * 第一个线程池：用于 ScheduledUpdateDate 任务
     */
    @Bean(name = "marketThreadPool") // 线程池名称，用于@Async指定
    public ThreadPoolTaskExecutor marketThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 核心线程数
        executor.setMaxPoolSize(20); // 最大线程数
        executor.setQueueCapacity(100); // 任务队列容量
        executor.setThreadNamePrefix("market-"); // 线程名前缀（方便日志区分）
        // 任务拒绝策略：当线程池满时，让提交任务的线程执行任务（避免任务丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 线程池关闭时等待所有任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60); // 等待时间（秒）
        executor.initialize();
        return executor;
    }
    /**
     * 第二个线程池：用于 RwScheduledUpdateDate 任务
     */
    @Bean(name = "rwThreadPool") // 线程池名称，用于@Async指定
    public ThreadPoolTaskExecutor rwThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // 核心线程数（可根据任务需求调整）
        executor.setMaxPoolSize(20); // 最大线程数
        executor.setQueueCapacity(100); // 任务队列容量
        executor.setThreadNamePrefix("rw-"); // 线程名前缀（与第一个线程池区分）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    /**
     * 专门用于 saveOrUpdateDetail 任务的线程池
     * 任务耗时久（约5分钟），适当调整参数
     */
    @Bean(name = "longTaskThreadPool")
    public ThreadPoolTaskExecutor longTaskThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数，根据服务器性能和任务数量调整，这里给5个
        executor.setCorePoolSize(10);
        // 最大线程数，防止任务积压过多时资源耗尽
        executor.setMaxPoolSize(20);
        // 任务队列容量，缓冲待执行任务
        executor.setQueueCapacity(100);
        // 线程名前缀，方便日志排查
        executor.setThreadNamePrefix("long-task-");
        // 任务拒绝策略：队列满时由调用线程执行（避免任务丢失）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 关闭时等待任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间（秒），根据任务最长耗时设置
        executor.setAwaitTerminationSeconds(900);
        executor.initialize();
        return executor;
    }
}