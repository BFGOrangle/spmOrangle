package com.spmorangle.crm.taskmanagement.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("RecurrenceServiceImpl Tests")
class RecurrenceServiceImplTest {

    @InjectMocks
    private RecurrenceServiceImpl recurrenceService;

    @BeforeEach
    void setUp() {
        System.out.println("Setting up recurrence...");
    }

    @Nested
    @DisplayName("generateOccurrence Tests")
    class generateOccurrenceTests {

        @Test
        @DisplayName("Should generate occurrence successfully")
        void generateOccurrence_ShouldCreateSuccessfully() {
            // Mock Inputs
            String rruleStr = "FREQ=WEEKLY;BYDAY=MO,WE,FR;UNTIL=20250331";
            OffsetDateTime start = OffsetDateTime.parse("2024-02-14T15:30:00-05:00");
            OffsetDateTime end = OffsetDateTime.parse("2024-03-31T18:00:00-05:00");

            // Act
            List<OffsetDateTime> result = recurrenceService.generateOccurrence(rruleStr, start, end);
            System.out.println("Generated " + result.size() + " occurrences:");
            result.forEach(date -> System.out.println("  - " + date));

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            assertThat(result.size()).isGreaterThan(0);
        }
    }


}
