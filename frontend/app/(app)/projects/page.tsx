"use client";
import { ProjectsList } from "@/components/projects-list";
import { useRouter } from "next/navigation";

export default function ProjectsPage() {
  const router = useRouter();
  const handleProjectSelect = (projectId: number) => {
    if (!Number.isNaN(projectId) && projectId > 0) {
      router.push(`/projects/${projectId}`);
    } else {
      console.error('Invalid project ID:', projectId);
    }
  };

  return (
      <ProjectsList onProjectSelect={handleProjectSelect} />
  );
}
