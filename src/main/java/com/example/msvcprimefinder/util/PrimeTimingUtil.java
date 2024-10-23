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
        long startTime = System.nanoTime();
        List<Long> result = fn.get();
        long duration = System.nanoTime() - startTime;
        logger.info("Execution Time for {}: {} ns", fnName, duration);
        return new FindPrimesResponse(result, duration);
    }
}
