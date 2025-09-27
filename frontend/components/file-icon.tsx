/**
 * File icon component for displaying file attachments with appropriate icons
 */

import {
  FileText,
  Image,
  FileSpreadsheet,
  Archive,
  Video,
  Music,
  File as FileIcon,
  Download
} from "lucide-react";
import { FileService, FileResponse } from "@/services/file-service";
import { cn } from "@/lib/utils";

interface FileIconProps {
  file: FileResponse;
  size?: 'sm' | 'md' | 'lg';
  showDownload?: boolean;
  className?: string;
}

const sizeClasses = {
  sm: 'h-4 w-4',
  md: 'h-6 w-6',
  lg: 'h-8 w-8'
};

const containerSizeClasses = {
  sm: 'h-6 w-6',
  md: 'h-8 w-8',
  lg: 'h-10 w-10'
};

export function FileIconComponent({ file, size = 'sm', showDownload = false, className }: FileIconProps) {
  const filename = file.fileUrl.split('/').pop() || 'file';
  const fileType = FileService.getFileType(filename);
  const extension = FileService.getFileExtension(filename);

  const getFileIcon = () => {
    // For image files, render the actual image
    if (fileType === 'image') {
      return (
        <img
          src={file.fileUrl}
          alt={filename}
          className={`${sizeClasses[size]} object-cover rounded-sm`}
          onError={(e) => {
            // Fallback to icon if image fails to load
            const target = e.target as HTMLImageElement;
            target.style.display = 'none';
            target.nextElementSibling?.classList.remove('hidden');
          }}
        />
      );
    }

    // For non-image files, show appropriate icons
    switch (fileType) {
      case 'document':
        return <FileText className={sizeClasses[size]} />;
      case 'spreadsheet':
        return <FileSpreadsheet className={sizeClasses[size]} />;
      case 'archive':
        return <Archive className={sizeClasses[size]} />;
      case 'video':
        return <Video className={sizeClasses[size]} />;
      case 'audio':
        return <Music className={sizeClasses[size]} />;
      default:
        return <FileIcon className={sizeClasses[size]} />;
    }
  };

  const getFileColor = () => {
    switch (fileType) {
      case 'image':
        return 'text-green-600 bg-green-50 hover:bg-green-100 border-green-200';
      case 'document':
        return 'text-blue-600 bg-blue-50 hover:bg-blue-100 border-blue-200';
      case 'spreadsheet':
        return 'text-emerald-600 bg-emerald-50 hover:bg-emerald-100 border-emerald-200';
      case 'archive':
        return 'text-purple-600 bg-purple-50 hover:bg-purple-100 border-purple-200';
      case 'video':
        return 'text-red-600 bg-red-50 hover:bg-red-100 border-red-200';
      case 'audio':
        return 'text-orange-600 bg-orange-50 hover:bg-orange-100 border-orange-200';
      default:
        return 'text-gray-600 bg-gray-50 hover:bg-gray-100 border-gray-200';
    }
  };

  const handleDownload = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    window.open(file.fileUrl, '_blank');
  };

  return (
    <div
      className={cn(
        'relative group flex items-center justify-center rounded-md border transition-colors cursor-pointer overflow-hidden',
        containerSizeClasses[size],
        getFileColor(),
        className
      )}
      title={`${filename} (${extension.toUpperCase()})`}
      onClick={handleDownload}
    >
      {getFileIcon()}

      {/* Fallback icon for images that fail to load */}
      {fileType === 'image' && (
        <Image className={`${sizeClasses[size]} hidden`} />
      )}

      {showDownload && (
        <div className="absolute -top-1 -right-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <div className="bg-white rounded-full p-0.5 shadow-sm border">
            <Download className="h-2.5 w-2.5 text-gray-600" />
          </div>
        </div>
      )}

      {size !== 'sm' && (
        <div className="absolute -bottom-1 left-1/2 transform -translate-x-1/2 opacity-0 group-hover:opacity-100 transition-opacity">
          <div className="bg-gray-900 text-white text-xs px-1 py-0.5 rounded text-center whitespace-nowrap">
            {extension.toUpperCase()}
          </div>
        </div>
      )}
    </div>
  );
}

interface FileListProps {
  files: FileResponse[];
  maxDisplay?: number;
  size?: 'sm' | 'md' | 'lg';
  showDownload?: boolean;
}

export function FileList({ files, maxDisplay = 5, size = 'sm', showDownload = false }: FileListProps) {
  const displayFiles = files.slice(0, maxDisplay);
  const remainingCount = Math.max(files.length - maxDisplay, 0);

  if (files.length === 0) {
    return null;
  }

  return (
    <div className="flex items-center gap-1 flex-wrap">
      {displayFiles.map((file) => (
        <FileIconComponent
          key={file.id}
          file={file}
          size={size}
          showDownload={showDownload}
        />
      ))}

      {remainingCount > 0 && (
        <div
          className={cn(
            'flex items-center justify-center rounded-md border border-dashed border-muted-foreground/50 bg-background text-muted-foreground text-xs font-medium',
            containerSizeClasses[size]
          )}
          title={`${remainingCount} more files`}
        >
          +{remainingCount}
        </div>
      )}
    </div>
  );
}
