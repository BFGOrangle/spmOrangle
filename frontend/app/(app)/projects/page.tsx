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
import { demoProjects, type ProjectStatus } from "@/lib/mvp-data";

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
  const totals = demoProjects.reduce(
    (acc, project) => {
      acc[project.status] += 1;
      acc.progress += project.progress;
      acc.completed += project.tasksCompleted;
      acc.total += project.tasksTotal;
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

  const averageProgress = Math.round(totals.progress / demoProjects.length);
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
          <Button size="sm">New Project</Button>
        </header>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <Card>
            <CardHeader>
              <CardDescription>Total projects</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {demoProjects.length}
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
            {demoProjects.map((project) => (
              <Card key={project.id} className="h-full">
                <CardHeader className="pb-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="space-y-1">
                      <CardTitle className="text-lg font-semibold">
                        {project.name}
                      </CardTitle>
                      <CardDescription className="text-sm">
                        {project.description}
                      </CardDescription>
                    </div>
                    <Badge className={statusTone[project.status]}>
                      {project.status}
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
                        style={{ width: `${project.progress}%` }}
                      />
                    </div>
                    <div className="flex justify-between text-xs text-muted-foreground">
                      <span>{project.progress}% complete</span>
                      <span>
                        {project.tasksCompleted} / {project.tasksTotal} tasks
                      </span>
                    </div>
                  </div>

                  <div className="grid gap-3 sm:grid-cols-2">
                    <div className="space-y-1 text-sm">
                      <p className="text-muted-foreground text-xs uppercase tracking-wide">
                        Owner
                      </p>
                      <p className="font-medium">{project.owner}</p>
                    </div>
                    <div className="space-y-1 text-sm">
                      <p className="text-muted-foreground text-xs uppercase tracking-wide">
                        Due date
                      </p>
                      <p className="font-medium">
                        {formatDate(project.dueDate)}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </section>
      </div>
    </SidebarInset>
  );
}
