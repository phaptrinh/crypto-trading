package com.pt.crypto_trading.domain.entity;

import com.pt.crypto_trading.domain.enums.TradeType;
import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.enums.PriceSource;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "best_prices",
       uniqueConstraints = @UniqueConstraint(columnNames = {"trading_pair"}),
       indexes = {
           @Index(name = "idx_best_price_trading_pair", columnList = "trading_pair"),
           @Index(name = "idx_best_price_updated_at", columnList = "updated_at")
       })
@Data
@NoArgsConstructor
public class BestPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trading_pair", nullable = false)
    private TradingPair tradingPair;
    
    @Column(name = "best_bid_price", precision = 18, scale = 8)
    private BigDecimal bestBidPrice;
    
    @Column(name = "best_ask_price", precision = 18, scale = 8)
    private BigDecimal bestAskPrice;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "bid_source", length = 20)
    private PriceSource bidSource;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ask_source", length = 20)
    private PriceSource askSource;
    
    @Version
    private Long version;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BestPrice(TradingPair tradingPair) {
        this.tradingPair = tradingPair;
    }

    public BigDecimal getPriceForTradeType(TradeType tradeType) {
        return tradeType == TradeType.BUY ? bestAskPrice : bestBidPrice;
    }
    
    public boolean isValidPrice() {
        return bestBidPrice != null && bestAskPrice != null && 
               bestBidPrice.compareTo(BigDecimal.ZERO) > 0 && 
               bestAskPrice.compareTo(BigDecimal.ZERO) > 0;
    }
}