"use client";

import { useState, useEffect } from "react";
import { ArrowLeft, CheckCircle, Circle, Clock, AlertCircle, Plus, Filter } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { TaskResponse } from "@/services/project-service";
import { projectService } from "@/services/project-service";
import FullPageSpinnerLoader from "@/components/full-page-spinner-loader";
import { ErrorMessageCallout } from "@/components/error-message-callout";
import { TaskCreationDialog } from "@/components/task-creation-dialog";

interface ProjectDetailProps {
  projectId: number;
  projectName: string;
  onBackToProjects: () => void;
}

interface TaskCardProps {
  task: TaskResponse;
}

const statusIcons = {
  TODO: Circle,
  IN_PROGRESS: Clock,
  COMPLETED: CheckCircle,
  BLOCKED: AlertCircle,
};

const statusColors = {
  TODO: "text-gray-500",
  IN_PROGRESS: "text-blue-500",
  COMPLETED: "text-green-500",
  BLOCKED: "text-red-500",
};

const taskTypeColors = {
  BUG: "destructive",
  FEATURE: "default",
  CHORE: "secondary",
  RESEARCH: "outline",
} as const;

function TaskCard({ task }: TaskCardProps) {
  const StatusIcon = statusIcons[task.status];
  const subtasksCount = task.subtasks?.length || 0;
  const completedSubtasks = task.subtasks?.filter(s => s.status === "COMPLETED").length || 0;

  return (
    <Card className="hover:shadow-md transition-shadow duration-200">
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div className="flex items-center gap-2 flex-1 min-w-0">
            <StatusIcon className={`h-5 w-5 ${statusColors[task.status]} flex-shrink-0`} />
            <CardTitle className="text-lg line-clamp-2 flex-1">{task.title}</CardTitle>
          </div>
          <div className="flex gap-2 flex-shrink-0 ml-2">
            <Badge variant={taskTypeColors[task.taskType]}>
              {task.taskType}
            </Badge>
            <Badge variant="outline">
              {task.status.replace('_', ' ')}
            </Badge>
          </div>
        </div>
        
        {task.description && (
          <CardDescription className="line-clamp-3 mt-2">
            {task.description}
          </CardDescription>
        )}
      </CardHeader>
      
      <CardContent className="pt-0">
        <div className="space-y-3">
          {/* Tags */}
          {task.tags && task.tags.length > 0 && (
            <div className="flex flex-wrap gap-1">
              {task.tags.map((tag, index) => (
                <Badge key={index} variant="secondary" className="text-xs">
                  {tag}
                </Badge>
              ))}
            </div>
          )}

          {/* Subtasks Progress */}
          {subtasksCount > 0 && (
            <div className="space-y-2">
              <div className="flex items-center justify-between text-sm">
                <span className="font-medium">Subtasks</span>
                <span className="text-muted-foreground">
                  {completedSubtasks} / {subtasksCount}
                </span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-1.5">
                <div 
                  className="bg-blue-600 h-1.5 rounded-full transition-all duration-300"
                  style={{ 
                    width: `${subtasksCount > 0 ? (completedSubtasks / subtasksCount) * 100 : 0}%` 
                  }}
                />
              </div>
            </div>
          )}

          {/* Task Meta */}
          <div className="flex items-center justify-between text-xs text-muted-foreground pt-2 border-t">
            <span>
              Created {new Date(task.createdAt).toLocaleDateString()}
            </span>
            {task.updatedAt && (
              <span>
                Updated {new Date(task.updatedAt).toLocaleDateString()}
              </span>
            )}
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

export function ProjectDetail({ projectId, projectName, onBackToProjects }: ProjectDetailProps) {
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [filteredTasks, setFilteredTasks] = useState<TaskResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [statusFilter, setStatusFilter] = useState<string>("ALL");
  const [typeFilter, setTypeFilter] = useState<string>("ALL");
  const [showTaskDialog, setShowTaskDialog] = useState(false);

  useEffect(() => {
    loadTasks();
  }, [projectId]);

  useEffect(() => {
    applyFilters();
  }, [tasks, statusFilter, typeFilter]);

  const loadTasks = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const projectTasks = await projectService.getProjectTasks(projectId);
      setTasks(projectTasks);
    } catch (err) {
      console.error("Error loading tasks:", err);
      setError(err instanceof Error ? err.message : "Failed to load tasks");
    } finally {
      setIsLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...tasks];

    if (statusFilter !== "ALL") {
      filtered = filtered.filter(task => task.status === statusFilter);
    }

    if (typeFilter !== "ALL") {
      filtered = filtered.filter(task => task.taskType === typeFilter);
    }

    setFilteredTasks(filtered);
  };

  const getTaskCountsByStatus = () => {
    return tasks.reduce((counts, task) => {
      counts[task.status] = (counts[task.status] || 0) + 1;
      return counts;
    }, {} as Record<string, number>);
  };

  const statusCounts = getTaskCountsByStatus();

  const handleTaskCreated = (newTask: TaskResponse) => {
    setTasks(prev => [...prev, newTask]);
  };

  if (isLoading) {
    return <FullPageSpinnerLoader />;
  }

  if (error) {
    return (
      <div className="flex flex-1 flex-col gap-6 p-6 pb-12 lg:p-10">
        <div className="mb-4">
          <Button variant="outline" onClick={onBackToProjects}>
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Projects
          </Button>
        </div>
        <ErrorMessageCallout errorMessage={error} />
        <div className="mt-4">
          <Button onClick={loadTasks} variant="outline">
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-1 flex-col gap-6 p-6 pb-12 lg:p-10">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <Button variant="outline" onClick={onBackToProjects} className="mb-3">
            <ArrowLeft className="h-4 w-4 mr-2" />
            Back to Projects
          </Button>
          <h1 className="text-3xl font-bold">{projectName}</h1>
          <p className="text-muted-foreground mt-1">
            {tasks.length} {tasks.length === 1 ? 'task' : 'tasks'} in this project
          </p>
        </div>
        <Button onClick={() => setShowTaskDialog(true)}>
          <Plus className="h-4 w-4 mr-2" />
          New Task
        </Button>
      </div>

      {/* Status Overview */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        {Object.entries(statusCounts).map(([status, count]) => {
          const StatusIcon = statusIcons[status as keyof typeof statusIcons];
          return (
            <Card key={status}>
              <CardContent className="p-4 text-center">
                <StatusIcon className={`h-6 w-6 mx-auto mb-2 ${statusColors[status as keyof typeof statusColors]}`} />
                <div className="text-2xl font-bold">{count}</div>
                <div className="text-sm text-muted-foreground">
                  {status.replace('_', ' ')}
                </div>
              </CardContent>
            </Card>
          );
        })}
      </div>

      {/* Filters */}
      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2">
          <Filter className="h-4 w-4" />
          <span className="text-sm font-medium">Filters:</span>
        </div>
        <Select value={statusFilter} onValueChange={setStatusFilter}>
          <SelectTrigger className="w-40">
            <SelectValue placeholder="Status" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All Status</SelectItem>
            <SelectItem value="TODO">To Do</SelectItem>
            <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
            <SelectItem value="COMPLETED">Completed</SelectItem>
            <SelectItem value="BLOCKED">Blocked</SelectItem>
          </SelectContent>
        </Select>
        
        <Select value={typeFilter} onValueChange={setTypeFilter}>
          <SelectTrigger className="w-40">
            <SelectValue placeholder="Type" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">All Types</SelectItem>
            <SelectItem value="BUG">Bug</SelectItem>
            <SelectItem value="FEATURE">Feature</SelectItem>
            <SelectItem value="CHORE">Chore</SelectItem>
            <SelectItem value="RESEARCH">Research</SelectItem>
          </SelectContent>
        </Select>

        {(statusFilter !== "ALL" || typeFilter !== "ALL") && (
          <Button 
            variant="outline" 
            size="sm"
            onClick={() => {
              setStatusFilter("ALL");
              setTypeFilter("ALL");
            }}
          >
            Clear Filters
          </Button>
        )}
      </div>

      {/* Tasks List */}
      {filteredTasks.length === 0 ? (
        <div className="text-center py-12">
          <CheckCircle className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2">
            {tasks.length === 0 ? "No tasks yet" : "No tasks match your filters"}
          </h3>
          <p className="text-muted-foreground mb-4">
            {tasks.length === 0 
              ? "Create your first task to get started"
              : "Try adjusting your filters or clearing them"
            }
          </p>
          {tasks.length === 0 && (
            <Button onClick={() => setShowTaskDialog(true)}>
              <Plus className="h-4 w-4 mr-2" />
              Create Task
            </Button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {filteredTasks.map((task) => (
            <TaskCard key={task.id} task={task} />
          ))}
        </div>
      )}

      {/* Task Creation Dialog */}
      <TaskCreationDialog
        open={showTaskDialog}
        onOpenChange={setShowTaskDialog}
        projectId={projectId}
        onTaskCreated={handleTaskCreated}
      />
    </div>
  );
}