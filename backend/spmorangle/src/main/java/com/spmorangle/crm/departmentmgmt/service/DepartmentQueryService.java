package com.spmorangle.crm.departmentmgmt.service;

import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;

import java.util.List;
import java.util.Optional;

public interface DepartmentQueryService {

    Optional<DepartmentDto> getById(Long id);

    Optional<DepartmentDto> getByNameCaseInsensitive(String name);

    Optional<DepartmentDto> getParent(Long id);

    List<DepartmentDto> getChildren(Long id);

    List<DepartmentDto> getAncestors(Long id, boolean includeSelf);

    List<DepartmentDto> getDescendants(Long id, boolean includeSelf);

    List<DepartmentDto> getRoots();

    boolean exists(Long id);

    List<String> getPathNames(Long id, boolean includeSelf);
}
