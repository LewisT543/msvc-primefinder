package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.exception.FindPrimesArgException;
import com.example.msvcprimefinder.model.entity.Prime;
import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.repository.PrimeRepository;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.util.PrimesTimer;
import com.example.msvcprimefinder.util.type.PrimesTimerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.example.msvcprimefinder.algo.PrimeFinder.*;

@Service
public class FindPrimesServiceImpl implements FindPrimesService {
    private static final Logger logger = LoggerFactory.getLogger(FindPrimesServiceImpl.class);
    private static final EnumSet<PrimeAlgorithmNames> VALID_LARGE_LIMIT_ALGORITHMS = EnumSet.of(
            PrimeAlgorithmNames.SEGMENTED_SIEVE,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_BITSET,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_STREAMS,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_CONCURRENT
    );
    private static final String CACHE_HIT_MESSAGE = "**CACHE_HIT**";
    private static final String CACHE_SAVE_MESSAGE = "SAVE_TO_CACHE";
    private final PrimeRepository primeRepository;

    private static final List<Long> DUMMY_RESPONSE = List.of(1L, 2L, 3L);

    @Autowired
    public FindPrimesServiceImpl(PrimeRepository primeRepository) {
        this.primeRepository = primeRepository;
    }
    
    public FindPrimesResponse findPrimes(long limit, PrimeAlgorithmNames selectedAlgorithm) {
        throwInputErrors(limit, selectedAlgorithm);

        // Check cache
        PrimesTimerResult<Prime> maxPrimeResult = PrimesTimer.measureExecutionTime(primeRepository::findMaxPrime);
        if (limit <= maxPrimeResult.primes().getValue()) {
            PrimesTimerResult<List<Long>> result = PrimesTimer.measureExecutionTime(() ->
                    primeRepository.findByValueLessThanEqual(limit).stream().map(Prime::getValue).toList()
            );
            logger.info("Execution Time for {}: {} ms", CACHE_HIT_MESSAGE, result.durationMs());
            return new FindPrimesResponse(
                    DUMMY_RESPONSE,
                    result.primes().size(),
                    result.durationMs() + maxPrimeResult.durationMs(),
                    result.durationNs() + maxPrimeResult.durationNs(),
                    CACHE_HIT_MESSAGE
            );
        }

        // Generate primes
        PrimesTimerResult<List<Long>> result = PrimesTimer.measureExecutionTime(getPrimesFn(limit, selectedAlgorithm));
        logger.info("Execution Time for {}: {} ms", selectedAlgorithm.name(), result.durationMs());

        // Save primes
        PrimesTimerResult<Integer> saveToCacheResult = PrimesTimer.measureExecutionTime(() -> batchSavePrimes(result.primes()));
        logger.info("Execution Time for {}: {} ms", CACHE_SAVE_MESSAGE, saveToCacheResult.durationMs());

        return new FindPrimesResponse(
                DUMMY_RESPONSE,
                result.primes().size(),
                result.durationMs() + saveToCacheResult.durationMs(),
                result.durationNs() + saveToCacheResult.durationNs(),
                selectedAlgorithm.name()
        );
    }

    private Supplier<List<Long>> getPrimesFn(long limit, PrimeAlgorithmNames selectedAlgorithm) {
        return switch(selectedAlgorithm) {
            case NAIVE:                         yield () -> findPrimesNaive(limit);
            case SIEVE:                         yield () -> findPrimesWithSieve(limit);
            case SIEVE_BITSET:                  yield () -> findPrimesWithSieve_BitSet(limit);
            case SIEVE_STREAMS:                 yield () -> findPrimesWithSieve_StreamsAPI(limit);
            case SEGMENTED_SIEVE:               yield () -> findPrimesWithSegmentedSieve(limit);
            case SEGMENTED_SIEVE_BITSET:        yield () -> findPrimesWithSegmentedSieve_BitSet(limit);
            case SEGMENTED_SIEVE_STREAMS:       yield () -> findPrimesWithSegmentedSieve_StreamsAPI(limit);
            case SEGMENTED_SIEVE_CONCURRENT:    yield () -> findPrimesWithSegmentedSieve_Concurrent(limit);
        };
    }

    private Integer batchSavePrimes(List<Long> primes) {
        int batchSize = primes.size() > 100_000 ? 10_000 : 1000;
        int upperBound = (primes.size() + batchSize - 1) / batchSize;

        IntStream.range(0, upperBound)
                .mapToObj(i -> {
                    int startIndex = i * batchSize;
                    int endIndex = Math.min((i + 1) * batchSize, primes.size());
                    return primes.subList(startIndex, endIndex);
                }).forEach(batch -> {
                    List<Prime> primeEntities = batch.stream().map(Prime::new).toList();
                    primeRepository.saveAll(primeEntities);
                });
        return primes.size();
    }

    private void throwInputErrors(long limit, PrimeAlgorithmNames selectedAlgorithm) {
        if (limit < 2) {
            logger.warn("[findPrimes]: limit < 2");
            throw new FindPrimesArgException("Limit must be greater than or equal to 2");
        }
        if (limit >= Integer.MAX_VALUE && !VALID_LARGE_LIMIT_ALGORITHMS.contains(selectedAlgorithm)) {
            logger.warn("[findPrimes]: limit < MAX_INT without Seg-Sieve algorithm");
            throw new FindPrimesArgException("Limit is greater than MAX_INT, please use the Segmented-Sieve algorithm");
        }
    }
}

