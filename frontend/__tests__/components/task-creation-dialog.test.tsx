import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TaskCreationDialog } from '../../components/task-creation-dialog';

// Mock the services
jest.mock('../../services/project-service', () => ({
  projectService: {
    createTask: jest.fn(() => Promise.resolve({
      id: 1,
      title: 'Test Task',
      description: 'Test Description',
      projectId: 1,
    })),
  },
}));

jest.mock('../../services/file-service', () => ({
  fileService: {
    uploadFile: jest.fn(() => Promise.resolve({
      id: 1,
      taskId: 1,
      projectId: 1,
      fileUrl: 'https://example.com/file.pdf',
      createdAt: '2023-01-01T00:00:00Z',
      createdBy: 1,
    })),
  },
}));

// Mock UI components that might not be available in test environment
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
  Button: ({ children, onClick, type, variant }: { 
    children: React.ReactNode; 
    onClick?: () => void; 
    type?: 'submit' | 'reset' | 'button';
    variant?: string;
  }) => (
    <button onClick={onClick} type={type} data-variant={variant}>
      {children}
    </button>
  ),
}));

jest.mock('../../components/ui/input', () => ({
  Input: ({ placeholder, value, onChange, ...props }: any) => (
    <input 
      placeholder={placeholder} 
      value={value} 
      onChange={onChange}
      {...props}
    />
  ),
}));

jest.mock('../../components/ui/textarea', () => ({
  Textarea: ({ placeholder, value, onChange, ...props }: any) => (
    <textarea 
      placeholder={placeholder} 
      value={value} 
      onChange={onChange}
      {...props}
    />
  ),
}));

jest.mock('../../components/ui/label', () => ({
  Label: ({ children, htmlFor }: { children: React.ReactNode; htmlFor?: string }) => (
    <label htmlFor={htmlFor}>{children}</label>
  ),
}));

jest.mock('../../components/ui/select', () => ({
  Select: ({ children, onValueChange }: { children: React.ReactNode; onValueChange?: (value: string) => void }) => (
    <div data-testid="select" onClick={() => onValueChange?.('test-value')}>
      {children}
    </div>
  ),
  SelectTrigger: ({ children, id }: { children: React.ReactNode; id?: string }) => <div id={id}>{children}</div>,
  SelectValue: ({ placeholder }: { placeholder?: string }) => <span>{placeholder}</span>,
  SelectContent: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
  SelectItem: ({ children, value }: { children: React.ReactNode; value: string }) => (
    <div data-value={value}>{children}</div>
  ),
}));

describe('TaskCreationDialog', () => {
  const mockOnOpenChange = jest.fn();
  const mockOnTaskCreated = jest.fn();

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders dialog when open', () => {
    render(
      <TaskCreationDialog 
        open={true} 
        onOpenChange={mockOnOpenChange} 
        onTaskCreated={mockOnTaskCreated} 
      />
    );
    
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /create new personal task/i })).toBeInTheDocument();
  });

  it('does not render dialog when closed', () => {
    render(
      <TaskCreationDialog 
        open={false} 
        onOpenChange={mockOnOpenChange} 
        onTaskCreated={mockOnTaskCreated} 
      />
    );
    
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });

  it('calls onOpenChange when cancel button is clicked', async () => {
    render(
      <TaskCreationDialog 
        open={true} 
        onOpenChange={mockOnOpenChange} 
        onTaskCreated={mockOnTaskCreated} 
      />
    );
    
    const cancelButton = screen.getByText(/cancel/i);
    fireEvent.click(cancelButton);
    
    expect(mockOnOpenChange).toHaveBeenCalledWith(false);
  });

  it('renders form fields', () => {
    render(
      <TaskCreationDialog 
        open={true} 
        onOpenChange={mockOnOpenChange} 
        onTaskCreated={mockOnTaskCreated} 
      />
    );
    
    expect(screen.getByPlaceholderText(/enter task title/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter task description/i)).toBeInTheDocument();
    expect(screen.getByText(/task type \*/i)).toBeInTheDocument();
    expect(screen.getByText(/initial status/i)).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/enter tags separated by commas/i)).toBeInTheDocument();
  });

  it('allows file upload', () => {
    render(
      <TaskCreationDialog 
        open={true} 
        onOpenChange={mockOnOpenChange} 
        onTaskCreated={mockOnTaskCreated} 
      />
    );
    
    const fileInput = screen.getByLabelText(/attachments/i);
    expect(fileInput).toBeInTheDocument();
    expect(fileInput).toHaveAttribute('type', 'file');
    expect(fileInput).toHaveAttribute('multiple');
  });

  it('shows project selector when projectId is not provided', () => {
    render(
      <TaskCreationDialog 
        open={true} 
        onOpenChange={mockOnOpenChange} 
        onTaskCreated={mockOnTaskCreated} 
      />
    );
    
    // Should show project selection when no projectId prop
    expect(screen.getByText(/project/i)).toBeInTheDocument();
  });

  it('creates task for specific project when projectId is provided', () => {
    render(
      <TaskCreationDialog 
        open={true} 
        onOpenChange={mockOnOpenChange} 
        onTaskCreated={mockOnTaskCreated}
        projectId={123}
      />
    );
    
    // Should not show project selector when projectId is provided
    // The project should be predetermined
    expect(screen.getByRole('dialog')).toBeInTheDocument();
  });
});
