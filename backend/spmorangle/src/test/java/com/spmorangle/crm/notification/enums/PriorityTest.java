package com.spmorangle.crm.notification.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DisplayName("Priority Test Cases")
class PriorityTest {

    @Test
    @DisplayName("Should have all priority levels")
    void testAllPriorityLevels() {
        assertNotNull(Priority.LOW);
        assertNotNull(Priority.MEDIUM);
        assertNotNull(Priority.HIGH);
    }

    @Test
    @DisplayName("Should have exactly 3 priority levels")
    void testTotalPriorityCount() {
        Priority[] priorities = Priority.values();
        assertEquals(3, priorities.length);
    }

    @Test
    @DisplayName("Should convert string to enum")
    void testValueOf() {
        Priority priority = Priority.valueOf("HIGH");
        assertEquals(Priority.HIGH, priority);
    }

    @Test
    @DisplayName("Should throw exception for invalid enum value")
    void testInvalidValueOf() {
        assertThrows(IllegalArgumentException.class, () -> {
            Priority.valueOf("INVALID_PRIORITY");
        });
    }

    @Test
    @DisplayName("Should verify enum order")
    void testEnumOrder() {
        Priority[] priorities = Priority.values();
        assertEquals(Priority.LOW, priorities[0]);
        assertEquals(Priority.MEDIUM, priorities[1]);
        assertEquals(Priority.HIGH, priorities[2]);
    }

    @Test
    @DisplayName("Should verify enum name")
    void testEnumName() {
        assertEquals("LOW", Priority.LOW.name());
        assertEquals("MEDIUM", Priority.MEDIUM.name());
        assertEquals("HIGH", Priority.HIGH.name());
    }

    @Test
    @DisplayName("Should support enum comparison")
    void testEnumComparison() {
        assertEquals(Priority.LOW, Priority.LOW);
        assertNotEquals(Priority.LOW, Priority.MEDIUM);
        assertNotEquals(Priority.MEDIUM, Priority.HIGH);
    }

    @Test
    @DisplayName("Should verify ordinal values")
    void testOrdinalValues() {
        assertEquals(0, Priority.LOW.ordinal());
        assertEquals(1, Priority.MEDIUM.ordinal());
        assertEquals(2, Priority.HIGH.ordinal());
    }
}
