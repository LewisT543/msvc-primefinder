package com.example.msvcprimefinder.controller;

import com.example.msvcprimefinder.service.FindPrimesService;
import com.example.msvcprimefinder.service.RedisPrimeCacheService;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FindPrimesControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private RedisPrimeCacheService redisPrimeCacheService;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        redisPrimeCacheService = spy(redisPrimeCacheService);
    }

    private final List<Long> primesTo100 = List.of(2L, 3L, 5L, 7L,11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L,
            43L, 47L, 53L, 59L, 61L, 67L, 71L, 73L, 79L, 83L, 89L, 97L);

    @Test
    void findPrimes_NoCache_SmallLimit_Happy() {
        Response response = given()
            .queryParam("limit", 100)
            .queryParam("useCache", false)
            .queryParam("algorithm", "SIEVE")
            .when()
            .get("/api/find-primes");

        List<Long> responsePrimes = response.jsonPath().getList("result", Long.class);

        response.then()
            .statusCode(HttpStatus.OK.value())
            .body("algorithmName", equalTo("SIEVE"));

        assertEquals(primesTo100, responsePrimes);
        verify(redisPrimeCacheService, never()).savePrimes(anyList());
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

        List<Long> responsePrimes = response.jsonPath().getList("result", Long.class);

        response.then()
            .statusCode(HttpStatus.OK.value())
            .body("algorithmName", equalTo("SIEVE"));

        assertEquals(primesTo100, responsePrimes);
        assertEquals(primesTo100, redisPrimeCacheService.getPrimesUpTo(limit), "Database should contain primes upto and including limit");

        Response response2 = given()
            .queryParam("limit", limit)
            .queryParam("useCache", true)
            .when()
            .get("/api/find-primes");

        List<Long> responsePrimes2 = response2.jsonPath().getList("result", Long.class);

        response2.then()
                .statusCode(HttpStatus.OK.value())
                .body("algorithmName", equalTo("CACHE_HIT"));

        assertEquals(primesTo100, responsePrimes2);
        verify(redisPrimeCacheService, times(1)).getPrimesUpTo(limit);
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

        verify(redisPrimeCacheService, never()).savePrimes(anyList());
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

        List<Long> responsePrimes = response.xmlPath().getList("FindPrimesResponse.result.prime", Long.class);
        Long responseLength = response.xmlPath().getLong("FindPrimesResponse.numberOfPrimes");

        assertEquals(primesTo100, responsePrimes);
        assertEquals(primesTo100.size(), responseLength);

        response.then()
                .statusCode(HttpStatus.OK.value())
                .contentType("application/xml")
                .body("FindPrimesResponse.algorithmName", equalTo("SIEVE"));

        verify(redisPrimeCacheService, never()).savePrimes(anyList());
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

        verify(redisPrimeCacheService, never()).savePrimes(anyList());
    }

}
