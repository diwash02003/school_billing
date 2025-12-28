package com.example.demo.services;

import com.example.demo.services.pdf.InvoicePdfService;
import com.example.demo.services.pdf.ReceiptPdfService;
import com.example.demo.utils.FileNameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author diwash
 * @date 10/5/25
 * @description This file contains...
 */

@Service
@RequiredArgsConstructor
public class BillService {
    private final InvoicePdfService invoicePdfService;
    private final ReceiptPdfService receiptPdfService;
    private final FileNameGenerator fileNameGenerator;

    // INVOICE
    public byte[] generateInvoicePdf(Long paymentId) {
        return invoicePdfService.generateInvoicePdf(paymentId);
    }

    public String generateInvoiceFileName(Long paymentId) {
        return fileNameGenerator.generateInvoiceFileName(paymentId);
    }

    // RECEIPT
    public byte[] generateReceiptPdf(Long receiptId) {
        return receiptPdfService.generateReceiptPdf(receiptId);
    }

    public String generateReceiptFileName(Long receiptId) {
        return fileNameGenerator.generateReceiptFileName(receiptId);
    }
}