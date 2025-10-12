package com.example.demo.repositories;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

import com.example.demo.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findByIsActiveTrue();

    @Query("SELECT s FROM Student s LEFT JOIN FETCH s.invoices WHERE s.id = :id")
    Optional<Student> findByIdWithPayments(@Param("id") Long id);

    boolean existsByPhone(String phone);
}
