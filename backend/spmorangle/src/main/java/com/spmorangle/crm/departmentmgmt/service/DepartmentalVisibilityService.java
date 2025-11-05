package com.spmorangle.crm.departmentmgmt.service;

import java.util.Set;

public interface DepartmentalVisibilityService {

    Set<Long> visibleDepartmentsForAssignedDept(Long dept);

    boolean canUserSeeTask(Long userId);

    boolean canUserSeeTask(Set<Long> viewingUserVisibleDeptIds, Long taskAssigneeId);

}
