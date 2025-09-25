package com.spmorangle.crm.projectmanagement.repository;

import java.util.List;

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
}
