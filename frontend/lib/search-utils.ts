/**
 * Search and highlighting utilities for calendar events
 */

import React from 'react';

/**
 * Highlights search terms in text
 */
export const highlightSearchTerm = (text: string, searchTerm: string): React.ReactNode => {
  if (!searchTerm.trim() || !text) {
    return text;
  }

  const regex = new RegExp(`(${searchTerm.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi');
  const parts = text.split(regex);

  return parts.map((part, index) => {
    if (regex.test(part)) {
      return React.createElement('mark', {
        key: index,
        className: 'bg-yellow-200 text-yellow-900 px-0.5 rounded'
      }, part);
    }
    return part;
  });
};

/**
 * Gets CSS classes for search highlighting
 */
export const getSearchHighlightClasses = (isHighlighted: boolean, hasSearchTerm: boolean): string => {
  if (!hasSearchTerm) {
    return '';
  }
  
  if (isHighlighted) {
    return 'ring-2 ring-yellow-400 ring-offset-1';
  }
  
  return 'opacity-30';
};

/**
 * Checks if an event matches search criteria
 */
export const matchesSearchTerm = (event: any, searchTerm: string): boolean => {
  if (!searchTerm.trim()) {
    return true;
  }

  const searchLower = searchTerm.toLowerCase();
  
  return (
    event.title?.toLowerCase().includes(searchLower) ||
    event.description?.toLowerCase().includes(searchLower) ||
    event.projectName?.toLowerCase().includes(searchLower) ||
    event.taskType?.toLowerCase().includes(searchLower) ||
    event.status?.toLowerCase().includes(searchLower) ||
    event.tags?.some((tag: string) => tag.toLowerCase().includes(searchLower))
  );
};
