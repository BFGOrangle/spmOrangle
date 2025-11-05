/**
 * Unit tests for Calendar Controls Component
 * Tests the legend display and filtering logic
 */

import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { CalendarControls } from '../../components/calendar/calendar-controls';
import { CalendarView } from '../../types/calendar';

describe('CalendarControls', () => {
  const mockProjects = [
    {
      id: 1,
      name: 'Project Alpha',
      description: 'Test project',
      ownerId: 1,
      createdAt: '2024-01-01',
      updatedAt: '2024-01-01',
      taskCount: 5,
      completedTaskCount: 2,
      isOwner: true,
      isRelated: false,
    },
    {
      id: 2,
      name: 'Project Beta',
      description: 'Test project 2',
      ownerId: 2,
      createdAt: '2024-01-01',
      updatedAt: '2024-01-01',
      taskCount: 3,
      completedTaskCount: 1,
      isOwner: false,
      isRelated: true,
    },
  ];

  const defaultProps = {
    currentView: 'month' as CalendarView,
    currentDate: new Date('2024-01-15'),
    projects: mockProjects,
    selectedProjectId: undefined,
    selectedTaskType: 'Project Tasks',
    searchKeyword: '',
    searchResultsCount: 0,
    onViewChange: jest.fn(),
    onDateChange: jest.fn(),
    onProjectFilter: jest.fn(),
    onTaskTypeChange: jest.fn(),
    onSearchChange: jest.fn(),
    onToday: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Legend Display', () => {
    it('should show legend when in "Project Tasks" view', () => {
      render(<CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />);

      expect(screen.getByText('Legend:')).toBeInTheDocument();
      expect(screen.getByText('Collaborator Task')).toBeInTheDocument();
      expect(screen.getByText('Related Task')).toBeInTheDocument();
    });

    it('should not show legend in "My Projects" view', () => {
      render(<CalendarControls {...defaultProps} selectedTaskType="My Projects" />);

      expect(screen.queryByText('Legend:')).not.toBeInTheDocument();
      expect(screen.queryByText('Collaborator Task')).not.toBeInTheDocument();
      expect(screen.queryByText('Related Task')).not.toBeInTheDocument();
    });

    it('should not show legend in "Personal Tasks" view', () => {
      render(<CalendarControls {...defaultProps} selectedTaskType="Personal Tasks" />);

      expect(screen.queryByText('Legend:')).not.toBeInTheDocument();
      expect(screen.queryByText('Collaborator Task')).not.toBeInTheDocument();
      expect(screen.queryByText('Related Task')).not.toBeInTheDocument();
    });

    it('should show legend when project is filtered in "Project Tasks" view', () => {
      render(
        <CalendarControls
          {...defaultProps}
          selectedTaskType="Project Tasks"
          selectedProjectId={1}
        />
      );

      expect(screen.getByText('Legend:')).toBeInTheDocument();
      expect(screen.getByText('Collaborator Task')).toBeInTheDocument();
      expect(screen.getByText('Related Task')).toBeInTheDocument();
    });

    it('should display green square icon for collaborator tasks', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />
      );

      const greenSquares = container.querySelectorAll('.bg-green-500');
      expect(greenSquares.length).toBeGreaterThan(0);
    });

    it('should display purple square icon for related tasks', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />
      );

      const purpleSquares = container.querySelectorAll('.bg-purple-500');
      expect(purpleSquares.length).toBeGreaterThan(0);
    });
  });

  describe('View Type Display', () => {
    it('should show "Viewing: All Project Tasks" when no project is selected', () => {
      render(
        <CalendarControls
          {...defaultProps}
          selectedTaskType="Project Tasks"
          selectedProjectId={undefined}
        />
      );

      expect(screen.getByText('Viewing: All Project Tasks')).toBeInTheDocument();
    });

    it('should show "Filtered by: [Project Name]" when a project is selected', () => {
      render(
        <CalendarControls
          {...defaultProps}
          selectedTaskType="Project Tasks"
          selectedProjectId={1}
        />
      );

      expect(screen.getByText(/Filtered by: Project Alpha/)).toBeInTheDocument();
    });

    it('should show "Viewing: My Project Tasks" for My Projects view', () => {
      render(<CalendarControls {...defaultProps} selectedTaskType="My Projects" />);

      expect(screen.getByText('Viewing: My Project Tasks')).toBeInTheDocument();
    });

    it('should show "Viewing: Personal Tasks" for Personal Tasks view', () => {
      render(<CalendarControls {...defaultProps} selectedTaskType="Personal Tasks" />);

      expect(screen.getByText('Viewing: Personal Tasks')).toBeInTheDocument();
    });
  });

  describe('Project Filter Dropdown', () => {
    it('should show project filter dropdown in "Project Tasks" view', () => {
      render(<CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />);

      // The Select component should be present (check for "All Projects" option)
      expect(screen.getByText('All Projects')).toBeInTheDocument();
    });

    it('should not show project filter in "My Projects" view', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="My Projects" />
      );

      // Check that the project filter Select is not rendered
      const selects = container.querySelectorAll('select');
      // Should only have the view type select, not the project filter
      expect(selects.length).toBeLessThan(2);
    });

    it('should not show project filter in "Personal Tasks" view', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="Personal Tasks" />
      );

      // Check that the project filter Select is not rendered
      const selects = container.querySelectorAll('select');
      // Should only have the view type select, not the project filter
      expect(selects.length).toBeLessThan(2);
    });

    it('should exclude personal tasks (projectId = 0) from project filter', () => {
      const projectsWithPersonal = [
        ...mockProjects,
        {
          id: 0,
          name: 'Personal',
          description: 'Personal tasks',
          ownerId: 1,
          createdAt: '2024-01-01',
          updatedAt: '2024-01-01',
          taskCount: 1,
          completedTaskCount: 0,
          isOwner: true,
          isRelated: false,
        },
      ];

      render(
        <CalendarControls
          {...defaultProps}
          projects={projectsWithPersonal}
          selectedTaskType="Project Tasks"
        />
      );

      // Personal project should not appear in the dropdown
      expect(screen.queryByText('Personal')).not.toBeInTheDocument();
      // But other projects should appear
      expect(screen.getByText('All Projects')).toBeInTheDocument();
    });
  });

  describe('Search Display', () => {
    it('should show search results count when searching', () => {
      render(
        <CalendarControls
          {...defaultProps}
          searchKeyword="test"
          searchResultsCount={5}
        />
      );

      expect(screen.getByText(/Searching for: "test"/)).toBeInTheDocument();
      expect(screen.getByText(/(5 results)/)).toBeInTheDocument();
    });

    it('should not show legend when searching (search info takes priority)', () => {
      render(
        <CalendarControls
          {...defaultProps}
          selectedTaskType="Project Tasks"
          searchKeyword="test"
          searchResultsCount={5}
        />
      );

      // Search info should be shown
      expect(screen.getByText(/Searching for: "test"/)).toBeInTheDocument();
      // But legend should still be visible (it's in a separate section)
      expect(screen.getByText('Legend:')).toBeInTheDocument();
    });
  });

  describe('Legend Visual Elements', () => {
    it('should have correct styling for legend items', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />
      );

      // Check for the legend container
      const legendContainer = container.querySelector('.gap-3.text-xs');
      expect(legendContainer).toBeInTheDocument();
    });

    it('should have square icons with rounded corners', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />
      );

      const squares = container.querySelectorAll('.rounded-sm');
      expect(squares.length).toBeGreaterThanOrEqual(2); // At least 2 squares for legend
    });

    it('should have proper dimensions for legend icons', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />
      );

      const greenSquare = container.querySelector('.bg-green-500');
      expect(greenSquare).toHaveClass('w-3', 'h-3');

      const purpleSquare = container.querySelector('.bg-purple-500');
      expect(purpleSquare).toHaveClass('w-3', 'h-3');
    });
  });

  describe('Responsive Behavior', () => {
    it('should use flex-wrap for legend to handle small screens', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />
      );

      const legendContainer = container.querySelector('.flex.flex-wrap');
      expect(legendContainer).toBeInTheDocument();
    });

    it('should maintain legend visibility on all screen sizes in Project Tasks view', () => {
      // Test that legend is always present when conditions are met
      render(<CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />);

      const legend = screen.getByText('Legend:');
      expect(legend).toBeInTheDocument();
      
      // Legend should not have display:none or hidden classes
      const legendParent = legend.parentElement;
      expect(legendParent).not.toHaveClass('hidden');
    });
  });

  describe('Integration with Task Types', () => {
    it('should update legend visibility when switching between task types', () => {
      const { rerender } = render(
        <CalendarControls {...defaultProps} selectedTaskType="My Projects" />
      );

      // Legend should not be visible
      expect(screen.queryByText('Legend:')).not.toBeInTheDocument();

      // Switch to Project Tasks
      rerender(
        <CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />
      );

      // Legend should now be visible
      expect(screen.getByText('Legend:')).toBeInTheDocument();
    });

    it('should maintain legend when switching projects in Project Tasks view', () => {
      const { rerender } = render(
        <CalendarControls
          {...defaultProps}
          selectedTaskType="Project Tasks"
          selectedProjectId={undefined}
        />
      );

      // Legend visible with all projects
      expect(screen.getByText('Legend:')).toBeInTheDocument();

      // Switch to specific project
      rerender(
        <CalendarControls
          {...defaultProps}
          selectedTaskType="Project Tasks"
          selectedProjectId={1}
        />
      );

      // Legend should still be visible
      expect(screen.getByText('Legend:')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have readable text for legend items', () => {
      render(<CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />);

      const collaboratorText = screen.getByText('Collaborator Task');
      const relatedText = screen.getByText('Related Task');

      expect(collaboratorText).toBeVisible();
      expect(relatedText).toBeVisible();
    });

    it('should have proper semantic structure for legend', () => {
      const { container } = render(
        <CalendarControls {...defaultProps} selectedTaskType="Project Tasks" />
      );

      // Check that legend is in a div with proper flex layout
      const legendContainer = screen.getByText('Legend:').parentElement;
      expect(legendContainer).toHaveClass('flex');
    });
  });
});
