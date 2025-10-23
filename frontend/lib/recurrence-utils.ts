import { RRule } from "rrule";

/**
 * Formats an RRULE string into a human-readable text
 * @param rruleString - The RRULE string (e.g., "RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,WE")
 * @returns Human-readable recurrence description (e.g., "Weekly on Monday, Wednesday")
 */
export function formatRecurrenceRule(rruleString: string | null | undefined): string {
  if (!rruleString) {
    return "";
  }

  try {
    // Handle both formats: "RRULE:FREQ=..." and "FREQ=..."
    const rruleToUse = rruleString.startsWith("RRULE:")
      ? rruleString
      : `RRULE:${rruleString}`;

    const rule = RRule.fromString(rruleToUse);

    // Use RRule's built-in toText() method for human-readable output
    const text = rule.toText();

    // Capitalize first letter and return
    return text.charAt(0).toUpperCase() + text.slice(1);
  } catch (error) {
    console.error("Error parsing RRULE:", error);
    return "Invalid recurrence rule";
  }
}

/**
 * Checks if a task is recurring
 * @param task - The task object
 * @returns True if the task has recurrence enabled
 */
export function isRecurringTask(task: { isRecurring?: boolean; recurrenceRuleStr?: string | null }): boolean {
  return task.isRecurring === true && task.recurrenceRuleStr !== null && task.recurrenceRuleStr !== undefined;
}
