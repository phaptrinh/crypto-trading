package com.pt.crypto_trading.service;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.client.PriceProvider;

import java.util.List;
import java.util.Map;

public interface PriceAggregationService {
    void aggregateAndUpdatePrices();
    Map<TradingPair, BestPriceResult> aggregatePrices(List<Map<TradingPair, PriceProvider.PriceData>> pricesByProvider);
    
    record BestPriceResult(
        PriceProvider.PriceData bestBid,
        PriceProvider.PriceData bestAsk
    ) {}
}
