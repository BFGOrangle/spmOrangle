"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
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
import { dashboardService, DepartmentDashboardResponse } from "@/services/dashboard-service";
import { projectService, ProjectResponse, TaskResponse } from "@/services/project-service";
import { useCurrentUser } from "@/contexts/user-context";
import { Route } from "@/enums/Route";

const managerialRoles = new Set(["MANAGER", "DIRECTOR", "HR"]);

const formatDate = (value: string) =>
  new Date(value).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  });

const statusTone: Record<string, string> = {
  Active:
    "border-emerald-200 bg-emerald-100 text-emerald-700 dark:border-emerald-500/40 dark:bg-emerald-500/15 dark:text-emerald-200",
  Planning:
    "border-blue-200 bg-blue-50 text-blue-600 dark:border-blue-500/40 dark:bg-blue-500/15 dark:text-blue-200",
  "At Risk":
    "border-amber-300 bg-amber-100 text-amber-700 dark:border-amber-500/40 dark:bg-amber-500/15 dark:text-amber-200",
  Completed:
    "border-slate-200 bg-slate-100 text-slate-700 dark:border-slate-500/40 dark:bg-slate-500/15 dark:text-slate-200",
};

// Map backend task status to frontend status label
const mapTaskStatus = (status: string): string => {
  switch (status) {
    case "TODO":
      return "Todo";
    case "IN_PROGRESS":
      return "In Progress";
    case "BLOCKED":
      return "Blocked";
    case "COMPLETED":
      return "Done";
    default:
      return status;
  }
};

const defaultDepartmentDashboard = (
  department?: string,
): DepartmentDashboardResponse => ({
  department,
  includedDepartments: department ? [department] : [],
  metrics: {
    activeProjects: 0,
    totalTasks: 0,
    completedTasks: 0,
    blockedTasks: 0,
    highPriorityTasks: 0,
    completionRate: 0,
  },
  projects: [],
  upcomingCommitments: [],
  priorityQueue: [],
  teamLoad: [],
});

