package com.example.msvcprimefinder.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class PrimeFinder {
    /**
     * Naive method to find all prime numbers up to the given limit.
     *
     * @param limit The upper limit up to which prime numbers are found.
     * @return List of prime numbers up to and including the limit.
     */
    public static List<Long> findPrimesNaive(long limit) {
        List<Long> primes = new ArrayList<>();

        // Check each number from 2 up to limit
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
        boolean[] isPrime = simpleIntSieve(intLimit);
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
        long segmentSize = (long) Math.sqrt(limit) + 1;

        // Create the boolean array for primes up to sqrt(limit)
        boolean[] isPrime = simpleIntSieve((int) segmentSize);
        List<Long> primes = new ArrayList<>();

        // Collect primes from the boolean array
        for (int i = 2; i <= segmentSize; i++) {
            if (isPrime[i]) primes.add((long) i);
        }

        // List to hold all primes up to the limit
        List<Long> resultPrimes = new ArrayList<>(primes);

        long low = segmentSize;
        long high = 2 * segmentSize;

        // Process each segment and mark non-primes
        while (low <= limit) {
            // Adjust the high for final segment as to not exceed array size
            if (high > limit) high = limit;

            // Mark all numbers in the current segment as prime
            boolean[] mark = new boolean[(int) (high - low + 1)];
            Arrays.fill(mark, true);

            // Use the primes from the simple sieve to mark multiples in the current segment
            for (long prime : primes) {
                long start = Math.max(prime * prime, (low + prime - 1) / prime * prime);

                for (long j = start; j <= high; j += prime) {
                    mark[(int) (j - low)] = false;
                }
            }

            // Collect all primes from the current segment
            for (int i = 0; i < mark.length; i++) {
                if (mark[i]) {
                    resultPrimes.add(low + i);
                }
            }

            // Slide up bv segmentSize to next segment
            low += segmentSize;
            high += segmentSize;
        }

        return resultPrimes;
    }

    public static List<Long> findPrimesWithSegmentedSieve_StreamsAPI(long limit) {
        long segmentSize = (long) Math.sqrt(limit) + 1;

        // Generate all primes up to sqrt(limit) using the simple int sieve
        boolean[] isPrime = simpleIntSieve((int) segmentSize);
        List<Long> primes = IntStream.range(2, isPrime.length)
                .filter(i -> isPrime[i])
                .mapToObj(i -> (long) i)
                .toList();

        // Use the small primes to mark non-primes in segments up to limit
        List<Long> resultPrimes = new ArrayList<>(primes);

        // Iterate over segments, starting from segmentSize
        LongStream.iterate(segmentSize, low -> low <= limit, low -> low + segmentSize)
                .forEach(low -> {
                    // Adjust the high for the last segment
                    long high = Math.min(low + segmentSize - 1, limit);

                    // Mark all numbers in this segment as prime to begin with
                    boolean[] mark = new boolean[(int) (high - low + 1)];
                    Arrays.fill(mark, true);

                    // Use primes from the smaller range to mark non-primes
                    primes.forEach(prime -> {
                        long start = Math.max(prime * prime, (low + prime - 1) / prime * prime);
                        LongStream.iterate(start, j -> j <= high, j -> j + prime)
                                .forEach(j -> mark[(int) (j - low)] = false);
                    });

                    // Collect primes from this segment and add them to result
                    IntStream.range(0, mark.length)
                            .filter(i -> mark[i])
                            .mapToLong(i -> low + i)
                            .forEach(resultPrimes::add);
                });

        return resultPrimes;
    }

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

    private static boolean[] simpleIntSieve(int limit) {
        boolean[] isPrime = new boolean[limit + 1];
        Arrays.fill(isPrime, true);
        for (int i = 2; (long) i * i <= limit; i++) {
            if (isPrime[i]) {
                // Setting all multiples of isPrime[i] to false
                for (int multiple = i * i; multiple <= limit; multiple += i) {
                    isPrime[multiple] = false;
                }
            }
        }
        return isPrime;
    }
}
