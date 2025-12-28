package com.example.demo.services.pdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfPTable;

import java.io.ByteArrayOutputStream;

/**
 * @author diwash
 * @created 12/20/25
 */
public interface PdfGenerator {
    PdfPTable createHeader() throws Exception;

    PdfPTable createSignatureTable() throws DocumentException;

    Document createDocument(ByteArrayOutputStream out) throws DocumentException;
}
