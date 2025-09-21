import { type PaginatedResponse, type Pageable } from "@/types/common";

describe("Common Types", () => {
  describe("PaginatedResponse", () => {
    test("should handle string content type", () => {
      const response: PaginatedResponse<string> = {
        content: ["item1", "item2", "item3"],
        totalElements: 3,
        totalPages: 1,
        last: true,
        first: true,
        numberOfElements: 3,
        size: 10,
        number: 0,
        empty: false,
      };

      expect(response.content).toHaveLength(3);
      expect(response.totalElements).toBe(3);
      expect(response.totalPages).toBe(1);
      expect(response.first).toBe(true);
      expect(response.last).toBe(true);
      expect(response.empty).toBe(false);
    });

    test("should handle empty paginated response", () => {
      const response: PaginatedResponse<any> = {
        content: [],
        totalElements: 0,
        totalPages: 0,
        last: true,
        first: true,
        numberOfElements: 0,
        size: 10,
        number: 0,
        empty: true,
      };

      expect(response.content).toHaveLength(0);
      expect(response.totalElements).toBe(0);
      expect(response.totalPages).toBe(0);
      expect(response.empty).toBe(true);
    });

    test("should handle object content type", () => {
      interface TestObject {
        id: number;
        name: string;
      }

      const response: PaginatedResponse<TestObject> = {
        content: [
          { id: 1, name: "Test 1" },
          { id: 2, name: "Test 2" },
        ],
        totalElements: 2,
        totalPages: 1,
        last: true,
        first: true,
        numberOfElements: 2,
        size: 10,
        number: 0,
        empty: false,
      };

      expect(response.content[0]).toEqual({ id: 1, name: "Test 1" });
      expect(response.content[1]).toEqual({ id: 2, name: "Test 2" });
      expect(response.size).toBe(10);
      expect(response.number).toBe(0);
    });

    test("should handle multi-page response", () => {
      const response: PaginatedResponse<number> = {
        content: [1, 2, 3, 4, 5],
        totalElements: 15,
        totalPages: 3,
        last: false,
        first: false,
        numberOfElements: 5,
        size: 5,
        number: 1,
        empty: false,
      };

      expect(response.number).toBe(1); // Current page
      expect(response.totalPages).toBe(3);
      expect(response.first).toBe(false);
      expect(response.last).toBe(false);
      expect(response.totalElements).toBe(15);
    });

    test("should handle first page", () => {
      const response: PaginatedResponse<string> = {
        content: ["a", "b", "c"],
        totalElements: 10,
        totalPages: 4,
        last: false,
        first: true,
        numberOfElements: 3,
        size: 3,
        number: 0,
        empty: false,
      };

      expect(response.first).toBe(true);
      expect(response.last).toBe(false);
      expect(response.number).toBe(0);
    });

    test("should handle last page", () => {
      const response: PaginatedResponse<string> = {
        content: ["x"],
        totalElements: 10,
        totalPages: 4,
        last: true,
        first: false,
        numberOfElements: 1,
        size: 3,
        number: 3,
        empty: false,
      };

      expect(response.first).toBe(false);
      expect(response.last).toBe(true);
      expect(response.number).toBe(3);
      expect(response.numberOfElements).toBe(1); // Only 1 item on last page
    });
  });

  describe("Pageable", () => {
    test("should create basic pageable", () => {
      const pageable: Pageable = {
        page: 0,
        size: 20,
        sort: [],
      };

      expect(pageable.page).toBe(0);
      expect(pageable.size).toBe(20);
      expect(pageable.sort).toEqual([]);
    });

    test("should handle sorted pageable", () => {
      const pageable: Pageable = {
        page: 2,
        size: 10,
        sort: ["name,asc", "createdAt,desc"],
      };

      expect(pageable.page).toBe(2);
      expect(pageable.size).toBe(10);
      expect(pageable.sort).toHaveLength(2);
      expect(pageable.sort).toContain("name,asc");
      expect(pageable.sort).toContain("createdAt,desc");
    });

    test("should handle single sort", () => {
      const pageable: Pageable = {
        page: 1,
        size: 5,
        sort: ["id,desc"],
      };

      expect(pageable.sort).toHaveLength(1);
      expect(pageable.sort![0]).toBe("id,desc");
    });

    test("should handle different page sizes", () => {
      const pageables: Pageable[] = [
        { page: 0, size: 5, sort: [] },
        { page: 0, size: 10, sort: [] },
        { page: 0, size: 25, sort: [] },
        { page: 0, size: 50, sort: [] },
      ];

      expect(pageables[0].size).toBe(5);
      expect(pageables[1].size).toBe(10);
      expect(pageables[2].size).toBe(25);
      expect(pageables[3].size).toBe(50);
    });

    test("should handle high page numbers", () => {
      const pageable: Pageable = {
        page: 999,
        size: 10,
        sort: [],
      };

      expect(pageable.page).toBe(999);
    });

    test("should handle optional properties", () => {
      const minimalPageable: Pageable = {};
      expect(minimalPageable.page).toBeUndefined();
      expect(minimalPageable.size).toBeUndefined();
      expect(minimalPageable.sort).toBeUndefined();

      const partialPageable: Pageable = { page: 1 };
      expect(partialPageable.page).toBe(1);
      expect(partialPageable.size).toBeUndefined();
    });
  });

  describe("Type Integration", () => {
    test("should work together in realistic scenario", () => {
      interface User {
        id: number;
        email: string;
        name: string;
      }

      const pageable: Pageable = {
        page: 1,
        size: 2,
        sort: ["name,asc"],
      };

      const response: PaginatedResponse<User> = {
        content: [
          { id: 3, email: "charlie@test.com", name: "Charlie" },
          { id: 4, email: "david@test.com", name: "David" },
        ],
        totalElements: 10,
        totalPages: 5,
        last: false,
        first: false,
        numberOfElements: 2,
        size: 2,
        number: 1,
        empty: false,
      };

      // Test the pageable and response separately
      expect(pageable.page).toBe(1);
      expect(pageable.size).toBe(2);
      expect(pageable.sort).toContain("name,asc");
      
      expect(response.content).toHaveLength(2);
      expect(response.totalElements).toBe(10);
      expect(response.totalPages).toBe(5);
      expect(response.size).toBe(2);
      expect(response.number).toBe(1);
    });

    test("should handle complex sorting in Pageable", () => {
      const complexPageable: Pageable = {
        page: 0,
        size: 2,
        sort: ["category,asc", "price,desc", "name,asc"],
      };

      expect(complexPageable.sort).toHaveLength(3);
      expect(complexPageable.sort).toEqual([
        "category,asc",
        "price,desc",
        "name,asc",
      ]);
    });

    test("should validate type constraints", () => {
      interface Product {
        id: number;
        name: string;
        price: number;
        category: string;
      }

      const response: PaginatedResponse<Product> = {
        content: [
          { id: 1, name: "Product A", price: 29.99, category: "Electronics" },
          { id: 2, name: "Product B", price: 19.99, category: "Books" },
        ],
        totalElements: 50,
        totalPages: 25,
        last: false,
        first: true,
        numberOfElements: 2,
        size: 2,
        number: 0,
        empty: false,
      };

      expect(response.content).toHaveLength(2);
      expect(response.content[0].id).toBe(1);
      expect(response.content[0].price).toBe(29.99);
      expect(response.totalElements).toBe(50);
    });
  });
});
