package com.pt.crypto_trading.service;

import com.pt.crypto_trading.dto.TradeRequestDto;
import com.pt.crypto_trading.dto.TradeResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TradeService {
    TradeResponseDto executeTrade(Long userId, TradeRequestDto request);
    Page<TradeResponseDto> getUserTradeHistory(Long userId, Pageable pageable);
}
