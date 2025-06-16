package com.pt.crypto_trading.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "idempotent_requests",
       indexes = {
           @Index(name = "idx_idempotent_key_user", columnList = "idempotency_key, user_id", unique = true),
           @Index(name = "idx_idempotent_created_at", columnList = "created_at"),
           @Index(name = "idx_idempotent_expires_at", columnList = "expires_at")
       })
@Data
@NoArgsConstructor
public class IdempotentRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "idempotency_key", nullable = false, length = 64)
    private String idempotencyKey;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "trade_id")
    private Long tradeId;
    
    @Column(name = "request_hash", nullable = false, length = 256)
    private String requestHash;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        expiresAt = createdAt.plusHours(24);
    }
    
    public IdempotentRequest(String idempotencyKey, Long userId, String requestHash) {
        this.idempotencyKey = idempotencyKey;
        this.userId = userId;
        this.requestHash = requestHash;
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
