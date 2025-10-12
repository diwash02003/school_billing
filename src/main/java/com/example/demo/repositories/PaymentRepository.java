package com.example.demo.repositories;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

import com.example.demo.models.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByStudentIdOrderByPaymentDateDesc(Long studentId);

    @Query("SELECT p FROM Invoice p JOIN FETCH p.student WHERE p.id = :id")
    Optional<Invoice> findByIdWithStudent(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Invoice p WHERE p.student.id = :studentId AND p.admissionFee > 0")
    boolean existsByStudentIdAndAdmissionFeePaid(@Param("studentId") Long studentId);


    // Fixed query to get all payments for a student and then extract months in service
    @Query("SELECT p FROM Invoice p WHERE p.student.id = :studentId")
    List<Invoice> findAllByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT p FROM Invoice p JOIN FETCH p.student WHERE p.student.id = :studentId")
    List<Invoice> findAllByStudentIdWithStudent(@Param("studentId") Long studentId);

    boolean existsByInvoiceNo(String invoiceNo);
}
