import React from 'react';
import { render, screen } from '@testing-library/react';
import { FileIconComponent, FileList } from '../../components/file-icon';
import { FileResponse } from '../../services/file-service';

// Mock the file service
jest.mock('../../services/file-service', () => ({
  FileService: {
    getFileType: jest.fn((filename: string) => {
      if (filename.endsWith('.pdf') || filename.endsWith('.docx')) return 'document';
      if (filename.endsWith('.png') || filename.endsWith('.jpg')) return 'image';
      if (filename.endsWith('.zip')) return 'archive';
      return 'unknown';
    }),
    getFileExtension: jest.fn((filename: string) => {
      const ext = filename.split('.').pop()?.toLowerCase();
      return ext || '';
    }),
  },
}));

// Mock window.open for download functionality
const mockWindowOpen = jest.fn();
Object.defineProperty(window, 'open', {
  value: mockWindowOpen,
  writable: true,
});

describe('FileIconComponent', () => {
  const mockFileResponse: FileResponse = {
    id: 1,
    taskId: 1,
    projectId: 1,
    fileUrl: 'https://example.com/files/document.pdf',
    createdAt: '2023-01-01T00:00:00Z',
    createdBy: 1,
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders the correct icon for a PDF file', () => {
    const pdfFile = { ...mockFileResponse, fileUrl: 'https://example.com/files/document.pdf' };
    render(<FileIconComponent file={pdfFile} />);
    
    // Should render a document icon (FileText from lucide-react)
    const container = screen.getByTitle(/document\.pdf/i);
    expect(container).toBeInTheDocument();
    expect(container).toHaveAttribute('title', expect.stringContaining('PDF'));
  });

  it('renders the correct icon for a DOCX file', () => {
    const docxFile = { ...mockFileResponse, fileUrl: 'https://example.com/files/report.docx' };
    render(<FileIconComponent file={docxFile} />);
    
    const container = screen.getByTitle(/report\.docx/i);
    expect(container).toBeInTheDocument();
    expect(container).toHaveAttribute('title', expect.stringContaining('DOCX'));
  });

  it('renders the correct icon for an image file', () => {
    const imageFile = { ...mockFileResponse, fileUrl: 'https://example.com/files/image.png' };
    render(<FileIconComponent file={imageFile} />);
    
    const image = screen.getByAltText('image.png');
    expect(image).toBeInTheDocument();
    expect(image).toHaveAttribute('src', 'https://example.com/files/image.png');
  });

  it('renders download icon when showDownload is true', () => {
    render(<FileIconComponent file={mockFileResponse} showDownload={true} />);
    
    // The download icon should be in the DOM but initially hidden (opacity-0)
    const container = screen.getByTitle(/document\.pdf/i);
    expect(container).toBeInTheDocument();
  });

  it('handles click to download file', () => {
    render(<FileIconComponent file={mockFileResponse} />);
    
    const container = screen.getByTitle(/document\.pdf/i);
    container.click();
    
    expect(mockWindowOpen).toHaveBeenCalledWith('https://example.com/files/document.pdf', '_blank');
  });

  it('applies different sizes correctly', () => {
    const { rerender } = render(<FileIconComponent file={mockFileResponse} size="sm" />);
    let container = screen.getByTitle(/document\.pdf/i);
    expect(container).toHaveClass('h-6 w-6'); // containerSizeClasses for sm
    
    rerender(<FileIconComponent file={mockFileResponse} size="lg" />);
    container = screen.getByTitle(/document\.pdf/i);
    expect(container).toHaveClass('h-10 w-10'); // containerSizeClasses for lg
  });
});

describe('FileList', () => {
  const mockFiles: FileResponse[] = [
    {
      id: 1,
      taskId: 1,
      projectId: 1,
      fileUrl: 'https://example.com/files/doc1.pdf',
      createdAt: '2023-01-01T00:00:00Z',
      createdBy: 1,
    },
    {
      id: 2,
      taskId: 1,
      projectId: 1,
      fileUrl: 'https://example.com/files/doc2.docx',
      createdAt: '2023-01-01T00:00:00Z',
      createdBy: 1,
    },
  ];

  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders all files when count is within maxDisplay', () => {
    render(<FileList files={mockFiles} maxDisplay={5} />);
    
    expect(screen.getByTitle(/doc1\.pdf/i)).toBeInTheDocument();
    expect(screen.getByTitle(/doc2\.docx/i)).toBeInTheDocument();
  });

  it('shows remaining count when files exceed maxDisplay', () => {
    const manyFiles = Array(7).fill(null).map((_, i) => ({
      ...mockFiles[0],
      id: i + 1,
      fileUrl: `https://example.com/files/doc${i + 1}.pdf`,
    }));
    
    render(<FileList files={manyFiles} maxDisplay={3} />);
    
    // Should show "+4" for remaining files
    expect(screen.getByText('+4')).toBeInTheDocument();
    expect(screen.getByTitle('4 more files')).toBeInTheDocument();
  });

  it('returns null when no files provided', () => {
    const { container } = render(<FileList files={[]} />);
    expect(container.firstChild).toBeNull();
  });
});
