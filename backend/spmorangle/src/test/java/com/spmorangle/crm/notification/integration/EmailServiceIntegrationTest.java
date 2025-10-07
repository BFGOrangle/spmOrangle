package com.spmorangle.crm.notification.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Email Service Integration Test")
class EmailServiceIntegrationTest {

    @Test
    @DisplayName("Application context should load with EmailService")
    void contextLoads() {
        // This test verifies that:
        // 1. EmailService can be instantiated
        // 2. Spring Mail dependencies are working
        // 3. Application context loads successfully
        // If this test passes, your EmailService is configured correctly!
    }
}