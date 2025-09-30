import { useDroppable } from "@dnd-kit/core";
import { cn } from "@/lib/utils";

interface DroppableColumnProps {
  id: string;
  children: React.ReactNode;
  className?: string;
}

export function DroppableColumn({ id, children, className }: DroppableColumnProps) {
  const { isOver, setNodeRef } = useDroppable({
    id,
  });

  return (
    <div
      ref={setNodeRef}
      className={cn(
        "transition-colors",
        isOver && "ring-2 ring-primary ring-opacity-50",
        className
      )}
    >
      {children}
    </div>
  );
}