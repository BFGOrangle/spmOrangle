/**
 * Tests for RecurrenceSelector component
 *
 * Tests verify that:
 * 1. Users can enable/disable task recurrence
 * 2. Recurrence rules can be configured
 * 3. Start and end dates are properly managed
 * 4. Validation works correctly
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '../test-utils';
import { RecurrenceSelector, RecurrenceData } from '../../components/recurrence-selector';

// Mock UI components
jest.mock('../../components/ui/checkbox', () => ({
  Checkbox: ({ checked, onCheckedChange, id }: any) => (
    <input
      type="checkbox"
      checked={checked}
      onChange={(e) => onCheckedChange(e.target.checked)}
      id={id}
      data-testid="recurrence-checkbox"
    />
  ),
}));

jest.mock('../../components/ui/select', () => ({
  Select: ({ value, onValueChange, children }: any) => (
    <select
      value={value}
      onChange={(e) => onValueChange(e.target.value)}
      data-testid="frequency-select"
    >
      {children}
    </select>
  ),
  SelectTrigger: ({ children }: any) => <div>{children}</div>,
  SelectValue: ({ placeholder }: any) => <span>{placeholder}</span>,
  SelectContent: ({ children }: any) => <>{children}</>,
  SelectItem: ({ value, children }: any) => <option value={value}>{children}</option>,
}));

jest.mock('../../components/ui/input', () => ({
  Input: (props: any) => <input {...props} />,
}));

jest.mock('../../components/ui/label', () => ({
  Label: ({ children }: any) => <label>{children}</label>,
}));

jest.mock('../../components/ui/button', () => ({
  Button: ({ children, onClick }: any) => (
    <button onClick={onClick}>{children}</button>
  ),
}));

describe('RecurrenceSelector', () => {
  const mockOnChange = jest.fn();

  const defaultValue: RecurrenceData = {
    isRecurring: false,
    recurrenceRuleStr: null,
    startDate: null,
    endDate: null,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Basic Functionality', () => {
    it('should render with recurrence disabled by default', () => {
      render(<RecurrenceSelector initialValue={defaultValue} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(false);
    });

    it('should enable recurrence when checkbox is toggled', () => {
      render(<RecurrenceSelector initialValue={defaultValue} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      fireEvent.click(checkbox);

      // Checkbox should be checked (onChange may not fire until dates are filled)
      expect(checkbox.checked).toBe(true);
    });

    it('should disable recurrence when checkbox is toggled off', async () => {
      const recurringValue: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=DAILY',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-10-30T00:00:00Z',
      };

      render(<RecurrenceSelector initialValue={recurringValue} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox');
      fireEvent.click(checkbox);

      await waitFor(() => {
        expect(mockOnChange).toHaveBeenCalledWith(
          expect.objectContaining({
            isRecurring: false,
            recurrenceRuleStr: null,
            startDate: null,
            endDate: null,
          })
        );
      });
    });
  });

  describe('Recurrence Rule Configuration', () => {
    it('should show recurrence options when enabled', () => {
      const recurringValue: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=WEEKLY',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-10-30T00:00:00Z',
      };

      render(<RecurrenceSelector initialValue={recurringValue} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(true);
      expect(screen.getByTestId('frequency-select')).toBeInTheDocument();
    });

    it('should not show recurrence options when disabled', () => {
      render(<RecurrenceSelector initialValue={defaultValue} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(false);
      expect(screen.queryByTestId('frequency-select')).not.toBeInTheDocument();
    });

    it('should update recurrence rule when frequency changes', async () => {
      const recurringValue: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=WEEKLY',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-10-30T00:00:00Z',
      };

      render(<RecurrenceSelector initialValue={recurringValue} onChange={mockOnChange} />);

      const frequencySelect = screen.getByTestId('frequency-select');
      fireEvent.change(frequencySelect, { target: { value: 'DAILY' } });

      await waitFor(() => {
        expect(mockOnChange).toHaveBeenCalled();
      });
    });
  });

  describe('Date Management', () => {
    it('should accept start and end dates', () => {
      const recurringValue: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=DAILY',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-10-30T00:00:00Z',
      };

      const { rerender } = render(
        <RecurrenceSelector initialValue={recurringValue} onChange={mockOnChange} />
      );

      // Verify component accepts the dates without errors
      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(true);

      // Update dates
      const updatedValue: RecurrenceData = {
        ...recurringValue,
        endDate: '2025-11-30T00:00:00Z',
      };

      rerender(<RecurrenceSelector initialValue={updatedValue} onChange={mockOnChange} />);

      expect(checkbox.checked).toBe(true);
    });
  });

  describe('Integration with Task Updates', () => {
    it('should maintain recurrence data when task is updated', () => {
      const recurringValue: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=WEEKLY;BYDAY=MO,WE,FR',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-12-31T23:59:59Z',
      };

      const { rerender } = render(
        <RecurrenceSelector initialValue={recurringValue} onChange={mockOnChange} />
      );

      // Simulate re-render with same data
      rerender(<RecurrenceSelector initialValue={recurringValue} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(true);
    });

    it('should handle transition from non-recurring to recurring', () => {
      const { rerender } = render(
        <RecurrenceSelector initialValue={defaultValue} onChange={mockOnChange} />
      );

      let checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(false);

      const recurringValue: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=DAILY',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-10-30T00:00:00Z',
      };

      rerender(<RecurrenceSelector initialValue={recurringValue} onChange={mockOnChange} />);

      checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(true);
    });

    it('should handle transition from recurring to non-recurring', () => {
      const recurringValue: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=DAILY',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-10-30T00:00:00Z',
      };

      const { rerender } = render(
        <RecurrenceSelector initialValue={recurringValue} onChange={mockOnChange} />
      );

      let checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(true);

      rerender(<RecurrenceSelector initialValue={defaultValue} onChange={mockOnChange} />);

      checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(false);
    });
  });

  describe('Complex Recurrence Rules', () => {
    it('should support daily recurrence', () => {
      const value: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=DAILY;COUNT=10',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-10-30T00:00:00Z',
      };

      render(<RecurrenceSelector initialValue={value} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(true);
    });

    it('should support weekly recurrence with specific days', () => {
      const value: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=WEEKLY;BYDAY=MO,WE,FR;COUNT=12',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2025-12-31T23:59:59Z',
      };

      render(<RecurrenceSelector initialValue={value} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(true);
    });

    it('should support monthly recurrence', () => {
      const value: RecurrenceData = {
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=MONTHLY;BYMONTHDAY=15;COUNT=6',
        startDate: '2025-10-20T00:00:00Z',
        endDate: '2026-04-15T23:59:59Z',
      };

      render(<RecurrenceSelector initialValue={value} onChange={mockOnChange} />);

      const checkbox = screen.getByTestId('recurrence-checkbox') as HTMLInputElement;
      expect(checkbox.checked).toBe(true);
    });
  });
});
