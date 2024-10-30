package com.example.msvcprimefinder.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.LocalDateTime;

@JacksonXmlRootElement(localName = "FindPrimesErrorResponse")
public record FindPrimesErrorResponse(
        @JacksonXmlProperty(localName = "message") String message,
        @JacksonXmlProperty(localName = "status") int status,
        @JacksonXmlProperty(localName = "timestamp") LocalDateTime timestamp
) {
    public FindPrimesErrorResponse(String message, int status) {
        this(message, status, LocalDateTime.now());
    }
}
