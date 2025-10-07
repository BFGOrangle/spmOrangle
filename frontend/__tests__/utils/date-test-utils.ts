/**
 * Utility functions for testing due date functionality
 */

/**
 * Mock a date to ensure consistent test results
 */
export const mockCurrentDate = (dateString: string) => {
  const mockDate = new Date(dateString);
  jest.useFakeTimers();
  jest.setSystemTime(mockDate);
};

/**
 * Restore real timers after testing
 */
export const restoreRealTimers = () => {
  jest.useRealTimers();
};

/**
 * Generate a datetime string for a future date
 */
export const getFutureDateString = (daysFromNow: number, hour: number = 12, minute: number = 0): string => {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  date.setHours(hour, minute, 0, 0);
  return date.toISOString().slice(0, 16); // Format: YYYY-MM-DDTHH:mm
};

/**
 * Generate a datetime ISO string for a future date
 */
export const getFutureISOString = (daysFromNow: number, hour: number = 12, minute: number = 0): string => {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  date.setHours(hour, minute, 0, 0);
  return date.toISOString();
};

/**
 * Generate a past datetime ISO string
 */
export const getPastISOString = (daysAgo: number, hour: number = 12, minute: number = 0): string => {
  const date = new Date();
  date.setDate(date.getDate() - daysAgo);
  date.setHours(hour, minute, 0, 0);
  return date.toISOString();
};

/**
 * Test data for various timezone scenarios
 */
export const timezoneTestCases = [
  {
    name: 'UTC midnight',
    utc: '2025-12-25T00:00:00.000Z',
    expectedPatterns: [/Dec.*25.*2025/i, /Dec.*24.*2025/i] // Could be either depending on timezone
  },
  {
    name: 'UTC noon',
    utc: '2025-06-15T12:00:00.000Z',
    expectedPatterns: [/Jun.*15.*2025/i]
  },
  {
    name: 'UTC near midnight',
    utc: '2025-08-20T23:59:00.000Z',
    expectedPatterns: [/Aug.*20.*2025/i, /Aug.*21.*2025/i] // Could cross midnight
  },
  {
    name: 'Leap year date',
    utc: '2028-02-29T15:30:00.000Z',
    expectedPatterns: [/Feb.*29.*2028/i]
  },
  {
    name: 'New Year transition',
    utc: '2025-12-31T23:30:00.000Z',
    expectedPatterns: [/Dec.*31.*2025/i, /Jan.*1.*2026/i] // Could cross new year
  }
];

/**
 * Test data for various date formats
 */
export const dateFormatTestCases = [
  {
    input: '2025-01-15T09:30:00.000Z',
    description: 'Morning time',
    expectedHour: /9|10/i, // Could be 9 AM or 10 AM depending on timezone
    expectedAMPM: /AM/i
  },
  {
    input: '2025-06-20T13:45:00.000Z',
    description: 'Afternoon time',
    expectedHour: /1|2/i, // Could be 1 PM or 2 PM depending on timezone
    expectedAMPM: /PM/i
  },
  {
    input: '2025-12-31T22:15:00.000Z',
    description: 'Evening time',
    expectedHour: /6|7|8|9|10/i, // Various possible hours depending on timezone
    expectedAMPM: /AM|PM/i
  }
];

/**
 * Validate that a date string matches expected patterns
 */
export const validateDateFormat = (dateString: string, patterns: RegExp[]): boolean => {
  return patterns.some(pattern => pattern.test(dateString));
};

/**
 * Common assertions for due date testing
 */
export const dueDateAssertions = {
  /**
   * Assert that a due date display contains required elements
   */
  hasRequiredElements: (dueDateText: string) => {
    expect(dueDateText).toMatch(/\w{3}/); // Month abbreviation
    expect(dueDateText).toMatch(/\d{1,2}/); // Day
    expect(dueDateText).toMatch(/\d{4}/); // Year
    expect(dueDateText).toMatch(/\d{1,2}:\d{2}/); // Time
    expect(dueDateText).toMatch(/AM|PM/i); // 12-hour format
  },

  /**
   * Assert that a due date is in Singapore locale format
   */
  isSingaporeLocale: (dueDateText: string) => {
    // Singapore format: "Aug 15, 2025, 12:30 PM"
    expect(dueDateText).toMatch(/\w{3}\s+\d{1,2},\s+\d{4}/); // Month Day, Year
    expect(dueDateText).toMatch(/\d{1,2}:\d{2}\s+(AM|PM)/i); // Time with AM/PM
  },

  /**
   * Assert that a date represents a specific year/month
   */
  representsDate: (dueDateText: string, year: number, month: string) => {
    expect(dueDateText).toMatch(new RegExp(month, 'i'));
    expect(dueDateText).toMatch(new RegExp(year.toString()));
  }
};

/**
 * Edge case test scenarios
 */
export const edgeCaseScenarios = [
  {
    name: 'Leap year February 29th',
    validDate: '2028-02-29T12:00:00.000Z',
    invalidDate: '2025-02-29T12:00:00.000Z' // Non-leap year
  },
  {
    name: 'End of year',
    date: '2025-12-31T23:59:59.999Z'
  },
  {
    name: 'Beginning of year',
    date: '2025-01-01T00:00:00.000Z'
  },
  {
    name: 'Daylight saving transition (US)',
    date: '2025-03-09T07:00:00.000Z' // 2 AM EST becomes 3 AM EDT
  },
  {
    name: 'Daylight saving transition (EU)',
    date: '2025-03-30T01:00:00.000Z' // 2 AM CET becomes 3 AM CEST
  }
];

/**
 * Generate test cases for all months
 */
export const generateMonthTestCases = () => {
  const months = [
    { num: 1, name: 'Jan' },
    { num: 2, name: 'Feb' },
    { num: 3, name: 'Mar' },
    { num: 4, name: 'Apr' },
    { num: 5, name: 'May' },
    { num: 6, name: 'Jun' },
    { num: 7, name: 'Jul' },
    { num: 8, name: 'Aug' },
    { num: 9, name: 'Sep' },
    { num: 10, name: 'Oct' },
    { num: 11, name: 'Nov' },
    { num: 12, name: 'Dec' }
  ];

  return months.map(({ num, name }) => ({
    input: `2025-${num.toString().padStart(2, '0')}-15T12:00:00.000Z`,
    expected: new RegExp(name, 'i'),
    monthName: name,
    monthNumber: num
  }));
};

/**
 * Helper to create datetime-local string from ISO string
 */
export const isoToDatetimeLocal = (isoString: string): string => {
  const date = new Date(isoString);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  return `${year}-${month}-${day}T${hours}:${minutes}`;
};

/**
 * Helper to create ISO string from datetime-local string
 */
export const datetimeLocalToISO = (datetimeLocal: string): string => {
  const date = new Date(datetimeLocal);
  return date.toISOString();
};

/**
 * Verify ISO string format
 */
export const isValidISOString = (dateString: string): boolean => {
  return /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/.test(dateString);
};

/**
 * Verify datetime-local format
 */
export const isValidDatetimeLocal = (dateString: string): boolean => {
  return /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(dateString);
};
