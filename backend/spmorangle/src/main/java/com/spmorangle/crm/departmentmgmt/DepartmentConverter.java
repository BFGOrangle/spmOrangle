package com.spmorangle.crm.departmentmgmt;

import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.model.Department;

import java.util.ArrayList;

public class DepartmentConverter {
    public static DepartmentDto convert(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .parentId(department.getParentId())
                .build();
    }
}
