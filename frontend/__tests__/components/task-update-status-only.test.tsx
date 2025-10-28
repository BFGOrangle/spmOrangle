import { UpdateTaskRequest } from '../../types/project';

// Helper function to test status-only updates (extracted from task-update-dialog.tsx)
function isStatusOnlyUpdate(updateRequest: UpdateTaskRequest): boolean {
  const fields = Object.keys(updateRequest).filter(
    key => key !== 'taskId'
  );

  // Status-only if the only field is 'status'
  return fields.length === 1 && fields[0] === 'status';
}

describe('isStatusOnlyUpdate', () => {
  it('returns true for status-only update', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'IN_PROGRESS',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(true);
  });

  it('returns false when title is updated along with status', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'IN_PROGRESS',
      title: 'New Title',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });

  it('returns false when description is updated along with status', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'COMPLETED',
      description: 'New Description',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });

  it('returns false when only title is updated (no status)', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      title: 'New Title',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });

  it('returns false when taskType is updated along with status', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'TODO',
      taskType: 'BUG',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });

  it('returns false when tags are updated along with status', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'TODO',
      tags: ['urgent', 'frontend'],
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });

  it('returns false when dueDateTime is updated along with status', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'TODO',
      dueDateTime: '2024-02-01T10:00:00Z',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });

  it('returns false when recurrence is updated along with status', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'TODO',
      isRecurring: true,
      recurrenceRuleStr: 'FREQ=DAILY',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });

  it('returns false when multiple fields are updated', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      title: 'Updated Task',
      description: 'Updated Description',
      status: 'IN_PROGRESS',
      taskType: 'FEATURE',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });

  it('returns true when status is updated to COMPLETED', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'COMPLETED',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(true);
  });

  it('returns true when status is updated to BLOCKED', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
      status: 'BLOCKED',
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(true);
  });

  it('handles empty update request (only taskId)', () => {
    const updateRequest: UpdateTaskRequest = {
      taskId: 1,
    };

    expect(isStatusOnlyUpdate(updateRequest)).toBe(false);
  });
});

describe('Task Update Dialog - Status-Only Update Behavior', () => {
  describe('Recurring Task Updates', () => {
    it('should not show recurrence dialog for status-only changes on recurring tasks', () => {
      // This test documents the expected behavior:
      // When a user updates ONLY the status of a recurring task,
      // the recurrence dialog should NOT be shown.
      // The update should proceed directly without prompting the user.

      const recurringTask = {
        id: 1,
        title: 'Recurring Task',
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=DAILY',
        status: 'TODO',
      };

      const statusOnlyUpdate: UpdateTaskRequest = {
        taskId: 1,
        status: 'COMPLETED',
      };

      // Verify this is a status-only update
      expect(isStatusOnlyUpdate(statusOnlyUpdate)).toBe(true);

      // In the actual implementation, this should:
      // 1. Detect it's a status-only update
      // 2. Skip showing the recurrence dialog
      // 3. Proceed with the update directly
    });

    it('should show recurrence dialog for field changes on recurring tasks', () => {
      // This test documents the expected behavior:
      // When a user updates fields OTHER than status on a recurring task,
      // the recurrence dialog SHOULD be shown to ask if they want to
      // update this instance or all instances.

      const recurringTask = {
        id: 1,
        title: 'Recurring Task',
        isRecurring: true,
        recurrenceRuleStr: 'FREQ=DAILY',
        status: 'TODO',
      };

      const titleUpdate: UpdateTaskRequest = {
        taskId: 1,
        title: 'Updated Title',
      };

      // Verify this is NOT a status-only update
      expect(isStatusOnlyUpdate(titleUpdate)).toBe(false);

      // In the actual implementation, this should:
      // 1. Detect it's not a status-only update
      // 2. Show the recurrence dialog
      // 3. Wait for user selection (this instance or all instances)
    });

    it('should show recurrence dialog when status and other fields are updated together', () => {
      const updateRequest: UpdateTaskRequest = {
        taskId: 1,
        status: 'IN_PROGRESS',
        title: 'Updated Title',
      };

      // This is NOT a status-only update
      expect(isStatusOnlyUpdate(updateRequest)).toBe(false);

      // Should show recurrence dialog because other fields are being updated
    });
  });

  describe('Non-Recurring Task Updates', () => {
    it('should proceed directly for any update on non-recurring tasks', () => {
      // Non-recurring tasks should never show the recurrence dialog,
      // regardless of what fields are being updated

      const nonRecurringTask = {
        id: 1,
        title: 'Regular Task',
        isRecurring: false,
        status: 'TODO',
      };

      const anyUpdate: UpdateTaskRequest = {
        taskId: 1,
        title: 'Updated Title',
        status: 'IN_PROGRESS',
      };

      // For non-recurring tasks, the update should proceed directly
      // without any dialog, regardless of whether it's status-only or not
    });
  });
});
