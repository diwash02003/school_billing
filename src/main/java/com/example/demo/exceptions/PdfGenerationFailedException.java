package com.example.demo.exceptions;

/**
 * @author diwash
 * @created 12/20/25
 */
public class PdfGenerationFailedException extends RuntimeException {
    public PdfGenerationFailedException(String message) {
        super(message);
    }
}
