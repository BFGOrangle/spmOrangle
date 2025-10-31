package com.spmorangle.crm.taskmanagement.model;

import java.time.OffsetDateTime;
import java.util.Set;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tasks", schema = "syncup")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @Column(name = "project_id", nullable = true)
    private Long projectId;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = true)
    private String description;

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
    private long createdBy;

    @Column(name = "updated_by", nullable = true)
    private Long updatedBy;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "task_tag",
        schema = "syncup",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags;

    @Column(name = "due_datetime", nullable = true)
    private OffsetDateTime dueDateTime;

    @Column(name="is_recurring")
    private Boolean isRecurring;

    @Column(name = "recurrence_str")
    private String recurrenceRuleStr;

    @Column(name = "start_date")
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "has_sent_overdue", nullable = false)
    @ColumnDefault("false")
    private Boolean hasSentOverdue = false;

    @Column(name = "has_sent_predue", nullable = false)
    @ColumnDefault("false")
    private Boolean hasSentPreDue = false;

    @Column(name = "is_rescheduled", nullable = false)
    @ColumnDefault("false")
    private Boolean isRescheduled = false;
}
