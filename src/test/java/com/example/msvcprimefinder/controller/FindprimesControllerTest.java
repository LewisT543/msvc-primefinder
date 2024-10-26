package com.example.msvcprimefinder.controller;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FindprimesControllerTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.port = port;
    }

    @Test
    void findPrimes_validRequest_returnsResponse() {
        given()
                .param("limit", 100)
                .param("algo", "SIEVE")
                .when()
                .get("/api/find-primes")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
    }
}
