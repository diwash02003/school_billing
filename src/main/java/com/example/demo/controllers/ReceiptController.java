package com.example.demo.controllers;

import com.example.demo.dtos.ReceiptRequestDTO;
import com.example.demo.services.BillService;
import com.example.demo.services.ReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * @author diwash
 * @date 10/10/25
 * @description This file contains...
 */

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:3000}")
public class ReceiptController {

    private final ReceiptService receiptService;
    private final BillService billService;

    @PostMapping
    public ResponseEntity<?> createReceipt(@Valid @RequestBody ReceiptRequestDTO req) {
        try {
            var res = receiptService.createReceipt(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getReceiptsForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(receiptService.getReceiptsByStudent(studentId));
    }

    // ------------------ PDF GENERATION ------------------
    @GetMapping("/{receiptId}/bill")
    public ResponseEntity<?> generateReceiptPdf(@PathVariable Long receiptId) {
        try {
            byte[] pdfBytes = billService.generateReceiptPdf(receiptId); // New method in BillService
            String filename = billService.generateReceiptFileName(receiptId); // New filename generator

            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(filename)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to generate receipt PDF: " + e.getMessage());
        }
    }
}

