package com.spmorangle.common.converter;

import com.spmorangle.common.model.User;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.usermanagement.dto.UserResponseDto;

public class UserConverter {

    public static UserResponseDto convert(User user, DepartmentQueryService departmentQueryService) {
        if (user == null) {
            return null;
        }

        String departmentName = null;
        if (user.getDepartmentId() != null && departmentQueryService != null) {
            departmentName = departmentQueryService.getById(user.getDepartmentId())
                .map(dept -> dept.getName())
                .orElse(null);
        }

        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUserName())
                .email(user.getEmail())
                .roleType(user.getRoleType())
                .isActive(user.getIsActive())
                .department(departmentName)
                .cognitoSub(user.getCognitoSub())
                .build();
    }

    /**
     * Convenience method for backwards compatibility - doesn't populate department name
     * @deprecated Use convert(User, DepartmentQueryService) instead
     */
    @Deprecated
    public static UserResponseDto convert(User user) {
        return convert(user, null);
    }
}
