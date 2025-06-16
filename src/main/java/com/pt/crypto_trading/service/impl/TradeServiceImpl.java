package com.pt.crypto_trading.service.impl;

import com.pt.crypto_trading.domain.entity.Trade;
import com.pt.crypto_trading.domain.enums.Currency;
import com.pt.crypto_trading.domain.enums.TradeType;
import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.dto.TradeRequestDto;
import com.pt.crypto_trading.dto.TradeResponseDto;
import com.pt.crypto_trading.exception.InsufficientBalanceException;
import com.pt.crypto_trading.exception.ResourceNotFoundException;
import com.pt.crypto_trading.repository.TradeRepository;
import com.pt.crypto_trading.service.IdempotencyService;
import com.pt.crypto_trading.service.PriceService;
import com.pt.crypto_trading.service.TradeService;
import com.pt.crypto_trading.service.UserService;
import com.pt.crypto_trading.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeServiceImpl implements TradeService {
    
    private final TradeRepository tradeRepository;
    private final WalletService walletService;
    private final PriceService priceService;
    private final UserService userService;
    private final IdempotencyService idempotencyService;
    
    @Override
    @Transactional
    public TradeResponseDto executeTrade(Long userId, TradeRequestDto request) {
        log.info("Executing trade for user {}: {} {} {}", 
                userId, request.getType(), request.getQuantity(), request.getTradingPair());
        
        TradeResponseDto existingResult = idempotencyService.checkIdempotency(userId, request);
        if (existingResult != null) {
            return existingResult;
        }
        
        request.validateBusinessRules();
        
        if (!userService.userExists(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        
        String requestHash = null;
        if (request.getIdempotencyKey() != null) {
            requestHash = generateRequestHash(request);
        }
        
        try {
            BigDecimal executionPrice = priceService.getExecutionPrice(request.getTradingPair(), request.getType());
            BigDecimal totalAmount = request.getQuantity().multiply(executionPrice);
            
            Trade trade = new Trade(userId, request.getTradingPair(), request.getType(), 
                                  request.getQuantity(), executionPrice);
            
            validateAndUpdateWallets(userId, request.getTradingPair(), request.getType(), 
                                   request.getQuantity(), totalAmount);
            
            trade.markAsCompleted();
            Trade savedTrade = tradeRepository.save(trade);
            
            idempotencyService.saveIdempotentRequest(userId, request.getIdempotencyKey(), 
                                                   requestHash, savedTrade.getId());
            
            log.info("Successfully executed trade {} for user {}", savedTrade.getId(), userId);
            return TradeResponseDto.fromTrade(savedTrade);
            
        } catch (Exception e) {
            log.error("Failed to execute trade for user {}: {}", userId, e.getMessage(), e);
            
            Trade failedTrade = new Trade(userId, request.getTradingPair(), request.getType(), 
                                        request.getQuantity(), BigDecimal.ZERO);
            failedTrade.markAsFailed(e.getMessage());
            Trade savedTrade = tradeRepository.save(failedTrade);
            
            idempotencyService.saveIdempotentRequest(userId, request.getIdempotencyKey(), 
                                                   requestHash, savedTrade.getId());
            
            TradeResponseDto response = TradeResponseDto.fromTrade(savedTrade);
            response.setFailureReason(e.getMessage());
            
            if (e instanceof InsufficientBalanceException) {
                throw e;
            }
            throw new RuntimeException("Trade execution failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<TradeResponseDto> getUserTradeHistory(Long userId, Pageable pageable) {
        log.debug("Fetching trade history for user: {}", userId);
        
        if (!userService.userExists(userId)) {
            throw new ResourceNotFoundException("User not found: " + userId);
        }
        
        Page<Trade> trades = tradeRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return trades.map(TradeResponseDto::fromTrade);
    }
    
    private void validateAndUpdateWallets(Long userId, TradingPair tradingPair, TradeType tradeType, 
                                        BigDecimal quantity, BigDecimal totalAmount) {
        
        Currency baseCurrency = getBaseCurrency(tradingPair);
        Currency quoteCurrency = getQuoteCurrency(tradingPair);
        
        if (tradeType == TradeType.BUY) {
            if (!walletService.hasSufficientBalance(userId, quoteCurrency, totalAmount)) {
                throw new InsufficientBalanceException(
                    String.format("Insufficient %s balance. Required: %s", quoteCurrency, totalAmount));
            }
            
            walletService.updateBalance(userId, quoteCurrency, totalAmount.negate());
            walletService.updateBalance(userId, baseCurrency, quantity);
            
        } else {
            if (!walletService.hasSufficientBalance(userId, baseCurrency, quantity)) {
                throw new InsufficientBalanceException(
                    String.format("Insufficient %s balance. Required: %s", baseCurrency, quantity));
            }
            
            walletService.updateBalance(userId, baseCurrency, quantity.negate());
            walletService.updateBalance(userId, quoteCurrency, totalAmount);
        }
    }
    
    private Currency getBaseCurrency(TradingPair tradingPair) {
        return switch (tradingPair) {
            case BTCUSDT -> Currency.BTC;
            case ETHUSDT -> Currency.ETH;
        };
    }
    
    private Currency getQuoteCurrency(TradingPair tradingPair) {
        return Currency.USDT;
    }
    
    private String generateRequestHash(TradeRequestDto request) {
        try {
            String content = String.format("%s-%s-%s", 
                    request.getTradingPair(), 
                    request.getType(), 
                    request.getQuantity().toPlainString());
            
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
