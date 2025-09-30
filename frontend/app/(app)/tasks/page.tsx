"use client";

import { useEffect, useMemo, useState } from "react";
import {
  DndContext,
  DragEndEvent,
  DragOverlay,
  DragStartEvent,
  PointerSensor,
  useSensor,
  useSensors,
  closestCenter,
  pointerWithin,
  rectIntersection,
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { ScrollArea } from "@/components/ui/scroll-area";
import { DroppableColumn } from "@/components/ui/droppable-column";
import {
  type TaskPriority,
  type TaskStatus,
} from "@/lib/mvp-data";
import {
  CalendarDays,
  Clock4,
  MoreHorizontal,
  Paperclip,
  Plus,
  Users as UsersIcon,
} from "lucide-react";
import { projectService, TaskResponse, ProjectResponse } from "@/services/project-service";
import { TaskCreationDialog } from "@/components/task-creation-dialog";
import { TaskCard } from "@/components/task-card";
import { DraggableTaskCard } from "@/components/draggable-task-card";
import { useCurrentUser } from "@/contexts/user-context";
import { cn } from "@/lib/utils";

const STATUS_FILTERS: (TaskStatus | "All")[] = [
  "All",
  "Todo",
  "In Progress",
  "Blocked",
  "Done",
];

const TASK_TYPE_FILTERS = [
  "All Tasks",
  "Personal Tasks",
];

// Map backend status to frontend status
const mapBackendStatus = (status: string): TaskStatus => {
  switch (status) {
    case 'TODO': return 'Todo';
    case 'IN_PROGRESS': return 'In Progress';
    case 'BLOCKED': return 'Blocked';
    case 'COMPLETED': return 'Done';
    default: return 'Todo';
  }
};

// Map backend task type to priority (simplified)
const mapTaskTypeToPriority = (taskType: string): TaskPriority => {
  switch (taskType) {
    case 'BUG': return 'High';
    case 'FEATURE': return 'Medium';
    case 'CHORE': return 'Low';
    case 'RESEARCH': return 'Medium';
    default: return 'Medium';
  }
};

const statusOrder: TaskStatus[] = [
  "Todo",
  "In Progress",
  "Blocked",
  "Done",
];

const statusStyles: Record<TaskStatus, string> = {
  Todo: "border-border bg-background",
  "In Progress": "border-border bg-background",
  Blocked: "border-border bg-background",
  Review: "border-border bg-background", // Keep for compatibility but won't be used
  Done: "border-border bg-background",
};

const priorityStyles: Record<TaskPriority, { badge: string; text: string }> = {
  High: {
    badge:
      "border-rose-300 bg-rose-100 text-rose-600 dark:border-rose-500/40 dark:bg-rose-500/20 dark:text-rose-200",
    text: "text-rose-600 dark:text-rose-200",
  },
  Medium: {
    badge:
      "border-amber-300 bg-amber-100 text-amber-600 dark:border-amber-500/40 dark:bg-amber-500/20 dark:text-amber-200",
    text: "text-amber-600 dark:text-amber-200",
  },
  Low: {
    badge:
      "border-slate-300 bg-slate-100 text-slate-600 dark:border-slate-500/40 dark:bg-slate-500/20 dark:text-slate-200",
    text: "text-slate-600 dark:text-slate-200",
  },
};

const formatDate = (value: string) =>
  new Date(value).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  });

const formatRelativeDate = (value: string) => {
  const now = new Date();
  const target = new Date(value);
  const diffMs = now.getTime() - target.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays <= 0) return "Today";
  if (diffDays === 1) return "1 day ago";
  if (diffDays < 7) return `${diffDays} days ago`;

  const diffWeeks = Math.floor(diffDays / 7);
  if (diffWeeks < 5) return `${diffWeeks}w ago`;

  return target.toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  });
};

const getInitials = (value: string) =>
  value
    .split(" ")
    .filter(Boolean)
    .map((part) => part[0] ?? "")
    .join("")
    .slice(0, 2)
    .toUpperCase();

