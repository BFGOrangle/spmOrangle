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

export type TaskSubtaskStatus = "Todo" | "In Progress" | "Done";

export type TaskSubtask = {
  id: string;
  title: string;
  status: TaskSubtaskStatus;
};

export type TaskSummary = {
  id: string;
  key: string;
  title: string;
  description: string;
  project: string;
  owner: string;
  collaborators: string[];
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string;
  lastUpdated: string;
  subtasks: TaskSubtask[];
  attachments: number;
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
    key: "MOB-21",
    title: "Draft product specification",
    description:
      "Capture requirements, user journeys, and technical constraints for the cross-platform experience.",
    project: "Cross-platform Mobile App",
    owner: "Alicia Keys",
    collaborators: ["Marcus Lee", "Priya Patel"],
    status: "In Progress",
    priority: "High",
    dueDate: "2025-01-07",
    lastUpdated: "2024-12-01",
    attachments: 3,
    subtasks: [
      {
        id: "task-001-1",
        title: "Collect stakeholder feedback",
        status: "Done",
      },
      {
        id: "task-001-2",
        title: "Outline functional requirements",
        status: "In Progress",
      },
      {
        id: "task-001-3",
        title: "Review tech feasibility with engineering",
        status: "Todo",
      },
    ],
  },
  {
    id: "task-002",
    key: "REV-88",
    title: "QA regression sweep",
    description:
      "Validate key workflows before pushing the revenue intelligence release to production.",
    project: "Revenue Intelligence",
    owner: "Daniel Cho",
    collaborators: ["Julia Sato", "Jamal Carter"],
    status: "Review",
    priority: "Medium",
    dueDate: "2024-12-05",
    lastUpdated: "2024-11-29",
    attachments: 1,
    subtasks: [
      {
        id: "task-002-1",
        title: "Document acceptance criteria",
        status: "Done",
      },
      {
        id: "task-002-2",
        title: "Capture high severity defects",
        status: "Done",
      },
      {
        id: "task-002-3",
        title: "Summarize QA findings",
        status: "In Progress",
      },
    ],
  },
  {
    id: "task-003",
    key: "DATA-34",
    title: "Update retention dashboard",
    description:
      "Refresh cohort analysis charts and wire data to the unified metrics layer.",
    project: "Data Quality Refresh",
    owner: "Marcus Lee",
    collaborators: ["Alicia Keys", "Julia Sato"],
    status: "Blocked",
    priority: "High",
    dueDate: "2024-12-18",
    lastUpdated: "2024-12-02",
    attachments: 2,
    subtasks: [
      {
        id: "task-003-1",
        title: "Align new metric definitions",
        status: "Done",
      },
      {
        id: "task-003-2",
        title: "Update Metabase queries",
        status: "In Progress",
      },
      {
        id: "task-003-3",
        title: "Verify dashboard permissions",
        status: "Todo",
      },
    ],
  },
  {
    id: "task-004",
    key: "ONB-12",
    title: "Sketch onboarding flows",
    description:
      "Design guided walkthroughs and collaborative canvases for new users entering the product.",
    project: "Customer Onboarding",
    owner: "Priya Patel",
    collaborators: ["Alicia Keys"],
    status: "Todo",
    priority: "Medium",
    dueDate: "2025-01-15",
    lastUpdated: "2024-11-27",
    attachments: 0,
    subtasks: [
      {
        id: "task-004-1",
        title: "Audit existing onboarding steps",
        status: "Todo",
      },
      {
        id: "task-004-2",
        title: "Draft new walkthrough copy",
        status: "Todo",
      },
    ],
  },
  {
    id: "task-005",
    key: "DATA-35",
    title: "Migrate analytics warehouse",
    description:
      "Transition data models and pipelines to the new Snowflake environment with zero downtime.",
    project: "Data Quality Refresh",
    owner: "Julia Sato",
    collaborators: ["Marcus Lee", "Daniel Cho"],
    status: "In Progress",
    priority: "High",
    dueDate: "2025-01-02",
    lastUpdated: "2024-12-03",
    attachments: 4,
    subtasks: [
      {
        id: "task-005-1",
        title: "Provision new warehouse schemas",
        status: "Done",
      },
      {
        id: "task-005-2",
        title: "Backfill historical data",
        status: "In Progress",
      },
      {
        id: "task-005-3",
        title: "Cut over scheduled jobs",
        status: "Todo",
      },
      {
        id: "task-005-4",
        title: "Monitor post-migration health",
        status: "Todo",
      },
    ],
  },
  {
    id: "task-006",
    key: "REV-75",
    title: "Sync CRM segments",
    description:
      "Consolidate pipeline stages and sync cohorts with the marketing automation platform.",
    project: "Revenue Intelligence",
    owner: "Jamal Carter",
    collaborators: [],
    status: "Done",
    priority: "Low",
    dueDate: "2024-11-21",
    lastUpdated: "2024-11-20",
    attachments: 5,
    subtasks: [
      {
        id: "task-006-1",
        title: "Audit existing CRM segments",
        status: "Done",
      },
      {
        id: "task-006-2",
        title: "Configure API sync jobs",
        status: "Done",
      },
    ],
  },
];
