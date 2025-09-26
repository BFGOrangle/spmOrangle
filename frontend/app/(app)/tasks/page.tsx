"use client";

import { useEffect, useMemo, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import {
  type TaskPriority,
  type TaskStatus,
  type TaskSummary,
} from "@/lib/mvp-data";
import {
  CalendarDays,
  Clock4,
  MoreHorizontal,
  Paperclip,
  Plus,
  Users as UsersIcon,
} from "lucide-react";
import { projectService, TaskResponse } from "@/services/project-service";
import { TaskCreationDialog } from "@/components/task-creation-dialog";
import { TaskCard } from "@/components/task-card";

const STATUS_FILTERS: (TaskStatus | "All")[] = [
  "All",
  "Todo",
  "In Progress",
  "Blocked",
  "Review",
  "Done",
];

const TASK_TYPE_FILTERS = [
  "All Tasks",
  "Personal Tasks",
  "Project Tasks",
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

const statusOrder: TaskStatus[] = [
  "Todo",
  "In Progress",
  "Blocked",
  "Review",
  "Done",
];

const statusStyles: Record<TaskStatus, string> = {
  Todo: "border-slate-300 bg-slate-100 text-slate-700 dark:border-slate-500/40 dark:bg-slate-500/20 dark:text-slate-100",
  "In Progress":
    "border-sky-300 bg-sky-100 text-sky-700 dark:border-sky-500/40 dark:bg-sky-500/20 dark:text-sky-100",
  Blocked:
    "border-amber-300 bg-amber-100 text-amber-700 dark:border-amber-500/40 dark:bg-amber-500/20 dark:text-amber-100",
  Review:
    "border-purple-300 bg-purple-100 text-purple-700 dark:border-purple-500/40 dark:bg-purple-500/20 dark:text-purple-100",
  Done: "border-emerald-300 bg-emerald-100 text-emerald-700 dark:border-emerald-500/40 dark:bg-emerald-500/20 dark:text-emerald-100",
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

const daysUntil = (value: string) => {
  const now = new Date();
  const target = new Date(value);
  const diffMs = target.getTime() - now.getTime();
  return Math.ceil(diffMs / (1000 * 60 * 60 * 24));
};

const getInitials = (value: string) =>
  value
    .split(" ")
    .filter(Boolean)
    .map((part) => part[0] ?? "")
    .join("")
    .slice(0, 2)
    .toUpperCase();

// Helper function to get task properties safely for both TaskSummary and TaskResponse
const getTaskProperties = (task: TaskSummary | TaskResponse) => {
  const isTaskSummary = 'key' in task && 'collaborators' in task;
  
  return {
    id: task.id,
    title: task.title,
    description: task.description || '',
    status: isTaskSummary ? task.status : mapBackendStatus((task as TaskResponse).status),
    key: isTaskSummary ? (task as TaskSummary).key : `TASK-${task.id}`,
    priority: isTaskSummary ? (task as TaskSummary).priority : 'Medium' as TaskPriority,
    owner: isTaskSummary ? (task as TaskSummary).owner : `User ${(task as TaskResponse).ownerId}`,
    collaborators: isTaskSummary ? (task as TaskSummary).collaborators : [],
    dueDate: isTaskSummary ? (task as TaskSummary).dueDate : (task as TaskResponse).createdAt,
    lastUpdated: isTaskSummary ? (task as TaskSummary).lastUpdated : ((task as TaskResponse).updatedAt || (task as TaskResponse).createdAt),
    attachments: isTaskSummary ? (task as TaskSummary).attachments : 0,
    project: isTaskSummary ? (task as TaskSummary).project : ((task as TaskResponse).projectId ? `Project ${(task as TaskResponse).projectId}` : 'Personal Task'),
    subtasks: 'subtasks' in task && task.subtasks ? task.subtasks : [],
    isTaskSummary
  };
};

const getSubtaskSummary = (task: TaskSummary | TaskResponse) => {
  const { subtasks } = getTaskProperties(task);
  const total = subtasks.length;
  
  // Map backend status to frontend status for counting
  const done = subtasks.filter((subtask) => {
    if ('details' in subtask) {
      // TaskResponse subtask (SubtaskResponse)
      const mappedStatus = mapBackendStatus(subtask.status as string);
      return mappedStatus === "Done";
    } else {
      // TaskSummary subtask
      return subtask.status === "Done";
    }
  }).length;
  
  const progress = total > 0 ? Math.round((done / total) * 100) : 0;

  return { total, done, progress };
};

type TaskBoardCardProps = {
  task: TaskSummary | TaskResponse;
};

const TaskBoardCard = ({ task }: TaskBoardCardProps) => {
  const { total, done, progress } = getSubtaskSummary(task);
  const taskProps = getTaskProperties(task);
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
            {taskProps.collaborators.slice(0, 2).map((name) => (
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
      )}

      {total > 0 && (
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
  task: TaskSummary | TaskResponse;
};

const TaskTableRow = ({ task }: TaskTableRowProps) => {
  const { total, done, progress } = getSubtaskSummary(task);
  const taskProps = getTaskProperties(task);
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
          {taskProps.collaborators.slice(0, 3).map((name) => (
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
  const [selectedStatus, setSelectedStatus] =
    useState<(typeof STATUS_FILTERS)[number]>("All");
  const [selectedTaskType, setSelectedTaskType] = useState<string>("All Tasks");
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showCreateDialog, setShowCreateDialog] = useState(false);

  useEffect(() => {
    const loadTasks = async () => {
      try {
        setLoading(true);
        // For now, we'll use a dummy user ID. In a real app, this would come from auth context
        const userId = 1;
        
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
        // Fall back to demo data on error
        setTasks([]);
      } finally {
        setLoading(false);
      }
    };

    loadTasks();
  }, [selectedTaskType]);

  const handleTaskCreated = (newTask: TaskResponse) => {
    // Add the new task to the current tasks list
    setTasks(prevTasks => [newTask, ...prevTasks]);
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

  const sortedTasks = useMemo(() => {
    return [...tasks].sort(
      (a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime(),
    );
  }, [tasks]);

  const tasksByStatus = useMemo(() => {
    const grouped = statusOrder.reduce<Record<TaskStatus, (TaskResponse | TaskSummary)[]>>(
      (acc, status) => {
        acc[status] = [];
        return acc;
      },
      {} as Record<TaskStatus, (TaskResponse | TaskSummary)[]>,
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
    let tasksToFilter: (TaskSummary | TaskResponse)[] = sortedTasks;
    
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

          {/* Status Filter */}
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
              <div className="w-full overflow-x-auto">
                <div className="flex gap-3 h-full min-h-[calc(100vh-400px)] pb-4" style={{ minWidth: `${boardStatuses.length * 280}px` }}>
                  {boardStatuses.map((status) => {
                    const tasks = tasksByStatus[status];

                  return (
                    <div
                      key={status}
                      className="flex flex-col rounded-lg border bg-card h-full w-72 flex-shrink-0"
                      data-testid={`board-column-${status.toLowerCase().replace(/\s+/g, "-")}`}
                    >
                      <div className="flex items-center justify-between border-b px-3 py-2.5">
                        <div>
                          <p className="text-sm font-semibold">{status}</p>
                          <p className="text-muted-foreground text-xs">
                            {tasks.length} card{tasks.length === 1 ? "" : "s"}
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
                      <div className="flex flex-1 flex-col gap-2 overflow-y-auto px-2.5 py-2.5">
                        {tasks.length ? (
                          tasks.map((task) => (
                            <TaskCard key={task.id} task={task} variant="board" />
                          ))
                        ) : (
                          <div className="rounded-lg border border-dashed border-border/70 p-3 text-center text-xs text-muted-foreground">
                            No cards in this column yet.
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
                </div>
              </div>
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
                  <TaskCard key={task.id} task={task} variant="table" />
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
      />
    </SidebarInset>
  );
}
