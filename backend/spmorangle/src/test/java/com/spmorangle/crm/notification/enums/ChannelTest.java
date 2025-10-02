package com.spmorangle.crm.notification.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DisplayName("Channel Test Cases")
class ChannelTest {

    @Test
    @DisplayName("Should have all channel types")
    void testAllChannelTypes() {
        assertNotNull(Channel.IN_APP);
        assertNotNull(Channel.EMAIL);
        assertNotNull(Channel.SMS);
    }

    @Test
    @DisplayName("Should have exactly 3 channel types")
    void testTotalChannelCount() {
        Channel[] channels = Channel.values();
        assertEquals(3, channels.length);
    }

    @Test
    @DisplayName("Should convert string to enum")
    void testValueOf() {
        Channel channel = Channel.valueOf("IN_APP");
        assertEquals(Channel.IN_APP, channel);
    }

    @Test
    @DisplayName("Should throw exception for invalid enum value")
    void testInvalidValueOf() {
        assertThrows(IllegalArgumentException.class, () -> {
            Channel.valueOf("INVALID_CHANNEL");
        });
    }

    @Test
    @DisplayName("Should verify enum order")
    void testEnumOrder() {
        Channel[] channels = Channel.values();
        assertEquals(Channel.IN_APP, channels[0]);
        assertEquals(Channel.EMAIL, channels[1]);
        assertEquals(Channel.SMS, channels[2]);
    }

    @Test
    @DisplayName("Should verify enum name")
    void testEnumName() {
        assertEquals("IN_APP", Channel.IN_APP.name());
        assertEquals("EMAIL", Channel.EMAIL.name());
        assertEquals("SMS", Channel.SMS.name());
    }

    @Test
    @DisplayName("Should support enum comparison")
    void testEnumComparison() {
        assertEquals(Channel.IN_APP, Channel.IN_APP);
        assertNotEquals(Channel.IN_APP, Channel.EMAIL);
        assertNotEquals(Channel.EMAIL, Channel.SMS);
    }
}
