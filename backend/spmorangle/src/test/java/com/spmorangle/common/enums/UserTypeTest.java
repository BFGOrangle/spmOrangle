package com.spmorangle.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTypeTest {

    @Test
    void userType_ShouldHaveCorrectValues() {
        // Test DIRECTOR
        assertEquals("DIRECTOR", UserType.DIRECTOR.getCode());
        assertEquals(3, UserType.DIRECTOR.getRank());

        // Test MANAGER
        assertEquals("MANAGER", UserType.MANAGER.getCode());
        assertEquals(2, UserType.MANAGER.getRank());

        // Test HR
        assertEquals("HR", UserType.HR.getCode());
        assertEquals(1, UserType.HR.getRank());
        // Test STAFF
        assertEquals("STAFF", UserType.STAFF.getCode());
        assertEquals(1, UserType.STAFF.getRank());
    }

    @Test
    void valueOf_WithValidString_ShouldReturnCorrectEnum() {
        assertEquals(UserType.DIRECTOR, UserType.valueOf("DIRECTOR"));
        assertEquals(UserType.MANAGER, UserType.valueOf("MANAGER"));
        assertEquals(UserType.HR, UserType.valueOf("HR"));
        assertEquals(UserType.STAFF, UserType.valueOf("STAFF"));
    }

    @Test
    void valueOf_WithInvalidString_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> UserType.valueOf("INVALID"));
        assertThrows(IllegalArgumentException.class, () -> UserType.valueOf("admin"));
        assertThrows(IllegalArgumentException.class, () -> UserType.valueOf(""));
    }

    @Test
    void values_ShouldReturnAllEnumValues() {
        UserType[] values = UserType.values();
        assertEquals(4, values.length);
        assertTrue(java.util.Arrays.asList(values).contains(UserType.DIRECTOR));
        assertTrue(java.util.Arrays.asList(values).contains(UserType.MANAGER));
        assertTrue(java.util.Arrays.asList(values).contains(UserType.HR));
        assertTrue(java.util.Arrays.asList(values).contains(UserType.STAFF));
    }

    @Test
    void name_ShouldReturnCorrectName() {
        assertEquals("DIRECTOR", UserType.DIRECTOR.name());
        assertEquals("MANAGER", UserType.MANAGER.name());
        assertEquals("HR", UserType.HR.name());
        assertEquals("STAFF", UserType.STAFF.name());
    }
}

