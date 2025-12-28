package com.example.demo.services.pdf;

import com.example.demo.constants.DateFormat;
import com.example.demo.exceptions.PaymentValidationException;
import com.example.demo.exceptions.PdfGenerationFailedException;
import com.example.demo.models.Receipt;
import com.example.demo.repositories.ReceiptRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

/**
 * @author diwash
 * @created 12/20/25
 */


@Service
@RequiredArgsConstructor
public class ReceiptPdfService extends BasePdfService {

    private final ReceiptRepository receiptRepository;

    public byte[] generateReceiptPdf(Long receiptId) {
        Receipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new PaymentValidationException("Receipt not found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = createDocument(out);

            // Header
            document.add(createHeader());
            addEmptyLine(document, 1);

            // Title
            document.add(createTitle());

            // Meta information
            document.add(createMetaTable(receipt));
            document.add(createHorizontalLine());

            // Received from section
            document.add(createReceivedFromSection(receipt));

            // O/A and Class info
            document.add(createInfoTable(receipt));

            // Balance table
            document.add(createBalanceTable(receipt));

            // Footer information
            document.add(createFooterTable(receipt));

            // Remarks
            addRemarks(document, receipt);

            // Disclaimer
            document.add(createDisclaimer());

            // Signature
            addEmptyLine(document, 1);
            document.add(createSignatureTable());

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new PdfGenerationFailedException("Error generating receipt PDF");
        }
    }

    private Paragraph createTitle() {
        Paragraph title = new Paragraph("RECEIPT", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20f);
        return title;
    }

    private PdfPTable createMetaTable(Receipt receipt) throws DocumentException {
        PdfPTable metaTable = new PdfPTable(2);
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{1, 1});
        metaTable.setSpacingAfter(4f);

        // Left cell - Receipt No
        metaTable.addCell(createSimpleCell(
                "Receipt No: " + (receipt.getReceiptNo() != null ? receipt.getReceiptNo() : PLACE_HOLDER_NOT_AVAILABLE),
                Element.ALIGN_LEFT,
                true
        ));

        // Right cell - Date
        String formattedDate = getFormattedDate(receipt.getReceiptDate(), DateFormat.DATE_FORMAT_DD_MM_YYYY);
        metaTable.addCell(createSimpleCell("Date: " + formattedDate, Element.ALIGN_RIGHT, true));

