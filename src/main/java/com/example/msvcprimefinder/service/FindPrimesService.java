package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesResponse;

import java.util.concurrent.ExecutionException;

public interface FindPrimesService {
    FindPrimesResponse findPrimes(long limit, PrimeAlgorithmNames selectedAlgorithm, boolean buildCache, boolean withCache, boolean withResult);
    void deleteAllPrimesSafe();
}
