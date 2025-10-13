import { highlightSearchTerm, getSearchHighlightClasses } from '@/lib/search-utils';

describe('Search Utils', () => {
  describe('highlightSearchTerm', () => {
    it('should highlight matching text with mark element', () => {
      const result = highlightSearchTerm('This is a test message', 'test');
      
      // The function returns an array of React nodes
      expect(Array.isArray(result)).toBe(true);
      const resultArray = result as React.ReactNode[];
      
      expect(resultArray).toHaveLength(3);
      expect(resultArray[0]).toBe('This is a ');
      expect(resultArray[2]).toBe(' message');
      
      // Check the mark element
      const markElement = resultArray[1] as any;
      expect(markElement.type).toBe('mark');
      expect(markElement.props.children).toBe('test');
      expect(markElement.props.className).toBe('bg-yellow-200 text-yellow-900 px-0.5 rounded');
    });

    it('should handle case-insensitive search', () => {
      const result = highlightSearchTerm('TEST Message', 'test');
      const resultArray = result as React.ReactNode[];
      
      expect(resultArray).toHaveLength(3);
      expect(resultArray[0]).toBe('');
      expect(resultArray[2]).toBe(' Message');
      
      const markElement = resultArray[1] as any;
      expect(markElement.props.children).toBe('TEST');
    });

    it('should handle multiple matches', () => {
      const result = highlightSearchTerm('test this test again', 'test');
      const resultArray = result as React.ReactNode[];
      
      expect(resultArray).toHaveLength(5);
      expect(resultArray[0]).toBe('');
      expect(resultArray[2]).toBe(' this ');
      expect(resultArray[4]).toBe(' again');
      
      const firstMark = resultArray[1] as any;
      const secondMark = resultArray[3] as any;
      expect(firstMark.props.children).toBe('test');
      expect(secondMark.props.children).toBe('test');
    });

    it('should return original text when no matches', () => {
      const result = highlightSearchTerm('no matches here', 'xyz');
      
      // When there are no matches, the function returns an array with the original text
      expect(Array.isArray(result)).toBe(true);
      const resultArray = result as React.ReactNode[];
      expect(resultArray).toHaveLength(1);
      expect(resultArray[0]).toBe('no matches here');
    });

    it('should handle empty search term', () => {
      const result = highlightSearchTerm('some text', '');
      
      expect(result).toBe('some text');
    });

    it('should handle partial word matches', () => {
      const result = highlightSearchTerm('testing the application', 'test');
      const resultArray = result as React.ReactNode[];
      
      expect(resultArray).toHaveLength(3);
      expect(resultArray[0]).toBe('');
      expect(resultArray[2]).toBe('ing the application');
      
      const markElement = resultArray[1] as any;
      expect(markElement.props.children).toBe('test');
    });
  });

  describe('getSearchHighlightClasses', () => {
    it('should return highlight classes for highlighted items with search term', () => {
      const classes = getSearchHighlightClasses(true, true);
      expect(classes).toBe('ring-2 ring-yellow-400 ring-offset-1');
    });

    it('should return dim classes for non-highlighted items with search term', () => {
      const classes = getSearchHighlightClasses(false, true);
      expect(classes).toBe('opacity-30');
    });

    it('should return empty string when no search term', () => {
      const classes = getSearchHighlightClasses(true, false);
      expect(classes).toBe('');
    });
  });
});

