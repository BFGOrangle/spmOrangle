package com.spmorangle.crm.taskmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@IdClass(TaskAssigneeCK.class)
@Table(name = "task_assignees", schema = "syncup")
public class TaskAssignee {
    @Id
    @Column(name = "task_id", nullable = false)
    private long taskId;

    @Id
    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "assigned_at", nullable = false)
    @ColumnDefault("now()")
    private OffsetDateTime assignedAt;

    @Id
    @Column(name = "assigned_id", nullable = false)
    private long assignedId;

    @PrePersist
    protected void onCreate() {
        if (assignedAt == null) {
            assignedAt = OffsetDateTime.now();
        }
    }
}
