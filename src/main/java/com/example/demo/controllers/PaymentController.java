package com.example.demo.controllers;

import com.example.demo.dtos.*;
import com.example.demo.services.BillService;
import com.example.demo.services.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins:http://localhost:3000}")
public class PaymentController {
    private final PaymentService paymentService;
    private final BillService billService;

    @PostMapping
    public ResponseEntity<?> createPayment(@Valid @RequestBody PaymentRequestDTO paymentRequest) {
        try {
            PaymentResponseDTO createdPayment = paymentService.createPayment(paymentRequest);
            return ResponseEntity.ok(createdPayment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable Long id) {
        return paymentService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByStudent(@PathVariable Long studentId) {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByStudentId(studentId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/student/{studentId}/status")
    public ResponseEntity<PaymentStatusDTO> getPaymentStatus(@PathVariable Long studentId) {
        PaymentStatusDTO status = paymentService.getPaymentStatus(studentId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/student/{studentId}/form-data")
    public ResponseEntity<PaymentFormDataDTO> getPaymentFormData(@PathVariable Long studentId) {
        PaymentFormDataDTO formData = paymentService.getPaymentFormData(studentId);
        return ResponseEntity.ok(formData);
    }


    @GetMapping("/{paymentId}/bill")
    public ResponseEntity<?> generateBill(@PathVariable Long paymentId) {
        try {
            byte[] pdfBytes = billService.generateBillPdf(paymentId);

            String filename = billService.generateBillFileName(paymentId);
            ContentDisposition contentDisposition = ContentDisposition.builder("attachment")
                    .filename(filename)
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to generate bill: " + e.getMessage());
        }
    }
}
