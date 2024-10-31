package com.example.msvcprimefinder.util;

public class PrimeEstimator {
    /**
     * uses Prime Number Theorem to estimate the rough size of an array required to hold all
     * our primes.
     * A safety buffer is also applied to ensure we always overestimate the size required.
     * For small limits PNT is inaccurate and tends to underestimate, so we add an extra 2000
     * length for these cases to ensure we don't go out of bounds when calculating primes.
     * */
    public static int estimateNumberOfPrimes(long limit) {
        double safeBufferAdjuster = 1.2;
        double estimatedArrLength = (int) (limit / Math.log(limit)) * safeBufferAdjuster;
        return limit < 10000 ? (int) estimatedArrLength + 2000 : (int) estimatedArrLength;
    }
}
