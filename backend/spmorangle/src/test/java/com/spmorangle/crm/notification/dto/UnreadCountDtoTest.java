package com.spmorangle.crm.notification.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DisplayName("UnreadCountDto Test Cases")
class UnreadCountDtoTest {

    @Test
    @DisplayName("Should create UnreadCountDto with valid count")
    void testCreateUnreadCountDto() {
        // Arrange
        int expectedCount = 5;

        // Act
        UnreadCountDto dto = new UnreadCountDto(expectedCount);

        // Assert
        assertNotNull(dto);
        assertEquals(expectedCount, dto.getCount());
    }

    @Test
    @DisplayName("Should create UnreadCountDto with zero count")
    void testCreateUnreadCountDtoWithZero() {
        // Arrange
        int expectedCount = 0;

        // Act
        UnreadCountDto dto = new UnreadCountDto(expectedCount);

        // Assert
        assertEquals(0, dto.getCount());
    }

    @Test
    @DisplayName("Should create UnreadCountDto with large count")
    void testCreateUnreadCountDtoWithLargeCount() {
        // Arrange
        int expectedCount = 999;

        // Act
        UnreadCountDto dto = new UnreadCountDto(expectedCount);

        // Assert
        assertEquals(999, dto.getCount());
    }

    @Test
    @DisplayName("Should handle count as immutable")
    void testUnreadCountDtoImmutability() {
        // Arrange
        int originalCount = 10;
        UnreadCountDto dto = new UnreadCountDto(originalCount);

        // Act
        int retrievedCount = dto.getCount();

        // Assert
        assertEquals(originalCount, retrievedCount);
        assertEquals(10, dto.getCount()); // Should still be 10
    }
}
