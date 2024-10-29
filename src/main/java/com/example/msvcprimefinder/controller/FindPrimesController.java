package com.example.msvcprimefinder.controller;

import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.service.FindPrimesService;
import com.example.msvcprimefinder.service.FindPrimesServiceImpl;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api")
public class FindPrimesController implements FindPrimesAPI {

    private final FindPrimesService findPrimesService;

    @Autowired
    public FindPrimesController(FindPrimesServiceImpl findPrimesService) {
        this.findPrimesService = findPrimesService;
    }

    @GetMapping("/find-primes")
    public ResponseEntity<FindPrimesResponse> findPrimes(
            @RequestParam @Min(2) long limit,
            @RequestParam(required = false, defaultValue = "SMART") PrimeAlgorithmNames algo,
            @RequestParam(required = false, defaultValue = "false") boolean useCache,
            @RequestParam(required = false, defaultValue = "false") boolean buildCache,
            @RequestParam(required = false, defaultValue = "true") boolean withResult
    ) {
        return ResponseEntity.ok(findPrimesService.findPrimes(limit, algo, useCache, buildCache, withResult));
    }
}

