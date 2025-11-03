package com.spmorangle.crm.departmentmgmt.service.impl;

import com.spmorangle.crm.departmentmgmt.dto.DepartmentDto;
import com.spmorangle.crm.departmentmgmt.model.Department;
import com.spmorangle.crm.departmentmgmt.repository.DepartmentRepository;
import com.spmorangle.crm.departmentmgmt.service.DepartmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentQueryServiceImpl implements DepartmentQueryService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Optional<DepartmentDto> getById(Long id) {
        return departmentRepository.findById(id).map(this::mapToDto);
    }

    @Override
    public Optional<DepartmentDto> getByNameCaseInsensitive(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return departmentRepository.findByNameIgnoreCase(name).map(this::mapToDto);
    }

    @Override
    public Optional<DepartmentDto> getParent(Long id) {
        return departmentRepository.findById(id)
                .map(Department::getParentId)
                .flatMap(parentId -> parentId == null ? Optional.empty() : departmentRepository.findById(parentId))
                .map(this::mapToDto);
    }

    @Override
    public List<DepartmentDto> getChildren(Long id) {
        return departmentRepository.findByParentId(id).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<DepartmentDto> getAncestors(Long id, boolean includeSelf) {
        Map<Long, Department> allDepartments = getAllDepartmentsById();
        List<DepartmentDto> ancestors = new ArrayList<>();

        Department current = allDepartments.get(id);
        if (current == null) {
            return ancestors;
        }

        if (includeSelf) {
            ancestors.add(mapToDto(current));
        }

        Long parentId = current.getParentId();
        while (parentId != null) {
            Department parent = allDepartments.get(parentId);
            if (parent == null) {
                break;
            }
            ancestors.add(mapToDto(parent));
            parentId = parent.getParentId();
        }
        return ancestors;
    }

    @Override
    public List<DepartmentDto> getDescendants(Long id, boolean includeSelf) {
        Map<Long, Department> allDepartments = getAllDepartmentsById();
        List<DepartmentDto> descendants = new ArrayList<>();
        if (allDepartments.isEmpty() || !allDepartments.containsKey(id)) {
            return descendants;
        }

        if (includeSelf) {
            descendants.add(mapToDto(allDepartments.get(id)));
        }

        Deque<Long> stack = new ArrayDeque<>();
        stack.push(id);

        while (!stack.isEmpty()) {
            Long currentId = stack.pop();
            for (Department child : allDepartments.values()) {
                if (currentId.equals(child.getParentId())) {
                    descendants.add(mapToDto(child));
                    stack.push(child.getId());
                }
            }
        }

        return descendants;
    }

    @Override
    public List<DepartmentDto> getRoots() {
        return departmentRepository.findByParentId(null).stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public boolean exists(Long id) {
        if (id == null) {
            return false;
        }
        return departmentRepository.existsById(id);
    }

    private DepartmentDto mapToDto(Department department) {
        return DepartmentDto.builder()
                .id(department.getId())
                .name(department.getName())
                .parentId(department.getParentId())
                .build();
    }

    private Map<Long, Department> getAllDepartmentsById() {
        return departmentRepository.findAll().stream()
                .collect(Collectors.toMap(Department::getId, Function.identity()));
    }
}
