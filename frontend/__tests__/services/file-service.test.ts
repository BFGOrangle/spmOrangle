import { FileService, fileService, FileResponse, UploadFileRequest } from '../../services/file-service';
import { AuthenticatedApiClient } from '../../services/authenticated-api-client';

// Mock the AuthenticatedApiClient
jest.mock('../../services/authenticated-api-client');

const MockedAuthenticatedApiClient = AuthenticatedApiClient as jest.MockedClass<typeof AuthenticatedApiClient>;

describe('FileService', () => {
  let mockAuthenticatedClient: jest.Mocked<Pick<AuthenticatedApiClient, 'get' | 'post' | 'put' | 'patch' | 'delete' | 'postMultipart'>>;
  let service: FileService;

  beforeEach(() => {
    jest.clearAllMocks();
    mockAuthenticatedClient = {
      get: jest.fn(),
      post: jest.fn(),
      put: jest.fn(),
      patch: jest.fn(),
      delete: jest.fn(),
      postMultipart: jest.fn(),
    };

    MockedAuthenticatedApiClient.mockImplementation(() => mockAuthenticatedClient as any);
    service = new FileService();
  });

  describe('getFilesByTaskAndProject', () => {
    it('should fetch files for a specific task and project', async () => {
      const mockFiles: FileResponse[] = [
        {
          id: 1,
          taskId: 123,
          projectId: 456,
          fileUrl: 'https://example.com/file1.pdf',
          createdAt: '2023-01-01T00:00:00Z',
          createdBy: 1,
        },
        {
          id: 2,
          taskId: 123,
          projectId: 456,
          fileUrl: 'https://example.com/file2.docx',
          createdAt: '2023-01-02T00:00:00Z',
          createdBy: 2,
        },
      ];

      mockAuthenticatedClient.get.mockResolvedValue(mockFiles);

      const result = await service.getFilesByTaskAndProject(123, 456);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/files/project/456/task/123');
      expect(result).toEqual(mockFiles);
    });

    it('should handle API errors gracefully', async () => {
      const error = new Error('API Error');
      mockAuthenticatedClient.get.mockRejectedValue(error);

      await expect(service.getFilesByTaskAndProject(123, 456)).rejects.toThrow('API Error');
      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/files/project/456/task/123');
    });
  });

  describe('getFilesByProject', () => {
    it('should fetch all files for a project', async () => {
      const mockFiles: FileResponse[] = [
        {
          id: 1,
          taskId: 123,
          projectId: 456,
          fileUrl: 'https://example.com/file1.pdf',
          createdAt: '2023-01-01T00:00:00Z',
          createdBy: 1,
        },
        {
          id: 2,
          taskId: 124,
          projectId: 456,
          fileUrl: 'https://example.com/file2.png',
          createdAt: '2023-01-02T00:00:00Z',
          createdBy: 2,
        },
      ];

      mockAuthenticatedClient.get.mockResolvedValue(mockFiles);

      const result = await service.getFilesByProject(456);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/files/project/456');
      expect(result).toEqual(mockFiles);
    });

    it('should return empty array when no files exist', async () => {
      mockAuthenticatedClient.get.mockResolvedValue([]);

      const result = await service.getFilesByProject(456);

      expect(mockAuthenticatedClient.get).toHaveBeenCalledWith('/api/files/project/456');
      expect(result).toEqual([]);
    });
  });

  describe('uploadFile', () => {
    it('should upload a file with required parameters', async () => {
      const mockFile = new File(['test content'], 'test.pdf', { type: 'application/pdf' });
      const uploadRequest: UploadFileRequest = {
        file: mockFile,
        taskId: 123,
        projectId: 456,
      };

      const mockResponse: FileResponse = {
        id: 1,
        taskId: 123,
        projectId: 456,
        fileUrl: 'https://example.com/uploaded-file.pdf',
        createdAt: '2023-01-01T00:00:00Z',
        createdBy: 1,
      };

      mockAuthenticatedClient.postMultipart.mockResolvedValue(mockResponse);

      const result = await service.uploadFile(uploadRequest);

      expect(mockAuthenticatedClient.postMultipart).toHaveBeenCalledWith(
        '/api/files/upload',
        expect.any(FormData)
      );

      // Verify FormData contents
      const formDataCall = mockAuthenticatedClient.postMultipart.mock.calls[0][1] as FormData;
      expect(formDataCall.get('file')).toBe(mockFile);
      expect(formDataCall.get('taskId')).toBe('123');
      expect(formDataCall.get('projectId')).toBe('456');
      expect(formDataCall.get('bucket')).toBeNull();

      expect(result).toEqual(mockResponse);
    });

    it('should upload a file with optional bucket parameter', async () => {
      const mockFile = new File(['test content'], 'test.pdf', { type: 'application/pdf' });
      const uploadRequest: UploadFileRequest = {
        file: mockFile,
        taskId: 123,
        projectId: 456,
        bucket: 'custom-bucket',
      };

      const mockResponse: FileResponse = {
        id: 1,
        taskId: 123,
        projectId: 456,
        fileUrl: 'https://example.com/uploaded-file.pdf',
        createdAt: '2023-01-01T00:00:00Z',
        createdBy: 1,
      };

      mockAuthenticatedClient.postMultipart.mockResolvedValue(mockResponse);

      const result = await service.uploadFile(uploadRequest);

      expect(mockAuthenticatedClient.postMultipart).toHaveBeenCalledWith(
        '/api/files/upload',
        expect.any(FormData)
      );

      // Verify FormData contents including bucket
      const formDataCall = mockAuthenticatedClient.postMultipart.mock.calls[0][1] as FormData;
      expect(formDataCall.get('file')).toBe(mockFile);
      expect(formDataCall.get('taskId')).toBe('123');
      expect(formDataCall.get('projectId')).toBe('456');
      expect(formDataCall.get('bucket')).toBe('custom-bucket');

      expect(result).toEqual(mockResponse);
    });

    it('should handle upload failures', async () => {
      const mockFile = new File(['test content'], 'test.pdf', { type: 'application/pdf' });
      const uploadRequest: UploadFileRequest = {
        file: mockFile,
        taskId: 123,
        projectId: 456,
      };

      const error = new Error('Upload failed');
      mockAuthenticatedClient.postMultipart.mockRejectedValue(error);

      await expect(service.uploadFile(uploadRequest)).rejects.toThrow('Upload failed');
    });
  });

  describe('getFileExtension', () => {
    it('should extract file extension from filename', () => {
      expect(FileService.getFileExtension('document.pdf')).toBe('pdf');
      expect(FileService.getFileExtension('image.PNG')).toBe('png');
      expect(FileService.getFileExtension('archive.tar.gz')).toBe('gz');
      expect(FileService.getFileExtension('README.md')).toBe('md');
    });

    it('should extract extension from URL path', () => {
      expect(FileService.getFileExtension('https://example.com/files/document.pdf')).toBe('pdf');
      expect(FileService.getFileExtension('/path/to/file.docx')).toBe('docx');
    });

    it('should return empty string for files without extension', () => {
      expect(FileService.getFileExtension('README')).toBe('');
      expect(FileService.getFileExtension('file-without-extension')).toBe('');
      expect(FileService.getFileExtension('')).toBe('');
    });

    it('should handle files with dots in name but no extension', () => {
      expect(FileService.getFileExtension('file.name.without.extension')).toBe('extension');
      expect(FileService.getFileExtension('file.')).toBe('');
    });
  });

  describe('getFileType', () => {
    describe('image files', () => {
      it('should identify image file types', () => {
        expect(FileService.getFileType('photo.jpg')).toBe('image');
        expect(FileService.getFileType('image.JPEG')).toBe('image');
        expect(FileService.getFileType('icon.png')).toBe('image');
        expect(FileService.getFileType('animation.gif')).toBe('image');
        expect(FileService.getFileType('bitmap.bmp')).toBe('image');
        expect(FileService.getFileType('vector.svg')).toBe('image');
        expect(FileService.getFileType('modern.webp')).toBe('image');
      });
    });

    describe('document files', () => {
      it('should identify document file types', () => {
        expect(FileService.getFileType('report.pdf')).toBe('document');
        expect(FileService.getFileType('letter.doc')).toBe('document');
        expect(FileService.getFileType('resume.DOCX')).toBe('document');
        expect(FileService.getFileType('notes.txt')).toBe('document');
        expect(FileService.getFileType('formatted.rtf')).toBe('document');
        expect(FileService.getFileType('open-doc.odt')).toBe('document');
      });
    });

    describe('spreadsheet files', () => {
      it('should identify spreadsheet file types', () => {
        expect(FileService.getFileType('budget.xls')).toBe('spreadsheet');
        expect(FileService.getFileType('data.XLSX')).toBe('spreadsheet');
        expect(FileService.getFileType('export.csv')).toBe('spreadsheet');
        expect(FileService.getFileType('calc.ods')).toBe('spreadsheet');
      });
    });

    describe('archive files', () => {
      it('should identify archive file types', () => {
        expect(FileService.getFileType('backup.zip')).toBe('archive');
        expect(FileService.getFileType('files.RAR')).toBe('archive');
        expect(FileService.getFileType('compressed.7z')).toBe('archive');
        expect(FileService.getFileType('archive.tar')).toBe('archive');
        expect(FileService.getFileType('gzipped.gz')).toBe('archive');
      });
    });

    describe('video files', () => {
      it('should identify video file types', () => {
        expect(FileService.getFileType('movie.mp4')).toBe('video');
        expect(FileService.getFileType('video.AVI')).toBe('video');
        expect(FileService.getFileType('clip.mov')).toBe('video');
        expect(FileService.getFileType('windows.wmv')).toBe('video');
        expect(FileService.getFileType('flash.flv')).toBe('video');
        expect(FileService.getFileType('web.webm')).toBe('video');
      });
    });

    describe('audio files', () => {
      it('should identify audio file types', () => {
        expect(FileService.getFileType('song.mp3')).toBe('audio');
        expect(FileService.getFileType('sound.WAV')).toBe('audio');
        expect(FileService.getFileType('audio.aac')).toBe('audio');
        expect(FileService.getFileType('music.ogg')).toBe('audio');
        expect(FileService.getFileType('windows.wma')).toBe('audio');
      });
    });

    describe('other files', () => {
      it('should return "other" for unknown file types', () => {
        expect(FileService.getFileType('program.exe')).toBe('other');
        expect(FileService.getFileType('data.bin')).toBe('other');
        expect(FileService.getFileType('config.ini')).toBe('other');
        expect(FileService.getFileType('unknown.xyz')).toBe('other');
        expect(FileService.getFileType('no-extension')).toBe('other');
        expect(FileService.getFileType('')).toBe('other');
      });
    });

    it('should handle case insensitive extensions', () => {
      expect(FileService.getFileType('DOCUMENT.PDF')).toBe('document');
      expect(FileService.getFileType('Image.JPG')).toBe('image');
      expect(FileService.getFileType('DATA.xlsx')).toBe('spreadsheet');
    });
  });

  describe('fileService singleton', () => {
    it('should export a singleton instance', () => {
      expect(fileService).toBeInstanceOf(FileService);
      expect(fileService).toBe(fileService); // Same reference
    });
  });
});
