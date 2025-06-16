package com.pt.crypto_trading.service.impl;

import com.pt.crypto_trading.domain.entity.User;
import com.pt.crypto_trading.domain.enums.Currency;
import com.pt.crypto_trading.dto.CreateUserRequestDto;
import com.pt.crypto_trading.exception.ResourceNotFoundException;
import com.pt.crypto_trading.repository.UserRepository;
import com.pt.crypto_trading.service.UserService;
import com.pt.crypto_trading.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    private final WalletService walletService;
    
    @Override
    @Transactional
    public User createUser(CreateUserRequestDto request) {
        log.info("Creating user with username: {}", request.getUsername());
        User user = new User(request.getUsername(), request.getEmail());
        return createUserWithWallets(user);
    }

    private User createUserWithWallets(User user) {
        try {
            User savedUser = userRepository.save(user);
            
            walletService.createWallet(savedUser.getId(), Currency.USDT, new BigDecimal("50000.00000000"));
            walletService.createWallet(savedUser.getId(), Currency.BTC, BigDecimal.ZERO);
            walletService.createWallet(savedUser.getId(), Currency.ETH, BigDecimal.ZERO);
            
            log.info("Successfully created user {} with initial wallets", savedUser.getId());
            return savedUser;
            
        } catch (DataIntegrityViolationException e) {
            log.error("Failed to create user due to constraint violation: {}", e.getMessage());
            throw new IllegalArgumentException("Username or email already exists");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        log.debug("Fetching user by ID: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean userExists(Long userId) {
        return userRepository.existsById(userId);
    }
}
