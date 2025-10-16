package com.spmorangle.crm.reporting.repository;

import com.spmorangle.crm.reporting.model.TaskTimeTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskTimeTrackingRepository extends JpaRepository<TaskTimeTracking, Long> {
    
    Optional<TaskTimeTracking> findByTaskIdAndUserId(Long taskId, Long userId);
    
    List<TaskTimeTracking> findByTaskId(Long taskId);
    
    List<TaskTimeTracking> findByUserId(Long userId);
    
    @Query("""
        SELECT SUM(ttt.totalHours) 
        FROM TaskTimeTracking ttt 
        JOIN Task t ON ttt.taskId = t.id 
        JOIN User u ON ttt.userId = u.id 
        WHERE (:department = '' OR u.department = :department)
        AND (FUNCTION('DATE', ttt.startedAt) >= :startDate)
        AND (FUNCTION('DATE', ttt.completedAt) <= :endDate OR (ttt.completedAt IS NULL AND FUNCTION('DATE', ttt.startedAt) <= :endDate))
        """)
    BigDecimal getTotalHoursByDepartmentAndDateRange(
        @Param("department") String department,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT u.department, SUM(ttt.totalHours) 
        FROM TaskTimeTracking ttt 
        JOIN Task t ON ttt.taskId = t.id 
        JOIN User u ON ttt.userId = u.id 
        WHERE (FUNCTION('DATE', ttt.startedAt) >= :startDate)
        AND (FUNCTION('DATE', ttt.completedAt) <= :endDate OR (ttt.completedAt IS NULL AND FUNCTION('DATE', ttt.startedAt) <= :endDate))
        AND u.department IS NOT NULL
        GROUP BY u.department
        """)
    List<Object[]> getHoursByDepartment(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT p.name, SUM(ttt.totalHours) 
        FROM TaskTimeTracking ttt 
        JOIN Task t ON ttt.taskId = t.id 
        JOIN Project p ON t.projectId = p.id 
        JOIN User u ON ttt.userId = u.id 
        WHERE (:department = '' OR u.department = :department)
        AND (:projectIds IS NULL OR t.projectId IN :projectIds)
        AND (FUNCTION('DATE', ttt.startedAt) >= :startDate)
        AND (FUNCTION('DATE', ttt.completedAt) <= :endDate OR (ttt.completedAt IS NULL AND FUNCTION('DATE', ttt.startedAt) <= :endDate))
        AND t.projectId IS NOT NULL
        GROUP BY p.name
        """)
    List<Object[]> getHoursByProject(
        @Param("department") String department,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT p.name, u.department, SUM(ttt.totalHours),
               COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END),
               COUNT(DISTINCT CASE WHEN t.status = 'IN_PROGRESS' THEN t.id END)
        FROM TaskTimeTracking ttt 
        JOIN Task t ON ttt.taskId = t.id 
        JOIN Project p ON t.projectId = p.id 
        JOIN User u ON t.ownerId = u.id 
        WHERE (:department = '' OR u.department = :department)
        AND (:projectIds IS NULL OR t.projectId IN :projectIds)
        AND (FUNCTION('DATE', ttt.startedAt) >= :startDate)
        AND (FUNCTION('DATE', ttt.completedAt) <= :endDate OR (ttt.completedAt IS NULL AND FUNCTION('DATE', ttt.startedAt) <= :endDate))
        AND t.projectId IS NOT NULL
        GROUP BY p.name, u.department
        """)
    List<Object[]> getProjectDetails(
        @Param("department") String department,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

