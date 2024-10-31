package com.example.msvcprimefinder.algo;

import com.example.msvcprimefinder.exception.ConcurrentSieveException;
import com.example.msvcprimefinder.util.PrimeEstimator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class PrimeFinder {
    private static final Logger logger = LoggerFactory.getLogger(PrimeFinder.class);

    public static long[] findPrimesNaive(long limit) {
        long[] primes = new long[PrimeEstimator.estimatePrimesArrayLength(limit)];
        int count = 0;

        // Check each number from 2 up to limit
        for (long i = 2; i <= limit; i++) {
            if (isPrimeNaive(i)) {
                primes[count++] = i;  // If prime, add to list
            }
        }
        return Arrays.copyOf(primes, count);
    }

    public static long[] findPrimesWithSieve(long limit) {
        int intLimit = (int) limit; // if limit > max_int exception has already been thrown
        boolean[] isPrime = simpleIntSieve(intLimit);
        long[] primes = new long[PrimeEstimator.estimatePrimesArrayLength(limit) + 1];
        int count = 0;
        for (int i = 2; i <= limit; i++) {
            if (isPrime[i]){
                primes[count++] = i;
            }
        }
        return Arrays.copyOf(primes, count);
    }

    public static long[] findPrimesWithSieve_BitSet(long limit) {
        int intLimit = (int)limit; // if limit > max_int exception has already been thrown
        BitSet isPrime = bitSetIntSieve(intLimit);
        long[] primes = new long[PrimeEstimator.estimatePrimesArrayLength(limit)];
        int count = 0;
        for (int i = 2; i <= limit; i++) {
            if (isPrime.get(i)) {
                primes[count++] = i;
            }
        }
        return Arrays.copyOf(primes, count);
    }

    // Just for fun - Arguably less readable than for loops in this case
    public static long[] findPrimesWithSieve_StreamsAPI(long limit) {
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

        long count = IntStream.rangeClosed(2, intLimit).filter(i -> isPrime[i]).count();

        long[] result = new long[(int) count];
        int resultCount = 0;

        for(int i = 2; i <= intLimit; i++) {
            if (isPrime[i]) {
                result[resultCount++] = i;
            }
        }

        return result;
    }

    public static long[] findPrimesWithSegmentedSieve(long limit) {
        long segmentSize = (long) Math.sqrt(limit) + 1;

        // Create the boolean array for result up to sqrt(limit)
        boolean[] isPrime = simpleIntSieve((int) segmentSize);
        long[] smallPrimes = new long[(int) segmentSize];
        int smallPrimesCount = 0;

        // Collect result from the boolean array
        for (int i = 2; i < segmentSize; i++) {
            if (isPrime[i]) {
                smallPrimes[smallPrimesCount++] = i;
            }
        }

        // List to hold all result up to the limit
        long[] resultPrimes = new long[PrimeEstimator.estimatePrimesArrayLength(limit)];
        int resultCount = 0;

        long low = 2;
        long high;

        // Process each segment and mark non-result
        while (low <= limit) {
            // Adjust the high for final segment as to not exceed array size
            high = Math.min(low + segmentSize - 1, limit);

            // Mark all numbers in the current segment as prime
            boolean[] mark = new boolean[(int) (high - low + 1)];
            Arrays.fill(mark, true);

            // Use the result from the simple sieve to mark multiples in the current segment
            for (int i = 0; i < smallPrimesCount; i++) {
                long prime = smallPrimes[i];
                long start = Math.max(prime * prime, (low + prime - 1) / prime * prime);
                for (long j = start; j <= high; j += prime) {
                    mark[(int) (j - low)] = false;
                }
            }

            // Collect all result from the current segment
            for (int i = 0; i < mark.length; i++) {
                if (mark[i]) {
                    resultPrimes[resultCount++] = low + i;
                }
            }

            // Slide up by segmentSize to next segment
            low += segmentSize;
        }

        return Arrays.copyOf(resultPrimes, resultCount);
    }

    public static long[] findPrimesWithSegmentedSieve_BitSet(long limit) {
        long segmentSize = (long) Math.sqrt(limit) + 1;

        // Create the boolean array for result up to sqrt(limit)
        BitSet isPrime = bitSetIntSieve((int) segmentSize);
        long[] smallPrimes = new long[(int) segmentSize];
        int smallPrimesCount = 0;

        // Collect result from the boolean array
        for (int i = 2; i < segmentSize; i++) {
            if (isPrime.get(i)) {
                smallPrimes[smallPrimesCount++] =  i;
            }
        }

        // List to hold all result up to the limit
        long[] resultPrimes = new long[PrimeEstimator.estimatePrimesArrayLength(limit)];
        int resultCount = 0;

        long low = 2;
        long high;

        // Process each segment and mark non-result
        while (low <= limit) {
            // Adjust the high for final segment as to not exceed array size
            high = Math.min(low + segmentSize - 1, limit);

            // Mark all numbers in the current segment as prime
            BitSet mark = new BitSet((int) (high - low + 1));
            mark.set(0, (int) (high - low + 1));

            // Use the result from the simple sieve to mark multiples in the current segment

            for (int i = 0; i < smallPrimesCount; i++) {
                long prime = smallPrimes[i];
                long start = Math.max(prime * prime, (low + prime - 1) / prime * prime);
                for (long j = start; j <= high; j += prime) {
                    mark.clear((int) (j - low));
                }
            }

            // Collect all result from the current segment
            for (int i = 0; i < mark.size(); i++) {
                if (mark.get(i)) {
                    resultPrimes[resultCount++] = low + i;
                }
            }

            // Slide low up bv segmentSize to next segment
            low += segmentSize;
        }

        return Arrays.copyOf(resultPrimes, resultCount);
    }

    public static long[] findPrimesWithSegmentedSieve_StreamsAPI(long limit) {
        long segmentSize = (long) Math.sqrt(limit) + 1;

        // Generate all result up to sqrt(limit) using the simple int sieve
        boolean[] isPrime = simpleIntSieve((int) segmentSize);
        long[] smallPrimes = IntStream.range(2, isPrime.length - 1)
                .filter(i -> isPrime[i])
                .mapToLong(i -> i)
                .toArray();

        // Use the small result to mark non-result in segments up to limit
        long[] resultPrimes = new long[PrimeEstimator.estimatePrimesArrayLength(limit)];
        AtomicInteger resultCount = new AtomicInteger( 0);

        // Iterate over segments, starting from segmentSize
        LongStream.iterate(2, low -> low <= limit, low -> low + segmentSize)
                .forEach(low -> {
                    // Adjust the high for the last segment
                    long high = Math.min(low + segmentSize - 1, limit);

                    // Mark all numbers in this segment as prime to begin with
                    boolean[] mark = new boolean[(int) (high - low + 1)];
                    Arrays.fill(mark, true);

                    // Use result from the smaller range to mark non-result
                    for (long prime : smallPrimes) {
                        long start = Math.max(prime * prime, (low + prime - 1) / prime * prime);
                        LongStream.iterate(start, j -> j <= high, j -> j + prime)
                                .forEach(j -> mark[(int) (j - low)] = false);
                    }

                    for (int i = 0; i < mark.length; i++) {
                        if (mark[i]) {
                            int index = resultCount.getAndIncrement();
                            resultPrimes[index] = low + i;
                        }
                    }
                });

        return Arrays.copyOf(resultPrimes, resultCount.get());
    }

    public static long[] findPrimesWithSegmentedSieve_Concurrent(long limit, long segmentSize, ExecutorService executor) {
        // Create the boolean array for result up to sqrt(limit)
        boolean[] isPrime = simpleIntSieve((int) segmentSize);
        long[] smallPrimes = new long[isPrime.length];
        AtomicInteger smallPrimesCount = new AtomicInteger(0);

        // Collect result from the boolean array
        for (int i = 2; i <= segmentSize; i++) {
            if (isPrime[i]) {
                smallPrimes[smallPrimesCount.getAndIncrement()] =  i;
            }
        }

        // List to hold all result up to the limit
        long[] resultPrimes = new long[PrimeEstimator.estimatePrimesArrayLength(limit)];
        AtomicInteger resultCount = new AtomicInteger(0);

        // Get processors and make a thread pool (try w resources)
        logger.info("[Concurrent Sieve] Available processors: " + Runtime.getRuntime().availableProcessors());

        long low = 2;
        long high;

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Process each segment and mark non-result
        while (low <= limit) {
            // Adjust the high for final segment as to not exceed array size
            high = Math.min(low + segmentSize - 1, limit);

            // Mark all numbers in the current segment as prime
            boolean[] mark = new boolean[(int) (high - low + 1)];
            Arrays.fill(mark, true);

            // Immutability for thread safe concurrency
            final long segmentLow = low;
            final long segmentHigh = high;

            // Build threads and add them to futures
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                // Use the result from the simple sieve to mark multiples in the current segment
                for (int i = 0; i < smallPrimesCount.get(); i++) {
                    long prime = smallPrimes[i];
                    long start = Math.max(prime * prime, (segmentLow + prime - 1) / prime * prime);
                    for (long j = start; j <= segmentHigh; j += prime) {
                        mark[(int) (j - segmentLow)] = false;
                    }
                }

                // Collect all result from the current segment
                synchronized (resultPrimes) {
                    for (int i = 0; i < mark.length; i++) {
                        if (mark[i]) {
                            resultPrimes[resultCount.getAndIncrement()] = segmentLow + i;
                        }
                    }
                }
            }, executor).exceptionally(ex -> {
                logger.error("[Concurrent Segmented Sieve]: Error in segment [{}, {}]", segmentLow, segmentHigh);
                return null;
            });

            futures.add(future);
            // Slide up bv segmentSize to next segment
            low += segmentSize;
        }

        logger.info("[Concurrent Sieve] Configured active threads: " + Thread.activeCount());

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        try {
            allFutures.join();
        } catch (Exception e) {
            throw new ConcurrentSieveException(e.getMessage(), e.getCause());
        }

        return Arrays.copyOf(resultPrimes, resultCount.get());
    }

    private static boolean isPrimeNaive(long num) {
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
        isPrime[0] = isPrime[1] = false;
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

    // With a BitSet using 1/8th of the memory (bit = 1bit, boolean = 1byte = 8bits)
    private static BitSet bitSetIntSieve(int limit) {
        BitSet isPrime = new BitSet(limit + 1);
        isPrime.set(2, limit + 1); // flip all bits
        for (int i = 2; i * i <= limit; i++) {
            if (isPrime.get(i)) {
                for (int multiple = i * i; multiple <= limit; multiple += i) {
                    // clear multiples of isPrime[i]
                    isPrime.clear(multiple);
                }
            }
        }
        return isPrime;
    }
}
