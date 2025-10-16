package com.srllc.aws_textract.domain.exception;

public class ReceiptNotFoundException extends RuntimeException {
    public ReceiptNotFoundException(String message) {
        super(message);
    }
}