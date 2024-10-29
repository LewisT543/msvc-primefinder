package com.example.msvcprimefinder.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ExecutorServiceProvider {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorServiceProvider.class);
    private final ExecutorService executor;

    public ExecutorServiceProvider() {
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public long getDynamicSegmentSize(long limit) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        long freeMemory = Runtime.getRuntime().freeMemory();
        int scalingFactor = switch((int) Math.log10(limit)) {
            case 0, 1, 2, 3, 4, 5, 6, 7     -> 1;   // up to 10^7 (10_000_000)        10M
            case 8, 9                       -> 2;   // up to 10^9 (1_000_000_000)     1B
            default                         -> 4;   // beyond 10^10 (10_000_000_000)  10B+ (bad idea...)
        };
        logger.info("[Concurrent Sieve]:[Dynamic Segment Size] SegmentSize scaling factor: " + scalingFactor);
        long maxMemPerThread = freeMemory / availableProcessors / scalingFactor;
        return Math.min(maxMemPerThread, (long) Math.sqrt(limit));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
