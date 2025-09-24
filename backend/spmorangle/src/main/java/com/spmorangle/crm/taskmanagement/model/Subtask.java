package com.spmorangle.crm.taskmanagement.model;

import java.time.OffsetDateTime;

import org.hibernate.annotations.ColumnDefault;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "subtasks", schema = "syncup")
public class Subtask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @NotNull
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "details", nullable = true)
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.TODO;

    @Column(name = "delete_ind", nullable = false)
    private boolean deleteInd = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = true)
    @ColumnDefault("now()")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_by", nullable = true)
    private Long updatedBy;
}