package com.example.demo.dtos;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author diwash
 * @date 10/5/25
 * @description This file contains...
 */

@Getter
@Setter
public class InvoiceFormDataDTO {
    private Long studentId;
    private String studentName;
    private String studentClass;
    private Double previousDue;
    private Double admissionFee;
    private Double monthlyFee;
    private Double transportFee;
    private boolean hasPaidAdmission;
    private List<String> availableMonths;
    private List<String> paidMonths;
}
