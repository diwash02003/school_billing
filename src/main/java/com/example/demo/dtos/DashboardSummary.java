package com.example.demo.dtos;

import lombok.*;

import java.math.BigDecimal;

/**
 * @author diwash
 * @created 12/28/25
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private Long totalActiveStudents;
    private BigDecimal totalRevenueCollected;
    private BigDecimal totalDueLeft;
}
