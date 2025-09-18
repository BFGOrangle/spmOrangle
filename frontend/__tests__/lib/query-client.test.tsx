import { render, screen } from "@testing-library/react";
import { QueryProvider } from "../../lib/query-client";
import { useQuery } from "@tanstack/react-query";

// Test component that uses useQuery
const TestComponent = () => {
  const { data, isLoading } = useQuery({
    queryKey: ["test"],
    queryFn: () => Promise.resolve("test data"),
  });

  if (isLoading) return <div>Loading...</div>;
  return <div>Data: {data}</div>;
};

describe("QueryProvider", () => {
  it("should provide QueryClient context to child components", async () => {
    render(
      <QueryProvider>
        <TestComponent />
      </QueryProvider>,
    );

    // Initially should show loading
    expect(screen.getByText("Loading...")).toBeInTheDocument();

    // Wait for data to load
    expect(await screen.findByText("Data: test data")).toBeInTheDocument();
  });

  it("should render children correctly", () => {
    render(
      <QueryProvider>
        <div>Test Child Component</div>
      </QueryProvider>,
    );

    expect(screen.getByText("Test Child Component")).toBeInTheDocument();
  });
});
