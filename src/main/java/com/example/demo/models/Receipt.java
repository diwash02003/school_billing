package com.example.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author diwash
 * @date 10/10/25
 * @description This file contains...
 */

@Entity
@Table(name = "receipts")
@Getter
@Setter
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_no", nullable = false, unique = true)
    private String receiptNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate = LocalDate.now();

    @PositiveOrZero
    @Column(name = "paid_amount", nullable = false)
    private Double paidAmount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "remarks")
    private String remarks;

    @PositiveOrZero
    @Column(name = "previous_due_snapshot")
    private Double previousDueSnapshot;

    @PositiveOrZero
    @Column(name = "remaining_due")
    private Double remainingDue;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}

