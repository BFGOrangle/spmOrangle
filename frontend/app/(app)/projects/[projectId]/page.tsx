"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import {
  ArrowLeft,
  Clock4,
  MoreHorizontal,
  Plus,
  Users as UsersIcon,
} from "lucide-react";

import { projectService, ProjectResponse, TaskResponse } from "@/services/project-service";
import { TaskCreationDialog } from "@/components/task-creation-dialog";
import { TaskCard } from "@/components/task-card";

// Map backend status to frontend status
const mapBackendStatus = (status: string) => {
  switch (status) {
    case 'TODO': return 'Todo';
    case 'IN_PROGRESS': return 'In Progress';
    case 'BLOCKED': return 'Blocked';
    case 'COMPLETED': return 'Done';
    default: return status;
  }
};

type TaskStatus = "Todo" | "In Progress" | "Blocked" | "Review" | "Done";

const STATUS_FILTERS: (TaskStatus | "All")[] = [
  "All",
  "Todo",
  "In Progress",
  "Blocked",
  "Review",
  "Done",
];

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

const TaskBoardCard = ({ task }: { task: TaskResponse }) => {
  return (
    <TaskCard task={task} variant="board" />
  );
};

const TaskTableRow = ({ task }: { task: TaskResponse }) => {
  return (
    <TaskCard task={task} variant="table" />
  );
};

