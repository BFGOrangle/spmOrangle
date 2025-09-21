import {
  ROLE_TYPE_DISPLAY,
  COMMON_JOB_TITLES,
  StaffUtils,
  type StaffResponseDto,
  type StaffFormData,
  type StaffUpdateFormData,
  type StaffDisplayView,
  type StaffFilterOptions,
  type RoleType,
} from "@/types/staff";

describe("Staff Types and Utils", () => {
  const mockStaffResponse: StaffResponseDto = {
    id: 1,
    cognitoSub: "12345-uuid",
    centerId: 1,
    centerName: "Test Center",
    firstName: "John",
    lastName: "Doe",
    fullName: "John Doe",
    jobTitle: "Care Coordinator",
    contactPhone: "+1234567890",
    contactEmail: "john.doe@example.com",
    roleType: "STAFF" as RoleType,
    isActive: true,
    lastLoginAt: "2024-01-01T10:00:00Z",
    createdAt: "2023-12-01T09:00:00Z",
    updatedAt: "2024-01-01T10:00:00Z",
  };

  const mockFormData: StaffFormData = {
    firstName: "Jane",
    lastName: "Smith",
    jobTitle: "Nurse",
    contactPhone: "+1987654321",
    contactEmail: "jane.smith@example.com",
    password: "SecurePass123!",
    roleType: "ADMIN" as RoleType,
  };

  describe("Constants", () => {
    test("ROLE_TYPE_DISPLAY contains correct mappings", () => {
      expect(ROLE_TYPE_DISPLAY.ADMIN).toBe("Administrator");
      expect(ROLE_TYPE_DISPLAY.STAFF).toBe("Staff Member");
    });

    test("COMMON_JOB_TITLES contains expected job titles", () => {
      expect(COMMON_JOB_TITLES).toContain("Care Coordinator");
      expect(COMMON_JOB_TITLES).toContain("Nurse");
      expect(COMMON_JOB_TITLES).toContain("Manager");
      expect(COMMON_JOB_TITLES).toContain("Other");
    });
  });

  describe("StaffUtils.toDisplayView", () => {
    test("converts StaffResponseDto to StaffDisplayView correctly", () => {
      const result = StaffUtils.toDisplayView(mockStaffResponse);

      expect(result).toEqual({
        ...mockStaffResponse,
        statusBadge: "active",
        roleDisplay: "Staff Member",
        lastLoginDisplay: "1/1/2024",
        formattedCreatedAt: "12/1/2023",
      });
    });

    test("handles inactive staff member", () => {
      const inactiveStaff = { ...mockStaffResponse, isActive: false };
      const result = StaffUtils.toDisplayView(inactiveStaff);

      expect(result.statusBadge).toBe("inactive");
    });

    test("handles null lastLoginAt", () => {
      const staffWithoutLogin = { ...mockStaffResponse, lastLoginAt: null };
      const result = StaffUtils.toDisplayView(staffWithoutLogin);

      expect(result.lastLoginDisplay).toBe("Never");
    });

    test("handles unknown role type", () => {
      const staffWithUnknownRole = {
        ...mockStaffResponse,
        roleType: "UNKNOWN" as RoleType,
      };
      const result = StaffUtils.toDisplayView(staffWithUnknownRole);

      expect(result.roleDisplay).toBe("UNKNOWN");
    });
  });

  describe("StaffUtils.formDataToCreateDto", () => {
    test("converts form data to CreateStaffDto correctly", () => {
      const result = StaffUtils.formDataToCreateDto(mockFormData);

      expect(result).toEqual({
        firstName: "Jane",
        lastName: "Smith",
        jobTitle: "Nurse",
        contactPhone: "+1987654321",
        contactEmail: "jane.smith@example.com",
        password: "SecurePass123!",
        roleType: "ADMIN",
      });
    });

    test("trims whitespace from string fields", () => {
      const formDataWithSpaces = {
        ...mockFormData,
        firstName: "  Jane  ",
        lastName: "  Smith  ",
        jobTitle: "  Nurse  ",
        contactEmail: "  jane.smith@example.com  ",
      };

      const result = StaffUtils.formDataToCreateDto(formDataWithSpaces);

      expect(result.firstName).toBe("Jane");
      expect(result.lastName).toBe("Smith");
      expect(result.jobTitle).toBe("Nurse");
      expect(result.contactEmail).toBe("jane.smith@example.com");
    });

    test("handles empty contactPhone", () => {
      const formDataWithEmptyPhone = {
        ...mockFormData,
        contactPhone: "",
      };

      const result = StaffUtils.formDataToCreateDto(formDataWithEmptyPhone);

      expect(result.contactPhone).toBeNull();
    });

    test("handles whitespace-only contactPhone", () => {
      const formDataWithSpacePhone = {
        ...mockFormData,
        contactPhone: "   ",
      };

      const result = StaffUtils.formDataToCreateDto(formDataWithSpacePhone);

      expect(result.contactPhone).toBeNull();
    });
  });

  describe("StaffUtils.formDataToUpdateDto", () => {
    const updateFormData: StaffUpdateFormData = {
      firstName: "Updated Jane",
      lastName: "Updated Smith",
      jobTitle: "Senior Nurse",
      contactPhone: "+1555666777",
      contactEmail: "updated.jane@example.com",
      roleType: "STAFF" as RoleType,
      isActive: false,
    };

    test("converts update form data correctly", () => {
      const result = StaffUtils.formDataToUpdateDto(updateFormData);

      expect(result).toEqual({
        firstName: "Updated Jane",
        lastName: "Updated Smith",
        jobTitle: "Senior Nurse",
        contactPhone: "+1555666777",
        contactEmail: "updated.jane@example.com",
        roleType: "STAFF",
        isActive: false,
      });
    });

    test("handles empty contactPhone in update", () => {
      const updateWithEmptyPhone = { ...updateFormData, contactPhone: "" };
      const result = StaffUtils.formDataToUpdateDto(updateWithEmptyPhone);

      expect(result.contactPhone).toBeNull();
    });
  });

  describe("StaffUtils.dtoToFormData", () => {
    test("converts StaffResponseDto to form data correctly", () => {
      const result = StaffUtils.dtoToFormData(mockStaffResponse);

      expect(result).toEqual({
        firstName: "John",
        lastName: "Doe",
        jobTitle: "Care Coordinator",
        contactPhone: "+1234567890",
        contactEmail: "john.doe@example.com",
        roleType: "STAFF",
        isActive: true,
      });
    });

    test("handles null contactPhone", () => {
      const staffWithNullPhone = { ...mockStaffResponse, contactPhone: null };
      const result = StaffUtils.dtoToFormData(staffWithNullPhone);

      expect(result.contactPhone).toBe("");
    });
  });

  describe("StaffUtils.getInitials", () => {
    test("gets initials from full name", () => {
      expect(StaffUtils.getInitials("John Doe")).toBe("JD");
      expect(StaffUtils.getInitials("Jane Mary Smith")).toBe("JMS");
      expect(StaffUtils.getInitials("SingleName")).toBe("S");
    });

    test("handles lowercase names", () => {
      expect(StaffUtils.getInitials("john doe")).toBe("JD");
    });

    test("handles extra spaces", () => {
      expect(StaffUtils.getInitials("John  Doe")).toBe("JD");
    });
  });

  describe("StaffUtils.formatDate", () => {
    test("formats valid date string", () => {
      const result = StaffUtils.formatDate("2024-01-01T10:00:00Z");
      expect(result).toBe("1/1/2024");
    });

    test("handles null date", () => {
      expect(StaffUtils.formatDate(null)).toBe("N/A");
    });

    test("handles empty string", () => {
      expect(StaffUtils.formatDate("")).toBe("N/A");
    });
  });

  describe("StaffUtils.formatDateTime", () => {
    test("formats datetime string", () => {
      const result = StaffUtils.formatDateTime("2024-01-01T10:00:00Z");
      // Note: actual result depends on timezone, just check it's a string
      expect(typeof result).toBe("string");
      expect(result.length).toBeGreaterThan(0);
    });
  });

  describe("StaffUtils.filterStaff", () => {
    const staffList: StaffDisplayView[] = [
      {
        ...mockStaffResponse,
        id: 1,
        fullName: "John Doe",
        roleType: "STAFF" as RoleType,
        jobTitle: "Nurse",
        isActive: true,
        statusBadge: "active",
        roleDisplay: "Staff Member",
        lastLoginDisplay: "1/1/2024",
        formattedCreatedAt: "12/1/2023",
      },
      {
        ...mockStaffResponse,
        id: 2,
        firstName: "Jane",
        lastName: "Smith", 
        fullName: "Jane Smith",
        contactEmail: "jane.smith@example.com",
        roleType: "ADMIN" as RoleType,
        jobTitle: "Manager",
        isActive: false,
        statusBadge: "inactive",
        roleDisplay: "Administrator",
        lastLoginDisplay: "Never",
        formattedCreatedAt: "11/1/2023",
      },
    ];

    test("filters by role type", () => {
      const filters: StaffFilterOptions = { roleType: ["ADMIN"] };
      const result = StaffUtils.filterStaff(staffList, filters);

      expect(result).toHaveLength(1);
      expect(result[0].roleType).toBe("ADMIN");
    });

    test("filters by active status", () => {
      const filters: StaffFilterOptions = { isActive: true };
      const result = StaffUtils.filterStaff(staffList, filters);

      expect(result).toHaveLength(1);
      expect(result[0].isActive).toBe(true);
    });

    test("filters by job title", () => {
      const filters: StaffFilterOptions = { jobTitle: ["Nurse"] };
      const result = StaffUtils.filterStaff(staffList, filters);

      expect(result).toHaveLength(1);
      expect(result[0].jobTitle).toBe("Nurse");
    });

    test("filters by search term", () => {
      const filters: StaffFilterOptions = { searchTerm: "john" };
      const result = StaffUtils.filterStaff(staffList, filters);

      expect(result).toHaveLength(1);
      expect(result[0].fullName).toBe("John Doe");
    });

    test("applies multiple filters", () => {
      const filters: StaffFilterOptions = {
        roleType: ["STAFF"],
        isActive: true,
        searchTerm: "john",
      };
      const result = StaffUtils.filterStaff(staffList, filters);

      expect(result).toHaveLength(1);
      expect(result[0].fullName).toBe("John Doe");
    });

    test("returns empty array when no matches", () => {
      const filters: StaffFilterOptions = { searchTerm: "nonexistent" };
      const result = StaffUtils.filterStaff(staffList, filters);

      expect(result).toHaveLength(0);
    });
  });

  describe("StaffUtils.sortStaff", () => {
    const staffList: StaffDisplayView[] = [
      {
        ...mockStaffResponse,
        id: 1,
        fullName: "Charlie Brown",
        statusBadge: "active",
        roleDisplay: "Staff Member",
        lastLoginDisplay: "1/1/2024",
        formattedCreatedAt: "12/1/2023",
      },
      {
        ...mockStaffResponse,
        id: 2,
        fullName: "Alice Johnson",
        statusBadge: "inactive",
        roleDisplay: "Administrator",
        lastLoginDisplay: "Never",
        formattedCreatedAt: "11/1/2023",
      },
    ];

    test("sorts by fullName ascending", () => {
      const result = StaffUtils.sortStaff(staffList, "fullName", "asc");

      expect(result[0].fullName).toBe("Alice Johnson");
      expect(result[1].fullName).toBe("Charlie Brown");
    });

    test("sorts by fullName descending", () => {
      const result = StaffUtils.sortStaff(staffList, "fullName", "desc");

      expect(result[0].fullName).toBe("Charlie Brown");
      expect(result[1].fullName).toBe("Alice Johnson");
    });

    test("handles null values", () => {
      const staffWithNull = [
        { ...staffList[0], contactPhone: null },
        { ...staffList[1], contactPhone: "+1234567890" },
      ];

      const result = StaffUtils.sortStaff(
        staffWithNull,
        "contactPhone",
        "asc",
      );

      expect(result).toHaveLength(2);
    });

    test("defaults to ascending sort", () => {
      const result = StaffUtils.sortStaff(staffList, "fullName");

      expect(result[0].fullName).toBe("Alice Johnson");
      expect(result[1].fullName).toBe("Charlie Brown");
    });
  });

  describe("StaffUtils.isValidEmail", () => {
    test("validates correct email", () => {
      expect(StaffUtils.isValidEmail("test@example.com")).toBe(true);
      expect(StaffUtils.isValidEmail("user.name@domain.co.uk")).toBe(true);
    });

    test("rejects invalid email", () => {
      expect(StaffUtils.isValidEmail("invalid.email")).toBe(false);
      expect(StaffUtils.isValidEmail("@domain.com")).toBe(false);
      expect(StaffUtils.isValidEmail("user@")).toBe(false);
      expect(StaffUtils.isValidEmail("")).toBe(false);
    });
  });

  describe("StaffUtils.validatePassword", () => {
    test("validates strong password", () => {
      const result = StaffUtils.validatePassword("StrongPass123!");

      expect(result.isValid).toBe(true);
      expect(result.errors).toHaveLength(0);
    });

    test("rejects password too short", () => {
      const result = StaffUtils.validatePassword("Short1!");

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        "Password must be at least 8 characters long",
      );
    });

    test("rejects password without lowercase", () => {
      const result = StaffUtils.validatePassword("PASSWORD123!");

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        "Password must contain at least one lowercase letter",
      );
    });

    test("rejects password without uppercase", () => {
      const result = StaffUtils.validatePassword("password123!");

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        "Password must contain at least one uppercase letter",
      );
    });

    test("rejects password without number", () => {
      const result = StaffUtils.validatePassword("Password!");

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        "Password must contain at least one number",
      );
    });

    test("rejects password without special character", () => {
      const result = StaffUtils.validatePassword("Password123");

      expect(result.isValid).toBe(false);
      expect(result.errors).toContain(
        "Password must contain at least one special character",
      );
    });

    test("returns multiple errors for weak password", () => {
      const result = StaffUtils.validatePassword("weak");

      expect(result.isValid).toBe(false);
      expect(result.errors.length).toBeGreaterThan(1);
    });
  });
});
