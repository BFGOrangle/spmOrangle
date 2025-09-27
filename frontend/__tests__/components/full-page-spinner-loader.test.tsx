import { render, screen } from "@testing-library/react";
import FullPageSpinnerLoader from "../../components/full-page-spinner-loader";

// Mock Lucide icons
jest.mock("lucide-react", () => ({
  Loader2: ({ className, ...props }: any) => (
    <div data-testid="loader-icon" className={className} {...props} />
  ),
}));

describe("FullPageSpinnerLoader", () => {
  it("renders spinner without loading message", () => {
    render(<FullPageSpinnerLoader />);
    
    const loader = screen.getByTestId("loader-icon");
    expect(loader).toBeInTheDocument();
    expect(loader).toHaveClass("h-8", "w-8", "animate-spin", "text-blue-600");
  });

  it("renders spinner with loading message", () => {
    const loadingMessage = "Loading data...";
    render(<FullPageSpinnerLoader loadingMessage={loadingMessage} />);
    
    expect(screen.getByTestId("loader-icon")).toBeInTheDocument();
    expect(screen.getByText(loadingMessage)).toBeInTheDocument();
  });

  it("applies correct CSS classes to loading message", () => {
    const loadingMessage = "Please wait...";
    render(<FullPageSpinnerLoader loadingMessage={loadingMessage} />);
    
    const messageElement = screen.getByText(loadingMessage);
    expect(messageElement).toHaveClass(
      "text-gray-600",
      "font-medium", 
      "pl-3"
    );
  });

  it("has correct container structure and classes", () => {
    const { container } = render(<FullPageSpinnerLoader />);
    
    const outerContainer = container.firstChild as Element;
    expect(outerContainer).toHaveClass("h-[100vh]", "w-full");
    
    const innerContainer = outerContainer.firstChild as Element;
    expect(innerContainer).toHaveClass(
      "w-full",
      "h-full", 
      "flex",
      "items-center",
      "justify-center"
    );
  });

  it("does not render message span when no loading message provided", () => {
    const { container } = render(<FullPageSpinnerLoader />);
    
    const spans = container.querySelectorAll("span");
    expect(spans).toHaveLength(0);
  });

  it("renders message span only when loading message is provided", () => {
    const { container } = render(<FullPageSpinnerLoader loadingMessage="Loading..." />);
    
    const spans = container.querySelectorAll("span");
    expect(spans).toHaveLength(1);
    expect(spans[0]).toHaveTextContent("Loading...");
  });

  it("handles empty loading message", () => {
    render(<FullPageSpinnerLoader loadingMessage="" />);
    
    expect(screen.getByTestId("loader-icon")).toBeInTheDocument();
    // Empty string should not render the span
    const { container } = render(<FullPageSpinnerLoader loadingMessage="" />);
    const spans = container.querySelectorAll("span");
    expect(spans).toHaveLength(0);
  });

  it("handles long loading messages", () => {
    const longMessage = "This is a very long loading message that should still render correctly without breaking the layout";
    render(<FullPageSpinnerLoader loadingMessage={longMessage} />);
    
    expect(screen.getByText(longMessage)).toBeInTheDocument();
    expect(screen.getByTestId("loader-icon")).toBeInTheDocument();
  });

  it("maintains flexbox layout with loading message", () => {
    render(<FullPageSpinnerLoader loadingMessage="Loading..." />);
    
    const container = screen.getByTestId("loader-icon").parentElement;
    expect(container).toHaveClass("flex", "items-center", "justify-center");
  });
});