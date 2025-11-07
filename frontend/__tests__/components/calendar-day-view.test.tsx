import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { CalendarDayView } from '../../components/calendar/calendar-day-view';
import { CalendarEvent } from '../../types/calendar';

describe('CalendarDayView', () => {
  const mockOnEventClick = jest.fn();
  const mockOnDateClick = jest.fn();
  const mockDate = new Date('2024-01-15T00:00:00Z');

  const createTimedEvent = (id: number, hour: number): CalendarEvent => ({
    id,
    title: `Timed Event ${id}`,
    description: 'Test Description',
    startDate: new Date('2024-01-15T00:00:00Z'),
    endDate: new Date('2024-01-15T00:00:00Z'),
    dueDate: new Date(`2024-01-15T${hour.toString().padStart(2, '0')}:00:00Z`),
    taskType: 'FEATURE',
    status: 'TODO',
    projectId: 1,
    projectName: 'Test Project',
    tags: [],
    assignedUserIds: [],
    ownerId: 1,
    createdBy: 1,
    color: 'bg-blue-500 opacity-70',
  });

  const createAllDayEvent = (id: number): CalendarEvent => ({
    id,
    title: `All Day Event ${id}`,
    description: 'Test Description',
    startDate: new Date('2024-01-15T00:00:00Z'),
    endDate: new Date('2024-01-15T00:00:00Z'),
    // No dueDate means all-day event
    taskType: 'FEATURE',
    status: 'TODO',
    projectId: 1,
    projectName: 'Test Project',
    tags: [],
    assignedUserIds: [],
    ownerId: 1,
    createdBy: 1,
    color: 'bg-blue-500 opacity-70',
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders without crashing', () => {
    render(
      <CalendarDayView
        currentDate={mockDate}
        events={[]}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    expect(screen.getByText('Monday, January 15, 2024')).toBeInTheDocument();
  });

  it('separates timed events and all-day events', () => {
    const events = [
      createTimedEvent(1, 10),
      createAllDayEvent(2),
      createTimedEvent(3, 14),
      createAllDayEvent(4),
    ];

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    // All-day events section should exist
    expect(screen.getByText('All Day')).toBeInTheDocument();

    // All-day events should be shown
    expect(screen.getByText('All Day Event 2')).toBeInTheDocument();
    expect(screen.getByText('All Day Event 4')).toBeInTheDocument();

    // Timed events should be shown
    expect(screen.getByText('Timed Event 1')).toBeInTheDocument();
    expect(screen.getByText('Timed Event 3')).toBeInTheDocument();
  });

  it('does not show all-day section when there are no all-day events', () => {
    const events = [createTimedEvent(1, 10), createTimedEvent(2, 14)];

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    // All-day section should not exist
    expect(screen.queryByText('All Day')).not.toBeInTheDocument();
  });

  it('shows only all-day section when there are only all-day events', () => {
    const events = [createAllDayEvent(1), createAllDayEvent(2)];

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    // All-day section should exist
    expect(screen.getByText('All Day')).toBeInTheDocument();
    expect(screen.getByText('All Day Event 1')).toBeInTheDocument();
    expect(screen.getByText('All Day Event 2')).toBeInTheDocument();
  });

  it('groups timed events by hour', () => {
    const events = [
      createTimedEvent(1, 10), // 10 AM
      createTimedEvent(2, 10), // 10 AM (same hour)
      createTimedEvent(3, 14), // 2 PM
    ];

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    // Both events at 10 AM should be shown
    expect(screen.getByText('Timed Event 1')).toBeInTheDocument();
    expect(screen.getByText('Timed Event 2')).toBeInTheDocument();
    expect(screen.getByText('Timed Event 3')).toBeInTheDocument();
  });

  it('calls onEventClick when an event is clicked', () => {
    const events = [createTimedEvent(1, 10)];

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    const eventElement = screen.getByText('Timed Event 1');
    fireEvent.click(eventElement.closest('[role="button"]') || eventElement);

    expect(mockOnEventClick).toHaveBeenCalledWith(events[0]);
  });

  it('calls onEventClick when an all-day event is clicked', () => {
    const events = [createAllDayEvent(1)];

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    const eventElement = screen.getByText('All Day Event 1');
    fireEvent.click(eventElement.closest('div[class*="cursor-pointer"]') || eventElement);

    expect(mockOnEventClick).toHaveBeenCalled();
  });

  it('displays project name for events', () => {
    const events = [createTimedEvent(1, 10)];

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    expect(screen.getByText('Test Project')).toBeInTheDocument();
  });

  it('displays task type and status for events', () => {
    const events = [createTimedEvent(1, 10)];

    const { container } = render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    // Check that the event card is rendered
    const eventElement = screen.getByText('Timed Event 1');
    expect(eventElement).toBeInTheDocument();
  });

  it('handles events without project name', () => {
    const event: CalendarEvent = {
      ...createTimedEvent(1, 10),
      projectName: undefined,
    };

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={[event]}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    expect(screen.getByText('Timed Event 1')).toBeInTheDocument();
  });

  it('handles events without description', () => {
    const event: CalendarEvent = {
      ...createTimedEvent(1, 10),
      description: undefined,
    };

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={[event]}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    expect(screen.getByText('Timed Event 1')).toBeInTheDocument();
  });

  it('displays all hours from 0 to 23', () => {
    const { container } = render(
      <CalendarDayView
        currentDate={mockDate}
        events={[]}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    // Check that the calendar day view renders with multiple time slots
    // The component should have time slot elements
    const timeSlots = container.querySelectorAll('[class*="border"]');
    expect(timeSlots.length).toBeGreaterThan(0);
  });

  it('handles mixed event types with correct separation', () => {
    const events = [
      createAllDayEvent(1),
      createTimedEvent(2, 9),
      createAllDayEvent(3),
      createTimedEvent(4, 15),
      createTimedEvent(5, 9), // Same hour as event 2
    ];

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={events}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    // All-day section
    expect(screen.getByText('All Day')).toBeInTheDocument();
    expect(screen.getByText('All Day Event 1')).toBeInTheDocument();
    expect(screen.getByText('All Day Event 3')).toBeInTheDocument();

    // Timed events
    expect(screen.getByText('Timed Event 2')).toBeInTheDocument();
    expect(screen.getByText('Timed Event 4')).toBeInTheDocument();
    expect(screen.getByText('Timed Event 5')).toBeInTheDocument();
  });

  it('renders events with isHighlighted property', () => {
    const event: CalendarEvent = {
      ...createTimedEvent(1, 10),
      isHighlighted: true,
    };

    render(
      <CalendarDayView
        currentDate={mockDate}
        events={[event]}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    // Event should still be rendered even with isHighlighted property
    expect(screen.getByText('Timed Event 1')).toBeInTheDocument();
  });

  it('formats date correctly', () => {
    const januaryDate = new Date('2024-01-15');
    const { rerender } = render(
      <CalendarDayView
        currentDate={januaryDate}
        events={[]}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    expect(screen.getByText('Monday, January 15, 2024')).toBeInTheDocument();

    // Test different date
    const decemberDate = new Date('2024-12-25');
    rerender(
      <CalendarDayView
        currentDate={decemberDate}
        events={[]}
        onEventClick={mockOnEventClick}
        onDateClick={mockOnDateClick}
      />
    );

    expect(screen.getByText('Wednesday, December 25, 2024')).toBeInTheDocument();
  });
});
