package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.response.FindPrimesResponse;

public interface FindPrimesService {
    FindPrimesResponse findPrimes(long limit);
}
