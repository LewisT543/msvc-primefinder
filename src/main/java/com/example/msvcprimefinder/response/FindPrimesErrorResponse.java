package com.example.msvcprimefinder.response;

import java.time.LocalDateTime;

public record FindPrimesErrorResponse(String message, int status, LocalDateTime timestamp) {
    public FindPrimesErrorResponse(String message, int status) {
        this(message, status, LocalDateTime.now());
    }
}
