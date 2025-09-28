package com.spmorangle.crm.taskmanagement.repository;

import com.spmorangle.crm.taskmanagement.enums.Status;
import com.spmorangle.crm.taskmanagement.enums.TaskType;
import com.spmorangle.crm.taskmanagement.model.Task;
import com.spmorangle.crm.taskmanagement.model.TaskComment;
import com.spmorangle.crm.taskmanagement.model.Subtask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TaskCommentRepository Tests")
class TaskCommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskCommentRepository taskCommentRepository;

    private TaskComment comment1;
    private TaskComment comment2;
    private TaskComment comment3;
    private TaskComment comment4;
    private TaskComment deletedComment;
    private TaskComment replyComment;

    private Long task1Id;
    private Long task2Id;
    private Long subtask1Id;

    @BeforeEach
    void setUp() {
        OffsetDateTime now = OffsetDateTime.now();

        // Create required Task entities for foreign key constraints
        Task task1 = new Task();
        task1.setProjectId(100L);
        task1.setOwnerId(1L);
        task1.setTaskType(TaskType.FEATURE);
        task1.setTitle("Test Task 1");
        task1.setStatus(Status.TODO);
        task1.setCreatedBy(1L);
        task1.setCreatedAt(now);
        task1.setUpdatedBy(1L);
        task1.setUpdatedAt(now);
        entityManager.persistAndFlush(task1);
        task1Id = task1.getId();

        Task task2 = new Task();
        task2.setProjectId(200L);
        task2.setOwnerId(2L);
        task2.setTaskType(TaskType.BUG);
        task2.setTitle("Test Task 2");
        task2.setStatus(Status.IN_PROGRESS);
        task2.setCreatedBy(2L);
        task2.setCreatedAt(now);
        task2.setUpdatedBy(2L);
        task2.setUpdatedAt(now);
        entityManager.persistAndFlush(task2);
        task2Id = task2.getId();

        // Create required Subtask entities for foreign key constraints
        Subtask subtask1 = new Subtask();
        subtask1.setTaskId(task1Id);
        subtask1.setProjectId(100L);
        subtask1.setTitle("Test Subtask 1");
        subtask1.setTaskType(TaskType.FEATURE);
        subtask1.setStatus(Status.TODO);
        subtask1.setCreatedBy(1L);
        subtask1.setCreatedAt(now);
        subtask1.setUpdatedBy(1L);
        subtask1.setUpdatedAt(now);
        entityManager.persistAndFlush(subtask1);
        subtask1Id = subtask1.getId();

        // Task comment with mentions
        comment1 = new TaskComment();
        comment1.setTaskId(task1Id);
        comment1.setProjectId(100L);
        comment1.setContent("Comment with mentions @user2 @user3");
        comment1.setMentionedUserIds(Arrays.asList(2L, 3L));
        comment1.setCreatedBy(1L);
        comment1.setCreatedAt(now.minusHours(3));
        comment1.setDeleted(false);
        comment1.setResolved(false);

        // Subtask comment with single mention
        comment2 = new TaskComment();
        comment2.setSubtaskId(subtask1Id);
        comment2.setProjectId(100L);
        comment2.setContent("Subtask comment @user2");
        comment2.setMentionedUserIds(Arrays.asList(2L));
        comment2.setCreatedBy(4L);
        comment2.setCreatedAt(now.minusHours(2));
        comment2.setDeleted(false);
        comment2.setResolved(true);

        // Comment without mentions
        comment3 = new TaskComment();
        comment3.setTaskId(task2Id);
        comment3.setProjectId(200L);
        comment3.setContent("Regular comment");
        comment3.setMentionedUserIds(Collections.emptyList());
        comment3.setCreatedBy(2L);
        comment3.setCreatedAt(now.minusHours(1));
        comment3.setDeleted(false);
        comment3.setResolved(false);

        // Comment with multiple mentions including user 2
        comment4 = new TaskComment();
        comment4.setTaskId(task1Id);
        comment4.setProjectId(100L);
        comment4.setContent("Another comment @user2 @user5");
        comment4.setMentionedUserIds(Arrays.asList(2L, 5L));
        comment4.setCreatedBy(3L);
        comment4.setCreatedAt(now.minusMinutes(30));
        comment4.setDeleted(false);
        comment4.setResolved(false);

        // Deleted comment with mentions
        deletedComment = new TaskComment();
        deletedComment.setTaskId(task1Id);
        deletedComment.setProjectId(100L);
        deletedComment.setContent("Deleted comment @user2");
        deletedComment.setMentionedUserIds(Arrays.asList(2L));
        deletedComment.setCreatedBy(1L);
        deletedComment.setUpdatedBy(1L);
        deletedComment.setCreatedAt(now.minusHours(4));
        deletedComment.setDeleted(true);
        deletedComment.setResolved(false);

        entityManager.persistAndFlush(comment1);

        // Reply comment with mentions - must create after comment1 is persisted to get its ID
        replyComment = new TaskComment();
        replyComment.setTaskId(task1Id);
        replyComment.setProjectId(100L);
        replyComment.setContent("Reply comment @user2 @user3");
        replyComment.setMentionedUserIds(Arrays.asList(2L, 3L));
        replyComment.setCreatedBy(2L);
        replyComment.setUpdatedBy(2L);
        replyComment.setCreatedAt(now);
        replyComment.setDeleted(false);
        replyComment.setResolved(false);
        replyComment.setParentCommentId(comment1.getId());

        entityManager.persistAndFlush(comment2);
        entityManager.persistAndFlush(comment3);
        entityManager.persistAndFlush(comment4);
        entityManager.persistAndFlush(deletedComment);
        entityManager.persistAndFlush(replyComment);
    }

    @Nested
    @DisplayName("findByMentionedUserId Tests")
    class FindByMentionedUserIdTests {

        @Test
        @DisplayName("Should find comments where user is mentioned")
        void findByMentionedUserId_UserIsMentioned_ReturnsComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findByMentionedUserId(2L);

            // Then
            assertThat(result).hasSize(4);
            assertThat(result).extracting(TaskComment::getId)
                    .containsExactlyInAnyOrder(comment1.getId(), comment2.getId(), comment4.getId(), replyComment.getId());

            // Verify ordering (DESC by createdAt)
            assertThat(result.get(0).getCreatedAt()).isAfter(result.get(1).getCreatedAt());
            assertThat(result.get(1).getCreatedAt()).isAfter(result.get(2).getCreatedAt());
        }

        @Test
        @DisplayName("Should find comments where user 3 is mentioned")
        void findByMentionedUserId_User3IsMentioned_ReturnsComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findByMentionedUserId(3L);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(TaskComment::getId)
                    .containsExactlyInAnyOrder(comment1.getId(), replyComment.getId());
        }

        @Test
        @DisplayName("Should return empty list when user is not mentioned anywhere")
        void findByMentionedUserId_UserNotMentioned_ReturnsEmptyList() {
            // When
            List<TaskComment> result = taskCommentRepository.findByMentionedUserId(999L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should not include deleted comments")
        void findByMentionedUserId_DeletedCommentsExcluded_ReturnsOnlyNonDeletedComments() {
            // deletedComment mentions user 2 but should not be returned
            // When
            List<TaskComment> result = taskCommentRepository.findByMentionedUserId(2L);

            // Then
            assertThat(result).hasSize(4); // Should not include deletedComment
            assertThat(result).extracting(TaskComment::getId)
                    .doesNotContain(deletedComment.getId());
        }

        @Test
        @DisplayName("Should handle user mentioned in multiple comments correctly")
        void findByMentionedUserId_UserMentionedMultipleTimes_ReturnsAllComments() {
            // User 2 is mentioned in comment1, comment2, comment4, and replyComment
            // When
            List<TaskComment> result = taskCommentRepository.findByMentionedUserId(2L);

            // Then
            assertThat(result).hasSize(4);
            assertThat(result).extracting(TaskComment::getContent)
                    .containsExactlyInAnyOrder(
                            comment1.getContent(),
                            comment2.getContent(),
                            comment4.getContent(),
                            replyComment.getContent()
                    );
        }

        @Test
        @DisplayName("Should handle zero user ID correctly")
        void findByMentionedUserId_ZeroUserId_ReturnsEmptyList() {
            // When
            List<TaskComment> result = taskCommentRepository.findByMentionedUserId(0L);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle negative user ID correctly")
        void findByMentionedUserId_NegativeUserId_ReturnsEmptyList() {
            // When
            List<TaskComment> result = taskCommentRepository.findByMentionedUserId(-1L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByTaskIdAndNotDeleted Tests")
    class FindByTaskIdAndNotDeletedTests {

        @Test
        @DisplayName("Should find non-deleted comments for task")
        void findByTaskIdAndNotDeleted_ValidTaskId_ReturnsNonDeletedComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findByTaskIdAndNotDeleted(task1Id);

            // Then
            assertThat(result).hasSize(3); // comment1, comment4, replyComment
            assertThat(result).extracting(TaskComment::getId)
                    .containsExactlyInAnyOrder(comment1.getId(), comment4.getId(), replyComment.getId());
            assertThat(result).allMatch(comment -> !comment.isDeleted());
        }

        @Test
        @DisplayName("Should return empty list for non-existent task")
        void findByTaskIdAndNotDeleted_NonExistentTaskId_ReturnsEmptyList() {
            // When
            List<TaskComment> result = taskCommentRepository.findByTaskIdAndNotDeleted(999L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySubtaskIdAndNotDeleted Tests")
    class FindBySubtaskIdAndNotDeletedTests {

        @Test
        @DisplayName("Should find non-deleted comments for subtask")
        void findBySubtaskIdAndNotDeleted_ValidSubtaskId_ReturnsNonDeletedComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findBySubtaskIdAndNotDeleted(subtask1Id);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(comment2.getId());
            assertThat(result.get(0).isDeleted()).isFalse();
        }

        @Test
        @DisplayName("Should return empty list for non-existent subtask")
        void findBySubtaskIdAndNotDeleted_NonExistentSubtaskId_ReturnsEmptyList() {
            // When
            List<TaskComment> result = taskCommentRepository.findBySubtaskIdAndNotDeleted(999L);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findTopLevelCommentsByTaskId Tests")
    class FindTopLevelCommentsByTaskIdTests {

        @Test
        @DisplayName("Should find only top-level comments for task")
        void findTopLevelCommentsByTaskId_ValidTaskId_ReturnsTopLevelComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findTopLevelCommentsByTaskId(task1Id);

            // Then
            assertThat(result).hasSize(2); // comment1 and comment4 (replyComment has parentCommentId)
            assertThat(result).extracting(TaskComment::getId)
                    .containsExactlyInAnyOrder(comment1.getId(), comment4.getId());
            assertThat(result).allMatch(comment -> comment.getParentCommentId() == null);
        }
    }

    @Nested
    @DisplayName("findRepliesByParentCommentId Tests")
    class FindRepliesByParentCommentIdTests {

        @Test
        @DisplayName("Should find replies to parent comment")
        void findRepliesByParentCommentId_ValidParentId_ReturnsReplies() {
            // When
            List<TaskComment> result = taskCommentRepository.findRepliesByParentCommentId(comment1.getId());

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(replyComment.getId());
            assertThat(result.get(0).getParentCommentId()).isEqualTo(comment1.getId());
        }

        @Test
        @DisplayName("Should return empty list for comment with no replies")
        void findRepliesByParentCommentId_NoReplies_ReturnsEmptyList() {
            // When
            List<TaskComment> result = taskCommentRepository.findRepliesByParentCommentId(comment2.getId());

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("Should filter task comments by author")
        void findTaskCommentsWithFilters_FilterByAuthor_ReturnsFilteredComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findTaskCommentsWithFilters(task1Id, 1L, null);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(comment1.getId());
            assertThat(result.get(0).getCreatedBy()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should filter task comments by resolution status")
        void findTaskCommentsWithFilters_FilterByResolved_ReturnsFilteredComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findTaskCommentsWithFilters(task1Id, null, false);

            // Then
            assertThat(result).hasSize(3); // All comments for task 1 are unresolved
            assertThat(result).allMatch(comment -> !comment.isResolved());
        }

        @Test
        @DisplayName("Should filter subtask comments by resolution status")
        void findSubtaskCommentsWithFilters_FilterByResolved_ReturnsFilteredComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findSubtaskCommentsWithFilters(subtask1Id, null, true);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(comment2.getId());
            assertThat(result.get(0).isResolved()).isTrue();
        }
    }

    @Nested
    @DisplayName("Count and Aggregation Tests")
    class CountAndAggregationTests {

        @Test
        @DisplayName("Should count replies correctly")
        void countRepliesByParentCommentId_ValidParentId_ReturnsCorrectCount() {
            // When
            long count = taskCommentRepository.countRepliesByParentCommentId(comment1.getId());

            // Then
            assertThat(count).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should return zero for comment with no replies")
        void countRepliesByParentCommentId_NoReplies_ReturnsZero() {
            // When
            long count = taskCommentRepository.countRepliesByParentCommentId(comment2.getId());

            // Then
            assertThat(count).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should find distinct comment authors by task")
        void findCommentAuthorsByTaskId_ValidTaskId_ReturnsDistinctAuthors() {
            // When
            List<Long> authors = taskCommentRepository.findCommentAuthorsByTaskId(task1Id);

            // Then
            assertThat(authors).hasSize(3);
            assertThat(authors).containsExactlyInAnyOrder(1L, 2L, 3L);
        }

        @Test
        @DisplayName("Should find distinct comment authors by subtask")
        void findCommentAuthorsBySubtaskId_ValidSubtaskId_ReturnsDistinctAuthors() {
            // When
            List<Long> authors = taskCommentRepository.findCommentAuthorsBySubtaskId(subtask1Id);

            // Then
            assertThat(authors).hasSize(1);
            assertThat(authors).contains(4L);
        }
    }

    @Nested
    @DisplayName("Project and User Filtering Tests")
    class ProjectAndUserFilteringTests {

        @Test
        @DisplayName("Should find comments by project ID")
        void findByProjectIdAndNotDeleted_ValidProjectId_ReturnsProjectComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findByProjectIdAndNotDeleted(100L);

            // Then
            assertThat(result).hasSize(4); // comment1, comment2, comment4, replyComment
            assertThat(result).allMatch(comment -> comment.getProjectId().equals(100L));
            assertThat(result).allMatch(comment -> !comment.isDeleted());
        }

        @Test
        @DisplayName("Should find comments by author ID")
        void findByAuthorIdAndNotDeleted_ValidAuthorId_ReturnsAuthorComments() {
            // When
            List<TaskComment> result = taskCommentRepository.findByAuthorIdAndNotDeleted(1L);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(comment1.getId());
            assertThat(result.get(0).getCreatedBy()).isEqualTo(1L);
        }
    }
}