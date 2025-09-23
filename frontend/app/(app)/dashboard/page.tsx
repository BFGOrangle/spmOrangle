"use client";

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
import {
  demoProjects,
  demoTasks,
  type ProjectSummary,
  type TaskSummary,
} from "@/lib/mvp-data";

const activeProjects = demoProjects.filter(
  (project) => project.status !== "Completed",
);
const upcomingTasks = [...demoTasks]
  .filter((task) => task.status !== "Done")
  .sort((a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime())
  .slice(0, 5);
const highPriorityTasks = demoTasks.filter((task) => task.priority === "High");

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

const summarizeTeamLoad = (tasks: TaskSummary[]) => {
  const load = tasks.reduce<Record<string, number>>((acc, task) => {
    acc[task.assignee] = (acc[task.assignee] ?? 0) + 1;
    return acc;
  }, {});
  return Object.entries(load)
    .sort(([, aCount], [, bCount]) => bCount - aCount)
    .slice(0, 4);
};

const teamLoad = summarizeTeamLoad(demoTasks);

const projectCompletion = (project: ProjectSummary) =>
  project.tasksTotal
    ? Math.round((project.tasksCompleted / project.tasksTotal) * 100)
    : 0;

export default function Dashboard() {
  const totalTasks = demoTasks.length;
  const completedTasks = demoTasks.filter(
    (task) => task.status === "Done",
  ).length;
  const blockedTasks = demoTasks.filter(
    (task) => task.status === "Blocked",
  ).length;
  const onTrackProjects = activeProjects.filter(
    (project) => project.status === "Active",
  ).length;

  return (
    <SidebarInset>
      <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
        <header className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <SidebarTrigger className="-ml-1" />
            <div>
              <h1 className="text-2xl font-semibold tracking-tight">
                My Analytics
              </h1>
              <p className="text-muted-foreground text-sm">
                A single view of delivery health across projects, priorities,
                and owners.
              </p>
            </div>
          </div>
          <Button size="sm" variant="outline">
            Share update
          </Button>
        </header>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <Card>
            <CardHeader>
              <CardDescription>Active projects</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {activeProjects.length}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {onTrackProjects} on track ·{" "}
              {activeProjects.length - onTrackProjects} flagged
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
              {completedTasks} closed • {totalTasks - completedTasks} in flight
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
              Critical items requiring updates this week
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
              Surface blockers quickly to keep work moving
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
              {activeProjects.map((project) => (
                <div
                  key={project.id}
                  className="grid gap-3 py-4 sm:grid-cols-[1.2fr_auto_auto]"
                >
                  <div>
                    <p className="font-medium">{project.name}</p>
                    <p className="text-muted-foreground text-sm">
                      {project.owner}
                    </p>
                  </div>
                  <div className="flex flex-col gap-1 text-sm">
                    <span className="text-muted-foreground text-xs uppercase tracking-wide">
                      Status
                    </span>
                    <Badge className={statusTone[project.status] ?? ""}>
                      {project.status}
                    </Badge>
                  </div>
                  <div className="flex flex-col gap-1 text-sm">
                    <span className="text-muted-foreground text-xs uppercase tracking-wide">
                      Completion
                    </span>
                    <span className="font-medium">
                      {projectCompletion(project)}% · {project.tasksCompleted}/
                      {project.tasksTotal} tasks
                    </span>
                  </div>
                </div>
              ))}
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
              {upcomingTasks.map((task) => (
                <div
                  key={task.id}
                  className="flex flex-col gap-2 py-4 sm:flex-row sm:items-center sm:justify-between"
                >
                  <div className="space-y-1">
                    <p className="font-medium">{task.title}</p>
                    <p className="text-muted-foreground text-sm">
                      {task.project}
                    </p>
                  </div>
                  <div className="flex flex-wrap items-center gap-2 text-sm">
                    <Badge
                      variant="outline"
                      className="border-primary/40 text-primary"
                    >
                      {task.assignee}
                    </Badge>
                    <Badge>{task.status}</Badge>
                    <span className="font-medium">
                      Due {formatDate(task.dueDate)}
                    </span>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </section>

        <section className="grid gap-4 xl:grid-cols-[1fr_1fr]">
          <Card>
            <CardHeader className="pb-4">
              <CardTitle className="text-lg font-semibold">Team load</CardTitle>
              <CardDescription>
                Who is carrying the most tasks right now.
              </CardDescription>
            </CardHeader>
            <Separator className="mx-6" />
            <CardContent className="flex flex-col gap-3 py-4">
              {teamLoad.map(([owner, count]) => (
                <div
                  key={owner}
                  className="flex items-center justify-between text-sm"
                >
                  <div className="flex items-center gap-3">
                    <div className="h-8 w-8 rounded-full bg-primary/10 text-center font-medium leading-8 text-primary">
                      {owner[0] ?? ""}
                    </div>
                    <span className="font-medium">{owner}</span>
                  </div>
                  <span className="text-muted-foreground">{count} tasks</span>
                </div>
              ))}
            </CardContent>
          </Card>

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
              {highPriorityTasks.map((task) => (
                <div
                  key={task.id}
                  className="flex flex-col gap-2 rounded-lg border p-3"
                >
                  <div className="flex items-center justify-between gap-2">
                    <p className="font-medium">{task.title}</p>
                    <Badge className="border-red-200 bg-red-100 text-red-700 dark:border-red-500/40 dark:bg-red-500/15 dark:text-red-200">
                      High priority
                    </Badge>
                  </div>
                  <div className="flex flex-wrap items-center gap-2 text-sm text-muted-foreground">
                    <span>{task.project}</span>
                    <span>•</span>
                    <span>{task.assignee}</span>
                    <span>•</span>
                    <span>Due {formatDate(task.dueDate)}</span>
                  </div>
                </div>
              ))}
            </CardContent>
          </Card>
        </section>
      </div>
    </SidebarInset>
  );
}
