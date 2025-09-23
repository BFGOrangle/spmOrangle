"use client";

import { useMemo, useState } from "react";

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
import { demoTasks, type TaskPriority, type TaskStatus } from "@/lib/mvp-data";

const statusTone: Record<TaskStatus, string> = {
  Todo: "border-slate-200 bg-slate-100 text-slate-700 dark:border-slate-500/40 dark:bg-slate-500/15 dark:text-slate-200",
  "In Progress":
    "border-blue-200 bg-blue-50 text-blue-600 dark:border-blue-500/40 dark:bg-blue-500/15 dark:text-blue-200",
  Blocked:
    "border-amber-300 bg-amber-100 text-amber-700 dark:border-amber-500/40 dark:bg-amber-500/15 dark:text-amber-200",
  Review:
    "border-purple-200 bg-purple-100 text-purple-700 dark:border-purple-500/40 dark:bg-purple-500/15 dark:text-purple-200",
  Done: "border-emerald-200 bg-emerald-100 text-emerald-700 dark:border-emerald-500/40 dark:bg-emerald-500/15 dark:text-emerald-200",
};

const priorityTone: Record<TaskPriority, string> = {
  High: "border-red-200 bg-red-100 text-red-700 dark:border-red-500/40 dark:bg-red-500/15 dark:text-red-200",
  Medium:
    "border-amber-200 bg-amber-50 text-amber-700 dark:border-amber-500/40 dark:bg-amber-500/15 dark:text-amber-200",
  Low: "border-slate-200 bg-slate-100 text-slate-700 dark:border-slate-500/40 dark:bg-slate-500/15 dark:text-slate-200",
};

const STATUS_FILTERS: (TaskStatus | "All")[] = [
  "All",
  "Todo",
  "In Progress",
  "Blocked",
  "Review",
  "Done",
];

const formatDate = (value: string) =>
  new Date(value).toLocaleDateString(undefined, {
    month: "short",
    day: "numeric",
  });

export default function TasksPage() {
  const [selectedStatus, setSelectedStatus] =
    useState<(typeof STATUS_FILTERS)[number]>("All");

  const totals = useMemo(() => {
    return demoTasks.reduce(
      (acc, task) => {
        acc.total += 1;
        if (task.priority === "High") acc.high += 1;
        if (task.status === "Blocked") acc.blocked += 1;
        if (task.status === "Done") acc.done += 1;
        return acc;
      },
      { total: 0, high: 0, blocked: 0, done: 0 },
    );
  }, []);

  const filteredTasks = useMemo(() => {
    const base = [...demoTasks].sort(
      (a, b) => new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime(),
    );
    if (selectedStatus === "All") return base;
    return base.filter((task) => task.status === selectedStatus);
  }, [selectedStatus]);

  return (
    <SidebarInset>
      <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
        <header className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <SidebarTrigger className="-ml-1" />
            <div>
              <h1 className="text-2xl font-semibold tracking-tight">Tasks</h1>
              <p className="text-muted-foreground text-sm">
                Track individual commitments, unblock teammates, and keep
                delivery flowing.
              </p>
            </div>
          </div>
          <Button size="sm">Log New Task</Button>
        </header>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          <Card>
            <CardHeader>
              <CardDescription>Open tasks</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {totals.total}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              {totals.done} closed • {totals.blocked} blocked
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>High priority</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {totals.high}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              Focus areas that unlock project delivery
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Shipping velocity</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {totals.total
                  ? Math.round((totals.done / totals.total) * 100)
                  : 0}
                %
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              Completion rate across the active board
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardDescription>Team throughput</CardDescription>
              <CardTitle className="text-3xl font-semibold">
                {totals.total - totals.done}
              </CardTitle>
            </CardHeader>
            <CardContent className="text-muted-foreground text-sm">
              Tasks remaining for this sprint cycle
            </CardContent>
          </Card>
        </section>

        <section className="space-y-4">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h2 className="text-lg font-semibold">Board view</h2>
              <p className="text-muted-foreground text-sm">
                Slice work by status to surface risk and upcoming ownership.
              </p>
            </div>
            <div className="flex flex-wrap items-center gap-2">
              {STATUS_FILTERS.map((status) => (
                <Button
                  key={status}
                  size="sm"
                  variant={selectedStatus === status ? "default" : "outline"}
                  onClick={() => setSelectedStatus(status)}
                >
                  {status}
                </Button>
              ))}
            </div>
          </div>

          <Card>
            <CardHeader className="pb-4">
              <div className="grid gap-3 text-xs font-medium uppercase tracking-wide text-muted-foreground sm:grid-cols-[minmax(200px,1.4fr)_minmax(140px,1fr)_minmax(160px,1fr)_minmax(120px,0.7fr)]">
                <span>Task</span>
                <span>Assignee</span>
                <span>Status</span>
                <span>Due</span>
              </div>
            </CardHeader>
            <Separator className="mx-6" />
            <CardContent className="divide-border flex flex-col divide-y">
              {filteredTasks.map((task) => (
                <div
                  key={task.id}
                  className="grid gap-3 py-4 transition hover:bg-accent/60 hover:text-accent-foreground sm:grid-cols-[minmax(200px,1.4fr)_minmax(140px,1fr)_minmax(160px,1fr)_minmax(120px,0.7fr)] sm:py-5"
                >
                  <div>
                    <p className="font-medium">{task.title}</p>
                    <p className="text-muted-foreground text-sm">
                      {task.project}
                    </p>
                  </div>
                  <div className="flex items-center gap-3 text-sm">
                    <div className="h-8 w-8 shrink-0 rounded-full bg-primary/10 text-center font-medium leading-8 text-primary">
                      {task.assignee[0] ?? ""}
                    </div>
                    <span>{task.assignee}</span>
                  </div>
                  <div className="flex flex-wrap items-center gap-2">
                    <Badge className={statusTone[task.status]}>
                      {task.status}
                    </Badge>
                    <Badge
                      variant="outline"
                      className={priorityTone[task.priority]}
                    >
                      {task.priority} priority
                    </Badge>
                  </div>
                  <div className="text-sm font-medium">
                    {formatDate(task.dueDate)}
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
