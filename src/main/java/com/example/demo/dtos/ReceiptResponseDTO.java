package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * @author diwash
 * @date 10/10/25
 * @description This file contains...
 */

@Getter
@Setter
public class ReceiptResponseDTO {
    private Long id;
    private String receiptNo;
    private Long studentId;
    private String studentName;
    private LocalDate receiptDate;
    private Double paidAmount;
    private String paymentMethod;
    private String remarks;
    private Double previousDueSnapshot;
    private Double remainingDue;
}