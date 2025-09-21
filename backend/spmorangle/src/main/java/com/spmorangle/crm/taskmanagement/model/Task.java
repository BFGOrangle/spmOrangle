package com.spmorangle.crm.taskmanagement.model;

import com.spmorangle.crm.taskmanagement.enums.Status;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "tasks", schema = "syncup")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private int id;

    @NotNull
    @Column(name = "project_id", nullable = false)
    private int projectId;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private int ownerId;

    @Column(name = "task_type", nullable = false)
    private int taskType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = true)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.TODO;

    @Column(name = "delete_index", nullable = false)
    private boolean deleteIndex = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @ColumnDefault("now()")
    private OffsetDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private int createdBy;

    @Column(name = "updated_by", nullable = false)
    private int updatedBy;
}
