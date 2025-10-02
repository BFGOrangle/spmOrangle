package com.spmorangle.crm.notification.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DisplayName("WebSocketConfig Test Cases")
class WebSocketConfigTest {

    @Test
    @DisplayName("Should create WebSocketConfig instance")
    void testWebSocketConfigCreation() {
        // Act
        WebSocketConfig config = new WebSocketConfig();

        // Assert
        assertNotNull(config);
    }

    @Test
    @DisplayName("Should verify WebSocketConfig is properly annotated")
    void testWebSocketConfigAnnotations() {
        // Verify the class has required annotations
        assertTrue(WebSocketConfig.class.isAnnotationPresent(
            org.springframework.context.annotation.Configuration.class));
        assertTrue(WebSocketConfig.class.isAnnotationPresent(
            org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker.class));
    }

    @Test
    @DisplayName("Should implement WebSocketMessageBrokerConfigurer")
    void testWebSocketConfigImplementsInterface() {
        // Verify the class implements the correct interface
        assertTrue(org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer.class
            .isAssignableFrom(WebSocketConfig.class));
    }
}
