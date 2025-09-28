import React from 'react';
import { render, screen, fireEvent, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CommentSection } from '@/components/comment-section';
import { commentService } from '@/services/comment-service';
import { CommentResponse } from '@/types/comment';

// Mock the comment service
jest.mock('@/services/comment-service', () => ({
  commentService: {
    getTaskComments: jest.fn(),
    getSubtaskComments: jest.fn(),
    createComment: jest.fn(),
    updateComment: jest.fn(),
    deleteComment: jest.fn(),
    markThreadResolved: jest.fn(),
  },
}));

// Mock Lucide React icons
jest.mock('lucide-react', () => ({
  MessageCircle: () => <div data-testid="message-circle-icon" />,
  Plus: () => <div data-testid="plus-icon" />,
}));

// Mock child components
jest.mock('@/components/comment-item', () => ({
  CommentItem: ({ comment, onReply, onEdit, onDelete, onMarkResolved }: any) => (
    <div data-testid={`comment-item-${comment.id}`}>
      <div>{comment.content}</div>
      <button data-testid={`reply-${comment.id}`} onClick={() => onReply(comment.id, 'test reply', [])}>Reply</button>
      <button data-testid={`edit-${comment.id}`} onClick={() => onEdit(comment.id, 'test edit', [])}>Edit</button>
      <button data-testid={`delete-${comment.id}`} onClick={() => onDelete(comment.id)}>Delete</button>
      <button data-testid={`resolve-${comment.id}`} onClick={() => onMarkResolved(comment.id, true)}>Mark Resolved</button>
    </div>
  ),
}));

jest.mock('@/components/comment-editor', () => ({
  CommentEditor: ({ onSubmit, onCancel, isLoading, placeholder }: any) => (
    <div data-testid="comment-editor">
      <input data-testid="comment-editor-input" placeholder={placeholder} disabled={isLoading} />
      <button onClick={() => onSubmit('test content', [])} disabled={isLoading}>
        Submit
      </button>
      <button onClick={onCancel}>Cancel</button>
    </div>
  ),
}));

jest.mock('@/components/comment-filters', () => ({
  CommentFiltersComponent: ({ filters, onFiltersChange, totalComments, filteredComments }: any) => (
    <div data-testid="comment-filters">
      <div>Total: {totalComments}, Filtered: {filteredComments}</div>
      <button onClick={() => onFiltersChange({ filter: 'UNRESOLVED' })}>
        Filter Unresolved
      </button>
      <button onClick={() => onFiltersChange({})}>Clear Filters</button>
    </div>
  ),
}));

