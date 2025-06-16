package com.pt.crypto_trading.repository;

import com.pt.crypto_trading.domain.entity.BestPrice;
import com.pt.crypto_trading.domain.enums.TradingPair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BestPriceRepository extends JpaRepository<BestPrice, Long> {
    
    Optional<BestPrice> findByTradingPair(TradingPair tradingPair);
    
    List<BestPrice> findAllByOrderByTradingPair();
    
    @Query("SELECT bp FROM BestPrice bp WHERE bp.updatedAt >= :since ORDER BY bp.tradingPair")
    List<BestPrice> findAllUpdatedSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT bp FROM BestPrice bp WHERE bp.tradingPair IN :tradingPairs ORDER BY bp.tradingPair")
    List<BestPrice> findByTradingPairIn(@Param("tradingPairs") List<TradingPair> tradingPairs);
    
    @Modifying
    @Query("DELETE FROM BestPrice bp WHERE bp.updatedAt < :cutoffTime")
    int deleteOldPrices(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    boolean existsByTradingPair(TradingPair tradingPair);
}
