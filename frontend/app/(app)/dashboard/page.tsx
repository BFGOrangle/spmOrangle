"use client";

import { useEffect, useState } from "react";
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
import { LayoutDashboard } from "lucide-react";
import { projectService, ProjectResponse, TaskResponse } from "@/services/project-service";
import { useCurrentUser } from "@/contexts/user-context";

const formatDate = (value: string) =>
  new Date(value).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  });

const statusTone = {
  Active:
    "border-emerald-200 bg-emerald-100 text-emerald-700 dark:border-emerald-500/40 dark:bg-emerald-500/15 dark:text-emerald-200",
  Planning:
    "border-blue-200 bg-blue-50 text-blue-600 dark:border-blue-500/40 dark:bg-blue-500/15 dark:text-blue-200",
  "At Risk":
    "border-amber-300 bg-amber-100 text-amber-700 dark:border-amber-500/40 dark:bg-amber-500/15 dark:text-amber-200",
  Completed:
    "border-slate-200 bg-slate-100 text-slate-700 dark:border-slate-500/40 dark:bg-slate-500/15 dark:text-slate-200",
};

// Map backend task status to frontend status
const mapTaskStatus = (status: string): string => {
  switch (status) {
    case 'TODO': return 'Todo';
    case 'IN_PROGRESS': return 'In Progress';
    case 'BLOCKED': return 'Blocked';
    case 'COMPLETED': return 'Done';
    default: return status;
  }
};

// Map backend task type to priority (simplified mapping)
const mapTaskTypeToPriority = (taskType: string): string => {
  switch (taskType) {
    case 'BUG': return 'High';
    case 'FEATURE': return 'Medium';
    case 'CHORE': return 'Low';
    case 'RESEARCH': return 'Medium';
    default: return 'Medium';
  }
};

