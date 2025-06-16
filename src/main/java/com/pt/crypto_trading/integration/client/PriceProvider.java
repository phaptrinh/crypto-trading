package com.pt.crypto_trading.integration.client;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.enums.PriceSource;

import java.math.BigDecimal;
import java.util.Map;

public interface PriceProvider {
    PriceSource getSource();
    Map<TradingPair, PriceData> fetchPrices();
    boolean isAvailable();
    
    record PriceData(BigDecimal bidPrice, BigDecimal askPrice, PriceSource source) {}
}
