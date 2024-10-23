package com.example.msvcprimefinder.response;

import java.time.LocalDateTime;
import java.util.List;

public record FindPrimesResponse(List<Long> result, long executionTimeNs, LocalDateTime timestamp) {
    public FindPrimesResponse(List<Long> result, long executionTimeNs) {
        this(result, executionTimeNs, LocalDateTime.now());
    }
}
