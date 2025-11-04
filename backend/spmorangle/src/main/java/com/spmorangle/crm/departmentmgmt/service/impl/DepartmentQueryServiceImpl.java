package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.crm.departmentmgmt.DepartmentConverter;
import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.model.Department;
import com.spmorangle.crm.departmentmgmt.repository.DepartmentRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentQueryServiceImpl implements DepartmentQueryService {

    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public Optional<DepartmentDto> getById(Long id) {
        return departmentRepository.findById(id).map(DepartmentConverter::convert);
    }

    /**
     * Returns the immediate children of the given department id.
     * If id is null, returns all root departments.
     */
    @Transactional(readOnly = true)
    public List<DepartmentDto> getSubDepartments(Long id) {
        List<DepartmentDto> all = departmentRepository.findAll().stream()
                .map(DepartmentConverter::convert)
                .toList();

        // Build parentId -> children (immediate) directly from flat list
        Map<Long, List<DepartmentDto>> idx = all.stream()
                .filter(d -> d.getParentId() != null)
                .collect(Collectors.groupingBy(DepartmentDto::getParentId));

        List<DepartmentDto> children = idx.getOrDefault(id, List.of());

        return children.stream()
                .toList();
    }

    /**
     * Returns the parent of the given department. If includeSelf, returns self when no parent exists.
     */
    @Transactional(readOnly = true)
    public Optional<DepartmentDto> getParentDepartment(Long id, boolean includeSelf) {
        Department child = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + id));
        Long parentId = child.getParentId();
        if (parentId == null) {
            if (includeSelf) return Optional.of(DepartmentConverter.convert(child));
            return Optional.empty();
        }

        return departmentRepository.findById(parentId)
                .map(DepartmentConverter::convert);

//        Department parent = departmentRepository.findById(parentId)
//                .orElseThrow(() -> new IllegalArgumentException("Parent Department not found with id: " + parentId));
//        return DepartmentConverter.convert(parent);
    }
}
