package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.exception.InvalidNumberException;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.util.TimingUtil;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class FindPrimesServiceImpl implements FindPrimesService {
    public FindPrimesResponse findPrimes(long limit) {
        if (limit < 2) {
            throw new InvalidNumberException("Limit must be greater than or equal to 2");
        }
        return TimingUtil.measureExecutionTime((l) ->
                new FindPrimesResponse(calculatePrimes(l), 500),
                limit,
                "findPrimes()"
        );
    }

    private List<Integer> calculatePrimes(long limit) {
        return Arrays.asList(1,2,3);
    }
}

