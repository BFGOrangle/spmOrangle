"use client";

import { useState, useRef, useEffect } from "react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Send, X, AtSign } from "lucide-react";
import { UserMention } from "@/types/comment";
import { commentService } from "@/services/comment-service";

interface CommentEditorProps {
  initialContent?: string;
  onSubmit: (content: string, mentions: number[]) => Promise<void>;
  onCancel: () => void;
  isLoading?: boolean;
  placeholder?: string;
  compact?: boolean;
  projectId?: number;
}

export function CommentEditor({
  initialContent = "",
  onSubmit,
  onCancel,
  isLoading = false,
  placeholder = "Write a comment...",
  compact = false,
  projectId,
}: CommentEditorProps) {
  const [content, setContent] = useState(initialContent);
  const [mentions, setMentions] = useState<UserMention[]>([]);
  const [mentionSearch, setMentionSearch] = useState("");
  const [showMentionPopover, setShowMentionPopover] = useState(false);
  const [cursorPosition, setCursorPosition] = useState(0);
  const [availableUsers, setAvailableUsers] = useState<UserMention[]>([]);

  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    // Fetch project members when projectId changes
    const fetchProjectMembers = async () => {
      if (projectId) {
        try {
          const members = await commentService.getProjectMembers(projectId);
          setAvailableUsers(members);
        } catch (error) {
          console.error("Failed to fetch project members:", error);
          setAvailableUsers([]);
        }
      } else {
        setAvailableUsers([]);
      }
    };

    fetchProjectMembers();
  }, [projectId]);

  const handleContentChange = (value: string) => {
    setContent(value);

    // Check if user is typing a mention
    const textarea = textareaRef.current;
    if (textarea) {
      const position = textarea.selectionStart;
      const textBeforeCursor = value.substring(0, position);
      const atIndex = textBeforeCursor.lastIndexOf("@");

      if (atIndex !== -1) {
        const textAfterAt = textBeforeCursor.substring(atIndex + 1);
        if (!textAfterAt.includes(" ") && textAfterAt.length <= 20) {
          setMentionSearch(textAfterAt);
          setShowMentionPopover(true);
          setCursorPosition(position);
        } else {
          setShowMentionPopover(false);
        }
      } else {
        setShowMentionPopover(false);
      }
    }
  };

  const handleMentionSelect = (user: UserMention) => {
    const textarea = textareaRef.current;
    if (textarea) {
      const beforeAt = content.substring(0, content.lastIndexOf("@"));
      const afterMention = content.substring(cursorPosition);
      const mentionText = `@${user.username} `;

      const newContent = beforeAt + mentionText + afterMention;
      setContent(newContent);

      // Add to mentions if not already included
      if (!mentions.some(m => m.id === user.id)) {
        setMentions([...mentions, user]);
      }

      setShowMentionPopover(false);
      setMentionSearch("");

      // Focus back to textarea
      setTimeout(() => {
        textarea.focus();
        const newPosition = beforeAt.length + mentionText.length;
        textarea.setSelectionRange(newPosition, newPosition);
      }, 0);
    }
  };

  const removeMention = (userId: number) => {
    setMentions(mentions.filter(m => m.id !== userId));
    // Also remove from content
    const userToRemove = mentions.find(m => m.id === userId);
    if (userToRemove) {
      const newContent = content.replace(new RegExp(`@${userToRemove.username}`, 'g'), '');
      setContent(newContent);
    }
  };

  const handleSubmit = async () => {
    if (!content.trim()) return;

    try {
      await onSubmit(content.trim(), mentions.map(m => m.id));
      setContent("");
      setMentions([]);
    } catch (error) {
      console.error("Failed to submit comment:", error);
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && (e.ctrlKey || e.metaKey)) {
      e.preventDefault();
      handleSubmit();
    }
  };

  const filteredUsers = availableUsers.filter(user =>
    user.username.toLowerCase().includes(mentionSearch.toLowerCase()) ||
    (user.displayName && user.displayName.toLowerCase().includes(mentionSearch.toLowerCase()))
  );

  return (
    <div className={`border rounded-lg bg-background ${compact ? "p-3" : "p-4"} ${compact ? "border-dashed" : ""}`}>
        {/* Mentions Display */}
        {mentions.length > 0 && (
          <div className="flex flex-wrap gap-1 mb-2">
            {mentions.map((mention) => (
              <Badge
                key={mention.id}
                variant="secondary"
                className="text-xs flex items-center gap-1"
              >
                <AtSign className="h-3 w-3" />
                {mention.displayName || mention.username}
                <button
                  onClick={() => removeMention(mention.id)}
                  className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
                >
                  <X className="h-2 w-2" />
                </button>
              </Badge>
            ))}
          </div>
        )}

        {/* Text Editor */}
        <div className="relative">
          <textarea
            ref={textareaRef}
            value={content}
            onChange={(e) => handleContentChange(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={placeholder}
            className={`w-full resize-y border rounded-md border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50 ${
              compact ? "min-h-[60px] max-h-[120px]" : "min-h-[100px] max-h-[200px]"
            }`}
            disabled={isLoading}
          />

          {/* Mention Dropdown */}
          {showMentionPopover && (
            <div className="absolute z-50 w-full sm:w-72 mt-1 bg-background border rounded-md shadow-lg max-h-56 overflow-hidden">
              <div className="p-2 border-b">
                <Input
                  placeholder="Search users..."
                  value={mentionSearch}
                  onChange={(e) => setMentionSearch(e.target.value)}
                  className="h-8 text-sm"
                />
              </div>
              <div className="max-h-40 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-transparent">
                {filteredUsers.length === 0 ? (
                  <div className="p-3 text-sm text-muted-foreground text-center">No users found.</div>
                ) : (
                  filteredUsers.map((user) => (
                    <button
                      key={user.id}
                      onClick={() => handleMentionSelect(user)}
                      className="w-full p-3 text-left hover:bg-accent hover:text-accent-foreground flex items-center gap-3 transition-colors duration-150"
                    >
                      <AtSign className="h-4 w-4 text-muted-foreground" />
                      <div className="flex-1 min-w-0">
                        <div className="font-medium text-sm truncate">{user.displayName || user.username}</div>
                        <div className="text-xs text-muted-foreground truncate">{user.email}</div>
                      </div>
                    </button>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

        {/* Actions */}
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 sm:gap-0 mt-3">
          <div className="text-xs text-muted-foreground order-2 sm:order-1">
            <span className="hidden sm:inline">Press Ctrl+Enter to submit</span>
            <span className="sm:hidden">Tap to submit</span>
          </div>
          <div className="flex items-center gap-2 order-1 sm:order-2">
            <Button
              variant="ghost"
              size="sm"
              onClick={onCancel}
              disabled={isLoading}
              className="flex-1 sm:flex-none"
            >
              Cancel
            </Button>
            <Button
              size="sm"
              onClick={handleSubmit}
              disabled={!content.trim() || isLoading}
              className="flex-1 sm:flex-none"
            >
              {isLoading ? (
                "Posting..."
              ) : (
                <>
                  <Send className="mr-1 h-3 w-3" />
                  {compact ? "Reply" : "Comment"}
                </>
              )}
            </Button>
          </div>
        </div>
    </div>
  );
}