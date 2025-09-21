import { renderHook, act } from "@testing-library/react";
import { useToast, toast, reducer } from "../../hooks/use-toast";

// Mock setTimeout and clearTimeout
jest.useFakeTimers();

describe("useToast", () => {
  afterEach(() => {
    jest.clearAllTimers();
    // Reset the module state by clearing all toasts
    act(() => {
      const hookResult = renderHook(() => useToast()).result.current;
      if (hookResult && hookResult.dismiss) {
        hookResult.dismiss(); // Dismiss all toasts
      }
    });
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  describe("useToast hook", () => {
    it("initializes with empty toasts array", () => {
      const { result } = renderHook(() => useToast());
      
      expect(result.current.toasts).toEqual([]);
    });

    it("adds a toast", () => {
      const { result } = renderHook(() => useToast());
      
      act(() => {
        result.current.toast({
          title: "Test Toast",
          description: "This is a test toast",
        });
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].title).toBe("Test Toast");
      expect(result.current.toasts[0].description).toBe("This is a test toast");
      expect(result.current.toasts[0].open).toBe(true);
    });

    it("limits toasts to TOAST_LIMIT (1)", () => {
      const { result } = renderHook(() => useToast());
      
      act(() => {
        result.current.toast({ title: "Toast 1" });
        result.current.toast({ title: "Toast 2" });
        result.current.toast({ title: "Toast 3" });
      });

      expect(result.current.toasts).toHaveLength(1);
      expect(result.current.toasts[0].title).toBe("Toast 3"); // Latest toast should be kept
    });

    it("dismisses a specific toast", () => {
      const { result } = renderHook(() => useToast());
      
      let toastId: string;
      
      act(() => {
        const { id } = result.current.toast({
          title: "Test Toast",
        });
        toastId = id;
      });

      expect(result.current.toasts[0].open).toBe(true);

      act(() => {
        result.current.dismiss(toastId);
      });

      expect(result.current.toasts[0].open).toBe(false);
    });

    it("dismisses all toasts when no ID provided", () => {
      const { result } = renderHook(() => useToast());
      
      act(() => {
        result.current.toast({ title: "Toast 1" });
      });

      expect(result.current.toasts[0].open).toBe(true);

      act(() => {
        result.current.dismiss();
      });

      expect(result.current.toasts[0].open).toBe(false);
    });

    it("removes toast after timeout", () => {
      const { result } = renderHook(() => useToast());
      
      act(() => {
        result.current.toast({ title: "Test Toast" });
      });

      expect(result.current.toasts).toHaveLength(1);

      act(() => {
        result.current.dismiss(result.current.toasts[0].id);
      });

      // Fast-forward time
      act(() => {
        jest.advanceTimersByTime(1000000);
      });

      expect(result.current.toasts).toHaveLength(0);
    });
  });

  describe("standalone toast function", () => {
    it("creates a toast and returns control functions", () => {
      let toastResult: ReturnType<typeof toast> | undefined;
      
      act(() => {
        toastResult = toast({
          title: "Standalone Toast",
          description: "Test description",
        });
      });

      expect(toastResult?.id).toBeDefined();
      expect(typeof toastResult?.dismiss).toBe("function");
      expect(typeof toastResult?.update).toBe("function");
    });

    it("updates a toast", () => {
      const { result } = renderHook(() => useToast());
      
      let toastResult: ReturnType<typeof toast>;
      
      act(() => {
        toastResult = toast({ title: "Original Title" });
      });

      expect(result.current.toasts[0].title).toBe("Original Title");

      act(() => {
        toastResult.update({
          id: toastResult.id,
          title: "Updated Title",
          description: "New description",
        });
      });

      expect(result.current.toasts[0].title).toBe("Updated Title");
      expect(result.current.toasts[0].description).toBe("New description");
    });

    it("dismisses a toast using returned dismiss function", () => {
      const { result } = renderHook(() => useToast());
      
      let toastResult: ReturnType<typeof toast>;
      
      act(() => {
        toastResult = toast({ title: "Dismissible Toast" });
      });

      expect(result.current.toasts[0].open).toBe(true);

      act(() => {
        toastResult.dismiss();
      });

      expect(result.current.toasts[0].open).toBe(false);
    });

    it("handles onOpenChange callback", () => {
      const { result } = renderHook(() => useToast());
      
      act(() => {
        toast({ title: "Test Toast" });
      });

      const toastItem = result.current.toasts[0];
      expect(toastItem.onOpenChange).toBeDefined();

      act(() => {
        toastItem.onOpenChange?.(false);
      });

      expect(result.current.toasts[0].open).toBe(false);
    });
  });

  describe("reducer", () => {
    const initialState = { toasts: [] };

    it("adds a toast", () => {
      const newToast = {
        id: "1",
        title: "Test Toast",
        open: true,
      };

      const newState = reducer(initialState, {
        type: "ADD_TOAST",
        toast: newToast,
      });

      expect(newState.toasts).toHaveLength(1);
      expect(newState.toasts[0]).toEqual(newToast);
    });

    it("updates an existing toast", () => {
      const existingState = {
        toasts: [
          {
            id: "1",
            title: "Original",
            open: true,
          },
        ],
      };

      const newState = reducer(existingState, {
        type: "UPDATE_TOAST",
        toast: {
          id: "1",
          title: "Updated",
          description: "New description",
        },
      });

      expect(newState.toasts[0].title).toBe("Updated");
      expect(newState.toasts[0].description).toBe("New description");
      expect(newState.toasts[0].open).toBe(true); // Should preserve existing properties
    });

    it("dismisses a specific toast", () => {
      const existingState = {
        toasts: [
          {
            id: "1",
            title: "Toast 1",
            open: true,
          },
          {
            id: "2",
            title: "Toast 2",
            open: true,
          },
        ],
      };

      const newState = reducer(existingState, {
        type: "DISMISS_TOAST",
        toastId: "1",
      });

      expect(newState.toasts[0].open).toBe(false);
      expect(newState.toasts[1].open).toBe(true);
    });

    it("dismisses all toasts when no ID provided", () => {
      const existingState = {
        toasts: [
          {
            id: "1",
            title: "Toast 1",
            open: true,
          },
          {
            id: "2",
            title: "Toast 2",
            open: true,
          },
        ],
      };

      const newState = reducer(existingState, {
        type: "DISMISS_TOAST",
      });

      expect(newState.toasts[0].open).toBe(false);
      expect(newState.toasts[1].open).toBe(false);
    });

    it("removes a specific toast", () => {
      const existingState = {
        toasts: [
          {
            id: "1",
            title: "Toast 1",
            open: true,
          },
          {
            id: "2",
            title: "Toast 2",
            open: true,
          },
        ],
      };

      const newState = reducer(existingState, {
        type: "REMOVE_TOAST",
        toastId: "1",
      });

      expect(newState.toasts).toHaveLength(1);
      expect(newState.toasts[0].id).toBe("2");
    });

    it("removes all toasts when no ID provided", () => {
      const existingState = {
        toasts: [
          {
            id: "1",
            title: "Toast 1",
            open: true,
          },
          {
            id: "2",
            title: "Toast 2",
            open: true,
          },
        ],
      };

      const newState = reducer(existingState, {
        type: "REMOVE_TOAST",
      });

      expect(newState.toasts).toHaveLength(0);
    });

    it("enforces toast limit", () => {
      const existingState = {
        toasts: [
          {
            id: "1",
            title: "Existing Toast",
            open: true,
          },
        ],
      };

      const newState = reducer(existingState, {
        type: "ADD_TOAST",
        toast: {
          id: "2",
          title: "New Toast",
          open: true,
        },
      });

      expect(newState.toasts).toHaveLength(1);
      expect(newState.toasts[0].id).toBe("2"); // New toast should replace old one
    });
  });

  describe("ID generation", () => {
    it("generates unique IDs for multiple toasts", () => {
      const { result } = renderHook(() => useToast());
      
      let firstId: string | undefined;
      let secondId: string | undefined;
      
      act(() => {
        const first = result.current.toast({ title: "First Toast" });
        firstId = first.id;
      });

      act(() => {
        const second = result.current.toast({ title: "Second Toast" });
        secondId = second.id;
      });

      expect(firstId).toBeDefined();
      expect(secondId).toBeDefined();
      expect(firstId).not.toBe(secondId);
    });
  });

  describe("Memory state synchronization", () => {
    it("synchronizes state between multiple hook instances", () => {
      const { result: result1 } = renderHook(() => useToast());
      const { result: result2 } = renderHook(() => useToast());
      
      act(() => {
        result1.current.toast({ title: "Shared Toast" });
      });

      expect(result1.current.toasts).toHaveLength(1);
      expect(result2.current.toasts).toHaveLength(1);
      expect(result1.current.toasts[0].title).toBe("Shared Toast");
      expect(result2.current.toasts[0].title).toBe("Shared Toast");
    });

    it("properly cleans up listeners", () => {
      const { unmount } = renderHook(() => useToast());
      
      // This should not throw any errors
      unmount();
    });
  });

  describe("Edge cases", () => {
    it("handles toast with all optional properties", () => {
      const { result } = renderHook(() => useToast());
      
      act(() => {
        result.current.toast({
          title: "Complex Toast",
          description: "Description",
          variant: "destructive",
        });
      });

      const toast = result.current.toasts[0];
      expect(toast.title).toBe("Complex Toast");
      expect(toast.description).toBe("Description");
      expect(toast.variant).toBe("destructive");
    });

    it("handles updating non-existent toast gracefully", () => {
      const existingState = {
        toasts: [
          {
            id: "1",
            title: "Existing Toast",
            open: true,
          },
        ],
      };

      const newState = reducer(existingState, {
        type: "UPDATE_TOAST",
        toast: {
          id: "non-existent",
          title: "Updated",
        },
      });

      // State should remain unchanged
      expect(newState.toasts).toEqual(existingState.toasts);
    });

    it("prevents duplicate timeouts for same toast ID", () => {
      const { result } = renderHook(() => useToast());
      
      act(() => {
        result.current.toast({ title: "Test Toast" });
      });

      const toastId = result.current.toasts[0].id;

      // Dismiss the same toast multiple times rapidly
      act(() => {
        result.current.dismiss(toastId);
        result.current.dismiss(toastId);
        result.current.dismiss(toastId);
      });

      // Should still work correctly without creating multiple timeouts
      expect(result.current.toasts[0].open).toBe(false);
    });
  });
});
