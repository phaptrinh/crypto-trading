package com.pt.crypto_trading.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledPriceUpdateService {
    
    private final PriceAggregationService priceAggregationService;
    
    @Scheduled(fixedRate = 10000)
    public void updatePrices() {
        try {
            log.debug("Starting scheduled price update");
            priceAggregationService.aggregateAndUpdatePrices();
        } catch (Exception e) {
            log.error("Scheduled price update failed", e);
        }
    }
}
