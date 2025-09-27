"use client";

import { useState } from "react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import {
  CalendarDays,
  Clock4,
  MoreHorizontal,
  Paperclip,
  Users as UsersIcon,
} from "lucide-react";
import { TaskSummary, TaskPriority, TaskStatus } from "@/lib/mvp-data";
import { TaskResponse, SubtaskResponse } from "@/services/project-service";
import { SubtaskList } from "./subtask-list";
import { TaskCollaboratorManagement } from "./task-collaborator-management";

// Status and priority styles (moved from tasks page)
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

// Helper functions
const formatDate = (value: string) =>
  new Date(value).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  });

const getInitials = (value: string) =>
  value
    .split(" ")
    .filter(Boolean)
    .map((part) => part[0] ?? "")
    .join("")
    .slice(0, 2)
    .toUpperCase();

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
    projectId: isTaskSummary ? null : (task as TaskResponse).projectId,
    isTaskSummary
  };
};

interface TaskCardProps {
  task: TaskSummary | TaskResponse;
  variant?: 'board' | 'table';
  onSubtaskUpdated?: (taskId: number | string, subtasks: SubtaskResponse[]) => void;
  currentUserId?: number;
}

export function TaskCard({ task, variant = 'board', onSubtaskUpdated, currentUserId = 1 }: TaskCardProps) {
  const taskProps = getTaskProperties(task);
  const [showDetails, setShowDetails] = useState(false);
  const [subtasks, setSubtasks] = useState<SubtaskResponse[]>(taskProps.subtasks as SubtaskResponse[]);
  // For now, convert collaborator names to IDs (mock mapping)
  const [collaboratorIds, setCollaboratorIds] = useState<number[]>(() => {
    // Convert existing collaborator names to mock IDs for demo
    return taskProps.collaborators.map((_, index) => index + 2); // Start from ID 2
  });

  const handleSubtaskCreated = (newSubtask: SubtaskResponse) => {
    const updatedSubtasks = [...subtasks, newSubtask];
    setSubtasks(updatedSubtasks);
    onSubtaskUpdated?.(taskProps.id, updatedSubtasks);
  };

  const handleSubtaskUpdated = (updatedSubtask: SubtaskResponse) => {
    const updatedSubtasks = subtasks.map(st => 
      st.id === updatedSubtask.id ? updatedSubtask : st
    );
    setSubtasks(updatedSubtasks);
    onSubtaskUpdated?.(taskProps.id, updatedSubtasks);
  };

  const getSubtaskSummary = () => {
    const total = subtasks.length;
    const done = subtasks.filter((subtask) => subtask.status === 'COMPLETED').length;
    const progress = total > 0 ? Math.round((done / total) * 100) : 0;
    return { total, done, progress };
  };

  if (variant === 'table') {
    return <TaskTableCard task={task} />;
  }

  const { total, done, progress } = getSubtaskSummary();
  const collaboratorOverflow = Math.max(collaboratorIds.length - 2, 0);

  return (
    <Card className="hover:shadow-md transition-shadow">
      <CardHeader className="pb-2">
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

        {collaboratorIds.length > 0 && (
          <div className="flex items-center gap-2 mb-2 text-xs text-muted-foreground">
            <UsersIcon className="h-3 w-3" aria-hidden="true" />
            <div className="flex items-center -space-x-1">
              {collaboratorIds.slice(0, 2).map((id) => (
                <span
                  key={id}
                  className="flex h-5 w-5 items-center justify-center rounded-full border border-background bg-secondary text-[0.6rem] font-semibold text-secondary-foreground shadow-sm"
                >
                  {id}
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
      </CardHeader>

      <CardContent className="pt-0">
        {total > 0 && (
          <div className="space-y-1 mb-3">
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

        <div className="flex items-center justify-between">
          {taskProps.attachments > 0 && (
            <div className="flex items-center gap-1 text-xs text-muted-foreground">
              <Paperclip className="h-3 w-3" aria-hidden="true" />
              <span>{taskProps.attachments}</span>
            </div>
          )}

          <Dialog open={showDetails} onOpenChange={setShowDetails}>
            <DialogTrigger asChild>
              <Button variant="ghost" size="sm">
                View Details
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl">
              <DialogHeader>
                <DialogTitle>{taskProps.title}</DialogTitle>
                <DialogDescription>
                  {taskProps.key} • {taskProps.project}
                </DialogDescription>
              </DialogHeader>
              
              <div className="space-y-4">
                {taskProps.description && (
                  <div>
                    <h4 className="text-sm font-medium mb-2">Description</h4>
                    <p className="text-sm text-muted-foreground">{taskProps.description}</p>
                  </div>
                )}

                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div>
                    <span className="font-medium">Status:</span>
                    <Badge className={`ml-2 ${statusStyles[taskProps.status]} text-xs`}>
                      {taskProps.status}
                    </Badge>
                  </div>
                  <div>
                    <span className="font-medium">Priority:</span>
                    <Badge variant="outline" className={`ml-2 ${priorityStyles[taskProps.priority].badge} text-xs`}>
                      {taskProps.priority}
                    </Badge>
                  </div>
                  <div>
                    <span className="font-medium">Owner:</span>
                    <span className="ml-2">{taskProps.owner}</span>
                  </div>
                  <div>
                    <span className="font-medium">Due:</span>
                    <span className="ml-2">{formatDate(taskProps.dueDate)}</span>
                  </div>
                </div>

                <div className="border-t pt-4">
                  <div className="flex items-center justify-between mb-3">
                    <h4 className="text-sm font-medium">Collaborators</h4>
                    <TaskCollaboratorManagement
                      taskId={Number(taskProps.id)}
                      currentCollaboratorIds={collaboratorIds}
                      currentUserId={currentUserId}
                      onCollaboratorsChange={setCollaboratorIds}
                    />
                  </div>

                  {collaboratorIds.length > 0 ? (
                    <div className="flex flex-wrap gap-2">
                      {collaboratorIds.map((collaboratorId) => (
                        <div key={collaboratorId} className="flex items-center gap-2 px-3 py-1 bg-secondary rounded-full">
                          <div className="flex h-6 w-6 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary">
                            {collaboratorId}
                          </div>
                          <span className="text-sm">User ID: {collaboratorId}</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-sm text-muted-foreground">No collaborators assigned</p>
                  )}
                </div>

                {!taskProps.isTaskSummary && taskProps.projectId && (
                  <div className="border-t pt-4">
                    <SubtaskList
                      taskId={Number(taskProps.id)}
                      projectId={taskProps.projectId}
                      subtasks={subtasks}
                      onSubtaskCreated={handleSubtaskCreated}
                      onSubtaskUpdated={handleSubtaskUpdated}
                    />
                  </div>
                )}
              </div>
            </DialogContent>
          </Dialog>
        </div>
      </CardContent>
    </Card>
  );
}

// Table variant component (simplified version of the existing TaskTableRow)
function TaskTableCard({ task }: { task: TaskSummary | TaskResponse }) {
  const taskProps = getTaskProperties(task);
  const subtasks = taskProps.subtasks as SubtaskResponse[];
  const total = subtasks.length;
  const done = subtasks.filter((subtask) => subtask.status === 'COMPLETED').length;
  const progress = total > 0 ? Math.round((done / total) * 100) : 0;
  const collaboratorOverflow = Math.max(taskProps.collaborators.length - 3, 0);

  return (
    <div
      className="grid gap-3 py-4 transition hover:bg-accent/50 hover:text-accent-foreground sm:grid-cols-[minmax(240px,1.6fr)_minmax(160px,1fr)_minmax(160px,1.1fr)_minmax(140px,0.9fr)_minmax(140px,0.9fr)_minmax(140px,0.8fr)] sm:py-5"
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
          {formatDate(taskProps.lastUpdated)}
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
}
