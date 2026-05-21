package com.example.desafio.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Define o pool de threads dedicado ao processamento de CSV.
 *
 * Pool separado do Tomcat para que uploads grandes não bloqueiem outras requisições.
 * Tamanhos configuráveis via application.properties para ajuste por ambiente.
 */
@Configuration
public class AsyncConfig {

    @Value("${app.processing.core-pool-size:4}")
    private int corePoolSize;

    @Value("${app.processing.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${app.processing.queue-capacity:100}")
    private int queueCapacity;

    @Value("${app.processing.thread-name-prefix:chunk-processor-}")
    private String threadNamePrefix;

    /**
     * Executor referenciado em @Async("csvTaskExecutor").
     * WaitForTasksToCompleteOnShutdown=true garante que o app não encerra
     * no meio de um processamento durante um graceful shutdown.
     */
    @Bean("csvTaskExecutor")
    public Executor csvTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(120);
        executor.initialize();
        return executor;
    }
}
