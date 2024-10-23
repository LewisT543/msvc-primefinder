package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.algo.PrimeFinder;
import com.example.msvcprimefinder.exception.FindPrimesArgException;
import com.example.msvcprimefinder.model.enums.PrimeAlgorithms;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.util.PrimeTimingUtil;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
public class FindPrimesServiceImpl implements FindPrimesService {
    public FindPrimesResponse findPrimes(long limit, PrimeAlgorithms selectedAlgo) {
        throwInputErrors(limit, selectedAlgo);

        Supplier<List<Long>> primeCalculationFn = switch(selectedAlgo) {
            case SIEVE:             yield () -> PrimeFinder.findPrimesWithSieve(limit);
            case SIEVE_STREAMS:     yield () -> PrimeFinder.findPrimesWithSieve_StreamsAPI(limit);
            case SEGMENTED_SIEVE:   yield () -> PrimeFinder.findPrimesWithSegmentedSieve(limit);
            case NAIVE:             yield () -> PrimeFinder.findPrimesNaive(limit);
        };

        return PrimeTimingUtil.measureExecutionTime(primeCalculationFn, selectedAlgo.name());
    }

    private void throwInputErrors(long limit, PrimeAlgorithms selectedAlgo) {
        if (limit < 2) {
            throw new FindPrimesArgException("Limit must be greater than or equal to 2");
        }
        if (limit >= Integer.MAX_VALUE && selectedAlgo != PrimeAlgorithms.SEGMENTED_SIEVE) {
            throw new FindPrimesArgException("Limit is greater than MAX_INT, please use the Segmented-Sieve algorithm");
        }
    }
}

