package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * STUB IMPLEMENTATION - Temporary placeholder for DepartmentQueryService.
 * This allows the application to compile and run while the real implementation is being developed.
 *
 * TODO: Replace this stub with the actual implementation that:
 * - Queries the departments table
 * - Performs recursive hierarchy traversal
 * - Returns proper DepartmentDto objects
 */
@Slf4j
@Service
public class DepartmentQueryServiceImpl implements DepartmentQueryService {

    @Override
    public Optional<DepartmentDto> getById(Long id) {
        log.warn("STUB: DepartmentQueryService.getById({}) - returning empty. Replace with real implementation.", id);
        return Optional.empty();
    }

    @Override
    public Optional<DepartmentDto> getByNameCaseInsensitive(String name) {
        log.warn("STUB: DepartmentQueryService.getByNameCaseInsensitive('{}') - returning empty. Replace with real implementation.", name);
        return Optional.empty();
    }

    @Override
    public Optional<DepartmentDto> getParent(Long id) {
        log.warn("STUB: DepartmentQueryService.getParent({}) - returning empty. Replace with real implementation.", id);
        return Optional.empty();
    }

    @Override
    public List<DepartmentDto> getChildren(Long id) {
        log.warn("STUB: DepartmentQueryService.getChildren({}) - returning empty list. Replace with real implementation.", id);
        return Collections.emptyList();
    }

    @Override
    public List<DepartmentDto> getAncestors(Long id, boolean includeSelf) {
        log.warn("STUB: DepartmentQueryService.getAncestors({}, {}) - returning empty list. Replace with real implementation.", id, includeSelf);
        return Collections.emptyList();
    }

    @Override
    public List<DepartmentDto> getDescendants(Long id, boolean includeSelf) {
        log.warn("STUB: DepartmentQueryService.getDescendants({}, {}) - returning empty list. Replace with real implementation.", id, includeSelf);
        return Collections.emptyList();
    }

    @Override
    public List<DepartmentDto> getRoots() {
        log.warn("STUB: DepartmentQueryService.getRoots() - returning empty list. Replace with real implementation.");
        return Collections.emptyList();
    }

    @Override
    public boolean exists(Long id) {
        log.warn("STUB: DepartmentQueryService.exists({}) - returning false. Replace with real implementation.", id);
        return false;
    }
}
