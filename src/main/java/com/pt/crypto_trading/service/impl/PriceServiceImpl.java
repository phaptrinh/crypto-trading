package com.pt.crypto_trading.service.impl;

import com.pt.crypto_trading.domain.entity.BestPrice;
import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.domain.enums.TradeType;
import com.pt.crypto_trading.dto.PriceDto;
import com.pt.crypto_trading.exception.ResourceNotFoundException;
import com.pt.crypto_trading.repository.BestPriceRepository;
import com.pt.crypto_trading.service.PriceService;
import com.pt.crypto_trading.service.PriceAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceServiceImpl implements PriceService {
    
    private final BestPriceRepository bestPriceRepository;
    private final PriceAggregationService priceAggregationService;
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "allPrices", unless = "#result.isEmpty()")
    public List<PriceDto> getAllLatestPrices() {
        log.debug("Fetching all latest prices from database");
        
        return Arrays.stream(TradingPair.values())
                .map(this::getLatestPriceFromDatabase)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "prices", key = "#tradingPair", unless = "#result == null")
    public PriceDto getLatestPrice(TradingPair tradingPair) {
        log.debug("Fetching latest price for: {} from database", tradingPair);
        return getLatestPriceFromDatabase(tradingPair);
    }
    
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "executionPrices", key = "#tradingPair + '_' + #tradeType", unless = "#result == null")
    public BigDecimal getExecutionPrice(TradingPair tradingPair, TradeType tradeType) {
        log.debug("Getting execution price for {} {} from database", tradeType, tradingPair);
        
        BestPrice bestPrice = bestPriceRepository.findByTradingPair(tradingPair)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found for trading pair: " + tradingPair));
        
        if (!bestPrice.isValidPrice()) {
            throw new IllegalStateException("Invalid price data for trading pair: " + tradingPair);
        }
        
        return bestPrice.getPriceForTradeType(tradeType);
    }

    private PriceDto getLatestPriceFromDatabase(TradingPair tradingPair) {
        BestPrice bestPrice = bestPriceRepository.findByTradingPair(tradingPair)
                .orElseThrow(() -> new ResourceNotFoundException("Price not found for trading pair: " + tradingPair));
        
        return new PriceDto(
                bestPrice.getTradingPair(),
                bestPrice.getBestBidPrice(),
                bestPrice.getBestAskPrice(),
                bestPrice.getBidSource(),
                bestPrice.getAskSource(),
                bestPrice.getUpdatedAt()
        );
    }
}
