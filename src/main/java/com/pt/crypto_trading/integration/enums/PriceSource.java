package com.pt.crypto_trading.integration.enums;

import lombok.Getter;

@Getter
public enum PriceSource {
    BINANCE("Binance", "https://api.binance.com/api/v3/ticker/bookTicker"),
    HUOBI("Huobi", "https://api.huobi.pro/market/tickers");
    
    private final String name;
    private final String apiUrl;
    
    PriceSource(String name, String apiUrl) {
        this.name = name;
        this.apiUrl = apiUrl;
    }

}