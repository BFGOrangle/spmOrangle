package com.spmorangle.crm.departmentmgmt.service;

import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;

import java.util.List;
import java.util.Optional;


public interface DepartmentQueryService {

    Optional<DepartmentDto> getById(Long id);

    List<DepartmentDto> getSubDepartments(Long id);

    DepartmentDto getParentDepartment(Long id, boolean includeSelf);

}
