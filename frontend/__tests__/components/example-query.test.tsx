import { screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { ExampleComponent } from "../../components/example-query";
import { render } from "../test-utils";

// Mock fetch globally
const mockFetch = global.fetch as jest.MockedFunction<typeof fetch>;

const mockUsers = [
  {
    id: 1,
    name: "John Doe",
    username: "johndoe",
    email: "john@example.com",
  },
  {
    id: 2,
    name: "Jane Smith",
    username: "janesmith",
    email: "jane@example.com",
  },
];

describe("ExampleComponent", () => {
  beforeEach(() => {
    mockFetch.mockClear();
  });

  it("should display loading state initially", () => {
    mockFetch.mockImplementation(
      () => new Promise(() => {}), // Never resolves, stays in loading state
    );

    render(<ExampleComponent />);

    expect(screen.getByText("Loading users...")).toBeInTheDocument();
  });

  it("should display users when API call succeeds", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsers,
    } as Response);

    render(<ExampleComponent />);

    // Wait for loading to finish and users to be displayed
    await waitFor(() => {
      expect(screen.getByText("Users Example")).toBeInTheDocument();
    });

    // Check for individual parts of the user display
    expect(screen.getByText("John Doe")).toBeInTheDocument();
    expect(screen.getByText(/johndoe/)).toBeInTheDocument();
    expect(screen.getByText(/john@example\.com/)).toBeInTheDocument();
    expect(screen.getByText("Jane Smith")).toBeInTheDocument();
    expect(screen.getByText(/janesmith/)).toBeInTheDocument();
    expect(screen.getByText(/jane@example\.com/)).toBeInTheDocument();
  });

  it("should display error state when API call fails", async () => {
    // Mock multiple failures to handle retries (component has retry: 2)
    mockFetch.mockRejectedValue(new Error("Failed to fetch users"));

    render(<ExampleComponent />);

    // Wait longer for retries to complete (component has retry: 2)
    await waitFor(
      () => {
        expect(screen.getByText(/Failed to fetch users/)).toBeInTheDocument();
      },
      { timeout: 10000 },
    );

    expect(screen.getByText("Try Again")).toBeInTheDocument();
  });

  it("should display error when response is not ok", async () => {
    // Mock multiple failures to handle retries (component has retry: 2)
    mockFetch.mockResolvedValue({
      ok: false,
      status: 404,
    } as Response);

    render(<ExampleComponent />);

    // Wait longer for retries to complete (component has retry: 2)
    await waitFor(
      () => {
        expect(screen.getByText(/Failed to fetch users/)).toBeInTheDocument();
      },
      { timeout: 10000 },
    );
  });

  it("should refetch data when refresh button is clicked", async () => {
    // First call succeeds
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsers,
    } as Response);

    render(<ExampleComponent />);

    // Wait for initial data to load
    await waitFor(() => {
      expect(screen.getByText("Users Example")).toBeInTheDocument();
    });

    // Setup second call
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => [
        ...mockUsers,
        {
          id: 3,
          name: "Bob Wilson",
          username: "bobwilson",
          email: "bob@example.com",
        },
      ],
    } as Response);

    // Click refresh button
    const refreshButton = screen.getByText("Refresh Data");
    await userEvent.click(refreshButton);

    // Wait for new data to load
    await waitFor(() => {
      expect(screen.getByText("Bob Wilson")).toBeInTheDocument();
    });

    expect(screen.getByText(/bobwilson/)).toBeInTheDocument();
    expect(screen.getByText(/bob@example\.com/)).toBeInTheDocument();

    // Verify fetch was called twice
    expect(mockFetch).toHaveBeenCalledTimes(2);
  });

  it("should retry failed requests when Try Again button is clicked", async () => {
    // Mock multiple failures to handle retries (component has retry: 2)
    mockFetch.mockRejectedValue(new Error("Network error"));

    render(<ExampleComponent />);

    // Wait longer for retries to complete (component has retry: 2)
    await waitFor(
      () => {
        expect(screen.getByText(/Network error/)).toBeInTheDocument();
      },
      { timeout: 10000 },
    );

    // Clear previous mock and setup successful retry
    mockFetch.mockClear();
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsers,
    } as Response);

    // Click try again button
    const tryAgainButton = screen.getByText("Try Again");
    await userEvent.click(tryAgainButton);

    // Wait for successful data load
    await waitFor(() => {
      expect(screen.getByText("Users Example")).toBeInTheDocument();
    });

    expect(screen.getByText("John Doe")).toBeInTheDocument();
  });

  it("should call the correct API endpoint", async () => {
    mockFetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockUsers,
    } as Response);

    render(<ExampleComponent />);

    await waitFor(() => {
      expect(mockFetch).toHaveBeenCalledWith(
        "https://jsonplaceholder.typicode.com/users",
      );
    });
  });
});
