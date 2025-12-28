package com.example.demo.services;

import com.example.demo.dtos.DashboardSummary;
import com.example.demo.mappers.DashboardMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author diwash
 * @created 12/28/25
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class DashBoardService {
    private final DashboardMapper dashboardMapper;

    public DashboardSummary getDashboardSummary() {
        return dashboardMapper.getDashboardSummary();
    }
}

