package com.example.msvcprimefinder.response;

import java.time.LocalDateTime;
import java.util.List;

public record FindPrimesResponse(List<Integer> result, long executionTime, LocalDateTime timestamp) {
    public FindPrimesResponse(List<Integer> result, long executionTime) {
        this(result, executionTime, LocalDateTime.now());
    }
}
