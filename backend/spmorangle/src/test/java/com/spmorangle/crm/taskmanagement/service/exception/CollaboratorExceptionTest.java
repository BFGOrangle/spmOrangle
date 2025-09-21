package com.spmorangle.crm.taskmanagement.service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Collaborator Exception Tests")
class CollaboratorExceptionTest {

    @Nested
    @DisplayName("CollaboratorAlreadyExistsException Tests")
    class CollaboratorAlreadyExistsExceptionTests {

        @Test
        @DisplayName("Should create exception with correct message for positive IDs")
        void collaboratorAlreadyExistsException_PositiveIds_CreatesCorrectMessage() {
            // Given
            long taskId = 123L;
            long collaboratorId = 456L;

            // When
            CollaboratorAlreadyExistsException exception = new CollaboratorAlreadyExistsException(taskId, collaboratorId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator 456 already assigned to task 123");
        }

        @Test
        @DisplayName("Should create exception with correct message for large IDs")
        void collaboratorAlreadyExistsException_LargeIds_CreatesCorrectMessage() {
            // Given
            long taskId = Long.MAX_VALUE;
            long collaboratorId = Long.MAX_VALUE - 1;

            // When
            CollaboratorAlreadyExistsException exception = new CollaboratorAlreadyExistsException(taskId, collaboratorId);

            // Then
            String expectedMessage = String.format("Collaborator %d already assigned to task %d", collaboratorId, taskId);
            assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("Should create exception with correct message for zero IDs")
        void collaboratorAlreadyExistsException_ZeroIds_CreatesCorrectMessage() {
            // Given
            long taskId = 0L;
            long collaboratorId = 0L;

            // When
            CollaboratorAlreadyExistsException exception = new CollaboratorAlreadyExistsException(taskId, collaboratorId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator 0 already assigned to task 0");
        }

        @Test
        @DisplayName("Should create exception with correct message for negative IDs")
        void collaboratorAlreadyExistsException_NegativeIds_CreatesCorrectMessage() {
            // Given
            long taskId = -1L;
            long collaboratorId = -2L;

            // When
            CollaboratorAlreadyExistsException exception = new CollaboratorAlreadyExistsException(taskId, collaboratorId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator -2 already assigned to task -1");
        }

        @Test
        @DisplayName("Should be a RuntimeException")
        void collaboratorAlreadyExistsException_InheritanceCheck_IsRuntimeException() {
            // Given
            CollaboratorAlreadyExistsException exception = new CollaboratorAlreadyExistsException(1L, 2L);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should have ResponseStatus annotation with CONFLICT status")
        void collaboratorAlreadyExistsException_ResponseStatusAnnotation_HasConflictStatus() {
            // Given
            Class<CollaboratorAlreadyExistsException> exceptionClass = CollaboratorAlreadyExistsException.class;

            // When
            ResponseStatus responseStatus = exceptionClass.getAnnotation(ResponseStatus.class);

            // Then
            assertThat(responseStatus).isNotNull();
            assertThat(responseStatus.value()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("Should handle same task and collaborator IDs")
        void collaboratorAlreadyExistsException_SameIds_CreatesCorrectMessage() {
            // Given
            long sameId = 100L;

            // When
            CollaboratorAlreadyExistsException exception = new CollaboratorAlreadyExistsException(sameId, sameId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator 100 already assigned to task 100");
        }

        @Test
        @DisplayName("Should handle mixed positive and negative IDs")
        void collaboratorAlreadyExistsException_MixedIds_CreatesCorrectMessage() {
            // Given
            long taskId = 50L;
            long collaboratorId = -25L;

            // When
            CollaboratorAlreadyExistsException exception = new CollaboratorAlreadyExistsException(taskId, collaboratorId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator -25 already assigned to task 50");
        }
    }

    @Nested
    @DisplayName("CollaboratorAssignmentNotFoundException Tests")
    class CollaboratorAssignmentNotFoundExceptionTests {

        @Test
        @DisplayName("Should create exception with correct message for positive IDs")
        void collaboratorAssignmentNotFoundException_PositiveIds_CreatesCorrectMessage() {
            // Given
            long taskId = 789L;
            long collaboratorId = 321L;

            // When
            CollaboratorAssignmentNotFoundException exception = new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator 321 not assigned to task 789");
        }

        @Test
        @DisplayName("Should create exception with correct message for large IDs")
        void collaboratorAssignmentNotFoundException_LargeIds_CreatesCorrectMessage() {
            // Given
            long taskId = Long.MAX_VALUE - 5;
            long collaboratorId = Long.MAX_VALUE - 10;

            // When
            CollaboratorAssignmentNotFoundException exception = new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);

            // Then
            String expectedMessage = String.format("Collaborator %d not assigned to task %d", collaboratorId, taskId);
            assertThat(exception.getMessage()).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("Should create exception with correct message for zero IDs")
        void collaboratorAssignmentNotFoundException_ZeroIds_CreatesCorrectMessage() {
            // Given
            long taskId = 0L;
            long collaboratorId = 0L;

            // When
            CollaboratorAssignmentNotFoundException exception = new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator 0 not assigned to task 0");
        }

        @Test
        @DisplayName("Should create exception with correct message for negative IDs")
        void collaboratorAssignmentNotFoundException_NegativeIds_CreatesCorrectMessage() {
            // Given
            long taskId = -10L;
            long collaboratorId = -20L;

            // When
            CollaboratorAssignmentNotFoundException exception = new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator -20 not assigned to task -10");
        }

        @Test
        @DisplayName("Should be a RuntimeException")
        void collaboratorAssignmentNotFoundException_InheritanceCheck_IsRuntimeException() {
            // Given
            CollaboratorAssignmentNotFoundException exception = new CollaboratorAssignmentNotFoundException(1L, 2L);

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should have ResponseStatus annotation with NOT_FOUND status")
        void collaboratorAssignmentNotFoundException_ResponseStatusAnnotation_HasNotFoundStatus() {
            // Given
            Class<CollaboratorAssignmentNotFoundException> exceptionClass = CollaboratorAssignmentNotFoundException.class;

            // When
            ResponseStatus responseStatus = exceptionClass.getAnnotation(ResponseStatus.class);

            // Then
            assertThat(responseStatus).isNotNull();
            assertThat(responseStatus.value()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Should handle same task and collaborator IDs")
        void collaboratorAssignmentNotFoundException_SameIds_CreatesCorrectMessage() {
            // Given
            long sameId = 200L;

            // When
            CollaboratorAssignmentNotFoundException exception = new CollaboratorAssignmentNotFoundException(sameId, sameId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator 200 not assigned to task 200");
        }

        @Test
        @DisplayName("Should handle mixed positive and negative IDs")
        void collaboratorAssignmentNotFoundException_MixedIds_CreatesCorrectMessage() {
            // Given
            long taskId = -75L;
            long collaboratorId = 150L;

            // When
            CollaboratorAssignmentNotFoundException exception = new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Collaborator 150 not assigned to task -75");
        }
    }

    @Nested
    @DisplayName("Exception Comparison Tests")
    class ExceptionComparisonTests {

        @Test
        @DisplayName("Should have different HTTP status codes")
        void exceptionComparison_HttpStatusCodes_AreDifferent() {
            // Given
            ResponseStatus alreadyExistsStatus = CollaboratorAlreadyExistsException.class.getAnnotation(ResponseStatus.class);
            ResponseStatus notFoundStatus = CollaboratorAssignmentNotFoundException.class.getAnnotation(ResponseStatus.class);

            // Then
            assertThat(alreadyExistsStatus.value()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(notFoundStatus.value()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(alreadyExistsStatus.value()).isNotEqualTo(notFoundStatus.value());
        }

        @Test
        @DisplayName("Should have different message patterns for same IDs")
        void exceptionComparison_SameIds_DifferentMessagePatterns() {
            // Given
            long taskId = 100L;
            long collaboratorId = 200L;

            // When
            CollaboratorAlreadyExistsException alreadyExistsException = new CollaboratorAlreadyExistsException(taskId, collaboratorId);
            CollaboratorAssignmentNotFoundException notFoundException = new CollaboratorAssignmentNotFoundException(taskId, collaboratorId);

            // Then
            assertThat(alreadyExistsException.getMessage()).contains("already assigned");
            assertThat(notFoundException.getMessage()).contains("not assigned");
            assertThat(alreadyExistsException.getMessage()).isNotEqualTo(notFoundException.getMessage());
        }

        @Test
        @DisplayName("Should both extend RuntimeException")
        void exceptionComparison_Inheritance_BothExtendRuntimeException() {
            // Given
            CollaboratorAlreadyExistsException alreadyExistsException = new CollaboratorAlreadyExistsException(1L, 2L);
            CollaboratorAssignmentNotFoundException notFoundException = new CollaboratorAssignmentNotFoundException(1L, 2L);

            // Then
            assertThat(alreadyExistsException).isInstanceOf(RuntimeException.class);
            assertThat(notFoundException).isInstanceOf(RuntimeException.class);
        }
    }
}