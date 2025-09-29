"use client";

import { useState, useEffect } from "react";
import { formatDistanceToNow } from "date-fns";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Badge } from "@/components/ui/badge";
import {
  MessageCircle,
  MoreHorizontal,
  Edit3,
  Trash2,
  Reply,
  Check,
  X,
  AtSign,
} from "lucide-react";
import { CommentResponse, UserMention } from "@/types/comment";
import { CommentEditor } from "./comment-editor";
import { commentService } from "@/services/comment-service";

interface CommentItemProps {
  comment: CommentResponse;
  onReply: (parentId: number, content: string, mentions: number[]) => Promise<void>;
  onEdit: (commentId: number, content: string, mentions: number[]) => Promise<void>;
  onDelete: (commentId: number) => Promise<void>;
  onMarkResolved?: (commentId: number, resolved: boolean) => Promise<void>;
  depth?: number;
}

export function CommentItem({
  comment,
  onReply,
  onEdit,
  onDelete,
  onMarkResolved,
  depth = 0,
}: CommentItemProps) {
  const [isReplying, setIsReplying] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [mentionedUsers, setMentionedUsers] = useState<UserMention[]>([]);

  // Resolve mentioned user IDs to user details
  useEffect(() => {
    const resolveMentions = async () => {
      if (comment.mentionedUserIds && comment.mentionedUserIds.length > 0) {
        try {
          const users = await commentService.resolveUsersByIds(comment.mentionedUserIds);
          setMentionedUsers(users);
        } catch (error) {
          console.error("Failed to resolve mentioned users:", error);
          setMentionedUsers([]);
        }
      } else {
        setMentionedUsers([]);
      }
    };

    resolveMentions();
  }, [comment.mentionedUserIds]);

  const handleReply = async (content: string, mentions: number[]) => {
    setIsLoading(true);
    try {
      await onReply(comment.id, content, mentions);
      setIsReplying(false);
    } catch (error) {
      console.error("Failed to reply to comment:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = async (content: string, mentions: number[]) => {
    setIsLoading(true);
    try {
      await onEdit(comment.id, content, mentions);
      setIsEditing(false);
    } catch (error) {
      console.error("Failed to edit comment:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDelete = async () => {
    if (confirm("Are you sure you want to delete this comment?")) {
      setIsLoading(true);
      try {
        await onDelete(comment.id);
      } catch (error) {
        console.error("Failed to delete comment:", error);
      } finally {
        setIsLoading(false);
      }
    }
  };

  const handleMarkResolved = async (resolved: boolean) => {
    if (onMarkResolved) {
      setIsLoading(true);
      try {
        await onMarkResolved(comment.id, resolved);
      } catch (error) {
        console.error("Failed to update comment status:", error);
      } finally {
        setIsLoading(false);
      }
    }
  };

  // Calculate indentation for threaded display
  const marginLeft = depth * 24;

  if (comment.isDeleted) {
    return (
      <div className="relative">
        {depth > 0 && (
          <div className="absolute left-0 top-0 bottom-0 flex">
            {Array.from({ length: depth }).map((_, i) => (
              <div
                key={i}
                className="w-6 flex justify-center"
              >
                <div className="w-px bg-gray-200 dark:bg-gray-700" />
              </div>
            ))}
          </div>
        )}
        <div
          className="text-sm text-muted-foreground italic py-3 pl-2"
          style={{ marginLeft: `${depth * 24}px` }}
        >
          [Comment deleted]
        </div>
        {comment.replies.length > 0 && (
          <div>
            {comment.replies.map((reply) => (
              <CommentItem
                key={reply.id}
                comment={reply}
                onReply={onReply}
                onEdit={onEdit}
                onDelete={onDelete}
                onMarkResolved={onMarkResolved}
                depth={depth + 1}
              />
            ))}
          </div>
        )}
      </div>
    );
  }

  return (
    <div className="relative">
      {/* Threading Lines */}
      {depth > 0 && (
        <div className="absolute left-0 top-0 bottom-0 flex">
          {Array.from({ length: depth }).map((_, i) => (
            <div
              key={i}
              className="w-6 flex justify-center"
            >
              <div className="w-px bg-gray-200 dark:bg-gray-700" />
            </div>
          ))}
        </div>
      )}

      <div
        className="py-2 pl-2 hover:bg-gray-50 dark:hover:bg-gray-900/50 transition-colors duration-150"
        style={{ marginLeft: `${depth * 24}px` }}
      >
        {/* Comment Header */}
        <div className="flex items-start justify-between mb-1">
          <div className="flex items-center gap-2 flex-wrap">
            <span className="font-medium text-sm text-blue-600 dark:text-blue-400">{comment.authorUsername}</span>
            <span className="text-xs text-muted-foreground">
              {formatDistanceToNow(new Date(comment.createdAt), { addSuffix: true })}
            </span>
            {comment.isEdited && (
              <Badge variant="secondary" className="text-xs px-1 py-0">
                edited
              </Badge>
            )}
            {depth === 0 && comment.replyCount > 0 && (
              <span className="text-xs text-muted-foreground">
                {comment.replyCount} {comment.replyCount === 1 ? "reply" : "replies"}
              </span>
            )}
          </div>

          {/* Actions Menu */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="sm" className="h-6 w-6 p-0 opacity-60 hover:opacity-100">
                <MoreHorizontal className="h-3 w-3" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              {comment.canReply && (
                <DropdownMenuItem onClick={() => setIsReplying(true)}>
                  <Reply className="mr-2 h-4 w-4" />
                  Reply
                </DropdownMenuItem>
              )}
              {comment.canEdit && (
                <DropdownMenuItem onClick={() => setIsEditing(true)}>
                  <Edit3 className="mr-2 h-4 w-4" />
                  Edit
                </DropdownMenuItem>
              )}
              {comment.canDelete && (
                <DropdownMenuItem onClick={handleDelete}>
                  <Trash2 className="mr-2 h-4 w-4" />
                  Delete
                </DropdownMenuItem>
              )}
              {comment.canModerate && onMarkResolved && (
                <>
                  <DropdownMenuItem onClick={() => handleMarkResolved(true)}>
                    <Check className="mr-2 h-4 w-4" />
                    Mark Resolved
                  </DropdownMenuItem>
                  <DropdownMenuItem onClick={() => handleMarkResolved(false)}>
                    <X className="mr-2 h-4 w-4" />
                    Mark Unresolved
                  </DropdownMenuItem>
                </>
              )}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        {/* Comment Content */}
        {isEditing ? (
          <div className="mt-2">
            <CommentEditor
              initialContent={comment.content}
              onSubmit={handleEdit}
              onCancel={() => setIsEditing(false)}
              isLoading={isLoading}
              placeholder="Edit your comment..."
              projectId={comment.projectId}
              compact
            />
          </div>
        ) : (
          <div className="mt-1">
            <p className="text-sm leading-relaxed whitespace-pre-wrap break-words overflow-wrap-anywhere text-gray-800 dark:text-gray-200">
              {comment.content}
            </p>
          </div>
        )}

        {/* Mentions */}
        {mentionedUsers.length > 0 && (
          <div className="mt-2">
            <div className="flex flex-wrap gap-1">
              {mentionedUsers.map((user) => (
                <Badge key={user.id} variant="secondary" className="text-xs flex items-center gap-1 px-1 py-0">
                  <AtSign className="h-3 w-3" />
                  {user.displayName || user.username}
                </Badge>
              ))}
            </div>
          </div>
        )}

        {/* Action Buttons */}
        {!isEditing && (
          <div className="mt-2 flex items-center gap-3">
            {comment.canReply && !isReplying && (
              <button
                onClick={() => setIsReplying(true)}
                className="text-xs text-muted-foreground hover:text-blue-600 dark:hover:text-blue-400 font-medium transition-colors duration-150"
              >
                Reply
              </button>
            )}
            <button className="text-xs text-muted-foreground hover:text-gray-600 dark:hover:text-gray-400 font-medium transition-colors duration-150">
              ↑
            </button>
            <button className="text-xs text-muted-foreground hover:text-gray-600 dark:hover:text-gray-400 font-medium transition-colors duration-150">
              ↓
            </button>
          </div>
        )}
      </div>

      {/* Reply Editor */}
      {isReplying && (
        <div className="mt-3" style={{ marginLeft: `${depth * 24 + 24}px` }}>
          <CommentEditor
            onSubmit={handleReply}
            onCancel={() => setIsReplying(false)}
            isLoading={isLoading}
            placeholder="Write a reply..."
            compact
            projectId={comment.projectId}
          />
        </div>
      )}

      {/* Nested Replies */}
      {comment.replies && comment.replies.length > 0 && (
        <div>
          {comment.replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              onReply={onReply}
              onEdit={onEdit}
              onDelete={onDelete}
              onMarkResolved={onMarkResolved}
              depth={depth + 1}
            />
          ))}
        </div>
      )}
    </div>
  );
}