package com.example.demo.services;

import com.example.demo.dtos.PaymentResponseDTO;
import com.example.demo.exceptions.PaymentValidationException;
import com.example.demo.models.Payment;
import com.example.demo.repositories.PaymentRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
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

    public byte[] generateBillPdf(Long paymentId) {
        PaymentResponseDTO payment = paymentRepository.findByIdWithStudent(paymentId)
                .map(p -> {
                    PaymentResponseDTO dto = new PaymentResponseDTO();
                    dto.setId(p.getId());
                    dto.setStudentId(p.getStudent().getId());
                    dto.setStudentName(p.getStudent().getFullName());
                    dto.setStudentClass(p.getStudent().getStudentClass());
                    dto.setPaymentDate(p.getPaymentDate());
                    dto.setAdmissionFee(p.getAdmissionFee());
                    dto.setMonthlyFee(p.getMonthlyFee());
                    dto.setTransportFee(p.getTransportFee());
                    dto.setOthersFee(p.getOthersFee());
                    dto.setOthersNote(p.getOthersNote());
                    dto.setMonths(p.getMonths());
                    dto.setPreviousDue(p.getPreviousDue());
                    dto.setTotalAmount(p.getTotalAmount());
                    dto.setGrandTotal(p.getGrandTotal());
                    dto.setTotalPaidAmount(p.getTotalPaidAmount());
                    return dto;
                })
                .orElseThrow(() -> new PaymentValidationException("Payment not found"));

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            // Header with centered logo and school info - CENTERED ON PAGE
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(80); // Reduced width to allow centering
            headerTable.setWidths(new float[]{2, 5});

            // Logo cell (left side) - centered vertically
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            logoCell.setPaddingTop(10f);
            logoCell.setPaddingBottom(10f);

            try {
                // Load logo from resources
                InputStream logoStream = new ClassPathResource("static/images/logo.png").getInputStream();
                Image logo = Image.getInstance(logoStream.readAllBytes());
                logo.scaleToFit(120, 120); // Larger size
                logoCell.addElement(logo);
            } catch (Exception e) {
                // If logo not found, add placeholder text
                Paragraph logoPlaceholder = new Paragraph("LOGO",
                        new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.GRAY));
                logoPlaceholder.setAlignment(Element.ALIGN_CENTER);
                logoCell.addElement(logoPlaceholder);
            }

            // School info cell (right side of logo) - centered vertically
            PdfPCell infoCell = new PdfPCell();
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            infoCell.setPaddingTop(10f);
            infoCell.setPaddingBottom(10f);

            Paragraph schoolName = new Paragraph("Wonderkidz Preschool",
                    new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD));
            schoolName.setSpacingAfter(5f);

            Paragraph address = new Paragraph("Tokha-6, Kathmandu",
                    new Font(Font.FontFamily.HELVETICA, 11));
            address.setSpacingAfter(3f);

            Paragraph contact = new Paragraph("Tel: 01-4972224, Email: wonderkidzp@gmail.com",
                    new Font(Font.FontFamily.HELVETICA, 11));

            infoCell.addElement(schoolName);
            infoCell.addElement(address);
            infoCell.addElement(contact);

            headerTable.addCell(logoCell);
            headerTable.addCell(infoCell);


            // CENTER THE ENTIRE HEADER TABLE ON THE PAGE
            PdfPCell centeredCell = new PdfPCell(headerTable);
            centeredCell.setBorder(Rectangle.NO_BORDER);
            centeredCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPTable centeredTable = new PdfPTable(1);
            centeredTable.setWidthPercentage(100);
            centeredTable.addCell(centeredCell);

            document.add(centeredTable);

            document.add(Chunk.NEWLINE);

            // Add straight line after header
            Paragraph line = new Paragraph();
            line.add(new Chunk("\u00A0")); // Non-breaking space
            line.setAlignment(Element.ALIGN_CENTER);

            // Create a line separator
            LineSeparator lineSeparator = new LineSeparator();
            lineSeparator.setLineColor(BaseColor.BLACK);
            lineSeparator.setLineWidth(1f);
            line.add(lineSeparator);

            document.add(line);
            document.add(Chunk.NEWLINE);

            Paragraph invoice = new Paragraph("INVOICE",
                    new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
            invoice.setAlignment(Element.ALIGN_CENTER);
            invoice.setSpacingAfter(10f);
            document.add(invoice);

            // Rest of your existing code remains the same...
            // Invoice details
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            String formattedDate = payment.getPaymentDate() != null
                    ? payment.getPaymentDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                    : "";

            infoTable.addCell(getCell("Bill No: " + payment.getId(), PdfPCell.ALIGN_LEFT));
            infoTable.addCell(getCell("Date: " + formattedDate, PdfPCell.ALIGN_RIGHT));
            infoTable.addCell(getCell("Name: " + payment.getStudentName(), PdfPCell.ALIGN_LEFT));
            infoTable.addCell(getCell("Class: " + payment.getStudentClass(), PdfPCell.ALIGN_RIGHT));

            String months = payment.getMonths().isEmpty() ? "-" : String.join(", ", payment.getMonths());
            PdfPCell monthCell = new PdfPCell(new Phrase("Payment for the Month of: " + months));
            monthCell.setColspan(2);
            monthCell.setBorder(Rectangle.NO_BORDER);
            monthCell.setPaddingTop(5f);
            infoTable.addCell(monthCell);

            document.add(infoTable);

            document.add(Chunk.NEWLINE);

            // Fee Table
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 6, 3});

            table.addCell(headerCell("S.N."));
            table.addCell(headerCell("Particular"));
            table.addCell(headerCell("Amount"));

            int sn = 1;
            table.addCell(valueCell(String.valueOf(sn++)));
            table.addCell(valueCellLeft("Admission Fee (Registration)"));
            table.addCell(valueCell(payment.getAdmissionFee() > 0 ? payment.getAdmissionFee() + "/-" : "-"));

            table.addCell(valueCell(String.valueOf(sn++)));
            table.addCell(valueCellLeft("Monthly Fee (Tuition fee including meals)"));
            table.addCell(valueCell(payment.getMonthlyFee() > 0 ? payment.getMonthlyFee() + "/-" : "-"));

            table.addCell(valueCell(String.valueOf(sn++)));
            table.addCell(valueCellLeft("Transportation (Door to Door)"));
            table.addCell(valueCell(payment.getTransportFee() > 0 ? payment.getTransportFee() + "/-" : "-"));

            table.addCell(valueCell(String.valueOf(sn++)));
            table.addCell(valueCellLeft(payment.getOthersNote() != null && !payment.getOthersNote().isEmpty() ? payment.getOthersNote() : "Others"));
            table.addCell(valueCell(payment.getOthersFee() > 0 ? payment.getOthersFee() + "/-" : "-"));

            // Totals
            table.addCell(valueCell(" "));
            table.addCell(valueCellLeft("Total Amount: "));
            table.addCell(valueCell(payment.getTotalAmount() + "/-"));

            table.addCell(valueCell(" "));
            table.addCell(valueCellLeft("Previous Due: "));
            table.addCell(valueCell(payment.getPreviousDue() + "/-"));

            table.addCell(valueCell(" "));
            table.addCell(valueCellLeft("Grand Total: "));
            table.addCell(valueCell(payment.getGrandTotal() + "/-"));

