package com.spmorangle.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Slf4j
@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // Configure thread pool size for scheduled tasks
        scheduler.setPoolSize(5);

        // Set thread name prefix for easy identification in logs
        scheduler.setThreadNamePrefix("scheduled-task-");

        // Remove tasks from scheduler when cancelled
        scheduler.setRemoveOnCancelPolicy(true);

        // Set error handler for uncaught exceptions in scheduled tasks
        scheduler.setErrorHandler(throwable ->
            log.error("Unexpected error in scheduled task", throwable)
        );

        // Set thread group name
        scheduler.setThreadGroupName("scheduled-tasks");

        // Wait for tasks to complete on shutdown
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);

        scheduler.initialize();

        taskRegistrar.setTaskScheduler(scheduler);

        log.info("Configured scheduled task thread pool with {} threads", scheduler.getPoolSize());
    }
}

