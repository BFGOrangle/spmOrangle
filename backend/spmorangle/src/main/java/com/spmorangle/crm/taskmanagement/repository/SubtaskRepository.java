package com.spmorangle.crm.taskmanagement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.spmorangle.crm.taskmanagement.model.Subtask;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    
    @Query("SELECT s FROM Subtask s WHERE s.deleteInd = false AND s.taskId = :taskId")
    List<Subtask> findByTaskIdAndNotDeleted(@Param("taskId") Long taskId);
    
    @Query("SELECT s FROM Subtask s WHERE s.deleteInd = false AND s.projectId = :projectId")
    List<Subtask> findByProjectIdAndNotDeleted(@Param("projectId") Long projectId);
    
    @Query("SELECT s FROM Subtask s WHERE s.deleteInd = false AND s.id = :id")
    Subtask findByIdAndNotDeleted(@Param("id") Long id);
}
