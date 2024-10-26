package com.example.msvcprimefinder.service;

import com.example.msvcprimefinder.exception.FindPrimesArgException;
import com.example.msvcprimefinder.model.entity.Prime;
import com.example.msvcprimefinder.model.enums.PrimeAlgorithmNames;
import com.example.msvcprimefinder.repository.PrimeRepository;
import com.example.msvcprimefinder.response.FindPrimesResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FindPrimesServiceImplTest {

    @InjectMocks
    private FindPrimesServiceImpl findPrimesService;

    @Mock
    PrimeRepository primeRepository;


    private void mockFindByValueLessThanEqual(long limit, List<Long> primes) {
        when(primeRepository.findByValueLessThanEqual(limit)).thenReturn(primes);
    }

    private final long SMART_MAX_SWITCH = 5_000_000;
    private final List<Long> mockPrimes = List.of(2L, 3L, 5L, 7L,11L, 13L, 17L, 19L, 23L, 29L, 31L, 37L, 41L,
            43L, 47L, 53L, 59L, 61L, 67L, 71L, 73L, 79L, 83L, 89L, 97L);

    @Test
    public void testFindPrimes_NoCache() {
        long limit = 100;
        FindPrimesResponse response = findPrimesService.findPrimes(limit, PrimeAlgorithmNames.SIEVE, false, false);
        assertEquals(mockPrimes.size(), response.numberOfPrimes());
        assertEquals(PrimeAlgorithmNames.SIEVE.name(), response.algorithmName());
    }

    @Test
    public void testFindPrimes_CacheHit() {
        long limit = 100;
        mockFindByValueLessThanEqual(limit, mockPrimes);
        FindPrimesResponse loadCacheResponse = findPrimesService.findPrimes(limit, PrimeAlgorithmNames.SIEVE, true, true);
        FindPrimesResponse response = findPrimesService.findPrimes(limit, PrimeAlgorithmNames.SIEVE, true, false);
        assertEquals(mockPrimes.size(), response.numberOfPrimes());
        assertEquals("CACHE_HIT", response.algorithmName());
    }

    @Test
    public void testFindPrimes_InvalidLimit() {
        long limit = 1;
        Exception exception = assertThrows(FindPrimesArgException.class, () -> {
            findPrimesService.findPrimes(limit, PrimeAlgorithmNames.SIEVE, false, false);
        });
        assertEquals("Limit must be greater than or equal to 2", exception.getMessage());
    }

    @Test
    public void testFindPrimes_SmartAlgorithmSelection_LimitGt() {
        long limit = SMART_MAX_SWITCH + 1;
        FindPrimesResponse response = findPrimesService.findPrimes(limit, PrimeAlgorithmNames.SMART, false, false);
        assertEquals(PrimeAlgorithmNames.SEGMENTED_SIEVE_CONCURRENT.name(), response.algorithmName());
    }

    @Test
    public void testFindPrimes_SmartAlgorithmSelection_LimitEq() {
        long limit = SMART_MAX_SWITCH;
        FindPrimesResponse response = findPrimesService.findPrimes(limit, PrimeAlgorithmNames.SMART, false, false);
        assertEquals(PrimeAlgorithmNames.SIEVE.name(), response.algorithmName());
    }

    @Test
    public void testFindPrimes_SmartAlgorithmSelection_LimitLt() {
        long limit = SMART_MAX_SWITCH - 1;
        FindPrimesResponse response = findPrimesService.findPrimes(limit, PrimeAlgorithmNames.SMART, false, false);
        assertEquals(PrimeAlgorithmNames.SIEVE.name(), response.algorithmName());
    }

    @Test
    public void testFindPrimes_WithAllAlgorithms_SmallLimit() {
        long limit = 100;
        Arrays.stream(PrimeAlgorithmNames.values()).forEach(algorithm -> {
            FindPrimesResponse response = findPrimesService.findPrimes(limit, algorithm, false, false);
            PrimeAlgorithmNames expectedAlgorithm = algorithm == PrimeAlgorithmNames.SMART ? PrimeAlgorithmNames.SIEVE : algorithm;
            assertEquals(expectedAlgorithm.name(), response.algorithmName() , "Expected algorithm: " + expectedAlgorithm);
            assertEquals(mockPrimes.size(), response.numberOfPrimes(), "Count of primes should match for " + algorithm.name());
            assertEquals(mockPrimes, response.result().stream().sorted().toList(), "Primes returned should match for " + algorithm.name());
            assertFalse(response.buildCache(), "buildCache should be false for " + algorithm.name());
            assertFalse(response.useCache(), "useCache should be false for " + algorithm.name());
        });
    }

    @Test
    public void testFindPrimes_WithAllAlgorithms_BigLimit() {
        long limit = 1_000_000;
        int primesInAMillion = 78_498;
        Arrays.stream(PrimeAlgorithmNames.values()).forEach(algorithm -> {
            FindPrimesResponse response = findPrimesService.findPrimes(limit, algorithm, false, false);
            PrimeAlgorithmNames expectedAlgorithm = algorithm == PrimeAlgorithmNames.SMART ? PrimeAlgorithmNames.SIEVE : algorithm;
            assertEquals(expectedAlgorithm.name(), response.algorithmName() , "Expected algorithm: " + expectedAlgorithm);
            assertEquals(primesInAMillion, response.numberOfPrimes(), "Count of primes should match for " + algorithm.name());
            assertFalse(response.buildCache(), "buildCache should be false for " + algorithm.name());
            assertFalse(response.useCache(), "useCache should be false for " + algorithm.name());
        });
    }
}