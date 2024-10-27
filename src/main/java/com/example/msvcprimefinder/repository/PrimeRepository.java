package com.example.msvcprimefinder.repository;

import com.example.msvcprimefinder.model.entity.Prime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PrimeRepository extends JpaRepository<Prime, Long> {

    @Query("SELECT p.value FROM Prime p WHERE p.value <= :limit")
    List<Long> findByValueLessThanEqual(@Param("limit") Long limit);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Prime")
    void deleteAllPrimes();
}
