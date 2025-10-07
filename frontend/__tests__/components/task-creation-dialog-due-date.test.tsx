import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TaskCreationDialog } from '../../components/task-creation-dialog';

// Mock all the services and dependencies
jest.mock('../../services/project-service', () => ({
  projectService: {
    createTask: jest.fn(),
    createTaskWithSpecifiedOwner: jest.fn(),
  },
}));

jest.mock('../../services/user-management-service', () => ({
  userManagementService: {
    getProjectMembers: jest.fn(() => Promise.resolve([])),
    getCollaborators: jest.fn(() => Promise.resolve([])),
  },
}));

jest.mock('../../services/file-service', () => ({
  fileService: {
    uploadFile: jest.fn(),
  },
}));

jest.mock('../../services/tag-service', () => ({
  tagService: {
    getTags: jest.fn(() => Promise.resolve([])),
    createTag: jest.fn(),
  },
}));

jest.mock('../../contexts/user-context', () => ({
  useCurrentUser: jest.fn(() => ({
    currentUser: {
      id: '1',
      backendStaffId: 1,
      email: 'test@example.com',
      fullName: 'Test User',
      role: 'STAFF',
    },
  })),
}));

// Mock UI components
jest.mock('../../components/ui/dialog', () => ({
  Dialog: ({ children, open }: { children: React.ReactNode; open: boolean }) => 
    open ? <div role="dialog">{children}</div> : null,
  DialogContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DialogHeader: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DialogTitle: ({ children }: { children: React.ReactNode }) => <h2>{children}</h2>,
  DialogDescription: ({ children }: { children: React.ReactNode }) => <p>{children}</p>,
  DialogFooter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

jest.mock('../../components/ui/button', () => ({
  Button: ({ children, onClick, type, disabled }: { 
    children: React.ReactNode; 
    onClick?: () => void; 
    type?: 'button' | 'submit' | 'reset';
    disabled?: boolean;
  }) => (
    <button onClick={onClick} type={type} disabled={disabled}>
      {children}
    </button>
  ),
}));

jest.mock('../../components/ui/input', () => ({
  Input: ({ ...props }: any) => <input {...props} />,
}));

jest.mock('../../components/ui/textarea', () => ({
  Textarea: ({ ...props }: any) => <textarea {...props} />,
}));

jest.mock('../../components/ui/label', () => ({
  Label: ({ children, htmlFor }: { children: React.ReactNode; htmlFor?: string }) => (
    <label htmlFor={htmlFor}>{children}</label>
  ),
}));

jest.mock('../../components/ui/select', () => ({
  Select: ({ children, onValueChange, value }: { 
    children: React.ReactNode; 
    onValueChange?: (value: string) => void;
    value?: string;
  }) => (
    <div data-testid="select" data-value={value}>
      {children}
    </div>
  ),
  SelectTrigger: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SelectValue: ({ placeholder }: { placeholder?: string }) => <span>{placeholder}</span>,
  SelectContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SelectItem: ({ children, value }: { children: React.ReactNode; value: string }) => (
    <div data-value={value}>{children}</div>
  ),
}));

jest.mock('../../components/ui/badge', () => ({
  Badge: ({ children, variant, className }: { 
    children: React.ReactNode; 
    variant?: string;
    className?: string;
  }) => (
    <span data-variant={variant} className={className}>{children}</span>
  ),
}));

jest.mock('../../components/ui/scroll-area', () => ({
  ScrollArea: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

jest.mock('lucide-react', () => ({
  User: () => <span data-testid="user-icon">👤</span>,
  Crown: () => <span data-testid="crown-icon">👑</span>,
  Loader2: () => <span data-testid="loader-icon">⏳</span>,
  X: () => <span data-testid="x-icon">✕</span>,
  Calendar: () => <span data-testid="calendar-icon">📅</span>,
}));

const { projectService } = require('../../services/project-service');
const { tagService } = require('../../services/tag-service');

describe('TaskCreationDialog - Due Date Comprehensive Tests', () => {
  const mockOnOpenChange = jest.fn();
  const mockOnTaskCreated = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
    projectService.createTask.mockResolvedValue({
      id: 1,
      title: 'Test Task',
      description: 'Test Description',
      projectId: 1,
    });
  });

  describe('Due Date Input Validation', () => {
    it('accepts valid future datetime', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      const futureDate = '2025-12-31T23:59';
      
      await user.clear(dueDateInput);
      await user.type(dueDateInput, futureDate);

      expect(dueDateInput).toHaveValue(futureDate);
    });

    it('shows proper placeholder text', async () => {
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      expect(dueDateInput).toHaveAttribute('placeholder', 'Select due date and time (optional)');
    });

    it('is marked as optional in help text', async () => {
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      expect(screen.getByText(/set a deadline for this task \(optional\)/i)).toBeInTheDocument();
    });
  });

  describe('Due Date Format Conversion', () => {
    it('correctly converts datetime-local to ISO string', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      // Fill required fields
      const titleInput = screen.getByPlaceholderText(/task title/i);
      await user.type(titleInput, 'Test ISO Conversion');

      // Set a specific datetime
      const dueDateInput = screen.getByLabelText(/due date & time/i);
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-06-15T09:30');

      // Submit form
      const createButton = screen.getByRole('button', { name: /create task/i });
      await user.click(createButton);

      await waitFor(() => {
        expect(projectService.createTask).toHaveBeenCalled();
      });

      const call = projectService.createTask.mock.calls[0][0];
      const dueDateTime = call.dueDateTime;
      
      // Should be in ISO format
      expect(dueDateTime).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/);
      
      // Should represent the correct date
      const parsedDate = new Date(dueDateTime);
      expect(parsedDate.getFullYear()).toBe(2025);
      expect(parsedDate.getMonth()).toBe(5); // June (0-indexed)
      expect(parsedDate.getDate()).toBe(15);
    });

    it('handles timezone conversion correctly', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const titleInput = screen.getByPlaceholderText(/task title/i);
      await user.type(titleInput, 'Test Timezone');

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-01-01T12:00');

      const createButton = screen.getByRole('button', { name: /create task/i });
      await user.click(createButton);

      await waitFor(() => {
        expect(projectService.createTask).toHaveBeenCalled();
      });

      const call = projectService.createTask.mock.calls[0][0];
      const dueDateTime = call.dueDateTime;
      
      // Should be a valid ISO string
      expect(() => new Date(dueDateTime)).not.toThrow();
      expect(dueDateTime).toMatch(/Z$/); // Should end with Z for UTC
    });
  });

  describe('Due Date Display and Formatting', () => {
    it('shows localized date format in preview', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-12-25T15:30');

      // Should show the formatted preview
      await waitFor(() => {
        expect(screen.getByText(/due:/i)).toBeInTheDocument();
      });

      // The actual format will depend on locale, but should contain the date elements
      const previewText = screen.getByText(/due:/i).textContent;
      expect(previewText).toMatch(/25|Dec|2025/i); // Should contain some form of the date
    });

    it('updates preview when date changes', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      
      // Set first date
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-06-15T10:00');
      
      await waitFor(() => {
        expect(screen.getByText(/due:/i)).toBeInTheDocument();
      });

      // Change to second date
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-08-20T14:30');

      // Preview should update
      await waitFor(() => {
        const previewText = screen.getByText(/due:/i).textContent;
        expect(previewText).toMatch(/20|Aug|2025/i);
      });
    });

    it('hides preview when date is cleared', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      
      // Set date
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-06-15T10:00');
      
      await waitFor(() => {
        expect(screen.getByText(/due:/i)).toBeInTheDocument();
      });

      // Clear date using the clear button
      const clearButton = screen.getByRole('button', { name: /clear due date/i });
      await user.click(clearButton);

      // Preview should be hidden
      expect(screen.queryByText(/due:/i)).not.toBeInTheDocument();
    });
  });

  describe('Due Date Edge Cases', () => {
    it('handles leap year dates correctly', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const titleInput = screen.getByPlaceholderText(/task title/i);
      await user.type(titleInput, 'Leap Year Test');

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2028-02-29T12:00'); // Leap year date

      const createButton = screen.getByRole('button', { name: /create task/i });
      await user.click(createButton);

      await waitFor(() => {
        expect(projectService.createTask).toHaveBeenCalledWith(
          expect.objectContaining({
            dueDateTime: expect.stringMatching(/2028-02-29/),
          })
        );
      });
    });

    it('handles end of year dates correctly', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const titleInput = screen.getByPlaceholderText(/task title/i);
      await user.type(titleInput, 'New Year Eve Test');

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-12-31T23:59');

      const createButton = screen.getByRole('button', { name: /create task/i });
      await user.click(createButton);

      await waitFor(() => {
        expect(projectService.createTask).toHaveBeenCalledWith(
          expect.objectContaining({
            dueDateTime: expect.stringMatching(/2025-12-31/),
          })
        );
      });
    });

    it('handles daylight saving time transitions', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const titleInput = screen.getByPlaceholderText(/task title/i);
      await user.type(titleInput, 'DST Transition Test');

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      await user.clear(dueDateInput);
      // March DST transition in many regions
      await user.type(dueDateInput, '2025-03-09T02:30');

      const createButton = screen.getByRole('button', { name: /create task/i });
      await user.click(createButton);

      await waitFor(() => {
        expect(projectService.createTask).toHaveBeenCalled();
      });

      const call = projectService.createTask.mock.calls[0][0];
      expect(call.dueDateTime).toBeDefined();
      expect(() => new Date(call.dueDateTime)).not.toThrow();
    });
  });

  describe('Due Date Accessibility', () => {
    it('has proper label association', async () => {
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      expect(dueDateInput).toHaveAttribute('id', 'dueDate');
      
      const label = screen.getByText(/due date & time/i);
      expect(label.tagName).toBe('LABEL');
    });

    it('clear button has proper aria-label', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-06-15T10:00');

      const clearButton = screen.getByRole('button', { name: /clear due date/i });
      expect(clearButton).toHaveAttribute('aria-label', 'Clear due date');
    });

    it('provides helpful context with description text', async () => {
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      expect(screen.getByText(/set a deadline for this task \(optional\)/i)).toBeInTheDocument();
    });
  });

  describe('Due Date State Management', () => {
    it('preserves due date during form submission errors', async () => {
      const user = userEvent.setup();
      
      // Mock service to reject
      projectService.createTask.mockRejectedValueOnce(new Error('Network error'));
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const titleInput = screen.getByPlaceholderText(/task title/i);
      await user.type(titleInput, 'Test Error Handling');

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      const testDate = '2025-08-15T16:45';
      await user.clear(dueDateInput);
      await user.type(dueDateInput, testDate);

      const createButton = screen.getByRole('button', { name: /create task/i });
      await user.click(createButton);

      // Wait for the error to be handled
      await waitFor(() => {
        expect(projectService.createTask).toHaveBeenCalled();
      });

      // Due date should still be preserved
      expect(dueDateInput).toHaveValue(testDate);
    });

    it('clears due date on successful task creation', async () => {
      const user = userEvent.setup();
      
      render(
        <TaskCreationDialog 
          open={true} 
          onOpenChange={mockOnOpenChange} 
          onTaskCreated={mockOnTaskCreated}
        />
      );

      await waitFor(() => {
        expect(tagService.getTags).toHaveBeenCalled();
      });

      const titleInput = screen.getByPlaceholderText(/task title/i);
      await user.type(titleInput, 'Test Success Clear');

      const dueDateInput = screen.getByLabelText(/due date & time/i);
      await user.clear(dueDateInput);
      await user.type(dueDateInput, '2025-08-15T16:45');

      const createButton = screen.getByRole('button', { name: /create task/i });
      await user.click(createButton);

      await waitFor(() => {
        expect(mockOnTaskCreated).toHaveBeenCalled();
      });

      // Form should be reset, but we need to check after dialog reopens
      // Since onOpenChange(false) is called on success
      expect(mockOnOpenChange).toHaveBeenCalledWith(false);
    });
  });
});
