package com.pt.crypto_trading.controller;

import com.pt.crypto_trading.dto.TradeRequestDto;
import com.pt.crypto_trading.dto.TradeResponseDto;
import com.pt.crypto_trading.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trade", description = "Trading operations API")
public class TradeController {
    
    private final TradeService tradeService;
    
    @PostMapping
    @Operation(summary = "Execute trade", description = "Execute a buy or sell trade with optional idempotency key")
    public ResponseEntity<TradeResponseDto> executeTrade(
            @Parameter(description = "User ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Idempotency key to prevent duplicate trades", required = false)
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Parameter(description = "Trade request details", required = true)
            @Valid @RequestBody TradeRequestDto request) {
        
        if (request.getIdempotencyKey() == null && idempotencyKey != null) {
            request.setIdempotencyKey(idempotencyKey);
        }
        
        log.info("Executing trade for user {}: {} (idempotency: {})", 
                userId, request, request.getIdempotencyKey());
        TradeResponseDto response = tradeService.executeTrade(userId, request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(summary = "Get trade history", description = "Get user's trading history with pagination")
    public ResponseEntity<Page<TradeResponseDto>> getTradeHistory(
            @Parameter(description = "User ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        log.debug("Getting trade history for user: {}", userId);
        Page<TradeResponseDto> trades = tradeService.getUserTradeHistory(userId, pageable);
        return ResponseEntity.ok(trades);
    }
}
