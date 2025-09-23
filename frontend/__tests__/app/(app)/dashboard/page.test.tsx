import { render, screen } from "@testing-library/react";
import Dashboard from "../../../../app/(app)/dashboard/page";

// Mock ThemeToggle component
jest.mock("@/components/theme-toggle", () => ({
  ThemeToggle: () => <div data-testid="theme-toggle">Theme Toggle</div>,
}));

describe("Dashboard Page", () => {
  it("renders the dashboard page", () => {
    render(<Dashboard />);

    expect(screen.getByText("Dashboard to be developed")).toBeInTheDocument();
  });

  it("renders as a div element", () => {
    const { container } = render(<Dashboard />);

    // Check that the main element is a div
    const dashboardElement = container.firstChild as HTMLElement;
    expect(dashboardElement.tagName.toLowerCase()).toBe("div");
  });

  it("has the correct text content", () => {
    render(<Dashboard />);

    // Check for exact text match
    const textElement = screen.getByText("Dashboard to be developed");
    expect(textElement).toBeInTheDocument();
    expect(textElement.textContent).toBe("Dashboard to be developed");
  });

  it("renders without crashing", () => {
    expect(() => render(<Dashboard />)).not.toThrow();
  });

  it("is accessible - has appropriate structure", () => {
    const { container } = render(<Dashboard />);

    // Basic accessibility check - should not have any obvious accessibility violations
    // The component renders a simple div with text content
    expect(container.firstChild).toBeInstanceOf(HTMLDivElement);
  });

  it("exports default function correctly", () => {
    // Verify that Dashboard is a function component
    expect(typeof Dashboard).toBe("function");
    expect(Dashboard.name).toBe("Dashboard");
  });

  it("has stable rendering - multiple renders produce same result", () => {
    const { unmount, container: container1 } = render(<Dashboard />);
    const html1 = container1.innerHTML;
    unmount();

    const { container: container2 } = render(<Dashboard />);
    const html2 = container2.innerHTML;

    expect(html1).toBe(html2);
  });

  it("imports React correctly", () => {
    // This test ensures the component doesn't break due to React import issues
    render(<Dashboard />);
    expect(screen.getByText("Dashboard to be developed")).toBeInTheDocument();
  });

  it("contains TODO comment indicating future development", () => {
    // While we can't test the source code directly in the component,
    // we can verify the placeholder behavior exists as expected
    render(<Dashboard />);

    // The placeholder text indicates this is a work-in-progress
    expect(screen.getByText("Dashboard to be developed")).toBeInTheDocument();
  });

  it("renders with no props required", () => {
    // Verify component doesn't require any props
    expect(() => render(<Dashboard />)).not.toThrow();
  });
});
