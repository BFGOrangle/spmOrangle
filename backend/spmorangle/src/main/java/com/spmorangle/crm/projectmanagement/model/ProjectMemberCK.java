package com.spmorangle.crm.projectmanagement.model;

import java.io.Serializable;
import java.util.Objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberCK implements Serializable {
    private long projectId;
    private long userId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectMemberCK that = (ProjectMemberCK) o;
        return projectId == that.projectId && userId == that.userId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, userId);
    }
}
