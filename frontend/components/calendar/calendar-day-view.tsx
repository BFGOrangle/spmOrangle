/**
 * Day view component for the calendar
 * Shows tasks for a single day with hourly time slots
 */

import React from 'react';
import { format, startOfDay, endOfDay } from 'date-fns';
import { CalendarViewProps, CalendarEvent } from '../../types/calendar';
import { filterEventsByDateRange, isTaskOverdue } from '../../lib/calendar-utils';
import { highlightSearchTerm, getSearchHighlightClasses } from '../../lib/search-utils';
import { Card } from '../ui/card';
import { cn } from '@/lib/utils';

interface DayViewProps extends CalendarViewProps {}

export const CalendarDayView: React.FC<DayViewProps> = ({
  events,
  currentDate,
  searchKeyword = '',
  onEventClick,
  onDateClick,
}) => {
  const dayStart = startOfDay(currentDate);
  const dayEnd = endOfDay(currentDate);
  const dayEvents = filterEventsByDateRange(events, dayStart, dayEnd);

  // Generate hourly time slots
  const timeSlots = Array.from({ length: 24 }, (_, i) => {
    const hour = i;
    return {
      time: `${hour.toString().padStart(2, '0')}:00`,
      hour,
    };
  });

  // Group events by hour (based on due time if available, otherwise show at top)
  const eventsByHour = dayEvents.reduce((acc, event) => {
    let hour = 0; // Default to beginning of day
    
    if (event.dueDate) {
      hour = event.dueDate.getHours();
    } else if (event.endDate) {
      hour = event.endDate.getHours();
    }
    
    if (!acc[hour]) {
      acc[hour] = [];
    }
    acc[hour].push(event);
    return acc;
  }, {} as Record<number, CalendarEvent[]>);

  return (
    <div className="h-full overflow-auto">
      <div className="sticky top-0 bg-background border-b border-border p-2 sm:p-4 z-10">
        <h2 className="text-lg sm:text-xl font-semibold text-foreground">
          {format(currentDate, 'EEEE, MMMM d, yyyy')}
        </h2>
        <p className="text-xs sm:text-sm text-muted-foreground mt-1">
          {dayEvents.length} {dayEvents.length === 1 ? 'task' : 'tasks'} scheduled
        </p>
      </div>

{/* Events column */}
<div className="flex-1 max-h-[500px] overflow-y-auto border-y border-primary">
  <div className="flex">
    {/* Time column */}
    <div className="w-16 sm:w-20 flex-shrink-0 border-r border-border">
      {timeSlots.map(({ time, hour }) => (
        <div key={time} className="h-12 sm:h-16 border-b border-border/50 flex items-start pt-1">
          <span className="text-xs text-muted-foreground px-1 sm:px-2">{time}</span>
        </div>
      ))}
    </div>

    {/* Events column - wrapped in flex-1 container */}
    <div className="flex-1">
      {timeSlots.map(({ hour }) => (
        <div
          key={hour}
          className="h-12 sm:h-16 border-b border-border/50 relative p-1 cursor-pointer hover:bg-muted/50"
          onClick={() => onDateClick(currentDate)}
        >
          {eventsByHour[hour] && (
            <div className="flex flex-wrap gap-1">
              {eventsByHour[hour].map((event) => {
                // Check if event is overdue
                const eventIsOverdue = event.dueDate 
                  ? isTaskOverdue({ 
                      dueDateTime: event.dueDate.toISOString(), 
                      status: event.status 
                    })
                  : false;
                
                return (
                  <Card
                    key={event.id}
                    className={cn(
                      "p-1 sm:p-2 cursor-pointer hover:shadow-md transition-shadow text-white text-xs flex-1 min-w-0",
                      eventIsOverdue 
                        ? "bg-red-600 ring-2 ring-red-600 ring-offset-1" 
                        : event.color,
                      getSearchHighlightClasses(event.isHighlighted || false, !!searchKeyword)
                    )}
                    onClick={(e) => {
                      e.stopPropagation();
                      onEventClick(event);
                    }}
                    title={eventIsOverdue ? `${event.title} (OVERDUE)` : event.title}
                  >
                    <div className="font-medium truncate">
                      {eventIsOverdue && <span className="font-bold">⚠️ </span>}
                      {searchKeyword ? highlightSearchTerm(event.title, searchKeyword) : event.title}
                    </div>
                    <div className="text-xs opacity-90 hidden sm:block">
                      {event.projectName && (
                        <span className="mr-2">
                          {searchKeyword ? highlightSearchTerm(event.projectName, searchKeyword) : event.projectName}
                        </span>
                      )}
                      <span className="bg-white/20 px-1 rounded">
                        {event.taskType}
                      </span>
                    </div>
                  </Card>
                );
              })}
            </div>
          )}
        </div>
      ))}
    </div>
  </div>
</div>

      {/* All day events section */}
      {dayEvents.filter(event => !event.dueDate || event.dueDate.getHours() === 0).length > 0 && (
        <div className="border-t border-border p-4">
          <h3 className="text-sm font-medium text-foreground mb-2">All Day</h3>
          <div className="grid gap-2">
            {dayEvents
              .filter(event => !event.dueDate || event.dueDate.getHours() === 0)
              .map((event) => {
                // Check if event is overdue
                const eventIsOverdue = event.dueDate 
                  ? isTaskOverdue({ 
                      dueDateTime: event.dueDate.toISOString(), 
                      status: event.status 
                    })
                  : false;
                
                return (
                  <Card
                    key={event.id}
                    className={cn(
                      "p-3 cursor-pointer hover:shadow-md transition-shadow text-white",
                      eventIsOverdue 
                        ? "bg-red-600 ring-2 ring-red-600 ring-offset-1" 
                        : event.color,
                      getSearchHighlightClasses(event.isHighlighted || false, !!searchKeyword)
                    )}
                    onClick={() => onEventClick(event)}
                    title={eventIsOverdue ? `${event.title} (OVERDUE)` : event.title}
                  >
                    <div className="font-medium">
                      {eventIsOverdue && <span className="font-bold">⚠️ </span>}
                      {searchKeyword ? highlightSearchTerm(event.title, searchKeyword) : event.title}
                    </div>
                    <div className="text-sm opacity-90 mt-1">
                      {event.projectName && (
                        <span className="mr-2">
                          {searchKeyword ? highlightSearchTerm(event.projectName, searchKeyword) : event.projectName}
                        </span>
                      )}
                      <span className="bg-white/20 px-2 py-1 rounded text-xs">
                        {event.taskType} • {event.status}
                      </span>
                    </div>
                    {event.description && (
                      <div className="text-sm opacity-80 mt-1 line-clamp-2">
                        {searchKeyword ? highlightSearchTerm(event.description, searchKeyword) : event.description}
                      </div>
                    )}
                  </Card>
                );
              })}
          </div>
        </div>
      )}
    </div>
  );
};
