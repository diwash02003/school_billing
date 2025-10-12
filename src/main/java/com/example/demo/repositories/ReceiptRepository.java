package com.example.demo.repositories;

import com.example.demo.models.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author diwash
 * @date 10/10/25
 * @description This file contains...
 */

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    List<Receipt> findByStudentIdOrderByReceiptDateDesc(Long studentId);
}
