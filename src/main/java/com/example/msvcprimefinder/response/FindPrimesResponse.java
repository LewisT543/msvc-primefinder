package com.example.msvcprimefinder.response;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.LocalDateTime;
import java.util.List;

@JacksonXmlRootElement(localName = "FindPrimesResponse")
public record FindPrimesResponse(
        @JacksonXmlElementWrapper(localName = "result")
        @JacksonXmlProperty(localName = "prime") List<Long> result,
        @JacksonXmlProperty(localName = "numberOfPrimes") long numberOfPrimes,
        @JacksonXmlProperty(localName = "executionTimeMs") long executionTimeMs,
        @JacksonXmlProperty(localName = "executionTimeNs") long executionTimeNs,
        @JacksonXmlProperty(localName = "algorithmName") String algorithmName,
        @JacksonXmlProperty(localName = "useCache") boolean useCache,
        @JacksonXmlProperty(localName = "timestamp") LocalDateTime timestamp
) {
    public FindPrimesResponse(List<Long> result, long numberOfPrimes, long executionTimeMs, long executionTimeNs, String algorithmName, boolean useCache) {
        this(result, numberOfPrimes, executionTimeMs, executionTimeNs, algorithmName, useCache, LocalDateTime.now());
    }
}
