package com.example.demo.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceStatusDTO {
    private boolean hasPaidAdmission;
    private List<String> paidMonths;
    private List<String> availableMonths;
    private Double remainingDue;
}
