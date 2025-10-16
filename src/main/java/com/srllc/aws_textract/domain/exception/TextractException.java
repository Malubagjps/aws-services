package com.srllc.aws_textract.domain.exception;

public class TextractException extends RuntimeException {
    public TextractException(String message, Throwable cause) {
        super(message, cause);
    }
}
