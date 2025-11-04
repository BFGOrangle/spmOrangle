import { Page, Download } from '@playwright/test';
import * as fs from 'fs';
import * as path from 'path';

/**
 * Download helper utilities for E2E tests
 * Handles file download verification and validation
 */

/**
 * Wait for a file download and return the download object
 */
export async function waitForDownload(
  page: Page,
  action: () => Promise<void>,
  timeout: number = 30000
): Promise<Download> {
  const downloadPromise = page.waitForEvent('download', { timeout });
  await action();
  const download = await downloadPromise;
  return download;
}

/**
 * Wait for download with filename pattern matching
 */
export async function waitForDownloadWithFilename(
  page: Page,
  action: () => Promise<void>,
  filenamePattern: string | RegExp,
  timeout: number = 30000
): Promise<Download> {
  const download = await waitForDownload(page, action, timeout);
  const filename = download.suggestedFilename();
  
  const matches = typeof filenamePattern === 'string'
    ? filename.includes(filenamePattern)
    : filenamePattern.test(filename);
  
  if (!matches) {
    throw new Error(
      `Downloaded filename "${filename}" does not match pattern "${filenamePattern}"`
    );
  }
  
  return download;
}

/**
 * Verify downloaded file exists and has correct format
 */
export async function verifyDownloadedFile(
  download: Download,
  expectedFormat: 'pdf' | 'csv' | 'json'
): Promise<string> {
  const filename = download.suggestedFilename();
  const expectedExtension = `.${expectedFormat}`;
  
  if (!filename.toLowerCase().endsWith(expectedExtension)) {
    throw new Error(
      `Expected file extension ${expectedExtension}, but got ${filename}`
    );
  }
  
  // Save the download to a temporary location
  const downloadPath = await download.path();
  
  if (!downloadPath) {
    throw new Error('Download path is null - file may not have been downloaded');
  }
  
  // Verify file exists
  if (!fs.existsSync(downloadPath)) {
    throw new Error(`Downloaded file does not exist at path: ${downloadPath}`);
  }
  
  // Verify file is not empty
  const stats = fs.statSync(downloadPath);
  if (stats.size === 0) {
    throw new Error(`Downloaded file is empty: ${filename}`);
  }
  
  return downloadPath;
}

/**
 * Read CSV file content
 */
export async function readCsvContent(filePath: string): Promise<string[][]> {
  const content = fs.readFileSync(filePath, 'utf-8');
  const lines = content.split('\n').filter(line => line.trim() !== '');

  return lines.map(line => {
    // Simple CSV parsing (doesn't handle quotes with commas)
    return line.split(',').map(cell => cell.trim());
  });
}

/**
 * Parse task summary data from CSV report
 * Searches for task summary section and extracts totals
 */
export async function parseCsvTaskSummary(filePath: string): Promise<{
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  todoTasks: number;
  blockedTasks: number;
}> {
  const rows = await readCsvContent(filePath);

  // Initialize values
  let totalTasks = 0;
  let completedTasks = 0;
  let inProgressTasks = 0;
  let todoTasks = 0;
  let blockedTasks = 0;

  // Search for task summary values in CSV
  // CSV format varies, but typically has "Total Tasks", "Completed", etc. as labels
  for (let i = 0; i < rows.length; i++) {
    const row = rows[i];

    // Check each cell in the row
    for (let j = 0; j < row.length; j++) {
      const cell = row[j].toLowerCase();

      if (cell.includes('total tasks') || cell === 'total') {
        // Next cell or same row should have the value
        const valueCell = row[j + 1] || rows[i + 1]?.[j];
        if (valueCell) {
          totalTasks = parseInt(valueCell, 10) || 0;
        }
      } else if (cell === 'completed' || cell.includes('completed tasks')) {
        const valueCell = row[j + 1] || rows[i + 1]?.[j];
        if (valueCell) {
          completedTasks = parseInt(valueCell, 10) || 0;
        }
      } else if (cell === 'in progress' || cell.includes('in progress')) {
        const valueCell = row[j + 1] || rows[i + 1]?.[j];
        if (valueCell) {
          inProgressTasks = parseInt(valueCell, 10) || 0;
        }
      } else if (cell === 'to do' || cell === 'todo' || cell.includes('todo tasks')) {
        const valueCell = row[j + 1] || rows[i + 1]?.[j];
        if (valueCell) {
          todoTasks = parseInt(valueCell, 10) || 0;
        }
      } else if (cell === 'blocked' || cell.includes('blocked tasks')) {
        const valueCell = row[j + 1] || rows[i + 1]?.[j];
        if (valueCell) {
          blockedTasks = parseInt(valueCell, 10) || 0;
        }
      }
    }
  }

  return {
    totalTasks,
    completedTasks,
    inProgressTasks,
    todoTasks,
    blockedTasks,
  };
}

/**
 * Verify CSV contains expected headers
 */
export async function verifyCsvHeaders(
  filePath: string,
  expectedHeaders: string[]
): Promise<boolean> {
  const rows = await readCsvContent(filePath);
  
  if (rows.length === 0) {
    throw new Error('CSV file is empty');
  }
  
  const headers = rows[0];
  
  for (const expectedHeader of expectedHeaders) {
    const found = headers.some(header => 
      header.toLowerCase().includes(expectedHeader.toLowerCase())
    );
    
    if (!found) {
      throw new Error(
        `Expected header "${expectedHeader}" not found in CSV. Headers: ${headers.join(', ')}`
      );
    }
  }
  
  return true;
}

