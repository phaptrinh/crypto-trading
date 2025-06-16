package com.pt.crypto_trading.service;

import com.pt.crypto_trading.dto.TradeRequestDto;
import com.pt.crypto_trading.dto.TradeResponseDto;

public interface IdempotencyService {
    TradeResponseDto checkIdempotency(Long userId, TradeRequestDto request);
    void saveIdempotentRequest(Long userId, String idempotencyKey, String requestHash, Long tradeId);
}