describe('Calendar Event Search and Filtering', () => {
  const mockEvents = [
    {
      id: '1',
      title: 'Bug Fix Task',
      description: 'Fix critical bug in authentication',
      projectName: 'Frontend Project',
      start: new Date('2025-01-15T10:00:00Z'),
      end: new Date('2025-01-15T12:00:00Z'),
      isHighlighted: false,
      matchedFields: [],
    },
    {
      id: '2',
      title: 'Feature Development',
      description: 'Implement new user dashboard',
      projectName: 'Backend Project',
      start: new Date('2025-01-16T14:00:00Z'),
      end: new Date('2025-01-16T16:00:00Z'),
      isHighlighted: false,
      matchedFields: [],
    },
    {
      id: '3',
      title: 'Code Review',
      description: 'Review authentication changes',
      projectName: 'Frontend Project',
      start: new Date('2025-01-17T09:00:00Z'),
      end: new Date('2025-01-17T10:00:00Z'),
      isHighlighted: false,
      matchedFields: [],
    },
  ];

  // Mock the searchAndHighlightEvents function behavior
  const searchAndHighlightEvents = (events: any[], keyword: string) => {
    if (!keyword.trim()) {
      return events.map(event => ({ ...event, isHighlighted: false, matchedFields: [] }));
    }

    const searchTerm = keyword.toLowerCase().trim();
    
    return events.map(event => {
      const matchedFields: string[] = [];
      let isHighlighted = false;

      // Search in title
      if (event.title.toLowerCase().includes(searchTerm)) {
        matchedFields.push('title');
        isHighlighted = true;
      }

      // Search in description
      if (event.description && event.description.toLowerCase().includes(searchTerm)) {
        matchedFields.push('description');
        isHighlighted = true;
      }

      // Search in project name
      if (event.projectName && event.projectName.toLowerCase().includes(searchTerm)) {
        matchedFields.push('projectName');
        isHighlighted = true;
      }

      return {
        ...event,
        isHighlighted,
        matchedFields,
      };
    });
  };

  describe('searchAndHighlightEvents', () => {
    it('should highlight events matching search term in title', () => {
      const results = searchAndHighlightEvents(mockEvents, 'bug');
      
      expect(results[0].isHighlighted).toBe(true);
      expect(results[0].matchedFields).toContain('title');
      expect(results[1].isHighlighted).toBe(false);
      expect(results[2].isHighlighted).toBe(false);
    });

    it('should highlight events matching search term in description', () => {
      const results = searchAndHighlightEvents(mockEvents, 'authentication');
      
      expect(results[0].isHighlighted).toBe(true);
      expect(results[0].matchedFields).toContain('description');
      expect(results[2].isHighlighted).toBe(true);
      expect(results[2].matchedFields).toContain('description');
      expect(results[1].isHighlighted).toBe(false);
    });

    it('should highlight events matching search term in project name', () => {
      const results = searchAndHighlightEvents(mockEvents, 'frontend');
      
      expect(results[0].isHighlighted).toBe(true);
      expect(results[0].matchedFields).toContain('projectName');
      expect(results[2].isHighlighted).toBe(true);
      expect(results[2].matchedFields).toContain('projectName');
      expect(results[1].isHighlighted).toBe(false);
    });

    it('should handle case-insensitive search', () => {
      const results = searchAndHighlightEvents(mockEvents, 'BUG');
      
      expect(results[0].isHighlighted).toBe(true);
      expect(results[0].matchedFields).toContain('title');
    });

    it('should return all events unhighlighted when search term is empty', () => {
      const results = searchAndHighlightEvents(mockEvents, '');
      
      results.forEach(result => {
        expect(result.isHighlighted).toBe(false);
        expect(result.matchedFields).toEqual([]);
      });
    });

    it('should highlight events with multiple matching fields', () => {
      const results = searchAndHighlightEvents(mockEvents, 'fix');
      
      expect(results[0].isHighlighted).toBe(true);
      expect(results[0].matchedFields).toContain('title');
      expect(results[0].matchedFields).toContain('description');
    });

    it('should handle partial word matches', () => {
      const results = searchAndHighlightEvents(mockEvents, 'dev');
      
      expect(results[1].isHighlighted).toBe(true);
      expect(results[1].matchedFields).toContain('title');
    });

    it('should handle search terms not found in any event', () => {
      const results = searchAndHighlightEvents(mockEvents, 'nonexistent');
      
      results.forEach(result => {
        expect(result.isHighlighted).toBe(false);
        expect(result.matchedFields).toEqual([]);
      });
    });
  });

  describe('Due Date Filtering', () => {
    const mockTasksWithMixedDueDates = [
      {
        id: 1,
        title: 'Task with Due Date',
        dueDateTime: '2025-01-15T10:00:00Z',
        projectId: 1,
      },
      {
        id: 2,
        title: 'Task without Due Date',
        // No dueDateTime
        projectId: 1,
      },
      {
        id: 3,
        title: 'Another Task with Due Date',
        dueDateTime: '2025-01-20T14:00:00Z',
        projectId: 2,
      },
    ];

    it('should filter out tasks without due dates', () => {
      const tasksWithDueDates = mockTasksWithMixedDueDates.filter(task => task.dueDateTime);
      
      expect(tasksWithDueDates).toHaveLength(2);
      expect(tasksWithDueDates[0].title).toBe('Task with Due Date');
      expect(tasksWithDueDates[1].title).toBe('Another Task with Due Date');
    });

    it('should keep all tasks when all have due dates', () => {
      const allTasksWithDueDates = mockTasksWithMixedDueDates
        .map(task => ({ ...task, dueDateTime: '2025-01-15T10:00:00Z' }));
      
      const filtered = allTasksWithDueDates.filter(task => task.dueDateTime);
      
      expect(filtered).toHaveLength(3);
    });

    it('should return empty array when no tasks have due dates', () => {
      const tasksWithoutDueDates = mockTasksWithMixedDueDates
        .map(task => ({ ...task, dueDateTime: undefined }));
      
      const filtered = tasksWithoutDueDates.filter(task => task.dueDateTime);
      
      expect(filtered).toHaveLength(0);
    });
  });
});
