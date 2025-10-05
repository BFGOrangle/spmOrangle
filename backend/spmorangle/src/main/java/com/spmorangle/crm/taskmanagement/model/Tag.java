package com.spmorangle.crm.taskmanagement.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "tag", schema = "public")
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "tag_name", nullable = false, unique = true)
    private String tagName;

    @ManyToMany(mappedBy = "tags")
    private Set<Task> tasks = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(tagName, tag.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagName);
    }
}
