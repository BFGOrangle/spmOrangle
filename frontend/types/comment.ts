export interface CommentResponse {
  id: number;
  taskId?: number;
  subtaskId?: number;
  projectId: number;
  content: string;
  mentionedUserIds: number[];
  isEdited: boolean;
  isDeleted: boolean;
  authorId: number;
  authorUsername: string;
  createdAt: string;
  updatedAt?: string;
  parentCommentId?: number;
  replies: CommentResponse[];
  replyCount: number;
  canEdit: boolean;
  canDelete: boolean;
  canReply: boolean;
  canModerate: boolean;
}

export interface CreateCommentRequest {
  taskId?: number;
  subtaskId?: number;
  content: string;
  mentionedUserIds?: number[];
  parentCommentId?: number;
}

export interface CreateCommentResponse {
  id: number;
  taskId?: number;
  subtaskId?: number;
  content: string;
  authorId: number;
  createdAt: string;
}

export interface UpdateCommentRequest {
  commentId: number;
  content: string;
  mentionedUserIds?: number[];
}

export interface CommentFilters {
  authorId?: number;
  resolved?: boolean;
  filter?: 'ALL' | 'UNRESOLVED' | 'BY_COMMENTER';
}

export interface UserMention {
  id: number;
  username: string;
  email: string;
  displayName?: string;
}

export interface CommentPermissions {
  canRead: boolean;
  canWrite: boolean;
  canEdit: boolean;
  canDelete: boolean;
  canModerate: boolean;
}