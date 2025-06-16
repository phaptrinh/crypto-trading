package com.pt.crypto_trading.integration.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BinancePriceDto {
    private String symbol;
    
    @JsonProperty("bidPrice")
    private BigDecimal bidPrice;
    
    @JsonProperty("askPrice")
    private BigDecimal askPrice;
}
