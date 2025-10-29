package com.spmorangle.crm.reporting.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spmorangle.crm.taskmanagement.model.Task;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportingRepository extends JpaRepository<Task, Long> {
    
    @Query("""
        SELECT t.status, COUNT(t) 
        FROM Task t 
        JOIN User u ON t.ownerId = u.id 
        WHERE (:department = '' OR u.department = :department)
        AND (CAST(t.createdAt AS DATE) >= :startDate)
        AND (CAST(t.createdAt AS DATE) <= :endDate)
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
        WHERE (CAST(t.createdAt AS DATE) >= :startDate)
        AND (CAST(t.createdAt AS DATE) <= :endDate)
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
        AND (CAST(t.createdAt AS DATE) >= :startDate)
        AND (CAST(t.createdAt AS DATE) <= :endDate)
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
    
    /**
     * Get task counts by project and status for all projects (when no project filter is specified)
     * Similar to getTaskCountsByDepartmentAndStatus but for projects
     */
    @Query("""
        SELECT p.name, t.status, COUNT(t) 
        FROM Task t 
        JOIN Project p ON t.projectId = p.id 
        JOIN User u ON t.ownerId = u.id 
        WHERE (:department = '' OR u.department = :department)
        AND (CAST(t.createdAt AS DATE) >= :startDate)
        AND (CAST(t.createdAt AS DATE) <= :endDate)
        AND t.deleteInd = false
        AND t.projectId IS NOT NULL
        GROUP BY p.name, t.status
        """)
    List<Object[]> getAllTaskCountsByProjectAndStatus(
        @Param("department") String department,
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
    
    /**
     * Get all users for staff breakdown by department and optional project filters
     * Returns all users in the department, optionally filtered by project membership
     * Note: Pass null for projectIds to get all users (no project filter)
     */
    @Query("""
        SELECT DISTINCT u.id, u.userName, u.department
        FROM User u
        WHERE (:department = '' OR u.department = :department)
        AND (
            :projectIds IS NULL 
            OR u.id IN (
                SELECT pm.userId FROM ProjectMember pm
                WHERE pm.projectId IN :projectIds
            )
        )
        ORDER BY u.userName
        """)
    List<Object[]> getUsersForStaffBreakdown(
        @Param("department") String department,
        @Param("projectIds") List<Long> projectIds
    );
    
    /**
     * Get task counts by status for a specific user
     * Includes tasks where user is owner OR assignee (without double counting)
     */
    @Query("""
        SELECT t.status, COUNT(DISTINCT t.id)
        FROM Task t
        WHERE t.deleteInd = false
        AND (CAST(t.createdAt AS DATE) >= :startDate)
        AND (CAST(t.createdAt AS DATE) <= :endDate)
        AND (:projectIds IS NULL OR t.projectId IN :projectIds)
        AND (t.ownerId = :userId OR EXISTS (
            SELECT 1 FROM TaskAssignee ta WHERE ta.taskId = t.id AND ta.userId = :userId
        ))
        GROUP BY t.status
        """)
    List<Object[]> getTaskCountsByStatusForUser(
        @Param("userId") Long userId,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    /**
     * Get total logged hours for a specific user
     * Filters by project if projectIds are provided
     */
    @Query("""
        SELECT COALESCE(SUM(
        CASE 
            WHEN ttt.completedAt IS NOT NULL THEN ttt.totalHours
            WHEN ttt.startedAt IS NOT NULL THEN 
                (CAST(FUNCTION('timestampdiff', SECOND, ttt.startedAt, CURRENT_TIMESTAMP) AS double) / 3600.0)
            ELSE 0
        END
        ), 0)
        FROM TaskTimeTracking ttt
        JOIN Task t ON ttt.taskId = t.id
        WHERE ttt.userId = :userId
        AND (CAST(ttt.startedAt AS DATE) >= :startDate)
        AND (CAST(ttt.startedAt AS DATE) <= :endDate)
        AND (:projectIds IS NULL OR t.projectId IN :projectIds)
        """)
    BigDecimal getLoggedHoursForUser(
        @Param("userId") Long userId,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

