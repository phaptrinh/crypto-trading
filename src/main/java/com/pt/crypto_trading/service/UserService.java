package com.pt.crypto_trading.service;

import com.pt.crypto_trading.domain.entity.User;
import com.pt.crypto_trading.dto.CreateUserRequestDto;

public interface UserService {
    User createUser(CreateUserRequestDto request);
    User getUserById(Long userId);
    boolean userExists(Long userId);
}
