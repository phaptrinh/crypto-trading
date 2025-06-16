package com.pt.crypto_trading.domain.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@Getter
public enum TradingPair {
    BTCUSDT("BTCUSDT", Currency.BTC, Currency.USDT, new BigDecimal("0.00001")),
    ETHUSDT("ETHUSDT", Currency.ETH, Currency.USDT, new BigDecimal("0.0001"));
    
    private final String symbol;
    private final Currency baseCurrency;
    private final Currency quoteCurrency;
    private final BigDecimal minTradeAmount;
    
    TradingPair(String symbol, Currency baseCurrency, Currency quoteCurrency, BigDecimal minTradeAmount) {
        this.symbol = symbol;
        this.baseCurrency = baseCurrency;
        this.quoteCurrency = quoteCurrency;
        this.minTradeAmount = minTradeAmount;
    }

    public static TradingPair fromSymbol(String symbol) {
        return Arrays.stream(values())
            .filter(tp -> tp.symbol.equals(symbol))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown trading pair: " + symbol));
    }

    public static List<TradingPair> getAllValues() {
        return List.of(values());
    }
}
