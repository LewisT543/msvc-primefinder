package com.example.msvcprimefinder.util;

import com.example.msvcprimefinder.util.type.PrimesTimerResult;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class PrimeTimerTest {

    @Test
    void measureExecutionTime_validFunction_returnsResultAndDuration() {
        Supplier<String> functionToTest = () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "Primes";
        };
        PrimesTimerResult<String> result = PrimesTimer.measureExecutionTime(functionToTest);
        assertEquals("Primes", result.result());
        assertTrue(result.durationMs() >= 100);
        assertTrue(result.durationNs() >= TimeUnit.MILLISECONDS.toNanos(100));
    }

    @Test
    void measureExecutionTime_emptyFunction_returnsResultAndDuration() {
        Supplier<Integer> emptyFunction = () -> 10;
        PrimesTimerResult<Integer> result = PrimesTimer.measureExecutionTime(emptyFunction);
        assertEquals(10, result.result());
        assertTrue(result.durationMs() >= 0);
        assertTrue(result.durationNs() >= 0);
    }

    @Test
    void measureExecutionTime_functionThrowsException() {
        Supplier<Void> functionThatThrows = () -> {
            throw new RuntimeException("Test exception");
        };
        Exception exception = assertThrows(RuntimeException.class, () -> {
            PrimesTimer.measureExecutionTime(functionThatThrows);
        });
        assertEquals("Test exception", exception.getMessage());
    }
}
