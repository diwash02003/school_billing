package com.example.demo.services.pdf;

import com.example.demo.utils.NumberToWords;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

/**
 * @author diwash
 * @created 12/20/25
 */

@RequiredArgsConstructor
public abstract class BasePdfService implements PdfGenerator {

    protected static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    protected static final Font LABEL_FONT = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
    protected static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 11);
    protected static final Font NORMAL_FONT_WITH_UNDERLINE = new Font(Font.FontFamily.HELVETICA, 11, Font.UNDERLINE);
    protected static final Font SMALL_ITALIC = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);

    protected static final String PLACE_HOLDER_NOT_AVAILABLE = "______";

    @Override
    public Document createDocument(ByteArrayOutputStream out) throws DocumentException {
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        PdfWriter.getInstance(document, out);
        document.open();
        return document;
    }

    @Override
    public PdfPTable createHeader() throws Exception {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(80);
        headerTable.setWidths(new float[]{2, 5});
        headerTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        // Logo cell
        PdfPCell logoCell = createLogoCell();

        // School info cell
        PdfPCell infoCell = createInfoCell();

        headerTable.addCell(logoCell);
        headerTable.addCell(infoCell);

        return headerTable;
    }

    private PdfPCell createLogoCell() {
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingRight(10f);

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

        return logoCell;
    }

    private PdfPCell createInfoCell() {
        PdfPCell infoCell = new PdfPCell();
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        infoCell.setPaddingLeft(10f);

        PdfPTable nested = new PdfPTable(1);
        nested.setWidthPercentage(100);

        addInfoLine(nested, "Wonderkidz Preschool", TITLE_FONT);
        addInfoLine(nested, "Tokha-6, Kathmandu", NORMAL_FONT);
        addInfoLine(nested, "Tel: 01-4972224, Email: wonderkidzp@gmail.com", NORMAL_FONT);

        infoCell.addElement(nested);
        return infoCell;
    }

    private void addInfoLine(PdfPTable table, String text, Font font) {
        Paragraph paragraph = new Paragraph(text, font);
        PdfPCell cell = new PdfPCell(paragraph);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }

    @Override
    public PdfPTable createSignatureTable() throws DocumentException {
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setWidths(new float[]{7, 3});
        signatureTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);


        // Left cell - empty, NO BORDER
        PdfPCell emptyCell = new PdfPCell(new Paragraph(""));
        emptyCell.setBorder(Rectangle.NO_BORDER);
        signatureTable.addCell(emptyCell);

        // Right cell - signature
        PdfPCell signatureCell = createSignatureCell();
        signatureCell.setBorder(Rectangle.NO_BORDER);

        signatureTable.addCell(signatureCell);

        return signatureTable;
    }

    private PdfPCell createSignatureCell() {
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
            Paragraph placeholder = new Paragraph("[Signature]",
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC));
            placeholder.setAlignment(Element.ALIGN_RIGHT);
            signatureCell.addElement(placeholder);
        }

        Paragraph officerText = new Paragraph("Account Officer",
                new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD));
        officerText.setAlignment(Element.ALIGN_RIGHT);
        officerText.setSpacingBefore(5f);
        signatureCell.addElement(officerText);

        return signatureCell;
    }

    // Common utility methods
    protected PdfPCell createCell(String text, Font font, int alignment, int border) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        cell.setPadding(5f);
        return cell;
    }

    protected PdfPCell createCell(String text, int alignment, int border) {
        return createCell(text, NORMAL_FONT, alignment, border);
    }

    protected String formatAmount(Double amount) {
        return amount != null ? String.format("%,.2f", amount) : "0.00";
    }

    protected String getFormattedDate(java.time.LocalDate date, String pattern) {
        return date != null ? date.format(DateTimeFormatter.ofPattern(pattern)) : PLACE_HOLDER_NOT_AVAILABLE;
    }

    protected String getAmountInWords(Double amount) {
        return NumberToWords.convertAmountToWords(amount);
    }

    protected void addEmptyLine(Document document, int count) throws DocumentException {
        for (int i = 0; i < count; i++) {
            document.add(Chunk.NEWLINE);
        }
    }
}