package com.spmorangle.crm.projectmanagement.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spmorangle.crm.projectmanagement.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    @Query("SELECT p FROM Project p WHERE p.deleteInd = false AND p.ownerId = :userId")
    List<Project> findByOwnerIdAndNotDeleted(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Project p " +
           "JOIN ProjectMember pm ON p.id = pm.projectId " +
           "WHERE p.deleteInd = false AND pm.userId = :userId")
    List<Project> findProjectsByMemberId(@Param("userId") Long userId);
    
    @Query("SELECT p FROM Project p " +
           "LEFT JOIN ProjectMember pm ON p.id = pm.projectId " +
           "WHERE p.deleteInd = false AND (p.ownerId = :userId OR pm.userId = :userId)")
    List<Project> findUserProjects(@Param("userId") Long userId);

    @Query("SELECT p FROM Project p WHERE p.id IN :projectIds")
    List<Project> findByIdIn(@Param("projectIds") Set<Long> projectIds);

    /**
     * Find projects where staff from a specific department are members,
     * but the given user (manager) is NOT a member.
     * This is used for managers to see "related" cross-department projects.
     *
     * @param managerId The manager's user ID (to exclude projects they're already a member of)
     * @param department The department to search for
     * @return List of projects with department staff as members (excluding projects where manager is already a member)
     */
    @Query("SELECT DISTINCT p FROM Project p " +
           "JOIN ProjectMember pm ON p.id = pm.projectId " +
           "JOIN User u ON pm.userId = u.id " +
           "WHERE p.deleteInd = false " +
           "AND u.departmentId = :departmentId " +
           "AND p.id NOT IN (" +
           "  SELECT p2.id FROM Project p2 " +
           "  LEFT JOIN ProjectMember pm2 ON p2.id = pm2.projectId " +
           "  WHERE p2.deleteInd = false AND (p2.ownerId = :managerId OR pm2.userId = :managerId)" +
           ")")
    List<Project> findProjectsWithDepartmentStaff(@Param("managerId") Long managerId,
                                                   @Param("departmentId") Long departmentId);
}
