package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import com.spmorangle.crm.departmentmgmt.service.DepartmentalVisibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentalVisibilityServiceImpl implements DepartmentalVisibilityService {

    private final DepartmentQueryService departmentQueryService;

    @Override
    public Set<Long> visibilityDepartmentsForAssignedDept(Long leafDept) {
        return departmentQueryService.getDescendants(leafDept, true).stream()
                .map(DepartmentDto::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean canUserSeeTask(Set<Long> userDepartments, Long leafDept) {
        if (leafDept == null) {
            return false;
        }
        return userDepartments.contains(leafDept);
    }

    @Override
    public Set<Long> getChildDepartmentIds(Long leafDept) {
        return departmentQueryService.getChildren(leafDept).stream()
                .map(DepartmentDto::getId)
                .collect(Collectors.toSet());
    }
}
