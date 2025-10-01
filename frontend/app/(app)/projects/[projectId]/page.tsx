"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
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
import { ScrollArea } from "@/components/ui/scroll-area";
import { DroppableColumn } from "@/components/ui/droppable-column";
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
import { DraggableTaskCard } from "@/components/draggable-task-card";
import { cn } from "@/lib/utils";

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
  "Done",
];

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
  const [activeTask, setActiveTask] = useState<TaskResponse | null>(null);

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
        newStatus = mapBackendStatus(droppedOnTask.status) as TaskStatus;
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

  const handleTaskUpdated = (updatedTask: TaskResponse) => {
    // Update the task in the list
    setTasks(prevTasks =>
      prevTasks.map(task => task.id === updatedTask.id ? updatedTask : task)
    );
  };

  const handleTaskDeleted = (taskId: number) => {
    // Remove the task from the list
    setTasks(prevTasks => prevTasks.filter(task => task.id !== taskId));
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

          <DndContext
            sensors={sensors}
            collisionDetection={collisionDetectionStrategy}
            onDragStart={handleDragStart}
            onDragEnd={handleDragEnd}
          >
            <div className="w-full overflow-x-auto">
              <div className="flex gap-4 pb-4" style={{ minWidth: `${boardStatuses.length * 280}px` }}>
                {boardStatuses.map((status) => {
                  const columnTasks = tasksByStatus[status];
                  const count = columnTasks.length;

                  return (
                    <Card
                      key={status}
                      className={cn("flex min-h-[400px] w-72 flex-shrink-0 flex-col", statusStyles[status])}
                    >
                      <CardHeader className="pb-3 flex-shrink-0">
                        <div className="flex items-center justify-between">
                          <div className="flex items-center gap-2">
                            <h3 className="font-semibold">{status}</h3>
                            <Badge variant="secondary" className="text-xs bg-white/80">
                              {count}
                            </Badge>
                          </div>
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
                                    onTaskDeleted={handleTaskDeleted}
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
