package com.spmorangle.crm.projectmanagement.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spmorangle.crm.projectmanagement.model.ProjectMember;
import com.spmorangle.crm.projectmanagement.model.ProjectMemberCK;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberCK> {

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId")
    List<ProjectMember> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.userId = :userId")
    List<ProjectMember> findByUserId(@Param("userId") Long userId);

    // New methods for owner management
    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId = :projectId AND pm.isOwner = true")
    List<ProjectMember> findOwnersByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT pm FROM ProjectMember pm WHERE pm.projectId IN :projectIds AND pm.isOwner = true")
    List<ProjectMember> findOwnersByProjectIds(@Param("projectIds") Set<Long> projectIds);

    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END FROM ProjectMember pm " +
           "WHERE pm.projectId = :projectId AND pm.userId = :userId AND pm.isOwner = true")
    boolean existsByProjectIdAndUserIdAndIsOwner(@Param("projectId") Long projectId,
                                                   @Param("userId") Long userId,
                                                   @Param("isOwner") boolean isOwner);

    @Query("SELECT CASE WHEN COUNT(pm) > 0 THEN true ELSE false END FROM ProjectMember pm " +
           "WHERE pm.projectId = :projectId AND pm.userId = :userId")
    boolean existsByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
