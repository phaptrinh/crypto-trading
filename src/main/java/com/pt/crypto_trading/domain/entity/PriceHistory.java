package com.pt.crypto_trading.domain.entity;

import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.enums.PriceSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_history",
       indexes = {
           @Index(name = "idx_price_trading_pair_timestamp", columnList = "trading_pair, timestamp"),
           @Index(name = "idx_price_source_timestamp", columnList = "source, timestamp"),
           @Index(name = "idx_price_timestamp", columnList = "timestamp")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "trading_pair", nullable = false)
    private TradingPair tradingPair;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceSource source;
    
    @Column(name = "bid_price", precision = 18, scale = 8)
    private BigDecimal bidPrice;
    
    @Column(name = "ask_price", precision = 18, scale = 8)
    private BigDecimal askPrice;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
    
    public PriceHistory(TradingPair tradingPair, PriceSource source, 
                       BigDecimal bidPrice, BigDecimal askPrice) {
        this.tradingPair = tradingPair;
        this.source = source;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.timestamp = LocalDateTime.now();
    }
}