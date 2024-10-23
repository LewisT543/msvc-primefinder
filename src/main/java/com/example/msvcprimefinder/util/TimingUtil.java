package com.example.msvcprimefinder.util;

import com.example.msvcprimefinder.service.FindPrimesServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class TimingUtil {
    private static final Logger logger = LoggerFactory.getLogger(FindPrimesServiceImpl.class);

    public static <T, R> R measureExecutionTime(Function<T, R> function, T input, String functionName) {
        long startTime = System.currentTimeMillis();
        R result = function.apply(input);
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Execution Time for {}: {} ms", functionName, duration);
        return result;
    }
}
