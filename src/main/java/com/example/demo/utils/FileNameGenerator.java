package com.example.demo.utils;

import com.example.demo.exceptions.PaymentValidationException;
import com.example.demo.models.Invoice;
import com.example.demo.models.Receipt;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.repositories.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @author diwash
 * @created 12/20/25
 */

@Component
@RequiredArgsConstructor
public class FileNameGenerator {
    private final NameExtractor nameExtractor;
    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;

    private static final String PLACE_HOLDER_NA = "NA";
    private static final String INVOICE_PREFIX = "Invoice_";
    private static final String RECEIPT_PREFIX = "Receipt_";
    private static final String PDF_EXTENSION = ".pdf";
    private static final String UNDERSCORE = "_";

    // Regex pattern constants
    private static final String REGEX_NON_ALPHANUMERIC = "[^a-zA-Z0-9]";
    private static final String REGEX_NON_ALPHANUMERIC_UNDERSCORE = "[^a-zA-Z0-9_]";
    private static final String REPLACEMENT_UNDERSCORE = "_";

    public String generateInvoiceFileName(Long invoiceId) {
        Invoice invoice = paymentRepository.findByIdWithStudent(invoiceId)
                .orElseThrow(() -> new PaymentValidationException("Payment not found"));

        String firstName = nameExtractor.extractFirstName(invoice.getStudent().getFullName());
        String monthsPart = nameExtractor.formatMonthsForFileName(invoice.getMonths());
        return generateFileName(INVOICE_PREFIX, firstName, monthsPart, true);
    }

    public String generateReceiptFileName(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new PaymentValidationException("Receipt not found"));
        String firstName = nameExtractor.extractFirstName(receipt.getStudent().getFullName());
        String datePart = nameExtractor.formatDateForFileName(receipt.getReceiptDate());
        return generateFileName(RECEIPT_PREFIX, firstName, datePart, false);
    }

    private String generateFileName(String prefix, String firstName, String part, boolean allowUnderscoreInPart) {
        String sanitizedFirstName = sanitizeFileNamePart(firstName, false);
        String sanitizedPart = sanitizeFileNamePart(part, allowUnderscoreInPart);

        return prefix + sanitizedFirstName + UNDERSCORE + sanitizedPart + PDF_EXTENSION;
    }

    private String sanitizeFileNamePart(String text, boolean allowUnderscore) {
        if (text == null || text.isEmpty()) {
            return PLACE_HOLDER_NA;
        }

        String regex = allowUnderscore ? REGEX_NON_ALPHANUMERIC_UNDERSCORE : REGEX_NON_ALPHANUMERIC;
        return text.replaceAll(regex, REPLACEMENT_UNDERSCORE);
    }
}


