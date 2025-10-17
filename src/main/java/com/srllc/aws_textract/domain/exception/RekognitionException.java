package com.srllc.aws_textract.domain.exception;

public class RekognitionException extends RuntimeException {
    public RekognitionException(String message, Throwable cause) {
        super(message, cause);
    }
}