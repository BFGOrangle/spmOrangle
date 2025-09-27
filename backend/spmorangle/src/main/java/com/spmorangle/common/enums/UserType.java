package com.spmorangle.common.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum UserType {
    DIRECTOR("DIRECTOR", 3),
    MANAGER("MANAGER", 2),
    HR("HR", 1),
    STAFF("STAFF", 1);

    private static final Set<String> validUserTypes = java.util.Arrays.stream(UserType.values())
            .map(UserType::getCode)
            .collect(java.util.stream.Collectors.toSet());
    private final String code;
    private final int rank;

    UserType(String code, int rank) {
        this.code = code;
        this.rank = rank;
    }

    private static boolean isValidUserType(String userType) {
        return validUserTypes.contains(userType);
    }

    @Override
    public String toString() {
        return this.code;
    }
}

