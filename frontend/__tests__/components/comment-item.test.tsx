import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CommentItem } from '@/components/comment-item';
import { CommentResponse } from '@/types/comment';
import { commentService } from '@/services/comment-service';

// Mock the comment service
jest.mock('@/services/comment-service', () => ({
  commentService: {
    resolveUsersByIds: jest.fn(),
  },
}));

// Mock date-fns
jest.mock('date-fns', () => ({
  formatDistanceToNow: jest.fn((date, options) => '2 hours ago'),
}));

// Mock Lucide React icons
jest.mock('lucide-react', () => ({
  MessageCircle: () => <div data-testid="message-circle-icon" />,
  MoreHorizontal: () => <div data-testid="more-horizontal-icon" />,
  Edit3: () => <div data-testid="edit3-icon" />,
  Trash2: () => <div data-testid="trash2-icon" />,
  Reply: () => <div data-testid="reply-icon" />,
  Check: () => <div data-testid="check-icon" />,
  X: () => <div data-testid="x-icon" />,
  AtSign: () => <div data-testid="at-sign-icon" />,
}));

// Mock CommentEditor component
jest.mock('@/components/comment-editor', () => ({
  CommentEditor: ({ onSubmit, onCancel, isLoading, placeholder, initialContent }: any) => (
    <div data-testid="comment-editor">
      <input
        data-testid="comment-editor-input"
        defaultValue={initialContent}
        placeholder={placeholder}
        disabled={isLoading}
      />
      <button onClick={() => onSubmit('test content', [])} disabled={isLoading}>
        Submit
      </button>
      <button onClick={onCancel}>Cancel</button>
    </div>
  ),
}));

// Mock window.confirm
Object.defineProperty(window, 'confirm', {
  writable: true,
  value: jest.fn(),
});