export default function Dashboard() {
  const router = useRouter();
  const { currentUser, isLoading: userLoading } = useCurrentUser();

  const [departmentDashboard, setDepartmentDashboard] =
    useState<DepartmentDashboardResponse | null>(null);
  const [projects, setProjects] = useState<ProjectResponse[]>([]);
  const [tasks, setTasks] = useState<TaskResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const isDepartmentDashboard = managerialRoles.has(currentUser?.role ?? "");
  const isStaffUser = currentUser?.role === "STAFF";

  useEffect(() => {
    const loadDashboardData = async () => {
      if (userLoading) {
        return;
      }

      if (!currentUser?.backendStaffId) {
        setDepartmentDashboard(null);
        setProjects([]);
        setTasks([]);
        setLoading(false);
        return;
      }

      try {
        setLoading(true);
        setError(null);

        if (isDepartmentDashboard) {
          const data = await dashboardService.getDepartmentDashboard();
          setDepartmentDashboard(data);
          setProjects([]);
          setTasks([]);
        } else {
          setDepartmentDashboard(null);
          const userId = currentUser.backendStaffId;
          const [projectsData, tasksData] = await Promise.all([
            projectService.getUserProjects(userId),
            projectService.getAllUserTasks(userId),
          ]);

          setProjects(projectsData);
          setTasks(tasksData);
        }
      } catch (err) {
        console.error("Error loading dashboard data:", err);
        setError("Failed to load dashboard data");
      } finally {
        setLoading(false);
      }
    };

    loadDashboardData();
  }, [currentUser, userLoading, isDepartmentDashboard]);

  const upcomingTasks = useMemo(() => {
    if (isDepartmentDashboard) {
      return [];
    }

    const now = new Date();
    const fourteenDaysFromNow = new Date(now.getTime() + 14 * 24 * 60 * 60 * 1000);

    return tasks
      .filter((task) => {
        // Filter out completed tasks
        if (task.status === "COMPLETED") return false;
        
        // Only include tasks with a due date
        if (!task.dueDateTime) return false;
        
        // Check if due date is within the next 14 days
        const dueDate = new Date(task.dueDateTime);
        return dueDate >= now && dueDate <= fourteenDaysFromNow;
      })
      .sort((a, b) => {
        // Sort by due date (earliest first)
        if (a.dueDateTime && b.dueDateTime) {
          return new Date(a.dueDateTime).getTime() - new Date(b.dueDateTime).getTime();
        }
        return 0;
      })
      .slice(0, 5);
  }, [tasks, isDepartmentDashboard]);

  const highPriorityTasks = useMemo(() => {
    if (isDepartmentDashboard) {
      return [];
    }

    return tasks.filter((task) => {
      const isBlocked = task.status === "BLOCKED";
      const isHighPriority = task.priority && task.priority >= 8;
      const isBug = task.taskType === "BUG";
      return isBlocked || isHighPriority || isBug;
    });
  }, [tasks, isDepartmentDashboard]);

  const teamLoad = useMemo(() => {
    if (isDepartmentDashboard) {
      return [];
    }

    return Object.entries(
      tasks.reduce<Record<number, number>>((acc, task) => {
        if (task.ownerId !== undefined) {
          acc[task.ownerId] = (acc[task.ownerId] || 0) + 1;
        }
        return acc;
      }, {}),
    )
      .sort(([, aCount], [, bCount]) => bCount - aCount)
      .slice(0, 4);
  }, [tasks, isDepartmentDashboard]);

  // Staff dashboard calculations
  const projectsWithUserTasks = useMemo(() => {
    if (isDepartmentDashboard) {
      return [];
    }

    return projects
      .filter((project) => !project.isRelated && project.id !== 0)
      .map((project) => {
        const userProjectTasks = tasks.filter((task) => 
          task.projectId === project.id && 
          (task.ownerId === currentUser?.backendStaffId || 
           task.assignedUserIds?.includes(currentUser?.backendStaffId ?? -1))
        );
        
        return {
          project,
          userTaskCount: userProjectTasks.length,
          userCompletedTaskCount: userProjectTasks.filter(
            (task) => task.status === "COMPLETED"
          ).length,
        };
      })
      .filter((item) => item.userTaskCount > 0);
  }, [projects, tasks, currentUser?.backendStaffId, isDepartmentDashboard]);

  const totalUserTasks = projectsWithUserTasks.reduce((sum, item) => sum + item.userTaskCount, 0);
  const completedUserTasks = projectsWithUserTasks.reduce((sum, item) => sum + item.userCompletedTaskCount, 0);
  const blockedTasks = tasks.filter((task) => task.status === "BLOCKED").length;
  const activeProjects = projectsWithUserTasks.map(item => item.project).filter(project => project.id !== 0);
  const activeProjectsCount = activeProjects.length;

  if (loading || userLoading) {
    return (
      <SidebarInset>
        <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
          <div className="py-16 text-center">
            <div className="text-lg font-semibold">Loading dashboard...</div>
            <div className="text-muted-foreground">
              Please wait while we fetch your data
            </div>
          </div>
        </div>
      </SidebarInset>
    );
  }

  if (error) {
    return (
      <SidebarInset>
        <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
          <div className="py-16 text-center">
            <div className="text-lg font-semibold text-destructive">
              Failed to load dashboard
            </div>
            <div className="text-muted-foreground">
              Please try refreshing the page
            </div>
          </div>
        </div>
      </SidebarInset>
    );
  }

  if (isDepartmentDashboard) {
    const scope = departmentDashboard ?? defaultDepartmentDashboard(currentUser?.department);
    const completionPercent = Math.round(scope.metrics.completionRate ?? 0);
    const includedDepartmentsLabel = scope.includedDepartments?.length
      ? scope.includedDepartments.join(", ")
      : scope.department ?? "";

    return (
      <SidebarInset>
        <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
          <SidebarTrigger className="-ml-1" />
          <Separator orientation="vertical" className="mr-2 h-4" />
          <div className="flex items-center gap-2">
            <LayoutDashboard className="h-5 w-5" />
            <div>
              <h1 className="text-lg font-semibold">Department Dashboard</h1>
              {includedDepartmentsLabel && (
                <p className="text-xs text-muted-foreground">
                  Scope: {includedDepartmentsLabel}
                </p>
              )}
            </div>
          </div>
        </header>

        <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
          <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <Card>
              <CardHeader>
                <CardDescription>Active projects</CardDescription>
                <CardTitle className="text-3xl font-semibold">
                  {scope.metrics.activeProjects}
                </CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {scope.metrics.activeProjects === 0
                  ? "No projects yet"
                  : `${scope.metrics.activeProjects} in flight`}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardDescription>Task completion</CardDescription>
                <CardTitle className="text-3xl font-semibold">
                  {completionPercent}%
                </CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {scope.metrics.totalTasks === 0
                  ? "No tasks yet"
                  : `${scope.metrics.completedTasks} completed • ${scope.metrics.totalTasks - scope.metrics.completedTasks} in flight`}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardDescription>High priority focus</CardDescription>
                <CardTitle className="text-3xl font-semibold">
                  {scope.metrics.highPriorityTasks}
                </CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {scope.metrics.highPriorityTasks === 0
                  ? "No pressing items"
                  : "Critical work requiring follow-up"}
              </CardContent>
            </Card>
            <Card>
              <CardHeader>
                <CardDescription>Blocked tasks</CardDescription>
                <CardTitle className="text-3xl font-semibold">
                  {scope.metrics.blockedTasks}
                </CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {scope.metrics.blockedTasks === 0
                  ? "No blockers"
                  : "Surface blockers quickly to keep work moving"}
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
                      Status, completion, and risk across your portfolio.
                    </CardDescription>
                  </div>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-muted-foreground"
                    onClick={() => router.push(Route.Projects)}
                  >
                    View all
                  </Button>
                </div>
              </CardHeader>
              <Separator />
              <CardContent className="flex flex-col divide-y divide-border">
                {scope.projects.filter(p => p.projectId !== 0).length === 0 ? (
                  <div className="py-8 text-center text-muted-foreground">
                    No projects to display
                  </div>
                ) : (
                  scope.projects.filter(p => p.projectId !== 0).map((project) => (
                    <button
                      key={project.projectId}
                      type="button"
                      onClick={() => router.push(`${Route.Projects}/${project.projectId}`)}
                      className="grid gap-3 py-4 text-left transition hover:bg-muted/50 sm:grid-cols-[1.4fr_auto_auto_auto]"
                    >
                      <div>
                        <p className="font-medium">{project.projectName}</p>
                        <p className="text-xs uppercase tracking-wide text-muted-foreground">
                          Status
                        </p>
                        <Badge className={statusTone[project.status] ?? statusTone.Active}>
                          {project.status}
                        </Badge>
                      </div>
                      <div className="flex flex-col gap-1 text-sm">
                        <span className="text-xs uppercase tracking-wide text-muted-foreground">
                          Completion
                        </span>
                        <span className="font-medium">
                          {project.completionPercentage}% · {project.completedTasks}/{project.totalTasks}
                        </span>
                      </div>
                      <div className="flex flex-col gap-1 text-sm">
                        <span className="text-xs uppercase tracking-wide text-muted-foreground">
                          Blocked
                        </span>
                        <span className="font-medium">{project.blockedTasks}</span>
                      </div>
                    </button>
                  ))
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
                      Due dates across your scope for the next two weeks.
                    </CardDescription>
                  </div>
                  <Button
                    size="sm"
                    variant="ghost"
                    className="text-muted-foreground"
                    disabled
                  >
                    Add reminder
                  </Button>
                </div>
              </CardHeader>
              <Separator />
              <CardContent className="flex flex-col divide-y divide-border">
                {scope.upcomingCommitments.length === 0 ? (
                  <div className="py-8 text-center text-muted-foreground">
                    No upcoming tasks
                  </div>
                ) : (
                  scope.upcomingCommitments.map((task) => (
                    <div
                      key={task.id}
                      className="flex flex-col gap-2 py-4 sm:flex-row sm:items-center sm:justify-between"
                    >
                      <div className="space-y-1">
                        <p className="font-medium">{task.title}</p>
                        <p className="text-sm text-muted-foreground">
                          {task.projectName ?? "Personal task"}
                        </p>
                      </div>
                      <div className="flex flex-wrap items-center gap-2 text-sm">
                        <Badge variant="outline" className="border-primary/40 text-primary">
                          {task.ownerName}
                        </Badge>
                        <Badge>{mapTaskStatus(task.status)}</Badge>
                        {task.dueDateTime && (
                          <span className="font-medium">
                            Due {formatDate(task.dueDateTime)}
                          </span>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </CardContent>
            </Card>
          </section>

          <section className="grid gap-4 xl:grid-cols-[1fr_1fr]">
            <Card>
              <CardHeader className="pb-4">
                <CardTitle className="text-lg font-semibold">Team load</CardTitle>
                <CardDescription>
                  Task distribution across your department.
                </CardDescription>
              </CardHeader>
              <Separator />
              <CardContent className="flex flex-col gap-3 py-4">
                {scope.teamLoad.length === 0 ? (
                  <div className="py-4 text-center text-muted-foreground">
                    No team data available
                  </div>
                ) : (
                  scope.teamLoad.map((entry) => (
                    <div
                      key={entry.userId}
                      className="flex items-center justify-between text-sm"
                    >
                      <div className="flex items-center gap-3">
                        <div className="h-8 w-8 rounded-full bg-primary/10 text-center font-medium leading-8 text-primary">
                          {entry.fullName.charAt(0).toUpperCase()}
                        </div>
                        <div className="flex flex-col">
                          <span className="font-medium">{entry.fullName}</span>
                          {entry.department && (
                            <span className="text-xs text-muted-foreground">
                              {entry.department}
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-muted-foreground">{entry.taskCount} tasks</span>
                        {entry.blockedTaskCount > 0 && (
                          <Badge variant="secondary" className="bg-amber-100 text-amber-900">
                            {entry.blockedTaskCount} blocked
                          </Badge>
                        )}
                      </div>
                    </div>
                  ))
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-4">
                <CardTitle className="text-lg font-semibold">
                  Priority queue
                </CardTitle>
                <CardDescription>
                  High urgency or blocked work that needs attention.
                </CardDescription>
              </CardHeader>
              <Separator />
              <CardContent className="flex flex-col gap-3 py-4">
                {scope.priorityQueue.length === 0 ? (
                  <div className="py-4 text-center text-muted-foreground">
                    No high priority tasks
                  </div>
                ) : (
                  scope.priorityQueue.map((task) => (
                    <div
                      key={task.id}
                      className="flex flex-col gap-2 rounded-lg border p-3"
                    >
                      <div className="flex items-center justify-between gap-2">
                        <p className="font-medium">{task.title}</p>
                        <Badge className="border-red-200 bg-red-100 text-red-700 dark:border-red-500/40 dark:bg-red-500/15 dark:text-red-200">
                          {task.taskType ?? "HIGH"}
                        </Badge>
                      </div>
                      <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
                        <span>{task.projectName ?? "Personal task"}</span>
                        <span>•</span>
                        <span>{task.ownerName}</span>
                        {task.priority !== undefined && (
                          <>
                            <span>•</span>
                            <span>Priority {task.priority}</span>
                          </>
                        )}
                        {task.updatedAt && (
                          <>
                            <span>•</span>
                            <span>Updated {formatDate(task.updatedAt)}</span>
                          </>
                        )}
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
                {activeProjectsCount}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              {activeProjectsCount === 0 ? "No projects yet" : `${activeProjectsCount} on track`}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Task completion</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {totalUserTasks ? Math.round((completedUserTasks / totalUserTasks) * 100) : 0}%
              </CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              {totalUserTasks === 0
                ? "No tasks yet"
                : `${completedUserTasks} closed • ${totalUserTasks - completedUserTasks} in flight`}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>High priority focus</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {highPriorityTasks.length}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              {highPriorityTasks.length === 0
                ? "No high priority tasks"
                : "Critical items requiring updates this week"}
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Blocked tasks</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {blockedTasks}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-sm text-muted-foreground">
              {blockedTasks === 0
                ? "No blocked tasks"
                : "Surface blockers quickly to keep work moving"}
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
                  onClick={() => router.push(Route.Projects)}
                >
                  View all
                </Button>
              </div>
            </CardHeader>
            <Separator />
            <CardContent className="flex flex-col divide-y divide-border">
              {projectsWithUserTasks.length === 0 ? (
                <div className="py-8 text-center text-muted-foreground">
                  No projects to display
                </div>
              ) : (
                projectsWithUserTasks.map(({ project, userTaskCount, userCompletedTaskCount }) => {
                    const completion = userTaskCount > 0
                      ? Math.round((userCompletedTaskCount / userTaskCount) * 100)
                      : 0;
                    
                    return (
                      <div
                        key={project.id}
                        className="grid gap-3 py-4 sm:grid-cols-[1.2fr_auto_auto]"
                      >
                      <div>
                        <p className="font-medium">{project.name}</p>
                        <p className="text-sm text-muted-foreground">
                          Owner ID: {project.ownerId}
                        </p>
                      </div>
                      <div className="flex flex-col gap-1 text-sm">
                        <span className="text-xs uppercase tracking-wide text-muted-foreground">
                          Status
                        </span>
                        <Badge className={statusTone.Active}>
                          Active
                        </Badge>
                      </div>
                      <div className="flex flex-col gap-1 text-sm">
                        <span className="text-xs uppercase tracking-wide text-muted-foreground">
                          Completion
                        </span>
                        <span className="font-medium">
                          {completion}% · {userCompletedTaskCount}/
                          {userTaskCount} tasks
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
              </div>
            </CardHeader>
            <Separator />
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
                      <p className="text-sm text-muted-foreground">
                        {task.projectId ? `Project ${task.projectId}` : "Personal Task"}
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
                      {task.dueDateTime && (
                        <span className="font-medium">
                          Due {formatDate(task.dueDateTime)}
                        </span>
                      )}
                    </div>
                  </div>
                ))
              )}
            </CardContent>
          </Card>
        </section>

        <section className={`grid gap-4 ${isStaffUser ? "xl:grid-cols-1" : "xl:grid-cols-[1fr_1fr]"}`}>
          {!isStaffUser && (
            <Card>
              <CardHeader className="pb-4">
                <CardTitle className="text-lg font-semibold">Team load</CardTitle>
                <CardDescription>
                  Who is carrying the most tasks right now.
                </CardDescription>
              </CardHeader>
              <Separator />
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
            <Separator />
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
                      <span>{task.projectId ? `Project ${task.projectId}` : "Personal Task"}</span>
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
