package com.example.demo.services;

import com.example.demo.exceptions.PaymentValidationException;
import com.example.demo.models.Payment;
import com.example.demo.models.Receipt;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.repositories.ReceiptRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

/**
 * @author diwash
 * @date 10/5/25
 * @description This file contains...
 */
@Service
@RequiredArgsConstructor
public class BillService {

    private final PaymentRepository paymentRepository;
    private final ReceiptRepository receiptRepository;

    // ---------------- INVOICE PDF ----------------
    public byte[] generateInvoicePdf(Long paymentId) {
        Payment payment = paymentRepository.findByIdWithStudent(paymentId)
                .orElseThrow(() -> new PaymentValidationException("Payment not found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            document.add(createHeader());
            document.add(Chunk.NEWLINE);

            // Title
            Paragraph title = new Paragraph("INVOICE", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            document.add(title);

            // Info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            String formattedDate = payment.getPaymentDate() != null
                    ? payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    : "";

            infoTable.addCell(getCell("Bill No: " + payment.getInvoiceNo(), PdfPCell.ALIGN_LEFT));
            infoTable.addCell(getCell("Date: " + formattedDate, PdfPCell.ALIGN_RIGHT));
            infoTable.addCell(getCell("Name: " + payment.getStudent().getFullName(), PdfPCell.ALIGN_LEFT));
            infoTable.addCell(getCell("Class: " + payment.getStudent().getStudentClass(), PdfPCell.ALIGN_RIGHT));

            String months = payment.getMonths() == null || payment.getMonths().isEmpty() ? "-" : String.join(", ", payment.getMonths());
            PdfPCell monthCell = new PdfPCell(new Phrase("Payment for the Month of: " + months));
            monthCell.setColspan(2);
            monthCell.setBorder(Rectangle.NO_BORDER);
            monthCell.setPaddingTop(5f);
            infoTable.addCell(monthCell);

            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Fee table
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 6, 3});

            table.addCell(headerCell("S.N."));
            table.addCell(headerCell("Particular"));
            table.addCell(headerCell("Amount"));

            int sn = 1;
            addFeeRow(table, sn++, "Admission Fee", payment.getAdmissionFee());
            addFeeRow(table, sn++, "Monthly Fee", payment.getMonthlyFee());
            addFeeRow(table, sn++, "Transport Fee", payment.getTransportFee());
            String othersNote = "Others";
            if (payment.getOthersNote() != null && !payment.getOthersNote().trim().isEmpty()) {
                othersNote = "Others (" + payment.getOthersNote() + ")";
            }
            addFeeRow(table, sn++, othersNote, payment.getOthersFee()); // Always show Others row

            addTotalRow(table, "Total Amount", payment.getTotalAmount());
            addTotalRow(table, "Previous Due", payment.getPreviousDue());
            addTotalRow(table, "Grand Total", payment.getGrandTotal());

            document.add(table);

            // Signature
            document.add(Chunk.NEWLINE);
            document.add(createSignatureTable());

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice PDF", e);
        }
    }

    // ---------------- RECEIPT PDF ----------------
    public byte[] generateReceiptPdfOld(Long receiptId) {
        Receipt r = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new PaymentValidationException("Receipt not found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header
            document.add(createHeader());
            document.add(Chunk.NEWLINE);

            // Title
            Paragraph title = new Paragraph("RECEIPT", new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(10f);
            document.add(title);

            // Info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            String formattedDate = r.getReceiptDate() != null
                    ? r.getReceiptDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    : "";

            infoTable.addCell(getCell("Receipt No: " + r.getReceiptNo(), PdfPCell.ALIGN_LEFT));
            infoTable.addCell(getCell("Date: " + formattedDate, PdfPCell.ALIGN_RIGHT));
            infoTable.addCell(getCell("Name: " + r.getStudent().getFullName(), PdfPCell.ALIGN_LEFT));
            infoTable.addCell(getCell("Class: " + r.getStudent().getStudentClass(), PdfPCell.ALIGN_RIGHT));

            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Payment table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(60);
            table.setHorizontalAlignment(Element.ALIGN_CENTER);

            table.addCell(headerCell("Particular"));
            table.addCell(headerCell("Amount"));

            table.addCell(valueCellLeft("Paid Amount"));
            table.addCell(valueCell(r.getPaidAmount()));

            table.addCell(valueCellLeft("Previous Due"));
            table.addCell(valueCell(r.getPreviousDueSnapshot()));

            table.addCell(valueCellLeft("Remaining Due"));
            table.addCell(valueCell(r.getRemainingDue()));

            table.addCell(valueCellLeft("Payment Method"));
            table.addCell(valueCell(r.getPaymentMethod()));

            if (r.getRemarks() != null && !r.getRemarks().isEmpty()) {
                table.addCell(valueCellLeft("Remarks"));
                table.addCell(valueCell(r.getRemarks()));
            }

            document.add(table);

            // Signature
            document.add(Chunk.NEWLINE);
            document.add(createSignatureTable());

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating receipt PDF", e);
        }
    }

    // ------------------ COMMON UTILITY METHODS ------------------
    private PdfPCell getCell(String text, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private PdfPCell headerCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell valueCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-"));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell valueCell(Double amount) {
        return valueCell(amount != null ? amount + "/-" : "-");
    }

    private PdfPCell valueCellLeft(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5f);
        return cell;
    }