export default function ProjectTasksPage() {
  const params = useParams();
  const projectId = parseInt(params.projectId as string);
  
  const [project, setProject] = useState<ProjectResponse | null>(null);
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedStatus, setSelectedStatus] = useState<TaskStatus | "All">("All");
  const [selectedAssignee, setSelectedAssignee] = useState<string>("all"); // "all" or ownerId as string
  const [showCreateDialog, setShowCreateDialog] = useState(false);

  useEffect(() => {
    const loadProjectData = async () => {
      try {
        setLoading(true);
        
        // For now, we'll use a dummy user ID. In a real app, this would come from auth context
        const userId = 1;
        
        // Load project details and tasks in parallel
        const [projectsResponse, tasksResponse] = await Promise.all([
          projectService.getUserProjects(userId),
          projectService.getProjectTasks(projectId)
        ]);
        
        const currentProject = projectsResponse.find(p => p.id === projectId);
        if (!currentProject) {
          setError("Project not found");
          return;
        }
        
        setProject(currentProject);
        setTasks(tasksResponse);
      } catch (err) {
        console.error('Error loading project data:', err);
        setError("Failed to load project data");
      } finally {
        setLoading(false);
      }
    };

    if (projectId) {
      loadProjectData();
    }
  }, [projectId]);

  const handleTaskCreated = (newTask: TaskResponse) => {
    // Add the new task to the current tasks list
    setTasks(prevTasks => [newTask, ...prevTasks]);
  };

  const totals = useMemo(() => {
    const taskCount = tasks.length;
    const completedCount = tasks.filter(task => task.status === 'COMPLETED').length;
    const inProgressCount = tasks.filter(task => task.status === 'IN_PROGRESS').length;
    const todoCount = tasks.filter(task => task.status === 'TODO').length;
    const blockedCount = tasks.filter(task => task.status === 'BLOCKED').length;

    return {
      total: taskCount,
      completed: completedCount,
      inProgress: inProgressCount,
      todo: todoCount,
      blocked: blockedCount,
    };
  }, [tasks]);

  // Build unique assignee list from tasks
  const assignees = useMemo(() => {
    const unique = Array.from(new Set(tasks.map(t => t.ownerId)));
    return unique.sort((a, b) => a - b);
  }, [tasks]);

  // Assignee-first filtering for shared board/list
  const assigneeFilteredTasks = useMemo(() => {
    if (selectedAssignee === "all") return tasks;
    const ownerId = Number(selectedAssignee);
    if (Number.isNaN(ownerId)) return tasks;
    return tasks.filter(t => t.ownerId === ownerId);
  }, [tasks, selectedAssignee]);

  const tasksByStatus = useMemo(() => {
    const grouped = statusOrder.reduce<Record<TaskStatus, TaskResponse[]>>(
      (acc, status) => {
        acc[status] = [];
        return acc;
      },
      {} as Record<TaskStatus, TaskResponse[]>,
    );

    assigneeFilteredTasks.forEach((task) => {
      const mappedStatus = mapBackendStatus(task.status) as TaskStatus;
      if (grouped[mappedStatus]) {
        grouped[mappedStatus].push(task);
      }
    });

    return grouped;
  }, [assigneeFilteredTasks]);

  const boardStatuses: TaskStatus[] =
    selectedStatus === "All" ? statusOrder : [selectedStatus as TaskStatus];

  const filteredTasks = useMemo(() => {
    const base = assigneeFilteredTasks;
    if (selectedStatus === "All") {
      return base;
    }
    const backendStatus = selectedStatus === "Todo" ? "TODO" :
                         selectedStatus === "In Progress" ? "IN_PROGRESS" :
                         selectedStatus === "Done" ? "COMPLETED" :
                         selectedStatus === "Blocked" ? "BLOCKED" : "TODO";
    return base.filter(task => task.status === backendStatus);
  }, [selectedStatus, assigneeFilteredTasks]);

  if (loading) {
    return (
      <SidebarInset>
        <div className="flex h-full items-center justify-center">
          <div className="text-center">
            <div className="text-lg font-semibold">Loading project...</div>
          </div>
        </div>
      </SidebarInset>
    );
  }

  if (error || !project) {
    return (
      <SidebarInset>
        <div className="flex h-full items-center justify-center">
          <div className="text-center">
            <div className="text-lg font-semibold text-destructive">
              {error || "Project not found"}
            </div>
            <Link href="/projects">
              <Button variant="outline" className="mt-4">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Back to Projects
              </Button>
            </Link>
          </div>
        </div>
      </SidebarInset>
    );
  }

  return (
    <SidebarInset>
      <header className="flex h-16 shrink-0 items-center gap-2 transition-[width,height] ease-linear group-has-[[data-collapsible=icon]]/sidebar-wrapper:h-12">
        <div className="flex items-center gap-2 px-4">
          <SidebarTrigger className="-ml-1" />
          <Separator orientation="vertical" className="mr-2 h-4" />
          <Link href="/projects">
            <Button variant="ghost" size="sm">
              <ArrowLeft className="h-4 w-4 mr-2" />
              Projects
            </Button>
          </Link>
          <Separator orientation="vertical" className="h-4" />
          <h1 className="text-lg font-semibold">{project.name}</h1>
        </div>
      </header>

      <div className="flex flex-1 flex-col gap-4 p-4 pt-0">
        {/* Project Overview */}
        <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Total Tasks</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{totals.total}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Completed</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-emerald-600">{totals.completed}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">In Progress</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-sky-600">{totals.inProgress}</div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium">Blocked</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold text-amber-600">{totals.blocked}</div>
            </CardContent>
          </Card>
        </section>

        {/* Filters */}
        <section className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h2 className="text-lg font-semibold">Project Tasks</h2>
              <p className="text-muted-foreground text-sm">
                {project.description || "Manage tasks for this project"}
              </p>
            </div>
            <Button onClick={() => setShowCreateDialog(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Add Task
            </Button>
          </div>

          <div className="flex flex-col gap-3 md:flex-row md:items-center md:gap-4">
            <div className="flex flex-wrap gap-2">
              {STATUS_FILTERS.map((status) => (
                <Button
                  key={status}
                  variant={selectedStatus === status ? "default" : "outline"}
                  size="sm"
                  onClick={() => setSelectedStatus(status)}
                >
                  {status}
                </Button>
              ))}
            </div>
            <div className="flex items-center gap-2">
              <Label className="text-xs text-muted-foreground">Assignee</Label>
              <Select value={selectedAssignee} onValueChange={setSelectedAssignee}>
                <SelectTrigger className="h-8 w-[200px]">
                  <SelectValue placeholder="All assignees" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All assignees</SelectItem>
                  {assignees.map(id => (
                    <SelectItem key={id} value={String(id)}>User {id}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </section>

        {/* Kanban Board */}
        <section className="space-y-4">
          <div>
            <h2 className="text-lg font-semibold">Board view</h2>
            <p className="text-muted-foreground text-sm">
              Drag and drop tasks between columns to update their status.
            </p>
          </div>

          <div className="w-full overflow-x-auto">
            <div className="flex gap-4 pb-4" style={{ minWidth: `${boardStatuses.length * 280}px` }}>
              {boardStatuses.map((status) => {
                const tasks = tasksByStatus[status];
                const count = tasks.length;

                return (
                  <div
                    key={status}
                    className="flex min-h-[400px] w-72 flex-shrink-0 flex-col rounded-lg border bg-muted/50"
                  >
                    <div className="flex items-center justify-between p-4 pb-2">
                      <div className="flex items-center gap-2">
                        <h3 className="font-semibold">{status}</h3>
                        <Badge variant="secondary" className="text-xs">
                          {count}
                        </Badge>
                      </div>
                    </div>

                    <div className="flex flex-1 flex-col gap-2 overflow-y-auto px-2.5 py-2.5">
                      {tasks.length ? (
                        tasks.map((task) => (
                          <TaskBoardCard key={task.id} task={task} />
                        ))
                      ) : (
                        <div className="rounded-lg border border-dashed border-border/70 p-3 text-center text-xs text-muted-foreground">
                          No tasks in this column yet.
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </section>

        {/* Detailed List */}
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
                <span>Type</span>
                <span>Created</span>
              </div>
            </CardHeader>
            <Separator className="mx-6" />
            <CardContent className="divide-border flex flex-col divide-y">
              {filteredTasks.length > 0 ? (
                filteredTasks.map((task) => (
                  <TaskTableRow key={task.id} task={task} />
                ))
              ) : (
                <div className="py-8 text-center text-muted-foreground">
                  No tasks found for the selected filter.
                </div>
              )}
            </CardContent>
          </Card>
        </section>
      </div>

      <TaskCreationDialog
        open={showCreateDialog}
        onOpenChange={setShowCreateDialog}
        projectId={projectId}
        onTaskCreated={handleTaskCreated}
      />
    </SidebarInset>
  );
}
