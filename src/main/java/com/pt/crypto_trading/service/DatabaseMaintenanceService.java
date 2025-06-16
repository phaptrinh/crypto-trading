package com.pt.crypto_trading.service;

import com.pt.crypto_trading.repository.IdempotentRequestRepository;
import com.pt.crypto_trading.repository.PriceHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseMaintenanceService {
    
    private final PriceHistoryRepository priceHistoryRepository;
    private final IdempotentRequestRepository idempotentRequestRepository;
    
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    @Transactional
    public void cleanupOldData() {
        log.info("Starting database cleanup");
        
        // Cleanup old price history (older than 30 days)
        LocalDateTime priceHistoryCutoff = LocalDateTime.now().minusDays(30);
        int deletedPriceHistory = priceHistoryRepository.deleteOldHistory(priceHistoryCutoff);
        log.info("Deleted {} old price history records", deletedPriceHistory);
        
        // Cleanup expired idempotent requests
        LocalDateTime idempotentCutoff = LocalDateTime.now().minusHours(24);
        int deletedIdempotent = idempotentRequestRepository.deleteExpiredRequests(idempotentCutoff);
        log.info("Deleted {} expired idempotent requests", deletedIdempotent);
        
        log.info("Database cleanup completed");
    }
}
