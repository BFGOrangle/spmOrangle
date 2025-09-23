export type ProjectStatus = "Active" | "Planning" | "At Risk" | "Completed";

export type ProjectSummary = {
  id: string;
  name: string;
  description: string;
  status: ProjectStatus;
  dueDate: string;
  owner: string;
  progress: number;
  tasksCompleted: number;
  tasksTotal: number;
};

export type TaskStatus = "Todo" | "In Progress" | "Blocked" | "Review" | "Done";

export type TaskPriority = "Low" | "Medium" | "High";

export type TaskSummary = {
  id: string;
  title: string;
  project: string;
  assignee: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string;
};

export const demoProjects: ProjectSummary[] = [
  {
    id: "proj-001",
    name: "Cross-platform Mobile App",
    description:
      "Deliver a unified experience for iOS and Android users with feature parity.",
    status: "Active",
    dueDate: "2025-01-24",
    owner: "Alicia Keys",
    progress: 68,
    tasksCompleted: 34,
    tasksTotal: 50,
  },
  {
    id: "proj-002",
    name: "Data Quality Refresh",
    description:
      "Consolidate analytics pipelines and harden reporting dashboards.",
    status: "At Risk",
    dueDate: "2025-02-03",
    owner: "Marcus Lee",
    progress: 41,
    tasksCompleted: 19,
    tasksTotal: 46,
  },
  {
    id: "proj-003",
    name: "Customer Onboarding",
    description:
      "Roll out guided walkthroughs and measure new user activation.",
    status: "Planning",
    dueDate: "2025-03-12",
    owner: "Priya Patel",
    progress: 22,
    tasksCompleted: 5,
    tasksTotal: 23,
  },
  {
    id: "proj-004",
    name: "Revenue Intelligence",
    description: "Launch forecasting models that sync with GTM tooling.",
    status: "Completed",
    dueDate: "2024-12-10",
    owner: "Jamal Carter",
    progress: 100,
    tasksCompleted: 42,
    tasksTotal: 42,
  },
];

export const demoTasks: TaskSummary[] = [
  {
    id: "task-001",
    title: "Draft product specification",
    project: "Cross-platform Mobile App",
    assignee: "Alicia Keys",
    status: "In Progress",
    priority: "High",
    dueDate: "2025-01-07",
  },
  {
    id: "task-002",
    title: "QA regression sweep",
    project: "Revenue Intelligence",
    assignee: "Daniel Cho",
    status: "Review",
    priority: "Medium",
    dueDate: "2024-12-05",
  },
  {
    id: "task-003",
    title: "Update retention dashboard",
    project: "Data Quality Refresh",
    assignee: "Marcus Lee",
    status: "Blocked",
    priority: "High",
    dueDate: "2024-12-18",
  },
  {
    id: "task-004",
    title: "Sketch onboarding flows",
    project: "Customer Onboarding",
    assignee: "Priya Patel",
    status: "Todo",
    priority: "Medium",
    dueDate: "2025-01-15",
  },
  {
    id: "task-005",
    title: "Migrate analytics warehouse",
    project: "Data Quality Refresh",
    assignee: "Julia Sato",
    status: "In Progress",
    priority: "High",
    dueDate: "2025-01-02",
  },
  {
    id: "task-006",
    title: "Sync CRM segments",
    project: "Revenue Intelligence",
    assignee: "Jamal Carter",
    status: "Done",
    priority: "Low",
    dueDate: "2024-11-21",
  },
];
