"use client";
import { ProjectsList } from "@/components/projects-list";
import { useRouter } from "next/navigation";
import { SidebarInset, SidebarTrigger } from "@/components/ui/sidebar";
import { Separator } from "@/components/ui/separator";
import { FolderKanban } from "lucide-react";

export default function ProjectsPage() {
  const router = useRouter();
  const handleProjectSelect = (projectId: number) => {
    if (!Number.isNaN(projectId) && projectId >= 0) {
      router.push(`/projects/${projectId}`);
    } else {
      console.error('Invalid project ID:', projectId);
    }
  };

  return (
    <SidebarInset>
      <header className="flex h-16 shrink-0 items-center gap-2 border-b px-4">
        <SidebarTrigger className="-ml-1" />
        <Separator orientation="vertical" className="mr-2 h-4" />
        <div className="flex items-center gap-2">
          <FolderKanban className="h-5 w-5" />
          <span className="text-lg font-semibold">Projects</span>
        </div>
      </header>

      <div className="flex flex-1 flex-col gap-8 p-6 pb-12 lg:p-10">
        <ProjectsList onProjectSelect={handleProjectSelect} />
      </div>
    </SidebarInset>
  );
}