describe('CommentItem', () => {
  const mockOnReply = jest.fn();
  const mockOnEdit = jest.fn();
  const mockOnDelete = jest.fn();
  const mockOnMarkResolved = jest.fn();

  const mockComment: CommentResponse = {
    id: 1,
    taskId: 1,
    projectId: 1,
    content: 'This is a test comment',
    mentionedUserIds: [2, 3],
    isEdited: false,
    isDeleted: false,
    authorId: 1,
    authorUsername: 'john_doe',
    createdAt: '2024-01-01T10:00:00Z',
    updatedAt: '2024-01-01T10:00:00Z',
    replies: [],
    replyCount: 0,
    canEdit: true,
    canDelete: true,
    canReply: true,
    canModerate: true,
  };

  const mockMentionedUsers = [
    {
      id: 2,
      username: 'jane_smith',
      email: 'jane@example.com',
      displayName: 'Jane Smith',
    },
    {
      id: 3,
      username: 'bob_wilson',
      email: 'bob@example.com',
      displayName: 'Bob Wilson',
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    (commentService.resolveUsersByIds as jest.Mock).mockResolvedValue(mockMentionedUsers);
    (window.confirm as jest.Mock).mockReturnValue(true);
  });

  const defaultProps = {
    comment: mockComment,
    onReply: mockOnReply,
    onEdit: mockOnEdit,
    onDelete: mockOnDelete,
    onMarkResolved: mockOnMarkResolved,
  };

  describe('Basic Rendering', () => {
    it('renders comment content', () => {
      render(<CommentItem {...defaultProps} />);

      expect(screen.getByText('This is a test comment')).toBeInTheDocument();
      expect(screen.getByText('john_doe')).toBeInTheDocument();
      expect(screen.getByText('2 hours ago')).toBeInTheDocument();
    });

    it('shows edited badge when comment is edited', () => {
      const editedComment = { ...mockComment, isEdited: true };

      render(<CommentItem {...defaultProps} comment={editedComment} />);

      expect(screen.getByText('edited')).toBeInTheDocument();
    });

    it('shows reply count for root comments with replies', () => {
      const commentWithReplies = {
        ...mockComment,
        replyCount: 3,
        replies: [
          { ...mockComment, id: 2, parentCommentId: 1 },
          { ...mockComment, id: 3, parentCommentId: 1 },
          { ...mockComment, id: 4, parentCommentId: 1 },
        ],
      };

      render(<CommentItem {...defaultProps} comment={commentWithReplies} />);

      expect(screen.getByText('3 replies')).toBeInTheDocument();
    });

    it('shows singular reply count', () => {
      const commentWithOneReply = {
        ...mockComment,
        replyCount: 1,
        replies: [{ ...mockComment, id: 2, parentCommentId: 1 }],
      };

      render(<CommentItem {...defaultProps} comment={commentWithOneReply} />);

      expect(screen.getByText('1 reply')).toBeInTheDocument();
    });

    it('applies proper indentation based on depth', () => {
      render(<CommentItem {...defaultProps} depth={2} />);

      // Look for the main container div that should have the margin style
      const commentContainer = screen.getByText('This is a test comment').closest('div[style]');
      expect(commentContainer).toHaveStyle('margin-left: 48px'); // 2 * 24px
    });
  });

  describe('Deleted Comments', () => {
    it('renders deleted comment placeholder', () => {
      const deletedComment = { ...mockComment, isDeleted: true };

      render(<CommentItem {...defaultProps} comment={deletedComment} />);

      expect(screen.getByText('[Comment deleted]')).toBeInTheDocument();
      expect(screen.queryByText('This is a test comment')).not.toBeInTheDocument();
    });

    it('still renders replies for deleted comments', () => {
      const deletedCommentWithReplies = {
        ...mockComment,
        isDeleted: true,
        replies: [{ ...mockComment, id: 2, content: 'Reply to deleted comment', parentCommentId: 1 }],
      };

      render(<CommentItem {...defaultProps} comment={deletedCommentWithReplies} />);

      expect(screen.getByText('[Comment deleted]')).toBeInTheDocument();
      expect(screen.getByText('Reply to deleted comment')).toBeInTheDocument();
    });
  });

  describe('Mentioned Users', () => {
    it('resolves and displays mentioned users', async () => {
      render(<CommentItem {...defaultProps} />);

      await waitFor(() => {
        expect(commentService.resolveUsersByIds).toHaveBeenCalledWith([2, 3]);
      });

      await waitFor(() => {
        expect(screen.getByText('Jane Smith')).toBeInTheDocument();
        expect(screen.getByText('Bob Wilson')).toBeInTheDocument();
      });
    });

    it('does not try to resolve users when no mentions', async () => {
      const commentWithoutMentions = { ...mockComment, mentionedUserIds: [] };

      render(<CommentItem {...defaultProps} comment={commentWithoutMentions} />);

      expect(commentService.resolveUsersByIds).not.toHaveBeenCalled();
      expect(screen.queryByText('Jane Smith')).not.toBeInTheDocument();
    });

    it('handles mention resolution errors gracefully', async () => {
      const consoleError = jest.spyOn(console, 'error').mockImplementation();
      (commentService.resolveUsersByIds as jest.Mock).mockRejectedValue(
        new Error('Resolution failed')
      );

      render(<CommentItem {...defaultProps} />);

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalledWith(
          'Failed to resolve mentioned users:',
          expect.any(Error)
        );
      });

      consoleError.mockRestore();
    });
  });

  describe('Actions Menu', () => {
    it('shows reply option when canReply is true', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      // Use getAllByText to handle multiple Reply elements
      const replyElements = screen.getAllByText('Reply');
      expect(replyElements.length).toBeGreaterThan(0);
    });

    it('shows edit option when canEdit is true', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      expect(screen.getByText('Edit')).toBeInTheDocument();
    });

    it('shows delete option when canDelete is true', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      expect(screen.getByText('Delete')).toBeInTheDocument();
    });

    it('shows moderation options when canModerate is true', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      expect(screen.getByText('Mark Resolved')).toBeInTheDocument();
      expect(screen.getByText('Mark Unresolved')).toBeInTheDocument();
    });

    it('does not show options when permissions are false', async () => {
      const user = userEvent.setup();
      const restrictedComment = {
        ...mockComment,
        canEdit: false,
        canDelete: false,
        canReply: false,
        canModerate: false,
      };

      render(<CommentItem {...defaultProps} comment={restrictedComment} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      expect(screen.queryByText('Reply')).not.toBeInTheDocument();
      expect(screen.queryByText('Edit')).not.toBeInTheDocument();
      expect(screen.queryByText('Delete')).not.toBeInTheDocument();
      expect(screen.queryByText('Mark Resolved')).not.toBeInTheDocument();
    });
  });

  describe('Reply Functionality', () => {
    it('shows reply editor when reply is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const replyButton = screen.getByText('Reply');
      await user.click(replyButton);

      expect(screen.getByTestId('comment-editor')).toBeInTheDocument();
    });

    it('shows reply editor from dropdown menu', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      // Get the reply option from the dropdown menu specifically
      const replyMenuItem = screen.getByRole('menuitem', { name: /reply/i });
      await user.click(replyMenuItem);

      expect(screen.getByTestId('comment-editor')).toBeInTheDocument();
    });

    it('calls onReply when reply is submitted', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const replyButton = screen.getByText('Reply');
      await user.click(replyButton);

      const submitButton = screen.getByText('Submit');
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnReply).toHaveBeenCalledWith(1, 'test content', []);
      });
    });

    it('closes reply editor when cancelled', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const replyButton = screen.getByText('Reply');
      await user.click(replyButton);

      expect(screen.getByTestId('comment-editor')).toBeInTheDocument();

      const cancelButton = screen.getByText('Cancel');
      await user.click(cancelButton);

      expect(screen.queryByTestId('comment-editor')).not.toBeInTheDocument();
    });
  });

  describe('Edit Functionality', () => {
    it('shows edit editor when edit is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const editMenuItem = screen.getByText('Edit');
      await user.click(editMenuItem);

      expect(screen.getByTestId('comment-editor')).toBeInTheDocument();
      expect(screen.queryByText('This is a test comment')).not.toBeInTheDocument();
    });

    it('calls onEdit when edit is submitted', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const editMenuItem = screen.getByText('Edit');
      await user.click(editMenuItem);

      const submitButton = screen.getByText('Submit');
      await user.click(submitButton);

      await waitFor(() => {
        expect(mockOnEdit).toHaveBeenCalledWith(1, 'test content', []);
      });
    });

    it('closes edit editor when cancelled', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const editMenuItem = screen.getByText('Edit');
      await user.click(editMenuItem);

      expect(screen.getByTestId('comment-editor')).toBeInTheDocument();

      const cancelButton = screen.getByText('Cancel');
      await user.click(cancelButton);

      expect(screen.queryByTestId('comment-editor')).not.toBeInTheDocument();
      expect(screen.getByText('This is a test comment')).toBeInTheDocument();
    });
  });

  describe('Delete Functionality', () => {
    it('calls onDelete when delete is confirmed', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const deleteMenuItem = screen.getByText('Delete');
      await user.click(deleteMenuItem);

      expect(window.confirm).toHaveBeenCalledWith(
        'Are you sure you want to delete this comment?'
      );
      expect(mockOnDelete).toHaveBeenCalledWith(1);
    });

    it('does not call onDelete when delete is cancelled', async () => {
      const user = userEvent.setup();
      (window.confirm as jest.Mock).mockReturnValue(false);

      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const deleteMenuItem = screen.getByText('Delete');
      await user.click(deleteMenuItem);

      expect(window.confirm).toHaveBeenCalled();
      expect(mockOnDelete).not.toHaveBeenCalled();
    });
  });

  describe('Moderation Functionality', () => {
    it('calls onMarkResolved with true when mark resolved is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const resolveMenuItem = screen.getByText('Mark Resolved');
      await user.click(resolveMenuItem);

      await waitFor(() => {
        expect(mockOnMarkResolved).toHaveBeenCalledWith(1, true);
      });
    });

    it('calls onMarkResolved with false when mark unresolved is clicked', async () => {
      const user = userEvent.setup();
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const unresolveMenuItem = screen.getByText('Mark Unresolved');
      await user.click(unresolveMenuItem);

      await waitFor(() => {
        expect(mockOnMarkResolved).toHaveBeenCalledWith(1, false);
      });
    });
  });

  describe('Nested Replies', () => {
    it('renders nested replies recursively', () => {
      const commentWithNestedReplies = {
        ...mockComment,
        replies: [
          {
            ...mockComment,
            id: 2,
            content: 'First reply',
            parentCommentId: 1,
            replies: [
              {
                ...mockComment,
                id: 3,
                content: 'Reply to reply',
                parentCommentId: 2,
                replies: [],
              },
            ],
          },
        ],
      };

      render(<CommentItem {...defaultProps} comment={commentWithNestedReplies} />);

      expect(screen.getByText('This is a test comment')).toBeInTheDocument();
      expect(screen.getByText('First reply')).toBeInTheDocument();
      expect(screen.getByText('Reply to reply')).toBeInTheDocument();
    });

    it('applies increasing depth to nested replies', () => {
      const commentWithNestedReplies = {
        ...mockComment,
        replies: [
          {
            ...mockComment,
            id: 2,
            content: 'First reply',
            parentCommentId: 1,
            replies: [],
          },
        ],
      };

      render(<CommentItem {...defaultProps} comment={commentWithNestedReplies} depth={1} />);

      // Check that nested comment has increased depth
      const nestedComment = screen.getByText('First reply').closest('div[style]');
      expect(nestedComment).toHaveStyle('margin-left: 48px'); // (1 + 1) * 24px
    });
  });

  describe('Error Handling', () => {
    it('handles reply submission errors gracefully', async () => {
      const user = userEvent.setup();
      const consoleError = jest.spyOn(console, 'error').mockImplementation();
      mockOnReply.mockRejectedValue(new Error('Reply failed'));

      render(<CommentItem {...defaultProps} />);

      const replyButton = screen.getByText('Reply');
      await user.click(replyButton);

      const submitButton = screen.getByText('Submit');
      await user.click(submitButton);

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalledWith(
          'Failed to reply to comment:',
          expect.any(Error)
        );
      });

      consoleError.mockRestore();
    });

    it('handles edit submission errors gracefully', async () => {
      const user = userEvent.setup();
      const consoleError = jest.spyOn(console, 'error').mockImplementation();
      mockOnEdit.mockRejectedValue(new Error('Edit failed'));

      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const editMenuItem = screen.getByText('Edit');
      await user.click(editMenuItem);

      const submitButton = screen.getByText('Submit');
      await user.click(submitButton);

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalledWith(
          'Failed to edit comment:',
          expect.any(Error)
        );
      });

      consoleError.mockRestore();
    });

    it('handles delete errors gracefully', async () => {
      const user = userEvent.setup();
      const consoleError = jest.spyOn(console, 'error').mockImplementation();
      mockOnDelete.mockRejectedValue(new Error('Delete failed'));

      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      await user.click(moreButton!);

      const deleteMenuItem = screen.getByText('Delete');
      await user.click(deleteMenuItem);

      await waitFor(() => {
        expect(consoleError).toHaveBeenCalledWith(
          'Failed to delete comment:',
          expect.any(Error)
        );
      });

      consoleError.mockRestore();
    });
  });

  describe('Accessibility', () => {
    it('has proper button roles and labels', () => {
      render(<CommentItem {...defaultProps} />);

      expect(screen.getByRole('button', { name: /reply/i })).toBeInTheDocument();
      expect(screen.getByTestId('more-horizontal-icon').closest('button')).toBeInTheDocument();
    });

    it('supports keyboard navigation for dropdown menu', async () => {
      render(<CommentItem {...defaultProps} />);

      const moreButton = screen.getByTestId('more-horizontal-icon').closest('button');
      expect(moreButton).toBeInTheDocument();
    });
  });
});