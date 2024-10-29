package com.example.msvcprimefinder.response;

import java.time.LocalDateTime;
import java.util.List;

public record FindPrimesResponse(List<Long> result, long numberOfPrimes, long executionTimeMs, long executionTimeNs, String algorithmName, boolean useCache, LocalDateTime timestamp) {
    public FindPrimesResponse(List<Long> result, long numberOfPrimes, long executionTimeMs, long executionTimeNs, String algorithmName, boolean useCache) {
        this(result, numberOfPrimes, executionTimeMs, executionTimeNs, algorithmName, useCache, LocalDateTime.now());
    }
}