    private void addFeeRow(PdfPTable table, int sn, String label, Double amount) {
        table.addCell(valueCell(String.valueOf(sn)));
        table.addCell(valueCellLeft(label));
        table.addCell(valueCell(amount));
    }

    private void addTotalRow(PdfPTable table, String label, Double amount) {
        table.addCell(valueCell(" "));
        table.addCell(valueCellLeft(label));
        table.addCell(valueCell(amount));
    }

    private PdfPTable createHeader() throws Exception {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(80);
        headerTable.setWidths(new float[]{2, 5}); // Logo:Text ratio

        // Logo
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingRight(10f); // add some gap between logo and text

        try {
            InputStream logoStream = new ClassPathResource("static/images/logo.png").getInputStream();
            Image logo = Image.getInstance(logoStream.readAllBytes());
            logo.scaleToFit(120, 120);
            logoCell.addElement(logo);
        } catch (Exception e) {
            Paragraph placeholder = new Paragraph("LOGO",
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.GRAY));
            placeholder.setAlignment(Element.ALIGN_CENTER);
            logoCell.addElement(placeholder);
        }

        // School info
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE); // vertical center relative to logo
        infoCell.setPaddingLeft(10f); // optional extra gap

        // Use a nested table to center the 3 lines vertically
        PdfPTable nested = new PdfPTable(1);
        nested.setWidthPercentage(100);

        Paragraph line1 = new Paragraph("Wonderkidz Preschool", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
        Paragraph line2 = new Paragraph("Tokha-6, Kathmandu", new Font(Font.FontFamily.HELVETICA, 11));
        Paragraph line3 = new Paragraph("Tel: 01-4972224, Email: wonderkidzp@gmail.com", new Font(Font.FontFamily.HELVETICA, 11));

        // center text horizontally in nested cell
        PdfPCell p1 = new PdfPCell(line1);
        p1.setBorder(Rectangle.NO_BORDER);
        p1.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell p2 = new PdfPCell(line2);
        p2.setBorder(Rectangle.NO_BORDER);
        p2.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell p3 = new PdfPCell(line3);
        p3.setBorder(Rectangle.NO_BORDER);
        p3.setHorizontalAlignment(Element.ALIGN_LEFT);

        nested.addCell(p1);
        nested.addCell(p2);
        nested.addCell(p3);

        infoCell.addElement(nested);

        headerTable.addCell(logoCell);
        headerTable.addCell(infoCell);

        return headerTable;
    }


    private PdfPTable createSignatureTable() throws DocumentException {
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setWidths(new float[]{7, 3});

        PdfPCell emptyCell = new PdfPCell(new Paragraph(""));
        emptyCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell signatureCell = new PdfPCell();
        signatureCell.setBorder(Rectangle.NO_BORDER);
        signatureCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        try {
            InputStream signatureStream = new ClassPathResource("static/images/signature.jpeg").getInputStream();
            Image signature = Image.getInstance(signatureStream.readAllBytes());
            signature.scaleToFit(100, 40);
            signature.setAlignment(Element.ALIGN_RIGHT);
            signatureCell.addElement(signature);
        } catch (Exception e) {
            Paragraph placeholder = new Paragraph("[Signature]", new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
            placeholder.setAlignment(Element.ALIGN_RIGHT);
            signatureCell.addElement(placeholder);
        }

        Paragraph officerText = new Paragraph("Account Officer", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD));
        officerText.setAlignment(Element.ALIGN_RIGHT);
        officerText.setSpacingBefore(5f);
        signatureCell.addElement(officerText);

        signatureTable.addCell(emptyCell);
        signatureTable.addCell(signatureCell);
        return signatureTable;
    }

    // ------------------ FILE NAME GENERATORS ------------------
    public String generateInvoiceFileName(Long paymentId) {
        Payment payment = paymentRepository.findByIdWithStudent(paymentId)
                .orElseThrow(() -> new PaymentValidationException("Payment not found"));

        String firstName = payment.getStudent().getFullName().split(" ")[0];
        String monthsPart = (payment.getMonths() == null || payment.getMonths().isEmpty())
                ? "NA"
                : String.join("_", payment.getMonths());

        firstName = firstName.replaceAll("[^a-zA-Z0-9]", "_");
        monthsPart = monthsPart.replaceAll("[^a-zA-Z0-9_]", "_");

        return "Invoice_" + firstName + "_" + monthsPart + ".pdf";
    }

    public String generateReceiptFileName(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new PaymentValidationException("Receipt not found"));

        String firstName = receipt.getStudent().getFullName().split(" ")[0];
        String datePart = receipt.getReceiptDate() != null
                ? receipt.getReceiptDate().format(DateTimeFormatter.ofPattern("ddMMMyyyy"))
                : "NA";

        firstName = firstName.replaceAll("[^a-zA-Z0-9]", "_");
        datePart = datePart.replaceAll("[^a-zA-Z0-9]", "_");

        return "Receipt_" + firstName + "_" + datePart + ".pdf";
    }

    // ---------------- RECEIPT PDF ----------------
    public byte[] generateReceiptPdf(Long receiptId) {
        Receipt r = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new PaymentValidationException("Receipt not found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Use your existing header function
            document.add(createHeader());
            document.add(Chunk.NEWLINE);

            // Title - RECEIPT in center
            Paragraph title = new Paragraph("RECEIPT", new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // Horizontal line
            Paragraph line1 = new Paragraph("__________________________________________________________________________");
            line1.setAlignment(Element.ALIGN_CENTER);
            line1.setSpacingAfter(15f);
            document.add(line1);

            String amountInWords = convertToWords(r.getPaidAmount());

            Paragraph receivedPara = new Paragraph();
            receivedPara.setAlignment(Element.ALIGN_LEFT);
            receivedPara.setSpacingAfter(20f);

// Build the text with proper spacing
            receivedPara.add(new Phrase("Received with thanks from: ", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));

// Underlined student name with space
            Phrase studentNamePhrase = new Phrase("  " + r.getStudent().getFullName() + "  ",
                    new Font(Font.FontFamily.HELVETICA, 11, Font.UNDERLINE));
            receivedPara.add(studentNamePhrase);

            receivedPara.add(new Phrase("the sum of: ", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));

// Underlined amount with space
            String amountText = r.getPaidAmount() != null ? String.format("%,.2f", r.getPaidAmount()) : "______";
            Phrase amountPhrase = new Phrase("  Rs. " + amountText + "  ",
                    new Font(Font.FontFamily.HELVETICA, 11, Font.UNDERLINE));
            receivedPara.add(amountPhrase);

            receivedPara.add(new Phrase("in words: ", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD)));

// Underlined amount in words with space
            Phrase amountWordsPhrase = new Phrase("  " + amountInWords + "  ",
                    new Font(Font.FontFamily.HELVETICA, 11, Font.UNDERLINE));
            receivedPara.add(amountWordsPhrase);

            receivedPara.add(new Phrase(".", new Font(Font.FontFamily.HELVETICA, 11)));

            document.add(receivedPara);
            // O/A and Class information
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{1, 1});

            // O/A of (using Receipt No) and Class
            infoTable.addCell(createSimpleCell("O/A of " + (r.getReceiptNo() != null ? r.getReceiptNo() : "______"),
                    Element.ALIGN_LEFT, false));
            infoTable.addCell(createSimpleCell("Class: " + r.getStudent().getStudentClass(),
                    Element.ALIGN_RIGHT, false));

            infoTable.setSpacingAfter(15f);
            document.add(infoTable);

            // Balance table with borders - Rs. and amount on same line
            PdfPTable balanceTable = new PdfPTable(3);
            balanceTable.setWidthPercentage(100);
            balanceTable.setWidths(new float[]{1, 1, 1});

            balanceTable.addCell(createBorderedCell("PRE BALANCE", Element.ALIGN_CENTER, true, true));
            balanceTable.addCell(createBorderedCell("AMOUNT PAID", Element.ALIGN_CENTER, true, true));
            balanceTable.addCell(createBorderedCell("BALANCE DUE", Element.ALIGN_CENTER, true, true));

            balanceTable.addCell(createBorderedCell(
                    r.getPreviousDueSnapshot() != null ? "Rs. " + String.format("%,.2f", r.getPreviousDueSnapshot()) : "Rs. 0.00",
                    Element.ALIGN_CENTER, false, false));
            balanceTable.addCell(createBorderedCell(
                    r.getPaidAmount() != null ? "Rs. " + String.format("%,.2f", r.getPaidAmount()) : "Rs. 0.00",
                    Element.ALIGN_CENTER, false, false));
            balanceTable.addCell(createBorderedCell(
                    r.getRemainingDue() != null ? "Rs. " + String.format("%,.2f", r.getRemainingDue()) : "Rs. 0.00",
                    Element.ALIGN_CENTER, false, false));

            balanceTable.setSpacingAfter(20f);
            document.add(balanceTable);

            // Horizontal line
            Paragraph line2 = new Paragraph("__________________________________________________________________________");

            line2.setAlignment(Element.ALIGN_CENTER);
            line2.setSpacingAfter(15f);
            document.add(line2);

            // Month and additional information
            PdfPTable footerTable = new PdfPTable(2);
            footerTable.setWidthPercentage(100);
            footerTable.setWidths(new float[]{1, 1});

            String monthYear = r.getReceiptDate() != null ?
                    r.getReceiptDate().format(DateTimeFormatter.ofPattern("MMMM yyyy")) : "______";

            footerTable.addCell(createSimpleCell("Month: " + monthYear, Element.ALIGN_LEFT, false));

            // Payment method
            String paymentMethod = r.getPaymentMethod() != null ? r.getPaymentMethod() : "______";
            footerTable.addCell(createSimpleCell("Payment Method: " + paymentMethod, Element.ALIGN_RIGHT, false));

            footerTable.setSpacingAfter(10f);
            document.add(footerTable);

            // Remarks if available
            if (r.getRemarks() != null && !r.getRemarks().trim().isEmpty()) {
                Paragraph remarksPara = new Paragraph("Remarks: " + r.getRemarks(),
                        new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC));
                remarksPara.setAlignment(Element.ALIGN_LEFT);
                remarksPara.setSpacingAfter(10f);
                document.add(remarksPara);
            }

            // Use your existing signature function
            document.add(Chunk.NEWLINE);
            document.add(createSignatureTable());

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating receipt PDF", e);
        }
    }

    // New helper methods for the receipt
    private PdfPCell createSimpleCell(String text, int alignment, boolean isBold) {
        Font font = isBold ?
                new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD) :
                new Font(Font.FontFamily.HELVETICA, 11);

        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(6f);
        cell.setBorder(Rectangle.NO_BORDER);
        return cell;
    }

    private PdfPCell createBorderedCell(String text, int alignment, boolean isBold, boolean isHeader) {
        Font font = isBold ?
                new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD) :
                new Font(Font.FontFamily.HELVETICA, 11);

        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(8f);
        cell.setBorder(Rectangle.BOX);

        if (isHeader) {
            cell.setBackgroundColor(new BaseColor(240, 240, 240)); // Light gray background for headers
        }

        return cell;
    }

    // Enhanced amount to words converter
    private String convertToWords(Double amount) {
        if (amount == null) return "______";

        try {
            long rupees = amount.longValue();
            long paise = Math.round((amount - rupees) * 100);

            String rupeesWords = NumberToWords.convert(rupees);
            String paiseWords = paise > 0 ? NumberToWords.convert(paise) : "";

            if (paise > 0) {
                return rupeesWords + " rupees and " + paiseWords + " paise only";
            } else {
                return rupeesWords + " rupees only";
            }
        } catch (Exception e) {
            return String.format("%,.2f", amount) + " rupees only";
        }
    }

    // Number to words converter class
    public static class NumberToWords {
        private static final String[] units = {
                "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"
        };

        private static final String[] tens = {
                "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
        };

        public static String convert(long n) {
            if (n == 0) return "zero";
            if (n < 0) return "minus " + convert(-n);

            String words = "";

            // Crores
            if ((n / 10000000) > 0) {
                words += convert(n / 10000000) + " crore ";
                n %= 10000000;
            }

            // Lakhs
            if ((n / 100000) > 0) {
                words += convert(n / 100000) + " lakh ";
                n %= 100000;
            }

            // Thousands
            if ((n / 1000) > 0) {
                words += convert(n / 1000) + " thousand ";
                n %= 1000;
            }

            // Hundreds
            if ((n / 100) > 0) {
                words += convert(n / 100) + " hundred ";
                n %= 100;
            }

            // Tens and Units
            if (n > 0) {
                if (!words.equals("")) words += "and ";

                if (n < 20) {
                    words += units[(int) n];
                } else {
                    words += tens[(int) (n / 10)];
                    if ((n % 10) > 0) {
                        words += " " + units[(int) (n % 10)];
                    }
                }
            }

            return words.trim();
        }
    }
}