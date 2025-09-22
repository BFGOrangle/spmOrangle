import { render, screen } from "@testing-library/react";
import { ErrorMessageCallout } from "../../components/error-message-callout";

// Mock Lucide icons
jest.mock("lucide-react", () => ({
  AlertCircle: () => <div data-testid="alert-circle-icon" />,
  Clock: () => <div data-testid="clock-icon" />,
  Code: () => <div data-testid="code-icon" />,
}));

describe("ErrorMessageCallout", () => {
  const basicProps = {
    errorMessage: "Something went wrong",
  };

  it("renders with basic error message", () => {
    render(<ErrorMessageCallout {...basicProps} />);
    
    expect(screen.getByText("Something went wrong")).toBeInTheDocument();
    expect(screen.getByText("Error")).toBeInTheDocument();
    expect(screen.getByTestId("alert-circle-icon")).toBeInTheDocument();
  });

  it("renders with custom error header", () => {
    render(
      <ErrorMessageCallout
        {...basicProps}
        errorHeader="Custom Error"
      />
    );
    
    expect(screen.getByText("Custom Error")).toBeInTheDocument();
  });

  it("renders with error code and status text", () => {
    render(
      <ErrorMessageCallout
        {...basicProps}
        errorCode={400}
        statusText="Bad Request"
      />
    );
    
    expect(screen.getByText("400 Bad Request")).toBeInTheDocument();
    expect(screen.getByTestId("code-icon")).toBeInTheDocument();
  });

  it("renders detailed errors when provided", () => {
    const errors = [
      {
        message: "Field is required",
        field: "email",
        rejectedValue: "",
        timestamp: "2024-01-01T12:00:00Z",
      },
      {
        message: "Invalid format",
        field: "phone",
        rejectedValue: "123",
        timestamp: "2024-01-01T12:00:00Z",
      },
    ];

    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={errors}
      />
    );
    
    expect(screen.getByText("Error Details:")).toBeInTheDocument();
    expect(screen.getByText("Field is required")).toBeInTheDocument();
    expect(screen.getByText("Invalid format")).toBeInTheDocument();
    expect(screen.getByText("email")).toBeInTheDocument();
    expect(screen.getByText("phone")).toBeInTheDocument();
  });

  it("formats timestamp correctly", () => {
    const errors = [
      {
        message: "Test error",
        timestamp: "2024-01-01T12:00:00Z",
      },
    ];

    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={errors}
      />
    );
    
    // Check that some date/time text is rendered (exact format may vary by locale)
    expect(screen.getByText(/Time:/)).toBeInTheDocument();
  });

  it("handles invalid timestamp gracefully", () => {
    const errors = [
      {
        message: "Test error",
        timestamp: "invalid-date",
      },
    ];

    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={errors}
      />
    );
    
    // Should render "Invalid Date" when timestamp is invalid
    expect(screen.getByText(/Invalid Date/)).toBeInTheDocument();
  });

  it("shows rejected values when provided", () => {
    const errors = [
      {
        message: "Value too short",
        field: "password",
        rejectedValue: "123",
        timestamp: "2024-01-01T12:00:00Z",
      },
    ];

    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={errors}
      />
    );
    
    expect(screen.getByText("Value:")).toBeInTheDocument();
    expect(screen.getByText("123")).toBeInTheDocument();
  });

  it("handles rejected value of 0 correctly", () => {
    const errors = [
      {
        message: "Value cannot be zero",
        field: "amount",
        rejectedValue: 0,
        timestamp: "2024-01-01T12:00:00Z",
      },
    ];

    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={errors}
      />
    );
    
    expect(screen.getByText("0")).toBeInTheDocument();
  });

  it("hides details when showDetails is false", () => {
    const errors = [
      {
        message: "Field is required",
        field: "email",
        timestamp: "2024-01-01T12:00:00Z",
      },
    ];

    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={errors}
        showDetails={false}
      />
    );
    
    expect(screen.queryByText("Error Details:")).not.toBeInTheDocument();
    expect(screen.queryByText("Field is required")).not.toBeInTheDocument();
  });

  it("shows details with error code even when no errors array", () => {
    render(
      <ErrorMessageCallout
        {...basicProps}
        errorCode={500}
        statusText="Internal Server Error"
      />
    );
    
    // Should show separator and structure for details even without errors array
    expect(screen.getByText("500 Internal Server Error")).toBeInTheDocument();
  });

  it("handles empty errors array", () => {
    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={[]}
        errorCode={404}
      />
    );
    
    expect(screen.getByText("Something went wrong")).toBeInTheDocument();
    expect(screen.queryByText("Error Details:")).not.toBeInTheDocument();
  });

  it("renders multiple errors correctly", () => {
    const errors = [
      {
        message: "First error",
        field: "field1",
        timestamp: "2024-01-01T12:00:00Z",
      },
      {
        message: "Second error",
        field: "field2", 
        timestamp: "2024-01-01T12:01:00Z",
      },
      {
        message: "Third error",
        timestamp: "2024-01-01T12:02:00Z",
      },
    ];

    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={errors}
      />
    );
    
    expect(screen.getByText("First error")).toBeInTheDocument();
    expect(screen.getByText("Second error")).toBeInTheDocument();
    expect(screen.getByText("Third error")).toBeInTheDocument();
    expect(screen.getByText("field1")).toBeInTheDocument();
    expect(screen.getByText("field2")).toBeInTheDocument();
  });

  it("handles errors without fields or timestamps", () => {
    const errors = [
      {
        message: "Simple error message",
      },
    ];

    render(
      <ErrorMessageCallout
        {...basicProps}
        errors={errors}
      />
    );
    
    expect(screen.getByText("Simple error message")).toBeInTheDocument();
    expect(screen.queryByText("Field:")).not.toBeInTheDocument();
    expect(screen.queryByText("Time:")).not.toBeInTheDocument();
  });
});
