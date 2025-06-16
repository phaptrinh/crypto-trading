package com.pt.crypto_trading.repository;

import com.pt.crypto_trading.domain.entity.PriceHistory;
import com.pt.crypto_trading.domain.enums.TradingPair;
import com.pt.crypto_trading.integration.enums.PriceSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    
    @Modifying
    @Query("DELETE FROM PriceHistory ph WHERE ph.timestamp < :cutoffTime")
    int deleteOldHistory(@Param("cutoffTime") LocalDateTime cutoffTime);

}
