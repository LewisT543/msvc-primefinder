package com.example.msvcprimefinder.controller;

import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface FindPrimesAPI {
    @Operation(
            summary = "Find primes up to and including a specified limit",
            description = "Returns a result containing a list of prime numbers up to the given limit, the specified algorithm and time taken.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved primes",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = FindPrimesResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            }
    )
    @GetMapping("/api/find-primes")
    ResponseEntity<FindPrimesResponse> findPrimes(
            @Parameter(description = "Upper limit (inclusive) for finding primes", required = true, in = ParameterIn.QUERY)
            @RequestParam long limit,

            @Parameter(description = "Algorithm to use for finding primes", required = false, in = ParameterIn.QUERY)
            @RequestParam(required = false, defaultValue = "NAIVE") PrimeAlgorithmNames algo,

            @Parameter(description = "Use cache if available", required = false, in = ParameterIn.QUERY)
            @RequestParam(required = false, defaultValue = "false") boolean useCache,

            @Parameter(description = "Build cache after finding primes (slow)", required = false, in = ParameterIn.QUERY)
            @RequestParam(required = false, defaultValue = "false") boolean buildCache
    );
}

