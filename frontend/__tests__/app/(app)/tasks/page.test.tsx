import React from "react";
import { render, screen, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import TasksPage from "../../../../app/(app)/tasks/page";
import { demoTasks } from "@/lib/mvp-data";

jest.mock("@/components/ui/sidebar", () => ({
  SidebarInset: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sidebar-inset">{children}</div>
  ),
  SidebarTrigger: ({ className }: { className?: string }) => (
    <button data-testid="sidebar-trigger" className={className}>
      Toggle Sidebar
    </button>
  ),
}));

describe("TasksPage", () => {
  beforeAll(() => {
    jest.useFakeTimers();
    jest.setSystemTime(new Date("2024-12-01"));
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  const setup = () => {
    const user = userEvent.setup({
      advanceTimers: jest.advanceTimersByTime,
    });
    render(<TasksPage />);
    return user;
  };

  const ownerCount = new Set(demoTasks.map((task) => task.owner)).size;
  const collaboratorCount = new Set(
    demoTasks.flatMap((task) => task.collaborators),
  ).size;
  const subtaskStats = demoTasks.reduce(
    (acc, task) => {
      acc.total += task.subtasks.length;
      acc.done += task.subtasks.filter(
        (subtask) => subtask.status === "Done",
      ).length;
      return acc;
    },
    { total: 0, done: 0 },
  );

  it("renders the summary cards with key metrics", () => {
    setup();

    const activeHeader = screen.getByText("Active cards").parentElement;
    expect(activeHeader).toBeInTheDocument();
    expect(
      within(activeHeader as HTMLElement).getByText(String(demoTasks.length)),
    ).toBeInTheDocument();

    const ownersHeader = screen.getByText("Owners").parentElement;
    expect(
      within(ownersHeader as HTMLElement).getByText(String(ownerCount)),
    ).toBeInTheDocument();

    const collaboratorsHeader = screen
      .getAllByText("Collaborators", { selector: "div" })
      .find((node) => node.getAttribute("data-slot") === "card-description");
    expect(collaboratorsHeader).toBeInTheDocument();
    expect(
      within(collaboratorsHeader!.parentElement as HTMLElement).getByText(
        String(collaboratorCount),
      ),
    ).toBeInTheDocument();

    const subtasksHeader = screen.getByText("Subtasks remaining").parentElement;
    expect(
      within(subtasksHeader as HTMLElement).getByText(
        String(subtaskStats.total - subtaskStats.done),
      ),
    ).toBeInTheDocument();
  });

  it("shows all board columns by default", () => {
    setup();

    expect(screen.getByTestId("board-column-todo")).toBeInTheDocument();
    expect(screen.getByTestId("board-column-in-progress")).toBeInTheDocument();
    expect(screen.getByTestId("board-column-blocked")).toBeInTheDocument();
    expect(screen.getByTestId("board-column-review")).toBeInTheDocument();
    expect(screen.getByTestId("board-column-done")).toBeInTheDocument();

    const todoColumn = screen.getByTestId("board-column-todo");
    expect(
      within(todoColumn).getAllByTestId("board-card").length,
    ).toBeGreaterThan(0);
  });

  it("filters cards and table rows by status", async () => {
    const user = setup();

    await user.click(screen.getByRole("button", { name: "Blocked" }));

    expect(screen.getByTestId("board-column-blocked")).toBeInTheDocument();
    expect(() => screen.getByTestId("board-column-todo")).toThrow();

    const blockedColumn = screen.getByTestId("board-column-blocked");
    expect(within(blockedColumn).getAllByTestId("board-card")).toHaveLength(1);

    const tableRows = screen.getAllByTestId("table-row");
    expect(tableRows).toHaveLength(1);
    expect(
      within(tableRows[0]).getByText(/Update retention dashboard/i),
    ).toBeInTheDocument();
  });

  it("renders card details including owner, collaborators, and subtasks", () => {
    setup();

    const cardTitle = screen
      .getAllByText(/Draft product specification/i)
      .find((element) => element.tagName.toLowerCase() === "h3");

    const card = cardTitle?.closest('[data-testid="board-card"]');

    expect(card).not.toBeNull();
    const scoped = within(card as HTMLElement);
    expect(scoped.getByText(/Alicia Keys/i)).toBeInTheDocument();

    // Check for collaborator avatars instead of text
    const collaboratorAvatars = scoped.getAllByText(/ML|PP/); // Marcus Lee, Priya Patel initials
    expect(collaboratorAvatars.length).toBeGreaterThan(0);

    // Check for subtask progress instead of "Subtasks" heading
    expect(scoped.getByText(/1\/3|2\/3|3\/3/)).toBeInTheDocument(); // progress format
  });

  it("displays detailed information in the table view", () => {
    setup();

    const rows = screen.getAllByTestId("table-row");
    expect(rows).toHaveLength(demoTasks.length);

    const doneRow = rows.find((row) =>
      within(row).queryByText(/Sync CRM segments/i),
    );
    expect(doneRow).toBeDefined();

    const scoped = within(doneRow as HTMLElement);
    expect(scoped.getByText(/No collaborators/i)).toBeInTheDocument();
    expect(scoped.getByText(/priority/i)).toBeInTheDocument();
  });
});
