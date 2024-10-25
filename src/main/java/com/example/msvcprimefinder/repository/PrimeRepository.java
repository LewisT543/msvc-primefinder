package com.example.msvcprimefinder.repository;

import com.example.msvcprimefinder.model.entity.Prime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PrimeRepository extends JpaRepository<Prime, Long> {
    @Query("SELECT MAX(p.prime_number) FROM Prime p")
    Prime findMaxPrime();
    List<Prime> findByValueLessThanEqual(Long limit);
}
