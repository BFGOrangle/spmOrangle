package com.spmorangle.crm.taskmanagement.enums;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAdjusters;

public enum CalendarView {
    DAY(1, 0, 0),
    WEEK(7, 0, 0),
    MONTH(0, 1, 0),
    TIMELINE(0, 3, 0);  // 3 months for timeline view

    private final int days;
    private final int months;
    private final int years;

    CalendarView(int days, int months, int years)  {
        this.days = days;
        this.months = months;
        this.years = years;
    }

    // Get end dates
    public OffsetDateTime calculateEndDate(OffsetDateTime startDate) {
        OffsetDateTime endDate = startDate;

        if(days > 0) {
            endDate = endDate.plusDays(days);
        }
        if(months > 0) {
            endDate = endDate.plusMonths(months);
        } 
        if(years > 0) {
            endDate = endDate.plusYears(years);
        }

        return endDate;
    }

    // Get display limit
    public CalendarViewWindow getCalendarViewWindow() {
        return getCalendarViewWindow(OffsetDateTime.now());
    }

    public CalendarViewWindow getCalendarViewWindow(OffsetDateTime referenceDate) {
        OffsetDateTime start;
        OffsetDateTime end;

        switch(this) {
            case DAY:
            // Start of day 
            start = referenceDate.withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = referenceDate.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                break;
                
            case WEEK:
                // Start of week (Monday) to end of week (Sunday)
                start = referenceDate
                    .with(TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = referenceDate
                    .with(TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY))
                    .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                break;
                
            case MONTH:
                // First day of month to last day of month
                start = referenceDate
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = referenceDate
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                break;

            case TIMELINE:
                // Timeline shows 1 month before to 2 months after (3 months total)
                start = referenceDate
                    .minusMonths(1)
                    .with(TemporalAdjusters.firstDayOfMonth())
                    .withHour(0).withMinute(0).withSecond(0).withNano(0);
                end = referenceDate
                    .plusMonths(2)
                    .with(TemporalAdjusters.lastDayOfMonth())
                    .withHour(23).withMinute(59).withSecond(59).withNano(999999999);
                break;
            default:
                throw new IllegalStateException("Unsupported calendar view: " + this);
        }
        
        return new CalendarViewWindow(start, end);
        }

    public static class CalendarViewWindow {
        private final OffsetDateTime start;
        private final OffsetDateTime end;
        
        public CalendarViewWindow(OffsetDateTime start, OffsetDateTime end) {
            this.start = start;
            this.end = end;
        }
        
        public OffsetDateTime getStart() {
            return start;
        }
        
        public OffsetDateTime getEnd() {
            return end;
        }
        
        @Override
        public String toString() {
            return String.format("CalendarViewWindow{start=%s, end=%s}", start, end);
        }
    }
    


}
