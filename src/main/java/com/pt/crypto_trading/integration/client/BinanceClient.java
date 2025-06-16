package com.pt.crypto_trading.integration.client;

import com.pt.crypto_trading.config.PriceProviderProperties;
import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.dto.BinancePriceDto;
import com.pt.crypto_trading.integration.enums.PriceSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class BinanceClient implements PriceProvider {
    
    private final RestTemplate restTemplate;
    private final PriceProviderProperties priceProviderProperties;
    
    private static final Map<String, TradingPair> SYMBOL_MAPPING = Map.of(
            "BTCUSDT", TradingPair.BTCUSDT,
            "ETHUSDT", TradingPair.ETHUSDT
    );
    
    @Override
    public PriceSource getSource() {
        return PriceSource.BINANCE;
    }
    
    @Override
    public Map<TradingPair, PriceData> fetchPrices() {
        try {
            log.debug("Fetching prices from Binance");
            String apiUrl = priceProviderProperties.getBinance().getUrl();
            BinancePriceDto[] response = restTemplate.getForObject(apiUrl, BinancePriceDto[].class);
            
            if (response == null) {
                log.warn("Empty response from Binance API");
                return Map.of();
            }
            
            return Arrays.stream(response)
                    .filter(this::isValidPrice)
                    .filter(dto -> SYMBOL_MAPPING.containsKey(dto.getSymbol()))
                    .collect(Collectors.toMap(
                            dto -> SYMBOL_MAPPING.get(dto.getSymbol()),
                            dto -> new PriceData(dto.getBidPrice(), dto.getAskPrice(), PriceSource.BINANCE)
                    ));
                    
        } catch (Exception e) {
            log.error("Failed to fetch prices from Binance: {}", e.getMessage(), e);
            return Map.of();
        }
    }
    
    @Override
    public boolean isAvailable() {
        try {
            String apiUrl = priceProviderProperties.getBinance().getUrl();
            restTemplate.getForObject(apiUrl, BinancePriceDto[].class);
            return true;
        } catch (Exception e) {
            log.warn("Binance API is not available: {}", e.getMessage());
            return false;
        }
    }
    
    private boolean isValidPrice(BinancePriceDto dto) {
        return dto.getBidPrice() != null && 
               dto.getAskPrice() != null && 
               dto.getBidPrice().compareTo(dto.getAskPrice()) < 0;
    }
}
