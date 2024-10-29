package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.exception.FindPrimesArgException;
import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.util.PrimesTimer;
import com.example.msvcprimefinder.util.type.PrimesTimerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

import static com.example.msvcprimefinder.algo.PrimeFinder.*;

@Service
public class FindPrimesServiceImpl implements FindPrimesService {
    private static final Logger logger = LoggerFactory.getLogger(FindPrimesServiceImpl.class);
    private static final EnumSet<PrimeAlgorithmNames> VALID_LARGE_LIMIT_ALGORITHMS = EnumSet.of(
            PrimeAlgorithmNames.SEGMENTED_SIEVE,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_BITSET,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_STREAMS,
            PrimeAlgorithmNames.SEGMENTED_SIEVE_CONCURRENT,
            PrimeAlgorithmNames.SMART
    );
    private static final String CACHE_HIT_MESSAGE = "CACHE_HIT";
    private static final String CACHE_SAVE_MESSAGE = "SAVE_TO_CACHE";
    private static final int SMART_LIMIT_SWITCH = 5_000_000;
    private static final List<Long> EMPTY_PRIMES = List.of();

    private final RedisPrimeCacheService redisPrimeCacheService;
    private final ExecutorServiceProvider executorServiceProvider;

    private long cachedPrimesLimit = 0;

    @Autowired
    public FindPrimesServiceImpl(RedisPrimeCacheService redisPrimeCacheService, ExecutorServiceProvider executorServiceProvider) {
        this.redisPrimeCacheService = redisPrimeCacheService;
        this.executorServiceProvider = executorServiceProvider;
    }

    @Transactional
    public FindPrimesResponse findPrimes(long limit, PrimeAlgorithmNames selectedAlgorithm, boolean useCache, boolean withResult) {
        throwInputErrors(limit, selectedAlgorithm, useCache);
        long saveToCacheDurationMs = 0;
        long saveToCacheDurationNs = 0;

        if (useCache) {
            logger.warn("Cached Primes Limit: {}", cachedPrimesLimit);
            if (limit <= cachedPrimesLimit) {
                return handleCacheHit(limit, withResult);
            }
        }

        // Adjust SMART mode algorithm
        if (selectedAlgorithm == PrimeAlgorithmNames.SMART) {
            selectedAlgorithm = limit <= SMART_LIMIT_SWITCH
                    ? PrimeAlgorithmNames.SIEVE
                    : PrimeAlgorithmNames.SEGMENTED_SIEVE_CONCURRENT;
        }

        // Generate primes
        PrimesTimerResult<List<Long>> result = PrimesTimer.measureExecutionTime(getPrimesFn(limit, selectedAlgorithm));
        logger.info("Execution Time for {}: {} ms", selectedAlgorithm.name(), result.durationMs());

        if (useCache) {
            // Drop cache + save primes
            PrimesTimerResult<Integer> saveToCacheResult = PrimesTimer.measureExecutionTime(() -> redisPrimeCacheService.savePrimes(result.primes()));
            saveToCacheDurationMs = saveToCacheResult.durationMs();
            saveToCacheDurationNs = saveToCacheResult.durationNs();
            cachedPrimesLimit = limit;
            logger.info("Execution Time for {}: {} ms", CACHE_SAVE_MESSAGE, saveToCacheResult.durationMs());
        }

        return new FindPrimesResponse(
                withResult ? result.primes() : EMPTY_PRIMES,
                result.primes().size(),
                result.durationMs() + saveToCacheDurationMs,
                result.durationNs() + saveToCacheDurationNs,
                selectedAlgorithm.name(),
                useCache
        );
    }

    private FindPrimesResponse handleCacheHit(long limit, boolean withResult) {
        PrimesTimerResult<List<Long>> result = PrimesTimer.measureExecutionTime(() -> redisPrimeCacheService.getPrimesUpTo(limit));
        logger.info("Execution Time for {}: {} ms", CACHE_HIT_MESSAGE, result.durationMs());
        return new FindPrimesResponse(
                withResult ? result.primes() : EMPTY_PRIMES,
                result.primes().size(),
                result.durationMs(),
                result.durationNs(),
                CACHE_HIT_MESSAGE,
                true
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
            case SEGMENTED_SIEVE_CONCURRENT:    yield handleConcurrentSieve(limit);
            case SMART:                         throw new FindPrimesArgException("Failed to choose algorithm in SMART mode");
        };
    }

    private Supplier<List<Long>> handleConcurrentSieve(long limit) {
        ExecutorService executor = executorServiceProvider.getExecutor();
        return () -> findPrimesWithSegmentedSieve_Concurrent(limit, executorServiceProvider.getDynamicSegmentSize(limit), executor);
    }

    private void throwInputErrors(long limit, PrimeAlgorithmNames selectedAlgorithm, boolean useCache) {
        if (limit >= Integer.MAX_VALUE && !VALID_LARGE_LIMIT_ALGORITHMS.contains(selectedAlgorithm)) {
            logger.warn("[findPrimes]: limit > MAX_INT without Seg-Sieve algorithm");
            throw new FindPrimesArgException("Limit is greater than MAX_INT, please use a Segmented-Sieve algorithm variant");
        }
        if (useCache && limit > 100_000_000) {
            logger.warn("[findPrimes]: limit > 100_000_000 with buildCache enabled");
            throw new FindPrimesArgException("Limit too large for caching! Please disable caching (&useCache=false) or use a smaller limit");
        }
    }
}

