package com.example.msvcprimefinder.controller;

import com.example.msvcprimefinder.service.PrimeCacheService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FindPrimesControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @SpyBean
    PrimeCacheService primeCacheService;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        reset(primeCacheService);
    }

    private final long[] primesTo100 = new long[]{2L, 3L, 5L, 7L,11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L,
            43L, 47L, 53L, 59L, 61L, 67L, 71L, 73L, 79L, 83L, 89L, 97L};

    private long[] mapToArr(List<Long> primes) {
        return primes.stream().mapToLong(Long::valueOf).toArray();
    }

    @Test
    void findPrimes_NoCache_SmallLimit_Happy() {
        Response response = given()
            .queryParam("limit", 100)
            .queryParam("useCache", false)
            .queryParam("algorithm", "SIEVE")
            .when()
            .get("/api/find-primes");

        long[] responsePrimes = mapToArr(response.jsonPath().getList("result", Long.class));

        response.then()
            .statusCode(HttpStatus.OK.value())
            .body("algorithmName", equalTo("SIEVE"));

        assertArrayEquals(primesTo100, responsePrimes);
        verify(primeCacheService, never()).addPrimesToCache(any(long[].class));
    }

    @Test
    void findPrimes_WithCache_SmallLimit_Happy() {
        long limit = 100;
        Response response = given()
            .queryParam("limit", limit)
            .queryParam("useCache", true)
            .queryParam("algorithm", "SIEVE")
            .when()
            .get("/api/find-primes");

        long[] responsePrimes = mapToArr(response.jsonPath().getList("result", Long.class));

        response.then()
            .statusCode(HttpStatus.OK.value())
            .body("algorithmName", equalTo("SIEVE"));

        assertArrayEquals(primesTo100, responsePrimes);
        assertArrayEquals(primesTo100, primeCacheService.getPrimesFromCacheToLimit(limit), "Cache should contain result upto and including limit");

        Response response2 = given()
            .queryParam("limit", limit)
            .queryParam("useCache", true)
            .when()
            .get("/api/find-primes");

        long[] responsePrimes2 = mapToArr(response2.jsonPath().getList("result", Long.class));

        response2.then()
                .statusCode(HttpStatus.OK.value())
                .body("algorithmName", equalTo("CACHE_HIT"));

        assertArrayEquals(primesTo100, responsePrimes2);
        verify(primeCacheService, times(2)).getPrimesFromCacheToLimit(limit); // limit 2 here allowing for above check on line 81
    }

    @Test
    public void findPrimes_SmartAlgorithm_SwitchBasedOnLimit() {
        given()
            .queryParam("limit", 10000)
            .queryParam("algorithm", "SMART")
            .when()
            .get("/api/find-primes")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("algorithmName", equalTo("SIEVE"));

        given()
            .queryParam("limit", 10000000)
            .queryParam("algorithm", "SMART")
            .when()
            .get("/api/find-primes")
            .then()
            .statusCode(HttpStatus.OK.value())
            .body("algorithmName", equalTo("SEGMENTED_SIEVE_CONCURRENT"));
    }

    @Test
    public void findPrimes_InvalidArgumentHandling() {
        given()
            .queryParam("limit", -5)
            .when()
            .get("/api/find-primes")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .body("message", containsString("findPrimes.limit: must be greater than or equal to 2"));

        verify(primeCacheService, never()).addPrimesToCache(any(long[].class));
    }

    @Test
    public void findPrimes_LimitTooBig_MemoryErr() {
        long limit = 7_000_000_000L;
        given()
                .queryParam("limit", limit)
                .when()
                .get("/api/find-primes")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("message", containsString("Not enough memory to process limit: 7000000000"));

        verify(primeCacheService, never()).addPrimesToCache(any(long[].class));
    }


    @Test
    void findPrimes_XMLResponse_NoCache_SmallLimit_Happy() {
        Response response = given()
                .queryParam("limit", 100)
                .queryParam("useCache", false)
                .queryParam("withResult", true)
                .queryParam("algorithm", "SIEVE")
                .header("Accept", "application/xml")
                .when()
                .get("/api/find-primes");

        long[] responsePrimes = mapToArr(response.xmlPath().getList("FindPrimesResponse.result.prime", Long.class));
        Long responseLength = response.xmlPath().getLong("FindPrimesResponse.numberOfPrimes");

        assertArrayEquals(primesTo100, responsePrimes);
        assertEquals(primesTo100.length, responseLength);

        response.then()
                .statusCode(HttpStatus.OK.value())
                .contentType("application/xml")
                .body("FindPrimesResponse.algorithmName", equalTo("SIEVE"));

        verify(primeCacheService, never()).addPrimesToCache(any(long[].class));
    }

    @Test
    void findPrimes_XMLResponse_InvalidArgumentHandling() {
        Response response = given()
                .queryParam("limit", -5)
                .header("Accept", "application/xml")
                .when()
                .get("/api/find-primes");

        response.then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .contentType("application/xml")
                .body("FindPrimesErrorResponse.message", containsString("findPrimes.limit: must be greater than or equal to 2"));

        verify(primeCacheService, never()).addPrimesToCache(any(long[].class));
    }
}
