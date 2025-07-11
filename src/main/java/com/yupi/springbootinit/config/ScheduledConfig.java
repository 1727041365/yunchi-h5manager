package com.yupi.springbootinit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class ScheduledConfig implements SchedulingConfigurer {
    /**
     * 配置定时任务线程池
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 更灵活的线程池配置（推荐）
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(30); // 核心线程数
        scheduler.setThreadNamePrefix("scheduled-Market"); // 线程名前缀（方便日志追踪）
        scheduler.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy()); // 任务拒绝策略
        scheduler.setAwaitTerminationSeconds(60); // 线程池关闭时等待任务完成的时间
        scheduler.setWaitForTasksToCompleteOnShutdown(true); // 关闭时是否等待任务完成
        scheduler.initialize();
        taskRegistrar.setScheduler(scheduler);
    }
}