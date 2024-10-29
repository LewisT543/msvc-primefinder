package com.example.msvcprimefinder.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RedisPrimeCacheService {
    private final RedisTemplate<String, Long> redisTemplate;
    private static final String CACHE_KEY = "primes";

    @Autowired
    public RedisPrimeCacheService(RedisTemplate<String, Long> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Integer savePrimes(List<Long> primes) {
        ZSetOperations<String, Long> zSetOps = redisTemplate.opsForZSet();
        zSetOps.removeRange(CACHE_KEY, 0, -1);
        Set<ZSetOperations.TypedTuple<Long>> tuples = primes.stream()
                .map(p -> new DefaultTypedTuple<>(p, (double) p))
                .collect(Collectors.toSet()
        );
        zSetOps.add(CACHE_KEY, tuples);
        return primes.size();
    }

    public List<Long> getPrimesUpTo(long limit) {
        ZSetOperations<String, Long> zSetOps = redisTemplate.opsForZSet();
        Set<Long> primes = zSetOps.rangeByScore(CACHE_KEY, 0, limit);
        return primes == null ? List.of() : new ArrayList<>(primes);
    }
}
