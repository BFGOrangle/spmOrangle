"use client";

import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { TaskResponse } from "@/services/project-service";
import { TaskCard } from "./task-card";

interface DraggableTaskCardProps {
  task: TaskResponse;
  onTaskUpdated?: (updatedTask: TaskResponse) => void;
  onTaskDeleted?: (taskId: number) => void;
}

export function DraggableTaskCard({ task, onTaskUpdated, onTaskDeleted }: DraggableTaskCardProps) {
  const {
    attributes,
    listeners,
    setNodeRef,
    transform,
    transition,
    isDragging,
  } = useSortable({ id: task.id });

  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    opacity: isDragging ? 0.5 : 1,
  };

  return (
    <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
      <TaskCard task={task} variant="board" onTaskUpdated={onTaskUpdated} onTaskDeleted={onTaskDeleted} />
    </div>
  );
}