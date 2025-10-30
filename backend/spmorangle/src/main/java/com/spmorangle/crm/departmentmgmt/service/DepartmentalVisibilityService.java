package com.spmorangle.crm.departmentmgmt.service;

import java.util.Set;

public interface DepartmentalVisibilityService {

    Set<Long> visibilityDepartmentsForAssignedDept(Long leafDept);

    boolean canUserSeeTask(Set<Long> userDepartments, Long leafDept);

    Set<Long> getChildDepartmentIds(Long leafDept);
}
