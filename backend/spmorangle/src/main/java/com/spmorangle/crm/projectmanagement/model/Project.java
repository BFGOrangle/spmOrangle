package com.spmorangle.crm.projectmanagement.model;

import java.time.OffsetDateTime;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "projects", schema = "syncup")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "description")
    private String description;

    @Column(name = "delete_ind", nullable = false)
    private boolean deleteInd = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    @ColumnDefault("now()")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @ColumnDefault("now()")
    private OffsetDateTime updatedAt;

    @NotNull
    @Column(name = "created_by", nullable = false, updatable = false)
    private long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
