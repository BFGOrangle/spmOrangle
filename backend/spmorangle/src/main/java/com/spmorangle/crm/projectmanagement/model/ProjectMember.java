package com.spmorangle.crm.projectmanagement.model;

import java.time.OffsetDateTime;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@IdClass(ProjectMemberCK.class)
@Table(name = "project_members", schema = "syncup")
public class ProjectMember {
    @Id
    @Column(name = "project_id", nullable = false)
    private long projectId;

    @Id
    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name = "added_by", nullable = false)
    private long addedBy;

    @Column(name = "added_at", nullable = false)
    @ColumnDefault("now()")
    private OffsetDateTime addedAt;

    @Column(name = "is_owner", nullable = false)
    @ColumnDefault("false")
    private boolean isOwner;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = OffsetDateTime.now();
        }
    }

    // Manual setter to work around Lombok annotation processing timing
    public void setOwner(boolean isOwner) {
        this.isOwner = isOwner;
    }
}
