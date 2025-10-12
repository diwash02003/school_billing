package com.example.demo.repositories;

import com.example.demo.models.SequenceGenerator;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @author diwash
 * @date 10/11/25
 * @description This file contains...
 */

@Repository
public interface SequenceGeneratorRepository extends JpaRepository<SequenceGenerator, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sg FROM SequenceGenerator sg WHERE sg.sequenceName = :sequenceName")
    Optional<SequenceGenerator> findByNameForUpdate(@Param("sequenceName") String sequenceName);
}