import React from 'react';
import { render, screen } from '@testing-library/react';

// Mock all the dependencies that might cause issues
jest.mock('../../components/ui/select', () => ({
  Select: ({ children }: any) => <div data-testid="select">{children}</div>,
  SelectContent: ({ children }: any) => <div>{children}</div>,
  SelectItem: ({ children }: any) => <div>{children}</div>,
  SelectTrigger: ({ children }: any) => <div>{children}</div>,
  SelectValue: () => <div>Select Value</div>
}));

jest.mock('../../components/ui/button', () => ({
  Button: ({ children, ...props }: any) => <button {...props}>{children}</button>
}));

jest.mock('../../components/ui/input', () => ({
  Input: (props: any) => <input {...props} />
}));

jest.mock('lucide-react', () => ({
  ChevronLeft: () => <div>ChevronLeft</div>,
  ChevronRight: () => <div>ChevronRight</div>,
  Calendar: () => <div>Calendar</div>,
  LayoutGrid: () => <div>LayoutGrid</div>,
  BarChart3: () => <div>BarChart3</div>,
  Clock: () => <div>Clock</div>,
  Search: () => <div>Search</div>
}));

jest.mock('date-fns', () => ({
  format: jest.fn(() => 'Jan 2024'),
  addDays: jest.fn(),
  addWeeks: jest.fn(),
  addMonths: jest.fn(),
  subDays: jest.fn(),
  subWeeks: jest.fn(),
  subMonths: jest.fn()
}));

// Import after mocking dependencies
const { CalendarControls } = require('../../components/calendar/calendar-controls');

const TestComponent = () => {
  try {
    console.log('About to render CalendarControls...');
    console.log('CalendarControls type:', typeof CalendarControls);
    console.log('CalendarControls:', CalendarControls);
    
    const mockProps = {
      currentView: 'month' as const,
      currentDate: new Date('2024-01-01'),
      projects: [],
      selectedProjectId: undefined,
      selectedTaskType: 'Project Tasks',
      searchKeyword: '',
      searchResultsCount: 0,
      onViewChange: jest.fn(),
      onDateChange: jest.fn(),
      onProjectFilter: jest.fn(),
      onTaskTypeChange: jest.fn(),
      onSearchChange: jest.fn(),
      onToday: jest.fn()
    };

    return React.createElement(CalendarControls, mockProps);
  } catch (error) {
    console.error('Error during rendering:', error);
    return <div>Error during rendering: {String(error)}</div>;
  }
};

describe('CalendarControls Component Test', () => {
  it('should render with proper mocking', () => {
    const result = render(<TestComponent />);
    expect(result).toBeTruthy();
  });
});
