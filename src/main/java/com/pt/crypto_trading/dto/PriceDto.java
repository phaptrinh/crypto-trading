package com.pt.crypto_trading.dto;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.enums.PriceSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDto {
    private TradingPair tradingPair;
    private BigDecimal bidPrice;
    private BigDecimal askPrice;
    private PriceSource bidSource;
    private PriceSource askSource;
    private LocalDateTime updatedAt;
}