// Helper function to get task display properties from TaskResponse
const getTaskDisplayProps = (task: TaskResponse) => {
  return {
    id: task.id,
    title: task.title,
    description: task.description || '',
    status: mapBackendStatus(task.status),
    key: `TASK-${task.id}`,
    priority: mapTaskTypeToPriority(task.taskType),
    owner: `User ${task.ownerId}`,
    collaborators: [] as string[], // TODO: Add collaborators when available in API
    dueDate: task.createdAt, // Using createdAt as placeholder for due date
    lastUpdated: task.updatedAt || task.createdAt,
    attachments: 0, // TODO: Add attachment count when available
    project: task.projectId ? `Project ${task.projectId}` : 'Personal Task',
    subtasks: task.subtasks || [],
    ownerId: task.ownerId,
    projectId: task.projectId
  };
};

const getSubtaskSummary = (task: TaskResponse) => {
  const subtasks = task.subtasks || [];
  const total = subtasks.length;
  
  // Map backend status to frontend status for counting
  const done = subtasks.filter((subtask) => {
    const mappedStatus = mapBackendStatus(subtask.status);
    return mappedStatus === "Done";
  }).length;
  
  const progress = total > 0 ? Math.round((done / total) * 100) : 0;

  return { total, done, progress };
};

type TaskBoardCardProps = {
  task: TaskResponse;
};

const TaskBoardCard = ({ task }: TaskBoardCardProps) => {
  const { total, done, progress } = getSubtaskSummary(task);
  const taskProps = getTaskDisplayProps(task);
  const collaboratorOverflow = Math.max(taskProps.collaborators.length - 2, 0);

  return (
    <article
      className="rounded-lg border border-border/70 bg-background p-2.5 shadow-sm transition hover:border-primary/50 hover:shadow-md"
      data-testid="board-card"
    >
      <div className="flex items-start justify-between gap-2 mb-2">
        <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
          <span>{taskProps.key}</span>
          <Badge
            variant="outline"
            className={`border-none px-1 py-0 text-[0.6rem] ${priorityStyles[taskProps.priority].badge}`}
          >
            {taskProps.priority}
          </Badge>
        </div>
        <div className="flex items-center gap-1 text-xs text-muted-foreground">
          <CalendarDays className="h-3 w-3" aria-hidden="true" />
          <span>{formatDate(taskProps.dueDate)}</span>
        </div>
      </div>

      <h3 className="text-sm font-semibold leading-tight mb-2">{taskProps.title}</h3>

      <div className="flex items-center gap-2 mb-2">
        <div className="flex h-6 w-6 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary">
          {getInitials(taskProps.owner)}
        </div>
        <span className="text-xs font-medium truncate">{taskProps.owner}</span>
      </div>

        {taskProps.collaborators.length > 0 && (
        <div className="flex items-center gap-2 mb-2 text-xs text-muted-foreground">
          <UsersIcon className="h-3 w-3" aria-hidden="true" />
          <div className="flex items-center -space-x-1">
            {taskProps.collaborators.slice(0, 2).map((name: string) => (
              <span
                key={name}
                className="flex h-5 w-5 items-center justify-center rounded-full border border-background bg-secondary text-[0.6rem] font-semibold text-secondary-foreground shadow-sm"
              >
                {getInitials(name)}
              </span>
            ))}
            {collaboratorOverflow > 0 ? (
              <span className="flex h-5 w-5 items-center justify-center rounded-full border border-dashed border-muted-foreground/50 bg-background text-[0.6rem] font-semibold text-muted-foreground">
                +{collaboratorOverflow}
              </span>
            ) : null}
          </div>
        </div>
      )}      {total > 0 && (
        <div className="space-y-1">
          <div className="flex items-center justify-between text-xs">
            <span className="text-muted-foreground">
              {done}/{total}
            </span>
            <span className="text-muted-foreground">{progress}%</span>
          </div>
          <div className="h-1.5 w-full rounded-full bg-muted">
            <div
              className="h-1.5 rounded-full bg-primary transition-all"
              style={{ width: `${progress}%` }}
              aria-hidden="true"
            />
          </div>
        </div>
      )}

      {taskProps.attachments > 0 && (
        <div className="mt-2 flex items-center gap-1 text-xs text-muted-foreground">
          <Paperclip className="h-3 w-3" aria-hidden="true" />
          <span>{taskProps.attachments}</span>
        </div>
      )}
    </article>
  );
};