export default function Dashboard() {
  const { currentUser, isLoading: userLoading } = useCurrentUser();
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadDashboardData = async () => {
      // Wait for user context to load
      if (userLoading || !currentUser?.backendStaffId) {
        return;
      }

      try {
        setLoading(true);
        const userId = currentUser.backendStaffId;

        const [projectsData, tasksData] = await Promise.all([
          projectService.getUserProjects(userId),
          projectService.getAllUserTasks(userId)
        ]);

        setProjects(projectsData);
        setTasks(tasksData);
      } catch (err) {
        console.error('Error loading dashboard data:', err);
        setError("Failed to load dashboard data");
      } finally {
        setLoading(false);
      }
    };

    loadDashboardData();
  }, [currentUser, userLoading]);

  if (loading || userLoading) {
    return (
      <SidebarInset>
        <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
          <div className="text-center py-16">
            <div className="text-lg font-semibold">Loading dashboard...</div>
            <div className="text-muted-foreground">Please wait while we fetch your data</div>
          </div>
        </div>
      </SidebarInset>
    );
  }

  if (error) {
    return (
      <SidebarInset>
        <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
          <div className="text-center py-16">
            <div className="text-lg font-semibold text-destructive">Failed to load dashboard</div>
            <div className="text-muted-foreground">Please try refreshing the page</div>
          </div>
        </div>
      </SidebarInset>
    );
  }

  // Calculate statistics from real data
  const totalTasks = tasks.length;
  const completedTasks = tasks.filter(task => task.status === 'COMPLETED').length;
  const blockedTasks = tasks.filter(task => task.status === 'BLOCKED').length;
  const activeProjects = projects; // All projects are considered active for now
  const onTrackProjects = projects.length; // Simplified - all projects are on track

  // Get upcoming tasks (non-completed, tasks with due dates first, then by creation date)
  const upcomingTasks = tasks
    .filter(task => task.status !== 'COMPLETED')
    .sort((a, b) => {
      // Tasks with dueDateTime come first
      if (a.dueDateTime && !b.dueDateTime) return -1;
      if (!a.dueDateTime && b.dueDateTime) return 1;

      // If both have dueDateTime, sort by due date
      if (a.dueDateTime && b.dueDateTime) {
        return new Date(a.dueDateTime).getTime() - new Date(b.dueDateTime).getTime();
      }

      // If neither has dueDateTime, sort by creation date
      return new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
    })
    .slice(0, 5);

  // Get high priority tasks (blocked tasks OR priority 8-10 OR bug type)
  const highPriorityTasks = tasks.filter(task => {
    const isBlocked = task.status === 'BLOCKED';
    const isHighPriority = task.priority && task.priority >= 8;
    const isBug = task.taskType === 'BUG';
    return isBlocked || isHighPriority || isBug;
  });

  // Calculate team load based on task owners
  const teamLoad = Object.entries(
    tasks.reduce<Record<number, number>>((acc, task) => {
      acc[task.ownerId] = (acc[task.ownerId] || 0) + 1;
      return acc;
    }, {})
  )
    .sort(([, aCount], [, bCount]) => bCount - aCount)
    .slice(0, 4);

  // Check if current user is STAFF to hide Team Load widget
  const isStaffUser = currentUser?.role === 'STAFF';

  return (
    <SidebarInset>
      <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
        <SidebarTrigger className="-ml-1" />
        <Separator orientation="vertical" className="mr-2 h-4" />
        <div className="flex items-center gap-2">
          <LayoutDashboard className="h-5 w-5" />
          <h1 className="text-lg font-semibold">Dashboard</h1>
        </div>
      </header>

      <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <Card>
            <CardHeader>
              <CardDescription>Active projects</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {activeProjects.length}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {activeProjects.length === 0 ? "No projects yet" : `${onTrackProjects} on track`}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Task completion</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {totalTasks
                  ? Math.round((completedTasks / totalTasks) * 100)
                  : 0}
                %
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {totalTasks === 0 ? "No tasks yet" : `${completedTasks} closed • ${totalTasks - completedTasks} in flight`}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>High priority focus</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {highPriorityTasks.length}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {highPriorityTasks.length === 0 ? "No high priority tasks" : "Critical items requiring updates this week"}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Blocked tasks</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {blockedTasks}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {blockedTasks === 0 ? "No blocked tasks" : "Surface blockers quickly to keep work moving"}
            </CardContent>
          </Card>
        </section>

        <section className="grid gap-4 xl:grid-cols-[1.4fr_1fr]">
          <Card className="h-full">
            <CardHeader className="pb-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <CardTitle className="text-lg font-semibold">
                    Project health
                  </CardTitle>
                  <CardDescription>
                    Status, ownership, and completion trends across your
                    portfolio.
                  </CardDescription>
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  className="text-muted-foreground"
                >
                  View all
                </Button>
              </div>
            </CardHeader>
            <Separator className="mx-6" />
            <CardContent className="flex flex-col divide-y divide-border">
              {activeProjects.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground">
                  No projects to display
                </div>
              ) : (
                activeProjects.map((project) => {
                  const completion = project.taskCount > 0 
                    ? Math.round((project.completedTaskCount / project.taskCount) * 100)
                    : 0;
                  
                  return (
                    <div
                      key={project.id}
                      className="grid gap-3 py-4 sm:grid-cols-[1.2fr_auto_auto]"
                    >
                      <div>
                        <p className="font-medium">{project.name}</p>
                        <p className="text-muted-foreground text-sm">
                          Owner ID: {project.ownerId}
                        </p>
                      </div>
                      <div className="flex flex-col gap-1 text-sm">
                        <span className="text-muted-foreground text-xs uppercase tracking-wide">
                          Status
                        </span>
                        <Badge className={statusTone.Active}>
                          Active
                        </Badge>
                      </div>
                      <div className="flex flex-col gap-1 text-sm">
                        <span className="text-muted-foreground text-xs uppercase tracking-wide">
                          Completion
                        </span>
                        <span className="font-medium">
                          {completion}% · {project.completedTaskCount}/
                          {project.taskCount} tasks
                        </span>
                      </div>
                    </div>
                  );
                })
              )}
            </CardContent>
          </Card>

          <Card className="h-full">
            <CardHeader className="pb-4">
              <div className="flex items-start justify-between gap-3">
                <div>
                  <CardTitle className="text-lg font-semibold">
                    Upcoming commitments
                  </CardTitle>
                  <CardDescription>
                    Due dates and owners so you can unblock ahead of time.
                  </CardDescription>
                </div>
                <Button
                  size="sm"
                  variant="ghost"
                  className="text-muted-foreground"
                >
                  Add reminder
                </Button>
              </div>
            </CardHeader>
            <Separator className="mx-6" />
            <CardContent className="flex flex-col divide-y divide-border">
              {upcomingTasks.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground">
                  No upcoming tasks
                </div>
              ) : (
                upcomingTasks.map((task) => (
                  <div
                    key={task.id}
                    className="flex flex-col gap-2 py-4 sm:flex-row sm:items-center sm:justify-between"
                  >
                    <div className="space-y-1">
                      <p className="font-medium">{task.title}</p>
                      <p className="text-muted-foreground text-sm">
                        {task.projectId ? `Project ${task.projectId}` : 'Personal Task'}
                      </p>
                    </div>
                    <div className="flex flex-wrap items-center gap-2 text-sm">
                      <Badge
                        variant="outline"
                        className="border-primary/40 text-primary"
                      >
                        User {task.ownerId}
                      </Badge>
                      <Badge>{mapTaskStatus(task.status)}</Badge>
                      <span className="font-medium">
                        Updated {formatDate(task.updatedAt || task.createdAt)}
                      </span>
                    </div>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </section>

        <section className={`grid gap-4 ${isStaffUser ? 'xl:grid-cols-1' : 'xl:grid-cols-[1fr_1fr]'}`}>
          {!isStaffUser && (
            <Card>
              <CardHeader className="pb-4">
                <CardTitle className="text-lg font-semibold">Team load</CardTitle>
                <CardDescription>
                  Who is carrying the most tasks right now.
                </CardDescription>
              </CardHeader>
              <Separator className="mx-6" />
              <CardContent className="flex flex-col gap-3 py-4">
                {teamLoad.length === 0 ? (
                  <div className="py-4 text-center text-muted-foreground">
                    No team data available
                  </div>
                ) : (
                  teamLoad.map(([ownerId, count]) => (
                    <div
                      key={ownerId}
                      className="flex items-center justify-between text-sm"
                    >
                      <div className="flex items-center gap-3">
                        <div className="h-8 w-8 rounded-full bg-primary/10 text-center font-medium leading-8 text-primary">
                          U
                        </div>
                        <span className="font-medium">User {ownerId}</span>
                      </div>
                      <span className="text-muted-foreground">{count} tasks</span>
                    </div>
                  ))
                )}
              </CardContent>
            </Card>
          )}

          <Card>
            <CardHeader className="pb-4">
              <CardTitle className="text-lg font-semibold">
                Priority queue
              </CardTitle>
              <CardDescription>
                High-urgency items that warrant immediate attention.
              </CardDescription>
            </CardHeader>
            <Separator className="mx-6" />
            <CardContent className="flex flex-col gap-3 py-4">
              {highPriorityTasks.length === 0 ? (
                <div className="py-4 text-center text-muted-foreground">
                  No high priority tasks
                </div>
              ) : (
                highPriorityTasks.map((task) => (
                  <div
                    key={task.id}
                    className="flex flex-col gap-2 rounded-lg border p-3"
                  >
                    <div className="flex items-center justify-between gap-2">
                      <p className="font-medium">{task.title}</p>
                      <Badge className="border-red-200 bg-red-100 text-red-700 dark:border-red-500/40 dark:bg-red-500/15 dark:text-red-200">
                        {task.taskType}
                      </Badge>
                    </div>
                    <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
                      <span>{task.projectId ? `Project ${task.projectId}` : 'Personal Task'}</span>
                      <span>•</span>
                      <span>User {task.ownerId}</span>
                      <span>•</span>
                      <span>Updated {formatDate(task.updatedAt || task.createdAt)}</span>
                    </div>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </section>
      </div>
    </SidebarInset>
  );
}
