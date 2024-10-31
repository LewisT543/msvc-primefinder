package com.example.msvcprimefinder.util.type;

public record PrimesTimerResult<T>(T result, long durationMs, long durationNs) {}
