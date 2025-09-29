import { AuthenticatedApiClient } from './authenticated-api-client';
import {
  CommentResponse,
  CreateCommentRequest,
  CreateCommentResponse,
  UpdateCommentRequest,
  CommentFilters,
  UserMention
} from '@/types/comment';

/**
 * Service for managing comments on tasks and subtasks
 * Provides methods for creating, reading, updating, and deleting comments
 * Supports threaded comments, mentions, and filtering
 */
export class CommentService {
  private authenticatedClient: AuthenticatedApiClient;
  private baseUrl = '/api';
  private userCache = new Map<number, UserMention>();
  private projectMembersCache = new Map<number, { data: UserMention[], timestamp: number }>();
  private cacheExpiry = 5 * 60 * 1000; // 5 minutes

  constructor() {
    this.authenticatedClient = new AuthenticatedApiClient();
  }

  /**
   * Check if cached data is still valid
   */
  private isCacheValid(timestamp: number): boolean {
    return Date.now() - timestamp < this.cacheExpiry;
  }

  /**
   * Add users to cache
   */
  private cacheUsers(users: UserMention[]): void {
    users.forEach(user => {
      this.userCache.set(user.id, user);
    });
  }

  /**
   * Get cached users by IDs
   */
  private getCachedUsers(userIds: number[]): { cached: UserMention[], missing: number[] } {
    const cached: UserMention[] = [];
    const missing: number[] = [];

    userIds.forEach(id => {
      const cachedUser = this.userCache.get(id);
      if (cachedUser) {
        cached.push(cachedUser);
      } else {
        missing.push(id);
      }
    });

    return { cached, missing };
  }

  /**
   * Create a new comment on a task or subtask
   */
  async createComment(request: CreateCommentRequest): Promise<CreateCommentResponse> {
    return this.authenticatedClient.post<CreateCommentResponse>(`${this.baseUrl}/tasks/comments`, request);
  }

  /**
   * Get comments for a specific task with optional filtering
   */
  async getTaskComments(taskId: number, filters?: CommentFilters): Promise<CommentResponse[]> {
    const params = new URLSearchParams();
    if (filters?.authorId) params.append('authorId', filters.authorId.toString());
    if (filters?.resolved !== undefined) params.append('resolved', filters.resolved.toString());
    if (filters?.filter) params.append('filter', filters.filter);

    const queryString = params.toString();
    const url = `${this.baseUrl}/tasks/${taskId}/comments${queryString ? `?${queryString}` : ''}`;

    return this.authenticatedClient.get<CommentResponse[]>(url);
  }

  /**
   * Get comments for a specific subtask with optional filtering
   */
  async getSubtaskComments(subtaskId: number, filters?: CommentFilters): Promise<CommentResponse[]> {
    const params = new URLSearchParams();
    if (filters?.authorId) params.append('authorId', filters.authorId.toString());
    if (filters?.resolved !== undefined) params.append('resolved', filters.resolved.toString());
    if (filters?.filter) params.append('filter', filters.filter);

    const queryString = params.toString();
    const url = `${this.baseUrl}/subtasks/${subtaskId}/comments${queryString ? `?${queryString}` : ''}`;

    return this.authenticatedClient.get<CommentResponse[]>(url);
  }

  /**
   * Get a specific comment by ID
   */
  async getCommentById(commentId: number): Promise<CommentResponse> {
    return this.authenticatedClient.get<CommentResponse>(`${this.baseUrl}/tasks/comments/${commentId}`);
  }

  /**
   * Update an existing comment
   */
  async updateComment(request: UpdateCommentRequest): Promise<CommentResponse> {
    return this.authenticatedClient.put<CommentResponse>(`${this.baseUrl}/tasks/comments`, request);
  }

  /**
   * Delete a comment
   */
  async deleteComment(commentId: number): Promise<void> {
    return this.authenticatedClient.delete(`${this.baseUrl}/tasks/comments/${commentId}`);
  }

  /**
   * Get user mentions (comments where current user is mentioned)
   */
  async getUserMentions(): Promise<CommentResponse[]> {
    return this.authenticatedClient.get<CommentResponse[]>(`${this.baseUrl}/tasks/comments/mentions`);
  }

  /**
   * Get project members for mention suggestions
   */
  async getProjectMembers(projectId: number): Promise<UserMention[]> {
    // Check cache first
    const cached = this.projectMembersCache.get(projectId);
    if (cached && this.isCacheValid(cached.timestamp)) {
      return cached.data;
    }

    try {
      const users = await this.authenticatedClient.get<any[]>(`${this.baseUrl}/user/project/${projectId}/members`);
      const userMentions = users.map(user => ({
        id: user.id,
        username: user.username,
        email: user.email,
        displayName: user.username // Use username as display name for now
      }));

      // Cache the results
      this.projectMembersCache.set(projectId, {
        data: userMentions,
        timestamp: Date.now()
      });

      // Also cache individual users
      this.cacheUsers(userMentions);

      return userMentions;
    } catch (error) {
      // If API fails but we have expired cache, return it
      if (cached) {
        console.warn('API failed, returning expired cache for project members');
        return cached.data;
      }
      throw error;
    }
  }

  /**
   * Resolve user IDs to user details for display
   */
  async resolveUsersByIds(userIds: number[]): Promise<UserMention[]> {
    if (!userIds || userIds.length === 0) return [];

    // Check cache for existing users
    const { cached, missing } = this.getCachedUsers(userIds);

    // If all users are cached, return them
    if (missing.length === 0) {
      return cached;
    }

    try {
      // Fetch missing users from API
      const users = await this.authenticatedClient.post<any[]>(`${this.baseUrl}/user/batch`, missing);
      const newUserMentions = users.map(user => ({
        id: user.id,
        username: user.username,
        email: user.email,
        displayName: user.username
      }));

      // Cache the new users
      this.cacheUsers(newUserMentions);

      // Combine cached and new users, maintaining original order
      const result: UserMention[] = [];
      userIds.forEach(id => {
        const cachedUser = this.userCache.get(id);
        if (cachedUser) {
          result.push(cachedUser);
        }
      });

      return result;
    } catch (error) {
      // If API fails, return whatever we have in cache
      console.warn('Failed to resolve some users, returning cached users only');
      return cached;
    }
  }

  /**
   * Search for users to mention (for autocomplete)
   * Filters project members by query string
   */
  async searchUsersForMention(query: string, projectId?: number): Promise<UserMention[]> {
    if (!projectId) return [];

    const projectMembers = await this.getProjectMembers(projectId);

    if (!query.trim()) return projectMembers;

    const lowerQuery = query.toLowerCase();
    return projectMembers.filter(user =>
      user.username.toLowerCase().includes(lowerQuery) ||
      user.email.toLowerCase().includes(lowerQuery) ||
      (user.displayName && user.displayName.toLowerCase().includes(lowerQuery))
    );
  }

  /**
   * Mark a comment thread as resolved
   */
  async markThreadResolved(commentId: number, resolved: boolean): Promise<void> {
    return this.authenticatedClient.patch(`${this.baseUrl}/tasks/comments/${commentId}/resolve`, { resolved });
  }

  /**
   * Get comment activity for a user (notifications)
   */
  async getCommentActivity(): Promise<CommentResponse[]> {
    return this.authenticatedClient.get<CommentResponse[]>(`${this.baseUrl}/tasks/comments/activity`);
  }
}

export const commentService = new CommentService();