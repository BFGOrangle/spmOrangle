package com.spmorangle.crm.departmentmgmt.repository;

import com.spmorangle.crm.departmentmgmt.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Modifying
    @Query(value = "UPDATE syncup.departments SET name = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDepartmentNameById(Long id, String newName);

    Optional<Department> findByNameIgnoreCase(String name);

    List<Department> findByParentId(Long parentId);

    @Query("SELECT d FROM Department d WHERE LOWER(d.name) IN :names")
    List<Department> findByNamesIgnoreCase(@Param("names") List<String> names);
}
