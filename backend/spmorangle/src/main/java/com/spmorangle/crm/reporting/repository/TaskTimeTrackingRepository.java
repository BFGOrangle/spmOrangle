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
        JOIN User u ON ttt.userId = u.id
        WHERE (:departmentId IS NULL OR u.departmentId = :departmentId)
        AND (CAST(ttt.startedAt AS DATE) >= :startDate)
        AND (CAST(ttt.completedAt AS DATE) <= :endDate OR (ttt.completedAt IS NULL AND CAST(ttt.startedAt AS DATE) <= :endDate))
        """)
    BigDecimal getTotalHoursByDepartmentAndDateRange(
        @Param("departmentId") Long departmentId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT u.departmentId, SUM(
            CASE
                WHEN ttt.completedAt IS NOT NULL THEN ttt.totalHours
                WHEN ttt.startedAt IS NOT NULL THEN
                    (CAST(FUNCTION('timestampdiff', SECOND, ttt.startedAt, CURRENT_TIMESTAMP) AS double) / 3600.0)
                ELSE 0
            END
        )
        FROM TaskTimeTracking ttt
        JOIN Task t ON ttt.taskId = t.id
        JOIN User u ON ttt.userId = u.id
        WHERE (CAST(ttt.startedAt AS DATE) >= :startDate)
        AND (CAST(ttt.completedAt AS DATE) <= :endDate OR (ttt.completedAt IS NULL AND CAST(ttt.startedAt AS DATE) <= :endDate))
        AND u.departmentId IS NOT NULL
        AND (:departmentId IS NULL OR u.departmentId = :departmentId)
        AND (:projectIds IS NULL OR t.projectId IN :projectIds)
        GROUP BY u.departmentId
        """)
    List<Object[]> getHoursByDepartment(
        @Param("departmentId") Long departmentId,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT p.name, SUM(
            CASE
                WHEN ttt.completedAt IS NOT NULL THEN ttt.totalHours
                WHEN ttt.startedAt IS NOT NULL THEN
                    (CAST(FUNCTION('timestampdiff', SECOND, ttt.startedAt, CURRENT_TIMESTAMP) AS double) / 3600.0)
                ELSE 0
            END
        )
        FROM TaskTimeTracking ttt
        JOIN Task t ON ttt.taskId = t.id
        JOIN Project p ON t.projectId = p.id
        JOIN User u ON ttt.userId = u.id
        WHERE (:departmentId IS NULL OR u.departmentId = :departmentId)
        AND (:projectIds IS NULL OR t.projectId IN :projectIds)
        AND (CAST(ttt.startedAt AS DATE) >= :startDate)
        AND (CAST(ttt.completedAt AS DATE) <= :endDate OR (ttt.completedAt IS NULL AND CAST(ttt.startedAt AS DATE) <= :endDate))
        AND t.projectId IS NOT NULL
        GROUP BY p.name
        """)
    List<Object[]> getHoursByProject(
        @Param("departmentId") Long departmentId,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
    
    @Query("""
        SELECT p.name, u.departmentId, SUM(
            CASE
                WHEN ttt.completedAt IS NOT NULL THEN ttt.totalHours
                WHEN ttt.startedAt IS NOT NULL THEN
                    (CAST(FUNCTION('timestampdiff', SECOND, ttt.startedAt, CURRENT_TIMESTAMP) AS double) / 3600.0)
                ELSE 0
            END
        ),
        COUNT(DISTINCT CASE WHEN t.status = 'COMPLETED' THEN t.id END),
        COUNT(DISTINCT CASE WHEN t.status = 'IN_PROGRESS' THEN t.id END)
        FROM TaskTimeTracking ttt
        JOIN Task t ON ttt.taskId = t.id
        JOIN Project p ON t.projectId = p.id
        JOIN User u ON t.ownerId = u.id
        WHERE (:departmentId IS NULL OR u.departmentId = :departmentId)
        AND (:projectIds IS NULL OR t.projectId IN :projectIds)
        AND (CAST(ttt.startedAt AS DATE) >= :startDate)
        AND (CAST(ttt.completedAt AS DATE) <= :endDate OR (ttt.completedAt IS NULL AND CAST(ttt.startedAt AS DATE) <= :endDate))
        AND t.projectId IS NOT NULL
        GROUP BY p.name, u.departmentId
        """)
    List<Object[]> getProjectDetails(
        @Param("departmentId") Long departmentId,
        @Param("projectIds") List<Long> projectIds,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}

