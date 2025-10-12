package com.example.demo.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * @author diwash
 * @date 10/10/25
 * @description This file contains...
 */

@Getter
@Setter
public class ReceiptRequestDTO {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Paid amount is required")
    @Positive(message = "Paid amount must be greater than zero")
    private Double paidAmount;

    private String paymentMethod;
    private String remarks;
}