package com.example.msvcprimefinder.util.type;

import java.util.List;

public record PrimesTimerResult(List<Long> primes, long durationMs, long durationNs) {}