type TaskTableRowProps = {
  task: TaskResponse;
};

const TaskTableRow = ({ task }: TaskTableRowProps) => {
  const { total, done, progress } = getSubtaskSummary(task);
  const taskProps = getTaskDisplayProps(task);
  const collaboratorOverflow = Math.max(taskProps.collaborators.length - 3, 0);

  return (
    <div
      className="grid gap-3 py-4 transition hover:bg-accent/50 hover:text-accent-foreground sm:grid-cols-[minmax(200px,2fr)_minmax(120px,1fr)_minmax(120px,1fr)_minmax(100px,1fr)_minmax(100px,1fr)_minmax(100px,1fr)] sm:py-5"
      data-testid="table-row"
    >
      <div className="space-y-2">
        <div className="flex items-center gap-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
          <span>{taskProps.key}</span>
          <Badge
            variant="outline"
            className={`border-none px-1.5 py-0 text-[0.65rem] ${priorityStyles[taskProps.priority].badge}`}
          >
            {taskProps.priority}
          </Badge>
        </div>
        <p className="text-sm font-semibold leading-5">{taskProps.title}</p>
        <p className="text-muted-foreground text-xs leading-relaxed line-clamp-2">
          {taskProps.description}
        </p>
        <p className="text-muted-foreground text-xs">{taskProps.project}</p>
      </div>

      <div className="flex items-center gap-3">
        <div className="flex h-9 w-9 items-center justify-center rounded-full bg-primary/10 text-sm font-semibold text-primary">
          {getInitials(taskProps.owner)}
        </div>
        <div className="text-sm">
          <p className="font-medium">{taskProps.owner}</p>
          <p className="text-muted-foreground text-xs">Owner</p>
        </div>
      </div>

      <div className="flex flex-col gap-2">
        <div className="flex items-center -space-x-2">
          {taskProps.collaborators.slice(0, 3).map((name: string) => (
            <span
              key={name}
              className="flex h-8 w-8 items-center justify-center rounded-full border border-background bg-secondary text-[0.65rem] font-semibold text-secondary-foreground shadow-sm"
            >
              {getInitials(name)}
            </span>
          ))}
          {collaboratorOverflow > 0 ? (
            <span className="flex h-8 w-8 items-center justify-center rounded-full border border-dashed border-muted-foreground/50 bg-background text-[0.65rem] font-semibold text-muted-foreground">
              +{collaboratorOverflow}
            </span>
          ) : null}
        </div>
        <span className="text-xs text-muted-foreground">
          {taskProps.collaborators.length
            ? `${taskProps.collaborators.length} collaborator${
                taskProps.collaborators.length > 1 ? "s" : ""
              }`
            : "No collaborators"}
        </span>
      </div>

      <div className="flex flex-col gap-2 text-sm">
        <Badge className={`${statusStyles[taskProps.status]} px-2 py-0.5 text-xs`}>
          {taskProps.status}
        </Badge>
        <span className={`text-xs ${priorityStyles[taskProps.priority].text}`}>
          {taskProps.priority} priority
        </span>
      </div>

      <div className="space-y-2 text-xs">
        <div className="font-medium">
          {done}/{total} complete
        </div>
        <div className="h-2 w-full rounded-full bg-muted">
          <div
            className="h-2 rounded-full bg-primary transition-all"
            style={{ width: `${progress}%` }}
            aria-hidden="true"
          />
        </div>
      </div>

      <div className="flex flex-col gap-2 text-xs text-muted-foreground">
        <span className="flex items-center gap-1">
          <Clock4 className="h-3.5 w-3.5" aria-hidden="true" />
          {formatRelativeDate(taskProps.lastUpdated)}
        </span>
        {taskProps.attachments > 0 ? (
          <span className="flex items-center gap-1">
            <Paperclip className="h-3.5 w-3.5" aria-hidden="true" />
            {taskProps.attachments} attachment{taskProps.attachments > 1 ? "s" : ""}
          </span>
        ) : null}
      </div>
    </div>
  );
};

