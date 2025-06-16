package com.pt.crypto_trading.integration.client;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.dto.HuobiPriceDto;
import com.pt.crypto_trading.integration.enums.PriceSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class HuobiClient implements PriceProvider {
    
    private final RestTemplate restTemplate;
    
    private static final String API_URL = "https://api.huobi.pro/market/tickers";
    private static final Map<String, TradingPair> SYMBOL_MAPPING = Map.of(
            "btcusdt", TradingPair.BTCUSDT,
            "ethusdt", TradingPair.ETHUSDT
    );
    
    @Override
    public PriceSource getSource() {
        return PriceSource.HUOBI;
    }
    
    @Override
    public Map<TradingPair, PriceData> fetchPrices() {
        try {
            log.debug("Fetching prices from Huobi");
            HuobiPriceDto response = restTemplate.getForObject(API_URL, HuobiPriceDto.class);
            
            if (!isValidResponse(response)) {
                log.warn("Invalid response from Huobi API");
                return Map.of();
            }
            
            return response.getData().stream()
                    .filter(this::isValidTicker)
                    .filter(ticker -> SYMBOL_MAPPING.containsKey(ticker.getSymbol().toLowerCase()))
                    .collect(Collectors.toMap(
                            ticker -> SYMBOL_MAPPING.get(ticker.getSymbol().toLowerCase()),
                            ticker -> new PriceData(ticker.getBid(), ticker.getAsk(), PriceSource.HUOBI)
                    ));
                    
        } catch (Exception e) {
            log.error("Failed to fetch prices from Huobi: {}", e.getMessage(), e);
            return Map.of();
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            HuobiPriceDto response = restTemplate.getForObject(API_URL, HuobiPriceDto.class);
            return isValidResponse(response);
        } catch (Exception e) {
            log.warn("Huobi API is not available: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean isValidResponse(HuobiPriceDto response) {
        return response != null && 
               response.getData() != null && 
               "ok".equals(response.getStatus());
    }
    
    private boolean isValidTicker(HuobiPriceDto.HuobiTickerDto ticker) {
        return ticker.getBid() != null && 
               ticker.getAsk() != null && 
               ticker.getBid().compareTo(ticker.getAsk()) < 0;
    }
}
