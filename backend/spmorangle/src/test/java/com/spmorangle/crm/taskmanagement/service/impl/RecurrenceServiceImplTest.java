package com.spmorangle.crm.taskmanagement.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurrenceServiceImpl Tests")
class RecurrenceServiceImplTest {

    @InjectMocks
    private RecurrenceServiceImpl recurrenceService;

    @BeforeEach
    void setUp() {
        System.out.println("Setting up recurrence service tests...");
    }

    @Nested
    @DisplayName("generateOccurrence Tests")
    class GenerateOccurrenceTests {

        @Test
        @DisplayName("Should generate weekly occurrences successfully")
        void generateWeeklyOccurrence_ShouldCreateSuccessfully() {
            // Arrange
            String rruleStr = "FREQ=WEEKLY;BYDAY=MO,WE,FR;UNTIL=20250331";
            OffsetDateTime start = OffsetDateTime.parse("2024-02-14T15:30:00-05:00");
            OffsetDateTime end = OffsetDateTime.parse("2024-03-31T18:00:00-05:00");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            assertThat(result.size()).isGreaterThan(0);

            // Verify all occurrences are within the range
            result.forEach(occurrence -> {
                assertThat(occurrence).isAfterOrEqualTo(start);
                assertThat(occurrence).isBeforeOrEqualTo(end);
            });
        }

        @Test
        @DisplayName("Should generate daily occurrences successfully")
        void generateDailyOccurrence_ShouldCreateSuccessfully() {
            // Arrange
            String rruleStr = "FREQ=DAILY";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-01T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-01-07T09:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(7); // 7 days including start and end
        }

        @Test
        @DisplayName("Should generate monthly occurrences successfully")
        void generateMonthlyOccurrence_ShouldCreateSuccessfully() {
            // Arrange
            String rruleStr = "FREQ=MONTHLY";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-15T10:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-06-15T10:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(6); // Jan, Feb, Mar, Apr, May, Jun
        }

        @Test
        @DisplayName("Should generate occurrences with COUNT limit")
        void generateOccurrenceWithCount_ShouldRespectLimit() {
            // Arrange
            String rruleStr = "FREQ=DAILY;COUNT=5";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-01T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-01-31T09:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(5); // Exactly 5 occurrences
        }

        @Test
        @DisplayName("Should generate occurrences with INTERVAL")
        void generateOccurrenceWithInterval_ShouldCreateSuccessfully() {
            // Arrange
            String rruleStr = "FREQ=DAILY;INTERVAL=2"; // Every 2 days
            OffsetDateTime start = OffsetDateTime.parse("2024-01-01T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-01-11T09:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(6); // Days: 1, 3, 5, 7, 9, 11
        }

        @Test
        @DisplayName("Should handle occurrences on specific weekdays")
        void generateOccurrenceOnWeekdays_ShouldCreateCorrectly() {
            // Arrange
            String rruleStr = "FREQ=WEEKLY;BYDAY=MO,FR"; // Monday and Friday only
            OffsetDateTime start = OffsetDateTime.parse("2024-01-01T09:00:00Z"); // Monday
            OffsetDateTime end = OffsetDateTime.parse("2024-01-15T09:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();

            // Verify all are Monday or Friday
            result.forEach(occurrence -> {
                int dayOfWeek = occurrence.getDayOfWeek().getValue();
                assertThat(dayOfWeek).isIn(1, 5); // 1=Monday, 5=Friday
            });
        }

        @Test
        @DisplayName("Should return empty list when end date is before start date")
        void generateOccurrence_WhenEndBeforeStart_ShouldReturnEmpty() {
            // Arrange
            String rruleStr = "FREQ=DAILY";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-15T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-01-01T09:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should filter occurrences that are before start date")
        void generateOccurrence_ShouldFilterBeforeStart() {
            // Arrange
            String rruleStr = "FREQ=DAILY";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-05T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-01-10T09:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).allMatch(occurrence -> !occurrence.isBefore(start));
        }

        @Test
        @DisplayName("Should throw RuntimeException for invalid recurrence rule")
        void generateOccurrence_WithInvalidRule_ShouldThrowException() {
            // Arrange
            String invalidRruleStr = "INVALID_RULE";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-01T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-01-31T09:00:00Z");

            // Act & Assert
            assertThatThrownBy(() -> recurrenceService.generateOccurrence(invalidRruleStr, start, end))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Error generating Occurrence");
        }

        @Test
        @DisplayName("Should handle yearly recurrence")
        void generateYearlyOccurrence_ShouldCreateSuccessfully() {
            // Arrange
            String rruleStr = "FREQ=YEARLY";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-01T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2027-01-01T09:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(4); // 2024, 2025, 2026, 2027
        }

        @Test
        @DisplayName("Should handle complex recurrence rule with multiple parameters")
        void generateComplexOccurrence_ShouldCreateSuccessfully() {
            // Arrange - Every other week on Monday and Wednesday until March 31
            String rruleStr = "FREQ=WEEKLY;INTERVAL=2;BYDAY=MO,WE;UNTIL=20240331";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-01T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-03-31T23:59:59Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();

            // Verify all are Monday or Wednesday
            result.forEach(occurrence -> {
                int dayOfWeek = occurrence.getDayOfWeek().getValue();
                assertThat(dayOfWeek).isIn(1, 3); // 1=Monday, 3=Wednesday
            });
        }

        @Test
        @DisplayName("Should handle single occurrence (no recurrence)")
        void generateSingleOccurrence_ShouldReturnOne() {
            // Arrange
            String rruleStr = "FREQ=DAILY;COUNT=1";
            OffsetDateTime start = OffsetDateTime.parse("2024-01-01T09:00:00Z");
            OffsetDateTime end = OffsetDateTime.parse("2024-12-31T09:00:00Z");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isAfterOrEqualTo(start);
        }
    }
}
