package com.pocketmentor.pocketmentor.config;

import org.springframework.context.annotation.*;
import java.util.concurrent.*;

@Configuration
public class AsyncConfig {
    @Bean(destroyMethod = "shutdown")
    public ExecutorService genExecutor() { return Executors.newFixedThreadPool(2); }
}