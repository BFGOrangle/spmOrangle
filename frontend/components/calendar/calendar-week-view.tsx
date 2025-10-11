/**
 * Week view component for the calendar
 * Shows tasks across 7 days in a grid layout
 */

import React from 'react';
import { format, isToday, isSameDay } from 'date-fns';
import { CalendarViewProps, CalendarEvent } from '../../types/calendar';
import { getWeekDays, filterEventsForWeekView, groupEventsByDateForWeekView } from '../../lib/calendar-utils';
import { Card } from '../ui/card';

interface WeekViewProps extends CalendarViewProps {}

export const CalendarWeekView: React.FC<WeekViewProps> = ({
  events,
  currentDate,
  onEventClick,
  onDateClick,
}) => {
  const weekDays = getWeekDays(currentDate);
  const weekStart = weekDays[0];
  const weekEnd = weekDays[weekDays.length - 1];
  const weekEvents = filterEventsForWeekView(events, weekStart, weekEnd);
  const eventsByDate = groupEventsByDateForWeekView(weekEvents, weekDays);

  return (
    <div className="h-full flex flex-col">
      {/* Week header */}
      <div className="border-b border-border bg-background">
        <div className="grid grid-cols-7 gap-px bg-border">
          {weekDays.map((day) => (
            <div 
              key={day.toISOString()} 
              className={`bg-background p-2 sm:p-4 text-center ${isToday(day) ? 'bg-primary/10' : ''}`}
            >
              <div className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
                {format(day, 'EEE')}
              </div>
              <div 
                className={`mt-1 text-base sm:text-lg font-semibold cursor-pointer hover:bg-muted/50 rounded px-1 sm:px-2 py-1 ${
                  isToday(day) 
                    ? 'text-primary bg-primary/20' 
                    : 'text-foreground'
                }`}
                onClick={() => onDateClick(day)}
              >
                {format(day, 'd')}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Week body */}
      <div className="flex-1 overflow-auto">
        <div className="grid grid-cols-7 gap-px bg-border h-full">
          {weekDays.map((day) => {
            const dayKey = format(day, 'yyyy-MM-dd');
            const dayEvents = eventsByDate[dayKey] || [];
            
            return (
              <div 
                key={day.toISOString()} 
                className={`bg-background min-h-24 sm:min-h-32 p-1 sm:p-2 cursor-pointer hover:bg-muted/50 ${
                  isToday(day) ? 'bg-primary/10' : ''
                }`}
                onClick={() => onDateClick(day)}
              >
                <div className="space-y-1">
                  {dayEvents.slice(0, window.innerWidth < 640 ? 2 : 3).map((event: CalendarEvent) => (
                    <Card
                      key={event.id}
                      className={`p-1 sm:p-2 cursor-pointer hover:shadow-md transition-shadow ${event.color} text-white text-xs`}
                      onClick={(e) => {
                        e.stopPropagation();
                        onEventClick(event);
                      }}
                    >
                      <div className="font-medium truncate">{event.title}</div>
                      <div className="text-xs opacity-90 mt-1 hidden sm:block">
                        {event.dueDate && (
                          <span className="mr-1">
                            {format(event.dueDate, 'HH:mm')}
                          </span>
                        )}
                        <span className="bg-white/20 px-1 rounded">
                          {event.taskType}
                        </span>
                      </div>
                    </Card>
                  ))}
                  
                  {dayEvents.length > (window.innerWidth < 640 ? 2 : 3) && (
                    <div className="text-xs text-muted-foreground text-center py-1">
                      +{dayEvents.length - (window.innerWidth < 640 ? 2 : 3)} more
                    </div>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Week summary */}
      <div className="border-t border-border bg-muted p-4">
        <div className="flex justify-between items-center text-sm text-muted-foreground">
          <span>
            Week of {format(weekDays[0], 'MMM d')} - {format(weekDays[6], 'MMM d, yyyy')}
          </span>
          <span>
            {weekEvents.length} {weekEvents.length === 1 ? 'task' : 'tasks'} this week
          </span>
        </div>
      </div>
    </div>
  );
};