//            table.addCell(valueCell(" "));
//            table.addCell(valueCellLeftBold("Paid Amount: "));
//            table.addCell(valueCellBold(payment.getTotalPaidAmount() + "/-"));

//            table.addCell(valueCellLeft("Paid Amount: "));
//            table.addCell(valueCell(payment.getTotalPaidAmount() + "/-"));

            double dueLeft = payment.getGrandTotal() - payment.getTotalPaidAmount();
            if (dueLeft > 0) {
                table.addCell(valueCell(" "));
                table.addCell(valueCellLeft("Due Left: "));
                table.addCell(valueCell(dueLeft + "/-"));
            }

            // Optional: add empty row for spacing
            table.addCell(emptyCell(3));

            // Amount in words
            String amountInWords = "Amount in Words: " + numberToWords(payment.getTotalPaidAmount()) + " only";
            PdfPCell wordsCell = new PdfPCell(new Phrase(amountInWords,
                    new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));
            wordsCell.setColspan(3);
            wordsCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            wordsCell.setPadding(5f);
            table.addCell(wordsCell);
            document.add(table);

            document.add(Chunk.NEWLINE);

            // Signature section - Account Officer text below signature picture
            PdfPTable signatureTable = new PdfPTable(2);
            signatureTable.setWidthPercentage(100);
            signatureTable.setWidths(new float[]{7, 3});

