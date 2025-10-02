package com.spmorangle.crm.notification.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DisplayName("WebSocketTopics Test Cases")
class WebSocketTopicsTest {

    @Test
    @DisplayName("Should have correct user notifications topic pattern")
    void testNotificationsUserPattern() {
        assertEquals("/topic/notifications/{userId}", WebSocketTopics.NOTIFICATIONS_USER);
    }

    @Test
    @DisplayName("Should have correct all notifications topic")
    void testNotificationsAllTopic() {
        assertEquals("/topic/notifications/all", WebSocketTopics.NOTIFICATIONS_ALL);
    }

    @Test
    @DisplayName("Should generate correct user notification topic for valid userId")
    void testUserNotificationsWithValidId() {
        Long userId = 123L;
        String expectedTopic = "/topic/notifications/123";

        String actualTopic = WebSocketTopics.userNotifications(userId);

        assertEquals(expectedTopic, actualTopic);
    }

    @Test
    @DisplayName("Should generate correct user notification topic for different userIds")
    void testUserNotificationsWithDifferentIds() {
        assertEquals("/topic/notifications/1", WebSocketTopics.userNotifications(1L));
        assertEquals("/topic/notifications/999", WebSocketTopics.userNotifications(999L));
        assertEquals("/topic/notifications/42", WebSocketTopics.userNotifications(42L));
    }

    @Test
    @DisplayName("Should handle large userId values")
    void testUserNotificationsWithLargeId() {
        Long largeUserId = 9999999999L;
        String expectedTopic = "/topic/notifications/9999999999";

        String actualTopic = WebSocketTopics.userNotifications(largeUserId);

        assertEquals(expectedTopic, actualTopic);
    }

    @Test
    @DisplayName("Should start with /topic prefix")
    void testTopicPrefix() {
        assertTrue(WebSocketTopics.NOTIFICATIONS_USER.startsWith("/topic"));
        assertTrue(WebSocketTopics.NOTIFICATIONS_ALL.startsWith("/topic"));
        assertTrue(WebSocketTopics.userNotifications(1L).startsWith("/topic"));
    }

    @Test
    @DisplayName("Should generate unique topics for different users")
    void testUniqueTopicsForDifferentUsers() {
        String topic1 = WebSocketTopics.userNotifications(1L);
        String topic2 = WebSocketTopics.userNotifications(2L);

        assertNotEquals(topic1, topic2);
    }
}
