package com.example.msvcprimefinder.config;

import com.example.msvcprimefinder.service.ExecutorServiceProvider;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

    private final ExecutorServiceProvider executorServiceProvider;

    @Autowired
    public ExecutorConfig(ExecutorServiceProvider executorServiceProvider) {
        this.executorServiceProvider = executorServiceProvider;
    }

    @PreDestroy
    public void shutdown() {
        executorServiceProvider.shutdown();
    }
}
