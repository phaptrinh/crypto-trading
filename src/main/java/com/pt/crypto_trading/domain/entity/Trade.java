package com.pt.crypto_trading.domain.entity;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.domain.enums.TradeType;
import com.pt.crypto_trading.domain.enums.TradeStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades",
       indexes = {
           @Index(name = "idx_trade_user_created", columnList = "user_id, created_at"),
           @Index(name = "idx_trade_trading_pair", columnList = "trading_pair"),
           @Index(name = "idx_trade_status", columnList = "status"),
           @Index(name = "idx_trade_created_at", columnList = "created_at")
       })
@Data
@NoArgsConstructor
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trading_pair", nullable = false)
    private TradingPair tradingPair;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeType type;
    
    @Column(precision = 18, scale = 8, nullable = false)
    private BigDecimal quantity;
    
    @Column(precision = 18, scale = 8, nullable = false)
    private BigDecimal price;
    
    @Column(name = "total_amount", precision = 18, scale = 8, nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status = TradeStatus.PENDING;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "executed_at")
    private LocalDateTime executedAt;
    
    @Column(name = "error_message", length = 500)
    private String errorMessage;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (totalAmount == null && quantity != null && price != null) {
            totalAmount = quantity.multiply(price);
        }
    }
    
    public Trade(Long userId, TradingPair tradingPair, TradeType type, 
                 BigDecimal quantity, BigDecimal price) {
        this.userId = userId;
        this.tradingPair = tradingPair;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = quantity.multiply(price);
    }

    public void markAsCompleted() {
        this.status = TradeStatus.COMPLETED;
        this.executedAt = LocalDateTime.now();
    }
    
    public void markAsFailed(String errorMessage) {
        this.status = TradeStatus.FAILED;
        this.errorMessage = errorMessage;
        this.executedAt = LocalDateTime.now();
    }
    
    public boolean isCompleted() {
        return status == TradeStatus.COMPLETED;
    }
    
    public boolean isFailed() {
        return status == TradeStatus.FAILED;
    }
}