// Left side - Empty space
            PdfPCell emptyCell = new PdfPCell(new Paragraph(""));
            emptyCell.setBorder(Rectangle.NO_BORDER);
            emptyCell.setHorizontalAlignment(Element.ALIGN_LEFT);

// Right side - Signature with Account Officer text below
            PdfPCell signatureCell = new PdfPCell();
            signatureCell.setBorder(Rectangle.NO_BORDER);
            signatureCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

// Add signature image first
            try {
                // Load signature from resources
                InputStream signatureStream = new ClassPathResource("static/images/signature.jpeg").getInputStream();
                Image signature = Image.getInstance(signatureStream.readAllBytes());
                signature.scaleToFit(100, 40);
                signature.setAlignment(Element.ALIGN_RIGHT);
                signatureCell.addElement(signature);
            } catch (Exception e) {
                // If signature not found, add placeholder text
                Paragraph signaturePlaceholder = new Paragraph("[Signature]",
                        new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
                signaturePlaceholder.setAlignment(Element.ALIGN_RIGHT);
                signatureCell.addElement(signaturePlaceholder);
            }

// Add Account Officer text below the signature
            Paragraph officerText = new Paragraph("Account Officer",
                    new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD));
            officerText.setAlignment(Element.ALIGN_RIGHT);
            officerText.setSpacingBefore(5f); // Add some space between signature and text
            signatureCell.addElement(officerText);

            signatureTable.addCell(emptyCell);
            signatureTable.addCell(signatureCell);
            document.add(signatureTable);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating bill PDF", e);
        }
    }

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
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell emptyCell(int colspan) {
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setColspan(colspan);
        return cell;
    }

    private PdfPCell valueCellLeft(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell valueCellLeftBold(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell valueCellBold(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD)));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(5f);
        return cell;
    }

    public String generateBillFileName(Long paymentId) {
        Payment payment = paymentRepository.findByIdWithStudent(paymentId).orElseThrow(() -> new PaymentValidationException("Payment not found"));
        // Get first name
        String firstName = payment.getStudent().getFullName().split(" ")[0];

        // Convert months to 3-letter abbreviations
        String monthsPart;
        if (payment.getMonths() == null || payment.getMonths().isEmpty()) {
            monthsPart = "NA";
        } else {
            monthsPart = String.join("_", payment.getMonths());
        }

        // Replace any special characters to make filename safe
        firstName = firstName.replaceAll("[^a-zA-Z0-9]", "_");
        monthsPart = monthsPart.replaceAll("[^a-zA-Z0-9_]", "_");

        return firstName + "_" + monthsPart;
    }

    public String numberToWords(double num) {
        // Implementation of number to words conversion
        // (You can use the same logic from the previous Node.js implementation)
        String[] words = {"", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
        String[] tens = {"", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};

        int n = (int) num;
        if (n == 0) return "Zero rupees only";
        if (n < 20) return words[n] + " rupees only";
        if (n < 100) {
            return tens[n / 10] + (n % 10 != 0 ? " " + words[n % 10] : "") + " rupees only";
        }
        if (n < 1000) {
            return words[n / 100] + " Hundred" + (n % 100 != 0 ? " " + numberToWords(n % 100) : " rupees only");
        }
        if (n < 100000) {
            return numberToWords(n / 1000) + " Thousand" + (n % 1000 != 0 ? " " + numberToWords(n % 1000) : " rupees only");
        }
        return "Number too large";
    }
}