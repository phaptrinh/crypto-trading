package com.pt.crypto_trading.controller;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.dto.PriceDto;
import com.pt.crypto_trading.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/prices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Price", description = "Price information API")
public class PriceController {
    
    private final PriceService priceService;
    
    @GetMapping
    @Operation(summary = "Get all latest prices", description = "Get latest aggregated prices for all trading pairs")
    public ResponseEntity<List<PriceDto>> getAllLatestPrices() {
        log.debug("Getting all latest prices");
        List<PriceDto> prices = priceService.getAllLatestPrices();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.SECONDS))
                .body(prices);
    }
    
    @GetMapping("/{tradingPair}")
    @Operation(summary = "Get price for trading pair", description = "Get latest aggregated price for specific trading pair")
    public ResponseEntity<PriceDto> getLatestPrice(
            @Parameter(description = "Trading pair", required = true, example = "BTCUSDT")
            @PathVariable TradingPair tradingPair) {
        
        log.debug("Getting latest price for: {}", tradingPair);
        PriceDto price = priceService.getLatestPrice(tradingPair);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.SECONDS))
                .body(price);
    }
}
