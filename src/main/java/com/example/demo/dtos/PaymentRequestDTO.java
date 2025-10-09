package com.example.demo.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * @author diwash
 * @date 10/5/25
 * @description This file contains...
 */

@Getter
@Setter
public class PaymentRequestDTO {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @PositiveOrZero(message = "Admission fee must be positive or zero")
    private Double admissionFee = 0.0;

    @PositiveOrZero(message = "Monthly fee must be positive or zero")
    private Double monthlyFee = 0.0;

    @PositiveOrZero(message = "Transport fee must be positive or zero")
    private Double transportFee = 0.0;

    @PositiveOrZero(message = "Others fee must be positive or zero")
    private Double othersFee = 0.0;

    @Size(max = 250, message = "Others note must not exceed 250 characters")
    private String othersNote;

    @NotEmpty(message = "At least one month must be selected")
    @Size(min = 1, max = 12, message = "Must select between 1 and 12 months")
    private List<String> months;

    // These will be calculated automatically
    private Double previousDue = 0.0;
    private Double totalAmount = 0.0;
    private Double grandTotal = 0.0;
    private Double totalPaidAmount = 0.0;
}
