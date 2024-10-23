package com.example.msvcprimefinder.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PrimeFinder {
    /**
     * Naive method to find all prime numbers up to the given limit.
     *
     * @param limit The upper limit up to which prime numbers are found.
     * @return List of prime numbers up to and including the limit.
     */
    public static List<Long> findPrimesNaive(long limit) {
        List<Long> primes = new ArrayList<>();

        // Naive method: check each number from 2 up to limit
        for (long num = 2; num <= limit; num++) {
            if (isPrimeNaive(num)) {
                primes.add(num);  // If prime, add to list
            }
        }
        return primes;
    }

    /**
     * Sieve of Eratosthenes method to find all prime numbers up to the given limit.
     *
     * @param limit The upper limit up to which prime numbers are found.
     * @return List of prime numbers up to and including the limit.
     */
    public static List<Long> findPrimesWithSieve(long limit) {
        int intLimit = (int)limit; // if limit > max_int exception has already been thrown
        boolean[] isPrime = new boolean[intLimit + 1];
        for (int i = 2; i <= limit; i++) {
            isPrime[i] = true;
        }

        for (int i = 2; (long) i * i <= limit; i++) {
            if (isPrime[i]) {
                // Setting all multiples of isPrime[i] to false
                for (int multiple = i * i; multiple <= limit; multiple += i) {
                    isPrime[multiple] = false;
                }
            }
        }

        List<Long> primes = new ArrayList<>();
        for (int i = 2; i <= limit; i++) {
            if (isPrime[i]) primes.add((long) i);
        }

        return primes;
    }

    // Just for fun - Arguably less readable than for loops in this case
    public static List<Long> findPrimesWithSieve_StreamsAPI(long limit) {
        int intLimit = (int)limit; // if limit > max_int exception has already been thrown
        boolean[] isPrime = new boolean[intLimit + 1];
        IntStream.rangeClosed(2, intLimit).forEach(i -> isPrime[i] = true);

        IntStream.rangeClosed(2, (int) Math.sqrt(intLimit))
                .filter(i -> isPrime[i])
                .forEach(i -> {
                    // Setting all multiples of isPrime[i] to false
                    for (int j = i * i; j <= limit; j += i) {
                        isPrime[j] = false;
                    }
                });

        return IntStream.rangeClosed(2, intLimit)
                .filter(i -> isPrime[i])
                .mapToObj(Long::valueOf)
                .toList();
    }

    /**
     * Segmented Sieve method to find all prime numbers up to the given limit.
     *
     * @param limit The upper limit up to which prime numbers are found.
     * @return List of prime numbers up to and including the limit.
     */
    public static List<Long> findPrimesWithSegmentedSieve(long limit) {
        // Implementation for segmented sieve
        return Arrays.asList(2L, 3L, 5L, 7L, 11L); // Example primes
    }

    /**
     * Helper function to check if a number is prime (Naive approach).
     *
     * @param num The number to check.
     * @return True if prime, false otherwise.
     */
    private static boolean isPrimeNaive(long num) {
        if (num < 2) return false;
        // Only check up to sqrt(num) for 'efficiency' (if such a thing exists for this impl)
        for (long i = 2; i * i <= num; i++) {
            if (num % i == 0) {
                return false;
            }
        }
        return true;
    }
}