        return metaTable;
    }

    private LineSeparator createHorizontalLine() {
        LineSeparator line = new LineSeparator();
        line.setLineWidth(1.2f);
        line.setPercentage(100);
        line.setLineColor(BaseColor.BLACK);
        return line;
    }

    private Paragraph createReceivedFromSection(Receipt receipt) {
        Paragraph paragraph = new Paragraph();
        paragraph.setAlignment(Element.ALIGN_LEFT);
        paragraph.setSpacingBefore(15f);
        paragraph.setSpacingAfter(20f);

        // Received with thanks from
        paragraph.add(new Phrase("Received with thanks from: ", LABEL_FONT));

        // Student name
        paragraph.add(createUnderlinedPhrase("  " + receipt.getStudent().getFullName() + "  "));

        // The sum of
        paragraph.add(new Phrase("the sum of: ", LABEL_FONT));

        // Amount
        String amountText = receipt.getPaidAmount() != null ?
                String.format("%,.2f", receipt.getPaidAmount()) : PLACE_HOLDER_NOT_AVAILABLE;
        paragraph.add(createUnderlinedPhrase("  Rs. " + amountText + "  "));

        // In words
        paragraph.add(new Phrase("in words: ", LABEL_FONT));

        // Amount in words
        paragraph.add(createUnderlinedPhrase("  " + getAmountInWords(receipt.getPaidAmount()) + "  "));
        paragraph.add(new Phrase(".", NORMAL_FONT));

        return paragraph;
    }

    private Phrase createUnderlinedPhrase(String text) {
        return new Phrase(text, NORMAL_FONT_WITH_UNDERLINE);
    }

    private PdfPTable createInfoTable(Receipt receipt) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});

        infoTable.addCell(createSimpleCell(
                "O/A of " + (receipt.getReceiptNo() != null ? receipt.getReceiptNo() : PLACE_HOLDER_NOT_AVAILABLE),
                Element.ALIGN_LEFT,
                false
        ));

        infoTable.addCell(createSimpleCell("Class: " + receipt.getStudent().getStudentClass(), Element.ALIGN_RIGHT, true
        ));

        infoTable.setSpacingAfter(15f);
        return infoTable;
    }

    private PdfPTable createBalanceTable(Receipt receipt) throws DocumentException {
        PdfPTable balanceTable = new PdfPTable(3);
        balanceTable.setWidthPercentage(100);
        balanceTable.setWidths(new float[]{1, 1, 1});

        // Header row
        balanceTable.addCell(createBorderedCell("PRE BALANCE", true, true));
        balanceTable.addCell(createBorderedCell("AMOUNT PAID", true, true));
        balanceTable.addCell(createBorderedCell("BALANCE DUE", true, true));

        // Data row
        balanceTable.addCell(createBorderedCell(
                "Rs. " + formatAmount(receipt.getPreviousDueSnapshot()),
                false,
                false
        ));

        balanceTable.addCell(createBorderedCell(
                "Rs. " + formatAmount(receipt.getPaidAmount()),
                false,
                false
        ));

        balanceTable.addCell(createBorderedCell(
                "Rs. " + formatAmount(receipt.getRemainingDue()),
                false,
                false
        ));

        balanceTable.setSpacingAfter(20f);
        return balanceTable;
    }

    private PdfPTable createFooterTable(Receipt receipt) throws DocumentException {
        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setWidths(new float[]{1, 1});

        String monthYear = getFormattedDate(receipt.getReceiptDate(),
                DateFormat.DATE_FORMAT_MONTH_YEAR);
        String paymentMethod = receipt.getPaymentMethod() != null ?
                receipt.getPaymentMethod() : PLACE_HOLDER_NOT_AVAILABLE;

        footerTable.addCell(createSimpleCell(
                "Month: " + monthYear,
                Element.ALIGN_LEFT,
                false
        ));

        footerTable.addCell(createSimpleCell(
                "Payment Method: " + paymentMethod,
                Element.ALIGN_RIGHT,
                false
        ));

        footerTable.setSpacingAfter(10f);
        return footerTable;
    }

    private void addRemarks(Document document, Receipt receipt) throws DocumentException {
        if (receipt.getRemarks() != null && !receipt.getRemarks().trim().isEmpty()) {
            Paragraph remarksPara = new Paragraph("Remarks: " + receipt.getRemarks(), SMALL_ITALIC);
            remarksPara.setAlignment(Element.ALIGN_LEFT);
            remarksPara.setSpacingAfter(10f);
            document.add(remarksPara);
        }
    }

    private Paragraph createDisclaimer() {
        Paragraph disclaimer = new Paragraph(
                "Note: Fees once paid are not refundable.",
                new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.GRAY)
        );
        disclaimer.setAlignment(Element.ALIGN_CENTER);
        disclaimer.setSpacingBefore(30f);
        return disclaimer;
    }

    private PdfPCell createSimpleCell(String text, int alignment, boolean isBold) {
        Font font = isBold ? LABEL_FONT : NORMAL_FONT;
        return createCell(text, font, alignment, Rectangle.NO_BORDER);
    }

    private PdfPCell createBorderedCell(String text, boolean isBold, boolean isHeader) {
        Font font = isBold ? LABEL_FONT : NORMAL_FONT;
        PdfPCell cell = createCell(text, font, Element.ALIGN_CENTER, Rectangle.BOX);

        if (isHeader) {
            cell.setBackgroundColor(new BaseColor(240, 240, 240));
        }

        cell.setPadding(8f);
        return cell;
    }
}
