package com.pt.crypto_trading.controller;

import com.pt.crypto_trading.domain.entity.User;
import com.pt.crypto_trading.dto.CreateUserRequestDto;
import com.pt.crypto_trading.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User management API")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Get user details by user ID")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "User ID", required = true, example = "1")
            @PathVariable Long userId) {
        
        log.debug("Getting user by ID: {}", userId);
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user")
    public ResponseEntity<User> createUser(
            @Parameter(description = "User details", required = true)
            @RequestBody CreateUserRequestDto createUserRequestDto) {
        
        log.debug("Creating user with username: {}", createUserRequestDto.getUsername());
        User createdUser = userService.createUser(createUserRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
}
