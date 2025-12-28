package com.example.demo.services.pdf;

import com.example.demo.constants.DateFormat;
import com.example.demo.exceptions.PaymentValidationException;
import com.example.demo.exceptions.PdfGenerationFailedException;
import com.example.demo.models.Invoice;
import com.example.demo.repositories.PaymentRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * @author diwash
 * @created 12/20/25
 */


@Service
@RequiredArgsConstructor
public class InvoicePdfService extends BasePdfService {

    private final PaymentRepository paymentRepository;

    public byte[] generateInvoicePdf(Long paymentId) {
        Invoice invoice = paymentRepository.findByIdWithStudent(paymentId)
                .orElseThrow(() -> new PaymentValidationException("Payment not found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = createDocument(out);

            // Header
            document.add(createHeader());
            addEmptyLine(document, 1);

            // Title
            document.add(createTitle());

            // Info table
            document.add(createInvoiceInfoTable(invoice));
            addEmptyLine(document, 1);

            // Fee table
            document.add(createFeeTable(invoice));

            // Signature
            addEmptyLine(document, 1);
            document.add(createSignatureTable());

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new PdfGenerationFailedException("Error generating invoice PDF");
        }
    }

    private Paragraph createTitle() {
        Paragraph title = new Paragraph("INVOICE",
                new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10f);
        return title;
    }

    private PdfPTable createInvoiceInfoTable(Invoice invoice) {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);

        String formattedDate = getFormattedDate(invoice.getPaymentDate(),
                DateFormat.DATE_FORMAT_DD_MMM_YYYY);

        infoTable.addCell(createCell("Bill No: " + invoice.getInvoiceNo(),
                Element.ALIGN_LEFT, Rectangle.NO_BORDER));
        infoTable.addCell(createCell("Date: " + formattedDate,
                Element.ALIGN_RIGHT, Rectangle.NO_BORDER));

        infoTable.addCell(createCell("Name: " + invoice.getStudent().getFullName(),
                Element.ALIGN_LEFT, Rectangle.NO_BORDER));
        infoTable.addCell(createCell("Class: " + invoice.getStudent().getStudentClass(),
                Element.ALIGN_RIGHT, Rectangle.NO_BORDER));

        String months = invoice.getMonths() == null || invoice.getMonths().isEmpty()
                ? "-" : String.join(", ", invoice.getMonths());
        PdfPCell monthCell = new PdfPCell(new Phrase("Payment for the Month of: " + months));
        monthCell.setColspan(2);
        monthCell.setBorder(Rectangle.NO_BORDER);
        monthCell.setPaddingTop(5f);
        infoTable.addCell(monthCell);

        return infoTable;
    }

    private PdfPTable createFeeTable(Invoice invoice) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 6, 3});

        // Header
        table.addCell(createHeaderCell("S.N."));
        table.addCell(createHeaderCell("Particular"));
        table.addCell(createHeaderCell("Amount"));

        // Rows
        int sn = 1;
        addFeeRow(table, sn++, "Admission Fee", invoice.getAdmissionFee());
        addFeeRow(table, sn++, "Monthly Fee", invoice.getMonthlyFee());
        addFeeRow(table, sn++, "Transport Fee", invoice.getTransportFee());

        String othersNote = "Others";
        if (invoice.getOthersNote() != null && !invoice.getOthersNote().trim().isEmpty()) {
            othersNote = "Others (" + invoice.getOthersNote() + ")";
        }
        addFeeRow(table, sn++, othersNote, invoice.getOthersFee());

        // Total rows
        addTotalRow(table, "Total Amount", invoice.getTotalAmount());
        addTotalRow(table, "Previous Due", invoice.getPreviousDue());
        addTotalRow(table, "Grand Total", invoice.getGrandTotal());

        return table;
    }

    private PdfPCell createHeaderCell(String text) {
        return createCell(text, LABEL_FONT, Element.ALIGN_CENTER, Rectangle.BOX);
    }

    private void addFeeRow(PdfPTable table, int sn, String label, Double amount) {
        table.addCell(createCell(String.valueOf(sn), Element.ALIGN_CENTER, Rectangle.BOX));
        table.addCell(createCell(label, Element.ALIGN_LEFT, Rectangle.BOX));
        table.addCell(createCell(formatAmount(amount), Element.ALIGN_CENTER, Rectangle.BOX));
    }

    private void addTotalRow(PdfPTable table, String label, Double amount) {
        table.addCell(createCell(" ", Element.ALIGN_CENTER, Rectangle.BOX));
        table.addCell(createCell(label, LABEL_FONT, Element.ALIGN_LEFT, Rectangle.BOX));
        table.addCell(createCell(formatAmount(amount), LABEL_FONT, Element.ALIGN_CENTER, Rectangle.BOX));
    }
}