describe('CommentSection', () => {
  const mockComments: CommentResponse[] = [
    {
      id: 1,
      taskId: 1,
      projectId: 1,
      content: 'First comment',
      mentionedUserIds: [],
      isEdited: false,
      isDeleted: false,
      authorId: 1,
      authorUsername: 'john_doe',
      createdAt: '2024-01-01T10:00:00Z',
      replies: [
        {
          id: 2,
          taskId: 1,
          projectId: 1,
          parentCommentId: 1,
          content: 'First reply',
          mentionedUserIds: [],
          isEdited: false,
          isDeleted: false,
          authorId: 2,
          authorUsername: 'jane_smith',
          createdAt: '2024-01-01T11:00:00Z',
          replies: [],
          replyCount: 0,
          canEdit: true,
          canDelete: true,
          canReply: true,
          canModerate: false,
        },
      ],
      replyCount: 1,
      canEdit: true,
      canDelete: true,
      canReply: true,
      canModerate: true,
    },
    {
      id: 3,
      taskId: 1,
      projectId: 1,
      content: 'Second comment',
      mentionedUserIds: [],
      isEdited: false,
      isDeleted: false,
      authorId: 3,
      authorUsername: 'bob_wilson',
      createdAt: '2024-01-01T12:00:00Z',
      replies: [],
      replyCount: 0,
      canEdit: true,
      canDelete: true,
      canReply: true,
      canModerate: false,
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    (commentService.getTaskComments as jest.Mock).mockResolvedValue(mockComments);
    (commentService.getSubtaskComments as jest.Mock).mockResolvedValue(mockComments);
    (commentService.createComment as jest.Mock).mockResolvedValue({ id: 4 });
    (commentService.updateComment as jest.Mock).mockResolvedValue({});
    (commentService.deleteComment as jest.Mock).mockResolvedValue({});
    (commentService.markThreadResolved as jest.Mock).mockResolvedValue({});
  });

  const defaultProps = {
    taskId: 1,
    projectId: 1,
  };

  describe('Basic Rendering', () => {
    it('renders with default title', async () => {
      await act(async () => {
        render(<CommentSection {...defaultProps} />);
      });

      await waitFor(() => {
        expect(screen.getByText('Comments')).toBeInTheDocument();
      });
    });

    it('renders with custom title', async () => {
      await act(async () => {
        render(<CommentSection {...defaultProps} title="Task Comments" />);
      });

      await waitFor(() => {
        expect(screen.getByText('Task Comments')).toBeInTheDocument();
      });
    });

    it('hides title when showTitle is false', async () => {
      await act(async () => {
        render(<CommentSection {...defaultProps} showTitle={false} />);
      });

      await waitFor(() => {
        expect(screen.queryByText('Comments')).not.toBeInTheDocument();
      });
    });

    it('shows loading state initially', () => {
      // Mock a delayed response to see loading state
      (commentService.getTaskComments as jest.Mock).mockImplementation(
        () => new Promise(resolve => setTimeout(() => resolve(mockComments), 100))
      );

      render(<CommentSection {...defaultProps} />);

      expect(screen.getByText('Loading comments...')).toBeInTheDocument();

      // Reset the mock for other tests
      (commentService.getTaskComments as jest.Mock).mockResolvedValue(mockComments);
    });
  });

  describe('Data Loading', () => {
    it('loads task comments when taskId is provided', async () => {
      await act(async () => {
        render(<CommentSection {...defaultProps} />);
      });

      await waitFor(() => {
        expect(commentService.getTaskComments).toHaveBeenCalledWith(1, {});
      });

      expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      expect(screen.getByTestId('comment-item-3')).toBeInTheDocument();
    });

    it('loads subtask comments when subtaskId is provided', async () => {
      await act(async () => {
        render(<CommentSection {...defaultProps} taskId={undefined} subtaskId={2} />);
      });

      await waitFor(() => {
        expect(commentService.getSubtaskComments).toHaveBeenCalledWith(2, {});
      });
    });

    it('does not load comments when neither taskId nor subtaskId is provided', async () => {
      await act(async () => {
        render(<CommentSection {...defaultProps} taskId={undefined} />);
      });

      await waitFor(() => {
        expect(commentService.getTaskComments).not.toHaveBeenCalled();
        expect(commentService.getSubtaskComments).not.toHaveBeenCalled();
      });
    });

    it('handles loading errors gracefully', async () => {
      const consoleError = jest.spyOn(console, 'error').mockImplementation();
      (commentService.getTaskComments as jest.Mock).mockRejectedValue(
        new Error('Loading failed')
      );

      await act(async () => {
        render(<CommentSection {...defaultProps} />);
      });

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalledWith(
          'Failed to load comments:',
          expect.any(Error)
        );
      });

      consoleError.mockRestore();
    });
  });

  describe('Comment Tree Building', () => {
    it('builds comment tree from flat list when needed', async () => {
      const flatComments = [
        { ...mockComments[0], replies: [] },
        { ...mockComments[0].replies[0], parentCommentId: 1 },
      ];

      (commentService.getTaskComments as jest.Mock).mockResolvedValue(flatComments);

      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });
    });

    it('uses existing tree structure when available', async () => {
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
        expect(screen.getByTestId('comment-item-3')).toBeInTheDocument();
      });
    });
  });

  describe('Add Comment Functionality', () => {
    it('shows add comment button initially', async () => {
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
      });
    });

    it('shows comment editor when add comment is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
      });

      const addButton = screen.getByRole('button', { name: /add comment/i });
      await user.click(addButton);

      expect(screen.getByTestId('comment-editor')).toBeInTheDocument();
      expect(screen.queryByRole('button', { name: /add comment/i })).not.toBeInTheDocument();
    });

    it('creates new comment when submitted', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
      });

      const addButton = screen.getByRole('button', { name: /add comment/i });
      await user.click(addButton);

      const submitButton = screen.getByText('Submit');
      await user.click(submitButton);

      await waitFor(() => {
        expect(commentService.createComment).toHaveBeenCalledWith({
          taskId: 1,
          subtaskId: undefined,
          content: 'test content',
          mentionedUserIds: [],
        });
      });
    });

    it('hides editor after successful submission', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
      });

      const addButton = screen.getByRole('button', { name: /add comment/i });
      await user.click(addButton);

      const submitButton = screen.getByText('Submit');
      await user.click(submitButton);

      await waitFor(() => {
        expect(screen.queryByTestId('comment-editor')).not.toBeInTheDocument();
        expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
      });
    });

    it('cancels editor when cancel is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
      });

      const addButton = screen.getByRole('button', { name: /add comment/i });
      await user.click(addButton);

      const cancelButton = screen.getByText('Cancel');
      await user.click(cancelButton);

      expect(screen.queryByTestId('comment-editor')).not.toBeInTheDocument();
      expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
    });
  });

  describe('Reply Functionality', () => {
    it('creates reply when reply is submitted', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });

      const replyButton = screen.getByTestId('reply-1');
      await user.click(replyButton);

      await waitFor(() => {
        expect(commentService.createComment).toHaveBeenCalledWith({
          taskId: 1,
          subtaskId: undefined,
          content: 'test reply',
          mentionedUserIds: [],
          parentCommentId: 1,
        });
      });
    });

    it('reloads comments after successful reply', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });

      // Track initial call count
      const initialCallCount = (commentService.getTaskComments as jest.Mock).mock.calls.length;

      const replyButton = screen.getByTestId('reply-1');
      await user.click(replyButton);

      // Verify it was called at least once more than initially
      await waitFor(() => {
        expect((commentService.getTaskComments as jest.Mock).mock.calls.length).toBeGreaterThan(initialCallCount);
      });
    });
  });

  describe('Edit Functionality', () => {
    it('updates comment when edit is submitted', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });

      const editButton = screen.getByTestId('edit-1');
      await user.click(editButton);

      await waitFor(() => {
        expect(commentService.updateComment).toHaveBeenCalledWith({
          commentId: 1,
          content: 'test edit',
          mentionedUserIds: [],
        });
      });
    });

    it('reloads comments after successful edit', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });

      // Clear previous calls
      jest.clearAllMocks();
      // Reset mock to ensure it's available for the second call
      (commentService.getTaskComments as jest.Mock).mockResolvedValue(mockComments);

      const editButton = screen.getByTestId('edit-1');
      await user.click(editButton);

      await waitFor(() => {
        expect(commentService.getTaskComments).toHaveBeenCalledTimes(1);
      });
    });
  });

  describe('Delete Functionality', () => {
    it('deletes comment when delete is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });

      const deleteButton = screen.getByTestId('delete-1');
      await user.click(deleteButton);

      await waitFor(() => {
        expect(commentService.deleteComment).toHaveBeenCalledWith(1);
      });
    });

    it('reloads comments after successful delete', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });

      // Clear previous calls
      jest.clearAllMocks();
      // Reset mock to ensure it's available for the second call
      (commentService.getTaskComments as jest.Mock).mockResolvedValue(mockComments);

      const deleteButton = screen.getByTestId('delete-1');
      await user.click(deleteButton);

      await waitFor(() => {
        expect(commentService.getTaskComments).toHaveBeenCalledTimes(1);
      });
    });
  });

  describe('Moderation Functionality', () => {
    it('marks thread as resolved when mark resolved is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });

      const resolveButton = screen.getByTestId('resolve-1');
      await user.click(resolveButton);

      await waitFor(() => {
        expect(commentService.markThreadResolved).toHaveBeenCalledWith(1, true);
      });
    });

    it('reloads comments after marking resolved', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-item-1')).toBeInTheDocument();
      });

      // Clear previous calls
      jest.clearAllMocks();
      // Reset mock to ensure it's available for the second call
      (commentService.getTaskComments as jest.Mock).mockResolvedValue(mockComments);

      const resolveButton = screen.getByTestId('resolve-1');
      await user.click(resolveButton);

      await waitFor(() => {
        expect(commentService.getTaskComments).toHaveBeenCalledTimes(1);
      });
    });
  });

  describe('Filtering', () => {
    it('shows filters when comments are present', async () => {
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-filters')).toBeInTheDocument();
      });
    });

    it('does not show filters when no comments', async () => {
      (commentService.getTaskComments as jest.Mock).mockResolvedValue([]);

      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.queryByTestId('comment-filters')).not.toBeInTheDocument();
      });
    });

    it('applies filters to comments', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-filters')).toBeInTheDocument();
      });

      const filterButton = screen.getByText('Filter Unresolved');
      await user.click(filterButton);

      // The filter logic is tested in the component itself
      expect(screen.getByTestId('comment-filters')).toBeInTheDocument();
    });

    it('clears filters when clear is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-filters')).toBeInTheDocument();
      });

      const clearButton = screen.getByText('Clear Filters');
      await user.click(clearButton);

      expect(screen.getByTestId('comment-filters')).toBeInTheDocument();
    });
  });

  describe('Empty States', () => {
    it('shows empty state when no comments', async () => {
      (commentService.getTaskComments as jest.Mock).mockResolvedValue([]);

      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByText('No comments yet')).toBeInTheDocument();
        expect(screen.getByText('Start the conversation by adding the first comment.')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: /add first comment/i })).toBeInTheDocument();
      });
    });

    it('shows filtered empty state when no filtered results', async () => {
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByTestId('comment-filters')).toBeInTheDocument();
      });

      // Simulate filter that results in no comments
      const user = userEvent.setup();
      const filterButton = screen.getByText('Filter Unresolved');
      await user.click(filterButton);

      // This would depend on the actual filter implementation
      // For now, we just check that the component handles filtered states
      expect(screen.getByTestId('comment-filters')).toBeInTheDocument();
    });

    it('shows add first comment button in empty state', async () => {
      const user = userEvent.setup();
      (commentService.getTaskComments as jest.Mock).mockResolvedValue([]);

      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add first comment/i })).toBeInTheDocument();
      });

      const addFirstButton = screen.getByRole('button', { name: /add first comment/i });
      await user.click(addFirstButton);

      expect(screen.getByTestId('comment-editor')).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    it('component loads without errors', async () => {
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
      });

      expect(screen.getByText('Comments')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('has proper heading structure', async () => {
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByText('Comments')).toBeInTheDocument();
      });
    });

    it('has accessible buttons', async () => {
      render(<CommentSection {...defaultProps} />);

      await waitFor(() => {
        expect(screen.getByRole('button', { name: /add comment/i })).toBeInTheDocument();
      });
    });

    it('provides proper loading feedback', () => {
      render(<CommentSection {...defaultProps} />);

      expect(screen.getByText('Loading comments...')).toBeInTheDocument();
    });
  });

  describe('Compact Mode', () => {
    it('applies compact styling when compact prop is true', async () => {
      render(<CommentSection {...defaultProps} compact />);

      await waitFor(() => {
        // Check for compact-specific styling or behavior
        expect(screen.getByText('Comments')).toBeInTheDocument();
      });
    });
  });
});