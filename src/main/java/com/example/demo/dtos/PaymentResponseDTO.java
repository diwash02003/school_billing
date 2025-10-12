package com.example.demo.dtos;

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
public class PaymentResponseDTO {
    private Long id;
    private String invoiceNo;
    private Long studentId;
    private String studentName;
    private String studentClass;
    private LocalDate paymentDate;
    private Double admissionFee;
    private Double monthlyFee;
    private Double transportFee;
    private Double othersFee;
    private String othersNote;
    private List<String> months;
    private Double totalAmount;
    private Double previousDue;
    private Double grandTotal;
    private String status;
    private Boolean isAdmissionPaid;
}
