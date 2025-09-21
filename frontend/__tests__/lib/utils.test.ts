import { cn } from "../../lib/utils";

describe("Utils", () => {
  describe("cn function", () => {
    it("combines class names correctly", () => {
      const result = cn("bg-red-500", "text-white", "p-4");
      expect(result).toContain("bg-red-500");
      expect(result).toContain("text-white");
      expect(result).toContain("p-4");
    });

    it("handles conditional classes", () => {
      const isActive = true;
      const isDisabled = false;
      
      const result = cn(
        "base-class",
        isActive && "active-class",
        isDisabled && "disabled-class"
      );
      
      expect(result).toContain("base-class");
      expect(result).toContain("active-class");
      expect(result).not.toContain("disabled-class");
    });

    it("handles undefined and null values", () => {
      const result = cn("base-class", undefined, null, "another-class");
      
      expect(result).toContain("base-class");
      expect(result).toContain("another-class");
      expect(typeof result).toBe("string");
    });

    it("merges conflicting Tailwind classes correctly", () => {
      // This tests the twMerge functionality
      const result = cn("p-4", "p-2");
      
      // twMerge should keep only the last conflicting class
      expect(result).toContain("p-2");
      expect(result).not.toContain("p-4");
    });

    it("handles empty input", () => {
      const result = cn();
      
      expect(typeof result).toBe("string");
      expect(result.trim()).toBe("");
    });

    it("handles arrays of classes", () => {
      const result = cn(["class1", "class2"], "class3");
      
      expect(result).toContain("class1");
      expect(result).toContain("class2");
      expect(result).toContain("class3");
    });

    it("handles objects with boolean values", () => {
      const result = cn({
        "class1": true,
        "class2": false,
        "class3": true,
      });
      
      expect(result).toContain("class1");
      expect(result).toContain("class3");
      expect(result).not.toContain("class2");
    });

    it("handles mixed input types", () => {
      const result = cn(
        "base",
        ["array1", "array2"],
        {
          "conditional1": true,
          "conditional2": false,
        },
        false && "falsy-class",
        "final-class"
      );
      
      expect(result).toContain("base");
      expect(result).toContain("array1");
      expect(result).toContain("array2");
      expect(result).toContain("conditional1");
      expect(result).toContain("final-class");
      expect(result).not.toContain("conditional2");
      expect(result).not.toContain("falsy-class");
    });

    it("handles complex Tailwind merge scenarios", () => {
      // Test that twMerge correctly handles responsive and state variants
      const result = cn("hover:bg-red-500", "hover:bg-blue-500");
      
      expect(result).toContain("hover:bg-blue-500");
      expect(result).not.toContain("hover:bg-red-500");
    });

    it("handles margin and padding merge conflicts", () => {
      const result = cn("m-2", "mx-4", "ml-8");
      
      // tailwind-merge keeps all conflicting classes and relies on CSS specificity
      expect(result).toBe("m-2 mx-4 ml-8");
    });

    it("handles string interpolation", () => {
      const size = "lg";
      const variant = "primary";
      
      const result = cn(`text-${size}`, `bg-${variant}`);
      
      expect(result).toContain("text-lg");
      expect(result).toContain("bg-primary");
    });

    it("preserves whitespace handling", () => {
      const result = cn("  class1  ", "  class2  ");
      
      expect(result).toContain("class1");
      expect(result).toContain("class2");
      expect(result).not.toMatch(/^\s/); // No leading whitespace
      expect(result).not.toMatch(/\s$/); // No trailing whitespace
    });

    it("handles duplicate class names", () => {
      const result = cn("bg-red-500", "text-white", "bg-red-500");
      
      // Should deduplicate
      const matches = result.match(/bg-red-500/g);
      expect(matches).toHaveLength(1);
    });

    it("works with component variants pattern", () => {
      const variants = {
        size: {
          sm: "text-sm p-2",
          md: "text-base p-4",
          lg: "text-lg p-6",
        },
        variant: {
          primary: "bg-blue-500 text-white",
          secondary: "bg-gray-500 text-white",
        },
      };

      const result = cn(
        "base-button-class",
        variants.size.md,
        variants.variant.primary,
        "additional-class"
      );

      expect(result).toContain("base-button-class");
      expect(result).toContain("text-base");
      expect(result).toContain("p-4");
      expect(result).toContain("bg-blue-500");
      expect(result).toContain("text-white");
      expect(result).toContain("additional-class");
    });
  });
});
