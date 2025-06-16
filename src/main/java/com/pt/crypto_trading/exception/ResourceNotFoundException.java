package com.pt.crypto_trading.exception;

public class ResourceNotFoundException extends CryptoTradingException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
