package com.pt.crypto_trading.service.impl;

import com.pt.crypto_trading.domain.entity.IdempotentRequest;
import com.pt.crypto_trading.domain.entity.Trade;
import com.pt.crypto_trading.dto.TradeRequestDto;
import com.pt.crypto_trading.dto.TradeResponseDto;
import com.pt.crypto_trading.exception.DuplicateRequestException;
import com.pt.crypto_trading.repository.IdempotentRequestRepository;
import com.pt.crypto_trading.repository.TradeRepository;
import com.pt.crypto_trading.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyServiceImpl implements IdempotencyService {
    
    private final IdempotentRequestRepository idempotentRequestRepository;
    private final TradeRepository tradeRepository;
    
    @Override
    @Transactional(readOnly = true)
    public TradeResponseDto checkIdempotency(Long userId, TradeRequestDto request) {
        if (request.getIdempotencyKey() == null || request.getIdempotencyKey().trim().isEmpty()) {
            return null; // No idempotency key provided, proceed with normal execution
        }
        
        String requestHash = generateRequestHash(request);
        Optional<IdempotentRequest> existingRequest = idempotentRequestRepository
                .findByIdempotencyKeyAndUserId(request.getIdempotencyKey(), userId);
        
        if (existingRequest.isPresent()) {
            IdempotentRequest idempotentRequest = existingRequest.get();
            
            if (idempotentRequest.isExpired()) {
                log.warn("Expired idempotency key used: {} for user: {}", request.getIdempotencyKey(), userId);
                throw new DuplicateRequestException("Idempotency key has expired");
            }
            
            if (!requestHash.equals(idempotentRequest.getRequestHash())) {
                log.warn("Idempotency key reused with different request content: {} for user: {}", 
                        request.getIdempotencyKey(), userId);
                throw new DuplicateRequestException("Idempotency key already used with different request content");
            }
            
            if (idempotentRequest.getTradeId() != null) {
                Optional<Trade> trade = tradeRepository.findById(idempotentRequest.getTradeId());
                if (trade.isPresent()) {
                    log.info("Returning existing trade result for idempotency key: {} user: {}", 
                            request.getIdempotencyKey(), userId);
                    return TradeResponseDto.fromTrade(trade.get());
                }
            }
        }
        
        return null;
    }
    
    @Override
    @Transactional
    public void saveIdempotentRequest(Long userId, String idempotencyKey, String requestHash, Long tradeId) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return;
        }
        
        IdempotentRequest idempotentRequest = new IdempotentRequest(idempotencyKey, userId, requestHash);
        idempotentRequest.setTradeId(tradeId);
        idempotentRequestRepository.save(idempotentRequest);
        
        log.debug("Saved idempotent request: key={}, userId={}, tradeId={}", 
                idempotencyKey, userId, tradeId);
    }

    private String generateRequestHash(TradeRequestDto request) {
        try {
            String content = String.format("%s-%s-%s", 
                    request.getTradingPair(), 
                    request.getType(), 
                    request.getQuantity().toPlainString());
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
