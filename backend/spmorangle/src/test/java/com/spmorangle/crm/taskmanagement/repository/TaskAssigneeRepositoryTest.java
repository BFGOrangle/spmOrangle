package com.spmorangle.crm.taskmanagement.repository;

import com.spmorangle.crm.taskmanagement.model.TaskAssignee;
import com.spmorangle.crm.taskmanagement.model.TaskAssigneeCK;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TaskAssigneeRepository Tests")
class TaskAssigneeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskAssigneeRepository taskAssigneeRepository;

    private TaskAssignee taskAssignee1;
    private TaskAssignee taskAssignee2;
    private TaskAssignee taskAssignee3;

    @BeforeEach
    void setUp() {
        // Create test data
        taskAssignee1 = new TaskAssignee();
        taskAssignee1.setTaskId(1L);
        taskAssignee1.setUserId(2L);
        taskAssignee1.setAssignedId(3L);

        taskAssignee2 = new TaskAssignee();
        taskAssignee2.setTaskId(1L);
        taskAssignee2.setUserId(4L);
        taskAssignee2.setAssignedId(3L);

        taskAssignee3 = new TaskAssignee();
        taskAssignee3.setTaskId(2L);
        taskAssignee3.setUserId(2L);
        taskAssignee3.setAssignedId(5L);

        // Persist test data
        entityManager.persistAndFlush(taskAssignee1);
        entityManager.persistAndFlush(taskAssignee2);
        entityManager.persistAndFlush(taskAssignee3);
    }

    @Nested
    @DisplayName("existsByTaskIdAndUserIdAndAssignedId Tests")
    class ExistsByTaskIdAndUserIdAndAssignedIdTests {

        @Test
        @DisplayName("Should return true when assignment exists")
        void existsByTaskIdAndUserIdAndAssignedId_AssignmentExists_ReturnsTrue() {
            // When
            boolean exists = taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L);

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when assignment doesn't exist")
        void existsByTaskIdAndUserIdAndAssignedId_AssignmentDoesNotExist_ReturnsFalse() {
            // When
            boolean exists = taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(999L, 999L, 999L);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false when only task ID matches")
        void existsByTaskIdAndUserIdAndAssignedId_OnlyTaskIdMatches_ReturnsFalse() {
            // When
            boolean exists = taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 999L, 999L);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false when only user ID matches")
        void existsByTaskIdAndUserIdAndAssignedId_OnlyUserIdMatches_ReturnsFalse() {
            // When
            boolean exists = taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(999L, 2L, 999L);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false when only assigned ID matches")
        void existsByTaskIdAndUserIdAndAssignedId_OnlyAssignedIdMatches_ReturnsFalse() {
            // When
            boolean exists = taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(999L, 999L, 3L);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false when task ID and user ID match but assigned ID doesn't")
        void existsByTaskIdAndUserIdAndAssignedId_TaskIdAndUserIdMatch_ReturnsFalse() {
            // When
            boolean exists = taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 999L);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false when task ID and assigned ID match but user ID doesn't")
        void existsByTaskIdAndUserIdAndAssignedId_TaskIdAndAssignedIdMatch_ReturnsFalse() {
            // When
            boolean exists = taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 999L, 3L);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should return false when user ID and assigned ID match but task ID doesn't")
        void existsByTaskIdAndUserIdAndAssignedId_UserIdAndAssignedIdMatch_ReturnsFalse() {
            // When
            boolean exists = taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(999L, 2L, 3L);

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should handle multiple assignments for same task correctly")
        void existsByTaskIdAndUserIdAndAssignedId_MultipleAssignmentsForSameTask_HandlesCorrectly() {
            // Task 1 has both user 2 and user 4 assigned by user 3
            // When & Then
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L)).isTrue();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 4L, 3L)).isTrue();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 5L, 3L)).isFalse();
        }

        @Test
        @DisplayName("Should handle same user assigned to different tasks correctly")
        void existsByTaskIdAndUserIdAndAssignedId_SameUserDifferentTasks_HandlesCorrectly() {
            // User 2 is assigned to both task 1 and task 2
            // When & Then
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L)).isTrue();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(2L, 2L, 5L)).isTrue();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(2L, 2L, 3L)).isFalse();
        }

        @Test
        @DisplayName("Should handle zero values correctly")
        void existsByTaskIdAndUserIdAndAssignedId_ZeroValues_ReturnsFalse() {
            // When & Then
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(0L, 0L, 0L)).isFalse();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 0L, 3L)).isFalse();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(0L, 2L, 3L)).isFalse();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 0L)).isFalse();
        }

        @Test
        @DisplayName("Should handle negative values correctly")
        void existsByTaskIdAndUserIdAndAssignedId_NegativeValues_ReturnsFalse() {
            // When & Then
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(-1L, -1L, -1L)).isFalse();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, -1L, 3L)).isFalse();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(-1L, 2L, 3L)).isFalse();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, -1L)).isFalse();
        }
    }

    @Nested
    @DisplayName("CRUD Operations Tests")
    class CrudOperationsTests {

        @Test
        @DisplayName("Should save and retrieve TaskAssignee correctly")
        void save_ValidTaskAssignee_SavesAndRetrievesCorrectly() {
            // Given
            TaskAssignee newAssignee = new TaskAssignee();
            newAssignee.setTaskId(10L);
            newAssignee.setUserId(20L);
            newAssignee.setAssignedId(30L);

            // When
            TaskAssignee saved = taskAssigneeRepository.save(newAssignee);
            entityManager.flush();

            TaskAssigneeCK key = new TaskAssigneeCK(10L, 20L, 30L);
            TaskAssignee retrieved = taskAssigneeRepository.findById(key).orElse(null);

            // Then
            assertThat(saved).isNotNull();
            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getTaskId()).isEqualTo(10L);
            assertThat(retrieved.getUserId()).isEqualTo(20L);
            assertThat(retrieved.getAssignedId()).isEqualTo(30L);
            assertThat(retrieved.getAssignedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should delete TaskAssignee by composite key correctly")
        void deleteById_ValidCompositeKey_DeletesCorrectly() {
            // Given
            TaskAssigneeCK key = new TaskAssigneeCK(1L, 2L, 3L);

            // Verify exists before deletion
            assertThat(taskAssigneeRepository.existsById(key)).isTrue();

            // When
            taskAssigneeRepository.deleteById(key);
            entityManager.flush();

            // Then
            assertThat(taskAssigneeRepository.existsById(key)).isFalse();
            assertThat(taskAssigneeRepository.existsByTaskIdAndUserIdAndAssignedId(1L, 2L, 3L)).isFalse();
        }

        @Test
        @DisplayName("Should handle finding by non-existent composite key")
        void findById_NonExistentKey_ReturnsEmpty() {
            // Given
            TaskAssigneeCK nonExistentKey = new TaskAssigneeCK(999L, 999L, 999L);

            // When
            var result = taskAssigneeRepository.findById(nonExistentKey);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should count all task assignees correctly")
        void count_ExistingData_ReturnsCorrectCount() {
            // When
            long count = taskAssigneeRepository.count();

            // Then
            assertThat(count).isEqualTo(3L); // We persisted 3 entities in setUp
        }
    }
}