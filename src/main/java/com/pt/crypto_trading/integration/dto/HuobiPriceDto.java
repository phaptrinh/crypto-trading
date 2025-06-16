package com.pt.crypto_trading.integration.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HuobiPriceDto {
    private String status;
    private List<HuobiTickerDto> data;
    
    @Data
    public static class HuobiTickerDto {
        private String symbol;
        private BigDecimal bid;
        private BigDecimal ask;
    }
}
