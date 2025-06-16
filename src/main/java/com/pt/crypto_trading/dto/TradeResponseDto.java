package com.pt.crypto_trading.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pt.crypto_trading.domain.entity.Trade;
import com.pt.crypto_trading.domain.enums.TradeStatus;
import com.pt.crypto_trading.domain.enums.TradeType;
import com.pt.crypto_trading.domain.enums.TradingPair;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Trade response containing trade execution details")
public class TradeResponseDto {
    
    @Schema(description = "Trade ID", example = "1")
    private Long tradeId;
    
    @Schema(description = "User ID", example = "1")
    private Long userId;
    
    @Schema(description = "Trading pair", example = "BTCUSDT")
    private TradingPair tradingPair;
    
    @Schema(description = "Trade type", example = "BUY")
    private TradeType type;
    
    @Schema(description = "Quantity traded", example = "0.5")
    private BigDecimal quantity;
    
    @Schema(description = "Execution price", example = "45000.0")
    private BigDecimal price;
    
    @Schema(description = "Total trade value", example = "22500.0")
    private BigDecimal totalValue;
    
    @Schema(description = "Trade status", example = "COMPLETED")
    private TradeStatus status;
    
    @Schema(description = "Failure reason if trade failed")
    private String failureReason;
    
    @Schema(description = "Trade creation timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Schema(description = "Trade completion timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completedAt;
    
    public static TradeResponseDto fromTrade(Trade trade) {
        return new TradeResponseDto(
            trade.getId(),
            trade.getUserId(),
            trade.getTradingPair(),
            trade.getType(),
            trade.getQuantity(),
            trade.getPrice(),
            trade.getQuantity().multiply(trade.getPrice()),
            trade.getStatus(),
            null,
            trade.getCreatedAt(),
            trade.getExecutedAt() != null ? trade.getExecutedAt() : LocalDateTime.now()
        );
    }
}
