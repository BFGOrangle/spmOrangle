package com.spmorangle.common.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FrontendConfigTest {

    @Test
    void testGettersAndSetters() {
        // Arrange
        FrontendConfig config = new FrontendConfig();
        String expectedBaseUrl = "https://example.com";

        // Act
        config.setBaseUrl(expectedBaseUrl);
        String actualBaseUrl = config.getBaseUrl();

        // Assert
        assertEquals(expectedBaseUrl, actualBaseUrl);
    }

    @Test
    void testDefaultValue() {
        // Arrange & Act
        FrontendConfig config = new FrontendConfig();

        // Assert
        assertNull(config.getBaseUrl());
    }

    @Test
    void testWithLocalhostUrl() {
        // Arrange
        FrontendConfig config = new FrontendConfig();
        String localhostUrl = "http://localhost:3000";

        // Act
        config.setBaseUrl(localhostUrl);

        // Assert
        assertEquals(localhostUrl, config.getBaseUrl());
    }

    @Test
    void testWithProductionUrl() {
        // Arrange
        FrontendConfig config = new FrontendConfig();
        String productionUrl = "https://spmorangle.com";

        // Act
        config.setBaseUrl(productionUrl);

        // Assert
        assertEquals(productionUrl, config.getBaseUrl());
    }

    @Test
    void testWithTrailingSlash() {
        // Arrange
        FrontendConfig config = new FrontendConfig();
        String urlWithSlash = "https://example.com/";

        // Act
        config.setBaseUrl(urlWithSlash);

        // Assert
        assertEquals(urlWithSlash, config.getBaseUrl());
    }
}
