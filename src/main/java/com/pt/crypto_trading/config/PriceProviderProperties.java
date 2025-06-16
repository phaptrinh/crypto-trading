package com.pt.crypto_trading.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.price")
public class PriceProviderProperties {
    
    private Binance binance = new Binance();
    private Huobi huobi = new Huobi();
    private Cleanup cleanup = new Cleanup();
    
    @Data
    public static class Binance {
        private String url;
    }
    
    @Data
    public static class Huobi {
        private String url;
    }
    
    @Data
    public static class Cleanup {
        private int retentionDays = 30; // Default: keep 30 days of history
        private String cron = "0 0 2 * * ?"; // Default: 2 AM daily
    }
}
