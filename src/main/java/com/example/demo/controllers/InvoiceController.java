package com.example.demo.controllers;

import com.example.demo.dtos.*;
import com.example.demo.services.BillService;
import com.example.demo.services.InvoiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import com.example.demo.dtos.InvoiceRequestDTO;
import com.example.demo.dtos.InvoiceResponseDTO;

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
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final BillService billService;

    @PostMapping
    public ResponseEntity<?> createInvoice(@Valid @RequestBody InvoiceRequestDTO paymentRequest) {
        try {
            InvoiceResponseDTO created = invoiceService.createInvoice(paymentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        return invoiceService.getPaymentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoiceByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(invoiceService.getPaymentsByStudentId(studentId));
    }

    @GetMapping("/student/{studentId}/status")
    public ResponseEntity<InvoiceStatusDTO> getInvoiceStatus(@PathVariable Long studentId) {
        InvoiceStatusDTO status = invoiceService.getPaymentStatus(studentId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/student/{studentId}/form-data")
    public ResponseEntity<InvoiceFormDataDTO> getInvoiceFormData(@PathVariable Long studentId) {
        InvoiceFormDataDTO formData = invoiceService.getPaymentFormData(studentId);
        return ResponseEntity.ok(formData);
    }

    @GetMapping("/{paymentId}/bill")
    public ResponseEntity<?> generateBill(@PathVariable Long paymentId) {
        try {
            byte[] pdfBytes = billService.generateInvoicePdf(paymentId);
            String filename = billService.generateInvoiceFileName(paymentId);
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
