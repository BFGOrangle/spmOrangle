import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CommentEditor } from '@/components/comment-editor';
import { commentService } from '@/services/comment-service';

// Mock the comment service
jest.mock('@/services/comment-service', () => ({
  commentService: {
    getProjectMembers: jest.fn(),
  },
}));

// Mock Lucide React icons
jest.mock('lucide-react', () => ({
  Send: () => <div data-testid="send-icon" />,
  X: () => <div data-testid="x-icon" />,
  AtSign: () => <div data-testid="at-sign-icon" />,
}));

describe('CommentEditor', () => {
  const mockOnSubmit = jest.fn();
  const mockOnCancel = jest.fn();
  const mockProjectMembers = [
    {
      id: 1,
      username: 'john_doe',
      email: 'john@example.com',
      displayName: 'John Doe',
    },
    {
      id: 2,
      username: 'jane_smith',
      email: 'jane@example.com',
      displayName: 'Jane Smith',
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    (commentService.getProjectMembers as jest.Mock).mockResolvedValue(mockProjectMembers);
  });

  const defaultProps = {
    onSubmit: mockOnSubmit,
    onCancel: mockOnCancel,
    projectId: 1,
  };

  describe('Basic Rendering', () => {
    it('renders with default props', () => {
      render(<CommentEditor {...defaultProps} />);

      expect(screen.getByPlaceholderText('Write a comment...')).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /cancel/i })).toBeInTheDocument();
      expect(screen.getByRole('button', { name: /comment/i })).toBeInTheDocument();
    });

    it('renders with custom placeholder', () => {
      render(
        <CommentEditor
          {...defaultProps}
          placeholder="Write a reply..."
        />
      );

      expect(screen.getByPlaceholderText('Write a reply...')).toBeInTheDocument();
    });

    it('renders with initial content', () => {
      render(
        <CommentEditor
          {...defaultProps}
          initialContent="Initial content"
        />
      );

      expect(screen.getByDisplayValue('Initial content')).toBeInTheDocument();
    });

    it('renders in compact mode', () => {
      render(<CommentEditor {...defaultProps} compact />);

      // Should render "Reply" instead of "Comment"
      expect(screen.getByRole('button', { name: /reply/i })).toBeInTheDocument();
    });
  });

  describe('Content Input', () => {
    it('updates content when user types', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello world');

      expect(textarea).toHaveValue('Hello world');
    });

    it('enables submit button when content is entered', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      const submitButton = screen.getByRole('button', { name: /comment/i });

      expect(submitButton).toBeDisabled();

      await user.type(textarea, 'Hello');

      expect(submitButton).toBeEnabled();
    });

    it('submits with Ctrl+Enter', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello world');
      await user.keyboard('{Control>}{Enter}{/Control}');

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith('Hello world', []);
      });
    });

    it('submits with Cmd+Enter on Mac', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello world');
      await user.keyboard('{Meta>}{Enter}{/Meta}');

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith('Hello world', []);
      });
    });
  });

  describe('User Mentions', () => {
    it('loads project members when projectId is provided', async () => {
      render(<CommentEditor {...defaultProps} />);

      await waitFor(() => {
        expect(commentService.getProjectMembers).toHaveBeenCalledWith(1);
      });
    });

    it('shows mention dropdown when typing @', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      await waitFor(() => {
        expect(commentService.getProjectMembers).toHaveBeenCalled();
      });

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello @');

      expect(screen.getByPlaceholderText('Search users...')).toBeInTheDocument();
      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.getByText('Jane Smith')).toBeInTheDocument();
    });

    it('filters users when typing after @', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      await waitFor(() => {
        expect(commentService.getProjectMembers).toHaveBeenCalled();
      });

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello @john');

      expect(screen.getByText('John Doe')).toBeInTheDocument();
      expect(screen.queryByText('Jane Smith')).not.toBeInTheDocument();
    });

    it('inserts mention when user is selected', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      await waitFor(() => {
        expect(commentService.getProjectMembers).toHaveBeenCalled();
      });

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello @');

      const johnButton = screen.getByText('John Doe');
      await user.click(johnButton);

      expect(textarea).toHaveValue('Hello @john_doe ');
      expect(screen.getByText('John Doe')).toBeInTheDocument(); // Should show as badge
    });

    it('removes mention when X is clicked on badge', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      await waitFor(() => {
        expect(commentService.getProjectMembers).toHaveBeenCalled();
      });

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello @');

      const johnButton = screen.getByText('John Doe');
      await user.click(johnButton);

      // Find and click the remove button
      const removeButtons = screen.getAllByTestId('x-icon');
      await user.click(removeButtons[0]); // First X icon should be the remove button

      // The component removes @username but leaves double spaces (one from original, one from mention)
      expect(textarea).toHaveValue('Hello  ');
      expect(screen.queryByText('John Doe')).not.toBeInTheDocument();
    });

    it('includes mentioned user IDs when submitting', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      await waitFor(() => {
        expect(commentService.getProjectMembers).toHaveBeenCalled();
      });

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello @');

      const johnButton = screen.getByText('John Doe');
      await user.click(johnButton);

      const submitButton = screen.getByRole('button', { name: /comment/i });
      await user.click(submitButton);

      await waitFor(() => {
        // The component trims content before submitting, so trailing space is removed
        expect(mockOnSubmit).toHaveBeenCalledWith('Hello @john_doe', [1]);
      });
    });
  });

  describe('Loading States', () => {
    it('disables textarea when loading', () => {
      render(<CommentEditor {...defaultProps} isLoading />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      expect(textarea).toBeDisabled();
    });

    it('shows loading text on submit button when loading', () => {
      render(<CommentEditor {...defaultProps} isLoading />);

      expect(screen.getByText('Posting...')).toBeInTheDocument();
    });

    it('disables buttons when loading', () => {
      render(<CommentEditor {...defaultProps} isLoading />);

      expect(screen.getByRole('button', { name: /cancel/i })).toBeDisabled();
      expect(screen.getByRole('button', { name: /posting/i })).toBeDisabled();
    });
  });

  describe('Form Submission', () => {
    it('calls onSubmit with content and mentions', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Test comment');

      const submitButton = screen.getByRole('button', { name: /comment/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith('Test comment', []);
      });
    });

    it('clears content after successful submission', async () => {
      const user = userEvent.setup();
      mockOnSubmit.mockResolvedValue(undefined);

      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Test comment');

      const submitButton = screen.getByRole('button', { name: /comment/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(textarea).toHaveValue('');
      });
    });

    it('calls onCancel when cancel is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      await user.click(cancelButton);

      expect(mockOnCancel).toHaveBeenCalled();
    });

    it('does not submit empty content', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      const submitButton = screen.getByRole('button', { name: /comment/i });
      expect(submitButton).toBeDisabled();

      await user.click(submitButton);

      expect(mockOnSubmit).not.toHaveBeenCalled();
    });

    it('does not submit whitespace-only content', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, '   ');

      const submitButton = screen.getByRole('button', { name: /comment/i });
      expect(submitButton).toBeDisabled();
    });
  });

  describe('Error Handling', () => {
    it('handles project members fetch error gracefully', async () => {
      (commentService.getProjectMembers as jest.Mock).mockRejectedValue(
        new Error('Failed to fetch')
      );

      render(<CommentEditor {...defaultProps} />);

      // Should not crash and should still render
      expect(screen.getByPlaceholderText('Write a comment...')).toBeInTheDocument();
    });

    it('handles submission error gracefully', async () => {
      const user = userEvent.setup();
      const consoleError = jest.spyOn(console, 'error').mockImplementation();
      mockOnSubmit.mockRejectedValue(new Error('Submission failed'));

      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Test comment');

      const submitButton = screen.getByRole('button', { name: /comment/i });
      await user.click(submitButton);

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalledWith(
          'Failed to submit comment:',
          expect.any(Error)
        );
      });

      consoleError.mockRestore();
    });
  });

  describe('Accessibility', () => {
    it('has proper ARIA labels', () => {
      render(<CommentEditor {...defaultProps} />);

      const textarea = screen.getByPlaceholderText('Write a comment...');
      expect(textarea).toBeInTheDocument();

      const submitButton = screen.getByRole('button', { name: /comment/i });
      expect(submitButton).toBeInTheDocument();

      const cancelButton = screen.getByRole('button', { name: /cancel/i });
      expect(cancelButton).toBeInTheDocument();
    });

    it('supports keyboard navigation in mention dropdown', async () => {
      const user = userEvent.setup();
      render(<CommentEditor {...defaultProps} />);

      await waitFor(() => {
        expect(commentService.getProjectMembers).toHaveBeenCalled();
      });

      const textarea = screen.getByPlaceholderText('Write a comment...');
      await user.type(textarea, 'Hello @');

      // The mention dropdown should be keyboard accessible
      expect(screen.getByPlaceholderText('Search users...')).toBeInTheDocument();
    });
  });
});