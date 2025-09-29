"use client";
import { ProjectsList } from "@/components/projects-list";
import { useRouter } from "next/navigation";

export default function ProjectsPage() {;
  const router = useRouter();
  const handleProjectSelect = (projectId: number) => {
    router.push(`/projects/${projectId}`)
  };

  return (
      <ProjectsList onProjectSelect={handleProjectSelect} />
  );
}
