package com.pt.crypto_trading.service;

import com.pt.crypto_trading.domain.entity.Wallet;
import com.pt.crypto_trading.domain.enums.Currency;
import com.pt.crypto_trading.dto.WalletDto;

import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    List<WalletDto> getUserWallets(Long userId);
    WalletDto getUserWallet(Long userId, Currency currency);
    Wallet createWallet(Long userId, Currency currency, BigDecimal initialBalance);
    void updateBalance(Long userId, Currency currency, BigDecimal amount);
    boolean hasSufficientBalance(Long userId, Currency currency, BigDecimal amount);
}
