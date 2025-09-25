"use client";

import { useEffect, useState } from "react";
import Link from "next/link";

import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { type ProjectStatus } from "@/lib/mvp-data";
import { projectService, ProjectResponse } from "@/services/project-service";
import { ArrowRight, Plus } from "lucide-react";

const statusTone: Record<ProjectStatus, string> = {
  Active:
    "border-emerald-200 bg-emerald-100 text-emerald-700 dark:border-emerald-500/40 dark:bg-emerald-500/15 dark:text-emerald-200",
  Planning:
    "border-blue-200 bg-blue-50 text-blue-600 dark:border-blue-500/40 dark:bg-blue-500/15 dark:text-blue-200",
  "At Risk":
    "border-amber-300 bg-amber-100 text-amber-700 dark:border-amber-500/40 dark:bg-amber-500/15 dark:text-amber-200",
  Completed:
    "border-slate-200 bg-slate-100 text-slate-700 dark:border-slate-500/40 dark:bg-slate-500/15 dark:text-slate-200",
};

const formatDate = (value: string) =>
  new Date(value).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
    year: "numeric",
  });

export default function ProjectsPage() {
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadProjects = async () => {
      try {
        setLoading(true);
        // TODO: REPLACE WITH AUTH CONTEXT USER ID?
        const userId = 1;
        const projectsData = await projectService.getUserProjects(userId);
        setProjects(projectsData);
      } catch (err) {
        console.error('Error loading projects:', err);
        setError("Failed to load projects");
      } finally {
        setLoading(false);
      }
    };

    loadProjects();
  }, []);

  // Use only real projects - no fallback to demo data
  const displayProjects = projects;
  
  const totals = displayProjects.reduce(
    (acc, project) => {
      // For real projects, we'll need to map status differently
      const status = 'status' in project ? project.status : 'Active';
      const progress = 'progress' in project ? project.progress : 
                      project.completedTaskCount && project.taskCount ? 
                      Math.round((project.completedTaskCount / project.taskCount) * 100) : 0;
      const tasksCompleted = 'tasksCompleted' in project ? project.tasksCompleted : project.completedTaskCount || 0;
      const tasksTotal = 'tasksTotal' in project ? project.tasksTotal : project.taskCount || 0;
      
      acc[status as ProjectStatus] = (acc[status as ProjectStatus] || 0) + 1;
      acc.progress += progress;
      acc.completed += tasksCompleted;
      acc.total += tasksTotal;
      return acc;
    },
    {
      Active: 0,
      Planning: 0,
      "At Risk": 0,
      Completed: 0,
      progress: 0,
      completed: 0,
      total: 0,
    } as Record<ProjectStatus | "progress" | "completed" | "total", number>,
  );

  const averageProgress = displayProjects.length > 0 ? Math.round(totals.progress / displayProjects.length) : 0;
  const completionRate = totals.total
    ? Math.round((totals.completed / totals.total) * 100)
    : 0;

  return (
    <SidebarInset>
      <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
        <header className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <SidebarTrigger className="-ml-1" />
            <div>
              <h1 className="text-2xl font-semibold tracking-tight">
                Projects
              </h1>
              <p className="text-muted-foreground text-sm">
                A snapshot of where every initiative stands so your team can
                stay in sync.
              </p>
            </div>
          </div>
          <Button size="sm">
            <Plus className="h-4 w-4 mr-2" />
            New Project
          </Button>
        </header>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <Card>
            <CardHeader>
              <CardDescription>Total projects</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {displayProjects.length}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {totals.Completed} completed · {totals["At Risk"]} need attention
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Active delivery</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {totals.Active}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {averageProgress}% avg. progress across active work
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Planning pipeline</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {totals.Planning}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              Upcoming initiatives ready for kickoff
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Task completion</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {completionRate}%
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {totals.completed} of {totals.total} tasks complete
            </CardContent>
          </Card>
        </section>

        <section className="space-y-4">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h2 className="text-lg font-semibold">Active initiatives</h2>
              <p className="text-muted-foreground text-sm">
                Monitor ownership, status, and near-term milestones at a glance.
              </p>
            </div>
            <Button variant="ghost" size="sm" className="text-muted-foreground">
              Export report
            </Button>
          </div>
          <div className="grid gap-4 lg:grid-cols-2">
            {loading ? (
              Array.from({ length: 4 }).map((_, index) => (
                <Card key={index} className="h-full">
                  <CardHeader className="pb-4">
                    <div className="space-y-2">
                      <div className="h-5 bg-muted animate-pulse rounded"></div>
                      <div className="h-4 bg-muted animate-pulse rounded w-3/4"></div>
                    </div>
                  </CardHeader>
                  <Separator className="mx-6" />
                  <CardContent className="space-y-4 pt-4">
                    <div className="h-12 bg-muted animate-pulse rounded"></div>
                  </CardContent>
                </Card>
              ))
            ) : error ? (
              <div className="flex items-center justify-center h-64 col-span-2">
                <div className="text-center space-y-2">
                  <div className="text-lg font-semibold text-destructive">Failed to load projects</div>
                  <div className="text-muted-foreground">Please try refreshing the page</div>
                </div>
              </div>
            ) : displayProjects.length === 0 ? (
              <div className="flex items-center justify-center h-64 col-span-2">
                <div className="text-center space-y-4">
                  <div className="text-lg font-semibold">No projects yet</div>
                  <div className="text-muted-foreground">Create your first project to get started</div>
                  <Button>
                    <Plus className="mr-2 h-4 w-4" />
                    Create Project
                  </Button>
                </div>
              </div>
            ) : displayProjects.map((project) => {
              const isRealProject = 'taskCount' in project;
              const progress = isRealProject ? 
                (project.taskCount > 0 ? Math.round((project.completedTaskCount / project.taskCount) * 100) : 0) :
                project.progress;
              const tasksCompleted = isRealProject ? project.completedTaskCount : project.tasksCompleted;
              const tasksTotal = isRealProject ? project.taskCount : project.tasksTotal;
              const status = isRealProject ? 'Active' : project.status; // Default to Active for real projects
              const dueDate = isRealProject ? project.updatedAt : project.dueDate;
              const owner = isRealProject ? `User ${project.ownerId}` : project.owner;

              return (
                <Card key={project.id} className="h-full group hover:shadow-md transition-shadow cursor-pointer">
                  <Link href={`/projects/${project.id}`}>
                    <CardHeader className="pb-4">
                      <div className="flex items-start justify-between gap-3">
                        <div className="space-y-1 flex-1">
                          <div className="flex items-center gap-2">
                            <CardTitle className="text-lg font-semibold group-hover:text-primary transition-colors">
                              {project.name}
                            </CardTitle>
                            <ArrowRight className="h-4 w-4 opacity-0 group-hover:opacity-100 transition-opacity text-muted-foreground" />
                          </div>
                          <CardDescription className="text-sm">
                            {project.description || "No description available"}
                          </CardDescription>
                        </div>
                        <Badge className={statusTone[status as ProjectStatus]}>
                          {status}
                        </Badge>
                      </div>
                    </CardHeader>
                    <Separator className="mx-6" />
                    <CardContent className="space-y-4 pt-4">
                      <div className="space-y-2">
                        <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">
                          Progress
                        </p>
                        <div className="bg-secondary/60 w-full rounded-full">
                          <div
                            className="bg-primary h-2 rounded-full transition-all"
                            style={{ width: `${progress}%` }}
                          />
                        </div>
                        <div className="flex justify-between text-xs text-muted-foreground">
                          <span>{progress}% complete</span>
                          <span>
                            {tasksCompleted} / {tasksTotal} tasks
                          </span>
                        </div>
                      </div>

                      <div className="grid gap-3 sm:grid-cols-2">
                        <div className="space-y-1 text-sm">
                          <p className="text-muted-foreground text-xs uppercase tracking-wide">
                            Owner
                          </p>
                          <p className="font-medium">{owner}</p>
                        </div>
                        <div className="space-y-1 text-sm">
                          <p className="text-muted-foreground text-xs uppercase tracking-wide">
                            {isRealProject ? "Updated" : "Due date"}
                          </p>
                          <p className="font-medium">
                            {formatDate(dueDate)}
                          </p>
                        </div>
                      </div>
                    </CardContent>
                  </Link>
                </Card>
              );
            })}
          </div>
        </section>
      </div>
    </SidebarInset>
  );
}
