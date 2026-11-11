package com.kkumi.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// SchedulingConfigurer : @Scheduled는 이 스케쥴러를 쓰라고 지정해줌
@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {

    private static final int POOL_SIZE = 2;

    // shutdown이라 서버 종료 때 스레드도 같이 정리 됨
    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService schedulingExecutor() {
        return Executors.newScheduledThreadPool(POOL_SIZE,
                Thread.ofPlatform().name("scheduling-", 1).factory());
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(schedulingExecutor());
    }
}
