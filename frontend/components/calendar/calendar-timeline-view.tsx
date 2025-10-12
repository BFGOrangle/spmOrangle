/**
 * Timeline view component for the calendar
 * Shows tasks in a horizontal timeline with project grouping
 */

import React, { useState } from 'react';
import { format, startOfMonth, endOfMonth, eachDayOfInterval, addMonths, isSameDay } from 'date-fns';
import { TimelineViewProps as TimelineProps, CalendarEvent } from '../../types/calendar';
import { Card } from '../ui/card';
import { Button } from '../ui/button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

// Constants for timeline display
const MIN_EVENT_WIDTH_PERCENT = 2;
const MAX_EVENT_STACKS = 3;
const EVENT_STACK_HEIGHT = 16;

export const CalendarTimelineView: React.FC<TimelineProps> = ({
  events,
  currentDate,
  startDate,
  endDate,
  timeScale = 'days',
  onEventClick,
  onDateClick,
}) => {
  const [timelineStart, setTimelineStart] = useState(startDate || startOfMonth(currentDate));
  const [timelineEnd, setTimelineEnd] = useState(endDate || endOfMonth(addMonths(currentDate, 2)));

  // Generate time periods based on scale
  const generateTimePeriods = () => {
    switch (timeScale) {
      case 'days':
        return eachDayOfInterval({ start: timelineStart, end: timelineEnd });
      case 'weeks':
        // For weeks, we'll show weekly intervals
        const weeks = [];
        let current = timelineStart;
        while (current <= timelineEnd) {
          weeks.push(current);
          current = addMonths(current, 0);
          current.setDate(current.getDate() + 7);
        }
        return weeks;
      case 'months':
        const months = [];
        let currentMonth = timelineStart;
        while (currentMonth <= timelineEnd) {
          months.push(currentMonth);
          currentMonth = addMonths(currentMonth, 1);
        }
        return months;
      default:
        return eachDayOfInterval({ start: timelineStart, end: timelineEnd });
    }
  };

  const timePeriods = generateTimePeriods();

  // Group events by project
  const eventsByProject = events.reduce((acc, event) => {
    const projectKey = event.projectName || 'Personal Tasks';
    if (!acc[projectKey]) {
      acc[projectKey] = [];
    }
    acc[projectKey].push(event);
    return acc;
  }, {} as Record<string, CalendarEvent[]>);

  // Calculate event position and width based on timeline
  const getEventPosition = (event: CalendarEvent) => {
    const eventDate = event.dueDate || event.endDate;
    const totalDays = Math.ceil((timelineEnd.getTime() - timelineStart.getTime()) / (1000 * 60 * 60 * 24));
    const eventDay = Math.ceil((eventDate.getTime() - timelineStart.getTime()) / (1000 * 60 * 60 * 24));
    
    const leftPercent = (eventDay / totalDays) * 100;
    const widthPercent = Math.max(MIN_EVENT_WIDTH_PERCENT, 100 / totalDays); // Minimum width
    
    return {
      left: `${Math.max(0, Math.min(98, leftPercent))}%`,
      width: `${widthPercent}%`,
    };
  };

  const navigateTimeline = (direction: 'prev' | 'next') => {
    const timeSpan = timelineEnd.getTime() - timelineStart.getTime();
    
    if (direction === 'prev') {
      setTimelineStart(new Date(timelineStart.getTime() - timeSpan));
      setTimelineEnd(new Date(timelineEnd.getTime() - timeSpan));
    } else {
      setTimelineStart(new Date(timelineStart.getTime() + timeSpan));
      setTimelineEnd(new Date(timelineEnd.getTime() + timeSpan));
    }
  };

  return (
    <div className="h-full flex flex-col">
      {/* Timeline header */}
      <div className="border-b border-border bg-background p-2 sm:p-4">
        <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center mb-4 gap-2">
          <h2 className="text-lg sm:text-xl font-semibold text-foreground">
            Timeline View
          </h2>
          <div className="flex items-center space-x-2">
            <Button 
              variant="outline" 
              size="sm"
              onClick={() => navigateTimeline('prev')}
            >
              <ChevronLeft className="w-4 h-4" />
            </Button>
            <span className="text-xs sm:text-sm text-muted-foreground px-2 sm:px-3">
              {format(timelineStart, 'MMM d')} - {format(timelineEnd, 'MMM d, yyyy')}
            </span>
            <Button 
              variant="outline" 
              size="sm"
              onClick={() => navigateTimeline('next')}
            >
              <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        </div>

        {/* Time scale indicators */}
        <div className="relative h-6 sm:h-8 border-t border-border">
          <div className="absolute inset-0 flex overflow-x-auto">
            {timePeriods.map((period, index) => (
              <div 
                key={period.toISOString()}
                className="flex-shrink-0 border-r border-border text-center text-xs text-muted-foreground pt-1"
                style={{ minWidth: '40px', width: '60px' }}
              >
                {timeScale === 'days' && format(period, 'd')}
                {timeScale === 'weeks' && format(period, 'MMM d')}
                {timeScale === 'months' && format(period, 'MMM')}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Timeline body */}
      <div className="flex-1 overflow-auto">
        <div className="space-y-2 sm:space-y-4 p-2 sm:p-4">
          {Object.entries(eventsByProject).map(([projectName, projectEvents]) => (
            <div key={projectName} className="border border-border rounded-lg">
              {/* Project header */}
              <div className="bg-muted px-2 sm:px-4 py-2 border-b border-border">
                <h3 className="font-medium text-foreground text-sm sm:text-base">{projectName}</h3>
                <p className="text-xs sm:text-sm text-muted-foreground">
                  {(projectEvents as CalendarEvent[]).length} {(projectEvents as CalendarEvent[]).length === 1 ? 'task' : 'tasks'}
                </p>
              </div>

              {/* Project timeline */}
              <div className="p-2 sm:p-4 overflow-x-auto">
                <div className="relative h-12 sm:h-16 min-w-full">
                  {/* Timeline background */}
                  <div className="absolute inset-0 flex">
                    {timePeriods.map((period, index) => (
                      <div 
                        key={period.toISOString()}
                        className={`flex-shrink-0 border-r border-border/50 ${
                          isSameDay(period, currentDate) ? 'bg-primary/10' : ''
                        }`}
                        style={{ minWidth: '40px', width: '60px' }}
                      />
                    ))}
                  </div>

                  {/* Events */}
                  {(projectEvents as CalendarEvent[]).map((event, index) => {
                    const position = getEventPosition(event);
                    return (
                      <Card
                        key={event.id}
                        className={`absolute cursor-pointer hover:shadow-md transition-shadow ${event.color} text-white text-xs p-1 sm:p-2`}
                        style={{
                          left: position.left,
                          width: position.width,
                          top: `${(index % MAX_EVENT_STACKS) * EVENT_STACK_HEIGHT}px`, // Stack events vertically if they overlap
                          height: '14px',
                          minWidth: '40px',
                        }}
                        onClick={() => onEventClick(event)}
                        title={`${event.title} - Due: ${event.dueDate ? format(event.dueDate, 'MMM d, yyyy HH:mm') : 'No due date'}`}
                      >
                        <div className="truncate font-medium text-xs">
                          {event.title}
                        </div>
                      </Card>
                    );
                  })}
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Timeline legend */}
      <div className="border-t border-border bg-muted p-4">
        <div className="flex flex-wrap gap-4 text-xs">
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 bg-red-500 rounded"></div>
            <span>Bug</span>
          </div>
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 bg-blue-500 rounded"></div>
            <span>Feature</span>
          </div>
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 bg-yellow-500 rounded"></div>
            <span>Chore</span>
          </div>
          <div className="flex items-center space-x-1">
            <div className="w-3 h-3 bg-purple-500 rounded"></div>
            <span>Research</span>
          </div>
        </div>
      </div>
    </div>
  );
};
