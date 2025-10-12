package com.example.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

@Entity
@Table(name = "payments")
@Getter
@Setter
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Invoice number (e.g. INV-2025-0001)
    @Column(name = "invoice_no", nullable = false, unique = true)
    private String invoiceNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "invoice_date", nullable = false)
    private LocalDate paymentDate;

    @PositiveOrZero(message = "Admission fee must be positive or zero")
    @Column(name = "admission_fee")
    private Double admissionFee = 0.0;

    @PositiveOrZero(message = "Monthly fee must be positive or zero")
    @Column(name = "monthly_fee")
    private Double monthlyFee = 0.0;

    @PositiveOrZero(message = "Transport fee must be positive or zero")
    @Column(name = "transport_fee")
    private Double transportFee = 0.0;

    @PositiveOrZero(message = "Others fee must be positive or zero")
    @Column(name = "others_fee")
    private Double othersFee = 0.0;

    @Column(name = "others_note")
    @Size(max = 250, message = "Others note must not exceed 250 characters")
    private String othersNote;

    @ElementCollection
    @CollectionTable(name = "payment_months", joinColumns = @JoinColumn(name = "payment_id"))
    @Column(name = "month")
    private List<String> months = new ArrayList<>();

    @PositiveOrZero(message = "Total amount must be positive or zero")
    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    @PositiveOrZero(message = "Previous due must be positive or zero")
    @Column(name = "previous_due")
    private Double previousDue = 0.0;

    @PositiveOrZero(message = "Grand total must be positive or zero")
    @Column(name = "grand_total")
    private Double grandTotal = 0.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Payment status flags
    @Column(name = "is_admission_paid")
    private Boolean isAdmissionPaid = false;

    // NEW: Clear separation of amounts
    @PositiveOrZero(message = "Total paid amount must be positive or zero")
    @Column(name = "total_paid_amount")
    private Double totalPaidAmount = 0.0;

    // Invoice status: CREATED / CANCELLED (paid is tracked by receipts)
    @Column(name = "status", nullable = false)
    private String status = "CREATED";


    // Constructors
    public Invoice() {
        this.paymentDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    public void calculateTotals() {
        int monthsCount = this.months == null ? 0 : this.months.size();
        double monthlyTotal = this.monthlyFee * monthsCount;
        double transportTotal = this.transportFee * monthsCount;
        this.totalAmount = this.admissionFee + monthlyTotal + transportTotal + (this.othersFee == null ? 0 : this.othersFee);
        this.grandTotal = this.totalAmount + (this.previousDue == null ? 0 : this.previousDue);

        // Set admission flag only if included in this invoice
        this.isAdmissionPaid = this.admissionFee != null && this.admissionFee > 0;
    }
}
