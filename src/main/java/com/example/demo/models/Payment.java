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
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "payment_date", nullable = false)
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


    // Constructors
    public Payment() {
        this.paymentDate = LocalDate.now();
        this.createdAt = LocalDateTime.now();
    }

    // Updated calculation method
    public void calculateTotals() {
        int monthsCount = this.months.size();
        double monthlyTotal = this.monthlyFee * monthsCount;
        double transportTotal = this.transportFee * monthsCount;

        // Calculate total fees (without previous due)
        this.totalAmount = this.admissionFee + monthlyTotal + transportTotal + this.othersFee;

        // If totalPaidAmount is not set, assume full payment
        if (this.totalPaidAmount == null || this.totalPaidAmount == 0) {
            this.totalPaidAmount = this.totalAmount + this.previousDue;
        }

        // Grand total becomes the actual paid amount for partial payments
        this.grandTotal = this.totalPaidAmount;

        // Set admission paid flag
        this.isAdmissionPaid = this.admissionFee > 0;
    }

    // Helper method to check if payment is partial
    public Boolean isPartialPayment() {
        double totalDue = this.totalAmount + this.previousDue;
        return this.totalPaidAmount < totalDue;
    }

    // Helper method to get remaining due after this payment
    public Double getRemainingDue() {
        double totalDue = this.totalAmount + this.previousDue;
        return Math.max(0, totalDue - this.totalPaidAmount);
    }
}
