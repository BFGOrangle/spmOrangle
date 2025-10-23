package com.spmorangle.crm.taskmanagement.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Recurrence Start Date Verification Tests")
class RecurrenceStartDateTest {

    @InjectMocks
    private RecurrenceServiceImpl recurrenceService;

    @Test
    @DisplayName("Daily recurrence starting 1 year from now should NOT create tasks today")
    void dailyRecurrence_StartingInFuture_ShouldRespectStartDate() {
        // Arrange: Start date is 1 year from now
        OffsetDateTime today = OffsetDateTime.now();
        OffsetDateTime startOneYearFromNow = today.plusYears(1);
        OffsetDateTime endTwoYearsFromNow = today.plusYears(2);

        String rruleStr = "FREQ=DAILY;INTERVAL=1";

        // Act: Generate occurrences
        List<OffsetDateTime> occurrences = recurrenceService.generateOccurrence(
            rruleStr, 
            startOneYearFromNow, 
            endTwoYearsFromNow
        );

        // Assert: All occurrences should be AFTER the start date
        System.out.println("\n=== Test Results ===");
        System.out.println("Today: " + today);
        System.out.println("Start Date (1 year from now): " + startOneYearFromNow);
        System.out.println("Total occurrences generated: " + occurrences.size());
        
        if (!occurrences.isEmpty()) {
            System.out.println("\nFirst 3 occurrences:");
            for (int i = 0; i < Math.min(3, occurrences.size()); i++) {
                System.out.println("  " + (i+1) + ". " + occurrences.get(i));
            }
        }

        // Verify all occurrences are on or after start date
        assertThat(occurrences).isNotEmpty();
        assertThat(occurrences).allMatch(occurrence -> 
            !occurrence.isBefore(startOneYearFromNow),
            "All occurrences should be on or after the start date"
        );
        
        // Verify NO occurrences are before today + 1 year
        assertThat(occurrences).noneMatch(occurrence -> 
            occurrence.isBefore(startOneYearFromNow),
            "No occurrences should be before the start date"
        );

        System.out.println("\n✅ SUCCESS: All occurrences respect the start date!");
        System.out.println("   Tasks will NOT be created until the start date is reached.\n");
    }

    @Test
    @DisplayName("Weekly recurrence starting next month should not create tasks today")
    void weeklyRecurrence_StartingNextMonth_ShouldRespectStartDate() {
        // Arrange: Start date is next month
        OffsetDateTime today = OffsetDateTime.now();
        OffsetDateTime startNextMonth = today.plusMonths(1);
        OffsetDateTime endSixMonthsLater = startNextMonth.plusMonths(6);

        String rruleStr = "FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,WE,FR";

        // Act: Generate occurrences
        List<OffsetDateTime> occurrences = recurrenceService.generateOccurrence(
            rruleStr, 
            startNextMonth, 
            endSixMonthsLater
        );

        // Assert
        System.out.println("\n=== Weekly Recurrence Test ===");
        System.out.println("Today: " + today);
        System.out.println("Start Date (next month): " + startNextMonth);
        System.out.println("Total occurrences: " + occurrences.size());

        assertThat(occurrences).isNotEmpty();
        assertThat(occurrences).allMatch(occurrence -> 
            !occurrence.isBefore(startNextMonth),
            "All occurrences should be on or after the start date"
        );

        System.out.println("✅ Weekly recurrence also respects start date!\n");
    }
}
