package com.example.demo.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Table(name = "students")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotBlank(message = "Guardian name is required")
    @Column(name = "guardian_name", nullable = false)
    private String guardianName;

    @NotBlank(message = "Phone is required")
    @Column(nullable = false)
    private String phone;

    @Email(message = "Invalid email format")
    @Column
    private String email;

    @NotBlank(message = "Address is required")
    @Column(nullable = false)
    private String address;

    @NotBlank(message = "Class is required")
    @Column(nullable = false)
    private String studentClass;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @PositiveOrZero(message = "Admission fee must be positive or zero")
    @Column(name = "admission_fee")
    private Double admissionFee = 0.0;

    @PositiveOrZero(message = "Monthly fee must be positive or zero")
    @Column(name = "monthly_fee")
    private Double monthlyFee = 0.0;

    @PositiveOrZero(message = "Transport fee must be positive or zero")
    @Column(name = "transport_fee")
    private Double transportFee = 0.0;

    @PositiveOrZero(message = "Previous due must be positive or zero")
    @Column(name = "previous_due")
    private Double previousDue = 0.0;

    @Column
    private String notes;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Invoice> invoices = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Receipt> receipts = new ArrayList<>();

    // Constructors
    public Student() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
