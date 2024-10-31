package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.exception.FindPrimesArgException;
import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.util.PrimeEstimator;
import com.example.msvcprimefinder.util.PrimesTimer;
import com.example.msvcprimefinder.util.type.PrimesTimerResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
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
    private static final long[] EMPTY_PRIMES = new long[0];

    private final ExecutorServiceProvider executorServiceProvider;
    private final PrimeCacheService primeCacheService;

    @Autowired
    public FindPrimesServiceImpl(ExecutorServiceProvider executorServiceProvider, PrimeCacheService primeCacheService) {
        this.executorServiceProvider = executorServiceProvider;
        this.primeCacheService = primeCacheService;
    }

    public FindPrimesResponse findPrimes(long limit, PrimeAlgorithmNames selectedAlgorithm, boolean useCache, boolean withResult) {
        throwInputErrors(limit, selectedAlgorithm);
        long saveToCacheDurationMs = 0;
        long saveToCacheDurationNs = 0;

        if (useCache) {
            logger.warn("Cached primes max limit: {}", primeCacheService.getCachedLimit());
            if (primeCacheService.isCached(limit)) {
                return handleCacheHit(limit, withResult);
            }
        }

        // Adjust SMART mode algorithm
        if (selectedAlgorithm == PrimeAlgorithmNames.SMART) {
            selectedAlgorithm = limit <= SMART_LIMIT_SWITCH
                    ? PrimeAlgorithmNames.SIEVE
                    : PrimeAlgorithmNames.SEGMENTED_SIEVE_CONCURRENT;
        }

        // Generate result
        PrimesTimerResult<long[]> timerResult = PrimesTimer.measureExecutionTime(getPrimesFn(limit, selectedAlgorithm));
        logExecutionTime(selectedAlgorithm.name(), timerResult.durationMs());

        if (useCache) {
            // Drop cache + save result
            PrimesTimerResult<Boolean> saveToCacheResult = PrimesTimer.measureExecutionTime(() -> primeCacheService.addPrimesToCache(timerResult.result()));
            if (saveToCacheResult.result()) {
                primeCacheService.setCachedLimit(limit);
            } else {
                logger.warn("Skipped caching - result size: {} (bytes), too large for cache max size: {} (bytes)", timerResult.result().length * 8L, primeCacheService.getMaxSafeCacheSize());
            }
            saveToCacheDurationMs = saveToCacheResult.durationMs();
            saveToCacheDurationNs = saveToCacheResult.durationNs();
            logExecutionTime(CACHE_SAVE_MESSAGE, saveToCacheResult.durationMs());
        }

        return new FindPrimesResponse(
                withResult ? timerResult.result() : EMPTY_PRIMES,
                timerResult.result().length,
                timerResult.durationMs() + saveToCacheDurationMs,
                timerResult.durationNs() + saveToCacheDurationNs,
                selectedAlgorithm.name(),
                useCache
        );
    }

    private FindPrimesResponse handleCacheHit(long limit, boolean withResult) {
        PrimesTimerResult<long[]> result = PrimesTimer.measureExecutionTime(() -> primeCacheService.getPrimesFromCacheToLimit(limit));
        logExecutionTime(CACHE_HIT_MESSAGE, result.durationMs());
        return new FindPrimesResponse(
                withResult ? result.result() : EMPTY_PRIMES,
                result.result().length,
                result.durationMs(),
                result.durationNs(),
                CACHE_HIT_MESSAGE,
                true
        );
    }

    private Supplier<long[]> getPrimesFn(long limit, PrimeAlgorithmNames selectedAlgorithm) {
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

    private void logExecutionTime(String algorithmName, long timeInMs) {
        logger.info("Execution Time for {}: {} ms", algorithmName, timeInMs);
    }

    private Supplier<long[]> handleConcurrentSieve(long limit) {
        ExecutorService executor = executorServiceProvider.getExecutor();
        return () -> findPrimesWithSegmentedSieve_Concurrent(limit, executorServiceProvider.getDynamicSegmentSize(limit), executor);
    }

    private void throwInputErrors(long limit, PrimeAlgorithmNames selectedAlgorithm) {
        if (limit >= Integer.MAX_VALUE && !VALID_LARGE_LIMIT_ALGORITHMS.contains(selectedAlgorithm)) {
            logger.warn("[findPrimes]: limit > MAX_INT without Seg-Sieve algorithm");
            throw new FindPrimesArgException("Limit is greater than MAX_INT, please use a Segmented-Sieve algorithm variant");
        }
        if (PrimeEstimator.checkLimitAgainstMemory(limit)) {
            logger.warn("Not enough memory to process limit: " + limit);
            throw new FindPrimesArgException("Not enough memory to process limit: " + limit);
        }
    }
}

