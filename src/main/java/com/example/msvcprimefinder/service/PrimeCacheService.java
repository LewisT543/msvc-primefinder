package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.util.PrimeEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class PrimeCacheService {
    private static final Logger logger = LoggerFactory.getLogger(PrimeCacheService.class);
    private static final double MAX_CACHE_PERCENTAGE = 0.2;
    private long cachedPrimesLimit = 0;
    private long[] cachedPrimes;

    public long getMaxSafeCacheSize() {
        long maxHeapSize = Runtime.getRuntime().maxMemory();
        long maxSafeCacheSize = (long) (maxHeapSize * MAX_CACHE_PERCENTAGE);
        logger.warn("MAX SAFE CACHE SIZE: " + maxSafeCacheSize / (1024 * 1024) + "MB");
        return maxSafeCacheSize;
    }

    public Boolean addPrimesToCache(long[] primes) {
        if (primes.length * 8L > getMaxSafeCacheSize()) {
            return false;
        }
        cachedPrimes = new long[primes.length];
        System.arraycopy(primes, 0, cachedPrimes, 0, primes.length);
        return true;
    }

    public long[] getPrimesFromCacheToLimit(long limit) {
        long[] primesToLimit = new long[PrimeEstimator.estimatePrimesArrayLength(limit)];
        int count = 0;
        for (long prime : cachedPrimes) {
            if (prime <= limit) {
                primesToLimit[count++] = prime;
            }
            else break;
        }
        return Arrays.copyOf(primesToLimit, count);
    }

    public boolean isCached(long limit) {
        return cachedPrimesLimit >= limit;
    }

    public long getCachedLimit() {
        return cachedPrimesLimit;
    }

    public void setCachedLimit(long limit) {
        cachedPrimesLimit = limit;
    }
}
