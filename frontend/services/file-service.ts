/**
 * File management service for handling file uploads and retrieval
 */

import { AuthenticatedApiClient } from "./authenticated-api-client";

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

export class FileService extends AuthenticatedApiClient {
  /**
   * Get files for a specific task and project
   */
  async getFilesByTaskAndProject(taskId: number, projectId: number): Promise<FileResponse[]> {
    console.log(`[FileService] Fetching files for taskId: ${taskId}, projectId: ${projectId}`);
    try {
      const result = await this.request<FileResponse[]>(`/api/files/project/${projectId}/task/${taskId}`);
      console.log(`[FileService] Successfully fetched ${result.length} files`);
      return result;
    } catch (error) {
      console.error(`[FileService] Error fetching files for taskId ${taskId}, projectId ${projectId}:`, error);
      throw error;
    }
  }

  /**
   * Get all files for a project
   */
  async getFilesByProject(projectId: number): Promise<FileResponse[]> {
    console.log(`[FileService] Fetching files for projectId: ${projectId}`);
    try {
      const result = await this.request<FileResponse[]>(`/api/files/project/${projectId}`);
      console.log(`[FileService] Successfully fetched ${result.length} files for project`);
      return result;
    } catch (error) {
      console.error(`[FileService] Error fetching files for projectId ${projectId}:`, error);
      throw error;
    }
  }

  /**
   * Upload a file for a task - bypasses the authenticated client for multipart uploads
   */
  async uploadFile({ file, taskId, projectId, bucket }: UploadFileRequest): Promise<FileResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('taskId', String(taskId));
    formData.append('projectId', String(projectId));
    if (bucket) {
      formData.append('bucket', bucket);
    }

    const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080";
    console.log(`[FileService] Uploading file to: ${baseUrl}/api/files/upload`);

    const response = await fetch(`${baseUrl}/api/files/upload`, {
      method: 'POST',
      body: formData,
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error(`[FileService] Upload failed:`, errorText);
      throw new Error(`Upload failed: ${response.status} ${response.statusText} - ${errorText}`);
    }

    const result = await response.json();
    console.log(`[FileService] File uploaded successfully:`, result);
    return result;
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

// Export singleton instance
export const fileService = new FileService();
