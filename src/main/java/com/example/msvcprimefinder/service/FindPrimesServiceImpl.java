package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.exception.FindPrimesArgException;
import com.example.msvcprimefinder.model.entity.Prime;
import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.repository.PrimeRepository;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import com.example.msvcprimefinder.util.PrimesTimer;
import com.example.msvcprimefinder.util.type.PrimesTimerResult;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private static final String CACHE_HIT_MESSAGE = "CACHE_HIT";
    private static final String CACHE_SAVE_MESSAGE = "SAVE_TO_CACHE";
    private final PrimeRepository primeRepository;

    private long cachedPrimesLimit = 0;

    private static final List<Long> DUMMY_RESPONSE = List.of(1L, 2L, 3L);

    @Autowired
    public FindPrimesServiceImpl(PrimeRepository primeRepository) {
        this.primeRepository = primeRepository;
    }

    @Transactional
    public FindPrimesResponse findPrimes(long limit, PrimeAlgorithmNames selectedAlgorithm, boolean useCache, boolean buildCache) {
        throwInputErrors(limit, selectedAlgorithm, buildCache);
        long saveToCacheDurationMs = 0;
        long saveToCacheDurationNs = 0;

        if (useCache) {
            // Check against cached limit
            logger.warn("Cached Primes Limit: {}", cachedPrimesLimit);
            if (limit <= cachedPrimesLimit) {
                return handleCacheHit(limit, buildCache);
            }
        }

        // Generate primes
        PrimesTimerResult<List<Long>> result = PrimesTimer.measureExecutionTime(getPrimesFn(limit, selectedAlgorithm));
        logger.info("Execution Time for {}: {} ms", selectedAlgorithm.name(), result.durationMs());

        if (buildCache) {
            // Drop table + save primes
            primeRepository.dropTable();
            PrimesTimerResult<Integer> saveToCacheResult = PrimesTimer.measureExecutionTime(() -> batchSavePrimes(result.primes()));
            saveToCacheDurationMs = saveToCacheResult.durationMs();
            saveToCacheDurationNs = saveToCacheResult.durationNs();
            cachedPrimesLimit = limit;
            logger.info("Execution Time for {}: {} ms", CACHE_SAVE_MESSAGE, saveToCacheResult.durationMs());
        }

        return new FindPrimesResponse(
                DUMMY_RESPONSE,
                result.primes().size(),
                result.durationMs() + saveToCacheDurationMs,
                result.durationNs() + saveToCacheDurationNs,
                selectedAlgorithm.name(),
                buildCache,
                useCache
        );
    }

    private FindPrimesResponse handleCacheHit(long limit, boolean buildCache) {
        PrimesTimerResult<List<Long>> result = PrimesTimer.measureExecutionTime(() -> primeRepository.findByValueLessThanEqual(limit));
        logger.info("Execution Time for {}: {} ms", CACHE_HIT_MESSAGE, result.durationMs());
        return new FindPrimesResponse(
                DUMMY_RESPONSE,
                result.primes().size(),
                result.durationMs(),
                result.durationNs(),
                CACHE_HIT_MESSAGE,
                buildCache,
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
            case SEGMENTED_SIEVE_CONCURRENT:    yield () -> findPrimesWithSegmentedSieve_Concurrent(limit);
        };
    }

    @Transactional
    public Integer batchSavePrimes(List<Long> primes) {
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

    private void throwInputErrors(long limit, PrimeAlgorithmNames selectedAlgorithm, boolean buildCache) {
        if (limit < 2) {
            logger.warn("[findPrimes]: limit < 2");
            throw new FindPrimesArgException("Limit must be greater than or equal to 2");
        }
        if (limit >= Integer.MAX_VALUE && !VALID_LARGE_LIMIT_ALGORITHMS.contains(selectedAlgorithm)) {
            logger.warn("[findPrimes]: limit > MAX_INT without Seg-Sieve algorithm");
            throw new FindPrimesArgException("Limit is greater than MAX_INT, please use a Segmented-Sieve algorithm variant");
        }
        if (buildCache && limit > 10_000_000) {
            logger.warn("[findPrimes]: limit > 10_000_000 with buildCache enabled");
            throw new FindPrimesArgException("Limit too large for caching! Please disable caching (&buildCache=false) or use a smaller limit");
        }
    }
}

