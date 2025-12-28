package com.example.demo.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDate;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

@Setter
@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class StudentDTO {

    private Long id;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Guardian name is required")
    private String guardianName;

    @NotBlank(message = "Phone is required")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Class is required")
    private String studentClass;

    @NotNull(message = "Admission date is required")
//    private LocalDateTime admissionDate;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate admissionDate;

    @PositiveOrZero(message = "Admission fee must be positive or zero")
    private Double admissionFee = 0.0;

    @PositiveOrZero(message = "Monthly fee must be positive or zero")
    private Double monthlyFee = 0.0;

    @PositiveOrZero(message = "Transport fee must be positive or zero")
    private Double transportFee = 0.0;

    @PositiveOrZero(message = "Previous due must be positive or zero")
    private Double previousDue = 0.0;

    private String notes;
}
