package com.pt.crypto_trading.dto;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.domain.enums.TradeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeRequestDto {
    @NotNull(message = "Trading pair is required")
    private TradingPair tradingPair;
    
    @NotNull(message = "Trade type is required")
    private TradeType type;
    
    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.00000001", message = "Quantity must be greater than 0")
    @Digits(integer = 10, fraction = 8, message = "Invalid quantity precision")
    private BigDecimal quantity;
    
    @Size(max = 64, message = "Idempotency key must not exceed 64 characters")
    private String idempotencyKey;
    
    // Add validation method for production readiness
    public void validateBusinessRules() {
        if (tradingPair != TradingPair.ETHUSDT && tradingPair != TradingPair.BTCUSDT) {
            throw new IllegalArgumentException("Only ETHUSDT and BTCUSDT trading pairs are supported");
        }
    }
}
