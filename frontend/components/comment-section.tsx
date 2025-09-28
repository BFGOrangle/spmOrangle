"use client";

import { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { MessageCircle, Plus } from "lucide-react";
import { CommentItem } from "./comment-item";
import { CommentEditor } from "./comment-editor";
import { CommentFiltersComponent } from "./comment-filters";
import { commentService } from "@/services/comment-service";
import {
  CommentResponse,
  CreateCommentRequest,
  CommentFilters,
  UserMention
} from "@/types/comment";

interface CommentSectionProps {
  taskId?: number;
  subtaskId?: number;
  projectId: number;
  title?: string;
  showTitle?: boolean;
  compact?: boolean;
}

export function CommentSection({
  taskId,
  subtaskId,
  projectId,
  title = "Comments",
  showTitle = true,
  compact = false,
}: CommentSectionProps) {
  const [comments, setComments] = useState<CommentResponse[]>([]);
  const [filteredComments, setFilteredComments] = useState<CommentResponse[]>([]);
  const [filters, setFilters] = useState<CommentFilters>({});
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showNewCommentEditor, setShowNewCommentEditor] = useState(false);
  const [availableUsers, setAvailableUsers] = useState<UserMention[]>([]);
  const [currentUserId, setCurrentUserId] = useState<number | null>(null);

  // Get current user ID (placeholder - should come from auth context)
  useEffect(() => {
    // TODO: Replace with actual user service call
    setCurrentUserId(1); // Placeholder current user ID
  }, []);

  // Load comments on mount and when task/subtask changes
  useEffect(() => {
    loadComments();
  }, [taskId, subtaskId]);

  // Apply filters when comments, filters, or current user change
  useEffect(() => {
    applyFilters();
  }, [comments, filters, currentUserId]);

  const loadComments = async (skipFilters = false) => {
    setIsLoading(true);
    try {
      let commentsData: CommentResponse[];
      const filterToUse = skipFilters ? {} : filters;

      if (taskId) {
        commentsData = await commentService.getTaskComments(taskId, filterToUse);
      } else if (subtaskId) {
        commentsData = await commentService.getSubtaskComments(subtaskId, filterToUse);
      } else {
        commentsData = [];
      }

      // Check if we need to build the tree structure
      const hasNestedReplies = commentsData.some(comment => comment.replies && comment.replies.length > 0);

      // If no nested structure, try to build it
      if (!hasNestedReplies) {
        const treeComments = buildCommentTree(commentsData);
        setComments(treeComments);
      } else {
        setComments(commentsData);
      }

      // Extract unique users for filtering (including reply authors)
      const users = new Set<UserMention>();
      const extractUsers = (comments: CommentResponse[]) => {
        comments.forEach(comment => {
          users.add({
            id: comment.authorId,
            username: comment.authorUsername,
            email: `${comment.authorUsername}@example.com`, // In real app, get from user service
            displayName: comment.authorUsername
          });
          // Also extract users from replies
          if (comment.replies && comment.replies.length > 0) {
            extractUsers(comment.replies);
          }
        });
      };

      extractUsers(commentsData);
      setAvailableUsers(Array.from(users));

    } catch (error) {
      console.error("Failed to load comments:", error);
      setComments([]);
    } finally {
      setIsLoading(false);
    }
  };

  const buildCommentTree = (flatComments: CommentResponse[]): CommentResponse[] => {
    // Create a map for quick lookup
    const commentMap = new Map<number, CommentResponse>();
    const rootComments: CommentResponse[] = [];

    // First, create a map of all comments and initialize empty replies arrays
    flatComments.forEach(comment => {
      commentMap.set(comment.id, { ...comment, replies: [] });
    });

    // Then, build the tree by assigning children to their parents
    flatComments.forEach(comment => {
      const commentCopy = commentMap.get(comment.id)!;

      if (comment.parentCommentId) {
        // This is a reply, add it to its parent's replies array
        const parent = commentMap.get(comment.parentCommentId);
        if (parent) {
          parent.replies.push(commentCopy);
        } else {
          // Parent not found, treat as root comment
          console.warn(`Parent comment ${comment.parentCommentId} not found for comment ${comment.id}`);
          rootComments.push(commentCopy);
        }
      } else {
        // This is a root comment
        rootComments.push(commentCopy);
      }
    });

    return rootComments;
  };

  const getFilteredEmptyStateMessage = () => {
    if (filters.filter === "BY_COMMENTER") {
      return "You haven't participated in any comment threads yet.";
    }
    if (filters.filter === "UNRESOLVED") {
      return "All comment threads have been resolved.";
    }
    if (filters.authorId) {
      const author = availableUsers.find(u => u.id === filters.authorId);
      const authorName = author?.displayName || author?.username || "Selected user";
      return `${authorName} hasn't commented on this item yet.`;
    }
    if (filters.resolved === true) {
      return "No resolved comment threads found.";
    }
    if (filters.resolved === false) {
      return "No unresolved comment threads found.";
    }
    return "Try adjusting your filters to see more comments.";
  };

  const applyFilters = () => {
    let filtered = [...comments];

    // Helper function to check if a comment thread has any unresolved issues
    const isThreadUnresolved = (comment: CommentResponse): boolean => {
      // A thread is considered unresolved if:
      // 1. It's a top-level comment with no replies (needs attention)
      // 2. It has replies but the last reply is not from the comment author (ongoing discussion)
      if (comment.replies.length === 0) {
        return true; // No replies = unresolved
      }

      const lastReply = comment.replies[comment.replies.length - 1];
      // If the last reply is not from the original comment author, it's still unresolved
      return lastReply.authorId !== comment.authorId;
    };

    // Filter by author
    if (filters.authorId) {
      filtered = filtered.filter(comment => {
        // Include comment if author matches OR any reply author matches
        if (comment.authorId === filters.authorId) return true;
        return comment.replies.some(reply => reply.authorId === filters.authorId);
      });
    }

    // Filter by resolution status
    if (filters.resolved !== undefined) {
      filtered = filtered.filter(comment => {
        const isResolved = !isThreadUnresolved(comment);
        return filters.resolved ? isResolved : !isResolved;
      });
    }

    // Filter by type
    if (filters.filter) {
      switch (filters.filter) {
        case "UNRESOLVED":
          filtered = filtered.filter(comment => isThreadUnresolved(comment));
          break;
        case "BY_COMMENTER":
          if (currentUserId) {
            filtered = filtered.filter(comment => {
              // Show comments authored by current user OR where current user replied
              if (comment.authorId === currentUserId) return true;
              return comment.replies.some(reply => reply.authorId === currentUserId);
            });
          }
          break;
      }
    }

    setFilteredComments(filtered);
  };

  const handleCreateComment = async (content: string, mentions: number[]) => {
    setIsSubmitting(true);
    try {
      const request: CreateCommentRequest = {
        taskId,
        subtaskId,
        content,
        mentionedUserIds: mentions,
      };

      await commentService.createComment(request);
      await loadComments(true); // Reload without filters to ensure new comment is visible
      setShowNewCommentEditor(false);
    } catch (error) {
      console.error("Failed to create comment:", error);
      throw error;
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleReply = async (parentId: number, content: string, mentions: number[]) => {
    const request: CreateCommentRequest = {
      taskId,
      subtaskId,
      content,
      mentionedUserIds: mentions,
      parentCommentId: parentId,
    };

    try {
      await commentService.createComment(request);

      // Add a short delay and retry to handle potential database consistency issues
      await new Promise(resolve => setTimeout(resolve, 500));
      await loadComments(true); // Reload without filters to ensure replies are visible
    } catch (error) {
      console.error('Failed to create reply:', error);
      throw error;
    }
  };

  const handleEdit = async (commentId: number, content: string, mentions: number[]) => {
    await commentService.updateComment({
      commentId,
      content,
      mentionedUserIds: mentions,
    });
    await loadComments(true); // Reload without filters to ensure edited comment is visible
  };

  const handleDelete = async (commentId: number) => {
    await commentService.deleteComment(commentId);
    await loadComments(true); // Reload without filters to update the list
  };

  const handleMarkResolved = async (commentId: number, resolved: boolean) => {
    await commentService.markThreadResolved(commentId, resolved);
    await loadComments(true); // Reload without filters to update status
  };

  if (isLoading) {
    return (
      <Card className={compact ? "border-dashed" : ""}>
        <CardContent className="p-6">
          <div className="flex items-center justify-center">
            <div className="text-muted-foreground">Loading comments...</div>
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className={compact ? "border-dashed" : ""}>
      {showTitle && (
        <CardHeader className="pb-3">
          <CardTitle className="flex items-center gap-2 text-lg">
            <MessageCircle className="h-5 w-5" />
            {title}
            <span className="text-sm font-normal text-muted-foreground">
              ({comments.length})
            </span>
          </CardTitle>
        </CardHeader>
      )}

      <CardContent className={showTitle ? "pt-0" : "pt-6"}>
        {/* Add New Comment Button */}
        {!showNewCommentEditor && (
          <div className="mb-4">
            <Button
              variant="outline"
              onClick={() => setShowNewCommentEditor(true)}
              className="w-full"
            >
              <Plus className="mr-2 h-4 w-4" />
              Add Comment
            </Button>
          </div>
        )}

        {/* New Comment Editor */}
        {showNewCommentEditor && (
          <div className="mb-6">
            <CommentEditor
              onSubmit={handleCreateComment}
              onCancel={() => setShowNewCommentEditor(false)}
              isLoading={isSubmitting}
              placeholder="Write a comment..."
              projectId={projectId}
            />
          </div>
        )}

        {/* Filters */}
        {comments.length > 0 && (
          <>
            <CommentFiltersComponent
              filters={filters}
              onFiltersChange={setFilters}
              availableUsers={availableUsers}
              totalComments={comments.length}
              filteredComments={filteredComments.length}
              currentUserId={currentUserId}
            />
            <Separator className="my-4" />
          </>
        )}

        {/* Comments List */}
        {filteredComments.length === 0 ? (
          <div className="text-center py-8">
            <MessageCircle className="mx-auto h-12 w-12 text-muted-foreground/50 mb-4" />
            <h3 className="text-lg font-medium text-muted-foreground mb-2">
              {comments.length === 0 ? "No comments yet" : "No comments match your filters"}
            </h3>
            <p className="text-sm text-muted-foreground mb-4">
              {comments.length === 0
                ? "Start the conversation by adding the first comment."
                : getFilteredEmptyStateMessage()
              }
            </p>
            {comments.length === 0 && !showNewCommentEditor && (
              <Button onClick={() => setShowNewCommentEditor(true)}>
                <Plus className="mr-2 h-4 w-4" />
                Add First Comment
              </Button>
            )}
          </div>
        ) : (
          <div className={`overflow-y-auto pr-1 sm:pr-2 scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-transparent hover:scrollbar-thumb-gray-400 border-l border-gray-100 dark:border-gray-800 ${compact ? 'max-h-[300px]' : 'max-h-[400px] sm:max-h-[500px] lg:max-h-[600px]'}`}>
            {filteredComments.map((comment) => (
              <CommentItem
                key={comment.id}
                comment={comment}
                onReply={handleReply}
                onEdit={handleEdit}
                onDelete={handleDelete}
                onMarkResolved={handleMarkResolved}
              />
            ))}
          </div>
        )}
      </CardContent>
    </Card>
  );
}