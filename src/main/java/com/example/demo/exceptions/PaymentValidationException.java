package com.example.demo.exceptions;

/**
 * @author diwash
 * @date 10/4/25
 * @description This file contains...
 */

public class PaymentValidationException extends RuntimeException {
    public PaymentValidationException(String message) {
        super(message);
    }
}