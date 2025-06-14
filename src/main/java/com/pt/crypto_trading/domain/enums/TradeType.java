package com.pt.crypto_trading.domain.enums;

import lombok.Getter;

@Getter
public enum TradeType {
    BUY("BUY", "Buy Order"),
    SELL("SELL", "Sell Order");
    
    private final String code;
    private final String description;
    
    TradeType(String code, String description) {
        this.code = code;
        this.description = description;
    }

}
