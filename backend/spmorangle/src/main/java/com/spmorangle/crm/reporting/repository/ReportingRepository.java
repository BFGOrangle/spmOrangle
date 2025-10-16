package com.spmorangle.crm.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spmorangle.crm.taskmanagement.model.Task;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportingRepository extends JpaRepository<Task, Long> {
    
    @Query("""
        SELECT t.status, COUNT(t) 
        FROM Task t 
        JOIN User u ON t.ownerId = u.id 
        WHERE (:department = '' OR u.department = :department)
        AND (FUNCTION('DATE', t.createdAt) >= :startDate)
        AND (FUNCTION('DATE', t.createdAt) <= :endDate)
        AND t.deleteInd = false
        GROUP BY t.status
        """)
    List<Object[]> getTaskCountsByStatus(
        @Param("department") String department,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT u.department, t.status, COUNT(t) 
        FROM Task t 
        JOIN User u ON t.ownerId = u.id 
        WHERE (FUNCTION('DATE', t.createdAt) >= :startDate)
        AND (FUNCTION('DATE', t.createdAt) <= :endDate)
        AND t.deleteInd = false
        AND u.department IS NOT NULL
        GROUP BY u.department, t.status
        """)
    List<Object[]> getTaskCountsByDepartmentAndStatus(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT p.name, t.status, COUNT(t) 
        FROM Task t 
        JOIN Project p ON t.projectId = p.id 
        JOIN User u ON t.ownerId = u.id 
        WHERE (:department = '' OR u.department = :department)
        AND t.projectId IN :projectIds
        AND (FUNCTION('DATE', t.createdAt) >= :startDate)
        AND (FUNCTION('DATE', t.createdAt) <= :endDate)
        AND t.deleteInd = false
        AND t.projectId IS NOT NULL
        GROUP BY p.name, t.status
        """)
    List<Object[]> getTaskCountsByProjectAndStatus(
        @Param("department") String department,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT DISTINCT u.department 
        FROM User u 
        WHERE u.department IS NOT NULL 
        ORDER BY u.department
        """)
    List<String> getAllDepartments();
    
    @Query("""
        SELECT DISTINCT p.id, p.name 
        FROM Project p 
        JOIN User u ON p.ownerId = u.id 
        WHERE (:department = '' OR u.department = :department)
        AND p.deleteInd = false
        ORDER BY p.name
        """)
    List<Object[]> getProjectsByDepartment(@Param("department") String department);
}