export default function TasksPage() {
  const { currentUser, isLoading: userLoading } = useCurrentUser();
  const [selectedStatus, setSelectedStatus] =
    useState<(typeof STATUS_FILTERS)[number]>("All");
  const [selectedTaskType, setSelectedTaskType] = useState<string>("All Tasks");
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateDialog, setShowCreateDialog] = useState(false);
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [selectedProject, setSelectedProject] = useState<string>("all"); // "all" | "personal" | "owned" | projectId string
  const [dateFrom, setDateFrom] = useState<string>(""); // yyyy-MM-dd
  const [dateTo, setDateTo] = useState<string>("");
  const [activeTask, setActiveTask] = useState<TaskResponse | null>(null);

  type SortBy = "due" | "status" | "updated";
  const [sortBy, setSortBy] = useState<SortBy>("due");

  // Drag and drop sensors
  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 8,
      },
    })
  );

  // Map frontend status to backend status
  const mapFrontendToBackend = (frontendStatus: TaskStatus): 'TODO' | 'IN_PROGRESS' | 'COMPLETED' | 'BLOCKED' => {
    switch (frontendStatus) {
      case 'Todo': return 'TODO';
      case 'In Progress': return 'IN_PROGRESS';
      case 'Blocked': return 'BLOCKED';
      case 'Done': return 'COMPLETED';
      default: return 'TODO';
    }
  };

  // Custom collision detection for columns
  const collisionDetectionStrategy = (args: any) => {
    const pointerIntersections = pointerWithin(args);
    const droppableIntersections = pointerIntersections.filter((intersection: any) => {
      return statusOrder.some(status => status === intersection.id);
    });

    if (droppableIntersections.length > 0) {
      return droppableIntersections;
    }

    const rectIntersections = rectIntersection(args);
    const droppableRectIntersections = rectIntersections.filter((intersection: any) => {
      return statusOrder.some(status => status === intersection.id);
    });

    if (droppableRectIntersections.length > 0) {
      return droppableRectIntersections;
    }

    const allIntersections = closestCenter(args);
    return allIntersections.filter((intersection: any) => {
      return statusOrder.some(status => status === intersection.id);
    });
  };

  const handleDragStart = (event: DragStartEvent) => {
    const { active } = event;
    const task = tasks.find((t) => t.id === active.id);
    setActiveTask(task || null);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
    const { active, over } = event;
    setActiveTask(null);

    if (!over) {
      return;
    }

    const taskId = active.id as number;
    let newStatus: TaskStatus | null = null;

    // Check if dropped directly on a column
    if (statusOrder.some(status => status === over.id)) {
      newStatus = over.id as TaskStatus;
    } else {
      // If dropped on a card, find which column it belongs to
      const droppedOnTask = tasks.find(t => t.id === over.id);
      if (droppedOnTask) {
        newStatus = mapBackendStatus(droppedOnTask.status);
      }
    }

    if (!newStatus) {
      return;
    }

    const task = tasks.find((t) => t.id === taskId);

    if (!task) {
      console.error("Task not found during drag end:", taskId);
      return;
    }

    const currentMappedStatus = mapBackendStatus(task.status);

    // Only update if the status actually changed
    if (currentMappedStatus !== newStatus) {
      try {
        const backendStatus = mapFrontendToBackend(newStatus);
        const updatedTask = await projectService.updateTask({
          taskId: task.id,
          status: backendStatus,
        });
        handleTaskUpdated(updatedTask);
      } catch (error) {
        console.error('Error updating task status:', error);
        // Optionally show error toast
      }
    }
  };

  useEffect(() => {
    const loadTasks = async () => {
      try {
        setLoading(true);
        
        // Check if we have a current user
        if (!currentUser) {
          setError("User not authenticated");
          return;
        }
        
        // Use backendStaffId if available, otherwise use a fallback (like parsing from cognitoSub or using 1)
        const userId = currentUser.backendStaffId || 1; // Fallback to 1 for now
        
        let tasksData: TaskResponse[] = [];
        if (selectedTaskType === "Personal Tasks") {
          tasksData = await projectService.getPersonalTasks(userId);
        } else if (selectedTaskType === "Project Tasks") {
          // Get all user tasks and filter for those with projectId
          const allTasks = await projectService.getAllUserTasks(userId);
          tasksData = allTasks.filter(task => task.projectId !== null);
        } else {
          // All tasks
          tasksData = await projectService.getAllUserTasks(userId);
        }
        
        setTasks(tasksData);
      } catch (err) {
        console.error('Error loading tasks:', err);
        setError("Failed to load tasks");
        setTasks([]);
      } finally {
        setLoading(false);
      }
    };

    // Only load tasks if we have a user and user context is not loading
    if (!userLoading && currentUser) {
      loadTasks();
    } else if (!userLoading && !currentUser) {
      setLoading(false);
      setError("Please log in to view tasks");
    }
  }, [selectedTaskType, currentUser, userLoading]);

  // Load project list for project filter
  useEffect(() => {
    const loadProjects = async () => {
      try {
        if (!currentUser) return;
        
        const userId = currentUser.backendStaffId || 1; // Fallback to 1 for now
        const data = await projectService.getUserProjects(userId);
        setProjects(data);
      } catch (e) {
        // Non-fatal: project filter will just show Personal/All
        setProjects([]);
      }
    };
    
    if (!userLoading && currentUser) {
      loadProjects();
    }
  }, [currentUser, userLoading]);

  const handleTaskCreated = (newTask: TaskResponse) => {
    // Add the new task to the current tasks list
    setTasks(prevTasks => [newTask, ...prevTasks]);
  };

  const handleTaskUpdated = (updatedTask: TaskResponse) => {
    // Update the task in the list
    setTasks(prevTasks =>
      prevTasks.map(task => task.id === updatedTask.id ? updatedTask : task)
    );
  };

  // Use only real tasks - no fallback to demo data
  const displayTasks = tasks;

  const totals = useMemo(() => {
    const owners = new Set<number>();
    let dueSoon = 0;

    tasks.forEach((task) => {
      owners.add(task.ownerId);
      
      const createdDate = new Date(task.createdAt);
      const daysSinceCreated = Math.floor((Date.now() - createdDate.getTime()) / (1000 * 60 * 60 * 24));
      if (daysSinceCreated <= 7) {
        dueSoon += 1;
      }
    });

    return {
      workItems: tasks.length,
      owners: owners.size,
      collaborators: 0, // We don't have collaborators data in the simple API response
      dueSoon,
      subtaskTotal: 0, // We don't have subtasks in the current model
      subtaskDone: 0,
      subtaskRemaining: 0,
    };
  }, [tasks]);

  // Apply project and due-date range filters first
  const baseFilteredTasks = useMemo(() => {
    const fromDate = dateFrom ? new Date(dateFrom) : null;
    const toDate = dateTo ? new Date(dateTo) : null;

    return tasks.filter((t) => {
      // Project filter
      if (selectedProject === "personal" && t.projectId) return false;
      if (selectedProject === "owned" && currentUser) {
        // Show only projects owned by current user
        const userId = currentUser.backendStaffId || 1;
        const project = projects.find(p => p.id === t.projectId);
        if (!project || project.ownerId !== userId) return false;
      }
      if (selectedProject !== "all" && selectedProject !== "personal" && selectedProject !== "owned") {
        const pid = Number(selectedProject);
        if (!Number.isNaN(pid) && t.projectId !== pid) return false;
      }

      // Due date range (using createdAt as due date placeholder)
      const due = new Date(t.createdAt);
      if (fromDate && due < fromDate) return false;
      if (toDate) {
        const endOfTo = new Date(toDate);
        endOfTo.setHours(23, 59, 59, 999);
        if (due > endOfTo) return false;
      }
      return true;
    });
  }, [tasks, selectedProject, dateFrom, dateTo, currentUser, projects]);

  // Sorting according to selection
  const sortedTasks = useMemo(() => {
    const copy = [...baseFilteredTasks];
    if (sortBy === "status") {
      return copy.sort((a, b) => {
        const sa = statusOrder.indexOf(mapBackendStatus(a.status));
        const sb = statusOrder.indexOf(mapBackendStatus(b.status));
        return sa - sb;
      });
    }
    if (sortBy === "updated") {
      return copy.sort((a, b) => {
        const au = new Date(a.updatedAt || a.createdAt).getTime();
        const bu = new Date(b.updatedAt || b.createdAt).getTime();
        return au - bu;
      });
    }
    // default: due date (using createdAt as placeholder)
    return copy.sort(
      (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    );
  }, [baseFilteredTasks, sortBy]);

  const tasksByStatus = useMemo(() => {
    const grouped = statusOrder.reduce<Record<TaskStatus, TaskResponse[]>>(
      (acc, status) => {
        acc[status] = [];
        return acc;
      },
      {} as Record<TaskStatus, TaskResponse[]>,
    );

    sortedTasks.forEach((task) => {
      const mappedStatus = mapBackendStatus(task.status);
      if (grouped[mappedStatus]) {
        grouped[mappedStatus].push(task);
      }
    });

    return grouped;
  }, [sortedTasks]);

  const boardStatuses: TaskStatus[] =
    selectedStatus === "All" ? statusOrder : [selectedStatus as TaskStatus];

  const filteredTasks = useMemo(() => {
    let tasksToFilter: TaskResponse[] = sortedTasks;
    if (selectedStatus !== "All") {
      tasksToFilter = [...tasksByStatus[selectedStatus as TaskStatus]];
    }
    return tasksToFilter;
  }, [selectedStatus, sortedTasks, tasksByStatus]);

  return (
    <SidebarInset>
      <div className="flex flex-1 flex-col gap-4 p-4 pb-8 lg:p-6">
        <header className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <SidebarTrigger className="-ml-1" />
            <div>
              <h1 className="text-xl font-semibold tracking-tight">Tasks</h1>
              <p className="text-muted-foreground text-sm">
                Rich cards that surface owners, collaborators, and delivery
                detail.
              </p>
            </div>
          </div>
          <Button size="sm" onClick={() => setShowCreateDialog(true)}>
            <Plus className="h-4 w-4 mr-2" />
            New Task
          </Button>
        </header>

        <section className="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
          <Card className="p-3">
            <CardHeader className="pb-2">
              <CardDescription>Active cards</CardDescription>
              <CardTitle className="text-2xl font-semibold">
                {totals.workItems}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm pt-0">
              {totals.dueSoon} due within 7 days
            </CardContent>
          </Card>
          <Card className="p-3">
            <CardHeader className="pb-2">
              <CardDescription>Owners</CardDescription>
              <CardTitle className="text-2xl font-semibold">
                {totals.owners}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm pt-0">
              People accountable for these work items
            </CardContent>
          </Card>
          <Card className="p-3">
            <CardHeader className="pb-2">
              <CardDescription>Collaborators</CardDescription>
              <CardTitle className="text-2xl font-semibold">
                {totals.collaborators}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm pt-0">
              Partners helping to ship these tasks
            </CardContent>
          </Card>
          <Card className="p-3">
            <CardHeader className="pb-2">
              <CardDescription>Subtasks remaining</CardDescription>
              <CardTitle className="text-2xl font-semibold">
                {totals.subtaskRemaining}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm pt-0">
              {totals.subtaskDone}/{totals.subtaskTotal} complete across cards
            </CardContent>
          </Card>
        </section>

        <section className="space-y-3 flex-1 min-h-0">
          {/* Task Type Filter */}
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-lg font-semibold">Task Management</h2>
              <p className="text-muted-foreground text-sm">
                Filter between personal tasks and project tasks, then organize by status.
              </p>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              {TASK_TYPE_FILTERS.map((taskType) => (
                <Button
                  key={taskType}
                  size="sm"
                  variant={selectedTaskType === taskType ? "default" : "outline"}
                  onClick={() => setSelectedTaskType(taskType)}
                >
                  {taskType}
                </Button>
              ))}
            </div>
          </div>

          {/* Status + Additional Filters */}
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h3 className="text-base font-semibold">Board view</h3>
              <p className="text-muted-foreground text-sm">
                {selectedTaskType === "Personal Tasks" 
                  ? "Your personal tasks organized by status."
                  : selectedTaskType === "Project Tasks"
                  ? "Tasks from your projects organized by status."
                  : "All your tasks organized by status."}
              </p>
            </div>
            <div className="flex flex-col gap-2 md:flex-row md:items-center md:gap-3">
              <div className="flex flex-wrap items-center gap-2">
                {STATUS_FILTERS.map((status) => (
                  <Button
                    key={status}
                    size="sm"
                    variant={selectedStatus === status ? "default" : "outline"}
                    onClick={() => setSelectedStatus(status)}
                  >
                    {status}
                  </Button>
                ))}
              </div>
              <div className="flex items-center gap-2">
                <Label className="text-xs text-muted-foreground">Project</Label>
                <Select value={selectedProject} onValueChange={setSelectedProject}>
                  <SelectTrigger className="h-8 w-[180px]">
                    <SelectValue placeholder="All" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All Projects</SelectItem>
                    <SelectItem value="personal">Personal Only</SelectItem>
                    <SelectItem value="owned">My Projects</SelectItem>
                    {projects.map((p) => (
                      <SelectItem key={p.id} value={String(p.id)}>{p.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="flex items-center gap-2">
                <Label htmlFor="from" className="text-xs text-muted-foreground">Due from</Label>
                <Input id="from" type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} className="h-8 w-[150px]" />
                <Label htmlFor="to" className="text-xs text-muted-foreground">to</Label>
                <Input id="to" type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} className="h-8 w-[150px]" />
              </div>
              <div className="flex items-center gap-2">
                <Label className="text-xs text-muted-foreground">Sort</Label>
                <Select value={sortBy} onValueChange={(v) => setSortBy(v as SortBy)}>
                  <SelectTrigger className="h-8 w-[170px]">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="due">Due date (default)</SelectItem>
                    <SelectItem value="status">Status</SelectItem>
                    <SelectItem value="updated">Last updated</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          <div className="flex-1">
            {loading ? (
              <div className="flex items-center justify-center h-64">
                <div className="text-center space-y-2">
                  <div className="text-lg font-semibold">Loading tasks...</div>
                  <div className="text-muted-foreground">Please wait while we fetch your tasks</div>
                </div>
              </div>
            ) : error ? (
              <div className="flex items-center justify-center h-64">
                <div className="text-center space-y-2">
                  <div className="text-lg font-semibold text-destructive">Failed to load tasks</div>
                  <div className="text-muted-foreground">Please try refreshing the page</div>
                </div>
              </div>
            ) : displayTasks.length === 0 ? (
              <div className="flex items-center justify-center h-64">
                <div className="text-center space-y-4">
                  <div className="text-lg font-semibold">No tasks yet</div>
                  <div className="text-muted-foreground">Create your first task to get started</div>
                  <Button onClick={() => setShowCreateDialog(true)}>
                    <Plus className="mr-2 h-4 w-4" />
                    Create Task
                  </Button>
                </div>
              </div>
            ) : (
              <DndContext
                sensors={sensors}
                collisionDetection={collisionDetectionStrategy}
                onDragStart={handleDragStart}
                onDragEnd={handleDragEnd}
              >
                <div className="w-full overflow-x-auto">
                  <div className="flex gap-3 h-full min-h-[calc(100vh-400px)] pb-4" style={{ minWidth: `${boardStatuses.length * 280}px` }}>
                    {boardStatuses.map((status) => {
                      const columnTasks = tasksByStatus[status];

                      return (
                        <Card
                          key={status}
                          className={cn("flex flex-col h-full w-72 flex-shrink-0", statusStyles[status])}
                          data-testid={`board-column-${status.toLowerCase().replace(/\s+/g, "-")}`}
                        >
                          <CardHeader className="pb-3 flex-shrink-0">
                            <div className="flex items-center justify-between">
                              <div>
                                <p className="text-sm font-semibold">{status}</p>
                                <p className="text-muted-foreground text-xs">
                                  {columnTasks.length} card{columnTasks.length === 1 ? "" : "s"}
                                </p>
                              </div>
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-7 w-7 text-muted-foreground"
                                aria-label={`Column actions for ${status}`}
                              >
                                <MoreHorizontal
                                  className="h-4 w-4"
                                  aria-hidden="true"
                                />
                              </Button>
                            </div>
                          </CardHeader>
                          <CardContent className="pt-0 flex-1 flex flex-col min-h-0 overflow-hidden">
                            <SortableContext
                              items={columnTasks.map((t) => t.id)}
                              strategy={verticalListSortingStrategy}
                            >
                              <DroppableColumn
                                id={status}
                                className="rounded flex-1 min-h-0 p-2 overflow-y-auto"
                              >
                                <ScrollArea className="h-full w-full">
                                  <div className="space-y-2 pr-2">
                                    {columnTasks.map((task) => (
                                      <DraggableTaskCard
                                        key={task.id}
                                        task={task}
                                        onTaskUpdated={handleTaskUpdated}
                                      />
                                    ))}
                                    {columnTasks.length === 0 && (
                                      <div className="rounded-lg border border-dashed border-border/70 p-3 text-center text-xs text-muted-foreground">
                                        Drop tasks here
                                      </div>
                                    )}
                                  </div>
                                </ScrollArea>
                              </DroppableColumn>
                            </SortableContext>
                          </CardContent>
                        </Card>
                      );
                    })}
                  </div>
                </div>
                <DragOverlay>
                  {activeTask ? (
                    <div className="rotate-3 scale-105">
                      <TaskCard task={activeTask} variant="board" />
                    </div>
                  ) : null}
                </DragOverlay>
              </DndContext>
            )}
          </div>
        </section>

        <section className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold">Detailed list</h2>
            <p className="text-muted-foreground text-sm">
              Rich table view for planning, filtering, and status updates.
            </p>
          </div>

          <Card>
            <CardHeader className="pb-4">
              <div className="hidden sm:grid gap-3 text-xs font-medium uppercase tracking-wide text-muted-foreground sm:grid-cols-[minmax(200px,2fr)_minmax(120px,1fr)_minmax(120px,1fr)_minmax(100px,1fr)_minmax(100px,1fr)_minmax(100px,1fr)]">
                <span>Task</span>
                <span>Owner</span>
                <span>Collaborators</span>
                <span>Status</span>
                <span>Subtasks</span>
                <span>Activity</span>
              </div>
            </CardHeader>
            <Separator className="mx-6" />
            <CardContent className="divide-border flex flex-col divide-y">
              {filteredTasks.length > 0 ? (
                filteredTasks.map((task) => (
                  <TaskCard key={task.id} task={task} variant="table" onTaskUpdated={handleTaskUpdated} />
                ))
              ) : (
                <div className="flex items-center justify-center py-16">
                  <div className="text-center space-y-2">
                    <div className="text-muted-foreground">No tasks match the current filters</div>
                    {displayTasks.length === 0 && (
                      <Button onClick={() => setShowCreateDialog(true)} variant="outline" size="sm">
                        <Plus className="mr-2 h-4 w-4" />
                        Create Task
                      </Button>
                    )}
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </section>
      </div>

      <TaskCreationDialog
        open={showCreateDialog}
        onOpenChange={setShowCreateDialog}
        onTaskCreated={handleTaskCreated}
        availableProjects={projects.map(p => ({ id: p.id, name: p.name }))}
      />
    </SidebarInset>
  );
}
