package com.example.msvcprimefinder.util;

import com.example.msvcprimefinder.util.type.PrimesTimerResult;

import java.util.List;
import java.util.function.Supplier;

public class PrimesTimer {
    public static <T>PrimesTimerResult<T> measureExecutionTime(Supplier<T> fn) {
        long startTimeMs = System.currentTimeMillis();
        long startTimeNs = System.nanoTime();
        T result = fn.get();
        long durationNs = System.nanoTime() - startTimeNs;
        long durationMs = System.currentTimeMillis() - startTimeMs;
        return new PrimesTimerResult<>(result, durationMs, durationNs);
    }
}
