package com.pt.crypto_trading.domain.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Currency {
    USDT("USDT", "Tether", 2),
    BTC("BTC", "Bitcoin", 8),
    ETH("ETH", "Ethereum", 8);
    
    private final String code;
    private final String name;
    private final int decimalPlaces;
    
    Currency(String code, String name, int decimalPlaces) {
        this.code = code;
        this.name = name;
        this.decimalPlaces = decimalPlaces;
    }

    public static Currency fromCode(String code) {
        return Arrays.stream(values())
            .filter(c -> c.code.equals(code))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown currency: " + code));
    }
}