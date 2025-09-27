package com.spmorangle.crm.projectmanagement.repository;

import java.util.List;

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
}
