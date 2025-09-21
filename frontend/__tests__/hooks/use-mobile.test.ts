import { renderHook, act } from "@testing-library/react";
import { useIsMobile } from "../../hooks/use-mobile";

// Mock window.matchMedia
const mockMatchMedia = jest.fn();

describe("useIsMobile", () => {
  const originalMatchMedia = window.matchMedia;

  beforeEach(() => {
    // Mock matchMedia
    window.matchMedia = mockMatchMedia;
    
    // Reset mock
    mockMatchMedia.mockClear();
  });

  afterEach(() => {
    // Restore original matchMedia
    window.matchMedia = originalMatchMedia;
  });

  it("returns false for desktop screen size", () => {
    // Mock desktop size (768px and above)
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 1024,
    });

    const mockMql = {
      matches: false,
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { result } = renderHook(() => useIsMobile());

    expect(result.current).toBe(false);
  });

  it("returns true for mobile screen size", () => {
    // Mock mobile size (below 768px)
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 375,
    });

    const mockMql = {
      matches: true,
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { result } = renderHook(() => useIsMobile());

    expect(result.current).toBe(true);
  });

  it("returns undefined initially", () => {
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 1024,
    });

    const mockMql = {
      matches: false,
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { result } = renderHook(() => useIsMobile());
    
    // Initially should be false (due to !!isMobile)
    expect(result.current).toBe(false);
  });

  it("responds to window resize events", () => {
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 1024,
    });

    const mockMql = {
      matches: false,
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { result } = renderHook(() => useIsMobile());

    expect(result.current).toBe(false);

    // Simulate window resize to mobile
    act(() => {
      Object.defineProperty(window, "innerWidth", {
        writable: true,
        configurable: true,
        value: 600,
      });
      
      // Trigger the change event that would be called by matchMedia
      const changeHandler = mockMql.addEventListener.mock.calls[0][1];
      changeHandler();
    });

    expect(result.current).toBe(true);
  });

  it("adds and removes event listener correctly", () => {
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 1024,
    });

    const mockMql = {
      matches: false,
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { unmount } = renderHook(() => useIsMobile());

    expect(mockMql.addEventListener).toHaveBeenCalledWith("change", expect.any(Function));

    unmount();

    expect(mockMql.removeEventListener).toHaveBeenCalledWith("change", expect.any(Function));
  });

  it("uses correct breakpoint value (768px)", () => {
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 768,
    });

    const mockMql = {
      matches: false, // 768px should be desktop (not mobile)
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { result } = renderHook(() => useIsMobile());

    expect(result.current).toBe(false);
    expect(mockMatchMedia).toHaveBeenCalledWith("(max-width: 767px)");
  });

  it("handles boundary cases correctly", () => {
    // Test exactly at breakpoint - 1 (767px should be mobile)
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 767,
    });

    const mockMql = {
      matches: true,
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { result } = renderHook(() => useIsMobile());

    expect(result.current).toBe(true);
  });

  it("handles very large screen sizes", () => {
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 2560,
    });

    const mockMql = {
      matches: false,
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { result } = renderHook(() => useIsMobile());

    expect(result.current).toBe(false);
  });

  it("handles very small screen sizes", () => {
    Object.defineProperty(window, "innerWidth", {
      writable: true,
      configurable: true,
      value: 320,
    });

    const mockMql = {
      matches: true,
      media: "(max-width: 767px)",
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
    };

    mockMatchMedia.mockReturnValue(mockMql);

    const { result } = renderHook(() => useIsMobile());

    expect(result.current).toBe(true);
  });

  it("handles matchMedia not available", () => {
    // Test graceful degradation if matchMedia is not available
    window.matchMedia = undefined as any;

    expect(() => {
      renderHook(() => useIsMobile());
    }).toThrow();
  });
});
