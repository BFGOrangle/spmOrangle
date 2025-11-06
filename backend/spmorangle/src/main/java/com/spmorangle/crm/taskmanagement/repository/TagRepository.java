package com.spmorangle.crm.taskmanagement.repository;

import com.spmorangle.crm.taskmanagement.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findByTagName(String tagName);

    /**
     * Find all non-deleted tags
     */
    @Query("SELECT t FROM Tag t WHERE t.deleteInd = false")
    List<Tag> findAllActive();

    /**
     * Find tag by name (including soft-deleted tags)
     */
    @Query("SELECT t FROM Tag t WHERE t.tagName = :tagName")
    Optional<Tag> findByTagNameIncludingDeleted(@Param("tagName") String tagName);

    /**
     * Find active (non-deleted) tag by name
     */
    @Query("SELECT t FROM Tag t WHERE t.tagName = :tagName AND t.deleteInd = false")
    Optional<Tag> findActiveByTagName(@Param("tagName") String tagName);
}
