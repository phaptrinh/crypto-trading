package com.pt.crypto_trading.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import com.pt.crypto_trading.domain.enums.Currency;

@Entity
@Table(name = "wallets", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "currency"}),
       indexes = {
           @Index(name = "idx_wallet_user_currency", columnList = "user_id, currency"),
           @Index(name = "idx_wallet_user_id", columnList = "user_id")
       })
@Data
@NoArgsConstructor
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Currency currency;
    
    @Column(precision = 18, scale = 8, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public Wallet(Long userId, Currency currency, BigDecimal balance) {
        this.userId = userId;
        this.currency = currency;
        this.balance = balance;
    }
    
    public void addBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive and non-null");
        }
        
        this.balance = this.balance.add(amount).setScale(8, RoundingMode.HALF_UP);
    }

    public void subtractBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive and non-null");
        }
        if (!hasSufficientBalance(amount)) {
            throw new IllegalArgumentException(
                String.format("Insufficient balance. Required: %s, Available: %s", 
                    amount.toPlainString(), this.balance.toPlainString()));
        }
        
        this.balance = this.balance.subtract(amount).setScale(8, RoundingMode.HALF_UP);
    }
    
    public boolean hasSufficientBalance(BigDecimal amount) {
        if (amount == null) return false;
        return this.balance.compareTo(amount) >= 0;
    }
}