import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { SubtaskList } from '../../components/subtask-list';
import { SubtaskResponse } from '../../services/project-service';

// Mock the services
const mockDeleteSubtask = jest.fn();
const mockUpdateSubtask = jest.fn();
const mockCreateSubtask = jest.fn();

jest.mock('../../services/project-service', () => ({
  projectService: {
    deleteSubtask: (...args: any[]) => mockDeleteSubtask(...args),
    updateSubtask: (...args: any[]) => mockUpdateSubtask(...args),
    createSubtask: (...args: any[]) => mockCreateSubtask(...args),
  },
  SubtaskResponse: {},
  CreateSubtaskRequest: {},
}));

// Mock the toast hook
const mockToast = jest.fn();
jest.mock('../../hooks/use-toast', () => ({
  useToast: () => ({ toast: mockToast }),
}));

// Mock CommentSection component
jest.mock('../../components/comment-section', () => ({
  CommentSection: () => <div data-testid="comment-section">Comment Section</div>,
}));

// Mock SubtaskUpdateDialog component
jest.mock('../../components/subtask-update-dialog', () => ({
  SubtaskUpdateDialog: ({ onSubtaskUpdated }: any) => (
    <div data-testid="subtask-update-dialog">
      <button onClick={() => onSubtaskUpdated({ id: 1, title: 'Updated' })}>
        Update
      </button>
    </div>
  ),
}));

// Mock UI components
jest.mock('../../components/ui/alert-dialog', () => ({
  AlertDialog: ({ children, open }: { children: React.ReactNode; open: boolean }) => 
    open ? <div role="alertdialog">{children}</div> : null,
  AlertDialogContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  AlertDialogHeader: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  AlertDialogTitle: ({ children }: { children: React.ReactNode }) => <h2>{children}</h2>,
  AlertDialogDescription: ({ children }: { children: React.ReactNode }) => <p>{children}</p>,
  AlertDialogFooter: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  AlertDialogAction: ({ children, onClick }: { children: React.ReactNode; onClick: () => void }) => (
    <button onClick={onClick} data-testid="alert-dialog-action">{children}</button>
  ),
  AlertDialogCancel: ({ children }: { children: React.ReactNode }) => (
    <button data-testid="alert-dialog-cancel">{children}</button>
  ),
}));

