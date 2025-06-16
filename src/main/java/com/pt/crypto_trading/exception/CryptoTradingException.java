package com.pt.crypto_trading.exception;

public class CryptoTradingException extends RuntimeException {
    
    public CryptoTradingException(String message) {
        super(message);
    }
    
    public CryptoTradingException(String message, Throwable cause) {
        super(message, cause);
    }
}
