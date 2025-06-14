package com.pt.crypto_trading.domain.enums;

import lombok.Getter;

@Getter
public enum TradeStatus {
    PENDING("PENDING", "Order Pending"),
    COMPLETED("COMPLETED", "Order Completed"), 
    FAILED("FAILED", "Order Failed"),
    CANCELLED("CANCELLED", "Order Cancelled");
    
    private final String code;
    private final String description;
    
    TradeStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED;
    }
}