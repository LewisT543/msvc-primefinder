package com.example.msvcprimefinder.util;

import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.service.FindPrimesServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

public class PrimeTimingUtil {
    private static final Logger logger = LoggerFactory.getLogger(FindPrimesServiceImpl.class);

    public static FindPrimesResponse measureExecutionTime(Supplier<List<Long>> fn, String fnName) {
        long startTimeMs = System.currentTimeMillis();
        long startTimeNs = System.nanoTime();
        List<Long> result = fn.get();
        long durationNs = System.nanoTime() - startTimeNs;
        long durationMs = System.currentTimeMillis() - startTimeMs;
        logger.info("Execution Time for {}: {} ms", fnName, durationMs);
        return new FindPrimesResponse(result, result.size(), durationMs, durationNs, fnName);
    }
}
