import {
  CommentResponse,
  CreateCommentRequest,
  CreateCommentResponse,
  UpdateCommentRequest,
  CommentFilters,
  UserMention,
  CommentPermissions,
} from '@/types/comment';

describe('Comment Types', () => {
  describe('CommentResponse', () => {
    it('should have all required properties', () => {
      const comment: CommentResponse = {
        id: 1,
        taskId: 1,
        subtaskId: undefined,
        projectId: 1,
        content: 'Test comment',
        mentionedUserIds: [2, 3],
        isEdited: false,
        isDeleted: false,
        authorId: 1,
        authorUsername: 'john_doe',
        createdAt: '2024-01-01T10:00:00Z',
        updatedAt: '2024-01-01T10:00:00Z',
        parentCommentId: undefined,
        replies: [],
        replyCount: 0,
        canEdit: true,
        canDelete: true,
        canReply: true,
        canModerate: false,
      };

      expect(comment.id).toBe(1);
      expect(comment.content).toBe('Test comment');
      expect(comment.authorUsername).toBe('john_doe');
      expect(comment.replies).toEqual([]);
      expect(comment.canEdit).toBe(true);
    });

    it('should support nested replies', () => {
      const reply: CommentResponse = {
        id: 2,
        taskId: 1,
        projectId: 1,
        content: 'Reply comment',
        mentionedUserIds: [],
        isEdited: false,
        isDeleted: false,
        authorId: 2,
        authorUsername: 'jane_doe',
        createdAt: '2024-01-01T11:00:00Z',
        parentCommentId: 1,
        replies: [],
        replyCount: 0,
        canEdit: true,
        canDelete: true,
        canReply: true,
        canModerate: false,
      };

      const comment: CommentResponse = {
        id: 1,
        taskId: 1,
        projectId: 1,
        content: 'Parent comment',
        mentionedUserIds: [],
        isEdited: false,
        isDeleted: false,
        authorId: 1,
        authorUsername: 'john_doe',
        createdAt: '2024-01-01T10:00:00Z',
        replies: [reply],
        replyCount: 1,
        canEdit: true,
        canDelete: true,
        canReply: true,
        canModerate: true,
      };

      expect(comment.replies).toHaveLength(1);
      expect(comment.replies[0].parentCommentId).toBe(1);
      expect(comment.replyCount).toBe(1);
    });

    it('should support both task and subtask comments', () => {
      const taskComment: CommentResponse = {
        id: 1,
        taskId: 1,
        projectId: 1,
        content: 'Task comment',
        mentionedUserIds: [],
        isEdited: false,
        isDeleted: false,
        authorId: 1,
        authorUsername: 'john_doe',
        createdAt: '2024-01-01T10:00:00Z',
        replies: [],
        replyCount: 0,
        canEdit: true,
        canDelete: true,
        canReply: true,
        canModerate: false,
      };

      const subtaskComment: CommentResponse = {
        id: 2,
        subtaskId: 1,
        projectId: 1,
        content: 'Subtask comment',
        mentionedUserIds: [],
        isEdited: false,
        isDeleted: false,
        authorId: 1,
        authorUsername: 'john_doe',
        createdAt: '2024-01-01T10:00:00Z',
        replies: [],
        replyCount: 0,
        canEdit: true,
        canDelete: true,
        canReply: true,
        canModerate: false,
      };

      expect(taskComment.taskId).toBe(1);
      expect(taskComment.subtaskId).toBeUndefined();
      expect(subtaskComment.taskId).toBeUndefined();
      expect(subtaskComment.subtaskId).toBe(1);
    });
  });

  describe('CreateCommentRequest', () => {
    it('should create task comment request', () => {
      const request: CreateCommentRequest = {
        taskId: 1,
        content: 'New task comment',
        mentionedUserIds: [2, 3],
      };

      expect(request.taskId).toBe(1);
      expect(request.subtaskId).toBeUndefined();
      expect(request.content).toBe('New task comment');
      expect(request.mentionedUserIds).toEqual([2, 3]);
    });

    it('should create subtask comment request', () => {
      const request: CreateCommentRequest = {
        subtaskId: 1,
        content: 'New subtask comment',
      };

      expect(request.subtaskId).toBe(1);
      expect(request.taskId).toBeUndefined();
      expect(request.content).toBe('New subtask comment');
      expect(request.mentionedUserIds).toBeUndefined();
    });

    it('should create reply comment request', () => {
      const request: CreateCommentRequest = {
        taskId: 1,
        content: 'Reply to comment',
        parentCommentId: 5,
        mentionedUserIds: [],
      };

      expect(request.taskId).toBe(1);
      expect(request.parentCommentId).toBe(5);
      expect(request.content).toBe('Reply to comment');
    });

    it('should allow minimal comment request', () => {
      const request: CreateCommentRequest = {
        taskId: 1,
        content: 'Simple comment',
      };

      expect(request.taskId).toBe(1);
      expect(request.content).toBe('Simple comment');
      expect(request.mentionedUserIds).toBeUndefined();
      expect(request.parentCommentId).toBeUndefined();
    });
  });

  describe('CreateCommentResponse', () => {
    it('should have required response properties', () => {
      const response: CreateCommentResponse = {
        id: 1,
        taskId: 1,
        content: 'Created comment',
        authorId: 1,
        createdAt: '2024-01-01T10:00:00Z',
      };

      expect(response.id).toBe(1);
      expect(response.taskId).toBe(1);
      expect(response.content).toBe('Created comment');
      expect(response.authorId).toBe(1);
      expect(response.createdAt).toBe('2024-01-01T10:00:00Z');
    });

    it('should support subtask creation response', () => {
      const response: CreateCommentResponse = {
        id: 2,
        subtaskId: 1,
        content: 'Created subtask comment',
        authorId: 1,
        createdAt: '2024-01-01T10:00:00Z',
      };

      expect(response.id).toBe(2);
      expect(response.subtaskId).toBe(1);
      expect(response.taskId).toBeUndefined();
    });
  });

  describe('UpdateCommentRequest', () => {
    it('should have required update properties', () => {
      const request: UpdateCommentRequest = {
        commentId: 1,
        content: 'Updated content',
        mentionedUserIds: [2, 3],
      };

      expect(request.commentId).toBe(1);
      expect(request.content).toBe('Updated content');
      expect(request.mentionedUserIds).toEqual([2, 3]);
    });

    it('should allow update without mentions', () => {
      const request: UpdateCommentRequest = {
        commentId: 1,
        content: 'Updated content without mentions',
      };

      expect(request.commentId).toBe(1);
      expect(request.content).toBe('Updated content without mentions');
      expect(request.mentionedUserIds).toBeUndefined();
    });
  });

  describe('CommentFilters', () => {
    it('should create filter with all properties', () => {
      const filters: CommentFilters = {
        authorId: 1,
        resolved: true,
        filter: 'UNRESOLVED',
      };

      expect(filters.authorId).toBe(1);
      expect(filters.resolved).toBe(true);
      expect(filters.filter).toBe('UNRESOLVED');
    });

    it('should create filter with single property', () => {
      const authorFilter: CommentFilters = {
        authorId: 1,
      };

      const resolvedFilter: CommentFilters = {
        resolved: false,
      };

      const typeFilter: CommentFilters = {
        filter: 'BY_COMMENTER',
      };

      expect(authorFilter.authorId).toBe(1);
      expect(resolvedFilter.resolved).toBe(false);
      expect(typeFilter.filter).toBe('BY_COMMENTER');
    });

    it('should create empty filter', () => {
      const filters: CommentFilters = {};

      expect(Object.keys(filters)).toHaveLength(0);
    });

    it('should support all filter types', () => {
      const allFilter: CommentFilters = { filter: 'ALL' };
      const unresolvedFilter: CommentFilters = { filter: 'UNRESOLVED' };
      const commenterFilter: CommentFilters = { filter: 'BY_COMMENTER' };

      expect(allFilter.filter).toBe('ALL');
      expect(unresolvedFilter.filter).toBe('UNRESOLVED');
      expect(commenterFilter.filter).toBe('BY_COMMENTER');
    });
  });

  describe('UserMention', () => {
    it('should have required user properties', () => {
      const user: UserMention = {
        id: 1,
        username: 'john_doe',
        email: 'john@example.com',
        displayName: 'John Doe',
      };

      expect(user.id).toBe(1);
      expect(user.username).toBe('john_doe');
      expect(user.email).toBe('john@example.com');
      expect(user.displayName).toBe('John Doe');
    });

    it('should allow user without display name', () => {
      const user: UserMention = {
        id: 1,
        username: 'john_doe',
        email: 'john@example.com',
      };

      expect(user.id).toBe(1);
      expect(user.username).toBe('john_doe');
      expect(user.email).toBe('john@example.com');
      expect(user.displayName).toBeUndefined();
    });
  });

  describe('CommentPermissions', () => {
    it('should have all permission properties', () => {
      const permissions: CommentPermissions = {
        canRead: true,
        canWrite: true,
        canEdit: false,
        canDelete: false,
        canModerate: true,
      };

      expect(permissions.canRead).toBe(true);
      expect(permissions.canWrite).toBe(true);
      expect(permissions.canEdit).toBe(false);
      expect(permissions.canDelete).toBe(false);
      expect(permissions.canModerate).toBe(true);
    });

    it('should support read-only permissions', () => {
      const readOnlyPermissions: CommentPermissions = {
        canRead: true,
        canWrite: false,
        canEdit: false,
        canDelete: false,
        canModerate: false,
      };

      expect(readOnlyPermissions.canRead).toBe(true);
      expect(readOnlyPermissions.canWrite).toBe(false);
      expect(readOnlyPermissions.canEdit).toBe(false);
      expect(readOnlyPermissions.canDelete).toBe(false);
      expect(readOnlyPermissions.canModerate).toBe(false);
    });

    it('should support full permissions', () => {
      const fullPermissions: CommentPermissions = {
        canRead: true,
        canWrite: true,
        canEdit: true,
        canDelete: true,
        canModerate: true,
      };

      expect(Object.values(fullPermissions).every(Boolean)).toBe(true);
    });
  });

  describe('Type Consistency', () => {
    it('should maintain ID consistency across types', () => {
      const commentId = 1;
      const userId = 2;
      const taskId = 3;
      const projectId = 4;

      const comment: CommentResponse = {
        id: commentId,
        taskId: taskId,
        projectId: projectId,
        content: 'Test',
        mentionedUserIds: [userId],
        isEdited: false,
        isDeleted: false,
        authorId: userId,
        authorUsername: 'test',
        createdAt: '2024-01-01T10:00:00Z',
        replies: [],
        replyCount: 0,
        canEdit: true,
        canDelete: true,
        canReply: true,
        canModerate: false,
      };

      const updateRequest: UpdateCommentRequest = {
        commentId: commentId,
        content: 'Updated',
        mentionedUserIds: [userId],
      };

      const user: UserMention = {
        id: userId,
        username: 'test',
        email: 'test@example.com',
      };

      expect(comment.id).toBe(updateRequest.commentId);
      expect(comment.authorId).toBe(user.id);
      expect(comment.mentionedUserIds[0]).toBe(user.id);
    });

    it('should support optional properties correctly', () => {
      // Test with minimal required properties
      const minimalComment: CommentResponse = {
        id: 1,
        projectId: 1,
        content: 'Test',
        mentionedUserIds: [],
        isEdited: false,
        isDeleted: false,
        authorId: 1,
        authorUsername: 'test',
        createdAt: '2024-01-01T10:00:00Z',
        replies: [],
        replyCount: 0,
        canEdit: false,
        canDelete: false,
        canReply: false,
        canModerate: false,
      };

      // taskId and subtaskId should be optional
      expect(minimalComment.taskId).toBeUndefined();
      expect(minimalComment.subtaskId).toBeUndefined();
      expect(minimalComment.parentCommentId).toBeUndefined();
      expect(minimalComment.updatedAt).toBeUndefined();
    });
  });

  describe('Data Validation', () => {
    it('should handle empty arrays correctly', () => {
      const comment: CommentResponse = {
        id: 1,
        projectId: 1,
        content: 'Test',
        mentionedUserIds: [],
        isEdited: false,
        isDeleted: false,
        authorId: 1,
        authorUsername: 'test',
        createdAt: '2024-01-01T10:00:00Z',
        replies: [],
        replyCount: 0,
        canEdit: true,
        canDelete: true,
        canReply: true,
        canModerate: false,
      };

      expect(Array.isArray(comment.mentionedUserIds)).toBe(true);
      expect(comment.mentionedUserIds).toHaveLength(0);
      expect(Array.isArray(comment.replies)).toBe(true);
      expect(comment.replies).toHaveLength(0);
    });

    it('should handle boolean flags correctly', () => {
      const comment: CommentResponse = {
        id: 1,
        projectId: 1,
        content: 'Test',
        mentionedUserIds: [],
        isEdited: true,
        isDeleted: false,
        authorId: 1,
        authorUsername: 'test',
        createdAt: '2024-01-01T10:00:00Z',
        replies: [],
        replyCount: 0,
        canEdit: false,
        canDelete: true,
        canReply: false,
        canModerate: true,
      };

      expect(typeof comment.isEdited).toBe('boolean');
      expect(typeof comment.isDeleted).toBe('boolean');
      expect(typeof comment.canEdit).toBe('boolean');
      expect(typeof comment.canDelete).toBe('boolean');
      expect(typeof comment.canReply).toBe('boolean');
      expect(typeof comment.canModerate).toBe('boolean');
    });

    it('should handle date strings correctly', () => {
      const now = new Date().toISOString();
      const comment: CommentResponse = {
        id: 1,
        projectId: 1,
        content: 'Test',
        mentionedUserIds: [],
        isEdited: false,
        isDeleted: false,
        authorId: 1,
        authorUsername: 'test',
        createdAt: now,
        updatedAt: now,
        replies: [],
        replyCount: 0,
        canEdit: true,
        canDelete: true,
        canReply: true,
        canModerate: false,
      };

      expect(typeof comment.createdAt).toBe('string');
      expect(typeof comment.updatedAt).toBe('string');
      expect(new Date(comment.createdAt).toISOString()).toBe(now);
      expect(new Date(comment.updatedAt!).toISOString()).toBe(now);
    });
  });
});