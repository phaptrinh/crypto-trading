package com.pt.crypto_trading.service.impl;

import com.pt.crypto_trading.domain.entity.BestPrice;
import com.pt.crypto_trading.domain.entity.PriceHistory;
import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.client.PriceProvider;
import com.pt.crypto_trading.repository.BestPriceRepository;
import com.pt.crypto_trading.repository.PriceHistoryRepository;
import com.pt.crypto_trading.service.PriceAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceAggregationServiceImpl implements PriceAggregationService {
    
    private final List<PriceProvider> priceProviders;
    private final BestPriceRepository bestPriceRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    
    @Override
    @Transactional
    @CacheEvict(value = {"prices", "allPrices", "executionPrices"}, allEntries = true)
    public void aggregateAndUpdatePrices() {
        log.debug("Starting price aggregation from {} providers and clearing cache", priceProviders.size());
        
        try {
            List<Map<TradingPair, PriceProvider.PriceData>> pricesByProvider = priceProviders.stream()
                    .parallel()
                    .map(this::fetchPricesWithFallback)
                    .filter(prices -> !prices.isEmpty())
                    .collect(Collectors.toList());
            
            if (pricesByProvider.isEmpty()) {
                log.warn("No price data available from any provider");
                return;
            }
            
            Map<TradingPair, BestPriceResult> bestPrices = aggregatePrices(pricesByProvider);
            
            updateBestPricesInDatabase(bestPrices);
            
            savePriceHistory(pricesByProvider);
            
            log.info("Successfully aggregated prices for {} trading pairs", bestPrices.size());
            
        } catch (Exception e) {
            log.error("Error during price aggregation", e);
            throw new RuntimeException("Price aggregation failed", e);
        }
    }
    
    @Override
    public Map<TradingPair, BestPriceResult> aggregatePrices(List<Map<TradingPair, PriceProvider.PriceData>> pricesByProvider) {
        return TradingPair.getAllValues().stream()
                .collect(Collectors.toMap(
                        tradingPair -> tradingPair,
                        tradingPair -> findBestPricesForPair(tradingPair, pricesByProvider)
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().bestBid() != null || entry.getValue().bestAsk() != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ));
    }
    
    private Map<TradingPair, PriceProvider.PriceData> fetchPricesWithFallback(PriceProvider provider) {
        try {
            if (!provider.isAvailable()) {
                log.warn("Provider {} is not available", provider.getSource());
                return Map.of();
            }
            
            Map<TradingPair, PriceProvider.PriceData> prices = provider.fetchPrices();
            log.debug("Fetched {} prices from {}", prices.size(), provider.getSource());
            return prices;
            
        } catch (Exception e) {
            log.error("Failed to fetch prices from provider {}: {}", provider.getSource(), e.getMessage());
            return Map.of();
        }
    }
    
    private BestPriceResult findBestPricesForPair(TradingPair tradingPair, 
                                                  List<Map<TradingPair, PriceProvider.PriceData>> pricesByProvider) {
        
        PriceProvider.PriceData bestBid = null;
        PriceProvider.PriceData bestAsk = null;
        
        for (Map<TradingPair, PriceProvider.PriceData> providerPrices : pricesByProvider) {
            PriceProvider.PriceData priceData = providerPrices.get(tradingPair);
            if (priceData == null) continue;
            
            if (bestBid == null || priceData.bidPrice().compareTo(bestBid.bidPrice()) > 0) {
                bestBid = priceData;
            }
            
            if (bestAsk == null || priceData.askPrice().compareTo(bestAsk.askPrice()) < 0) {
                bestAsk = priceData;
            }
        }
        
        return new BestPriceResult(bestBid, bestAsk);
    }
    
    private void updateBestPricesInDatabase(Map<TradingPair, BestPriceResult> bestPrices) {
        for (Map.Entry<TradingPair, BestPriceResult> entry : bestPrices.entrySet()) {
            TradingPair tradingPair = entry.getKey();
            BestPriceResult result = entry.getValue();
            
            updateBestPriceWithRetry(tradingPair, result);
        }
    }
    
    @Retryable(
        value = {ObjectOptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    private void updateBestPriceWithRetry(TradingPair tradingPair, BestPriceResult result) {
        try {
            Optional<BestPrice> existingBestPrice = bestPriceRepository.findByTradingPair(tradingPair);
            BestPrice bestPrice = existingBestPrice.orElse(new BestPrice(tradingPair));
            
            boolean shouldUpdate = false;
            
            if (result.bestBid() != null) {
                if (bestPrice.getBestBidPrice() == null || 
                    result.bestBid().bidPrice().compareTo(bestPrice.getBestBidPrice()) > 0) {
                    bestPrice.setBestBidPrice(result.bestBid().bidPrice());
                    bestPrice.setBidSource(result.bestBid().source());
                    shouldUpdate = true;
                }
            }
            
            if (result.bestAsk() != null) {
                if (bestPrice.getBestAskPrice() == null || 
                    result.bestAsk().askPrice().compareTo(bestPrice.getBestAskPrice()) < 0) {
                    bestPrice.setBestAskPrice(result.bestAsk().askPrice());
                    bestPrice.setAskSource(result.bestAsk().source());
                    shouldUpdate = true;
                }
            }
            
            if (shouldUpdate) {
                bestPriceRepository.save(bestPrice);
                log.debug("Updated best price for {}: bid={} ({}), ask={} ({}), version={}", 
                        tradingPair,
                        result.bestBid() != null ? result.bestBid().bidPrice() : null,
                        result.bestBid() != null ? result.bestBid().source() : null,
                        result.bestAsk() != null ? result.bestAsk().askPrice() : null,
                        result.bestAsk() != null ? result.bestAsk().source() : null,
                        bestPrice.getVersion());
            } else {
                log.debug("Skipped update for {} - no better prices available", tradingPair);
            }
            
        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking conflict for {}, retrying...", tradingPair);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update best price for {}: {}", tradingPair, e.getMessage(), e);
        }
    }
    
    private void savePriceHistory(List<Map<TradingPair, PriceProvider.PriceData>> pricesByProvider) {
        List<PriceHistory> histories = pricesByProvider.stream()
                .flatMap(providerPrices -> providerPrices.entrySet().stream())
                .map(entry -> new PriceHistory(
                        entry.getKey(),
                        entry.getValue().source(),
                        entry.getValue().bidPrice(),
                        entry.getValue().askPrice()
                ))
                .collect(Collectors.toList());
                
        priceHistoryRepository.saveAll(histories);
        log.debug("Saved {} price history records", histories.size());
    }
}
