'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useRouter } from 'next/navigation';
import { startOfMonth, endOfMonth, addMonths, subMonths } from 'date-fns';

// Types
import { CalendarView, CalendarEvent, CalendarFilters } from '../../../types/calendar';
import { TaskResponse, ProjectResponse } from '../../../services/project-service';
import { TaskResponseDto, TaskType, TaskStatus } from '../../../types/project';

// Services
import { projectService } from '../../../services/project-service';

// Contexts
import { useCurrentUser } from '../../../contexts/user-context';

// Components
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";
import { Calendar as CalendarIcon } from "lucide-react";
import { CalendarControls } from '../../../components/calendar/calendar-controls';
import { CalendarDayView } from '../../../components/calendar/calendar-day-view';
import { CalendarWeekView } from '../../../components/calendar/calendar-week-view';
import { CalendarMonthView } from '../../../components/calendar/calendar-month-view';
import { CalendarTimelineView } from '../../../components/calendar/calendar-timeline-view';
import FullPageSpinnerLoader from '../../../components/full-page-spinner-loader';
import { ErrorMessageCallout } from '../../../components/error-message-callout';

// Utils
import { taskToEvent, filterEventsByUserAccess } from '../../../lib/calendar-utils';

export default function CalendarPage() {
  // State
  const { currentUser, isLoading: isUserLoading } = useCurrentUser();
  const [currentView, setCurrentView] = useState<CalendarView>('month');
  const [currentDate, setCurrentDate] = useState(new Date());
  const [filters, setFilters] = useState<CalendarFilters>({});
  const [selectedProjectId, setSelectedProjectId] = useState<number | undefined>();
  const [selectedTaskType, setSelectedTaskType] = useState<string>('My Projects');
  const [searchKeyword, setSearchKeyword] = useState<string>('');

  // Hooks
  const router = useRouter();
  // Get current user ID, fallback to 1 for now
  const currentUserId = currentUser?.backendStaffId || 1;
  // Fetch projects
  const { 
    data: projects = [], 
    isLoading: isLoadingProjects, 
    error: projectsError 
  } = useQuery({
    queryKey: ['projects'],
    queryFn: () => projectService.getUserProjects(currentUserId || 0),
  });

  // Fetch all tasks for calendar with virtual instance expansion
  const {
    data: allTasks = [],
    isLoading: isLoadingTasks,
    error: tasksError,
    refetch: refetchTasks
  } = useQuery({
    queryKey: ['calendar-tasks', selectedProjectId, selectedTaskType, currentView, currentDate.toISOString()],
    queryFn: async () => {
      let tasksData: TaskResponse[] = [];

      // Pass calendar view to backend for recurring task expansion
      const backendCalendarView = currentView;

      if (selectedTaskType === "Personal Tasks") {
        // Fetch only personal tasks (projectId = 0)
        tasksData = await projectService.getPersonalTasks(currentUserId, undefined, backendCalendarView, currentDate);
      } else if (selectedTaskType === "My Projects") {
        // Two-step logic for My Projects:
        // Step 1: Projects are already fetched via getUserProjects() (see projects query above)
        // Step 2: For each project, fetch tasks with calendar view expansion
        console.log('🔄 Fetching tasks for My Projects');
        console.log('📋 Total projects:', projects.length);
        for (const project of projects) {
          if (project.id === 0) continue; // Skip personal tasks (projectId = 0)
          try {
            console.log(`\n📂 Fetching tasks for Project ID: ${project.id} - "${project.name}"`);
            const tasks = await projectService.getProjectTasks(project.id, undefined, backendCalendarView, currentDate);
            console.log(`✅ Received ${tasks.length} tasks from backend for project ${project.id}`);
            console.log('📝 Tasks:', tasks.map(t => ({ id: t.id, title: t.title, status: t.status, projectId: t.projectId })));
            tasksData.push(...tasks);
          } catch (error) {
            console.error('❌ Failed to fetch tasks for project:', project.id, error);
          }
        }
        console.log(`\n✅ Total tasks loaded for My Projects: ${tasksData.length}`);
      } else if (selectedTaskType === "Project Tasks") {
        if (selectedProjectId) {
          // Two-step logic for specific project:
          // Step 1: Projects are already fetched via getUserProjects() (see projects query above)
          // Step 2: Fetch tasks for the selected project with calendar view expansion
          const selectedProject = projects.find(p => p.id === selectedProjectId);
          console.log(`\n📂 Fetching tasks for Selected Project ID: ${selectedProjectId} - "${selectedProject?.name}"`);
          tasksData = await projectService.getProjectTasks(selectedProjectId, undefined, backendCalendarView, currentDate);
          console.log(`✅ Received ${tasksData.length} tasks from backend for project ${selectedProjectId}`);
          console.log('📝 Tasks:', tasksData.map(t => ({ id: t.id, title: t.title, status: t.status, projectId: t.projectId })));
        } else {
          // Show all project tasks (across all projects)
          // Step 1: Projects are already fetched via getUserProjects() (see projects query above)
          // Step 2: For each project, fetch tasks with calendar view expansion
          console.log('🔄 Fetching tasks for All Project Tasks');
          console.log('📋 Total projects:', projects.length);
          for (const project of projects) {
            if (project.id === 0) continue; // Skip personal tasks (projectId = 0)
            try {
              console.log(`\n📂 Fetching tasks for Project ID: ${project.id} - "${project.name}"`);
              const tasks = await projectService.getProjectTasks(project.id, undefined, backendCalendarView, currentDate);
              console.log(`✅ Received ${tasks.length} tasks from backend for project ${project.id}`);
              console.log('📝 Tasks:', tasks.map(t => ({ id: t.id, title: t.title, status: t.status, projectId: t.projectId })));
              tasksData.push(...tasks);
            } catch (error) {
              console.error('❌ Failed to fetch tasks for project:', project.id, error);
            }
          }
          console.log(`\n✅ Total tasks loaded for All Projects: ${tasksData.length}`);
        }
      }

      return tasksData;
    },
    enabled: projects.length > 0 || selectedTaskType === "Personal Tasks",
  });

  // Listen for task deletion events and trigger manual refetch
  useEffect(() => {
    const handleTaskDeleted = (event: CustomEvent) => {
      console.log('Calendar received taskDeleted event:', event.detail);
      refetchTasks();
    };

    const handleTaskCreated = (event: CustomEvent) => {
      console.log('=== CALENDAR RECEIVED TASK CREATED EVENT ===');
      console.log('Calendar received taskCreated event:', event.detail);
      console.log('Current selectedTaskType:', selectedTaskType);
      console.log('Current selectedProjectId:', selectedProjectId);
      
      const { task, isPersonalTask } = event.detail;
      
      // Refetch based on current view and task type
      const shouldRefetch = (
        (selectedTaskType === "Personal Tasks" && isPersonalTask) ||
        (selectedTaskType === "My Projects" && !isPersonalTask) ||
        (selectedTaskType === "Project Tasks" && !isPersonalTask)
      );
      
      console.log('Should refetch?', shouldRefetch);
      console.log('Task is personal?', isPersonalTask);
      
      if (shouldRefetch) {
        console.log('About to call refetchTasks...');
        refetchTasks();
        console.log('RefetchTasks called successfully');
      } else {
        console.log('Skipping refetch - task type does not match current view');
      }
      
      console.log('=== END CALENDAR TASK CREATED HANDLING ===');
    };

    const handleTaskUpdated = (event: CustomEvent) => {
      console.log('Calendar received taskUpdated event:', event.detail);
      console.log('Current selectedTaskType:', selectedTaskType);
      
      const { task, isPersonalTask } = event.detail;
      
      // Refetch based on current view and task type
      const shouldRefetch = (
        (selectedTaskType === "Personal Tasks" && isPersonalTask) ||
        (selectedTaskType === "My Projects" && !isPersonalTask) ||
        (selectedTaskType === "Project Tasks" && !isPersonalTask)
      );
      
      if (shouldRefetch) {
        refetchTasks();
      }
    };

    window.addEventListener('taskDeleted', handleTaskDeleted as EventListener);
    window.addEventListener('taskCreated', handleTaskCreated as EventListener);
    window.addEventListener('taskUpdated', handleTaskUpdated as EventListener);
    
    return () => {
      window.removeEventListener('taskDeleted', handleTaskDeleted as EventListener);
      window.removeEventListener('taskCreated', handleTaskCreated as EventListener);
      window.removeEventListener('taskUpdated', handleTaskUpdated as EventListener);
    };
  }, [refetchTasks]);

  // Convert tasks to calendar events
  const calendarEvents: CalendarEvent[] = React.useMemo(() => {
    return allTasks.map((task: TaskResponse) => {
      const project = projects.find((p: ProjectResponse) => p.id === task.projectId);
      // Convert TaskResponse to TaskResponseDto format for taskToEvent
      const taskDto: TaskResponseDto = {
        ...task,
        taskType: task.taskType as TaskType,
        status: task.status as TaskStatus,
      };
      return taskToEvent(taskDto, project, currentUser?.backendStaffId);
    });
  }, [allTasks, projects, currentUser?.backendStaffId]);

  // Search function to filter and highlight events
  const searchAndHighlightEvents = React.useCallback((events: CalendarEvent[], keyword: string): CalendarEvent[] => {
    if (!keyword.trim()) {
      return events.map(event => ({ ...event, isHighlighted: false, matchedFields: [] }));
    }

    const searchTerm = keyword.toLowerCase().trim();
    
    return events.map(event => {
      const matchedFields: string[] = [];
      let isHighlighted = false;

      // Search in title
      if (event.title.toLowerCase().includes(searchTerm)) {
        matchedFields.push('title');
        isHighlighted = true;
      }

      // Search in description
      if (event.description?.toLowerCase().includes(searchTerm)) {
        matchedFields.push('description');
        isHighlighted = true;
      }

      // Search in project name
      if (event.projectName?.toLowerCase().includes(searchTerm)) {
        matchedFields.push('projectName');
        isHighlighted = true;
      }

      // Search in task type
      if (event.taskType.toLowerCase().includes(searchTerm)) {
        matchedFields.push('taskType');
        isHighlighted = true;
      }

      // Search in status
      if (event.status.toLowerCase().includes(searchTerm)) {
        matchedFields.push('status');
        isHighlighted = true;
      }

      // Search in tags
      if (event.tags?.some(tag => tag.toLowerCase().includes(searchTerm))) {
        matchedFields.push('tags');
        isHighlighted = true;
      }

      return {
        ...event,
        isHighlighted,
        matchedFields,
      };
    });
  }, []);

  // Filter events based on current filters and search
  const filteredEvents = React.useMemo(() => {
    // First apply search and highlighting
    let searchedEvents = searchAndHighlightEvents(calendarEvents, searchKeyword);

    // Then apply other filters
    let filtered = searchedEvents;

    // Filter by user access based on selected task type
    // "Project Tasks" view: Show ALL tasks in the project (including colleagues' tasks)
    // "My Projects" and "Personal Tasks": Show only tasks user is assigned to or owns
    if (selectedTaskType !== "Project Tasks") {
      filtered = filterEventsByUserAccess(filtered, currentUser?.backendStaffId);
    }

    // Filter out tasks without due dates (only show tasks with dueDate)
    filtered = filtered.filter(event => event.dueDate !== undefined && event.dueDate !== null);

    // If searching, only show matching events (highlighted ones)
    if (searchKeyword.trim()) {
      filtered = filtered.filter(event => event.isHighlighted);
    }

    // Filter by project (already handled in query)
    if (filters.projectId) {
      filtered = filtered.filter(event => event.projectId === filters.projectId);
    }

    // Filter by status
    if (filters.status && filters.status.length > 0) {
      filtered = filtered.filter(event => filters.status!.includes(event.status));
    }

    // Filter by task type
    if (filters.taskType && filters.taskType.length > 0) {
      filtered = filtered.filter(event => filters.taskType!.includes(event.taskType));
    }

    // Filter by assigned user
    if (filters.assignedUserId) {
      filtered = filtered.filter(event => 
        event.assignedUserIds?.includes(filters.assignedUserId!)
      );
    }

    return filtered;
  }, [calendarEvents, filters, currentUser?.backendStaffId, searchKeyword, searchAndHighlightEvents]);

  // Event handlers
  const handleViewChange = useCallback((view: CalendarView) => {
    setCurrentView(view);
  }, []);

  const handleDateChange = useCallback((date: Date) => {
    setCurrentDate(date);
  }, []);

  const handleProjectFilter = useCallback((projectId?: number) => {
    setSelectedProjectId(projectId);
    setFilters(prev => ({ 
      ...prev, 
      projectId 
    }));
  }, []);

  const handleTaskTypeChange = useCallback((taskType: string) => {
    setSelectedTaskType(taskType);
    // Reset project filter when switching to My Projects or Personal Tasks
    if (taskType === "My Projects" || taskType === "Personal Tasks") {
      setSelectedProjectId(undefined);
      setFilters(prev => ({ 
        ...prev, 
        projectId: undefined 
      }));
    }
  }, []);

  const handleSearchChange = useCallback((keyword: string) => {
    setSearchKeyword(keyword);
  }, []);

  const handleToday = useCallback(() => {
    setCurrentDate(new Date());
  }, []);

  const handleEventClick = useCallback((event: CalendarEvent) => {
    // Navigate to task detail page
    router.push(`/tasks/${event.id}`);
  }, [router]);

  const handleDateClick = useCallback((date: Date) => {
    setCurrentDate(date);
    // If not in day view, switch to day view when clicking a date
    if (currentView !== 'day') {
      setCurrentView('day');
    }
  }, [currentView]);

  // Loading and error states
  if (isLoadingProjects) {
    return <FullPageSpinnerLoader />;
  }

  if (projectsError) {
    return (
      <div className="h-full flex items-center justify-center bg-background text-foreground">
        <ErrorMessageCallout 
          errorMessage="There was an error loading the calendar. Please try again." 
        />
      </div>
    );
  }

  if (tasksError) {
    return (
      <div className="h-full flex items-center justify-center bg-background text-foreground">
        <ErrorMessageCallout 
          errorMessage="There was an error loading the tasks. Please try again." 
        />
      </div>
    );
  }

  // Render appropriate view
  const renderCalendarView = () => {
    const viewProps = {
      events: filteredEvents,
      currentDate,
      searchKeyword,
      onEventClick: handleEventClick,
      onDateClick: handleDateClick,
    };

    switch (currentView) {
      case 'day':
        return <CalendarDayView {...viewProps} />;
      case 'week':
        return <CalendarWeekView {...viewProps} />;
      case 'month':
        return <CalendarMonthView {...viewProps} />;
      case 'timeline':
        return (
          <CalendarTimelineView 
            {...viewProps}
            startDate={startOfMonth(subMonths(currentDate, 1))}
            endDate={endOfMonth(addMonths(currentDate, 2))}
            timeScale="days"
          />
        );
      default:
        return <CalendarMonthView {...viewProps} />;
    }
  };

  return (
    <SidebarInset>
      <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
        <SidebarTrigger className="-ml-1" />
        <Separator orientation="vertical" className="mr-2 h-4" />
        <div className="flex items-center gap-2">
          <CalendarIcon className="h-5 w-5" />
          <h1 className="text-lg font-semibold">Calendar</h1>
        </div>
      </header>

      <div className="w-full min-h-screen p-2 mx-auto bg-background text-foreground">
        {/* Calendar Controls */}
        <CalendarControls
        currentView={currentView}
        currentDate={currentDate}
        projects={projects}
        selectedProjectId={selectedProjectId}
        selectedTaskType={selectedTaskType}
        searchKeyword={searchKeyword}
        searchResultsCount={filteredEvents.length}
        onViewChange={handleViewChange}
        onDateChange={handleDateChange}
        onProjectFilter={handleProjectFilter}
        onTaskTypeChange={handleTaskTypeChange}
        onSearchChange={handleSearchChange}
        onToday={handleToday}
      />

      {/* Calendar View */}
      <div className="flex-1 overflow-hidden relative">
        {isLoadingTasks ? (
          <div className="absolute inset-0 flex items-center justify-center bg-background/80 backdrop-blur-sm">
            <FullPageSpinnerLoader />
          </div>
        ) : (
          renderCalendarView()
        )}
      </div>
      </div>
    </SidebarInset>
  );
}

