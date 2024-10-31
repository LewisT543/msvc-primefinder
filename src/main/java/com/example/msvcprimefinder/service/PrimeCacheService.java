package com.example.msvcprimefinder.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public Boolean addPrimesToCache(List<Long> primes) {
        if (primes.size() * 8L > getMaxSafeCacheSize()) {
            return false;
        }
        cachedPrimes = new long[primes.size()];
        for (int i = 0; i < primes.size(); i++) {
            cachedPrimes[i] = primes.get(i);
        }
        return true;
    }

    public List<Long> getPrimesFromCacheToLimit(long limit) {
        List<Long> primesToLimit = new ArrayList<>();
        for (long prime : cachedPrimes) {
            if (prime <= limit) primesToLimit.add(prime);
            else break;
        }
        return primesToLimit;
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
