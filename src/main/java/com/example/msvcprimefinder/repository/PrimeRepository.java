package com.example.msvcprimefinder.repository;

import com.example.msvcprimefinder.model.entity.Prime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PrimeRepository extends JpaRepository<Prime, Long> {

    @Query("SELECT MAX(p.value) FROM Prime p")
    Prime findMaxPrime();

    List<Prime> findByValueLessThanEqual(Long limit);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM Prime")
    void dropTable();
}
