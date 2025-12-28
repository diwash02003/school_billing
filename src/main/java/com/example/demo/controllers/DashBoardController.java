package com.example.demo.controllers;

import com.example.demo.dtos.DashboardSummary;
import com.example.demo.services.DashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author diwash
 * @created 12/28/25
 */

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashBoardController {
    private final DashBoardService dashBoardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardSummary> getSummary() {
        return ResponseEntity.ok(dashBoardService.getDashboardSummary());
    }
}
