package com.pt.crypto_trading.service;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.dto.PriceDto;

import java.math.BigDecimal;
import java.util.List;

public interface PriceService {
    List<PriceDto> getAllLatestPrices();
    PriceDto getLatestPrice(TradingPair tradingPair);
    BigDecimal getExecutionPrice(TradingPair tradingPair, com.pt.crypto_trading.domain.enums.TradeType tradeType);
}
