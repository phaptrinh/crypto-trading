package com.pt.crypto_trading.exception;

public class DuplicateRequestException extends RuntimeException {
    public DuplicateRequestException(String message) {
        super(message);
    }
    
    public DuplicateRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
