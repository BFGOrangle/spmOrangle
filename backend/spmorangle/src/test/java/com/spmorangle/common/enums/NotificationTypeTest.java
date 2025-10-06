package com.spmorangle.common.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DisplayName("NotificationType Test Cases")
class NotificationTypeTest {

    @Test
    @DisplayName("Should have all comment event types")
    void testCommentEventTypes() {
        assertNotNull(NotificationType.COMMENT_ADDED);
        assertNotNull(NotificationType.MENTION);
        assertNotNull(NotificationType.COMMENT_REPLY);
    }

    @Test
    @DisplayName("Should have all task event types")
    void testTaskEventTypes() {
        assertNotNull(NotificationType.TASK_ASSIGNED);
        assertNotNull(NotificationType.TASK_COMPLETED);
        assertNotNull(NotificationType.TASK_DEADLINE_APPROACHING);
    }

    @Test
    @DisplayName("Should have all project event types")
    void testProjectEventTypes() {
        assertNotNull(NotificationType.PROJECT_INVITE);
        assertNotNull(NotificationType.PROJECT_MEMBER_JOINED);
        assertNotNull(NotificationType.PROJECT_DEADLINE_APPROACHING);
    }

    @Test
    @DisplayName("Should have all user event types")
    void testUserEventTypes() {
        assertNotNull(NotificationType.USER_REGISTERED);
        assertNotNull(NotificationType.PASSWORD_RESET_REQUESTED);
    }

    @Test
    @DisplayName("Should have all system event types")
    void testSystemEventTypes() {
        assertNotNull(NotificationType.SYSTEM_MAINTENANCE);
        assertNotNull(NotificationType.SECURITY_ALERT);
    }

    @Test
    @DisplayName("Should have exactly 13 notification types")
    void testTotalNotificationTypesCount() {
        NotificationType[] types = NotificationType.values();
        assertEquals(13, types.length);
    }

    @Test
    @DisplayName("Should convert string to enum")
    void testValueOf() {
        NotificationType type = NotificationType.valueOf("MENTION");
        assertEquals(NotificationType.MENTION, type);
    }

    @Test
    @DisplayName("Should throw exception for invalid enum value")
    void testInvalidValueOf() {
        assertThrows(IllegalArgumentException.class, () -> {
            NotificationType.valueOf("INVALID_TYPE");
        });
    }

    @Test
    @DisplayName("Should verify enum order")
    void testEnumOrder() {
        NotificationType[] types = NotificationType.values();
        assertEquals(NotificationType.COMMENT_ADDED, types[0]);
        assertEquals(NotificationType.MENTION, types[1]);
        assertEquals(NotificationType.COMMENT_REPLY, types[2]);
        assertEquals(NotificationType.TASK_ASSIGNED, types[3]);
        assertEquals(NotificationType.SECURITY_ALERT, types[12]);
    }

    @Test
    @DisplayName("Should verify enum name")
    void testEnumName() {
        assertEquals("MENTION", NotificationType.MENTION.name());
        assertEquals("TASK_ASSIGNED", NotificationType.TASK_ASSIGNED.name());
        assertEquals("SYSTEM_MAINTENANCE", NotificationType.SYSTEM_MAINTENANCE.name());
    }
}
