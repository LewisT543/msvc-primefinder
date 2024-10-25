package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.exception.FindPrimesArgException;
import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.util.PrimesTimer;
import com.example.msvcprimefinder.util.type.PrimesTimerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;

import static com.example.msvcprimefinder.algo.PrimeFinder.*;

@Service
public class FindPrimesServiceImpl implements FindPrimesService {
    private static final Logger logger = LoggerFactory.getLogger(FindPrimesServiceImpl.class);
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

        PrimesTimerResult result = PrimesTimer.measureExecutionTime(primeCalculationFn);

        logger.info("Execution Time for {}: {} ms", selectedAlgorithm.name(), result.durationMs());

        return new FindPrimesResponse(List.of(1L, 2L, 3L), result.primes().size(), result.durationMs(), result.durationNs(), selectedAlgorithm.name());
    }

    private void throwInputErrors(long limit, PrimeAlgorithmNames selectedAlgorithm) {
        if (limit < 2) {
            logger.warn("[findPrimes]: limit < 2");
            throw new FindPrimesArgException("Limit must be greater than or equal to 2");
        }
        if (limit >= Integer.MAX_VALUE && !validLargeLimitAlgos.contains(selectedAlgorithm)) {
            logger.warn("[findPrimes]: limit < MAX_INT without Seg-Sieve algorithm");
            throw new FindPrimesArgException("Limit is greater than MAX_INT, please use the Segmented-Sieve algorithm");
        }
    }
}

