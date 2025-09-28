import { CommentService, commentService } from '@/services/comment-service';
import { AuthenticatedApiClient } from '@/services/authenticated-api-client';
import {
  CommentResponse,
  CreateCommentRequest,
  UpdateCommentRequest,
  CommentFilters,
} from '@/types/comment';

// Mock the AuthenticatedApiClient
jest.mock('@/services/authenticated-api-client', () => ({
  AuthenticatedApiClient: jest.fn().mockImplementation(() => ({
    get: jest.fn(),
    post: jest.fn(),
    put: jest.fn(),
    delete: jest.fn(),
    patch: jest.fn(),
  })),
}));

describe('CommentService', () => {
  let service: CommentService;
  let mockApiClient: jest.Mocked<AuthenticatedApiClient>;

  const mockComment: CommentResponse = {
    id: 1,
    taskId: 1,
    projectId: 1,
    content: 'Test comment',
    mentionedUserIds: [2, 3],
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

  const mockUsers = [
    {
      id: 2,
      username: 'jane_smith',
      email: 'jane@example.com',
    },
    {
      id: 3,
      username: 'bob_wilson',
      email: 'bob@example.com',
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
    service = new CommentService();
    mockApiClient = (service as any).authenticatedClient;
  });

  describe('Constructor', () => {
    it('creates instance with authenticated client', () => {
      expect(AuthenticatedApiClient).toHaveBeenCalled();
      expect(service).toBeInstanceOf(CommentService);
    });

    it('exports singleton instance', () => {
      expect(commentService).toBeInstanceOf(CommentService);
    });
  });

  describe('createComment', () => {
    const createRequest: CreateCommentRequest = {
      taskId: 1,
      content: 'New comment',
      mentionedUserIds: [2],
    };

    it('creates comment successfully', async () => {
      const expectedResponse = { id: 1, taskId: 1, content: 'New comment', authorId: 1, createdAt: '2024-01-01T10:00:00Z' };
      mockApiClient.post.mockResolvedValue(expectedResponse);

      const result = await service.createComment(createRequest);

      expect(mockApiClient.post).toHaveBeenCalledWith('/api/tasks/comments', createRequest);
      expect(result).toEqual(expectedResponse);
    });

    it('handles API errors', async () => {
      mockApiClient.post.mockRejectedValue(new Error('API Error'));

      await expect(service.createComment(createRequest)).rejects.toThrow('API Error');
    });
  });

  describe('getTaskComments', () => {
    it('gets task comments without filters', async () => {
      mockApiClient.get.mockResolvedValue([mockComment]);

      const result = await service.getTaskComments(1);

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/tasks/1/comments');
      expect(result).toEqual([mockComment]);
    });

    it('gets task comments with filters', async () => {
      const filters: CommentFilters = {
        authorId: 1,
        resolved: true,
        filter: 'UNRESOLVED',
      };

      mockApiClient.get.mockResolvedValue([mockComment]);

      const result = await service.getTaskComments(1, filters);

      expect(mockApiClient.get).toHaveBeenCalledWith(
        '/api/tasks/1/comments?authorId=1&resolved=true&filter=UNRESOLVED'
      );
      expect(result).toEqual([mockComment]);
    });

    it('handles partial filters', async () => {
      const filters: CommentFilters = {
        authorId: 1,
      };

      mockApiClient.get.mockResolvedValue([mockComment]);

      await service.getTaskComments(1, filters);

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/tasks/1/comments?authorId=1');
    });

    it('handles boolean false filters correctly', async () => {
      const filters: CommentFilters = {
        resolved: false,
      };

      mockApiClient.get.mockResolvedValue([mockComment]);

      await service.getTaskComments(1, filters);

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/tasks/1/comments?resolved=false');
    });
  });

  describe('getSubtaskComments', () => {
    it('gets subtask comments without filters', async () => {
      mockApiClient.get.mockResolvedValue([mockComment]);

      const result = await service.getSubtaskComments(1);

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/subtasks/1/comments');
      expect(result).toEqual([mockComment]);
    });

    it('gets subtask comments with filters', async () => {
      const filters: CommentFilters = {
        filter: 'BY_COMMENTER',
      };

      mockApiClient.get.mockResolvedValue([mockComment]);

      const result = await service.getSubtaskComments(1, filters);

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/subtasks/1/comments?filter=BY_COMMENTER');
      expect(result).toEqual([mockComment]);
    });
  });

  describe('getCommentById', () => {
    it('gets comment by ID successfully', async () => {
      mockApiClient.get.mockResolvedValue(mockComment);

      const result = await service.getCommentById(1);

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/tasks/comments/1');
      expect(result).toEqual(mockComment);
    });
  });

  describe('updateComment', () => {
    const updateRequest: UpdateCommentRequest = {
      commentId: 1,
      content: 'Updated comment',
      mentionedUserIds: [2, 3],
    };

    it('updates comment successfully', async () => {
      const updatedComment = { ...mockComment, content: 'Updated comment', isEdited: true };
      mockApiClient.put.mockResolvedValue(updatedComment);

      const result = await service.updateComment(updateRequest);

      expect(mockApiClient.put).toHaveBeenCalledWith('/api/tasks/comments', updateRequest);
      expect(result).toEqual(updatedComment);
    });
  });

  describe('deleteComment', () => {
    it('deletes comment successfully', async () => {
      mockApiClient.delete.mockResolvedValue(undefined);

      await service.deleteComment(1);

      expect(mockApiClient.delete).toHaveBeenCalledWith('/api/tasks/comments/1');
    });
  });

  describe('getUserMentions', () => {
    it('gets user mentions successfully', async () => {
      mockApiClient.get.mockResolvedValue([mockComment]);

      const result = await service.getUserMentions();

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/tasks/comments/mentions');
      expect(result).toEqual([mockComment]);
    });
  });

  describe('getProjectMembers', () => {
    beforeEach(() => {
      // Clear cache before each test
      (service as any).projectMembersCache.clear();
    });

    it('gets project members successfully', async () => {
      mockApiClient.get.mockResolvedValue(mockUsers);

      const result = await service.getProjectMembers(1);

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/user/project/1/members');
      expect(result).toEqual([
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
        {
          id: 3,
          username: 'bob_wilson',
          email: 'bob@example.com',
          displayName: 'bob_wilson',
        },
      ]);
    });

    it('caches project members', async () => {
      mockApiClient.get.mockResolvedValue(mockUsers);

      // First call
      await service.getProjectMembers(1);
      // Second call should use cache
      await service.getProjectMembers(1);

      expect(mockApiClient.get).toHaveBeenCalledTimes(1);
    });

    it('returns expired cache when API fails', async () => {
      mockApiClient.get.mockResolvedValue(mockUsers);

      // First call to populate cache
      await service.getProjectMembers(1);

      // Expire cache manually
      const cache = (service as any).projectMembersCache.get(1);
      cache.timestamp = Date.now() - (6 * 60 * 1000); // 6 minutes ago

      // Mock API failure
      mockApiClient.get.mockRejectedValue(new Error('API Error'));
      const consoleWarn = jest.spyOn(console, 'warn').mockImplementation();

      const result = await service.getProjectMembers(1);

      expect(consoleWarn).toHaveBeenCalledWith(
        'API failed, returning expired cache for project members'
      );
      expect(result).toEqual([
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
        {
          id: 3,
          username: 'bob_wilson',
          email: 'bob@example.com',
          displayName: 'bob_wilson',
        },
      ]);

      consoleWarn.mockRestore();
    });

    it('throws error when API fails and no cache available', async () => {
      mockApiClient.get.mockRejectedValue(new Error('API Error'));

      await expect(service.getProjectMembers(1)).rejects.toThrow('API Error');
    });
  });

  describe('resolveUsersByIds', () => {
    beforeEach(() => {
      // Clear cache before each test
      (service as any).userCache.clear();
    });

    it('returns empty array for empty input', async () => {
      const result = await service.resolveUsersByIds([]);

      expect(result).toEqual([]);
      expect(mockApiClient.post).not.toHaveBeenCalled();
    });

    it('returns empty array for null input', async () => {
      const result = await service.resolveUsersByIds(null as any);

      expect(result).toEqual([]);
      expect(mockApiClient.post).not.toHaveBeenCalled();
    });

    it('resolves users by IDs successfully', async () => {
      mockApiClient.post.mockResolvedValue(mockUsers);

      const result = await service.resolveUsersByIds([2, 3]);

      expect(mockApiClient.post).toHaveBeenCalledWith('/api/user/batch', [2, 3]);
      expect(result).toEqual([
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
        {
          id: 3,
          username: 'bob_wilson',
          email: 'bob@example.com',
          displayName: 'bob_wilson',
        },
      ]);
    });

    it('uses cached users when available', async () => {
      // Populate cache
      const cachedUsers = [
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
      ];
      (service as any).cacheUsers(cachedUsers);

      const result = await service.resolveUsersByIds([2]);

      expect(mockApiClient.post).not.toHaveBeenCalled();
      expect(result).toEqual(cachedUsers);
    });

    it('fetches only missing users when some are cached', async () => {
      // Cache user 2
      const cachedUsers = [
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
      ];
      (service as any).cacheUsers(cachedUsers);

      // Mock API to return user 3
      mockApiClient.post.mockResolvedValue([mockUsers[1]]);

      const result = await service.resolveUsersByIds([2, 3]);

      expect(mockApiClient.post).toHaveBeenCalledWith('/api/user/batch', [3]);
      expect(result).toEqual([
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
        {
          id: 3,
          username: 'bob_wilson',
          email: 'bob@example.com',
          displayName: 'bob_wilson',
        },
      ]);
    });

    it('returns cached users when API fails', async () => {
      // Cache user 2
      const cachedUsers = [
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
      ];
      (service as any).cacheUsers(cachedUsers);

      mockApiClient.post.mockRejectedValue(new Error('API Error'));
      const consoleWarn = jest.spyOn(console, 'warn').mockImplementation();

      const result = await service.resolveUsersByIds([2, 3]);

      expect(consoleWarn).toHaveBeenCalledWith(
        'Failed to resolve some users, returning cached users only'
      );
      expect(result).toEqual(cachedUsers);

      consoleWarn.mockRestore();
    });
  });

  describe('searchUsersForMention', () => {
    beforeEach(() => {
      // Clear cache before each test
      (service as any).projectMembersCache.clear();
    });

    it('returns empty array when no projectId provided', async () => {
      const result = await service.searchUsersForMention('john');

      expect(result).toEqual([]);
      expect(mockApiClient.get).not.toHaveBeenCalled();
    });

    it('returns all project members when no query provided', async () => {
      mockApiClient.get.mockResolvedValue(mockUsers);

      const result = await service.searchUsersForMention('', 1);

      expect(result).toEqual([
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
        {
          id: 3,
          username: 'bob_wilson',
          email: 'bob@example.com',
          displayName: 'bob_wilson',
        },
      ]);
    });

    it('filters users by username', async () => {
      mockApiClient.get.mockResolvedValue(mockUsers);

      const result = await service.searchUsersForMention('jane', 1);

      expect(result).toEqual([
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
      ]);
    });

    it('filters users by email', async () => {
      mockApiClient.get.mockResolvedValue(mockUsers);

      const result = await service.searchUsersForMention('bob@example.com', 1);

      expect(result).toEqual([
        {
          id: 3,
          username: 'bob_wilson',
          email: 'bob@example.com',
          displayName: 'bob_wilson',
        },
      ]);
    });

    it('performs case-insensitive search', async () => {
      mockApiClient.get.mockResolvedValue(mockUsers);

      const result = await service.searchUsersForMention('JANE', 1);

      expect(result).toEqual([
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'jane_smith',
        },
      ]);
    });

    it('searches by display name when available', async () => {
      const usersWithDisplayNames = [
        {
          id: 2,
          username: 'jane_smith',
          email: 'jane@example.com',
          displayName: 'Jane Smith',
        },
      ];

      // Manually populate cache with display names
      (service as any).projectMembersCache.set(1, {
        data: usersWithDisplayNames,
        timestamp: Date.now(),
      });

      const result = await service.searchUsersForMention('Smith', 1);

      expect(result).toEqual(usersWithDisplayNames);
    });
  });

  describe('markThreadResolved', () => {
    it('marks thread as resolved', async () => {
      mockApiClient.patch.mockResolvedValue(undefined);

      await service.markThreadResolved(1, true);

      expect(mockApiClient.patch).toHaveBeenCalledWith(
        '/api/tasks/comments/1/resolve',
        { resolved: true }
      );
    });

    it('marks thread as unresolved', async () => {
      mockApiClient.patch.mockResolvedValue(undefined);

      await service.markThreadResolved(1, false);

      expect(mockApiClient.patch).toHaveBeenCalledWith(
        '/api/tasks/comments/1/resolve',
        { resolved: false }
      );
    });
  });

  describe('getCommentActivity', () => {
    it('gets comment activity successfully', async () => {
      mockApiClient.get.mockResolvedValue([mockComment]);

      const result = await service.getCommentActivity();

      expect(mockApiClient.get).toHaveBeenCalledWith('/api/tasks/comments/activity');
      expect(result).toEqual([mockComment]);
    });
  });

  describe('Cache Management', () => {
    it('validates cache expiry correctly', () => {
      const now = Date.now();
      const validTimestamp = now - (4 * 60 * 1000); // 4 minutes ago
      const expiredTimestamp = now - (6 * 60 * 1000); // 6 minutes ago

      expect((service as any).isCacheValid(validTimestamp)).toBe(true);
      expect((service as any).isCacheValid(expiredTimestamp)).toBe(false);
    });

    it('caches users correctly', () => {
      const users = [
        {
          id: 1,
          username: 'test',
          email: 'test@example.com',
          displayName: 'Test User',
        },
      ];

      (service as any).cacheUsers(users);

      const cachedUser = (service as any).userCache.get(1);
      expect(cachedUser).toEqual(users[0]);
    });

    it('retrieves cached users correctly', () => {
      const users = [
        {
          id: 1,
          username: 'test',
          email: 'test@example.com',
          displayName: 'Test User',
        },
        {
          id: 2,
          username: 'test2',
          email: 'test2@example.com',
          displayName: 'Test User 2',
        },
      ];

      (service as any).cacheUsers(users);

      const result = (service as any).getCachedUsers([1, 3]);

      expect(result.cached).toEqual([users[0]]);
      expect(result.missing).toEqual([3]);
    });
  });
});