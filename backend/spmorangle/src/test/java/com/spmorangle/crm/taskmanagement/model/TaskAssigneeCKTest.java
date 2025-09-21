package com.spmorangle.crm.taskmanagement.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TaskAssigneeCK Tests")
class TaskAssigneeCKTest {

    private TaskAssigneeCK taskAssigneeCK1;
    private TaskAssigneeCK taskAssigneeCK2;
    private TaskAssigneeCK taskAssigneeCK3;

    @BeforeEach
    void setUp() {
        taskAssigneeCK1 = new TaskAssigneeCK(1L, 2L, 3L);
        taskAssigneeCK2 = new TaskAssigneeCK(1L, 2L, 3L);
        taskAssigneeCK3 = new TaskAssigneeCK(4L, 5L, 6L);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create instance with default constructor")
        void defaultConstructor_CreateInstance_SetsDefaultValues() {
            // When
            TaskAssigneeCK key = new TaskAssigneeCK();

            // Then
            assertThat(key).isNotNull();
            assertThat(key.getTaskId()).isEqualTo(0L);
            assertThat(key.getUserId()).isEqualTo(0L);
            assertThat(key.getAssignedId()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should create instance with parameterized constructor")
        void parameterizedConstructor_CreateInstance_SetsCorrectValues() {
            // Given
            long taskId = 100L;
            long userId = 200L;
            long assignedId = 300L;

            // When
            TaskAssigneeCK key = new TaskAssigneeCK(taskId, userId, assignedId);

            // Then
            assertThat(key.getTaskId()).isEqualTo(taskId);
            assertThat(key.getUserId()).isEqualTo(userId);
            assertThat(key.getAssignedId()).isEqualTo(assignedId);
        }

        @Test
        @DisplayName("Should handle zero values in constructor")
        void parameterizedConstructor_ZeroValues_SetsCorrectly() {
            // When
            TaskAssigneeCK key = new TaskAssigneeCK(0L, 0L, 0L);

            // Then
            assertThat(key.getTaskId()).isEqualTo(0L);
            assertThat(key.getUserId()).isEqualTo(0L);
            assertThat(key.getAssignedId()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle negative values in constructor")
        void parameterizedConstructor_NegativeValues_SetsCorrectly() {
            // When
            TaskAssigneeCK key = new TaskAssigneeCK(-1L, -2L, -3L);

            // Then
            assertThat(key.getTaskId()).isEqualTo(-1L);
            assertThat(key.getUserId()).isEqualTo(-2L);
            assertThat(key.getAssignedId()).isEqualTo(-3L);
        }

        @Test
        @DisplayName("Should handle large values in constructor")
        void parameterizedConstructor_LargeValues_SetsCorrectly() {
            // Given
            long maxValue = Long.MAX_VALUE;

            // When
            TaskAssigneeCK key = new TaskAssigneeCK(maxValue, maxValue - 1, maxValue - 2);

            // Then
            assertThat(key.getTaskId()).isEqualTo(maxValue);
            assertThat(key.getUserId()).isEqualTo(maxValue - 1);
            assertThat(key.getAssignedId()).isEqualTo(maxValue - 2);
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should set and get taskId correctly")
        void setGetTaskId_ValidValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK key = new TaskAssigneeCK();
            long taskId = 999L;

            // When
            key.setTaskId(taskId);

            // Then
            assertThat(key.getTaskId()).isEqualTo(taskId);
        }

        @Test
        @DisplayName("Should set and get userId correctly")
        void setGetUserId_ValidValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK key = new TaskAssigneeCK();
            long userId = 888L;

            // When
            key.setUserId(userId);

            // Then
            assertThat(key.getUserId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("Should set and get assignedId correctly")
        void setGetAssignedId_ValidValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK key = new TaskAssigneeCK();
            long assignedId = 777L;

            // When
            key.setAssignedId(assignedId);

            // Then
            assertThat(key.getAssignedId()).isEqualTo(assignedId);
        }

        @Test
        @DisplayName("Should handle setting zero values")
        void setters_ZeroValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK key = new TaskAssigneeCK(1L, 2L, 3L);

            // When
            key.setTaskId(0L);
            key.setUserId(0L);
            key.setAssignedId(0L);

            // Then
            assertThat(key.getTaskId()).isEqualTo(0L);
            assertThat(key.getUserId()).isEqualTo(0L);
            assertThat(key.getAssignedId()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Should handle setting negative values")
        void setters_NegativeValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK key = new TaskAssigneeCK();

            // When
            key.setTaskId(-10L);
            key.setUserId(-20L);
            key.setAssignedId(-30L);

            // Then
            assertThat(key.getTaskId()).isEqualTo(-10L);
            assertThat(key.getUserId()).isEqualTo(-20L);
            assertThat(key.getAssignedId()).isEqualTo(-30L);
        }
    }

    @Nested
    @DisplayName("Equals Tests")
    class EqualsTests {

        @Test
        @DisplayName("Should return true for same object reference")
        void equals_SameReference_ReturnsTrue() {
            // When
            boolean result = taskAssigneeCK1.equals(taskAssigneeCK1);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true for objects with same values")
        void equals_SameValues_ReturnsTrue() {
            // When
            boolean result = taskAssigneeCK1.equals(taskAssigneeCK2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false for objects with different values")
        void equals_DifferentValues_ReturnsFalse() {
            // When
            boolean result = taskAssigneeCK1.equals(taskAssigneeCK3);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when compared with null")
        void equals_NullObject_ReturnsFalse() {
            // When
            boolean result = taskAssigneeCK1.equals(null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when compared with different class")
        void equals_DifferentClass_ReturnsFalse() {
            // Given
            Object differentObject = "different";

            // When
            boolean result = taskAssigneeCK1.equals(differentObject);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when only taskId differs")
        void equals_OnlyTaskIdDiffers_ReturnsFalse() {
            // Given
            TaskAssigneeCK differentTaskId = new TaskAssigneeCK(999L, 2L, 3L);

            // When
            boolean result = taskAssigneeCK1.equals(differentTaskId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when only userId differs")
        void equals_OnlyUserIdDiffers_ReturnsFalse() {
            // Given
            TaskAssigneeCK differentUserId = new TaskAssigneeCK(1L, 999L, 3L);

            // When
            boolean result = taskAssigneeCK1.equals(differentUserId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should return false when only assignedId differs")
        void equals_OnlyAssignedIdDiffers_ReturnsFalse() {
            // Given
            TaskAssigneeCK differentAssignedId = new TaskAssigneeCK(1L, 2L, 999L);

            // When
            boolean result = taskAssigneeCK1.equals(differentAssignedId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle zero values in equals comparison")
        void equals_ZeroValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK zeroKey1 = new TaskAssigneeCK(0L, 0L, 0L);
            TaskAssigneeCK zeroKey2 = new TaskAssigneeCK(0L, 0L, 0L);

            // When
            boolean result = zeroKey1.equals(zeroKey2);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle negative values in equals comparison")
        void equals_NegativeValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK negativeKey1 = new TaskAssigneeCK(-1L, -2L, -3L);
            TaskAssigneeCK negativeKey2 = new TaskAssigneeCK(-1L, -2L, -3L);

            // When
            boolean result = negativeKey1.equals(negativeKey2);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("HashCode Tests")
    class HashCodeTests {

        @Test
        @DisplayName("Should return same hash code for equal objects")
        void hashCode_EqualObjects_ReturnsSameHashCode() {
            // When
            int hash1 = taskAssigneeCK1.hashCode();
            int hash2 = taskAssigneeCK2.hashCode();

            // Then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Should return different hash codes for different objects")
        void hashCode_DifferentObjects_ReturnsDifferentHashCodes() {
            // When
            int hash1 = taskAssigneeCK1.hashCode();
            int hash3 = taskAssigneeCK3.hashCode();

            // Then
            assertThat(hash1).isNotEqualTo(hash3);
        }

        @Test
        @DisplayName("Should return consistent hash code for same object")
        void hashCode_SameObject_ReturnsConsistentHashCode() {
            // When
            int hash1 = taskAssigneeCK1.hashCode();
            int hash2 = taskAssigneeCK1.hashCode();

            // Then
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Should handle zero values in hash code")
        void hashCode_ZeroValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK zeroKey = new TaskAssigneeCK(0L, 0L, 0L);

            // When
            int hashCode = zeroKey.hashCode();

            // Then
            assertThat(hashCode).isNotNull();
        }

        @Test
        @DisplayName("Should handle negative values in hash code")
        void hashCode_NegativeValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK negativeKey = new TaskAssigneeCK(-1L, -2L, -3L);

            // When
            int hashCode = negativeKey.hashCode();

            // Then
            assertThat(hashCode).isNotNull();
        }

        @Test
        @DisplayName("Should handle large values in hash code")
        void hashCode_LargeValues_WorksCorrectly() {
            // Given
            TaskAssigneeCK largeKey = new TaskAssigneeCK(Long.MAX_VALUE, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2);

            // When
            int hashCode = largeKey.hashCode();

            // Then
            assertThat(hashCode).isNotNull();
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Contract Tests")
    class EqualsHashCodeContractTests {

        @Test
        @DisplayName("Should maintain equals-hashcode contract")
        void equalsHashCodeContract_EqualObjects_HaveSameHashCode() {
            // Given
            TaskAssigneeCK key1 = new TaskAssigneeCK(10L, 20L, 30L);
            TaskAssigneeCK key2 = new TaskAssigneeCK(10L, 20L, 30L);

            // When
            boolean areEqual = key1.equals(key2);
            int hash1 = key1.hashCode();
            int hash2 = key2.hashCode();

            // Then
            assertThat(areEqual).isTrue();
            assertThat(hash1).isEqualTo(hash2);
        }

        @Test
        @DisplayName("Should be reflexive - object equals itself")
        void equals_Reflexive_ObjectEqualsItself() {
            // When & Then
            assertThat(taskAssigneeCK1.equals(taskAssigneeCK1)).isTrue();
        }

        @Test
        @DisplayName("Should be symmetric - if a equals b, then b equals a")
        void equals_Symmetric_MutualEquality() {
            // When & Then
            assertThat(taskAssigneeCK1.equals(taskAssigneeCK2)).isEqualTo(taskAssigneeCK2.equals(taskAssigneeCK1));
        }

        @Test
        @DisplayName("Should be transitive - if a equals b and b equals c, then a equals c")
        void equals_Transitive_TransitiveEquality() {
            // Given
            TaskAssigneeCK key1 = new TaskAssigneeCK(1L, 2L, 3L);
            TaskAssigneeCK key2 = new TaskAssigneeCK(1L, 2L, 3L);
            TaskAssigneeCK key3 = new TaskAssigneeCK(1L, 2L, 3L);

            // When & Then
            assertThat(key1.equals(key2)).isTrue();
            assertThat(key2.equals(key3)).isTrue();
            assertThat(key1.equals(key3)).isTrue();
        }
    }

    @Nested
    @DisplayName("Serializable Tests")
    class SerializableTests {

        @Test
        @DisplayName("Should implement Serializable interface")
        void taskAssigneeCK_ImplementsSerializable() {
            // Then
            assertThat(taskAssigneeCK1).isInstanceOf(Serializable.class);
        }

        @Test
        @DisplayName("Should be assignable to Serializable")
        void taskAssigneeCK_AssignableToSerializable() {
            // When
            Serializable serializable = taskAssigneeCK1;

            // Then
            assertThat(serializable).isNotNull();
            assertThat(serializable).isInstanceOf(TaskAssigneeCK.class);
        }
    }
}