package com.spmorangle.crm.notification.constants;

public class WebSocketTopics {
    // Notification topics
    public static final String NOTIFICATIONS_USER = "/topic/notifications/{userId}";
    public static final String NOTIFICATIONS_ALL = "/topic/notifications/all";

    // Helper methods
    public static String userNotifications(Long userId) {
        return "/topic/notifications/" + userId;
    }
}