package com.example.msvcprimefinder.exception;

public class ConcurrentSieveException extends RuntimeException {
    public ConcurrentSieveException(String message, Throwable cause) {
        super(message, cause);
    }
}
