/**
 * Month view component for the calendar
 * Shows tasks across a full month in a traditional calendar grid
 */

import React from 'react';
import { format, isToday, isSameMonth, startOfMonth, endOfMonth } from 'date-fns';
import { CalendarViewProps, CalendarEvent } from '../../types/calendar';
import { getCalendarWeeks, filterEventsForMonthView, groupEventsByDateForMonthView } from '../../lib/calendar-utils';
import { highlightSearchTerm, getSearchHighlightClasses } from '../../lib/search-utils';
import { Card } from '../ui/card';

// Constants for event display
const MAX_EVENTS_PER_DAY = 2;

interface MonthViewProps extends CalendarViewProps {}

export const CalendarMonthView: React.FC<MonthViewProps> = ({
  events,
  currentDate,
  searchKeyword = '',
  onEventClick,
  onDateClick,
}) => {
  const calendarWeeks = getCalendarWeeks(currentDate);
  const monthStart = startOfMonth(currentDate);
  const monthEnd = endOfMonth(currentDate);
  
  // Filter events for the month view and get all calendar days
  const monthEvents = filterEventsForMonthView(events, monthStart, monthEnd);
  const allCalendarDays = calendarWeeks.flat();
  const eventsByDate = groupEventsByDateForMonthView(monthEvents, allCalendarDays);

  const daysOfWeek = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  return (
    <div className="h-full flex flex-col">
      {/* Month header */}
      <div className="border-b border-border bg-background p-4">
        <h2 className="text-xl font-semibold text-foreground">
          {format(currentDate, 'MMMM yyyy')}
        </h2>
        <p className="text-sm text-muted-foreground mt-1">
          {monthEvents.length} {monthEvents.length === 1 ? 'task' : 'tasks'} this month
        </p>
      </div>

      {/* Days of week header */}
      <div className="border-b bg-muted">
        <div className="grid grid-cols-7 gap-px">
          {daysOfWeek.map((day) => (
            <div key={day} className="bg-background p-3 text-center">
              <span className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                {day}
              </span>
            </div>
          ))}
        </div>
      </div>

      {/* Calendar grid */}
      <div className="flex-1 overflow-auto">
        <div className="grid grid-rows-6 gap-px bg-border h-full">
          {calendarWeeks.map((week, weekIndex) => (
            <div key={weekIndex} className="grid grid-cols-7 gap-px">
              {week.map((day) => {
                const dayKey = format(day, 'yyyy-MM-dd');
                const dayEvents = eventsByDate[dayKey] || [];
                const isCurrentMonth = isSameMonth(day, currentDate);
                const isDayToday = isToday(day);
                
                return (
                  <div 
                    key={day.toISOString()}
                    className={`bg-background min-h-24 p-1 cursor-pointer hover:bg-muted/50 relative ${
                      !isCurrentMonth ? 'bg-muted text-muted-foreground' : ''
                    } ${isDayToday ? 'bg-primary/10' : ''}`}
                    onClick={() => onDateClick(day)}
                  >
                    {/* Day number */}
                    <div className="flex justify-between items-start mb-1">
                      <span 
                        className={`text-sm font-medium ${
                          isDayToday 
                            ? 'text-primary bg-primary/20 rounded-full w-6 h-6 flex items-center justify-center' 
                            : isCurrentMonth ? 'text-foreground' : 'text-muted-foreground'
                        }`}
                      >
                        {format(day, 'd')}
                      </span>
                      
                      {dayEvents.length > MAX_EVENTS_PER_DAY && (
                        <span className="text-xs text-muted-foreground">
                          +{dayEvents.length - MAX_EVENTS_PER_DAY}
                        </span>
                      )}
                    </div>

                    {/* Events */}
                    <div className="space-y-1">
                      {dayEvents.slice(0, MAX_EVENTS_PER_DAY).map((event: CalendarEvent) => (
                        <div
                          key={event.id}
                          className={`text-xs p-1 rounded cursor-pointer hover:opacity-80 ${event.color} text-white truncate ${
                            getSearchHighlightClasses(event.isHighlighted || false, !!searchKeyword)
                          }`}
                          onClick={(e) => {
                            e.stopPropagation();
                            onEventClick(event);
                          }}
                          title={`${event.title} - ${event.projectName || 'No Project'}`}
                        >
                          <div className="font-medium truncate">
                            {searchKeyword ? highlightSearchTerm(event.title, searchKeyword) : event.title}
                          </div>
                          {event.dueDate && (
                            <div className="text-xs opacity-90">
                              <strong>Due:</strong> {format(event.dueDate, 'HH:mm')}
                            </div>
                          )}
                        </div>
                      ))}
                    </div>

                    {/* Overflow indicator */}
                    {dayEvents.length > MAX_EVENTS_PER_DAY && (
                      <div className="absolute bottom-1 right-1">
                        <div className="w-2 h-2 bg-muted-foreground rounded-full"></div>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>

      {/* Month summary
      <div className="border-t border-border bg-muted p-4">
        <div className="grid grid-cols-4 gap-4 text-sm">
          <div className="text-center">
            <div className="font-medium text-foreground">
              {events.filter(e => e.status === 'TODO').length}
            </div>
            <div className="text-muted-foreground">To Do</div>
          </div>
          <div className="text-center">
            <div className="font-medium text-foreground">
              {events.filter(e => e.status === 'IN_PROGRESS').length}
            </div>
            <div className="text-muted-foreground">In Progress</div>
          </div>
          <div className="text-center">
            <div className="font-medium text-foreground">
              {events.filter(e => e.status === 'COMPLETED').length}
            </div>
            <div className="text-muted-foreground">Completed</div>
          </div>
          <div className="text-center">
            <div className="font-medium text-foreground">
              {events.filter(e => e.status === 'BLOCKED').length}
            </div>
            <div className="text-muted-foreground">Blocked</div>
          </div>
        </div>
      </div> */}
    </div>
  );
};
