package com.pt.crypto_trading.repository;

import com.pt.crypto_trading.domain.entity.IdempotentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface IdempotentRequestRepository extends JpaRepository<IdempotentRequest, Long> {
    
    Optional<IdempotentRequest> findByIdempotencyKeyAndUserId(String idempotencyKey, Long userId);
    
    @Modifying
    @Query("DELETE FROM IdempotentRequest ir WHERE ir.expiresAt < :cutoffTime")
    int deleteExpiredRequests(@Param("cutoffTime") LocalDateTime cutoffTime);
    
}
