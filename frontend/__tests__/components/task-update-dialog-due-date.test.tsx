import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TaskUpdateDialog } from '../../components/task-update-dialog';
import type { TaskResponse } from '../../services/project-service';

// Simple mocks
jest.mock('../../services/project-service');
jest.mock('../../services/user-management-service');
jest.mock('../../services/tag-service');

jest.mock('../../contexts/user-context', () => ({
  useCurrentUser: () => ({
    currentUser: {
      id: '1',
      backendStaffId: 1,
      email: 'test@example.com',
      fullName: 'Test User',
      role: 'STAFF',
    },
  }),
}));

jest.mock('../../components/task-collaborator-management', () => ({
  TaskCollaboratorManagement: () => <div data-testid="collaborator-management">Collaborators</div>,
}));

// Simple UI mocks
jest.mock('../../components/ui/input', () => ({
  Input: React.forwardRef<HTMLInputElement, any>((props, ref) => <input ref={ref} {...props} />),
}));

jest.mock('../../components/ui/textarea', () => ({
  Textarea: React.forwardRef<HTMLTextAreaElement, any>((props, ref) => <textarea ref={ref} {...props} />),
}));

jest.mock('../../components/ui/button', () => ({
  Button: React.forwardRef<HTMLButtonElement, any>(({ children, ...props }, ref) => (
    <button ref={ref} {...props}>{children}</button>
  )),
}));

jest.mock('../../components/ui/dialog', () => ({
  Dialog: ({ children, open }: any) => open ? <div role="dialog">{children}</div> : null,
  DialogContent: ({ children }: any) => <div>{children}</div>,
  DialogHeader: ({ children }: any) => <div>{children}</div>,
  DialogTitle: ({ children }: any) => <h2>{children}</h2>,
  DialogDescription: ({ children }: any) => <p>{children}</p>,
  DialogFooter: ({ children }: any) => <div>{children}</div>,
}));

jest.mock('../../components/ui/label', () => ({
  Label: ({ children, htmlFor }: any) => <label htmlFor={htmlFor}>{children}</label>,
}));

jest.mock('../../components/ui/select', () => ({
  Select: ({ children, onValueChange, value }: any) => (
    <select onChange={(e) => onValueChange?.(e.target.value)} value={value}>
      {children}
    </select>
  ),
  SelectContent: ({ children }: any) => <>{children}</>,
  SelectItem: ({ children, value }: any) => <option value={value}>{children}</option>,
  SelectTrigger: ({ children }: any) => <>{children}</>,
  SelectValue: ({ placeholder }: any) => <span>{placeholder}</span>,
}));

jest.mock('../../components/ui/badge', () => ({
  Badge: ({ children }: any) => <span>{children}</span>,
}));

jest.mock('../../components/ui/separator', () => ({
  Separator: () => <hr />,
}));

jest.mock('../../hooks/use-toast', () => ({
  useToast: () => ({
    toast: jest.fn(),
  }),
}));

// Test data
const mockTask: TaskResponse = {
  id: 1,
  title: 'Test Task',
  description: 'Test Description',
  status: 'TODO',
  taskType: 'FEATURE',
  dueDateTime: '2024-12-31T10:30:00.000Z',
  createdAt: '2024-01-01T00:00:00.000Z',
  updatedAt: '2024-01-01T00:00:00.000Z',
  projectId: 1,
  ownerId: 1,
  createdBy: 1,
  userHasEditAccess: true,
  userHasDeleteAccess: true,
  tags: [],
  assignedUserIds: [],
  subtasks: [],
};

describe('TaskUpdateDialog - Due Date Features', () => {
  const defaultProps = {
    open: true,
    onOpenChange: jest.fn(),
    task: mockTask,
    onTaskUpdated: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  test('renders with existing due date converted to local time', () => {
    render(<TaskUpdateDialog {...defaultProps} />);
    
    // The component converts UTC to local time (Singapore timezone +8)
    // 2024-12-31T10:30:00.000Z becomes 2024-12-31T18:30 in Singapore time
    const dueDateInput = screen.getByDisplayValue('2024-12-31T18:30');
    expect(dueDateInput).toBeInTheDocument();
    expect(dueDateInput).toHaveAttribute('type', 'datetime-local');
  });

  test('shows clear due date button when due date exists', () => {
    render(<TaskUpdateDialog {...defaultProps} />);
    
    const clearButton = screen.getByLabelText('Clear due date');
    expect(clearButton).toBeInTheDocument();
  });

  test('allows changing the due date', async () => {
    const user = userEvent.setup();
    render(<TaskUpdateDialog {...defaultProps} />);
    
    const dueDateInput = screen.getByDisplayValue('2024-12-31T18:30');
    await user.clear(dueDateInput);
    await user.type(dueDateInput, '2024-12-25T14:00');
    
    expect(dueDateInput).toHaveValue('2024-12-25T14:00');
  });

  test('allows clearing the due date', async () => {
    const user = userEvent.setup();
    render(<TaskUpdateDialog {...defaultProps} />);
    
    const clearButton = screen.getByLabelText('Clear due date');
    await user.click(clearButton);
    
    // After clearing, input should be empty - use the specific input
    const dueDateInput = screen.getByPlaceholderText('Select due date and time (optional)');
    expect(dueDateInput).toHaveValue('');
  });

  test('handles task with no due date', () => {
    const taskWithoutDueDate = { ...mockTask, dueDateTime: undefined };
    render(<TaskUpdateDialog {...defaultProps} task={taskWithoutDueDate} />);
    
    // Use a more specific selector since there are multiple empty inputs
    const dueDateInput = screen.getByPlaceholderText('Select due date and time (optional)');
    expect(dueDateInput).toBeInTheDocument();
    expect(dueDateInput).toHaveValue('');
  });

  test('due date input has proper accessibility attributes', () => {
    render(<TaskUpdateDialog {...defaultProps} />);
    
    const dueDateInput = screen.getByDisplayValue('2024-12-31T18:30');
    expect(dueDateInput).toHaveAttribute('type', 'datetime-local');
    expect(dueDateInput).toHaveAttribute('id', 'dueDate');
    
    // Check if there's a label for the input
    const label = screen.getByText('Due Date & Time');
    expect(label).toBeInTheDocument();
  });

  test('shows due date preview when date is set', () => {
    render(<TaskUpdateDialog {...defaultProps} />);
    
    // The component shows formatted date preview - use getAllByText to get all matching elements
    const previewTexts = screen.getAllByText((content, element) => {
      return element?.textContent === 'Due: 31 Dec 2024, 6:30 pm';
    });
    expect(previewTexts.length).toBeGreaterThan(0);
    expect(previewTexts[0]).toBeInTheDocument();
  });
});
