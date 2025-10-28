package com.spmorangle.crm.taskmanagement.enums;

import com.spmorangle.crm.taskmanagement.enums.CalendarView.CalendarViewWindow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CalendarView Enum Tests")
class CalendarViewTest {

    @Test
    @DisplayName("DAY view should calculate correct end date")
    void dayView_ShouldCalculateCorrectEndDate() {
        // Arrange
        OffsetDateTime startDate = OffsetDateTime.parse("2024-01-15T09:00:00Z");
        
        // Act
        OffsetDateTime endDate = CalendarView.DAY.calculateEndDate(startDate);
        
        // Assert
        assertThat(endDate).isEqualTo(startDate.plusDays(1));
    }

    @Test
    @DisplayName("WEEK view should calculate correct end date")
    void weekView_ShouldCalculateCorrectEndDate() {
        // Arrange
        OffsetDateTime startDate = OffsetDateTime.parse("2024-01-15T09:00:00Z");
        
        // Act
        OffsetDateTime endDate = CalendarView.WEEK.calculateEndDate(startDate);
        
        // Assert
        assertThat(endDate).isEqualTo(startDate.plusDays(7));
    }

    @Test
    @DisplayName("MONTH view should calculate correct end date")
    void monthView_ShouldCalculateCorrectEndDate() {
        // Arrange
        OffsetDateTime startDate = OffsetDateTime.parse("2024-01-15T09:00:00Z");
        
        // Act
        OffsetDateTime endDate = CalendarView.MONTH.calculateEndDate(startDate);
        
        // Assert
        assertThat(endDate).isEqualTo(startDate.plusMonths(1));
    }

    @Test
    @DisplayName("TIMELINE view should calculate correct end date (3 months)")
    void timelineView_ShouldCalculateCorrectEndDate() {
        // Arrange
        OffsetDateTime startDate = OffsetDateTime.parse("2024-01-15T09:00:00Z");
        
        // Act
        OffsetDateTime endDate = CalendarView.TIMELINE.calculateEndDate(startDate);
        
        // Assert
        assertThat(endDate).isEqualTo(startDate.plusMonths(3));
    }

    @Test
    @DisplayName("getCalendarViewWindow should return correct window for DAY view")
    void getCalendarViewWindow_ForDayView_ShouldReturnCorrectWindow() {
        // Arrange
        OffsetDateTime referenceDate = OffsetDateTime.parse("2024-01-15T09:00:00Z");
        
        // Act
        CalendarViewWindow window = CalendarView.DAY.getCalendarViewWindow(referenceDate);
        
        // Assert
        assertThat(window).isNotNull();
        assertThat(window.getStart()).isNotNull();
        assertThat(window.getEnd()).isNotNull();
        assertThat(window.getEnd()).isAfter(window.getStart());
    }

    @Test
    @DisplayName("getCalendarViewWindow should return correct window for WEEK view")
    void getCalendarViewWindow_ForWeekView_ShouldReturnCorrectWindow() {
        // Arrange
        OffsetDateTime referenceDate = OffsetDateTime.parse("2024-01-15T09:00:00Z");
        
        // Act
        CalendarViewWindow window = CalendarView.WEEK.getCalendarViewWindow(referenceDate);
        
        // Assert
        assertThat(window).isNotNull();
        assertThat(window.getStart()).isNotNull();
        assertThat(window.getEnd()).isNotNull();
        assertThat(window.getEnd()).isAfter(window.getStart());
    }

    @Test
    @DisplayName("getCalendarViewWindow should return correct window for MONTH view")
    void getCalendarViewWindow_ForMonthView_ShouldReturnCorrectWindow() {
        // Arrange
        OffsetDateTime referenceDate = OffsetDateTime.parse("2024-01-15T09:00:00Z");
        
        // Act
        CalendarViewWindow window = CalendarView.MONTH.getCalendarViewWindow(referenceDate);
        
        // Assert
        assertThat(window).isNotNull();
        assertThat(window.getStart()).isNotNull();
        assertThat(window.getEnd()).isNotNull();
        assertThat(window.getEnd()).isAfter(window.getStart());
    }

    @Test
    @DisplayName("getCalendarViewWindow without parameter should use current date")
    void getCalendarViewWindow_WithoutParameter_ShouldUseCurrentDate() {
        // Act
        CalendarViewWindow window = CalendarView.DAY.getCalendarViewWindow();
        
        // Assert
        assertThat(window).isNotNull();
        assertThat(window.getStart()).isNotNull();
        assertThat(window.getEnd()).isNotNull();
        assertThat(window.getEnd()).isAfter(window.getStart());
    }

    @Test
    @DisplayName("All calendar views should be defined")
    void allCalendarViews_ShouldBeDefined() {
        // Assert
        assertThat(CalendarView.values()).hasSize(4);
        assertThat(CalendarView.values()).contains(
            CalendarView.DAY,
            CalendarView.WEEK,
            CalendarView.MONTH,
            CalendarView.TIMELINE
        );
    }

    @Test
    @DisplayName("Calendar view enum should have correct string representation")
    void calendarView_ShouldHaveCorrectStringRepresentation() {
        // Assert
        assertThat(CalendarView.DAY.name()).isEqualTo("DAY");
        assertThat(CalendarView.WEEK.name()).isEqualTo("WEEK");
        assertThat(CalendarView.MONTH.name()).isEqualTo("MONTH");
        assertThat(CalendarView.TIMELINE.name()).isEqualTo("TIMELINE");
    }

    @Test
    @DisplayName("Calendar view can be parsed from string")
    void calendarView_CanBeParsedFromString() {
        // Act & Assert
        assertThat(CalendarView.valueOf("DAY")).isEqualTo(CalendarView.DAY);
        assertThat(CalendarView.valueOf("WEEK")).isEqualTo(CalendarView.WEEK);
        assertThat(CalendarView.valueOf("MONTH")).isEqualTo(CalendarView.MONTH);
        assertThat(CalendarView.valueOf("TIMELINE")).isEqualTo(CalendarView.TIMELINE);
    }
}