/**
 * Verify CSV contains data rows (not just headers)
 */
export async function verifyCsvHasData(filePath: string): Promise<boolean> {
  const rows = await readCsvContent(filePath);
  
  if (rows.length <= 1) {
    throw new Error('CSV file has no data rows (only headers or empty)');
  }
  
  return true;
}

/**
 * Get CSV row count (excluding header)
 */
export async function getCsvRowCount(filePath: string): Promise<number> {
  const rows = await readCsvContent(filePath);
  return rows.length > 0 ? rows.length - 1 : 0;
}

/**
 * Search for a value in CSV content
 */
export async function csvContains(filePath: string, searchValue: string): Promise<boolean> {
  const rows = await readCsvContent(filePath);
  
  for (const row of rows) {
    for (const cell of row) {
      if (cell.includes(searchValue)) {
        return true;
      }
    }
  }
  
  return false;
}

/**
 * Verify PDF metadata (basic validation)
 */
export async function verifyPdfMetadata(filePath: string): Promise<boolean> {
  if (!fs.existsSync(filePath)) {
    throw new Error(`PDF file does not exist: ${filePath}`);
  }
  
  const stats = fs.statSync(filePath);
  
  // Verify file is not empty
  if (stats.size === 0) {
    throw new Error('PDF file is empty');
  }
  
  // Verify file has PDF signature (basic check)
  const buffer = fs.readFileSync(filePath);
  const pdfSignature = buffer.toString('utf-8', 0, 5);
  
  if (!pdfSignature.startsWith('%PDF-')) {
    throw new Error('File does not appear to be a valid PDF (missing PDF signature)');
  }
  
  return true;
}

/**
 * Get file size in bytes
 */
export function getFileSize(filePath: string): number {
  if (!fs.existsSync(filePath)) {
    throw new Error(`File does not exist: ${filePath}`);
  }
  
  const stats = fs.statSync(filePath);
  return stats.size;
}

/**
 * Save download to specific directory
 */
export async function saveDownload(
  download: Download,
  targetDir: string,
  filename?: string
): Promise<string> {
  // Ensure directory exists
  if (!fs.existsSync(targetDir)) {
    fs.mkdirSync(targetDir, { recursive: true });
  }
  
  const suggestedFilename = filename || download.suggestedFilename();
  const targetPath = path.join(targetDir, suggestedFilename);
  
  await download.saveAs(targetPath);
  
  return targetPath;
}

/**
 * Clean up downloaded files in a directory
 */
export function cleanupDownloads(directory: string): void {
  if (!fs.existsSync(directory)) {
    return;
  }
  
  const files = fs.readdirSync(directory);
  
  for (const file of files) {
    const filePath = path.join(directory, file);
    try {
      fs.unlinkSync(filePath);
    } catch (error) {
      console.warn(`Failed to delete file ${filePath}:`, error);
    }
  }
}

/**
 * Delete a specific file
 */
export function deleteFile(filePath: string): void {
  if (fs.existsSync(filePath)) {
    try {
      fs.unlinkSync(filePath);
    } catch (error) {
      console.warn(`Failed to delete file ${filePath}:`, error);
    }
  }
}

/**
 * Verify download filename matches expected pattern
 */
export function verifyFilenamePattern(
  filename: string,
  expectedPattern: {
    contains?: string[];
    extension?: string;
    startsWith?: string;
    endsWith?: string;
  }
): boolean {
  if (expectedPattern.contains) {
    for (const substring of expectedPattern.contains) {
      if (!filename.includes(substring)) {
        throw new Error(
          `Filename "${filename}" does not contain expected substring "${substring}"`
        );
      }
    }
  }
  
  if (expectedPattern.extension) {
    if (!filename.endsWith(expectedPattern.extension)) {
      throw new Error(
        `Filename "${filename}" does not have expected extension "${expectedPattern.extension}"`
      );
    }
  }
  
  if (expectedPattern.startsWith) {
    if (!filename.startsWith(expectedPattern.startsWith)) {
      throw new Error(
        `Filename "${filename}" does not start with "${expectedPattern.startsWith}"`
      );
    }
  }
  
  if (expectedPattern.endsWith) {
    if (!filename.endsWith(expectedPattern.endsWith)) {
      throw new Error(
        `Filename "${filename}" does not end with "${expectedPattern.endsWith}"`
      );
    }
  }
  
  return true;
}

/**
 * Helper to handle download and verify in one call
 */
export async function downloadAndVerify(
  page: Page,
  action: () => Promise<void>,
  options: {
    format: 'pdf' | 'csv' | 'json';
    filenamePattern?: string | RegExp;
    saveToDir?: string;
    timeout?: number;
  }
): Promise<{ download: Download; path: string; filename: string }> {
  const download = options.filenamePattern
    ? await waitForDownloadWithFilename(page, action, options.filenamePattern, options.timeout)
    : await waitForDownload(page, action, options.timeout);
  
  const downloadPath = await verifyDownloadedFile(download, options.format);
  const filename = download.suggestedFilename();
  
  // Optionally save to specific directory
  let finalPath = downloadPath;
  if (options.saveToDir) {
    finalPath = await saveDownload(download, options.saveToDir, filename);
  }
  
  // Format-specific validation
  if (options.format === 'pdf') {
    await verifyPdfMetadata(finalPath);
  } else if (options.format === 'csv') {
    await verifyCsvHasData(finalPath);
  }
  
  return { download, path: finalPath, filename };
}


