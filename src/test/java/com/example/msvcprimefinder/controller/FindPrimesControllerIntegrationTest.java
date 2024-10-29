package com.example.msvcprimefinder.controller;

import com.example.msvcprimefinder.model.entity.Prime;
import com.example.msvcprimefinder.repository.PrimeRepository;
import com.example.msvcprimefinder.service.FindPrimesService;
import com.example.msvcprimefinder.service.FindPrimesServiceImpl;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FindPrimesControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private PrimeRepository primeRepository;

    @Autowired
    private FindPrimesService primesService;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        primesService.deleteAllPrimesSafe();
    }

    private final List<Long> primesTo100 = List.of(2L, 3L, 5L, 7L,11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L,
            43L, 47L, 53L, 59L, 61L, 67L, 71L, 73L, 79L, 83L, 89L, 97L);

    @Test
    void findPrimes_NoCache_SmallLimit_Happy() {
        Response response = given()
            .queryParam("limit", 100)
            .queryParam("useCache", false)
            .queryParam("buildCache", false)
            .queryParam("algorithm", "SIEVE")
        .when()
            .get("/api/find-primes");
        List<Long> responsePrimes = response.jsonPath().getList("result", Long.class);
        response.then()
            .statusCode(HttpStatus.OK.value())
            .body("algorithmName", equalTo("SIEVE"));
        assertEquals(primesTo100, responsePrimes);
        assertTrue(primeRepository.findAll().isEmpty(), "Database should remain empty");
    }

    @Test
    void findPrimes_WithCache_SmallLimit_Happy() {
        Response response = given()
            .queryParam("limit", 100)
            .queryParam("useCache", false)
            .queryParam("buildCache", true)
            .queryParam("algorithm", "SIEVE")
        .when()
            .get("/api/find-primes");
        List<Long> responsePrimes = response.jsonPath().getList("result", Long.class);
        response.then()
            .statusCode(HttpStatus.OK.value())
            .body("algorithmName", equalTo("SIEVE"));
        assertEquals(primesTo100, responsePrimes);
        assertEquals(primesTo100, primeRepository.findAll().stream().map(Prime::getValue).toList(), "Database should contain primes upto and including limit");
        Response response2 = given()
            .queryParam("limit", 100)
            .queryParam("useCache", true)
        .when()
            .get("/api/find-primes");
        List<Long> responsePrimes2 = response2.jsonPath().getList("result", Long.class);
        response2.then()
                .statusCode(HttpStatus.OK.value())
                .body("algorithmName", equalTo("CACHE_HIT"));
        assertEquals(primesTo100, responsePrimes2);
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
        assertTrue(primeRepository.findAll().isEmpty(), "Database should remain empty after invalid request");
    }
}
