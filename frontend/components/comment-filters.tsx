"use client";

import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { X, MessageCircle, User, CheckCircle } from "lucide-react";
import { CommentFilters } from "@/types/comment";

interface CommentFiltersProps {
  filters: CommentFilters;
  onFiltersChange: (filters: CommentFilters) => void;
  availableUsers?: Array<{ id: number; username: string; displayName?: string }>;
  totalComments: number;
  filteredComments: number;
  currentUserId?: number | null;
}

export function CommentFiltersComponent({
  filters,
  onFiltersChange,
  availableUsers = [],
  totalComments,
  filteredComments,
  currentUserId,
}: CommentFiltersProps) {

  const updateFilter = (key: keyof CommentFilters, value: any) => {
    const newFilters = { ...filters, [key]: value };
    onFiltersChange(newFilters);
  };

  const clearFilter = (key: keyof CommentFilters) => {
    const newFilters = { ...filters };
    delete newFilters[key];
    onFiltersChange(newFilters);
  };

  const clearAllFilters = () => {
    onFiltersChange({});
  };

  const hasActiveFilters = Object.keys(filters).length > 0;
  const activeFilterCount = Object.keys(filters).length;

  const getAuthorName = (authorId: number) => {
    const user = availableUsers.find(u => u.id === authorId);
    return user?.displayName || user?.username || `User ${authorId}`;
  };

  return (
    <div className="space-y-3">
      {/* Filter Controls */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          {/* Quick Filter Buttons */}
          <Button
            variant={filters.filter === "UNRESOLVED" ? "default" : "outline"}
            size="sm"
            onClick={() => updateFilter("filter", filters.filter === "UNRESOLVED" ? undefined : "UNRESOLVED")}
          >
            <MessageCircle className="mr-2 h-4 w-4" />
            Unresolved
          </Button>

          <Button
            variant={filters.filter === "BY_COMMENTER" ? "default" : "outline"}
            size="sm"
            onClick={() => updateFilter("filter", filters.filter === "BY_COMMENTER" ? undefined : "BY_COMMENTER")}
            disabled={!currentUserId}
            title={!currentUserId ? "User authentication required" : "Show only your comments and threads you've participated in"}
          >
            <User className="mr-2 h-4 w-4" />
            My Comments
          </Button>

          {/* Clear All Filters Button */}
          {hasActiveFilters && (
            <Button
              variant="outline"
              size="sm"
              onClick={clearAllFilters}
            >
              <X className="mr-2 h-4 w-4" />
              Clear Filters
            </Button>
          )}
        </div>

        {/* Results Count */}
        <div className="text-sm text-muted-foreground">
          {hasActiveFilters ? (
            <>Showing {filteredComments} of {totalComments} comments</>
          ) : (
            <>{totalComments} comment{totalComments !== 1 ? "s" : ""}</>
          )}
        </div>
      </div>

      {/* Active Filters Display */}
      {hasActiveFilters && (
        <div className="flex flex-wrap gap-2">
          {filters.filter && (
            <Badge variant="secondary" className="flex items-center gap-1">
              {filters.filter === "UNRESOLVED" && <MessageCircle className="h-3 w-3" />}
              {filters.filter === "BY_COMMENTER" && <User className="h-3 w-3" />}
              {filters.filter === "UNRESOLVED" ? "Unresolved" :
               filters.filter === "BY_COMMENTER" ? "My Comments" : filters.filter}
              <button
                onClick={() => clearFilter("filter")}
                className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
              >
                <X className="h-2 w-2" />
              </button>
            </Badge>
          )}

          {filters.authorId && (
            <Badge variant="secondary" className="flex items-center gap-1">
              <User className="h-3 w-3" />
              {getAuthorName(filters.authorId)}
              <button
                onClick={() => clearFilter("authorId")}
                className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
              >
                <X className="h-2 w-2" />
              </button>
            </Badge>
          )}

          {filters.resolved !== undefined && (
            <Badge variant="secondary" className="flex items-center gap-1">
              <CheckCircle className="h-3 w-3" />
              {filters.resolved ? "Resolved" : "Unresolved"}
              <button
                onClick={() => clearFilter("resolved")}
                className="ml-1 hover:bg-destructive/20 rounded-full p-0.5"
              >
                <X className="h-2 w-2" />
              </button>
            </Badge>
          )}
        </div>
      )}
    </div>
  );
}