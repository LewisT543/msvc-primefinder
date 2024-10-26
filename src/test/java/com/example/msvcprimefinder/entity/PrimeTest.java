package com.example.msvcprimefinder.entity;

import com.example.msvcprimefinder.model.entity.Prime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PrimeTest {

    @Test
    public void testEquals_SameReference() {
        Prime prime = new Prime(7L);
        assertTrue(prime.equals(prime), "Should be equal to itself");
    }

    @Test
    public void testEquals_NullObject() {
        Prime prime = new Prime(7L);
        assertFalse(prime.equals(null), "Should not be equal to null");
    }

    @Test
    public void testEquals_DifferentClass() {
        Prime prime = new Prime(7L);
        String notAPrime = "I am not a Prime";
        assertFalse(prime.equals(notAPrime), "Should not be equal to an object of a different class");
    }

    @Test
    public void testEquals_SameId() {
        Prime prime1 = new Prime(7L);
        prime1.setId(1L); // Assign an ID
        Prime prime2 = new Prime(7L);
        prime2.setId(1L); // Assign the same ID
        assertTrue(prime1.equals(prime2), "Should be equal since IDs are the same");
    }

    @Test
    public void testEquals_DifferentId() {
        Prime prime1 = new Prime(7L);
        prime1.setId(1L); // Assign an ID
        Prime prime2 = new Prime(7L);
        prime2.setId(2L); // Assign a different ID
        assertFalse(prime1.equals(prime2), "Should not be equal since IDs are different");
    }

    @Test
    public void testHashCode_SameId() {
        Prime prime1 = new Prime(7L);
        prime1.setId(1L); // Assign an ID
        Prime prime2 = new Prime(7L);
        prime2.setId(1L); // Assign the same ID
        assertEquals(prime1.hashCode(), prime2.hashCode(), "Hash codes should be equal for primes with the same ID");
    }

    @Test
    public void testHashCode_DifferentId() {
        Prime prime1 = new Prime(7L);
        prime1.setId(1L); // Assign an ID
        Prime prime2 = new Prime(7L);
        prime2.setId(2L); // Assign a different ID
        assertEquals(prime1.hashCode(), prime2.hashCode(), "Hash codes should be equal for primes with different IDs but same values");
    }

    @Test
    public void testHashCode_SameReference() {
        Prime prime = new Prime(7L);
        assertEquals(prime.hashCode(), prime.hashCode(), "Hash code should be equal to itself");
    }

    @Test
    public void testHashCode_NullId() {
        Prime prime1 = new Prime(7L);
        prime1.setId(null); // Set ID to null
        Prime prime2 = new Prime(7L);
        prime2.setId(null); // Set ID to null
        assertEquals(prime1.hashCode(), prime2.hashCode(), "Hash codes should be equal for primes with null IDs");
    }
}

