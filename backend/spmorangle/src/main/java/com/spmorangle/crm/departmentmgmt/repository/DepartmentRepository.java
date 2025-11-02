package com.spmorangle.crm.departmentmgmt.repository;

import com.spmorangle.crm.departmentmgmt.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Modifying
    @Query(value = "UPDATE syncup.departments SET name = ?2 WHERE id = ?1", nativeQuery = true)
    void updateDepartmentNameById(Long id, String newName);
}
