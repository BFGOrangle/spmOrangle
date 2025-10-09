"use client";

import { useState, useEffect, useMemo } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Briefcase, Calendar, CheckCircle, Circle, Plus, Filter } from "lucide-react";
import { ProjectResponse, TaskResponse } from "@/services/project-service";
import { projectService } from "@/services/project-service";
import FullPageSpinnerLoader from "@/components/full-page-spinner-loader";
import { ErrorMessageCallout } from "@/components/error-message-callout";
import { useCurrentUser } from "@/contexts/user-context";
import { 
  DropdownMenu, 
  DropdownMenuContent, 
  DropdownMenuItem, 
  DropdownMenuTrigger 
} from "@/components/ui/dropdown-menu";

interface ProjectsListProps {
  onProjectSelect: (projectId: number) => void;
}

interface ProjectCardProps {
  project: ProjectResponse;
  onProjectClick: (projectId: number) => void;
  isViewOnly?: boolean;
}

function ProjectCard({ project, onProjectClick, isViewOnly = false }: ProjectCardProps) {
  const completionPercentage = project.taskCount > 0 
    ? Math.round((project.completedTaskCount / project.taskCount) * 100) 
    : 0;

  return (
    <Card 
      className="cursor-pointer hover:shadow-lg transition-shadow duration-200 h-full"
      onClick={() => onProjectClick(project.id)}
    >
      <CardHeader className="pb-4">
        <div className="flex items-start justify-between gap-4">
          <div className="flex flex-col gap-1">
            <div className="flex flex-wrap items-center gap-2">
              <Briefcase className="h-5 w-5 text-blue-600" />
              <CardTitle className="text-lg line-clamp-1">{project.name}</CardTitle>
              {isViewOnly && (
                <Badge
                  variant="outline"
                  className="border-amber-500/60 bg-amber-50/80 text-[10px] font-semibold uppercase tracking-wide text-amber-600"
                >
                  View only
                </Badge>
              )}
            </div>
          </div>
          <Badge variant={completionPercentage === 100 ? "default" : "secondary"}>
            {completionPercentage}%
          </Badge>
        </div>
        {project.description && (
          <CardDescription className="line-clamp-2 mt-2">
            {project.description}
          </CardDescription>
        )}
      </CardHeader>
      
      <CardContent className="pt-0">
        <div className="space-y-3">
          {/* Task Progress */}
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="font-medium">Progress</span>
              <span className="text-muted-foreground">
                {project.completedTaskCount} / {project.taskCount} tasks
              </span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2">
              <div 
                className="bg-blue-600 h-2 rounded-full transition-all duration-300"
                style={{ width: `${completionPercentage}%` }}
              />
            </div>
          </div>

          {/* Task Status Indicators */}
          <div className="flex items-center gap-4 text-sm text-muted-foreground">
            <div className="flex items-center gap-1">
              <CheckCircle className="h-4 w-4 text-green-600" />
              <span>{project.completedTaskCount} completed</span>
            </div>
            <div className="flex items-center gap-1">
              <Circle className="h-4 w-4 text-gray-400" />
              <span>{project.taskCount - project.completedTaskCount} remaining</span>
            </div>
          </div>

          {/* Created Date */}
          <div className="flex items-center gap-1 text-sm text-muted-foreground pt-2 border-t">
            <Calendar className="h-4 w-4" />
            <span>
              Created {new Date(project.createdAt).toLocaleDateString()}
            </span>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

export function ProjectsList({ onProjectSelect }: ProjectsListProps) {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filterOwner, setFilterOwner] = useState<"all" | "owned">("all");
  const [relatedTasks, setRelatedTasks] = useState<TaskResponse[]>([]);
  const [isRelatedLoading, setIsRelatedLoading] = useState(true);
  const [relatedError, setRelatedError] = useState<string | null>(null);
  const { currentUser } = useCurrentUser();

  useEffect(() => {
    loadProjects();
  }, []);

  const loadProjects = async () => {
    setIsLoading(true);
    setIsRelatedLoading(true);
    setError(null);
    setRelatedError(null);

    const [projectsResult, relatedTasksResult] = await Promise.allSettled([
      projectService.getUserProjects(0), // Using dummy userId, will be handled by authenticated client
      projectService.getRelatedProjectTasks(),
    ]);

    if (projectsResult.status === "fulfilled") {
      setProjects(projectsResult.value);
    } else {
      const cause = projectsResult.reason;
      console.error("Error loading projects:", cause);
      setProjects([]);
      setError(cause instanceof Error ? cause.message : "Failed to load projects");
    }

    if (relatedTasksResult.status === "fulfilled") {
      const projectScopedTasks = relatedTasksResult.value.filter(
        (task): task is TaskResponse & { projectId: number } => typeof task.projectId === "number",
      );
      setRelatedTasks(projectScopedTasks);
    } else {
      const cause = relatedTasksResult.reason;
      console.error("Error loading related project tasks:", cause);
      setRelatedTasks([]);
      setRelatedError(cause instanceof Error ? cause.message : "Failed to load related projects");
    }

    setIsLoading(false);
    setIsRelatedLoading(false);
  };

  // Filter projects based on ownership
  const filteredProjects = useMemo(() => {
    if (filterOwner === "all") {
      return projects;
    }
    
    // Filter for projects owned by current user
    if (!currentUser?.backendStaffId) {
      return projects; // Fallback to showing all if no user ID
    }
    
    return projects.filter(project => project.ownerId === currentUser.backendStaffId);
  }, [projects, filterOwner, currentUser?.backendStaffId]);

  const relatedProjects = useMemo(() => {
    if (relatedTasks.length === 0) {
      return [] as ProjectResponse[];
    }

    const relatedProjectIds = new Set<number>();
    for (const task of relatedTasks) {
      if (typeof task.projectId === "number") {
        relatedProjectIds.add(task.projectId);
      }
    }

    return projects.filter((project) => relatedProjectIds.has(project.id));
  }, [projects, relatedTasks]);

  if (isLoading) {
    return <FullPageSpinnerLoader />;
  }

  if (error) {
    return (
      <div className="flex flex-1 flex-col gap-6 p-6 pb-12 lg:p-10">
        <ErrorMessageCallout errorMessage={error} />
        <div className="mt-4">
          <Button onClick={loadProjects} variant="outline">
            Try Again
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-1 flex-col gap-6 p-6 pb-12 lg:p-10">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">
            {filterOwner === "owned" ? "My Projects" : "All Projects"}
          </h1>
          <p className="text-muted-foreground mt-1">
            {filterOwner === "owned" 
              ? `Projects you own (${filteredProjects.length})`
              : `All accessible projects (${filteredProjects.length})`
            }
          </p>
        </div>
        <div className="flex items-center gap-2">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" size="sm">
                <Filter className="h-4 w-4 mr-2" />
                {filterOwner === "owned" ? "My Projects" : "All Projects"}
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={() => setFilterOwner("all")}>
                All Projects
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setFilterOwner("owned")}>
                My Projects
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <Button onClick={() => {/* TODO: Add create project functionality */}}>
            <Plus className="h-4 w-4 mr-2" />
            New Project
          </Button>
        </div>
      </div>

      {filteredProjects.length === 0 ? (
        <div className="text-center py-12">
          <Briefcase className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-semibold mb-2">
            {filterOwner === "owned" ? "No projects owned" : "No projects yet"}
          </h3>
          <p className="text-muted-foreground mb-4">
            {filterOwner === "owned" 
              ? "You don't own any projects yet. Try switching to 'All Projects' or create a new one." 
              : "Create your first project to get started with task management"
            }
          </p>
          <Button onClick={() => {/* TODO: Add create project functionality */}}>
            <Plus className="h-4 w-4 mr-2" />
            Create Project
          </Button>
        </div>
      ) : (
        <div
          className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6"
          data-testid="projects-grid"
        >
          {filteredProjects.map((project) => (
            <ProjectCard
              key={project.id}
              project={project}
              onProjectClick={onProjectSelect}
            />
          ))}
        </div>
      )}

      <section className="mt-10">
        <div className="flex flex-col gap-1">
          <h2 className="text-2xl font-semibold">Related Projects</h2>
          <p className="text-sm text-muted-foreground">
            Projects that include collaborators from your department where you have view-only access
          </p>
        </div>

        <div className="mt-4">
          {isRelatedLoading ? (
            <div className="rounded-lg border border-dashed border-muted p-6 text-center text-muted-foreground">
              Loading related projects...
            </div>
          ) : relatedError ? (
            <div className="space-y-4">
              <ErrorMessageCallout errorMessage={relatedError} />
              <div>
                <Button onClick={loadProjects} variant="outline" size="sm">
                  Retry loading related projects
                </Button>
              </div>
            </div>
          ) : relatedProjects.length === 0 ? (
            <div
              className="rounded-lg border border-dashed border-muted p-6 text-center text-muted-foreground"
              data-testid="related-projects-empty"
            >
              No related projects to show yet.
            </div>
          ) : (
            <div
              className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3"
              data-testid="related-projects-grid"
            >
              {relatedProjects.map((project) => (
                <ProjectCard
                  key={`related-${project.id}`}
                  project={project}
                  onProjectClick={onProjectSelect}
                  isViewOnly
                />
              ))}
            </div>
          )}
        </div>
      </section>
    </div>
  );
}
