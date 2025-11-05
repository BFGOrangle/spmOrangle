/**
 * Calendar controls component
 * Provides view switching, date navigation, and project filtering
 */

import React from 'react';
import { format, addDays, addWeeks, addMonths, subDays, subWeeks, subMonths } from 'date-fns';
import { CalendarControlsProps, CalendarView } from '../../types/calendar';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../ui/select';
import { ChevronLeft, ChevronRight, Calendar, LayoutGrid, BarChart3, Clock, Search } from 'lucide-react';

export const CalendarControls: React.FC<CalendarControlsProps> = ({
  currentView,
  currentDate,
  projects,
  selectedProjectId,
  selectedTaskType = 'Project Tasks',
  searchKeyword = '',
  searchResultsCount,
  onViewChange,
  onDateChange,
  onProjectFilter,
  onTaskTypeChange,
  onSearchChange,
  onToday,
}) => {
  const viewIcons = {
    day: <Calendar className="w-4 h-4" />,
    week: <LayoutGrid className="w-4 h-4" />,
    month: <BarChart3 className="w-4 h-4" />,
    timeline: <Clock className="w-4 h-4" />,
  };

  const viewLabels = {
    day: 'Day',
    week: 'Week',
    month: 'Month',
    timeline: 'Timeline',
  };

  const navigateDate = (direction: 'prev' | 'next') => {
    let newDate: Date;
    
    switch (currentView) {
      case 'day':
        newDate = direction === 'next' 
          ? addDays(currentDate, 1) 
          : subDays(currentDate, 1);
        break;
      case 'week':
        newDate = direction === 'next' 
          ? addWeeks(currentDate, 1) 
          : subWeeks(currentDate, 1);
        break;
      case 'month':
      case 'timeline':
        newDate = direction === 'next' 
          ? addMonths(currentDate, 1) 
          : subMonths(currentDate, 1);
        break;
      default:
        newDate = currentDate;
    }
    
    onDateChange(newDate);
  };

  const formatDateForView = () => {
    switch (currentView) {
      case 'day':
        return format(currentDate, 'EEEE, MMMM d, yyyy');
      case 'week':
        return `Week of ${format(currentDate, 'MMM d, yyyy')}`;
      case 'month':
        return format(currentDate, 'MMMM yyyy');
      case 'timeline':
        return format(currentDate, 'MMMM yyyy');
      default:
        return format(currentDate, 'MMMM d, yyyy');
    }
  };

  return (
    <div className="border-b border-border bg-background p-2 sm:p-4">
      
        <div className="flex flex-col gap-4">
        {/* Left section: Date navigation */}
        <div className="flex items-center space-x-2 sm:space-x-4">
            <div className="flex items-center space-x-2">
            <Button
                variant="outline"
                size="sm"
                onClick={() => navigateDate('prev')}
                className="p-2"
            >
                <ChevronLeft className="w-4 h-4" />
            </Button>
            <div className="min-w-0 flex-1 sm:min-w-48">
                <h2 className="text-base sm:text-lg font-semibold text-foreground truncate">
                {formatDateForView()}
                </h2>
            </div>
            <Button
                variant="outline"
                size="sm"
                onClick={() => navigateDate('next')}
                className="p-2"
            >
                <ChevronRight className="w-4 h-4" />
            </Button>
            </div>
            <Button
            variant="outline"
            size="sm"
            onClick={onToday}
            className="text-xs sm:text-sm"
            >
            Today
            </Button>
        </div>

        {/* Right section: View controls and filters */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center space-y-2 sm:space-y-0 sm:space-x-4">
            {/* Task View filter */}
            <div className="min-w-0 flex-shrink-0">
            <Select
                value={selectedTaskType}
                onValueChange={(value) => {
                if (onTaskTypeChange) {
                    onTaskTypeChange(value);
                }
                }}
            >
                <SelectTrigger className="w-full sm:w-40">
                <SelectValue placeholder="View Type" />
                </SelectTrigger>
                <SelectContent>
                <SelectItem value="My Projects">My Project Tasks</SelectItem>
                <SelectItem value="Personal Tasks">My Personal Tasks</SelectItem>
                <SelectItem value="Project Tasks">All Project Tasks</SelectItem>
                </SelectContent>
            </Select>
            </div>

            {/* Project filter - only show when in Project Tasks mode */}
            {selectedTaskType === "Project Tasks" && (
            <div className="min-w-0 flex-shrink-0">
                <Select
                value={selectedProjectId?.toString() || 'all'}
                onValueChange={(value) => {
                    if (value === 'all') {
                    onProjectFilter(undefined);
                    } else {
                    onProjectFilter(parseInt(value));
                    }
                }}
                >
                <SelectTrigger className="w-full sm:w-40">
                    <SelectValue placeholder="All Projects" />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value="all">All Projects</SelectItem>
                    {projects.filter(project => project.id !== 0).map((project) => (
                    <SelectItem key={project.id} value={project.id.toString()}>
                        {project.name}
                    </SelectItem>
                    ))}
                </SelectContent>
                </Select>
            </div>
            )}

            {/* Search input */}
            <div className="min-w-0 flex-shrink-0 relative">
            <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                type="text"
                placeholder="Search tasks..."
                value={searchKeyword}
                onChange={(e) => onSearchChange?.(e.target.value)}
                className="w-full sm:w-48 pl-10 pr-4"
                />
            </div>
            </div>

            {/* View switcher */}
            <div className="flex items-center justify-center sm:justify-start space-x-1 bg-muted rounded-lg p-1">
            {Object.entries(viewLabels).map(([view, label]) => (
                <Button
                key={view}
                variant={currentView === view ? "outline" : "ghost"}
                size="sm"
                onClick={() => onViewChange(view as CalendarView)}
                className={`px-2 sm:px-3 py-1 text-xs font-medium ${
                    currentView === view
                    ? 'bg-background shadow-sm'
                    : 'text-muted-foreground hover:text-foreground'
                }`}
                >
                <span className="hidden sm:inline-flex items-center space-x-1">
                    {viewIcons[view as CalendarView]}
                    <span>{label}</span>
                </span>
                <span className="sm:hidden flex items-center space-x-1">
                    {viewIcons[view as CalendarView]}
                    <span className="text-xs">{label}</span>
                </span>
                </Button>
            ))}
            </div>
        </div>
        </div>

      {/* Additional info row */}
      <div className="mt-2 sm:mt-3 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-2 text-sm text-muted-foreground">
        <div className="text-xs sm:text-sm">
          {searchKeyword ? (
            <span>
              Searching for: "{searchKeyword}" 
              {searchResultsCount !== undefined && (
                <span className="ml-2">({searchResultsCount} results)</span>
              )}
            </span>
          ) : selectedTaskType === "Personal Tasks" ? (
            <span>Viewing: Personal Tasks</span>
          ) : selectedTaskType === "My Projects" ? (
            <span>Viewing: My Project Tasks</span>
          ) : selectedProjectId ? (
            <span>
              Filtered by: {projects.find(p => p.id === selectedProjectId)?.name}
            </span>
          ) : (
            <span>Viewing: All Project Tasks</span>
          )}
        </div>
        
        {/* Legend for Project Tasks view */}
        {selectedTaskType === "Project Tasks" && (
          <div className="flex flex-wrap items-center gap-3 text-xs">
            <span className="font-medium">Legend:</span>
            <div className="flex items-center gap-1.5">
              <div className="w-3 h-3 rounded-sm bg-green-500"></div>
              <span>Collaborator Task</span>
            </div>
            <div className="flex items-center gap-1.5">
              <div className="w-3 h-3 rounded-sm bg-purple-500"></div>
              <span>Related Task</span>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
