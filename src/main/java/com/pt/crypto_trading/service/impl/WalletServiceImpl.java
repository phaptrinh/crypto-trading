package com.pt.crypto_trading.service.impl;

import com.pt.crypto_trading.domain.entity.Wallet;
import com.pt.crypto_trading.domain.enums.Currency;
import com.pt.crypto_trading.dto.WalletDto;
import com.pt.crypto_trading.exception.ResourceNotFoundException;
import com.pt.crypto_trading.repository.WalletRepository;
import com.pt.crypto_trading.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements WalletService {
    
    private final WalletRepository walletRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<WalletDto> getUserWallets(Long userId) {
        log.debug("Fetching all wallets for user: {}", userId);
        
        List<Wallet> wallets = walletRepository.findByUserId(userId);
        return wallets.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public WalletDto getUserWallet(Long userId, Currency currency) {
        log.debug("Fetching {} wallet for user: {}", currency, userId);
        
        Wallet wallet = findWalletByUserIdAndCurrency(userId, currency);
        return convertToDto(wallet);
    }
    
    @Override
    @Transactional
    public Wallet createWallet(Long userId, Currency currency, BigDecimal initialBalance) {
        log.info("Creating {} wallet for user {} with initial balance: {}", 
                currency, userId, initialBalance);
        
        if (walletRepository.findByUserIdAndCurrency(userId, currency).isPresent()) {
            throw new IllegalArgumentException(
                String.format("Wallet already exists for user %d and currency %s", userId, currency));
        }
        
        Wallet wallet = new Wallet(userId, currency, initialBalance);
        return walletRepository.save(wallet);
    }
    
    @Override
    @Transactional
    public void updateBalance(Long userId, Currency currency, BigDecimal amount) {
        log.debug("Updating balance for user {} currency {} by amount: {}", userId, currency, amount);
        
        Wallet wallet = walletRepository.findByUserIdAndCurrencyForUpdate(userId, currency)
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("Wallet not found for user %d and currency %s", userId, currency)));
        
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            wallet.addBalance(amount);
        } else if (amount.compareTo(BigDecimal.ZERO) < 0) {
            wallet.subtractBalance(amount.abs());
        }
        
        walletRepository.save(wallet);
        log.debug("Updated wallet balance to: {}", wallet.getBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasSufficientBalance(Long userId, Currency currency, BigDecimal amount) {
        Wallet wallet = findWalletByUserIdAndCurrency(userId, currency);
        return wallet.hasSufficientBalance(amount);
    }
    
    private Wallet findWalletByUserIdAndCurrency(Long userId, Currency currency) {
        return walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("Wallet not found for user %d and currency %s", userId, currency)));
    }
    
    private WalletDto convertToDto(Wallet wallet) {
        return new WalletDto(
                wallet.getId(),
                wallet.getUserId(),
                wallet.getCurrency(),
                wallet.getBalance(),
                wallet.getCreatedAt(),
                wallet.getUpdatedAt()
        );
    }
}
