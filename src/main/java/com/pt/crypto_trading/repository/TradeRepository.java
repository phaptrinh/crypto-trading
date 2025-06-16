package com.pt.crypto_trading.repository;

import com.pt.crypto_trading.domain.entity.Trade;
import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.domain.enums.TradeStatus;
import com.pt.crypto_trading.domain.enums.TradeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    
    Page<Trade> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Trade> findByUserIdAndTradingPairOrderByCreatedAtDesc(Long userId, TradingPair tradingPair, Pageable pageable);
    
    Page<Trade> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, TradeType tradeType, Pageable pageable);
    
    Page<Trade> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, TradeStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Trade t WHERE t.userId = :userId AND t.createdAt >= :fromDate ORDER BY t.createdAt DESC")
    List<Trade> findByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("fromDate") LocalDateTime fromDate);
    
    @Query("SELECT t FROM Trade t WHERE t.userId = :userId AND t.tradingPair = :tradingPair AND t.type = :tradeType ORDER BY t.createdAt DESC")
    List<Trade> findByUserIdAndTradingPairAndTradeType(
            @Param("userId") Long userId, 
            @Param("tradingPair") TradingPair tradingPair, 
            @Param("tradeType") TradeType tradeType);
    
    @Query("SELECT SUM(t.quantity) FROM Trade t WHERE t.userId = :userId AND t.tradingPair = :tradingPair AND t.type = :tradeType AND t.status = 'COMPLETED'")
    BigDecimal sumQuantityByUserAndTradingPairAndTradeType(
            @Param("userId") Long userId, 
            @Param("tradingPair") TradingPair tradingPair, 
            @Param("tradeType") TradeType tradeType);
    
    @Query("SELECT COUNT(t) FROM Trade t WHERE t.userId = :userId AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TradeStatus status);
    
    @Query("SELECT t FROM Trade t WHERE t.status = :status ORDER BY t.createdAt")
    List<Trade> findByStatus(@Param("status") TradeStatus status);
    
    @Query(value = "SELECT COUNT(*) FROM trades WHERE user_id = :userId AND DATE(created_at) = CURRENT_DATE", nativeQuery = true)
    long countTodayTradesByUserId(@Param("userId") Long userId);
    
    boolean existsByUserIdAndId(Long userId, Long tradeId);
}
