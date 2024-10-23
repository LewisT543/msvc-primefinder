package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.model.enums.PrimeAlgorithms;
import com.example.msvcprimefinder.response.FindPrimesResponse;

public interface FindPrimesService {
    FindPrimesResponse findPrimes(long limit, PrimeAlgorithms selectedAlgo);
}
