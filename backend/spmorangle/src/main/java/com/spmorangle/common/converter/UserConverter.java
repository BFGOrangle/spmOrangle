package com.spmorangle.common.converter;

import com.spmorangle.common.model.User;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;

public class UserConverter {
    public static UserResponseDto convert(User user) {
        if (user == null) {
            return null;
        }
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUserName())
                .email(user.getEmail())
                .roleType(user.getRoleType())
                .cognitoSub(user.getCognitoSub())
                .build();

    }
}
