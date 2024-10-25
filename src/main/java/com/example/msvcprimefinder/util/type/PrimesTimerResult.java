package com.example.msvcprimefinder.util.type;

public record PrimesTimerResult<T>(T primes, long durationMs, long durationNs) {}
