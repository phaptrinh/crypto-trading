package com.pt.crypto_trading;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CryptoTradingApplicationTests {

	@Test
	void contextLoads() {
		// Verifies that Spring Boot application context loads successfully
		// This test ensures all beans are properly configured and dependencies are resolved
	}

	@Test
	void applicationStartsSuccessfully() {
		// This test passes if the Spring Boot application starts without errors
		// It validates the basic application configuration and component scanning
	}
}
