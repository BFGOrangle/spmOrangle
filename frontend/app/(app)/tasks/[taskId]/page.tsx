"use client";

import { use, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";
import {
  CalendarDays,
  ArrowLeft,
  Edit,
  Trash2,
  Users as UsersIcon,
} from "lucide-react";
import { projectService, TaskResponse, SubtaskResponse } from "@/services/project-service";
import { SubtaskList } from "@/components/subtask-list";
import { CommentSection } from "@/components/comment-section";
import { fileService, FileResponse } from "@/services/file-service";
import { FileList } from "@/components/file-icon";
import { TaskUpdateDialog } from "@/components/task-update-dialog";
import { useCurrentUser } from "@/contexts/user-context";
import {
  type TaskPriority,
  type TaskStatus,
} from "@/lib/mvp-data";

// Status and priority styles
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
    year: "numeric",
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

export default function TaskDetailPage({ params }: { params: Promise<{ taskId: string }> }) {
  const resolvedParams = use(params);
  const taskId = parseInt(resolvedParams.taskId);
  const router = useRouter();
  const { currentUser } = useCurrentUser();
  const [task, setTask] = useState<TaskResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [files, setFiles] = useState<FileResponse[]>([]);
  const [isLoadingFiles, setIsLoadingFiles] = useState(false);
  const [showUpdateDialog, setShowUpdateDialog] = useState(false);
  const [subtasks, setSubtasks] = useState<SubtaskResponse[]>([]);

  useEffect(() => {
    const loadTask = async () => {
      if (!currentUser) return;

      try {
        setLoading(true);
        const userId = currentUser.backendStaffId || 1;

        // Fetch all user tasks and find the one we need
        const allTasks = await projectService.getAllUserTasks(userId);
        const foundTask = allTasks.find(t => t.id === taskId);

        if (!foundTask) {
          setError("Task not found");
          return;
        }

        setTask(foundTask);
        setSubtasks(foundTask.subtasks || []);
      } catch (err) {
        console.error('Error loading task:', err);
        setError("Failed to load task");
      } finally {
        setLoading(false);
      }
    };

    loadTask();
  }, [taskId, currentUser]);

  useEffect(() => {
    const fetchFiles = async () => {
      if (!task || !task.projectId) return;

      setIsLoadingFiles(true);
      try {
        const fetchedFiles = await fileService.getFilesByTaskAndProject(
          task.id,
          task.projectId
        );
        setFiles(fetchedFiles);
      } catch (error) {
        console.error("Error fetching files:", error);
        setFiles([]);
      } finally {
        setIsLoadingFiles(false);
      }
    };

    fetchFiles();
  }, [task]);

  const handleTaskUpdated = (updatedTask: TaskResponse) => {
    setTask(updatedTask);
    setSubtasks(updatedTask.subtasks || []);
    setShowUpdateDialog(false);
  };

  const handleDelete = async () => {
    if (!task || !currentUser) return;

    if (!confirm("Are you sure you want to delete this task?")) return;

    try {
      const userId = currentUser.backendStaffId || 1;
      await projectService.deleteTask(task.id, userId);
      router.push('/tasks');
    } catch (error) {
      console.error("Error deleting task:", error);
      alert("Failed to delete task");
    }
  };

  const handleSubtaskCreated = (newSubtask: SubtaskResponse) => {
    setSubtasks([...subtasks, newSubtask]);
  };

  const handleSubtaskUpdated = (updatedSubtask: SubtaskResponse) => {
    setSubtasks(subtasks.map(st =>
      st.id === updatedSubtask.id ? updatedSubtask : st
    ));
  };

  const handleSubtaskDeleted = (subtaskId: number) => {
    setSubtasks(subtasks.filter(st => st.id !== subtaskId));
  };

  if (loading) {
    return (
      <SidebarInset>
        <div className="flex items-center justify-center h-screen">
          <div className="text-center space-y-2">
            <div className="text-lg font-semibold">Loading task...</div>
            <div className="text-muted-foreground">Please wait</div>
          </div>
        </div>
      </SidebarInset>
    );
  }

  if (error || !task) {
    return (
      <SidebarInset>
        <div className="flex items-center justify-center h-screen">
          <div className="text-center space-y-4">
            <div className="text-lg font-semibold text-destructive">{error || "Task not found"}</div>
            <Button onClick={() => router.push('/tasks')}>
              <ArrowLeft className="mr-2 h-4 w-4" />
              Back to Tasks
            </Button>
          </div>
        </div>
      </SidebarInset>
    );
  }

  const status = mapBackendStatus(task.status);
  const priority = mapTaskTypeToPriority(task.taskType);
  const key = `TASK-${task.id}`;

  return (
    <SidebarInset>
      <div className="flex flex-1 flex-col gap-6 p-4 pb-8 lg:p-6">
        <header className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <Button
              variant="ghost"
              size="icon"
              onClick={() => router.push('/tasks')}
            >
              <ArrowLeft className="h-4 w-4" />
            </Button>
            <SidebarTrigger className="-ml-1" />
            <div>
              <div className="flex items-center gap-2 mb-1">
                <h1 className="text-xl font-semibold tracking-tight">{task.title}</h1>
                <Badge
                  variant="outline"
                  className={`border-none px-2 py-0.5 text-xs ${priorityStyles[priority].badge}`}
                >
                  {priority}
                </Badge>
              </div>
              <p className="text-muted-foreground text-sm">
                {key} • {task.projectId ? `Project ${task.projectId}` : 'Personal Task'}
              </p>
            </div>
          </div>
          {task.userHasEditAccess && (
            <div className="flex gap-2">
              <Button size="sm" variant="outline" onClick={() => setShowUpdateDialog(true)}>
                <Edit className="h-4 w-4 mr-2" />
                Edit Task
              </Button>
              <Button size="sm" variant="destructive" onClick={handleDelete}>
                <Trash2 className="h-4 w-4 mr-2" />
                Delete
              </Button>
            </div>
          )}
        </header>

        <div className="grid gap-6 lg:grid-cols-[1fr_300px]">
          <div className="space-y-6">
            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Description</h2>
              </CardHeader>
              <CardContent>
                <p className="text-sm text-muted-foreground whitespace-pre-wrap">
                  {task.description || "No description provided."}
                </p>
              </CardContent>
            </Card>

            {task.projectId && (
              <Card>
                <CardHeader>
                  <h2 className="text-lg font-semibold">Subtasks</h2>
                </CardHeader>
                <CardContent>
                  <SubtaskList
                    taskId={task.id}
                    projectId={task.projectId}
                    subtasks={subtasks}
                    onSubtaskCreated={handleSubtaskCreated}
                    onSubtaskUpdated={handleSubtaskUpdated}
                    onSubtaskDeleted={handleSubtaskDeleted}
                  />
                </CardContent>
              </Card>
            )}

            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Files ({files.length})</h2>
              </CardHeader>
              <CardContent>
                {isLoadingFiles ? (
                  <div className="animate-pulse flex gap-2">
                    <div className="h-8 w-8 bg-muted rounded" />
                    <div className="h-8 w-8 bg-muted rounded" />
                    <div className="h-8 w-8 bg-muted rounded" />
                  </div>
                ) : files.length > 0 ? (
                  <FileList files={files} size="lg" showDownload={true} />
                ) : (
                  <p className="text-sm text-muted-foreground">No files attached to this task.</p>
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <h2 className="text-lg font-semibold">Discussion</h2>
              </CardHeader>
              <CardContent>
                <CommentSection
                  taskId={task.id}
                  projectId={task.projectId || 0}
                  title=""
                  compact={false}
                />
              </CardContent>
            </Card>
          </div>

          <div className="space-y-4">
            <Card>
              <CardHeader>
                <h3 className="text-sm font-semibold">Status</h3>
              </CardHeader>
              <CardContent>
                <Badge className={`${statusStyles[status]} text-sm px-3 py-1`}>
                  {status}
                </Badge>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <h3 className="text-sm font-semibold">Owner</h3>
              </CardHeader>
              <CardContent>
                <div className="flex items-center gap-3">
                  <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-sm font-semibold text-primary">
                    {getInitials(`User ${task.ownerId}`)}
                  </div>
                  <div>
                    <p className="text-sm font-medium">User {task.ownerId}</p>
                    <p className="text-xs text-muted-foreground">Task Owner</p>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <h3 className="text-sm font-semibold">Dates</h3>
              </CardHeader>
              <CardContent className="space-y-3">
                <div>
                  <p className="text-xs text-muted-foreground mb-1">Created</p>
                  <div className="flex items-center gap-2 text-sm">
                    <CalendarDays className="h-4 w-4 text-muted-foreground" />
                    <span>{formatDate(task.createdAt)}</span>
                  </div>
                </div>
                {task.updatedAt && (
                  <div>
                    <p className="text-xs text-muted-foreground mb-1">Last Updated</p>
                    <div className="flex items-center gap-2 text-sm">
                      <CalendarDays className="h-4 w-4 text-muted-foreground" />
                      <span>{formatDate(task.updatedAt)}</span>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>

            {task.tags && task.tags.length > 0 && (
              <Card>
                <CardHeader>
                  <h3 className="text-sm font-semibold">Tags</h3>
                </CardHeader>
                <CardContent>
                  <div className="flex flex-wrap gap-2">
                    {task.tags.map((tag, index) => (
                      <Badge key={index} variant="secondary" className="text-xs">
                        {tag}
                      </Badge>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      </div>

      {showUpdateDialog && (
        <TaskUpdateDialog
          task={task}
          open={showUpdateDialog}
          onOpenChange={setShowUpdateDialog}
          onTaskUpdated={handleTaskUpdated}
        />
      )}
    </SidebarInset>
  );
}