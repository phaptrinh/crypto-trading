package com.pt.crypto_trading.controller;

import com.pt.crypto_trading.domain.enums.Currency;
import com.pt.crypto_trading.dto.WalletDto;
import com.pt.crypto_trading.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Wallet", description = "Wallet management API")
public class WalletController {
    
    private final WalletService walletService;
    
    @GetMapping
    @Operation(summary = "Get user wallets", description = "Get all wallet balances for a user")
    public ResponseEntity<List<WalletDto>> getUserWallets(
            @Parameter(description = "User ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Long userId) {
        
        log.debug("Getting wallets for user: {}", userId);
        List<WalletDto> wallets = walletService.getUserWallets(userId);
        return ResponseEntity.ok(wallets);
    }
    
    @GetMapping("/{currency}")
    @Operation(summary = "Get specific wallet", description = "Get wallet balance for specific currency")
    public ResponseEntity<WalletDto> getUserWallet(
            @Parameter(description = "User ID", required = true, example = "1")
            @RequestHeader("X-User-Id") Long userId,
            @Parameter(description = "Currency code", required = true, example = "BTC")
            @PathVariable Currency currency) {
        
        log.debug("Getting {} wallet for user: {}", currency, userId);
        WalletDto wallet = walletService.getUserWallet(userId, currency);
        return ResponseEntity.ok(wallet);
    }
}
