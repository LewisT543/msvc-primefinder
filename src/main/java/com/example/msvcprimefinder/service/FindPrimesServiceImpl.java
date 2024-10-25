package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.exception.FindPrimesArgException;
import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.util.PrimeTimingUtil;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import static com.example.msvcprimefinder.algo.PrimeFinder.*;

@Service
public class FindPrimesServiceImpl implements FindPrimesService {
    private final EnumSet<PrimeAlgorithmNames> validLargeLimitAlgos = EnumSet.of(
            PrimeAlgorithmNames.SEGMENTED_SIEVE,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_BITSET,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_STREAMS,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_CONCURRENT    
    );
    
    public FindPrimesResponse findPrimes(long limit, PrimeAlgorithmNames selectedAlgorithm) {
        throwInputErrors(limit, selectedAlgorithm);

        Supplier<List<Long>> primeCalculationFn = switch(selectedAlgorithm) {
            case NAIVE:                         yield () -> findPrimesNaive(limit);
            case SIEVE:                         yield () -> findPrimesWithSieve(limit);
            case SIEVE_BITSET:                  yield () -> findPrimesWithSieve_BitSet(limit);
            case SIEVE_STREAMS:                 yield () -> findPrimesWithSieve_StreamsAPI(limit);
            case SEGMENTED_SIEVE:               yield () -> findPrimesWithSegmentedSieve(limit);
            case SEGMENTED_SIEVE_BITSET:        yield () -> findPrimesWithSegmentedSieve_BitSet(limit);
            case SEGMENTED_SIEVE_STREAMS:       yield () -> findPrimesWithSegmentedSieve_StreamsAPI(limit);
            case SEGMENTED_SIEVE_CONCURRENT:    yield () -> findPrimesWithSegmentedSieve_Concurrent(limit);
        };

        return PrimeTimingUtil.measureExecutionTime(primeCalculationFn, selectedAlgorithm.name());
    }

    private void throwInputErrors(long limit, PrimeAlgorithmNames selectedAlgorithm) {
        if (limit < 2) {
            throw new FindPrimesArgException("Limit must be greater than or equal to 2");
        }
        if (limit >= Integer.MAX_VALUE && !validLargeLimitAlgos.contains(selectedAlgorithm)) {
            throw new FindPrimesArgException("Limit is greater than MAX_INT, please use the Segmented-Sieve algorithm");
        }
    }
}