jest.mock('../../components/ui/dialog', () => ({
  Dialog: ({ children, open }: { children: React.ReactNode; open: boolean }) => 
    open ? <div role="dialog">{children}</div> : null,
  DialogContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DialogHeader: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  DialogTitle: ({ children }: { children: React.ReactNode }) => <h2>{children}</h2>,
  DialogDescription: ({ children }: { children: React.ReactNode }) => <p>{children}</p>,
  DialogTrigger: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

jest.mock('../../components/ui/button', () => ({
  Button: ({ children, onClick, type, variant, disabled, title }: any) => (
    <button 
      onClick={onClick} 
      type={type} 
      data-variant={variant} 
      disabled={disabled}
      title={title}
    >
      {children}
    </button>
  ),
}));

jest.mock('../../components/ui/badge', () => ({
  Badge: ({ children }: { children: React.ReactNode }) => <span>{children}</span>,
}));

jest.mock('../../components/ui/input', () => ({
  Input: (props: any) => <input {...props} />,
}));

jest.mock('../../components/ui/label', () => ({
  Label: ({ children, htmlFor }: { children: React.ReactNode; htmlFor?: string }) => (
    <label htmlFor={htmlFor}>{children}</label>
  ),
}));

jest.mock('../../components/ui/textarea', () => ({
  Textarea: (props: any) => <textarea {...props} />,
}));

jest.mock('../../components/ui/select', () => ({
  Select: ({ children, onValueChange, value }: any) => (
    <div data-testid="select">
      <select onChange={(e) => onValueChange(e.target.value)} value={value}>
        {children}
      </select>
    </div>
  ),
  SelectContent: ({ children }: { children: React.ReactNode }) => <>{children}</>,
  SelectItem: ({ children, value }: { children: React.ReactNode; value: string }) => (
    <option value={value}>{children}</option>
  ),
  SelectTrigger: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SelectValue: () => <span>Select</span>,
}));

// Mock lucide-react icons
jest.mock('lucide-react', () => ({
  Plus: () => <span data-testid="plus-icon">+</span>,
  CheckCircle2: () => <span data-testid="check-circle-icon">✓</span>,
  Circle: () => <span data-testid="circle-icon">○</span>,
  Clock: () => <span data-testid="clock-icon">⏰</span>,
  AlertCircle: () => <span data-testid="alert-circle-icon">!</span>,
  Eye: () => <span data-testid="eye-icon">👁</span>,
  Edit: () => <span data-testid="edit-icon">✎</span>,
  Trash2: () => <span data-testid="trash-icon">🗑</span>,
}));

describe('SubtaskList', () => {
  const mockSubtasks: SubtaskResponse[] = [
    {
      id: 1,
      taskId: 100,
      projectId: 200,
      title: 'Subtask 1',
      details: 'Details for subtask 1',
      status: 'TODO' as any,
      taskType: 'FEATURE' as any,
      userHasEditAccess: true,
      userHasDeleteAccess: true,
      createdAt: '2023-01-01T00:00:00Z',
      createdBy: 1,
    },
    {
      id: 2,
      taskId: 100,
      projectId: 200,
      title: 'Subtask 2',
      details: 'Details for subtask 2',
      status: 'IN_PROGRESS' as any,
      taskType: 'BUG' as any,
      userHasEditAccess: true,
      userHasDeleteAccess: false,
      createdAt: '2023-01-02T00:00:00Z',
      createdBy: 1,
    },
    {
      id: 3,
      taskId: 100,
      projectId: 200,
      title: 'Subtask 3',
      details: null,
      status: 'COMPLETED' as any,
      taskType: 'CHORE' as any,
      userHasEditAccess: false,
      userHasDeleteAccess: false,
      createdAt: '2023-01-03T00:00:00Z',
      createdBy: 1,
    },
  ];

  const defaultProps = {
    taskId: 100,
    projectId: 200,
    subtasks: mockSubtasks,
    onSubtaskCreated: jest.fn(),
    onSubtaskUpdated: jest.fn(),
    onSubtaskDeleted: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
    mockDeleteSubtask.mockResolvedValue(undefined);
    mockUpdateSubtask.mockResolvedValue({});
    mockCreateSubtask.mockResolvedValue({});
  });

  describe('Rendering', () => {
    it('renders subtask list with subtasks', () => {
      render(<SubtaskList {...defaultProps} />);
      
      expect(screen.getByText('Subtask 1')).toBeInTheDocument();
      expect(screen.getByText('Subtask 2')).toBeInTheDocument();
      expect(screen.getByText('Subtask 3')).toBeInTheDocument();
    });

    it('displays subtask count badge', () => {
      render(<SubtaskList {...defaultProps} />);
      
      // Should show completed/total count (1 completed out of 3)
      expect(screen.getByText('1/3')).toBeInTheDocument();
    });

    it('shows progress bar with correct percentage', () => {
      render(<SubtaskList {...defaultProps} />);
      
      // 1 out of 3 completed = 33%
      expect(screen.getByText('33% complete')).toBeInTheDocument();
      expect(screen.getByText('1 of 3 done')).toBeInTheDocument();
    });

    it('renders empty state when no subtasks', () => {
      render(<SubtaskList {...defaultProps} subtasks={[]} />);
      
      expect(screen.getByText('No subtasks yet')).toBeInTheDocument();
    });
  });

  describe('Delete Functionality', () => {
    it('opens delete confirmation dialog when delete button is clicked', async () => {
      const user = userEvent.setup();
      render(<SubtaskList {...defaultProps} />);

      // Find and click the first delete button
      const deleteButtons = screen.getAllByTitle('Delete subtask');
      await user.click(deleteButtons[0]);

      // Check if the alert dialog is shown
      await waitFor(() => {
        expect(screen.getByRole('alertdialog')).toBeInTheDocument();
        expect(screen.getByText('Delete Subtask')).toBeInTheDocument();
        expect(screen.getByText(/Are you sure you want to delete this subtask/)).toBeInTheDocument();
      });
    });

    it('successfully deletes subtask when confirmed', async () => {
      const user = userEvent.setup();
      render(<SubtaskList {...defaultProps} />);

      // Click delete button for first subtask
      const deleteButtons = screen.getAllByTitle('Delete subtask');
      await user.click(deleteButtons[0]);

      // Wait for dialog to appear
      await waitFor(() => {
        expect(screen.getByRole('alertdialog')).toBeInTheDocument();
      });

      // Click confirm button
      const confirmButton = screen.getByTestId('alert-dialog-action');
      await user.click(confirmButton);

      // Verify delete was called
      await waitFor(() => {
        expect(mockDeleteSubtask).toHaveBeenCalledWith(1);
        expect(defaultProps.onSubtaskDeleted).toHaveBeenCalledWith(1);
      });

      // Verify success toast was shown
      expect(mockToast).toHaveBeenCalledWith({
        title: 'Subtask deleted',
        description: 'The subtask has been successfully deleted.',
      });
    });

    it('does not delete subtask when cancelled', async () => {
      const user = userEvent.setup();
      render(<SubtaskList {...defaultProps} />);

      // Click delete button
      const deleteButtons = screen.getAllByTitle('Delete subtask');
      await user.click(deleteButtons[0]);

      // Wait for dialog
      await waitFor(() => {
        expect(screen.getByRole('alertdialog')).toBeInTheDocument();
      });

      // Click cancel button
      const cancelButton = screen.getByTestId('alert-dialog-cancel');
      await user.click(cancelButton);

      // Verify delete was not called
      expect(mockDeleteSubtask).not.toHaveBeenCalled();
      expect(defaultProps.onSubtaskDeleted).not.toHaveBeenCalled();
    });

    it('shows error toast when delete fails', async () => {
      const user = userEvent.setup();
      const errorMessage = 'Only project owner can delete the subtask';
      mockDeleteSubtask.mockRejectedValueOnce(new Error(errorMessage));

      render(<SubtaskList {...defaultProps} />);

      // Click delete button
      const deleteButtons = screen.getAllByTitle('Delete subtask');
      await user.click(deleteButtons[0]);

      // Wait for dialog and confirm
      await waitFor(() => {
        expect(screen.getByRole('alertdialog')).toBeInTheDocument();
      });

      const confirmButton = screen.getByTestId('alert-dialog-action');
      await user.click(confirmButton);

      // Verify error toast was shown
      await waitFor(() => {
        expect(mockToast).toHaveBeenCalledWith({
          title: 'Failed to delete subtask',
          description: errorMessage,
          variant: 'destructive',
        });
      });

      // Verify callback was not called on error
      expect(defaultProps.onSubtaskDeleted).not.toHaveBeenCalled();
    });

    it('handles delete from detail dialog', async () => {
      const user = userEvent.setup();
      render(<SubtaskList {...defaultProps} />);

      // Click view details button (eye icon)
      const viewButtons = screen.getAllByTitle('View details and comments');
      await user.click(viewButtons[0]);

      // Wait for detail dialog
      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });

      // Find and click delete button in the dialog
      const deleteButtonInDialog = screen.getByText('Delete').closest('button');
      await user.click(deleteButtonInDialog!);

      // The detail dialog should close and alert dialog should open
      await waitFor(() => {
        expect(screen.getByRole('alertdialog')).toBeInTheDocument();
      });

      // Confirm deletion
      const confirmButton = screen.getByTestId('alert-dialog-action');
      await user.click(confirmButton);

      // Verify delete was called
      await waitFor(() => {
        expect(mockDeleteSubtask).toHaveBeenCalledWith(1);
        expect(defaultProps.onSubtaskDeleted).toHaveBeenCalledWith(1);
      });
    });

  });

  describe('Update Functionality', () => {
    it('opens edit dialog when edit button is clicked', async () => {
      const user = userEvent.setup();
      render(<SubtaskList {...defaultProps} />);

      const editButtons = screen.getAllByTitle('Edit subtask');
      await user.click(editButtons[0]);

      await waitFor(() => {
        expect(screen.getByTestId('subtask-update-dialog')).toBeInTheDocument();
      });
    });
  });

  describe('Status Change', () => {
    it('updates subtask status when status icon is clicked', async () => {
      const user = userEvent.setup();
      mockUpdateSubtask.mockResolvedValueOnce({
        ...mockSubtasks[0],
        status: 'COMPLETED',
      });

      render(<SubtaskList {...defaultProps} />);

      // Click the status icon for the first subtask (TODO -> COMPLETED)
      const statusIcons = screen.getAllByTestId('circle-icon');
      await user.click(statusIcons[0].closest('button')!);

      await waitFor(() => {
        expect(mockUpdateSubtask).toHaveBeenCalledWith(1, { status: 'COMPLETED' });
        expect(defaultProps.onSubtaskUpdated).toHaveBeenCalled();
      });
    });
  });

  describe('Permission-Based Button Visibility', () => {
    it('shows edit and delete buttons only when user has permissions', () => {
      render(<SubtaskList {...defaultProps} />);

      // Subtask 1: has both edit and delete access
      const subtask1Row = screen.getByText('Subtask 1').closest('div')?.parentElement;
      expect(subtask1Row).toBeInTheDocument();
      
      // Check for edit button (subtask 1 should have it)
      const editButtons = screen.getAllByTitle('Edit subtask');
      expect(editButtons.length).toBeGreaterThan(0);

      // Check for delete button (subtask 1 should have it)
      const deleteButtons = screen.getAllByTitle('Delete subtask');
      expect(deleteButtons.length).toBe(1); // Only subtask 1 has delete access
    });

    it('hides edit button when user lacks edit access', () => {
      render(<SubtaskList {...defaultProps} />);

      // Subtask 3 has no edit access
      // Count total edit buttons - should be 2 (subtask 1 and 2)
      const editButtons = screen.queryAllByTitle('Edit subtask');
      expect(editButtons).toHaveLength(2);
    });

    it('hides delete button when user lacks delete access', () => {
      render(<SubtaskList {...defaultProps} />);

      // Count total delete buttons - should be 1 (only subtask 1)
      const deleteButtons = screen.queryAllByTitle('Delete subtask');
      expect(deleteButtons).toHaveLength(1);
    });

    it('shows/hides buttons in detail dialog based on permissions', async () => {
      const user = userEvent.setup();
      render(<SubtaskList {...defaultProps} />);

      // Open detail dialog for subtask 1 (has both permissions)
      const viewButtons = screen.getAllByTitle('View details and comments');
      await user.click(viewButtons[0]);

      await waitFor(() => {
        expect(screen.getByRole('dialog')).toBeInTheDocument();
      });

      // Should show both edit and delete buttons
      expect(screen.getByText('Edit')).toBeInTheDocument();
      expect(screen.getByText('Delete')).toBeInTheDocument();
    });
  });

  describe('Integration with Parent Component', () => {
    it('calls onSubtaskDeleted callback when provided', async () => {
      const user = userEvent.setup();
      const onSubtaskDeleted = jest.fn();
      
      render(<SubtaskList {...defaultProps} onSubtaskDeleted={onSubtaskDeleted} />);

      // Delete a subtask
      const deleteButtons = screen.getAllByTitle('Delete subtask');
      await user.click(deleteButtons[0]);

      await waitFor(() => {
        expect(screen.getByRole('alertdialog')).toBeInTheDocument();
      });

      const confirmButton = screen.getByTestId('alert-dialog-action');
      await user.click(confirmButton);

      await waitFor(() => {
        expect(onSubtaskDeleted).toHaveBeenCalledWith(1);
      });
    });

    it('does not fail when onSubtaskDeleted callback is not provided', async () => {
      const user = userEvent.setup();
      const { onSubtaskDeleted, ...propsWithoutCallback } = defaultProps;
      
      render(<SubtaskList {...propsWithoutCallback} />);

      // Delete a subtask
      const deleteButtons = screen.getAllByTitle('Delete subtask');
      await user.click(deleteButtons[0]);

      await waitFor(() => {
        expect(screen.getByRole('alertdialog')).toBeInTheDocument();
      });

      const confirmButton = screen.getByTestId('alert-dialog-action');
      await user.click(confirmButton);

      // Should not throw error
      await waitFor(() => {
        expect(mockDeleteSubtask).toHaveBeenCalledWith(1);
        expect(mockToast).toHaveBeenCalled();
      });
    });
  });
});

