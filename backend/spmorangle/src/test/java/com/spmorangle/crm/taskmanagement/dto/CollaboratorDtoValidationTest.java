package com.spmorangle.crm.taskmanagement.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import java.time.OffsetDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Collaborator DTO Validation Tests")
class CollaboratorDtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("AddCollaboratorRequestDto Validation Tests")
    class AddCollaboratorRequestDtoTests {

        @Test
        @DisplayName("Should pass validation with valid data")
        void addCollaboratorRequestDto_ValidData_PassesValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(1L, 2L, 3L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when taskId is null")
        void addCollaboratorRequestDto_NullTaskId_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(null, 2L, 3L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }

        @Test
        @DisplayName("Should fail validation when collaboratorId is null")
        void addCollaboratorRequestDto_NullCollaboratorId_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(1L, null, 3L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }

        @Test
        @DisplayName("Should fail validation when assignedById is null")
        void addCollaboratorRequestDto_NullAssignedById_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(1L, 2L, null);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }

        @Test
        @DisplayName("Should fail validation when taskId is zero")
        void addCollaboratorRequestDto_ZeroTaskId_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(0L, 2L, 3L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when collaboratorId is zero")
        void addCollaboratorRequestDto_ZeroCollaboratorId_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(1L, 0L, 3L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when assignedById is zero")
        void addCollaboratorRequestDto_ZeroAssignedById_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(1L, 2L, 0L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when taskId is negative")
        void addCollaboratorRequestDto_NegativeTaskId_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(-1L, 2L, 3L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when collaboratorId is negative")
        void addCollaboratorRequestDto_NegativeCollaboratorId_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(1L, -1L, 3L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when assignedById is negative")
        void addCollaboratorRequestDto_NegativeAssignedById_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(1L, 2L, -1L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when all fields are null")
        void addCollaboratorRequestDto_AllFieldsNull_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(null, null, null);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should fail validation when all fields are zero")
        void addCollaboratorRequestDto_AllFieldsZero_FailsValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(0L, 0L, 0L);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should pass validation with large valid values")
        void addCollaboratorRequestDto_LargeValidValues_PassesValidation() {
            // Given
            AddCollaboratorRequestDto dto = new AddCollaboratorRequestDto(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);

            // When
            Set<ConstraintViolation<AddCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("RemoveCollaboratorRequestDto Validation Tests")
    class RemoveCollaboratorRequestDtoTests {

        @Test
        @DisplayName("Should pass validation with valid data")
        void removeCollaboratorRequestDto_ValidData_PassesValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(1L, 2L, 3L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when taskId is null")
        void removeCollaboratorRequestDto_NullTaskId_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(null, 2L, 3L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }

        @Test
        @DisplayName("Should fail validation when collaboratorId is null")
        void removeCollaboratorRequestDto_NullCollaboratorId_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(1L, null, 3L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }

        @Test
        @DisplayName("Should fail validation when assignedById is null")
        void removeCollaboratorRequestDto_NullAssignedById_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(1L, 2L, null);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must not be null");
        }

        @Test
        @DisplayName("Should fail validation when taskId is zero")
        void removeCollaboratorRequestDto_ZeroTaskId_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(0L, 2L, 3L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when collaboratorId is zero")
        void removeCollaboratorRequestDto_ZeroCollaboratorId_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(1L, 0L, 3L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when assignedById is zero")
        void removeCollaboratorRequestDto_ZeroAssignedById_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(1L, 2L, 0L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when taskId is negative")
        void removeCollaboratorRequestDto_NegativeTaskId_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(-1L, 2L, 3L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when collaboratorId is negative")
        void removeCollaboratorRequestDto_NegativeCollaboratorId_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(1L, -1L, 3L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when assignedById is negative")
        void removeCollaboratorRequestDto_NegativeAssignedById_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(1L, 2L, -1L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).contains("must be greater than or equal to 1");
        }

        @Test
        @DisplayName("Should fail validation when all fields are null")
        void removeCollaboratorRequestDto_AllFieldsNull_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(null, null, null);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should fail validation when all fields are zero")
        void removeCollaboratorRequestDto_AllFieldsZero_FailsValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(0L, 0L, 0L);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).hasSize(3);
        }

        @Test
        @DisplayName("Should pass validation with large valid values")
        void removeCollaboratorRequestDto_LargeValidValues_PassesValidation() {
            // Given
            RemoveCollaboratorRequestDto dto = new RemoveCollaboratorRequestDto(Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE);

            // When
            Set<ConstraintViolation<RemoveCollaboratorRequestDto>> violations = validator.validate(dto);

            // Then
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("AddCollaboratorResponseDto Tests")
    class AddCollaboratorResponseDtoTests {

        @Test
        @DisplayName("Should create response DTO with all required fields")
        void addCollaboratorResponseDto_AllFields_CreatesCorrectly() {
            // Given
            long taskId = 1L;
            long collaboratorId = 2L;
            long assignedById = 3L;
            OffsetDateTime assignedAt = OffsetDateTime.now();

            // When
            AddCollaboratorResponseDto dto = AddCollaboratorResponseDto.builder()
                    .taskId(taskId)
                    .collaboratorId(collaboratorId)
                    .assignedById(assignedById)
                    .assignedAt(assignedAt)
                    .build();

            // Then
            assertThat(dto.getTaskId()).isEqualTo(taskId);
            assertThat(dto.getCollaboratorId()).isEqualTo(collaboratorId);
            assertThat(dto.getAssignedById()).isEqualTo(assignedById);
            assertThat(dto.getAssignedAt()).isEqualTo(assignedAt);
        }

        @Test
        @DisplayName("Should create response DTO using all-args constructor")
        void addCollaboratorResponseDto_AllArgsConstructor_CreatesCorrectly() {
            // Given
            long taskId = 1L;
            long collaboratorId = 2L;
            long assignedById = 3L;
            OffsetDateTime assignedAt = OffsetDateTime.now();

            // When
            AddCollaboratorResponseDto dto = new AddCollaboratorResponseDto(taskId, collaboratorId, assignedById, assignedAt);

            // Then
            assertThat(dto.getTaskId()).isEqualTo(taskId);
            assertThat(dto.getCollaboratorId()).isEqualTo(collaboratorId);
            assertThat(dto.getAssignedById()).isEqualTo(assignedById);
            assertThat(dto.getAssignedAt()).isEqualTo(assignedAt);
        }

        @Test
        @DisplayName("Should handle different OffsetDateTime values correctly")
        void addCollaboratorResponseDto_DifferentDateTimes_HandlesCorrectly() {
            // Given
            OffsetDateTime now = OffsetDateTime.now();
            OffsetDateTime past = now.minusDays(1);
            OffsetDateTime future = now.plusDays(1);

            // When
            AddCollaboratorResponseDto dtoNow = AddCollaboratorResponseDto.builder()
                    .taskId(1L).collaboratorId(2L).assignedById(3L).assignedAt(now).build();
            AddCollaboratorResponseDto dtoPast = AddCollaboratorResponseDto.builder()
                    .taskId(1L).collaboratorId(2L).assignedById(3L).assignedAt(past).build();
            AddCollaboratorResponseDto dtoFuture = AddCollaboratorResponseDto.builder()
                    .taskId(1L).collaboratorId(2L).assignedById(3L).assignedAt(future).build();

            // Then
            assertThat(dtoNow.getAssignedAt()).isEqualTo(now);
            assertThat(dtoPast.getAssignedAt()).isEqualTo(past);
            assertThat(dtoFuture.getAssignedAt()).isEqualTo(future);
        }
    }
}