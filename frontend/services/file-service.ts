
import { AuthenticatedApiClient } from './authenticated-api-client';

export interface FileResponse {
  id: number;
  taskId: number;
  projectId: number;
  fileUrl: string;
  createdAt: string;
  createdBy: number;
}

export interface UploadFileRequest {
  file: File;
  taskId: number;
  projectId: number;
  bucket?: string;
}

export class FileService {
  private authenticatedClient: AuthenticatedApiClient;

  constructor() {
    this.authenticatedClient = new AuthenticatedApiClient();
  }

  /**
   * Get files for a specific task and project
   */
  async getFilesByTaskAndProject(taskId: number, projectId: number): Promise<FileResponse[]> {
    return this.authenticatedClient.get(`/api/files/project/${projectId}/task/${taskId}`);
  }

  /**
   * Get all files for a project
   */
  async getFilesByProject(projectId: number): Promise<FileResponse[]> {
    return this.authenticatedClient.get(`/api/files/project/${projectId}`);
  }

  /**
   * Upload a file for a task
   */
  async uploadFile({ file, taskId, projectId, bucket }: UploadFileRequest): Promise<FileResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('taskId', String(taskId));
    formData.append('projectId', String(projectId));
    if (bucket) {
      formData.append('bucket', bucket);
    }
    return this.authenticatedClient.postMultipart('/api/files/upload', formData);
  }

  /**
   * Get file extension from filename or URL
   */
  static getFileExtension(filename: string): string {
    const parts = filename.split('.');
    return parts.length > 1 ? parts[parts.length - 1].toLowerCase() : '';
  }

  /**
   * Get file type category from extension
   */
  static getFileType(filename: string): 'image' | 'document' | 'spreadsheet' | 'archive' | 'video' | 'audio' | 'other' {
    const extension = this.getFileExtension(filename);

    const imageTypes = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp'];
    const documentTypes = ['pdf', 'doc', 'docx', 'txt', 'rtf', 'odt'];
    const spreadsheetTypes = ['xls', 'xlsx', 'csv', 'ods'];
    const archiveTypes = ['zip', 'rar', '7z', 'tar', 'gz'];
    const videoTypes = ['mp4', 'avi', 'mov', 'wmv', 'flv', 'webm'];
    const audioTypes = ['mp3', 'wav', 'aac', 'ogg', 'wma'];

    if (imageTypes.includes(extension)) return 'image';
    if (documentTypes.includes(extension)) return 'document';
    if (spreadsheetTypes.includes(extension)) return 'spreadsheet';
    if (archiveTypes.includes(extension)) return 'archive';
    if (videoTypes.includes(extension)) return 'video';
    if (audioTypes.includes(extension)) return 'audio';

    return 'other';
  }
}

export const fileService = new FileService();
