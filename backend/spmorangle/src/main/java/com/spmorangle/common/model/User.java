package com.spmorangle.common.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users", schema = "syncup")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "full_name", nullable = false, length = Integer.MAX_VALUE)
    private String fullName;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @NotNull
    @Column(name = "role_type", nullable = false)
    private String roleType;

    @NotNull
    @Column(name = "cognito_sub", nullable = false)
    private UUID cognitoSub;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

}